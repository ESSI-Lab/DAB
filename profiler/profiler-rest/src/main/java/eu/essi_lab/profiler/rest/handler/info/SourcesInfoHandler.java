package eu.essi_lab.profiler.rest.handler.info;

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

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class SourcesInfoHandler extends DiscoveryInfoHandler {

    @Override
    protected String createXMLResponse(WebRequest webRequest) throws GSException {

	List<GSSource> sources = ConfigurationWrapper.getAllSources();

	String out = "<gs:sourcesInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	out += " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gs=\"";
	out += CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "\">";
	out += "<gs:sourcesCount>" + sources.size() + "</gs:sourcesCount>";

	for (GSSource source : sources) {

	    String label = source.getLabel();
	    String id = source.getUniqueIdentifier();
	    String endpoint = source.getEndpoint();
	    BrokeringStrategy brokeringStrategy = source.getBrokeringStrategy();
	    SortOrder sortOrder = source.getSortOrder();
	    ResultsPriority resultsPriority = source.getResultsPriority();

	    out += "<gs:source gs:orderingDirection=\"" + sortOrder + "\" gs:resultsPriority=\"" + resultsPriority + "\" gs:strategy=\""
		    + brokeringStrategy + "\" gs:uniqueIdentifier=\"" + id + "\">";
	    out += "<gs:label>" + StringEscapeUtils.escapeXml11(label) + "</gs:label>";
	    out += "<gs:endpoint>" + StringEscapeUtils.escapeXml11(endpoint) + "</gs:endpoint>";
	    out += "</gs:source>";
	}

	out += "</gs:sourcesInfo>";

	return out;
    }
}
