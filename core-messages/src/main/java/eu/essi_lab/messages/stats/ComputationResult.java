/**
 * 
 */
package eu.essi_lab.messages.stats;

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

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.model.index.jaxb.CardinalValues;

/**
 * @author Fabrizio
 */
public class ComputationResult {

    @XmlTransient
    private String target;
    @XmlTransient
    private String value;

    /**
     * Separates the frequency item from the frequency value (e.g: some source idITEMSEP12)
     */
    public static final String FREQUENCY_ITEM_SEP = "ITEMSEP";

    /**
     * @return
     */
    @XmlAttribute(name = "target", required = false)
    public String getTarget() {
	return target;
    }

    /**
     * @param target
     */
    public void setTarget(String target) {
	this.target = target;
    }

    /**
     * @return
     */
    @XmlValue
    public String getValue() {

	return value;
    }

    @XmlTransient
    public Optional<CardinalValues> getCardinalValues() {

	CardinalValues values = new CardinalValues();

	try {
	    // west, south, east, north
	    String west = value.split(" ")[0];
	    String south = value.split(" ")[1];
	    String east = value.split(" ")[2];
	    String north = value.split(" ")[3];

	    values.setWest(west);
	    values.setEast(east);
	    values.setNorth(north);
	    values.setSouth(south);

	    return Optional.of(values);

	} catch (IndexOutOfBoundsException ex) {
	    return Optional.empty();
	}
    }

    @XmlTransient
    public List<TermFrequencyItem> getFrequencyItems() {

	return Arrays.asList(getValue().split(" ")).//
		stream().//
		map(f -> {

		    if (f.equals("")) {
			return null;
		    }

		    TermFrequencyItem item = new TermFrequencyItem();
		    item.setLabel(getTarget());

		    String[] values = f.split(FREQUENCY_ITEM_SEP);
		    try {
			item.setTerm(URLDecoder.decode(values[0], "UTF-8"));

		    } catch (Exception e) {
		    }

		    item.setDecodedTerm(values[0]);
		    item.setFreq(Integer.valueOf(values[1]));

		    return item;

		}).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());
    }

    /**
     * west south east north
     * 
     * @param value
     */
    public void setValue(String value) {
	this.value = value;
    }
}
