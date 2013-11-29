package fcatools.conexpng.gui.workers;

import java.util.ArrayList;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.StatusMessage;
import fcatools.conexpng.gui.contexteditor.ContextMatrix;
import fcatools.conexpng.gui.contexteditor.ContextMatrixModel;
import fcatools.conexpng.model.FormalContext;

/**
 * Worker to clarify and/or reduce objects and attributes of a context.
 * 
 * @author Torsten Casselt
 */
public class ClarificationReductionWorker extends AbstractWorker {

    private Conf state;
    private FormalContext context;
    private ContextMatrix matrix;
    private boolean type, reduction;
    private boolean contextReduction;

    /**
     * Creates the worker.
     * 
     * @param state
     *            needed to fetch context and status bar from
     * @param matrix
     *            context matrix to update
     * @param progressBarId
     *            id of progress bar for this calculation
     * @param type
     *            true if objects shall be clarified, false if attributes
     * @param reduction
     *            true if reduction shall be made, false if not
     * @param contextReduction
     *            true if the whole context shall be reduced, false if not
     */
    public ClarificationReductionWorker(Conf state, ContextMatrix matrix, Long progressBarId, boolean type,
            boolean reduction, boolean contextReduction) {
        super(progressBarId);
        this.state = state;
        this.statusBar = state.getStatusBar();
        this.statusBar.setIndeterminate(progressBarId, false);
        this.context = state.context;
        this.matrix = matrix;
        this.type = type;
        this.reduction = reduction;
        this.contextReduction = contextReduction;
    }

    /**
     * Clarifies objects/attributes.
     */
    private void clarify() {
        // start calculation and set progress bar
        state.startCalculation(type ? StatusMessage.CLARIFYINGOBJECTS : contextReduction ? StatusMessage.CLARIFYING
                : StatusMessage.CLARIFYINGATTRIBUTES);
        setProgressBarMessage(type ? StatusMessage.CLARIFYINGOBJECTS.toString()
                : contextReduction ? StatusMessage.CLARIFYING.toString() : StatusMessage.CLARIFYINGATTRIBUTES
                        .toString());
        int progress = 0;
        setProgress(0);
        // start algorithm
        IndexedSet<FullObject<String, String>> objects = context.getObjects();
        ArrayList<FullObject<String, String>> toBeRemoved = new ArrayList<>();
        for (int i = 0; i < context.getObjectCount(); i++) {
            FullObject<String, String> o1 = objects.getElementAt(i);
            for (int j = i + 1; j < context.getObjectCount(); j++) {
                FullObject<String, String> o2 = objects.getElementAt(j);
                if (context.getAttributesForObject(o1.getIdentifier()).equals(
                        context.getAttributesForObject(o2.getIdentifier()))) {
                    toBeRemoved.add(o2);
                }
            }
            setProgress((int) (((float) progress++ / context.getObjectCount()) * 100));
        }
        for (FullObject<String, String> o : toBeRemoved) {
            context.removeObjectOnly(o);
        }
        // end calculation
        state.endCalculation(type ? StatusMessage.CLARIFYINGOBJECTS : contextReduction ? StatusMessage.CLARIFYING
                : StatusMessage.CLARIFYINGATTRIBUTES);
    }

    /**
     * Reduces objects/attributes.
     */
    public void reduce() {
        // start calculation and set progress bar
        state.startCalculation(type ? StatusMessage.REDUCINGOBJECTS : contextReduction ? StatusMessage.REDUCING
                : StatusMessage.REDUCINGATTRIBUTES);
        setProgressBarMessage(type ? StatusMessage.REDUCINGOBJECTS.toString()
                : contextReduction ? StatusMessage.REDUCING.toString() : StatusMessage.REDUCINGATTRIBUTES.toString());
        int progress = 0;
        setProgress(0);
        // original comment of creator:
        // I have just implemented the functionality as I understood it by
        // reading
        // the documentation and playing around with the original ConExp. It
        // seems
        // to be correct. The code can probably be written in a much more
        // efficient
        // way, though.
        IndexedSet<FullObject<String, String>> objects = context.getObjects();
        ArrayList<FullObject<String, String>> toBeRemoved = new ArrayList<>();
        for (int i = 0; i < context.getObjectCount(); i++) {
            FullObject<String, String> o = objects.getElementAt(i);
            IndexedSet<FullObject<String, String>> otherObjects0 = new ListSet<>();
            for (FullObject<String, String> o0 : objects) {
                otherObjects0.add(o0);
            }
            otherObjects0.remove(o);
            for (FullObject<String, String> o0 : toBeRemoved) {
                otherObjects0.remove(o0);
            }
            ICombinatoricsVector<FullObject<String, String>> otherObjects = Factory.createVector(otherObjects0);
            Generator<FullObject<String, String>> gen = Factory.createSubSetGenerator(otherObjects);
            for (ICombinatoricsVector<FullObject<String, String>> subSet : gen) {
                if (subSet.getSize() < 2)
                    continue;
                IndexedSet<String> intersection = new ListSet<>();
                for (String attribute : context.getAttributesForObject(subSet.getValue(0).getIdentifier())) {
                    intersection.add(attribute);
                }
                for (int j = 1; j < subSet.getSize(); j++) {
                    intersection.retainAll(context.getAttributesForObject(subSet.getValue(j).getIdentifier()));
                }
                if (intersection.size() == 0)
                    continue;
                if (context.getAttributesForObject(o.getIdentifier()).equals(intersection)) {
                    toBeRemoved.add(o);
                    break;
                }
            }
            setProgress((int) (((float) progress++ / context.getObjectCount()) * 100));
        }
        for (FullObject<String, String> o : toBeRemoved) {
            context.removeObjectOnly(o);
        }
        // end calculation
        state.endCalculation(type ? StatusMessage.REDUCINGOBJECTS : contextReduction ? StatusMessage.REDUCING
                : StatusMessage.REDUCINGATTRIBUTES);
    }

    @Override
    protected Void doInBackground() throws Exception {
        init();

        // if reduction, save state first
        if (reduction || contextReduction) {
            state.saveConf();
        }
        // transpose matrix if attributes shall be handled
        if (!type && !contextReduction) {
            context.transpose();
        }
        // clarification
        clarify();
        // reduction if needed
        if (reduction || contextReduction) {
            reduce();
        }
        // transpose matrix back if attributes were handled
        // or whole context is reduced
        if (!type || contextReduction) {
            context.transpose();
        }
        // if whole context is reduced, reduce again and transpose
        if (contextReduction) {
            clarify();
            reduce();
            context.transpose();
        }
        return null;
    }

    /*
     * executed in EDT so no computations here.
     */
    @Override
    protected void done() {
        // if not cancelled finish the operation
        if (!isCancelled()) {
            ((ContextMatrixModel) matrix.getModel()).fireTableStructureChanged();
            matrix.clearSelection();
            matrix.saveSelection();
            state.contextChanged();
            if (reduction) {
                state.makeRedoable();
            }
        }
        super.done();
    }

}
