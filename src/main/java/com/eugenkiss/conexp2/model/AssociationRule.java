package com.eugenkiss.conexp2.model;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class AssociationRule {

    private SortedSet<String> premise;
    private SortedSet<String> consequent;
    private double sup;
    private double conf;

    public AssociationRule() {
        premise = new TreeSet<>();
        consequent = new TreeSet<>();
    }

    public AssociationRule(Set<String> premise, Set<String> consequent,
            double sup, double conf) {
        this();
        this.premise.addAll(premise);
        this.consequent.addAll(consequent);
        this.sup = sup;
        this.conf = conf;
    }

    public Set<String> getPremise() {
        return premise;
    }

    public void setPremise(Set<String> premise) {
        this.premise.clear();
        this.premise.addAll(premise);
    }

    public Set<String> getConsequent() {
        return consequent;
    }

    public void setConsequent(Set<String> consequent) {
        this.consequent.clear();
        this.consequent.addAll(consequent);
    }

    public double getSup() {
        return sup;
    }

    public void setSup(double sup) {
        this.sup = sup;
    }

    public double getConf() {
        return conf;
    }

    public void setConf(double conf) {
        this.conf = conf;
    }

    public boolean equals(AssociationRule ass) {
        return premise.equals(ass.getPremise())
                && consequent.equals(ass.getConsequent());
    }

    public String toString() {
        return premise + " -> " + consequent;
    }

}
