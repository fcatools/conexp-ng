package com.eugenkiss.conexp2.draw;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.eugenkiss.conexp2.model.FormalContext;

import de.tudresden.inf.tcs.fcalib.FullObject;

public class TestLatticeAlgorithm implements ILatticeAlgorithm {

	private FormalContext context;
	private HashMap<Set<String>, Set<String>> concepts;

	@Override
	public LatticeGraph computeLatticeGraph(FormalContext context) {
		LatticeGraph graph = new LatticeGraph();
		this.context = context;
		this.computeConcepts();
		System.out.println(concepts);

		for (Set<String> extent : concepts.keySet()) {
			Set<String> intent = concepts.get(extent);

			Node n = new Node();
			n.addObjects(extent);
			n.addAttributs(intent);

			graph.getNodes().add(n);
		}

		for (Node u : graph.getNodes()) {
			Set<String> uEx = u.getObjects();
			int count = 0;

			for (Node v : graph.getNodes()) {
				Set<String> vEx = v.getObjects();
				if (isSubconcept(uEx, vEx)) {
					count++;
				}
				if (isLowerNeighbour(uEx, vEx)) {
					v.addBelowNode(u);
					graph.getEdges().add(new Edge(u, v));
				}

			}
			u.update((int) (Math.random() * 500), count * 100);
		}

		return graph;
	}

	private void computeConcepts() {

		HashMap<String, Set<String>> extentPerAttr = new HashMap<String, Set<String>>();
		/*
		 * Step 1: Initialize a list of concept extents. To begin with, write
		 * for each attribute m # M the attribute extent {m}$ to this list (if
		 * not already present).
		 */
		for (String s : context.getAttributes()) {
			TreeSet<String> set = new TreeSet<String>();
			for (FullObject<String, String> f : context.getObjects()) {
				if (f.getDescription().getAttributes().contains(s)) {
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
					Set<String> result = this.intersection(
							extentPerAttr.get(s), extentPerAttr.get(t));
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
					Set<String> result = this.intersection(
							extentPerAttr.get(s), extentPerAttr.get(t));
					if (extentPerAttr.values().contains(result)) {
						TreeSet<String> set = new TreeSet<String>();
						for (FullObject<String, String> f : context
								.getObjects()) {
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
		concepts = new HashMap<Set<String>, Set<String>>();
		for (Set<String> e : extentPerAttr.values()) {
			TreeSet<String> intents = new TreeSet<String>();
			int count = 0;
			for (FullObject<String, String> i : context.getObjects()) {
				if (e.isEmpty()) {
					intents.addAll(i.getDescription().getAttributes());
				} else if (e.contains(i.getIdentifier().toString())) {
					TreeSet<String> prev = sort(i.getDescription()
							.getAttributes());
					if (count > 0) {
						intents = intersection(prev, intents);
					} else {
						intents = prev;
					}
					count++;
				}
			}
			concepts.put(e, intents);
		}

	}

	private TreeSet<String> intersection(Set<String> firstSet,
			Set<String> secondSet) {
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

	private boolean isSubconcept(Set<String> subEx, Set<String> superEx) {
		if (subEx == superEx) {
			return false;
		}
		if (subEx.size() > superEx.size()) {
			return false;
		}
		for (String s : subEx) {
			if (!superEx.contains(s)) {
				return false;
			}
		}
		return true;
	}

	private boolean isLowerNeighbour(Set<String> subEx, Set<String> superEx) {
		if (subEx == superEx) {
			return false;
		}
		if (!isSubconcept(subEx, superEx)) {
			return false;
		}
		for (Set<String> set : concepts.keySet()) {
			if (subEx == set || set == superEx) {
				if (isSubconcept(subEx, set) && isSubconcept(set, superEx)) {
					return false;
				}
			}
		}
		return true;
	}

}
