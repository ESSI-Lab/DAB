package eu.essi_lab.pdk.rsf.impl.xml.gs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;
import org.json.XML;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.ItemsSortOrder;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class TermFrequencyMapFormatter {

    private TermFrequencyMap map;
    private static final String TERMFREQ_MAP_NOSOURCE = "TERMFREQ_MAP_NOSOURCE";

    /**
     * @param map
     */
    public TermFrequencyMapFormatter(TermFrequencyMap map) {

	this.map = map;
    }

    /**
     * @return
     * @throws GSException
     */
    public String formatAsXML() throws GSException {

	String out = "<gs:sourceStatistics>";
	out += "<gs:sourcesCount>" + ConfigurationWrapper.getAllSources().size() + "</gs:sourcesCount>";

	{
	    List<TermFrequencyItem> items = map.getItems(TermFrequencyTarget.SOURCE, ItemsSortOrder.BY_FREQUENCY);
	    for (TermFrequencyItem item : items) {
		String label = item.getLabel();
		out += "<gs:source label='" + find(label).getLabel() + "' id='" + label + "' recordsCount='" + item.getFreq() + "'/>";
	    }
	    out += "</gs:sourceStatistics>";
	}

	{
	    out += "<gs:keywordStatistics>";

	    List<TermFrequencyItem> items = map.getItems(TermFrequencyTarget.KEYWORD, ItemsSortOrder.BY_FREQUENCY);
	    out += "<gs:keywordsCount>" + items.size() + "</gs:keywordsCount>";

	    for (TermFrequencyItem item : items) {
		String label = item.getLabel();
		out += "<gs:keyword value='" + label + "' frequency='" + item.getFreq() + "'/>";

	    }
	    out += "</gs:keywordStatistics>";
	}

	{
	    out += "<gs:formatStatistics>";
	    List<TermFrequencyItem> items = map.getItems(TermFrequencyTarget.FORMAT, ItemsSortOrder.BY_FREQUENCY);
	    out += "<gs:formatsCount>" + items.size() + "</gs:formatsCount>";

	    for (TermFrequencyItem item : items) {
		String label = item.getLabel();
		out += "<gs:format value='" + label + "' frequency='" + item.getFreq() + "'/>";
	    }
	    out += "</gs:formatStatistics>";
	}

	{
	    out += "<gs:protocolStatistics>";
	    List<TermFrequencyItem> items = map.getItems(TermFrequencyTarget.PROTOCOL, ItemsSortOrder.BY_FREQUENCY);
	    out += "<gs:protocolsCount>" + items.size() + "</gs:protocolsCount>";

	    for (TermFrequencyItem item : items) {
		String label = item.getLabel();
		out += "<gs:protocol value='" + label + "' frequency='" + item.getFreq() + "'/>";
	    }
	    out += "</gs:protocolStatistics>";
	}

	return out;
    }

    /**
     * @return
     * @throws GSException
     */
    public String formatAsJSON() throws GSException {

	String xml = formatAsXML();
	JSONObject jsonObject = XML.toJSONObject(xml);

	JSONUtils.clearNSDeclarations(jsonObject);

	return jsonObject.toString(4);
    }

    private GSSource find(String sourceId) throws GSException {

	Optional<GSSource> first = ConfigurationWrapper.getAllSources().//
		stream().//
		filter(s -> s.getUniqueIdentifier().equals(sourceId)).//
		findFirst();

	if (first.isPresent())
	    return first.get();

	throw GSException.createException(getClass(), "Can't find source " + sourceId, null, ErrorInfo.ERRORTYPE_INTERNAL,
		ErrorInfo.SEVERITY_ERROR, TERMFREQ_MAP_NOSOURCE);
    }
}
