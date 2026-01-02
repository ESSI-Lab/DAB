/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
    private boolean compositeMandatory;
    private InputPattern pattern;
    private Class<? extends LabeledEnum> enum_;
    private String composite;
    private boolean multiValue;

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
     * @param composite
     * @param compositeMandatory
     * @param name
     * @param type
     * @param pattern
     * @param mandatory
     * @return
     */
    public static Parameter of(//
	    String composite, //
	    boolean compositeMandatory, //
	    String name, //
	    ContentType type, //
	    InputPattern pattern, //
	    boolean mandatory) {

	return new Parameter(composite, compositeMandatory, name, type, pattern, mandatory);
    }

    /**
     * @param composite
     * @param compositeMandatory
     * @param name
     * @param type
     * @param mandatory
     * @return
     */
    public static Parameter of(//
	    String composite, //
	    boolean compositeMandatory, //
	    String name, //
	    ContentType type, //
	    boolean mandatory) {

	return new Parameter(composite, compositeMandatory, name, type, mandatory);

    }

    /**
     * @param composite
     * @param compositeMandatory
     * @param name
     * @param type
     * @param enum_
     * @param mandatory
     * @return
     */
    public static Parameter of(//
	    String composite, //
	    boolean compositeMandatory, //
	    String name, //
	    ContentType type, //
	    Class<? extends LabeledEnum> enum_, //
	    boolean mandatory) {

	return new Parameter(composite, compositeMandatory, name, type, enum_, mandatory);
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
     * @param composite
     * @param compositeMandatory
     * @param name
     * @param type
     * @param mandatory
     */
    public Parameter(String composite, boolean compositeMandatory, String name, ContentType type, boolean mandatory) {

	this.composite = composite;
	this.compositeMandatory = compositeMandatory;
	this.name = name;
	this.type = type;
	this.mandatory = mandatory;
    }

    /**
     * @param composite
     * @param compositeMandatory
     * @param name
     * @param type
     * @param pattern
     * @param mandatory
     */
    public Parameter(//
	    String composite, //
	    boolean compositeMandatory, //
	    String name, //
	    ContentType type, //
	    InputPattern pattern, //
	    boolean mandatory) {

	this.composite = composite;
	this.compositeMandatory = compositeMandatory;
	this.name = name;
	this.type = type;
	this.pattern = pattern;
	this.mandatory = mandatory;
    }

    /**
     * @param composite
     * @param compositeMandatory
     * @param name
     * @param type
     * @param enum_
     * @param mandatory
     */
    public Parameter(//
	    String composite, //
	    boolean compositeMandatory, //
	    String name, //
	    ContentType type, //
	    Class<? extends LabeledEnum> enum_, //
	    boolean mandatory) {

	this.composite = composite;
	this.compositeMandatory = compositeMandatory;
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
    public Optional<String> getCompositeName() {

	return Optional.ofNullable(composite);
    }

    /**
     * @return
     */
    public boolean isCompositeMandatory() {

	return compositeMandatory;
    }

    /**
     * 
     */
    public void setMultiValue() {

	this.multiValue = true;
    }

    /**
     * @return
     */
    public boolean isMultiValue() {

	return multiValue;
    }

    @Override
    public boolean equals(Object object) {

	return object instanceof Parameter

		&& ((Parameter) object).getName().equals(this.getName()) //

		&& ((Parameter) object).getContentType() == this.getContentType() //

		&& ((Parameter) object).getEnum().equals(this.getEnum()) //

		&& ((Parameter) object).isMandatory() == this.isMandatory() //

		&& ((Parameter) object).isCompositeMandatory() == this.isCompositeMandatory() //

		&& ((Parameter) object).getCompositeName().equals(this.getCompositeName()) //

		&& ((Parameter) object).isMultiValue() ==  this.isMultiValue() //

		&& ((Parameter) object).getInputPattern().equals(this.getInputPattern());
    }
}
