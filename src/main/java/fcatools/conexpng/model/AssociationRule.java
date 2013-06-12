package fcatools.conexpng.model;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class AssociationRule implements Comparable<AssociationRule> {

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

    public double getSupport() {
        return sup;
    }

    public void setSupport(double sup) {
        this.sup = sup;
    }

    public double getConfidence() {
        return conf;
    }

    public void setConfidence(double conf) {
        this.conf = conf;
    }

    public boolean equals(AssociationRule ass) {
        return premise.equals(ass.getPremise())
                && consequent.equals(ass.getConsequent());
    }

    public String toString() {
        return "<" + sup + "> " + premise + " [" + conf + "]-> " + consequent;
    }

    @Override
    public int compareTo(AssociationRule arg0) {
        if (equals(arg0))
            return 0;
        if (premise.isEmpty()) {
            if (arg0.premise.isEmpty())
                return compare(consequent, arg0.consequent);
            else
                return -1;
        } else if (arg0.premise.isEmpty())
            return 1;
        else if (!premise.first().equals(arg0.premise.first()))
            return premise.first().compareTo(arg0.premise.first());
        else {
            if (premise.equals(arg0.premise))
                return compare(consequent, arg0.consequent);
            else
                return compare(premise, arg0.premise);
        }
    }

    private int compare(SortedSet<String> set, SortedSet<String> set2) {
        Iterator<String> it = set.iterator();
        Iterator<String> it2 = set2.iterator();
        String s, s2;

        while (it.hasNext() && it2.hasNext()) {
            s = it.next();
            s2 = it2.next();
            if (!s.equals(s2))
                return s.compareTo(s2);
        }

        return it.hasNext() ? 1 : -1;
    }

}
