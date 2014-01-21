package fcatools.conexpng.gui.workers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.StatusMessage;
import fcatools.conexpng.gui.lattice.LatticeSettings;
import fcatools.conexpng.gui.lattice.LatticeView;
import fcatools.conexpng.model.FormalContext;
import fcatools.conexpng.model.LatticeConcept;

/**
 * Worker to calculate concepts and lattice.
 * 
 * @author Torsten Casselt
 */
public class ConceptWorker extends AbstractWorker {

    private Conf state;
    private FormalContext context;
    private boolean lattice;
    private LatticeView view;
    private ListSet<Concept<String, FullObject<String, String>>> conceptLattice;

    /**
     * Creates the concept worker.
     * 
     * @param view
     *            needed to fetch information to work with from
     * @param lattice
     *            true if the lattice shall be computed, false if only the
     *            concepts
     * @param progressBarId
     */
    public ConceptWorker(LatticeView view, boolean lattice, Long progressBarId) {
        super(progressBarId);
        this.view = view;
        this.state = view.getState();
        this.statusBar = state.getStatusBar();
        this.statusBar.setIndeterminate(progressBarId, false);
        this.context = state.context;
        this.lattice = lattice;
    }

    @Override
    protected Void doInBackground() throws Exception {

        conceptLattice = new ListSet<Concept<String, FullObject<String, String>>>();
        HashMap<String, Set<String>> extentPerAttr = new HashMap<String, Set<String>>();
        int progress = 0;

        state.startCalculation(StatusMessage.CALCULATINGCONCEPTS);
        setProgressBarMessage(StatusMessage.CALCULATINGCONCEPTS.toString());
        init();

        /*
         * Step 1: Initialize a list of concept extents. To begin with, write
         * for each attribute m # M the attribute extent {m}$ to this list (if
         * not already present).
         */
        for (String s : context.getAttributes()) {
            if (!context.getDontConsideredAttr().contains(s)) {
                TreeSet<String> set = new TreeSet<String>();
                for (FullObject<String, String> f : context.getObjects()) {
                    if (f.getDescription().getAttributes().contains(s) && (!context.getDontConsideredObj().contains(f))) {
                        set.add(f.getIdentifier());
                    }
                }
                extentPerAttr.put(s, set);
            }
        }
        // checks if the worker is cancelled
        if (isCancelled()) {
            return null;
        }
        /*
         * Step 2: For any two sets in this list, compute their intersection. If
         * the result is a set that is not yet in the list, then extend the list
         * by this set. With the extended list, continue to build all pairwise
         * intersections.
         */
        HashMap<String, Set<String>> temp = new HashMap<String, Set<String>>();
        for (String s : extentPerAttr.keySet()) {
            for (String t : extentPerAttr.keySet()) {
                if (!s.equals(t)) {
                    Set<String> result = context.intersection(extentPerAttr.get(s), extentPerAttr.get(t));
                    if (!extentPerAttr.values().contains(result)) {
                        if (!temp.containsValue(result)) {
                            temp.put(s + ", " + t, result);
                        }
                    }
                }
            }
            setProgress((int) (((float) progress++ / extentPerAttr.keySet().size()) * 99));
        }
        extentPerAttr.putAll(temp);
        // checks if the worker is cancelled
        if (isCancelled()) {
            return null;
        }
        /*
         * Step 3: If for any two sets of the list their intersection is also in
         * the list, then extend the list by the set G (provided it is not yet
         * contained in the list). The list then contains all concept extents
         * (and nothing else).
         */
        boolean notcontained = false;
        for (String s : extentPerAttr.keySet()) {
            if (notcontained)
                break;
            for (String t : extentPerAttr.keySet()) {
                if (!s.equals(t)) {
                    Set<String> result = context.intersection(extentPerAttr.get(s), extentPerAttr.get(t));
                    if (!extentPerAttr.values().contains(result)) {
                        notcontained = true;
                        break;
                    }
                }
            }
        }
        if (!notcontained) {
            TreeSet<String> set = new TreeSet<String>();
            for (FullObject<String, String> f : context.getObjects()) {
                set.add(f.getIdentifier());
            }
            if (!extentPerAttr.values().contains(set))
                extentPerAttr.put("", set);
        }
        // checks if the worker is cancelled
        if (isCancelled()) {
            return null;
        }
        /*
         * Step 4: For every concept extent A in the list compute the
         * corresponding intent A' to obtain a list of all formal concepts
         * (A,A') of (G,M, I).
         */

        HashSet<Set<String>> extents = new HashSet<Set<String>>();
        for (Set<String> e : extentPerAttr.values()) {
            if (!extents.contains(e))
                extents.add(e);
        }
        for (Set<String> e : extents) {
            TreeSet<String> intents = new TreeSet<String>();
            int count = 0;
            Concept<String, FullObject<String, String>> c = new LatticeConcept();
            if (e.isEmpty()) {
                intents.addAll(context.getAttributes());
            } else
                for (FullObject<String, String> i : context.getObjects()) {
                    if (!context.getDontConsideredObj().contains(i)) {
                        if (e.contains(i.getIdentifier().toString())) {
                            TreeSet<String> prev = context.sort(i.getDescription().getAttributes());
                            if (count > 0) {
                                intents = context.intersection(prev, intents);
                            } else {
                                intents = prev;
                            }
                            count++;
                            c.getExtent().add(i);
                        }
                    }
                }
            // checks if the worker is cancelled
            if (isCancelled()) {
                return null;
            }
            for (String s : intents) {
                if (!context.getDontConsideredAttr().contains(s))
                    c.getIntent().add(s);
            }
            conceptLattice.add(c);
        }
        return null;
    }

    /*
     * executed in EDT so no computations here.
     */
    @Override
    protected void done() {
        state.endCalculation(StatusMessage.CALCULATINGCONCEPTS);
        // if not cancelled finish the operation
        if (!isCancelled()) {
            state.concepts = conceptLattice;
            if (lattice) {
                state.startCalculation(StatusMessage.CALCULATINGLATTICE);
                setProgress(99);
                setProgressBarMessage(StatusMessage.CALCULATINGLATTICE.toString());
                state.lattice = view.getAlg().computeLatticeGraph(state.concepts, view.getBounds());
            } else {
                state.lattice.addEdges(state.concepts);
            }
            view.updateLatticeGraph();
            if (lattice) {
                ((LatticeSettings) view.getSettings()).update(state);
                setProgress(100);
                state.endCalculation(StatusMessage.CALCULATINGLATTICE);
            }
        }
        super.done();
    }
}
