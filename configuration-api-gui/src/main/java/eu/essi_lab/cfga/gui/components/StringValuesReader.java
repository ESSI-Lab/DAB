package eu.essi_lab.cfga.gui.components;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.select.Select;

import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * Reads as {@link String} the values and selected values of an {@link Option} with {@link SelectionMode#SINGLE}
 * or {@link SelectionMode#MULTI}. If the values are of {@link LabeledEnum} or {@link Enum} types, the conversion
 * is opportunely done according to the {@link LabeledEnum#getLabel()} or {@link Enum#name()} methods. For all the
 * other types, the {@link Object#toString()} method is applied.<br>
 * This utility class is used to correctly put {@link String} values in the selection components, {@link Select} and
 * {@link MultiselectComboBox}
 * 
 * @author Fabrizio
 */
public class StringValuesReader {

    /**
     * @param option
     * @return
     */
    public static List<String> readValues(Option<?> option) {

	return option.getValues().//
		stream().//
		map(v -> asString(option.getValueClass(), v)).//
		collect(Collectors.toList());
    }

    /**
     * @param option
     * @return
     */
    public static List<String> readSelectedValues(Option<?> option) {

	return option.getSelectedValues().//
		stream().//
		map(v -> asString(option.getValueClass(), v)).//
		collect(Collectors.toList());
    }

    /**
     * @param option
     * @return
     */
    public static Optional<String> readSelectedValue(Option<?> option) {

	List<String> selectedValues = readSelectedValues(option);
	if (!selectedValues.isEmpty()) {

	    return Optional.of(selectedValues.get(0));
	}

	return Optional.empty();
    }

    /**
     * @param valueClass
     * @param value
     * @return
     */
    private static String asString(Class<?> valueClass, Object value) {

	if (LabeledEnum.class.isAssignableFrom(valueClass)) {

	    LabeledEnum lEnum = (LabeledEnum) value;
	    return lEnum.getLabel();

	} else if (Enum.class.isAssignableFrom(valueClass)) {

	    @SuppressWarnings("rawtypes")
	    Enum enum_ = (Enum) value;
	    return enum_.name();
	}

	return value.toString();
    }
}
