package eu.essi_lab.accessor.wms.map;

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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.accessor.wms.WMSProfiler;
import eu.essi_lab.accessor.wms.WMSRequest.Parameter;
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
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.pdk.wrt.AccessRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class WMSMapTransformer extends AccessRequestTransformer {

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
		WMSMapRequest mapRequest = new WMSMapRequest(request);

		String format = mapRequest.getParameterValue(Parameter.FORMAT);
		descriptor.setDataFormat(DataFormat.fromIdentifier(format));

		CRS crs = CRS.fromIdentifier(mapRequest.getParameterValue(Parameter.CRS));
		descriptor.setCRS(crs);

		String time = mapRequest.getParameterValue(Parameter.TIME);
		if (time != null) {
		    if (time.contains(",")) {
			time = time.split(",")[0];
		    }
		    Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(time);
		    if (optionalDate.isPresent()) {
			Date date = optionalDate.get();
			descriptor.setTemporalDimension(date, date);
		    }
		}

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

		String bbox = mapRequest.getParameterValue(Parameter.BBOX);
		String[] split = bbox.split(",");
		List<DataDimension> dimensions = descriptor.getSpatialDimensions();
		ContinueDimension dimension1 = dimensions.get(0).getContinueDimension();
		dimension1.setLower(Double.parseDouble(split[0]));
		dimension1.setUpper(Double.parseDouble(split[2]));
		ContinueDimension dimension2 = dimensions.get(1).getContinueDimension();
		dimension2.setLower(Double.parseDouble(split[1]));
		dimension2.setUpper(Double.parseDouble(split[3]));
		descriptor.setSpatialDimensions(dimensions);

		String heightString = mapRequest.getParameterValue(Parameter.HEIGHT);
		Long height = Long.parseLong(heightString);
		String widthString = mapRequest.getParameterValue(Parameter.WIDTH);
		Long width = Long.parseLong(widthString);
		if (crs == null || crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		    dimension1.setSize(height);
		    dimension2.setSize(width);
		} else {
		    dimension2.setSize(height);
		    dimension1.setSize(width);
		}

		dimension1.setResolution(null);
		dimension2.setResolution(null);
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
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	

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

	return report.getFullDataDescriptor();

    }

    @Override
    protected String getOnlineId(WebRequest request) throws GSException {

	try {

	    WMSMapRequest mapRequest = new WMSMapRequest(request);

	    String onlineId = mapRequest.getParameterValue(eu.essi_lab.accessor.wms.WMSRequest.Parameter.LAYERS);

	    return onlineId;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't read variable", e);

	}

	return null;
    }

    @Override
    public String getProfilerType() {

	return WMSProfiler.WMS_SERVICE_INFO.getServiceType();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

}
