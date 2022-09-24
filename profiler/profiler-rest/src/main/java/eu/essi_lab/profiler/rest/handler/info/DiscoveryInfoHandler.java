package eu.essi_lab.profiler.rest.handler.info;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.rsf.impl.xml.gs.TermFrequencyMapFormatter;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

/**
 * @author Fabrizio
 */
public class DiscoveryInfoHandler extends RestInfoHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	String responseFormat = parser.getValue(RestParameter.RESPONSE_FORMAT.getName());

	if (parser.isValid(RestParameter.RESPONSE_FORMAT.getName())) {

	    return super.validateResponseFormat(responseFormat);
	}

	return message;
    }

    @Override
    protected String createXMLResponse(WebRequest webRequest) throws GSException {

	CountSet countSet = count(webRequest.getRequestId());

	String out = "<gs:discoveryInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	out += " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gs=\"";
	out += CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "\">";

	Optional<TermFrequencyMap> map = countSet.mergeTermFrequencyMaps(100);

	int totalCount = countSet.getCount();
	out += "<gs:recordsCount>" + totalCount + "</gs:recordsCount>";

	if (map.isPresent()) {
	    TermFrequencyMapFormatter formatter = new TermFrequencyMapFormatter(map.get());
	    out += formatter.formatAsXML();
	}

	out += "</gs:discoveryInfo>";

	return out;
    }

    protected CountSet count(String requestId) throws GSException {

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setRequestId(requestId);

	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);

	discoveryMessage.setPage(new Page(1, 1));

	discoveryMessage.setSources(ConfigurationWrapper.getAllSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	

	List<Queryable> queryables = Arrays.asList( //
		MetadataElement.KEYWORD, //
		MetadataElement.DISTRIBUTION_FORMAT, //
		ResourceProperty.SOURCE_ID, //
		MetadataElement.ONLINE_PROTOCOL);//

	// TermFrequencyBond bond = BondFactory.createTermFrequencyBond(queryables);
	// discoveryMessage.setUserBond(bond);

	discoveryMessage.setTermFrequencyTargets(queryables);

	return executor.count(discoveryMessage);
    }
}
