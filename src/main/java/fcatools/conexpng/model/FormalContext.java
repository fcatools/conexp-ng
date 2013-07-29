package fcatools.conexpng.model;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcaapi.FCAImplication;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalAttributeException;
import de.tudresden.inf.tcs.fcaapi.exception.IllegalObjectException;
import de.tudresden.inf.tcs.fcaapi.utils.IndexedSet;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.Implication;
import de.tudresden.inf.tcs.fcalib.ImplicationSet;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.gui.dependencies.AssociationMiner;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.*;

/**
 * A specialization of FormalContext<String,String> with the aim to remove the
 * verbose repetition of <String,String>. Plus, adds a couple of useful methods.
 * Due to the API of FormalContext<String,String> the here implemented methods
 * are extremely inefficient.
 */
public class FormalContext extends de.tudresden.inf.tcs.fcalib.FormalContext<String, String> {

    protected HashMap<String, SortedSet<String>> objectsOfAttribute = new HashMap<>();
    private ArrayList<String> dontConsideredAttr = new ArrayList<>();
    private ArrayList<FullObject<String, String>> dontConsideredObj = new ArrayList<>();

    @Override
    public boolean addAttribute(String attribute) throws IllegalAttributeException {
        if (super.addAttribute(attribute)) {
            objectsOfAttribute.put(attribute, new TreeSet<String>());
            return true;
        } else
            return false;
    }

