package eu.essi_lab.cfga.gs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.essi_lab.cfga.option.InputPattern;

/**
 * @author Fabrizio
 */
public class GSSourcePattern extends InputPattern {

    /**
     * Alphanumeric + '_' + '-'. Can also begin with '-' but followed by alphanumeric or '_'
     */
    public static final GSSourcePattern GS_SOURCE_ID = new GSSourcePattern("gsSourceId", "^(-*\\w+-*)+$", "^(-*\\w+-*)*$");

    /**
     * @param name
     * @param requiredPattern
     * @param pattern
     */
    public GSSourcePattern(String name, String requiredPattern, String pattern) {
	super(name, requiredPattern, pattern);
    }

    /**
     * @param name
     */
    public static InputPattern fromName(String name) {

	List<Field> list = new ArrayList<Field>(Arrays.asList(GSSourcePattern.class.getDeclaredFields()));

	list.addAll(Arrays.asList(InputPattern.class.getDeclaredFields()));

	InputPattern out = fromName(list, name);

	return out;
    }
}
