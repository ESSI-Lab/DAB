package eu.essi_lab.profiler.sos.observation;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.pdk.wrt.AccessRequestTransformer;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.sos.SOSProfiler;
import eu.essi_lab.profiler.sos.SOSRequest;
import eu.essi_lab.profiler.sos.SOSUtils;
import eu.essi_lab.profiler.sos.SOSRequest.Parameter;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class GetObservationTransformer extends AccessRequestTransformer {

    /**
     * 
     */
    private static final String GET_OBSERVATION_TRANSFORMER_ERROR = "GET_OBSERVATION_TRANSFORMER_ERROR";

    public String getOnlineId(WebRequest request) throws GSException {

	try {

	    SOSRequest sosRequest = getRequest(request);

	    String uniqueVariableCode = sosRequest.getParameterValue(Parameter.PROCEDURE);
	    if (uniqueVariableCode != null) {
		uniqueVariableCode = uniqueVariableCode.replace(SOSUtils.PROCEDURE_PREFIX, "");
	    }

	    String uniqueSiteCode = sosRequest.getParameterValue(Parameter.FEATURE_OF_INTEREST);

	    Optional<String> optionalViewId = request.extractViewId();

	    String onlineId = getOnlineId(optionalViewId, uniqueSiteCode, uniqueVariableCode);

	    return onlineId;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't read variable", e);

	}

	return null;
    }

    private String getOnlineId(Optional<String> optionalViewId, String siteCode, String variableCode) throws GSException {
	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	if (optionalViewId.isPresent()) {
	    StorageInfo storageUri = ConfigurationWrapper.getDatabaseURI();

	    Optional<View> optionalView = WebRequestTransformer.findView(storageUri, optionalViewId.get());

	    if (optionalView.isPresent()) {
		discoveryMessage.setView(optionalView.get());
	    }
	}

	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);

	discoveryMessage.setPage(new Page(1, 1));

	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

	List<Bond> bonds = new ArrayList<>();

	if (variableCode != null) {
	    bonds.add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
		    variableCode));
	}

	if (siteCode != null) {
	    bonds.add(BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, //
		    siteCode));
	}

	Bond bond;

	switch (bonds.size()) {
	case 0:
	    bond = null;
	    break;
	case 1:
	    bond = bonds.get(0);
	    break;
	default:
	    bond = BondFactory.createAndBond(bonds);
	    break;
	}

	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	if (resultSet.getResultsList().isEmpty()) {
	    return null;
	}
	GSResource result = resultSet.getResultsList().get(0);

	ReportsMetadataHandler handler = new ReportsMetadataHandler(result);

	List<DataComplianceReport> reports = handler.getReports();
	Optional<DataComplianceReport> optReport = reports.stream()
		.filter(r -> r.getExecutionResult().get().getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL)).findFirst();
	if (!optReport.isPresent()) {

	    return result.getHarmonizedMetadata().getCoreMetadata().getOnline().getIdentifier();
	}

	DataComplianceReport report = optReport.get();

	return report.getOnlineId();

    }

    public SOSRequest getRequest(WebRequest request) {
	return new GetObservationRequest(request);
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	String id = getOnlineId(request);
	if (id == null) {
	    ValidationMessage message = new ValidationMessage();
	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError("No data found with the specified parameters");
	    message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
	    return message;
	}

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	message.setError(null);
	return message;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    protected Optional<DataDescriptor> getTargetDescriptor(WebRequest request) throws GSException {

	try {

	    String id = getOnlineId(request);

	    DataDescriptor descriptor = getDefaultDescriptor(id);

	    if (descriptor != null) {

		GetObservationRequest getValuesRequest = new GetObservationRequest(request);

		String format = getValuesRequest.getParameterValue(Parameter.RESPONSE_FORMAT);
		descriptor.setDataFormat(DataFormat.O_M());
		if (format != null && !format.equals("http://www.opengis.net/om/2.0")) {
		    GSLoggerFactory.getLogger(getClass()).warn("Unexpected format requested by client: " + format);
		}

		String instantPosition = getValuesRequest.getParameterValue(Parameter.TIME_POSITION);

		String startDate = getValuesRequest.getParameterValue(Parameter.BEGIN_POSITION);

		String endDate = getValuesRequest.getParameterValue(Parameter.END_POSITION);

		if (instantPosition != null) {
		    startDate = instantPosition;
		    endDate = instantPosition;
		}

		if (startDate == null || startDate.equals("")) {
		    if (endDate == null || endDate.equals("")) {
			// if no data specified, requires the last data available (the more recent)
			Number upper = descriptor.getTemporalDimension().getContinueDimension().getUpper();
			descriptor.getTemporalDimension().getContinueDimension().setLower(upper);
			descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.MAXIMUM);
			descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.MAXIMUM);
		    } else {
			// START NULL, END SPECIFIED
			descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.MINIMUM);
		    }
		} else if (endDate == null || endDate.equals("")) {
		    // START SPECIFIED, END NULL
		    descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.MAXIMUM);
		}

		if (startDate != null && !startDate.equals("")) {

		    if (startDate.toLowerCase().equals("first")) {

			descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.MINIMUM);

		    } else if (startDate.toLowerCase().equals("last")) {

			descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.MAXIMUM);

		    } else {

			Date start = ISO8601DateTimeUtils.parseISO8601(startDate);

			descriptor.getTemporalDimension().getContinueDimension().setLower(start.getTime());
		    }
		}
		if (endDate != null && !endDate.equals("")) {

		    if (endDate.toLowerCase().equals("first")) {

			descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.MINIMUM);

		    } else if (endDate.toLowerCase().equals("last")) {

			descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.MAXIMUM);

		    } else {

			Date end = ISO8601DateTimeUtils.parseISO8601(endDate);

			descriptor.getTemporalDimension().getContinueDimension().setUpper(end.getTime());

		    }

		}

		GSLoggerFactory.getLogger(getClass()).info("SOS data request for interval: " + startDate + "/" + endDate);

		return Optional.of(descriptor);
	    }

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GET_OBSERVATION_TRANSFORMER_ERROR);
	}

	return Optional.empty();
    }

    private DataDescriptor getDefaultDescriptor(String onlineIdentifier) throws GSException {
	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);

	discoveryMessage.setPage(new Page(1, 1));

	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

	SimpleValueBond bond = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.ONLINE_ID, //
		onlineIdentifier);

	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	if (resultSet.getResultsList().isEmpty()) {
	    return null;
	}
	GSResource result = resultSet.getResultsList().get(0);
	ReportsMetadataHandler handler = new ReportsMetadataHandler(result);

	List<DataComplianceReport> reports = handler.getReports();
	Optional<DataComplianceReport> optReport = reports.stream().filter(r -> r.getOnlineId().equals(onlineIdentifier)).findFirst();
	if (!optReport.isPresent()) {

	    // TODO: error

	    return null;
	}

	DataComplianceReport report = optReport.get();

	return report.getFullDataDescriptor();

    }

    @Override
    public String getProfilerType() {

	return SOSProfiler.SOS_SERVICE_INFO.getServiceType();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

}
