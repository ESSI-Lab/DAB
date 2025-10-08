/**
 * 
 */
package eu.essi_lab.lib.skoss.fedx;

import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public enum QueryBinding implements LabeledEnum {

    /**
     * 
     */
    PREF("pref"),
    /**
     * 
     */
    ALT("alt"),
    /**
     * 
     */
    CLOSE_MATCH("closeMatch"),
    /**
     * 
     */
    EXPANDED("expanded"),
    /**
     * 
     */
    CONCEPT("concept");

    private String label;

    /**
     * @param label
     */
    private QueryBinding(String label) {

	this.label = label;
    }

    @Override
    public String getLabel() {

	return label;
    }
}
