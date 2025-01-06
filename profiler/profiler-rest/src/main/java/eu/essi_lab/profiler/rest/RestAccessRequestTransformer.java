package eu.essi_lab.profiler.rest;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.XML;

import eu.essi_lab.access.compliance.wrapper.DataDescriptorWrapper;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.pdk.wrt.AccessRequestTransformer;
import eu.essi_lab.profiler.rest.handler.info.MessageFormat;
import eu.essi_lab.profiler.rest.handler.info.RestParameter;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class RestAccessRequestTransformer extends AccessRequestTransformer {

    private static final String REST_ACCESS_REQ_TRANSFORMER_WRAP_ERROR = "REST_ACCESS_REQ_TRANSFORMER_WRAP_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	// --------------------------------
	//
	// Checking id
	//
	if (!parser.isValid(RestParameter.IDENTIFIER.getName())) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setLocator(RestParameter.IDENTIFIER.getName());
	    message.setError("Missing resource identifier");
	    return message;
	}

	String id = parser.getValue(RestParameter.IDENTIFIER.getName());

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	discoveryMessage.setRequestId(request.getRequestId());
	
	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);

	discoveryMessage.setPage(new Page(1, 1));

	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	

	SimpleValueBond bond = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.ONLINE_ID, //
		id);

	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	if (resultSet.getResultsList().isEmpty()) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError("Unknown identifier: " + id);
	    message.setLocator(RestParameter.IDENTIFIER.getName());

	    return message;
	}

	// --------------------------------
	//
	// Checking request format
	//
	if (parser.isValid(RestParameter.REQUEST_FORMAT.getName())) {

	    try {

		MessageFormat.fromFormat(parser.getValue(RestParameter.REQUEST_FORMAT.getName()));

	    } catch (IllegalArgumentException ex) {

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError("Invalid value for parameter " + RestParameter.REQUEST_FORMAT.getName() + ". Allowed values are: "
			+ Arrays.asList(MessageFormat.values()).//
				stream().//
				map(f -> f.getFormat()).//
				collect(Collectors.toList()));

		message.setLocator(RestParameter.REQUEST_FORMAT.getName());

		return message;
	    }
	}

	// --------------------------------
	//
	// Checking request content
	//

	if (request.getBodyStream() != null && request.getBodyStream().getLength() > 0) {

	    MessageFormat format = MessageFormat
		    .fromFormat(parser.getValue(RestParameter.REQUEST_FORMAT.getName(), MessageFormat.XML.getFormat()));

	    switch (format) {
	    case XML:

		try {
		    DataDescriptorWrapper.wrap(request.getBodyStream().clone());

		} catch (Exception e) {

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("Invalid XML request" + (e.getMessage() != null ? " :" + e.getMessage() : ""));

		    return message;
		}

		break;

	    case JSON:

		try {

		    JSONUtils.fromStream(request.getBodyStream().clone());

		} catch (Exception e) {

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError("Invalid JSON request" + (e.getMessage() != null ? " :" + e.getMessage() : ""));

		    return message;
		}
	    }
	}

	return message;
    }

    @Override
    protected String getOnlineId(WebRequest request) throws GSException {

	String queryString = request.getQueryString();
	KeyValueParser parser = new KeyValueParser(queryString);

	return parser.getValue("id");
    }

    @Override
    protected Optional<DataDescriptor> getTargetDescriptor(WebRequest request) throws GSException {

	if (request.isGetRequest()) {
	    return Optional.empty();
	}

	InputStream stream = request.getBodyStream().clone();

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	MessageFormat format = MessageFormat.fromFormat(parser.getValue(RestParameter.REQUEST_FORMAT.getName()));

	DataDescriptor descriptor = null;

	try {

	    switch (format) {
	    case XML:

		descriptor = DataDescriptorWrapper.wrap(stream);

		break;

	    case JSON:

		String json;

		json = IOStreamUtils.asUTF8String(stream);
		String xml = XML.toString(new JSONObject(json));
		xml = xml.replaceAll("<gs:DataDescriptor", "<gs:DataDescriptor xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\"");

		descriptor = DataDescriptorWrapper.wrap(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

		break;
	    }
	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    REST_ACCESS_REQ_TRANSFORMER_WRAP_ERROR, //
		    e);
	}

	return Optional.of(descriptor);
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

    @Override
    public String getProfilerType() {

	return new RestProfilerSetting().getServiceType();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
