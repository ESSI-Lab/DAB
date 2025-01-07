package eu.essi_lab.lib.utils;

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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An enum which implements this interface, allows to create its own constants as well as from the fields names
 * using the {@link Enum#valueOf(Class, String)}, also from a label assigned to the fields
 * by means of the {@link LabeledEnum#valueOf(Class, String)} method
 * 
 * @author Fabrizio
 */
public interface LabeledEnum {

    /**
     * Return all the enum constants of the given <code>enumType</code>
     * 
     * @param enumType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends LabeledEnum> List<T> values(Class<T> enumType) {

	return Arrays.asList(enumType.getFields()).//
		stream().//
		filter(f -> {
		    try {
			return LabeledEnum.class.isAssignableFrom(f.get(null).getClass());
		    } catch (Exception e1) {
		    }

		    return false;
		}).//
		map(f -> {
		    try {
			return (T) f.get(null);
		    } catch (IllegalAccessException e) {
		    }
		    return null;
		}).//
		collect(Collectors.toList());
    }

    /**
     * This method is very similar to {@link Enum#valueOf(Class, String)} but instead of the
     * enum constant name, it requires the enum constant label. If no enum constant of the given <code>enumType</code>
     * with the given <code>label</code> is found, {@link Optional#empty()} is returned
     * 
     * @see #getLabel()
     * @param enumType
     * @param label
     * @return
     */
    public static <T extends LabeledEnum> Optional<T> valueOf(Class<T> enumType, String label) {

	return values(enumType).//
		stream().//
		filter(l -> ((LabeledEnum) l).getLabel().equals(label)).//
		findFirst();
    }

    /**
     * Returns the label of this enum constant
     * 
     * @return
     */
    public String getLabel();
}
