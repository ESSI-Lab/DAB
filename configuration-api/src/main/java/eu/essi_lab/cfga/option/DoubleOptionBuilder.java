/**
 * 
 */
package eu.essi_lab.cfga.option;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import org.json.JSONObject;

/**
 * @author Fabrizio
 */
public class DoubleOptionBuilder extends OptionBuilder<Double> {

    /**
     *  
     */
    public DoubleOptionBuilder() {
	super(Double.class);
    }

    /**
     * @param object
     */
    public DoubleOptionBuilder(JSONObject object) {
	super(object);
    }

    /**
     * @param clazz
     * @return
     */
    public static DoubleOptionBuilder get() {

	return new DoubleOptionBuilder();
    }

    /**
     * @param clazz
     * @param object
     * @return
     */
    public static DoubleOptionBuilder get(JSONObject object) {

	return new DoubleOptionBuilder(object);
    }

}
