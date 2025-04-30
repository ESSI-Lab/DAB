/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Optional;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class Parameter {

    private String name;
    private ContentType type;
    private boolean mandatory;
    private boolean nestedMandatory;
    private InputPattern pattern;
    private Class<? extends LabeledEnum> enum_;
    private String nested;

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
     * @param enum_
     * @param mandatory
     * @return
     */
    public static Parameter of(String name, ContentType type, Class<? extends LabeledEnum> enum_, boolean mandatory) {

	return new Parameter(name, type, enum_, mandatory);
    }

    /**
     * @param nested
     * @param nestedMandatory
     * @param name
     * @param type
     * @param pattern
     * @param mandatory
     * @return
     */
    public static Parameter of(//
	    String nested, //
	    boolean nestedMandatory, //
	    String name, //
	    ContentType type, //
	    InputPattern pattern, //
	    boolean mandatory) {

	return new Parameter(nested, nestedMandatory, name, type, pattern, mandatory);
    }

    /**
     * @param nested
     * @param nestedMandatory
     * @param name
     * @param type
     * @param mandatory
     * @return
     */
    public static Parameter of(//
	    String nested, //
	    boolean nestedMandatory, //
	    String name, //
	    ContentType type, //
	    boolean mandatory) {

	return new Parameter(nested, nestedMandatory, name, type, mandatory);

    }

    /**
     * @param nested
     * @param nestedMandatory
     * @param name
     * @param type
     * @param enum_
     * @param mandatory
     * @return
     */
    public static Parameter of(//
	    String nested, //
	    boolean nestedMandatory, //
	    String name, //
	    ContentType type, //
	    Class<? extends LabeledEnum> enum_, //
	    boolean mandatory) {

	return new Parameter(nested, nestedMandatory, name, type, enum_, mandatory);
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

	this.name = name;
	this.type = type;
	this.mandatory = mandatory;
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
     * @param name
     * @param type
     * @param enum_
     * @param mandatory
     */
    public Parameter(String name, ContentType type, Class<? extends LabeledEnum> enum_, boolean mandatory) {

	this.name = name;
	this.type = type;
	this.enum_ = enum_;
	this.mandatory = mandatory;
    }

    /**
     * @param nested
     * @param nestedMandatory
     * @param name
     * @param type
     * @param mandatory
     */
    public Parameter(String nested, boolean nestedMandatory, String name, ContentType type, boolean mandatory) {

	this.nested = nested;
	this.nestedMandatory = nestedMandatory;
	this.name = name;
	this.type = type;
	this.mandatory = mandatory;
    }

    /**
     * @param nested
     * @param nestedMandatory
     * @param name
     * @param type
     * @param pattern
     * @param mandatory
     */
    public Parameter(//
	    String nested, //
	    boolean nestedMandatory, //
	    String name, //
	    ContentType type, //
	    InputPattern pattern, //
	    boolean mandatory) {

	this.nested = nested;
	this.nestedMandatory = nestedMandatory;
	this.name = name;
	this.type = type;
	this.pattern = pattern;
	this.mandatory = mandatory;
    }

    /**
     * @param nested
     * @param nestedMandatory
     * @param name
     * @param type
     * @param enum_
     * @param mandatory
     */
    public Parameter(//
	    String nested, //
	    boolean nestedMandatory, //
	    String name, //
	    ContentType type, //
	    Class<? extends LabeledEnum> enum_, //
	    boolean mandatory) {

	this.nested = nested;
	this.nestedMandatory = nestedMandatory;
	this.name = name;
	this.type = type;
	this.enum_ = enum_;
	this.mandatory = mandatory;
    }

    /**
     * @return
     */
    public Optional<InputPattern> getInputPattern() {

	return Optional.ofNullable(pattern);
    }

    /**
     * @return
     */
    public Optional<Class<? extends LabeledEnum>> getEnum() {

	return Optional.ofNullable(enum_);
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
    public ContentType getContentType() {

	return type;
    }

    /**
     * @return
     */
    public boolean isMandatory() {

	return mandatory;
    }

    /**
     * @return
     */
    public Optional<String> getNested() {

	return Optional.ofNullable(nested);
    }

    /**
     * @return
     */
    public boolean isNestedMandatory() {

	return nestedMandatory;
    }

    @Override
    public boolean equals(Object object) {

	return object instanceof Parameter

		&& ((Parameter) object).getName().equals(this.getName()) //

		&& ((Parameter) object).getContentType() == this.getContentType() //

		&& ((Parameter) object).getEnum().equals(this.getEnum()) //

		&& ((Parameter) object).isMandatory() == this.isMandatory() //

		&& ((Parameter) object).isNestedMandatory() == this.isNestedMandatory() //

		&& ((Parameter) object).getNested().equals(this.getNested()) //

		&& ((Parameter) object).getInputPattern().equals(this.getInputPattern());
    }
}
