package com.eugenkiss.conexp2.model;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public abstract class IAssociations extends Observable {

	private double minsup;
	private double confidence;
	private Set<AssociationRule> rules;

	public abstract boolean computeAssociationRules();

	@Override
	public abstract void notifyObservers();

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