    @Override
    public boolean addAttributeToObject(String attribute, String id) throws IllegalAttributeException,
            IllegalObjectException {
        if (super.addAttributeToObject(attribute, id)) {
            SortedSet<String> objects = objectsOfAttribute.get(attribute);
            if (objects != null)
                objects.add(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean addObject(FullObject<String, String> arg0) throws IllegalObjectException {
        if (super.addObject(arg0)) {
            for (String attribute : arg0.getDescription().getAttributes()) {
                objectsOfAttribute.get(attribute).add(arg0.getIdentifier());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAttributeFromObject(String attribute, String id) throws IllegalAttributeException,
            IllegalObjectException {
        if (super.removeAttributeFromObject(attribute, id)) {
            SortedSet<String> objects = objectsOfAttribute.get(attribute);
            if (objects != null)
                objects.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeObject(String id) throws IllegalObjectException {
        return removeObject(getObject(id));
    }

    @Override
    public boolean removeObject(FullObject<String, String> object) throws IllegalObjectException {
        if (super.removeObject(object)) {
            for (String attribute : object.getDescription().getAttributes()) {
                objectsOfAttribute.get(attribute).remove(object.getIdentifier());
            }
        }
        return false;
    }

    public FormalContext() {
        super();
        objectsOfAttribute = new HashMap<>();
    }

    @Override
    public Set<Concept<String, FullObject<String, String>>> getConcepts() {
        ListSet<Concept<String, FullObject<String, String>>> conceptLattice = new ListSet<Concept<String, FullObject<String, String>>>();

        HashMap<String, Set<String>> extentPerAttr = new HashMap<String, Set<String>>();
        /*
         * Step 1: Initialize a list of concept extents. To begin with, write
         * for each attribute m # M the attribute extent {m}$ to this list (if
         * not already present).
         */
        for (String s : this.getAttributes()) {
            TreeSet<String> set = new TreeSet<String>();
            for (FullObject<String, String> f : this.getObjects()) {
                if (f.getDescription().getAttributes().contains(s) && (!dontConsideredObj.contains(f))) {
                    set.add(f.getIdentifier());
                }
            }
            extentPerAttr.put(s, set);
        }

        /*
         * Step 2: For any two sets in this list, compute their intersection. If
         * the result is a set that is not yet in the list, then extend the list
         * by this set. With the extended list, continue to build all pairwise
         * intersections.
         */
        HashMap<String, Set<String>> temp = new HashMap<String, Set<String>>();
        for (String s : extentPerAttr.keySet()) {
            for (String t : extentPerAttr.keySet()) {
                if (!s.equals(t)) {
                    Set<String> result = this.intersection(extentPerAttr.get(s), extentPerAttr.get(t));
                    if (!extentPerAttr.values().contains(result)) {
                        if (!temp.containsValue(result)) {
                            temp.put(s + ", " + t, result);
                        }
                    }
                }
            }
        }
        extentPerAttr.putAll(temp);

        /*
         * Step 3: If for any two sets of the list their intersection is also in
         * the list, then extend the list by the set G (provided it is not yet
         * contained in the list). The list then contains all concept extents
         * (and nothing else).
         */
        for (String s : extentPerAttr.keySet()) {
            for (String t : extentPerAttr.keySet()) {
                if (!s.equals(t)) {
                    Set<String> result = this.intersection(extentPerAttr.get(s), extentPerAttr.get(t));
                    if (extentPerAttr.values().contains(result)) {
                        TreeSet<String> set = new TreeSet<String>();
                        for (FullObject<String, String> f : this.getObjects()) {
                            set.add(f.getIdentifier());
                        }
                        extentPerAttr.put(null, set);
                    }
                    break;
                }
            }
            break;
        }

        /*
         * Step 4: For every concept extent A in the list compute the
         * corresponding intent A' to obtain a list of all formal concepts
         * (A,A') of (G,M, I).
         */

        for (Set<String> e : extentPerAttr.values()) {
            TreeSet<String> intents = new TreeSet<String>();
            int count = 0;

            Concept<String, FullObject<String, String>> c = new LatticeConcept();

            for (FullObject<String, String> i : this.getObjects()) {
                if (e.isEmpty()) {
                    intents.addAll(i.getDescription().getAttributes());
                } else if (e.contains(i.getIdentifier().toString())) {
                    TreeSet<String> prev = sort(i.getDescription().getAttributes());
                    if (count > 0) {
                        intents = intersection(prev, intents);
                    } else {
                        intents = prev;
                    }
                    count++;
                    c.getExtent().add(i);
                }
            }
            // concepts.put(e, intents);
            for (String s : intents) {
                c.getIntent().add(s);
            }
            conceptLattice.add(c);
        }
        return conceptLattice;
    }

    public Set<Concept<String, FullObject<String, String>>> getConceptsWithoutConsideredElementa() {
        ListSet<Concept<String, FullObject<String, String>>> conceptLattice = new ListSet<Concept<String, FullObject<String, String>>>();

        HashMap<String, Set<String>> extentPerAttr = new HashMap<String, Set<String>>();
        /*
         * Step 1: Initialize a list of concept extents. To begin with, write
         * for each attribute m # M the attribute extent {m}$ to this list (if
         * not already present).
         */
        for (String s : this.getAttributes()) {
            if (!dontConsideredAttr.contains(s)) {
                TreeSet<String> set = new TreeSet<String>();
                for (FullObject<String, String> f : this.getObjects()) {
                    if (f.getDescription().getAttributes().contains(s) && (!dontConsideredObj.contains(f))) {
                        set.add(f.getIdentifier());
                    }
                }
                extentPerAttr.put(s, set);
            }
        }

        /*
         * Step 2: For any two sets in this list, compute their intersection. If
         * the result is a set that is not yet in the list, then extend the list
         * by this set. With the extended list, continue to build all pairwise
         * intersections.
         */
        HashMap<String, Set<String>> temp = new HashMap<String, Set<String>>();
        for (String s : extentPerAttr.keySet()) {
            for (String t : extentPerAttr.keySet()) {
                if (!s.equals(t)) {
                    Set<String> result = this.intersection(extentPerAttr.get(s), extentPerAttr.get(t));
                    if (!extentPerAttr.values().contains(result)) {
                        if (!temp.containsValue(result)) {
                            temp.put(s + ", " + t, result);
                        }
                    }
                }
            }
        }
        extentPerAttr.putAll(temp);

        /*
         * Step 3: If for any two sets of the list their intersection is also in
         * the list, then extend the list by the set G (provided it is not yet
         * contained in the list). The list then contains all concept extents
         * (and nothing else).
         */
        for (String s : extentPerAttr.keySet()) {
            for (String t : extentPerAttr.keySet()) {
                if (!s.equals(t)) {
                    Set<String> result = this.intersection(extentPerAttr.get(s), extentPerAttr.get(t));
                    if (extentPerAttr.values().contains(result)) {
                        TreeSet<String> set = new TreeSet<String>();
                        for (FullObject<String, String> f : this.getObjects()) {
                            set.add(f.getIdentifier());
                        }
                        extentPerAttr.put(null, set);
                    }
                    break;
                }
            }
            break;
        }

        /*
         * Step 4: For every concept extent A in the list compute the
         * corresponding intent A' to obtain a list of all formal concepts
         * (A,A') of (G,M, I).
         */

        for (Set<String> e : extentPerAttr.values()) {
            TreeSet<String> intents = new TreeSet<String>();
            int count = 0;

            Concept<String, FullObject<String, String>> c = new LatticeConcept();

            for (FullObject<String, String> i : this.getObjects()) {
                if (!dontConsideredObj.contains(i)) {
                    if (e.isEmpty()) {
                        intents.addAll(i.getDescription().getAttributes());
                    } else if (e.contains(i.getIdentifier().toString())) {
                        TreeSet<String> prev = sort(i.getDescription().getAttributes());
                        if (count > 0) {
                            intents = intersection(prev, intents);
                        } else {
                            intents = prev;
                        }
                        count++;
                        c.getExtent().add(i);
                    }
                }
            }
            // concepts.put(e, intents);
            for (String s : intents) {
                if (!dontConsideredAttr.contains(s))
                    c.getIntent().add(s);
            }
            conceptLattice.add(c);
        }
        return conceptLattice;
    }

    public int supportCount(Set<String> attributes) {
        if (attributes.isEmpty())
            return objects.size();
        int mincount = Integer.MAX_VALUE;
        String attributeWithMincount = "";
        // search for the attribute with the fewest objects
        for (String string : attributes) {
            if (objectsOfAttribute.get(string).size() < mincount) {
                mincount = objectsOfAttribute.get(string).size();
                attributeWithMincount = string;
            }
        }
        int count = 0;
        boolean notfound;
        // search the other attributes only in these objects
        for (String obj : objectsOfAttribute.get(attributeWithMincount)) {
            notfound = false;
            for (String att : attributes) {
                if (!objectHasAttribute(getObject(obj), att)) {
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
            if (A == null)
                return Collections.emptySet();
            if (!A.equals(doublePrime(A))) {
                Implication<String> im = new Implication<>(A, doublePrime(A));
                impl.add(im);
                result.add(im);
            }
        }
        // remove redundant items in the conclusion
        for (FCAImplication<String> fcaImplication : result) {
            fcaImplication.getConclusion().removeAll(fcaImplication.getPremise());
        }

        return result;
    }

    @Override
    public Set<FCAImplication<String>> getDuquenneGuiguesBase() {
        return getStemBase();
    }

    public Set<AssociationRule> getLuxenburgerBase(double minsup, double conf) {
        return new AssociationMiner(this, minsup, conf).computeAssociationRules();
    }

    public void clarifyObjects() {
        ArrayList<FullObject<String, String>> toBeRemoved = new ArrayList<>();
        for (int i = 0; i < getObjectCount(); i++) {
            FullObject<String, String> o1 = objects.getElementAt(i);
            for (int j = i + 1; j < getObjectCount(); j++) {
                FullObject<String, String> o2 = objects.getElementAt(j);
                if (getAttributesForObject(o1.getIdentifier()).equals(getAttributesForObject(o2.getIdentifier()))) {
                    toBeRemoved.add(o2);
                }
            }
        }
        for (FullObject<String, String> o : toBeRemoved) {
            objects.remove(o);
        }
    }

    public void clarifyAttributes() {
        transpose();
        clarifyObjects();
        transpose();
    }

    // I have just implemented the functionality as I understood it by reading
    // the documentation and playing around with the original ConExp. It seems
    // to be correct. The code can probably be written in a much more efficient
    // way, though.
    public void reduceObjects() {
        clarifyObjects();
        ArrayList<FullObject<String, String>> toBeRemoved = new ArrayList<>();
        for (int i = 0; i < getObjectCount(); i++) {
            FullObject<String, String> o = objects.getElementAt(i);
            IndexedSet<FullObject<String, String>> otherObjects0 = new ListSet<>();
            for (FullObject<String, String> o0 : objects) {
                otherObjects0.add(o0);
            }
            otherObjects0.remove(o);
            for (FullObject<String, String> o0 : toBeRemoved) {
                otherObjects0.remove(o0);
            }
            ICombinatoricsVector<FullObject<String, String>> otherObjects = Factory.createVector(otherObjects0);
            Generator<FullObject<String, String>> gen = Factory.createSubSetGenerator(otherObjects);
            for (ICombinatoricsVector<FullObject<String, String>> subSet : gen) {
                if (subSet.getSize() < 2)
                    continue;
                IndexedSet<String> intersection = new ListSet<>();
                for (String attribute : getAttributesForObject(subSet.getValue(0).getIdentifier())) {
                    intersection.add(attribute);
                }
                for (int j = 1; j < subSet.getSize(); j++) {
                    intersection.retainAll(getAttributesForObject(subSet.getValue(j).getIdentifier()));
                }
                if (intersection.size() == 0)
                    continue;
                if (getAttributesForObject(o.getIdentifier()).equals(intersection)) {
                    toBeRemoved.add(o);
                    break;
                }
            }
        }
        for (FullObject<String, String> o : toBeRemoved) {
            objects.remove(o);
        }
    }

    public void reduceAttributes() {
        transpose();
        reduceObjects();
        transpose();
    }

    public void reduce() {
        reduceObjects();
        transpose();
        reduceObjects();
        transpose();
    }

    public void transpose() {
        IndexedSet<FullObject<String, String>> newObjects = new ListSet<>();
        IndexedSet<String> newAttributes = new ListSet<>();
        for (String attribute : getAttributes()) {
            IndexedSet<String> allObjectsForAttribute = new ListSet<>();
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
        objectsOfAttribute.clear();
        for (String attribute : newAttributes) {
            getAttributes().add(attribute);
            objectsOfAttribute.put(attribute, new TreeSet<String>());
        }
        for (FullObject<String, String> object : objects) {
            for (String attribute : object.getDescription().getAttributes()) {
                objectsOfAttribute.get(attribute).add(object.getIdentifier());
            }
        }
    }

    public void toggleAttributeForObject(String attribute, String objectID) {
        if (objectHasAttribute(getObject(objectID), attribute)) {
            try {
                removeAttributeFromObject(attribute, objectID);
            } catch (IllegalObjectException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.addAttributeToObject(attribute, objectID);
            } catch (IllegalObjectException e) {
                e.printStackTrace();
            }
        }
    }

    public void invert(int objectStartIndex, int objectEndIndex, int attributeStartIndex, int attributeEndIndex) {
        for (int i = objectStartIndex; i < objectEndIndex; i++) {
            for (int j = attributeStartIndex; j < attributeEndIndex; j++) {
                String objectID = getObjectAtIndex(i).getIdentifier();
                String attribute = getAttributeAtIndex(j);
                toggleAttributeForObject(attribute, objectID);
            }
        }
    }

    public void invert() {
        invert(0, getObjectCount() - 1, 0, getAttributeCount() - 1);
    }

    public void clear(int objectStartIndex, int objectEndIndex, int attributeStartIndex, int attributeEndIndex) {
        for (int i = objectStartIndex; i < objectEndIndex; i++) {
            for (int j = attributeStartIndex; j < attributeEndIndex; j++) {
                FullObject<String, String> object = getObjectAtIndex(i);
                String attribute = getAttributeAtIndex(j);
                if (objectHasAttribute(object, attribute)) {
                    try {
                        removeAttributeFromObject(attribute, object.getIdentifier());
                    } catch (IllegalObjectException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void clear() {
        clear(0, getObjectCount() - 1, 0, getAttributeCount() - 1);
    }

    public void fill(int objectStartIndex, int objectEndIndex, int attributeStartIndex, int attributeEndIndex) {
        for (int i = objectStartIndex; i < objectEndIndex; i++) {
            for (int j = attributeStartIndex; j < attributeEndIndex; j++) {
                FullObject<String, String> object = getObjectAtIndex(i);
                String attribute = getAttributeAtIndex(j);
                if (!objectHasAttribute(object, attribute)) {
                    try {
                        addAttributeToObject(attribute, object.getIdentifier());
                    } catch (IllegalObjectException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void fill() {
        fill(0, getObjectCount() - 1, 0, getAttributeCount() - 1);
    }

    public void renameAttribute(String oldName, String newName) {
        IndexedSet<String> newAttributes = new ListSet<>();
        IndexedSet<FullObject<String, String>> filteredObjects = new ListSet<>();
        for (FullObject<String, String> object : objects) {
            if (objectHasAttribute(object, oldName)) {
                filteredObjects.add(object);
                try {
                    removeAttributeFromObject(oldName, object.getIdentifier());
                } catch (IllegalObjectException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String attribute : getAttributes()) {
            if (attribute.equals(oldName)) {
                newAttributes.add(newName);
            } else {
                newAttributes.add(attribute);
            }
        }
        getAttributes().clear();
        for (String attribute : newAttributes) {
            getAttributes().add(attribute);
        }
        for (FullObject<String, String> object : filteredObjects) {
            try {
                addAttributeToObject(newName, object.getIdentifier());
            } catch (IllegalObjectException e) {
                e.printStackTrace();
            }
        }
    }

    public void renameObject(String oldName, String newName) {
        IndexedSet<FullObject<String, String>> newObjects = new ListSet<>();
        // IndexedSet<String> filteredAttributes = new ListSet<>();
        for (FullObject<String, String> object : objects) {
            if (object.getIdentifier().equals(oldName)) {
                newObjects.add(new FullObject<String, String>(newName, getAttributesForObject(oldName)));
            } else {
                newObjects.add(object);
            }
        }
        objects = newObjects;
        for (SortedSet<String> objects : objectsOfAttribute.values()) {
            if (objects.contains(oldName)) {
                objects.remove(oldName);
                objects.add(newName);
            }
        }
    }

    public boolean existsAttributeAlready(String name) {
        for (String attribute : getAttributes()) {
            if (attribute.equals(name))
                return true;
        }
        return false;
    }

    public boolean existsObjectAlready(String name) {
        for (FullObject<String, String> object : objects) {
            if (object.getIdentifier().equals(name))
                return true;
        }
        return false;
    }

    public Set<String> getAttributesForObject(String objectID) {
        Set<String> attributes = new HashSet<>();
        FullObject<String, String> object = getObject(objectID);
        for (String attribute : getAttributes()) {
            if (objectHasAttribute(object, attribute)) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    public void removeAttribute(String attribute) {
        IndexedSet<String> newAttributes = new ListSet<>();
        for (FullObject<String, String> object : objects) {
            if (objectHasAttribute(object, attribute)) {
                try {
                    removeAttributeFromObject(attribute, object.getIdentifier());
                } catch (IllegalObjectException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String attr : getAttributes()) {
            if (attr.equals(attribute)) {
            } else {
                newAttributes.add(attr);
            }
        }
        getAttributes().clear();
        for (String attr : newAttributes) {
            getAttributes().add(attr);
        }
    }

    // Should not be used outside the context editor
    public void removeAttributeInternal(String attribute) {
        IndexedSet<String> newAttributes = new ListSet<>();
        for (String attr : getAttributes()) {
            if (attr.equals(attribute)) {
            } else {
                newAttributes.add(attr);
            }
        }
        getAttributes().clear();
        for (String attr : newAttributes) {
            getAttributes().add(attr);
        }
    }

    public void addObjectAt(FullObject<String, String> object, int i) {
        IndexedSet<FullObject<String, String>> newObjects = new ListSet<>();
        for (int j = 0; j < getObjectCount(); j++) {
            if (j == i)
                newObjects.add(object);
            newObjects.add(getObjectAtIndex(j));
        }
        if (i == getObjectCount())
            newObjects.add(object);
        objects = newObjects;
    }

    public void addAttributeAt(String attribute, int i) {
        IndexedSet<String> newAttributes = new ListSet<>();
        for (int j = 0; j < getAttributeCount(); j++) {
            if (j == i)
                newAttributes.add(attribute);
            newAttributes.add(getAttributeAtIndex(j));
        }
        if (i == getAttributeCount())
            newAttributes.add(attribute);
        getAttributes().clear();
        for (String attr : newAttributes) {
            getAttributes().add(attr);
        }
        objectsOfAttribute.put(attribute, new TreeSet<String>());
    }

    private TreeSet<String> intersection(Set<String> firstSet, Set<String> secondSet) {
        TreeSet<String> result = new TreeSet<String>();
        for (String s : firstSet) {
            for (String t : secondSet) {
                if (s == t) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    private TreeSet<String> sort(Set<String> sortable) {
        TreeSet<String> result = new TreeSet<String>();
        for (String s : sortable) {
            result.add(s);
        }
        return result;
    }

    /**
     * Set Attribute which don't be consider by lattice computation.
     *
     * @param attr
     */
    public void dontConsiderAttribute(String attr) {
        this.dontConsideredAttr.add(attr);
    }

    /**
     * Set Attribute which has to be reconsider by lattice computation.
     *
     * @param attr
     */
    public void considerAttribute(String attr) {
        this.dontConsideredAttr.remove(attr);
    }

    /**
     * Set Object which don't be consider by lattice computation.
     *
     * @param obj
     */
    public void dontConsiderObject(FullObject<String, String> obj) {
        this.dontConsideredObj.add(obj);
    }

    /**
     * Set Object which has to be reconsider by lattice computation.
     *
     * @param obj
     */
    public void considerObject(FullObject<String, String> obj) {
        this.dontConsideredObj.remove(obj);
    }

    public void clearConsidered() {
        dontConsideredAttr.clear();
        dontConsideredObj.clear();
    }




	public ArrayList<String> getDontConsideredAttr() {
		return dontConsideredAttr;
	}

	public ArrayList<FullObject<String, String>> getDontConsideredObj() {
		return dontConsideredObj;
	}
    
    
}
