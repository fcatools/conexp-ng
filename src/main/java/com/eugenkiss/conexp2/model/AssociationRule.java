package com.eugenkiss.conexp2.model;

public abstract class AssociationRule {

    private String premisse;
    private String consequent;
    private double sup;
    private double conf;

    public AssociationRule(String premisse, String consequent, double sup,
            double conf) {
        super();
        this.premisse = premisse;
        this.consequent = consequent;
        this.sup = sup;
        this.conf = conf;
    }

    public String getPremisse() {
        return premisse;
    }

    public void setPremisse(String premisse) {
        this.premisse = premisse;
    }

    public String getConsequent() {
        return consequent;
    }

    public void setConsequent(String consequent) {
        this.consequent = consequent;
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

}
