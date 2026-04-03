package eu.essi_lab.profiler.os;

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

import eu.essi_lab.messages.bond.*;
import eu.essi_lab.pdk.wrt.*;

import java.util.*;

/**
 * Extends the superclass to add particular OS parameter properties
 *
 * @author Fabrizio
 */
public class OSParameter extends WebRequestParameter {

    private String templateValue;

    /**
     * @param name
     * @param type
     * @param defaultValue
     * @param templateValue
     */
    public OSParameter(String name, String type, String defaultValue, String templateValue) {

	this(name, type, defaultValue, templateValue, true);
    }

    /**
     * @param name
     * @param type
     * @param defaultValue
     * @param templateValue
     * @param decodeValue
     */
    public OSParameter(String name, String type, String defaultValue, String templateValue, boolean decodeValue) {

	super(name, type, defaultValue, decodeValue);

	this.templateValue = templateValue;
    }

    @Override
    public Optional<Bond> asBond(String value, String... relatedValues) throws Exception {

	return Optional.empty();
    }

    public String getTemplateValue() {

	return templateValue;
    }

    public boolean isReplaceable() {

	return templateValue != null;
    }

}
