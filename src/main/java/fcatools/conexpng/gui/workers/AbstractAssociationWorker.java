package fcatools.conexpng.gui.workers;

import java.util.Set;

import fcatools.conexpng.model.AssociationRule;
import fcatools.conexpng.model.FormalContext;

public abstract class AbstractAssociationWorker extends AbstractWorker {

	protected double minsup;
	protected double confidence;
	protected Set<AssociationRule> rules;
	protected FormalContext context;

	public AbstractAssociationWorker(FormalContext context, Long progressBarId) {
		super(progressBarId);
		this.context = context;
	}

	public Set<AssociationRule> getRules() {
		return rules;
	}

	public double getMinsup() {
		return minsup;
	}

	public void setMinsup(double minsup) {
		this.minsup = minsup;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

}
