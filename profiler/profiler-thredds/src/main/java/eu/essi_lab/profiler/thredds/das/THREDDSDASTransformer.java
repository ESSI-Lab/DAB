package eu.essi_lab.profiler.thredds.das;

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

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.pdk.wrt.AccessRequestTransformer;
import eu.essi_lab.profiler.thredds.THREDDSProfilerSetting;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class THREDDSDASTransformer extends AccessRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
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

	    DataDescriptor descriptor = getDefaultDescriptor(request.getRequestId(), id);

	    if (descriptor != null) {

		descriptor.setDataFormat(DataFormat.DAS());
		
		DataDimension temporalDimension = descriptor.getTemporalDimension();
		if (temporalDimension != null) {
		    // Number lower = temporalDimension.getContinueDimension().getLower();
		    // if (lower != null) {
		    // temporalDimension.getContinueDimension().setUpper(lower);
		    temporalDimension.getContinueDimension().setLower(null);
		    temporalDimension.getContinueDimension().setUpper(null);
		    temporalDimension.getContinueDimension().setSize(1l);

		    // }
		}

		return Optional.of(descriptor);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't get target descriptor", e);

	}

	return Optional.empty();
    }

    private DataDescriptor getDefaultDescriptor(String requestId, String onlineIdentifier) throws GSException {
	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setRequestId(requestId);
	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.EXTENDED);

	discoveryMessage.setPage(new Page(1, 1));

	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	

	SimpleValueBond bond = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.ONLINE_ID, //
		onlineIdentifier);

	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	discoveryMessage.setResultsPriority(ResultsPriority.ALL);

	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);

	if (resultSet.getResultsList().isEmpty()) {
	    // TODO: error!
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

	return report.getPreviewDataDescriptor();

    }

    @Override
    protected String getOnlineId(WebRequest request) throws GSException {

	try {

	    String path = request.getRequestPath();
	    path = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
	    path = path.replace("urn-uuid-", "urn:uuid:");	    
	    return path;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't read online id", e);

	}

	return null;
    }

    @Override
    public String getProfilerType() {

	return new THREDDSProfilerSetting().getServiceType();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

}
