/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.Optional;

/**
 * @author Fabrizio
 */
public class SKOSResponseItem {

    private String concept;
    private String expanded;
    private String pref;
    private String alt;

    /**
     * @param concept
     * @param pref
     * @param expanded
     * @param alt
     * @return
     */
    public static SKOSResponseItem of(String concept, String pref, String expanded, String alt) {

	SKOSResponseItem item = new SKOSResponseItem();
	item.concept = concept;
	item.pref = pref;
	item.expanded = expanded;
	item.alt = alt;

	return item;
    }

    /**
     * @return
     */
    public String getConcept() {

	return concept;
    }

    /**
     * @return
     */
    public Optional<String> getExpanded() {

	return Optional.ofNullable(expanded);
    }

    /**
     * @return
     */
    public Optional<String> getPref() {

	return Optional.ofNullable(pref);
    }

    /**
     * @return
     */
    public Optional<String> getAlt() {

	return Optional.ofNullable(alt);
    }

    @Override
    public String toString() {

	return "concept: " + getConcept() + //
		getExpanded().map(v -> "\nexpanded: " + v).orElse("") + //
		getPref().map(v -> "\npref: " + v).orElse("") + //
		getAlt().map(v -> "\nalt: " + v).orElse("");//

    }

    @Override
    public boolean equals(Object other) {

	return other instanceof SKOSResponseItem && other.toString().equals(this.toString());
    }
}
