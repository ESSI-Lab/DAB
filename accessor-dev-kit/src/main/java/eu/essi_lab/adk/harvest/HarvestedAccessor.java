package eu.essi_lab.adk.harvest;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.adk.configuration.ADKConfBuilder;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.configuration.GSSourceAccessor;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionOrderingDirection;
import eu.essi_lab.model.configuration.option.GSConfOptionOrderingProperty;
import eu.essi_lab.model.configuration.option.GSConfOptionResultsPriority;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;
import eu.essi_lab.ommdk.ResourceMapperFactory;
public class HarvestedAccessor extends AbstractGSconfigurableComposed implements IHarvestedAccessor {

    public static final String GS_CONNECTOR_OPTION_KEY = "GS_CONNECTOR_OPTION_KEY";
    public static final String GS_RESULTS_PRIORITY_OPTION_KEY = "GS_RESULTS_PRIORITY_OPTION_KEY";
    private static final String NO_LIST_RECORDS_ERROR_RESPONSE_ERROR = "NO_LIST_RECORDS_ERROR";
    private static final String GS_ORDERING_DIRECTION_OPTION_KEY = "GS_ORDERING_DIRECTION_OPTION_KEY";
    private static final String GS_ORDERING_PROPERTY_OPTION_KEY = "GS_ORDERING_PROPERTY_OPTION_KEY";

    private static final long serialVersionUID = -70624865009969093L;
    private IHarvestedQueryConnector connector;
    private Map<String, GSConfOption<?>> oaiOptions = new HashMap<>();

    @JsonIgnore
    private transient ADKConfBuilder adkBuilder = new ADKConfBuilder();
    @JsonIgnore
    private int maxAttemptsCount;
    /**
     * 
     */
    private static final int DEFAULT_MAX_ATTEMPTS_COUNT = 3;

    public HarvestedAccessor() {
	super();

	GSConfOptionSubcomponent connectorOption = new GSConfOptionSubcomponent();
	connectorOption.setKey(GS_CONNECTOR_OPTION_KEY);
	connectorOption.setLabel("Select Connector");
	connectorOption.setMandatory(true);

	getSupportedOptions().put(GS_CONNECTOR_OPTION_KEY, connectorOption);

	GSConfOptionResultsPriority resultsPriorityOption = new GSConfOptionResultsPriority();

	resultsPriorityOption.setKey(GS_RESULTS_PRIORITY_OPTION_KEY);
	resultsPriorityOption.setLabel("Results Priority");
	resultsPriorityOption.setMandatory(true);

	getSupportedOptions().put(GS_RESULTS_PRIORITY_OPTION_KEY, resultsPriorityOption);

	setMaxAttemptsCount(DEFAULT_MAX_ATTEMPTS_COUNT);
    }

    public void setGSSource(GSSource s) throws GSException {

	GSSourceAccessor it = new Deserializer().deserialize(s.serialize(), GSSourceAccessor.class);

	it.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	it.setConfigurableAccessor(this);
	setInstantiableType(it);
    }

    @JsonIgnore
    @Override
    public GSSource getSource() {

	return (GSSource) getInstantiableType();
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	if (opt instanceof GSConfOptionSubcomponent) {

	    GSConfOptionSubcomponent co = (GSConfOptionSubcomponent) opt;

	    if (co.getKey().equalsIgnoreCase(GS_CONNECTOR_OPTION_KEY)) {

		adkBuilder.onOptionSet(co, this, "setConnector", IHarvestedQueryConnector.class);
	    }
	}

	if (opt instanceof GSConfOptionResultsPriority) {

	    ResultsPriority priority = ((GSConfOptionResultsPriority) opt).getValue();
	    getSource().setResultsPriority(priority);

	    if (priority == ResultsPriority.COLLECTION) {

		GSConfOptionOrderingDirection orderingDirectionOption = new GSConfOptionOrderingDirection();

		orderingDirectionOption.setKey(GS_ORDERING_DIRECTION_OPTION_KEY);
		orderingDirectionOption.setLabel("Ordering Direction");

		getSupportedOptions().put(GS_ORDERING_DIRECTION_OPTION_KEY, orderingDirectionOption);

		GSConfOptionOrderingProperty orderingPropertyOption = new GSConfOptionOrderingProperty();

		orderingPropertyOption.setKey(GS_ORDERING_PROPERTY_OPTION_KEY);
		orderingPropertyOption.setLabel("Ordering Property");

		getSupportedOptions().put(GS_ORDERING_PROPERTY_OPTION_KEY, orderingPropertyOption);

	    } else {

		getSupportedOptions().remove(GS_ORDERING_DIRECTION_OPTION_KEY);
		getSupportedOptions().remove(GS_ORDERING_PROPERTY_OPTION_KEY);
	    }
	}

	else if (opt instanceof GSConfOptionOrderingDirection) {
	    getSource().setOrderingDirection(((GSConfOptionOrderingDirection) opt).getValue());
	}

	else if (opt instanceof GSConfOptionOrderingProperty) {
	    getSource().setOrderingProperty(((GSConfOptionOrderingProperty) opt).getValue());
	}
    }

