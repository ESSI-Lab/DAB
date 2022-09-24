package eu.essi_lab.accessor.wof;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.ispra.ISPRAConnector;
import eu.essi_lab.accessor.wof.client.CUAHSIHISCentralClient;
import eu.essi_lab.accessor.wof.client.datamodel.ServiceInfo;
import eu.essi_lab.accessor.wof.setting.CUAHSIHISCentralConnectorSetting;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CUAHSIHISCentralConnector extends HarvestedQueryConnector<CUAHSIHISCentralConnectorSetting> {

    protected List<String> failedServices = new ArrayList<>();

    private CUAHSIHISCentralClient client = null;

    private List<ServiceInfo> services = null;

    private String currentServicePosition = null;

    @SuppressWarnings("rawtypes")
    private CUAHSIHISServerConnector currentConnector = null;

    private Integer recordsReturned = 0;
    private static final String CANT_RESUME_FROM_RESUMPTION_TOKEN = "Unable to resume from resumption token: ";

    /**
     * 
     */
    public static final String TYPE = "CUAHSIHISCentralConnector";

    private static final String CUAHSI_HIS_CENTRAL_CONNECTOR_ERROR = "CUAHSI_HIS_CENTRAL_CONNECTOR_ERROR";

    @Override
    public boolean supports(GSSource source) {
	String baseEndpoint = source.getEndpoint();
	if (baseEndpoint == null) {
	    return false;
	}

	try {
	    CUAHSIHISCentralClient testClient = new CUAHSIHISCentralClient(baseEndpoint);
	    List<ServiceInfo> servs = testClient.getServicesInBox("-180", "-90", "180", "90");
	    if (!servs.isEmpty()) {
		return true;
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(this.getClass()).warn("Exception during download or during XML parsing: {}", e.getMessage());
	}
	return false;
    }

    /**
     * 
     */
    public CUAHSIHISCentralConnector() {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	if (client == null) {
	    client = new CUAHSIHISCentralClient(getSourceURL());
	}

	if (services == null) {
	    services = client.getServicesInBox("-180", "-90", "180", "90");
	}

	String id = listRecords.getResumptionToken();

	String nextId = null;

	String childResumptionToken = null;

	Integer servicePositionInt = null;

	String servicePosition = null;

	if (id == null) {
	    // first request
	    servicePosition = "0";
	    servicePositionInt = 0;
	    childResumptionToken = null;

	} else {

	    if (!id.contains(":")) {

		throw GSException.createException(//
			getClass(), //
			CANT_RESUME_FROM_RESUMPTION_TOKEN + id, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CUAHSI_HIS_CENTRAL_CONNECTOR_ERROR); //
	    }

	    String[] split = id.split(":");

	    servicePosition = split[0];

	    try {
		servicePositionInt = Integer.parseInt(servicePosition);
	    } catch (NumberFormatException e) {

		throw GSException.createException(//
			getClass(), //
			CANT_RESUME_FROM_RESUMPTION_TOKEN + id + ", " + e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CUAHSI_HIS_CENTRAL_CONNECTOR_ERROR, //
			e); //
	    }

	    childResumptionToken = id.substring(id.indexOf(':') + 1);
	    if (childResumptionToken.isEmpty()) {
		childResumptionToken = null;
	    }

	}

	ServiceInfo serviceInfo = null;

	if (currentServicePosition == null || !currentServicePosition.equals(servicePosition)) {

	    if (servicePositionInt < 0 || services.size() < (servicePositionInt + 1)) {

		throw GSException.createException(//
			getClass(), //
			CANT_RESUME_FROM_RESUMPTION_TOKEN + id, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CUAHSI_HIS_CENTRAL_CONNECTOR_ERROR); //
	    }

	    serviceInfo = services.get(servicePositionInt);
	    GSLoggerFactory.getLogger(this.getClass()).info("Creating connector for service: {}", serviceInfo.getTitle());
	    currentServicePosition = servicePosition;
	    String url = serviceInfo.getServiceURL();
	    if (url.contains("hydroserver.ddns.net/italia")) {
		currentConnector = new ISPRAConnector();
	    } else {
		currentConnector = new CUAHSIHISServerConnector();
	    }
	    currentConnector.setFirstSiteOnly(isFirstSiteOnly());
	    currentConnector.setSourceURL(serviceInfo.getServiceURL());

	    Optional<Integer> mr = getSetting().getMaxRecords();
	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent()) {
		Integer maxRecords = mr.get();
		currentConnector.getSetting().setMaxRecords(maxRecords);
	    }

	}

	String childNextResumptionToken = null;

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	ListRecordsRequest childListRecords = new ListRecordsRequest();
	childListRecords.setResumptionToken(childResumptionToken);
	try {
	    ret = currentConnector.listRecords(childListRecords);
	    childNextResumptionToken = ret.getResumptionToken();
	    Iterator<OriginalMetadata> iterator = ret.getRecords();
	    if (iterator.hasNext()) {
		recordsReturned++;

		Optional<Integer> mr = getSetting().getMaxRecords();

		if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent()) {
		    Integer maxRecords = mr.get();
		    if (recordsReturned >= maxRecords) {
			GSLoggerFactory.getLogger(this.getClass()).info("Reached max records of {}", maxRecords);
			printServiceErrors();
			ret.setResumptionToken(null);
			return ret;
		    }
		}
	    }
	} catch (GSException e) {
	    GSLoggerFactory.getLogger(this.getClass()).error("CUAHSI HIS Server returned unexpected errors, skipping");
	    if (serviceInfo != null) {
		failedServices.add(servicePositionInt + ": " + serviceInfo.getTitle());
		GSLoggerFactory.getLogger(this.getClass()).error("HIS Server position: {}", servicePositionInt);
		GSLoggerFactory.getLogger(this.getClass()).error("HIS Server name: {}", serviceInfo.getTitle());
		GSLoggerFactory.getLogger(this.getClass()).error("HIS Server URL: {}", serviceInfo.getServiceURL());
	    }
	}

	if (childNextResumptionToken == null) {
	    // reached the end of the child service
	    if (services.size() > (servicePositionInt + 1)) {
		// move to next server
		nextId = (servicePositionInt + 1) + ":";
	    } else {
		// reached the end of the service list
		printServiceErrors();
		nextId = null;
	    }

	} else {
	    nextId = currentServicePosition + ":" + childNextResumptionToken;

	}

	ret.setResumptionToken(nextId);
	return ret;

    }

    /**
     * Prints the list of service errors occurred during the harvesting. See
     * {@link CUAHSIHISCentralConnectorExternalTestIT} for
     * a list of known service errors
     */
    private void printServiceErrors() {

	GSLoggerFactory.getLogger(this.getClass()).error("CUAHSICentralHarvestFinished Got errors from the following services:");
	if (failedServices.isEmpty()) {
	    GSLoggerFactory.getLogger(this.getClass()).error("None!");
	}
	for (String failedService : failedServices) {
	    GSLoggerFactory.getLogger(this.getClass()).error(failedService);
	}
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.WML1_NS_URI);
	return ret;
    }

    /**
     * @param firstSiteOnly
     */
    public void setFirstSiteOnly(Boolean firstSiteOnly) {

	getSetting().setHarvestFirstSiteOnly(firstSiteOnly);
    }

    protected boolean isFirstSiteOnly() {

	return getSetting().isFirstSiteHarvestOnlySet();
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected CUAHSIHISCentralConnectorSetting initSetting() {

	return new CUAHSIHISCentralConnectorSetting();
    }

}
