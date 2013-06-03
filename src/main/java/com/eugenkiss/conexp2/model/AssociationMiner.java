package com.eugenkiss.conexp2.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

public class AssociationMiner extends IAssociations {

    private IndexedSet<Set<SortedSet<String>>> FC;

    public AssociationMiner(FormalContext context) {
        super(context);
    }

    public AssociationMiner(FormalContext context, double minsup, double conf) {
        super(context);
        setMinsup(minsup);
        setConfidence(conf);
    }

    @Override
    public Set<AssociationRule> computeAssociationRules() {
        Set<AssociationRule> result = new TreeSet<AssociationRule>();
        FC = new ListSet<>();
        apriori();
        Gen_LB(result);
        return result;
    }

    /**
     * from Stumme et al. 2001
     *
     * @param result
     */
    private void Gen_LB(Set<AssociationRule> result) {
        List<Set<SortedSet<String>>> S = new ArrayList<>();
        int supL;
        for (int i = 1; i < FC.size(); i++) {
            double conf;
            for (SortedSet<String> L : FC.getElementAt(i)) {

                S = new ArrayList<>();

                supL = context.supportCount(L);

                for (int j = 0; j < i; j++) {
                    S.add(subsets(FC.getElementAt(j), L));
                }

                for (int j = i - 1; j >= 0; j--) {
                    for (SortedSet<String> L_prime : S.get(j)) {
                        conf = supL / (double) context.supportCount(L_prime);
                        if (conf >= getConfidence()) {
                            result.add(new AssociationRule(L_prime, minus(L,
                                    L_prime), supL
                                    / (double) context.getObjectCount(), conf));
                        }
                        for (int l = j; l >= 0; l--) {
                            S.set(l,
                                    minus(S.get(l), subsets(S.get(l), L_prime)));
                        }
                    }
                }
            }
        }
    }

    private Set<SortedSet<String>> minus(Set<SortedSet<String>> a,
            Set<SortedSet<String>> b) {
        Set<SortedSet<String>> result = new ListSet<>();
        Iterator<SortedSet<String>> ita = a.iterator();
        SortedSet<String> temp_a;
        while (ita.hasNext()) {
            temp_a = ita.next();
            if (!b.contains(temp_a)) {
                result.add(temp_a);
            }
        }
        return result;
    }

    /**
     *
     * @param superset
     * @param someElements
     * @return all the elemts of someElements which are subsets of superset
     */
    private Set<SortedSet<String>> subsets(Set<SortedSet<String>> someElements,
            SortedSet<String> superset) {

        Set<SortedSet<String>> result = new ListSet<>();
        for (SortedSet<String> elements : someElements) {
            if (superset.containsAll(elements))
                result.add(elements);
        }
        System.out.println(someElements + " - " + superset + " = " + result);
        return result;
    }

    private void apriori() {
        IndexedSet<SortedSet<String>> candidates = new ListSet<>();
        SortedSet<String> temp1;
        int k = 1;
        // find all frequent 1-itemsets
        for (String attribut : context.getAttributes()) {
            temp1 = new TreeSet<>();
            temp1.add(attribut);
            if (context.supportCount(temp1) >= getMinsup()
                    * context.getObjectCount())
                candidates.add(temp1);
        }
        IndexedSet<SortedSet<String>> temp_k = new ListSet<>();
        IndexedSet<SortedSet<String>> temp_km1 = new ListSet<>();
        for (SortedSet<String> sortedSet : candidates) {
            temp_k.add(sortedSet);
        }
        // check if the empty set is closed
        checkIfSetsK_1AreClosed(temp_km1, temp_k);
        temp_km1 = temp_k;
        while (!temp_k.isEmpty()) {
            k++;
            temp_k = new ListSet<>();
            candidates = apriori_gen(temp_km1, k);
            for (SortedSet<String> candidate : candidates) {
                if (context.supportCount(candidate) >= getMinsup()
                        * context.getObjectCount())
                    temp_k.add(candidate);
            }
            checkIfSetsK_1AreClosed(temp_km1, temp_k);
            temp_km1 = temp_k;
        }
    }

