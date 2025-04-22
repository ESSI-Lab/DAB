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
