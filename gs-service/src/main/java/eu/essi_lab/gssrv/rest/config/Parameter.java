/**
 * 
 */
package eu.essi_lab.gssrv.rest.config;

import java.util.Optional;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class Parameter {

    private String name;
    private ContentType type;
    private boolean mandatory;
    private InputPattern pattern;

    /**
     * @param name
     * @param type
     * @param mandatory
     * @return
     */
    public static Parameter of(String name, ContentType type, boolean mandatory) {

	return new Parameter(name, type, mandatory);

    }

    /**
     * @param name
     * @param type
     * @param pattern
     * @param mandatory
     * @return
     */
    public static Parameter of(String name, ContentType type, InputPattern pattern, boolean mandatory) {

	return new Parameter(name, type, pattern, mandatory);
    }

    /**
     * @param name
     * @param type
     * @param mandatory
     */
    public Parameter(String name, ContentType type, boolean mandatory) {

	this(name, type, null, mandatory);
    }

    /**
     * @param name
     * @param type
     * @param mandatory
     */
    public Parameter(String name, ContentType type, InputPattern pattern, boolean mandatory) {

	this.name = name;
	this.type = type;
	this.pattern = pattern;
	this.mandatory = mandatory;
    }

    /**
     * @return the pattern
     */
    public Optional<InputPattern> getPattern() {

	return Optional.ofNullable(pattern);
    }

    /**
     * @return the name
     */
    public String getName() {

	return name;
    }

    /**
     * @return the type
     */
    public ContentType getType() {

	return type;
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {

	return mandatory;
    }
}
