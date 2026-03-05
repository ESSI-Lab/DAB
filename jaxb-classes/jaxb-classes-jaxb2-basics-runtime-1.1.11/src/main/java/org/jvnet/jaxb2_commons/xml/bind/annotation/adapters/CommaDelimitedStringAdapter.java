package org.jvnet.jaxb2_commons.xml.bind.annotation.adapters;

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

import java.util.LinkedList;
import java.util.List;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.jvnet.jaxb2_commons.lang.StringUtils;

public class CommaDelimitedStringAdapter extends
		XmlAdapter<String, List<String>> {

	@Override
	public String marshal(List<String> value) throws Exception {
		if (value == null) {
			return null;
		} else {
			return StringUtils.join(value.iterator(), ", ");
		}
	}

	@Override
	public List<String> unmarshal(String text) throws Exception {

		if (text == null) {
			return null;
		} else

		{
			final List<String> value = new LinkedList<String>();
			final String[] items = StringUtils.split(text, ',');
			for (String item : items) {
				final String trimmedItem = item.trim();
				if (!StringUtils.isEmpty(trimmedItem)) {
					value.add(trimmedItem);
				}
			}
			return value;
		}
	}

}
