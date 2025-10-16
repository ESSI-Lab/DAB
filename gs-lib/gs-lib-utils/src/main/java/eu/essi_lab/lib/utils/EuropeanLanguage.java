/**
 * 
 */
package eu.essi_lab.lib.utils;

/**
 * @author Fabrizio
 */
public enum EuropeanLanguage implements LabeledEnum {
    /**
     * 
     */
    BULGARIAN("bg"),
    /**
     * 
     */
    CZECH("cs"),
    /**
     * 
     */
    DANISH("da"),
    /**
    * 
    */
    ENGLISH("en"),
    /**
     * 
     */
    GERMAN("de"),
    /**
     * 
     */
    GREEK("el"),
    /**
     * 
     */
    SPANISH("es"),
    /**
     * 
     */
    ESTONIAN("et"),
    /**
     * 
     */
    FINNISH("fi"),
    /**
     * 
     */
    FRENCH("fr"),
    /**
     * 
     */
    CROATIAN("hr"),
    /**
     * 
     */
    HUNGARIAN("hu"),
    /**
     * 
     */
    ITALIAN("it"),
    /**
     * 
     */
    LITHUANIAN("lt"),
    /**
     * 
     */
    LATVIAN("lv"),
    /**
     * 
     */
    DUTCH("nl"),
    /**
     * 
     */
    POLISH("pl"),
    /**
     * 
     */
    PORTUGUESE("pt"),
    /**
     * 
     */
    ROMANIAN("ro"),
    /**
     * 
     */
    SLOVAK("sk"),
    /**
     * 
     */
    SLOVENIAN("sl"),
    /**
     * 
     */
    SWEDISH("sv");

    private final String code;

    /**
     * @param code
     */
    private EuropeanLanguage(String code) {

	this.code = code;
    }

    @Override
    public String getLabel() {

	return code;
    }
}
