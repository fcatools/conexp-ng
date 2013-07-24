package fcatools.conexpng.model;

import java.util.Set;

public abstract class AbstractAssociationMiner {

    protected double minsup;
    protected double confidence;
    protected Set<AssociationRule> rules;
    protected FormalContext context;

    public abstract Set<AssociationRule> computeAssociationRules();

    public AbstractAssociationMiner(FormalContext context) {
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