    /**
     *
     * @param fk_1
     * @param k
     * @return canditates for a frequent itemset
     */
    private IndexedSet<SortedSet<String>> apriori_gen(
            IndexedSet<SortedSet<String>> fk_1, int k) {
        IndexedSet<SortedSet<String>> result = new ListSet<>();
        TreeSet<String> temp;
        if (k == 2) {
            for (int i = 0; i < fk_1.size() - 1; i++) {
                for (int j = i + 1; j < fk_1.size(); j++) {
                    temp = new TreeSet<>();
                    temp.add(fk_1.getElementAt(i).first());
                    temp.add(fk_1.getElementAt(j).first());
                    result.add(temp);
                }
            }
        } else {
            for (int i = 0; i < fk_1.size() - 1; i++) {
                for (int j = i + 1; j < fk_1.size(); j++) {
                    SortedSet<String> join = equijoin(fk_1.getElementAt(i),
                            fk_1.getElementAt(j));
                    if (join != null) {
                        result.add(join);
                    }
                }
            }
        }
        return result;
    }

    /**
     * a set is closed if no superset has the same support
     *
     * @param levelKm1
     * @param levelK
     */
    private void checkIfSetsK_1AreClosed(Set<SortedSet<String>> levelKm1,
            Set<SortedSet<String>> levelK) {
        Set<SortedSet<String>> closed = new ListSet<>();
        boolean isClosed = true;
        // is the empty set closed?
        if (levelKm1.isEmpty()) {
            SortedSet<String> emptySet = new TreeSet<>();
            int supportcount = context.supportCount(emptySet);
            for (SortedSet<String> k : levelK) {
                if (supportcount == context.supportCount(k)) {
                    isClosed = false;
                    break;
                }
            }
            if (isClosed) {
                closed.add(emptySet);
            }
        }
        for (SortedSet<String> km1 : levelKm1) {
            isClosed = true;
            int supportcount = context.supportCount(km1);
            for (SortedSet<String> k : levelK) {
                if (supportcount == context.supportCount(k)
                        && k.containsAll(km1)) {
                    isClosed = false;
                    break;
                }
            }
            if (isClosed) {
                closed.add(km1);
            }
        }
        FC.add(closed);
    }

    // Set-Operations

    /***
     *
     * Without Pruning, if the join is a superset of a non-frequent-itemset
     *
     * @param a
     * @param b
     * @return null, if a and b are not equal in their first k-1 elements
     */
    private SortedSet<String> equijoin(SortedSet<String> a, SortedSet<String> b) {
        Iterator<String> ita = a.iterator();
        Iterator<String> itb = b.iterator();
        TreeSet<String> result = new TreeSet<>();
        String temp_a;
        int k = a.size() - 1;
        while (0 < k) {
            temp_a = ita.next();
            if (!temp_a.equals(itb.next()))
                return null;
            else
                result.add(temp_a);
            k--;
        }
        result.add(ita.next());
        result.add(itb.next());
        return result;
    }

    private SortedSet<String> minus(SortedSet<String> a, SortedSet<String> b) {
        SortedSet<String> result = new TreeSet<>();
        Iterator<String> ita = a.iterator();
        String temp_a;
        while (ita.hasNext()) {
            temp_a = ita.next();
            if (!b.contains(temp_a))
                result.add(temp_a);
        }

        return result;
    }

    /**
     * Data-Mining-algorithm, computes not a base of associations
     *
     * @param fk
     * @param Hm
     * @param result
     */

    @SuppressWarnings("unused")
    private void ap_genrules(SortedSet<String> fk,
            IndexedSet<SortedSet<String>> Hm, Set<AssociationRule> result) {
        double conf = 0;
        SortedSet<String> fk_h;
        int k = fk.size();
        int m = Hm.isEmpty() ? 0 : Hm.getElementAt(0).size();
        if (k > m + 1) {
            if (m == 0) {
                TreeSet<String> temp;
                for (String i : fk) {
                    temp = new TreeSet<>();
                    temp.add(i);
                    Hm.add(temp);
                }
            } else {
                Hm = apriori_gen(Hm, m + 1);
            }
            for (int i = 0; i < Hm.size(); i++) {
                fk_h = minus(fk, Hm.getElementAt(i));
                conf = context.supportCount(fk)
                        / (double) context.supportCount(fk_h);
                if (conf >= getConfidence())
                    result.add(new AssociationRule(fk_h, Hm.getElementAt(i),
                            context.supportCount(fk)
                                    / (double) context.getObjectCount(), conf));
                else {
                    Hm.remove(Hm.getElementAt(i));
                    i--;
                }
            }
        }
    }

}