    @Override
    public void onFlush() throws GSException {

	// Nothing to do on flush action

    }

    public void setADKBuilder(ADKConfBuilder builder) {
	this.adkBuilder = builder;
    }

    @JsonIgnore
    public void setConnector(IHarvestedQueryConnector c) {

	c.setSourceURL(((GSSourceAccessor) getInstantiableType()).getEndpoint());

	connector = c;
    }

    @JsonIgnore
    public IHarvestedQueryConnector getConnector() {
	if (connector == null) {

	    connector = adkBuilder.readConfiguredComponent(this, IHarvestedQueryConnector.class);

	}

	return connector;
    }

    @JsonIgnore
    public IResourceMapper getMapper(String schemeUri) {

	return ResourceMapperFactory.getResourceMapper(schemeUri);

    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return oaiOptions;
    }

    public void addConnector(IHarvestedQueryConnector c) {

	adkBuilder.addSubComponent(GS_CONNECTOR_OPTION_KEY, c.getLabel(), c.getClass().getName(), this);
    }

    /**
     * Set the maximum number of connection attempts before throw exception. Default is 3
     * 
     * @param count
     */
    @JsonIgnore
    public void setMaxAttemptsCount(int count) {

	this.maxAttemptsCount = count;
    }

    @Override
    public ListRecordsResponse<GSResource> listRecords(ListRecordsRequest listRecords) throws GSException {

	IHarvestedQueryConnector conn = getConnector();

	int tries = 0;
	int numberOfTriesInOneDay = 0;
	ListRecordsResponse<OriginalMetadata> driverResponse = null;

	while (tries < maxAttemptsCount) {

	    tries++;

	    try {
		driverResponse = conn.listRecords(listRecords);
		if (driverResponse == null) {
		    GSLoggerFactory.getLogger(getClass()).warn("Null driver response!");
		} else {
		    GSLoggerFactory.getLogger(getClass()).info("Driver response obtained");
		}
		break;

	    } catch (GSException gse) {

		gse.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).warn("GS error occurred on try n. {}", tries);

		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(gse)));

	    } catch (Exception e) {

		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).warn("General error occurred on try n. {}", tries);

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }

	    if (tries < maxAttemptsCount) {
		try {
		    // it seems that if US Data Gov has a failure, it requires about 15 minutes to restore the service
		    if (conn.getSourceURL().contains("catalog.data.gov")) {
			numberOfTriesInOneDay++;
			// try for 24 hours
			if(numberOfTriesInOneDay < 96) {
			    tries = 0;
			} else {
			    tries = maxAttemptsCount;
			}
			GSLoggerFactory.getLogger(getClass()).debug("Waiting  15 minutes for a new attempt... {}/{}", numberOfTriesInOneDay,
				96);
			Thread.sleep(15 * 60 * 1000);
			
		    } else {
			GSLoggerFactory.getLogger(getClass()).debug("Waiting  60 seconds for a new attempt... {}/{}", tries,
				maxAttemptsCount);
			Thread.sleep(60000);
		    }

		} catch (InterruptedException ex) {
		    GSLoggerFactory.getLogger(getClass()).warn("Interrupted!", ex);
		    Thread.currentThread().interrupt();
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).debug("Too much attempts: {}/{}", tries, maxAttemptsCount);
	    }
	}

	if (driverResponse == null) {

	    throw GSException.createException(//
		    getClass(), //
		    "Source URL [" + conn.getSourceURL() + "]", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NO_LIST_RECORDS_ERROR_RESPONSE_ERROR);
	}

	ListRecordsResponse<GSResource> response = new ListRecordsResponse<>();

	response.setResumptionToken(driverResponse.getResumptionToken());

	driverResponse.getRecords().forEachRemaining(o -> {

	    try {

		IResourceMapper mapper = getMapper(o.getSchemeURI());

		GSResource resource = mapper.map(o, getSource());

		if (resource != null) {
		    response.addRecord(resource);

		    GSLoggerFactory.getLogger(getClass()).info("Mapped resource with original Id {}", resource.getOriginalId());

		} else

		    GSLoggerFactory.getLogger(getClass()).warn("Mapped resource is null");

	    } catch (GSException ex) {

		DefaultGSExceptionReader reader = new DefaultGSExceptionReader(ex);
		String description = reader.getLastErrorDescription();

		if (description != null && !description.equals("")) {
		    GSLoggerFactory.getLogger(getClass()).warn(description);
		}
	    }
	});

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	return getConnector().listMetadataFormats();
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {
	return getConnector().supportsIncrementalHarvesting();
    }

    public static void main(String[] args) {

	System.out.println(System.currentTimeMillis());
    }

    @Override
    public String toString() {
	String adk = adkBuilder == null ? "none" : adkBuilder.toString();
	String connectorStr = connector == null ? "none" : connector.getClass().getSimpleName() + " " + connector.toString();
	String oaiOptionsStr = oaiOptions == null ? "none" : oaiOptions.toString();
	return adk + connectorStr + oaiOptionsStr;
    }

}
