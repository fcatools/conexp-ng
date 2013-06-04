package com.eugenkiss.conexp2.model;

import java.util.Set;

public abstract class AbstractAssociationMiner {

    private double minsup;
    private double confidence;
    private Set<AssociationRule> rules;
    protected FormalContext context;

    public abstract Set<AssociationRule> computeAssociationRules();

    public AbstractAssociationMiner(FormalContext context) {
        this.context = context;
    }

    public Set<AssociationRule> getRules() {
        return rules;
    }

    public void setRules(Set<AssociationRule> rules) {
        this.rules = rules;
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
