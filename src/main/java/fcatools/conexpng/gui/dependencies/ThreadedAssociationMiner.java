package fcatools.conexpng.gui.dependencies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.model.AbstractAssociationMiner;
import fcatools.conexpng.model.AssociationRule;
import fcatools.conexpng.model.FormalContext;

public class ThreadedAssociationMiner extends AbstractAssociationMiner implements Runnable {

    // frequent and closed itemsets
    private IndexedSet<Set<SortedSet<String>>> FC;

    private Set<AssociationRule> result;

    public Set<AssociationRule> getResult() {
        return result;
    }

    public ThreadedAssociationMiner(FormalContext context) {
        super(context);
    }

    public ThreadedAssociationMiner(FormalContext context, double minsup, double conf) {
        super(context);
        setMinsup(minsup);
        setConfidence(conf);
    }

    @Override
    public Set<AssociationRule> computeAssociationRules() {
        result = new TreeSet<AssociationRule>();
        FC = new ListSet<>();
        apriori();
        Gen_LB();
        return result;
    }

    /**
     * from Stumme et al. 2001
     */
    private void Gen_LB() {
        List<Set<SortedSet<String>>> S = new ArrayList<>();
        int supL;
        for (int i = 1; i < FC.size(); i++) {
            if (Thread.interrupted()) {
                return;
            }
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
                            result.add(new AssociationRule(L_prime, minus(L, L_prime), supL
                                    / (double) context.getObjectCount(), conf));
                        }

                        for (int l = j; l >= 0; l--) {
                            S.set(l, minus(S.get(l), subsets(S.get(l), L_prime)));
                        }
                    }
                }
            }
        }
    }

    private void apriori() {
        IndexedSet<SortedSet<String>> candidates = new ListSet<>();
        SortedSet<String> temp1;
        int k = 1;
        // find all frequent 1-itemsets
        for (String attribut : context.getAttributes()) {
            temp1 = new TreeSet<>();
            temp1.add(attribut);
            if (context.supportCount(temp1) >= getMinsup() * context.getObjectCount())
                candidates.add(temp1);
        }
        IndexedSet<SortedSet<String>> temp_k = new ListSet<>();
        IndexedSet<SortedSet<String>> temp_km1 = new ListSet<>();
        for (SortedSet<String> sortedSet : candidates) {
            temp_k.add(sortedSet);
        }
        // check if the empty set is closed
        checkIfSetsKMinus1AreClosed(temp_km1, temp_k);
        temp_km1 = temp_k;
        while (!temp_km1.isEmpty()) {
            if (Thread.interrupted()) {
                return;
            }
            k++;
            temp_k = new ListSet<>();
            candidates = apriori_gen(temp_km1, k);
            // check if the candidates have the given minimal support
            for (SortedSet<String> candidate : candidates) {
                if (context.supportCount(candidate) >= getMinsup() * context.getObjectCount())
                    temp_k.add(candidate);
            }
            checkIfSetsKMinus1AreClosed(temp_km1, temp_k);
            temp_km1 = temp_k;
        }
    }

    /**
     * a set is closed if no superset has the same support
     *
     * @param levelKm1
     * @param levelK
     */
    private void checkIfSetsKMinus1AreClosed(Set<SortedSet<String>> levelKm1, Set<SortedSet<String>> levelK) {
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
        // check for every frequent itemset if a superset has the same support
        for (SortedSet<String> km1 : levelKm1) {
            isClosed = true;
            int supportcount = context.supportCount(km1);
            for (SortedSet<String> k : levelK) {
                if (supportcount == context.supportCount(k) && k.containsAll(km1)) {
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

    /**
     *
     * @param fk_1
     *            frequent itemsets one step before (length k-1)
     * @param k
     *            the length of itemsets which should computed
     * @return canditates for a frequent itemset
     */
    private IndexedSet<SortedSet<String>> apriori_gen(IndexedSet<SortedSet<String>> fk_1, int k) {
        IndexedSet<SortedSet<String>> result = new ListSet<>();
        TreeSet<String> temp;
        if (k == 2) {
            // just merge two sets with one element
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
                    SortedSet<String> join = equijoin(fk_1.getElementAt(i), fk_1.getElementAt(j));
                    if (join != null) {
                        result.add(join);
                    }
                }
            }
        }
        return result;
    }

    // Set-Operations

    /***
     *
     * Without Pruning, if the join is a superset of a non-frequent-itemset
     *
     * @param a
     *            a set with k elements
     * @param b
     *            another with k elements
     * @return {@code null}, if a and b are not equal in their first k-1
     *         elements
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

    @SuppressWarnings("unchecked")
    private <K, T extends Set<K>> T minus(T a, T b) {
        T result = (T) ((a instanceof SortedSet<?>) ? new TreeSet<>() : new ListSet<>());
        Iterator<K> ita = a.iterator();
        K temp_a;

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
     * @param someSets
     * @return all the sets of someSets which are subsets of superset
     */
    private Set<SortedSet<String>> subsets(Set<SortedSet<String>> someSets, SortedSet<String> superset) {

        Set<SortedSet<String>> result = new ListSet<>();

        for (SortedSet<String> elements : someSets) {
            if (superset.containsAll(elements))
                result.add(elements);
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
    private void ap_genrules(SortedSet<String> fk, IndexedSet<SortedSet<String>> Hm, Set<AssociationRule> result) {
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
                conf = context.supportCount(fk) / (double) context.supportCount(fk_h);
                if (conf >= getConfidence())
                    result.add(new AssociationRule(fk_h, Hm.getElementAt(i), context.supportCount(fk)
                            / (double) context.getObjectCount(), conf));
                else {
                    Hm.remove(Hm.getElementAt(i));
                    i--;
                }
            }
        }
    }

    @Override
    public void run() {
        result = new TreeSet<AssociationRule>();
        FC = new ListSet<>();
        apriori();
        Gen_LB();
    }
}