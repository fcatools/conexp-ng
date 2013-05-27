package com.eugenkiss.conexp2.model;

import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

/**
 * A specialization of FormalContext<String,String> with the aim to remove
 * the verbose repetition of <String,String>. Plus, adds a couple of useful
 * methods.
 */
public class FormalContext extends de.tudresden.inf.tcs.fcalib.FormalContext<String, String> {

    public FormalContext() {
        super();
    }

    public void transpose() {
        IndexedSet<FullObject<String,String>> newObjects = new ListSet<>();
        IndexedSet<String> newAttributes = new ListSet<>();
        for (String attribute : getAttributes()) {
            IndexedSet<String> allObjectsForAttribute = new ListSet<>();
            // TODO: there may be a more efficient way using the fcalib functions
            for (FullObject<String,String> object : objects) {
                if (objectHasAttribute(object, attribute))
                    allObjectsForAttribute.add(object.getIdentifier());
            }
            newObjects.add(new FullObject<>(attribute, allObjectsForAttribute));
        }
        for (FullObject<String,String> object : objects) {
            newAttributes.add(object.getIdentifier());
        }
        objects = newObjects;
        // Why can I access objects directly but not attributes? (I'm questioning the API-decision)
        getAttributes().clear();
        for (String attribute : newAttributes) {
            getAttributes().add(attribute);
        }
    }
}
