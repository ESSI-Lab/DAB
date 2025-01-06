package eu.essi_lab.adk.harvest;

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

import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;
import eu.essi_lab.ommdk.ResourceMapperFactory;

/**
 * @author ilsanto
 */
@SuppressWarnings("rawtypes")
public abstract class HarvestedAccessor<C extends IHarvestedQueryConnector> implements IHarvestedAccessor<C> {

    /**
     * 
     */
    private int maxAttemptsCount;
    /**
     * 
     */
    private static final int DEFAULT_MAX_ATTEMPTS_COUNT = 3;
    /**
     * 
     */
    private static final long SLEEP_TIME = TimeUnit.MINUTES.toMillis(1);
    /**
     * 
     */
    private static final String NO_LIST_RECORDS_ERROR_RESPONSE_ERROR = "NO_LIST_RECORDS_ERROR_RESPONSE";

    private AccessorSetting setting;
    private C connector;
    private ListRecordsRequest listRecords;
    private int tries;

    public HarvestedAccessor() {

	setMaxAttemptsCount(DEFAULT_MAX_ATTEMPTS_COUNT);

	configure();
    }

    /**
     * 
     */
    protected void configure() {

	AccessorSetting setting = AccessorSetting.createHarvested(//
		initAccessorType(), //
		initHarvestedConnectorSetting(), //
		initSettingName());

	String srcLabel = initSourceLabel();
	if (srcLabel != null) {
	    setting.getGSSourceSetting().setSourceLabel(srcLabel);
	}

	String srcEndpoint = initSourceEndpoint();
	if (srcEndpoint != null) {
	    setting.getGSSourceSetting().setSourceEndpoint(srcEndpoint);
	}

	configure(setting);
    }

    /**
     * 
     */
    protected String initSourceLabel() {

	return null;
    }

    /**
     * 
     */
    protected String initSourceEndpoint() {

	return null;
    }

    /**
     * 
     */
    protected abstract String initSettingName();

    /**
     * @return
     */
    protected abstract String initAccessorType();

    /**
     * @return
     */
    protected abstract HarvestedConnectorSetting initHarvestedConnectorSetting();

    @SuppressWarnings("unchecked")
    @Override
    public ListRecordsResponse<GSResource> listRecords(ListRecordsRequest listRecords) throws GSException {

	this.listRecords = listRecords;

	this.tries = 0;

	ListRecordsResponse<OriginalMetadata> driverResponse = null;

	GSLoggerFactory.getLogger(getClass()).debug("List records execution STARTED");

	while (tries < maxAttemptsCount) {

	    tries++;

	    try {
		driverResponse = getConnector().listRecords(listRecords);

		if (driverResponse == null) {

		    String warning = "Null driver response on try n. " + tries;

		    listRecords.getStatus().ifPresent(s -> s.addWarningMessage(warning));

		    GSLoggerFactory.getLogger(getClass()).warn(warning);

		} else {

		    break;
		}

	    } catch (Exception e) {

		String warning = "Error occurred on try n. " + tries + ": " + e.getMessage();

		listRecords.getStatus().ifPresent(s -> s.addWarningMessage(warning));

		GSLoggerFactory.getLogger(getClass()).warn(warning);
	    }

	    if (tries < maxAttemptsCount) {
		try {

		    GSLoggerFactory.getLogger(getClass()).debug("Waiting 60 seconds for a new attempt... {}/{}", (tries + 1),
			    maxAttemptsCount);

		    Thread.sleep(SLEEP_TIME);

		} catch (InterruptedException ex) {

		    GSLoggerFactory.getLogger(getClass()).warn("Interrupted!", ex);
		    Thread.currentThread().interrupt();
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).debug("Too many attempts: {}/{}", tries, maxAttemptsCount);
	    }
	}

	if (driverResponse == null) {

	    String error = "No list records response obtained after " + maxAttemptsCount + " tries. Source URL ["
		    + getConnector().getSourceURL() + "]";

	    listRecords.getStatus().ifPresent(s -> {
		s.addErrorMessage(error);
		s.setPhase(JobPhase.ERROR);
	    });

	    throw GSException.createException(//
		    getClass(), //
		    error, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NO_LIST_RECORDS_ERROR_RESPONSE_ERROR);
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records execution ENDED");

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).debug("Mapping of {} resources STARTED", driverResponse.getRecordsAsList().size());

	ListRecordsResponse<GSResource> response = new ListRecordsResponse<>();

	response.setResumptionToken(driverResponse.getResumptionToken());

	driverResponse.getRecords().forEachRemaining(original -> {

	    try {

		IResourceMapper mapper = getMapper(original.getSchemeURI());

		GSResource resource = mapper.map(original, getSource());

		if (resource != null) {

		    response.addRecord(resource);

		} else {

		    GSLoggerFactory.getLogger(getClass()).warn("Mapped resource is null");
		}
	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	});

	GSLoggerFactory.getLogger(getClass()).debug("Mapping of {} resources ENDED", driverResponse.getRecordsAsList().size());

	return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> listMetadataFormats() throws GSException {

	return getConnector().listMetadataFormats();
    }

    @Override
    public GSSource getSource() {

	return getSetting().getGSSourceSetting().asSource();
    }

    public C getConnector() {

	if (connector == null || this.listRecords == null || this.listRecords.isFirst()) {

	    HarvestedConnectorSetting connectorSetting = getSetting().getHarvestedConnectorSetting();

	    try {
		connector = connectorSetting.createConfigurable();
		connector.setSourceURL(getSetting().getGSSourceSetting().getSourceEndpoint());

	    } catch (Exception e) {

		e.printStackTrace();

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return connector;
    }

    public IResourceMapper getMapper(String schemeUri) {

	return ResourceMapperFactory.getResourceMapper(schemeUri);
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {

	return getConnector().supportsIncrementalHarvesting();
    }

    @Override
    public boolean supportsResumedHarvesting() throws GSException {

	return getConnector().supportsResumedHarvesting();
    }

    @Override
    public boolean supportsRecovery() throws GSException {

	return getConnector().supportsRecovery();
    }

    /**
     * Set the maximum number of connection attempts before throw exception. Default is 3
     * 
     * @param count
     */
    public void setMaxAttemptsCount(int count) {

	this.maxAttemptsCount = count;
    }

    @Override
    public void configure(AccessorSetting setting) {

	this.setting = setting;
    }

    @Override
    public boolean isMixed() {

	return getSetting().getBrokeringStrategy() == BrokeringStrategy.MIXED;
    }

    @Override
    public AccessorSetting getSetting() {

	return this.setting;
    }

    @Override
    public String getType() {

	return getSetting().getConfigurableType();
    }
}
