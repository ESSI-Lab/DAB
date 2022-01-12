package eu.essi_lab.accessor.wof;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.accessor.ispra.ISPRAConnector;
import eu.essi_lab.accessor.wof.client.CUAHSIHISCentralClient;
import eu.essi_lab.accessor.wof.client.datamodel.ServiceInfo;
import eu.essi_lab.cdk.harvest.AbstractHarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CUAHSIHISCentralConnector extends AbstractHarvestedQueryConnector {

    @JsonIgnore
    protected transient List<String> failedServices = new ArrayList<>();
    @JsonIgnore
    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());
    @JsonIgnore
    private transient CUAHSIHISCentralClient client = null;
    @JsonIgnore
    private transient List<ServiceInfo> services = null;
    @JsonIgnore
    private transient String currentServicePosition = null;
    @JsonIgnore
    private transient CUAHSIHISServerConnector currentConnector = null;
    @JsonIgnore
    private static final String FIRST_SITE_ONLY_OPTION_KEY = "FIRST_SITE_ONLY_OPTION_KEY";

    @JsonIgnore
    private Integer recordsReturned = 0;
    private static final String CANT_RESUME_FROM_RESUMPTION_TOKEN = "Unable to resume from resumption token: ";

    @Override
    public boolean supports(Source source) {
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

	    logger.warn("Exception during download or during XML parsing: {}", e.getMessage());
	}
	return false;
    }

    public CUAHSIHISCentralConnector() {

	GSConfOptionBoolean option = new GSConfOptionBoolean();

	option.setLabel("Harvest first site only");
	option.setKey(FIRST_SITE_ONLY_OPTION_KEY);
	option.setValue(false);

	getSupportedOptions().put(FIRST_SITE_ONLY_OPTION_KEY, option);
    }

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
		GSException gse = new GSException();
		ErrorInfo info = new ErrorInfo();
		info.setErrorDescription(CANT_RESUME_FROM_RESUMPTION_TOKEN + id);
		gse.addInfo(info);
		throw gse;
	    }

	    String[] split = id.split(":");

	    servicePosition = split[0];

	    try {
		servicePositionInt = Integer.parseInt(servicePosition);
	    } catch (NumberFormatException e) {
		GSException gse = new GSException();
		ErrorInfo info = new ErrorInfo();
		info.setErrorDescription(CANT_RESUME_FROM_RESUMPTION_TOKEN + id);
		gse.addInfo(info);
		throw gse;
	    }

	    childResumptionToken = id.substring(id.indexOf(':') + 1);
	    if (childResumptionToken.isEmpty()) {
		childResumptionToken = null;
	    }

	}

	ServiceInfo serviceInfo = null;

	if (currentServicePosition == null || !currentServicePosition.equals(servicePosition)) {

	    if (servicePositionInt < 0 || services.size() < (servicePositionInt + 1)) {
		GSException gse = new GSException();
		ErrorInfo info = new ErrorInfo();
		info.setErrorDescription(CANT_RESUME_FROM_RESUMPTION_TOKEN + id);
		gse.addInfo(info);
		throw gse;
	    }

	    serviceInfo = services.get(servicePositionInt);
	    logger.info("Creating connector for service: {}", serviceInfo.getTitle());
	    currentServicePosition = servicePosition;
	    String url = serviceInfo.getServiceURL();
	    if (url.contains("hydroserver.ddns.net/italia")) {
		currentConnector = new ISPRAConnector();
	    }else {
		currentConnector = new CUAHSIHISServerConnector();
	    }			    
	    currentConnector.setFirstSiteOnly(isFirstSiteOnly());
	    currentConnector.setSourceURL(serviceInfo.getServiceURL());
	    Optional<Integer> mr = getMaxRecords();
	    if (!isMaxRecordsUnlimited() && mr.isPresent()) {
		Integer maxRecords = mr.get();
		currentConnector.setMaxRecords(maxRecords);
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

		Optional<Integer> mr = getMaxRecords();

		if (!isMaxRecordsUnlimited() && mr.isPresent()) {
		    Integer maxRecords = mr.get();
		    if (recordsReturned >= maxRecords) {
			logger.info("Reached max records of {}", maxRecords);
			printServiceErrors();
			ret.setResumptionToken(null);
			return ret;
		    }
		}
	    }
	} catch (GSException e) {
	    logger.error("CUAHSI HIS Server returned unexpected errors, skipping");
	    if (serviceInfo != null) {
		failedServices.add(servicePositionInt + ": " + serviceInfo.getTitle());
		logger.error("HIS Server position: {}", servicePositionInt);
		logger.error("HIS Server name: {}", serviceInfo.getTitle());
		logger.error("HIS Server URL: {}", serviceInfo.getServiceURL());
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
    private void printServiceErrors() {

	logger.error("CUAHSICentralHarvestFinished Got errors from the following services:");
	if (failedServices.isEmpty()) {
	    logger.error("None!");
	}
	for (String failedService : failedServices) {
	    logger.error(failedService);
	}
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.WML1_NS_URI);
	return ret;
    }

    @Override
    public String getLabel() {
	return "CUAHSI HIS Central Connector";
    }

    /**
     * @param firstSiteOnly
     */
    public void setFirstSiteOnly(Boolean firstSiteOnly) {

	GSConfOptionBoolean option = (GSConfOptionBoolean) getSupportedOptions().get(FIRST_SITE_ONLY_OPTION_KEY);
	option.setValue(firstSiteOnly);
    }

    private boolean isFirstSiteOnly() {

	GSConfOptionBoolean option = (GSConfOptionBoolean) getSupportedOptions().get(FIRST_SITE_ONLY_OPTION_KEY);
	return option.getValue().equals(true);
    }

}
