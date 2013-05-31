package com.eugenkiss.conexp2.model;

import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.Implication;
import de.tudresden.inf.tcs.fcalib.ImplicationSet;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

/**
 * A specialization of FormalContext<String,String> with the aim to remove the
 * verbose repetition of <String,String>. Plus, adds a couple of useful methods.
 */
public class FormalContext extends
        de.tudresden.inf.tcs.fcalib.FormalContext<String, String> {

    public FormalContext() {
        super();
    }

    public int supportCount(Set<String> attributes) {
        int count = 0;
        boolean notfound;
        for (FullObject<String, String> obj : objects) {
            notfound = false;
            for (String att : attributes) {
                if (!objectHasAttribute(obj, att)) {
                    notfound = true;
                    break;
                }
            }
            if (!notfound)
                count++;
        }
        return count;

    }

    @Override
    public Set<FCAImplication<String>> getStemBase() {
        // de.tudresden.inf.tcs.fcalib.ImplicationSet<String> doesn't return the
        // implications, so we need this result-variable, maybe we should modify
        // ImplicationSet
        IndexedSet<FCAImplication<String>> result = new ListSet<>();

        ImplicationSet<String> impl = new ImplicationSet<>(this);

        // Next-Closure fca.04 implications page 14

        Set<String> A = new ListSet<>();

        while (!A.equals(getAttributes())) {
            A = impl.nextClosure(A);
            if (!A.equals(doublePrime(A))) {
                Implication<String> im = new Implication<>(A, doublePrime(A));
                impl.add(im);
                result.add(im);
            }
        }

        return andNot(result);
    }

    /**
     * Removes the elements of the conclusion which are also in the premise for
     * all given implications
     *
     * @param implications
     * @return
     */
    private Set<FCAImplication<String>> andNot(
            Set<FCAImplication<String>> implications) {
        Set<FCAImplication<String>> result = new ListSet<>();
        Set<String> conclusion;
        for (FCAImplication<String> fcaImplication : implications) {
            conclusion = fcaImplication.getConclusion();
            for (String premise : fcaImplication.getPremise()) {
                if (fcaImplication.getConclusion().contains(premise)) {
                    conclusion.remove(premise);
                }
            }
            result.add(new Implication<>(fcaImplication.getPremise(),
                    conclusion));
        }
        return result;
    }

    public void transpose() {
        IndexedSet<FullObject<String, String>> newObjects = new ListSet<>();
        IndexedSet<String> newAttributes = new ListSet<>();
        for (String attribute : getAttributes()) {
            IndexedSet<String> allObjectsForAttribute = new ListSet<>();
            // TODO: there may be a more efficient way using the fcalib
            // functions
            for (FullObject<String, String> object : objects) {
                if (objectHasAttribute(object, attribute))
                    allObjectsForAttribute.add(object.getIdentifier());
            }
            newObjects.add(new FullObject<>(attribute, allObjectsForAttribute));
        }
        for (FullObject<String, String> object : objects) {
            newAttributes.add(object.getIdentifier());
        }
        objects = newObjects;
        // Why can I access objects directly but not attributes? (I'm
        // questioning the API-decision)
        getAttributes().clear();
        for (String attribute : newAttributes) {
            getAttributes().add(attribute);
        }
    }
}
