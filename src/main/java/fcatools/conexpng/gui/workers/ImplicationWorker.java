package fcatools.conexpng.gui.workers;

import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.Implication;
import de.tudresden.inf.tcs.fcalib.ImplicationSet;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.StatusMessage;
import fcatools.conexpng.gui.dependencies.DependencyView;
import fcatools.conexpng.model.FormalContext;

/**
 * Worker to calculate implications.
 * 
 * @author Torsten Casselt
 */
public class ImplicationWorker extends AbstractWorker {

    private Conf state;
    private FormalContext context;
    private DependencyView view;
    // de.tudresden.inf.tcs.fcalib.ImplicationSet<String> doesn't return the
    // implications, so we need this result-variable, maybe we should modify
    // ImplicationSet
    private IndexedSet<FCAImplication<String>> result;

    /**
     * Creates the implication worker.
     * 
     * @param view
     *            needed to fetch information to work with from
     * @param progressBarId
     */
    public ImplicationWorker(DependencyView view, Long progressBarId) {
        super(progressBarId);
        this.view = view;
        this.state = view.getState();
        this.statusBar = state.getStatusBar();
        this.statusBar.setIndeterminate(progressBarId, true);
        this.context = state.context;
    }

    @Override
    protected Void doInBackground() throws Exception {

        state.startCalculation(StatusMessage.CALCULATINGIMPLICATIONS);
        setProgressBarMessage(StatusMessage.CALCULATINGIMPLICATIONS.toString());
        init();

        result = new ListSet<>();

        ImplicationSet<String> impl = new ImplicationSet<>(context);

        // Next-Closure

        Set<String> A = new ListSet<>();

        while (!A.equals(context.getAttributes())) {
            if (isCancelled()) {
                return null;
            }
            A = impl.nextClosure(A);
            if (A == null)
                break;
            if (!A.equals(context.doublePrime(A))) {
                Implication<String> im = new Implication<>(A, context.doublePrime(A));
                impl.add(im);
                result.add(im);
            }
        }
        // remove redundant items in the conclusion
        for (FCAImplication<String> fcaImplication : result) {
            if (isCancelled()) {
                return null;
            }
            fcaImplication.getConclusion().removeAll(fcaImplication.getPremise());
        }
        return null;
    }

    /*
     * executed in EDT so no computations here.
     */
    @Override
    protected void done() {
        state.endCalculation(StatusMessage.CALCULATINGIMPLICATIONS);
        if (!isCancelled()) {
            state.implications = result;
            view.writeImplications(0);
        }
        super.done();
    }

}
