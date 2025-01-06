package eu.essi_lab.cfga.option;

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

import java.util.Arrays;
import java.util.List;

import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public enum BooleanChoice implements LabeledEnum {

    /**
     * 
     */
    TRUE("Yes"),

    /**
     * 
     */
    FALSE("No");

    private String label;

    /**
     * @param label
     */
    private BooleanChoice(String label) {

	this.label = label;
    }

    /**
     * @return
     */
    public static List<BooleanChoice> listValues() {

	return Arrays.asList(values());
    }

    /**
     * @param value
     * @return
     */
    public static BooleanChoice fromBoolean(boolean value) {

	return value ? BooleanChoice.TRUE : BooleanChoice.FALSE;
    }

    /**
     * @param value
     * @return
     */
    public static Boolean toBoolean(BooleanChoice value) {

	return value == BooleanChoice.TRUE ? true : false;
    }

    @Override
    public String getLabel() {

	return label;
    }

    @Override
    public String toString() {

	return getLabel();
    }

}
