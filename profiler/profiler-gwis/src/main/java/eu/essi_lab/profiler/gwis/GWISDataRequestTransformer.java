package eu.essi_lab.profiler.gwis;

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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.pdk.wrt.AccessRequestTransformer;
import eu.essi_lab.profiler.gwis.request.data.GWISDataRequest;
import eu.essi_lab.profiler.gwis.request.data.GWISDataRequest.Parameter;
import eu.essi_lab.profiler.gwis.request.data.GWISDataRequestValidator;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class GWISDataRequestTransformer extends AccessRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	GWISDataRequestValidator validator = new GWISDataRequestValidator();

	ValidationMessage ret = validator.validate(request);

	return ret;

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

		GWISDataRequest dataRequest = new GWISDataRequest(request);

		descriptor.setDataFormat(DataFormat.RDB());

		String startDate = dataRequest.getParameterValue(Parameter.START_DT);

		String endDate = dataRequest.getParameterValue(Parameter.END_DT);

		if (startDate != null && !startDate.equals("") && endDate != null && !endDate.equals("")) {

		    Date start = ISO8601DateTimeUtils.parseISO8601ToDate(startDate).get();

		    Date end = ISO8601DateTimeUtils.parseISO8601ToDate(endDate).get();

		    descriptor.getTemporalDimension().getContinueDimension().setLower(start.getTime());

		    descriptor.getTemporalDimension().getContinueDimension().setUpper(end.getTime());

		}

		descriptor.getTemporalDimension().getContinueDimension().setResolution(null);
		descriptor.getTemporalDimension().getContinueDimension().setResolutionTolerance(null);

		return Optional.of(descriptor);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't get target descriptor", e);

	}

	return Optional.empty();
    }

    private String getOnlineId(String requestId, String siteCode, String variableCode) throws GSException {
	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setRequestId(requestId);

	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);

	discoveryMessage.setPage(new Page(1, 1));

	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	
	discoveryMessage.setResultsPriority(ResultsPriority.ALL);

	SimpleValueBond bond1 = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
		variableCode);
	SimpleValueBond bond2 = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, //
		siteCode);
	LogicalBond bond = BondFactory.createAndBond(bond1, bond2);

	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	if (resultSet.getResultsList().isEmpty()) {
	    // TODO: error!
	}
	GSResource result = resultSet.getResultsList().get(0);
	ReportsMetadataHandler handler = new ReportsMetadataHandler(result);

	List<DataComplianceReport> reports = handler.getReports();
	Optional<DataComplianceReport> optReport = reports.stream()
		.filter(r -> r.getExecutionResult().get().getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL)).findFirst();
	if (!optReport.isPresent()) {

	    // TODO: error

	    return null;
	}

	DataComplianceReport report = optReport.get();

	return report.getOnlineId();

    }

    private DataDescriptor getDefaultDescriptor(String onlineIdentifier) throws GSException {
	GSResource result = retrieveResource(onlineIdentifier);

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

    public GSResource retrieveResource(WebRequest request) throws GSException {
	String onlineId = getOnlineId(request);
	return retrieveResource(onlineId);
    }

    private GSResource retrieveResource(String onlineIdentifier) throws GSException {
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
	    // TODO: error!
	}
	GSResource result = resultSet.getResultsList().get(0);
	return result;
    }

    @Override
    protected String getOnlineId(WebRequest request) throws GSException {

	try {

	    GWISDataRequest dataRequest = new GWISDataRequest(request);

	    String uniqueVariableCode = dataRequest.getParameterValue(Parameter.PARAMETER_CODE);

	    String uniqueSiteCode = dataRequest.getParameterValue(Parameter.SITE_CODE);

	    String onlineId = getOnlineId(request.getRequestId(), uniqueSiteCode, uniqueVariableCode);

	    return onlineId;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't read variable", e);

	}

	return null;
    }

    @Override
    public String getProfilerType() {

	return GWISProfiler.GWIS_INFO.getServiceType();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

}
