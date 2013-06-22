package fcatools.conexpng.model;

import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;

public class LatticeConcept implements Concept<String, FullObject<String, String>> {

	private ListSet<FullObject<String, String>> extent;
	private ListSet<String> intent;
	
	public LatticeConcept(){
		extent = new ListSet<>();
		intent = new ListSet<>();
	}
	
	@Override
	public Set<FullObject<String, String>> getExtent() {
		return this.extent;
	}

	@Override
	public Set<String> getIntent() {
		return this.intent;
	}

}
