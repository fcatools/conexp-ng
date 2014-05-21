package fcatools.conexpng.model;

import java.util.Set;

import de.tudresden.inf.tcs.fcaapi.Concept;
import de.tudresden.inf.tcs.fcalib.FullObject;
import de.tudresden.inf.tcs.fcalib.utils.ListSet;
import fcatools.conexpng.io.locale.LocaleHandler;

/**
 * This class implemented the Concept interface of the fcalib.
 * 
 */
public class LatticeConcept implements Concept<String, FullObject<String, String>> {

    private ListSet<FullObject<String, String>> extent;
    private ListSet<String> intent;

    public LatticeConcept() {
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

    @Override
    public String toString() {
        return LocaleHandler.getString("LatticeConcept.toString.objects") + extent + "\n"
                + LocaleHandler.getString("LatticeConcept.toString.attributes") + intent + "\n";
    }
}
