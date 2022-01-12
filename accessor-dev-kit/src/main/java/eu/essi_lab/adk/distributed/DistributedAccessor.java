package eu.essi_lab.adk.distributed;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.adk.configuration.ADKConfBuilder;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.configuration.GSSourceAccessor;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;
import eu.essi_lab.ommdk.ResourceMapperFactory;
public class DistributedAccessor extends AbstractGSconfigurableComposed implements IDistributedAccessor {

    public static final String PREFIX = "accessor:distributed:";
    public static final String GS_CONNECTOR_OPTION_KEY = "GS_CONNECTOR_OPTION_KEY";
    public static final String GS_MAPPER_OPTION_KEY = "GS_MAPPER_OPTION_KEY";
    /**
     *
     */
    private static final long serialVersionUID = -3019988278258566974L;
    private Map<String, GSConfOption<?>> accessorOptions = new HashMap<>();

    private transient IDistributedQueryConnector connector;

    @JsonIgnore
    private  transient ADKConfBuilder adkBuilder = new ADKConfBuilder();

    public DistributedAccessor() {

	super();

	GSConfOptionSubcomponent connectorOption = new GSConfOptionSubcomponent();
	connectorOption.setKey(GS_CONNECTOR_OPTION_KEY);
	connectorOption.setLabel("Select Connector");
	connectorOption.setMandatory(true);

	getSupportedOptions().put(GS_CONNECTOR_OPTION_KEY, connectorOption);
    }

    /**
     * Implements the interface method returning a <code>ResultSet&lt;GSResource&gt;</code>
     * <h3>Implementation notes</h3>
     * The returned {@link GSResource}s have the following characteristics:
     * <ul>
     * <li>the concrete type is determined through a classification according to a list of well-known types
     * (e.g. datasets, services, observation, etc..)</li>
     * <li>the {@link GSResource#getPrivateId()} field is correctly set</li>
     * <li>the {@link GSResource#getPublicId()} field is correctly set</li>
     * <li>the {@link GSResource#getHarmonizedMetadata()} field is correctly set</li>
     * <li>the {@link GSResource#getOriginalMetadata()} field is correctly set</li>
     * <li>the {@link GSResource#getSource()} field is correctly set</li>
     * </ul>
     * The {@link GSResource#getPrivateId()} and {@link GSResource#getPublicId()} fields are set by an
     * {@link IdentifierDecorator}
     */
    @Override
    public ResultSet<GSResource> query(ReducedDiscoveryMessage message, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Query STARTED");

	List<GSResource> results = new ArrayList<>();

	if (message.isOutputSources()) {

	    DatasetCollection collection = new DatasetCollection();
	    collection.setOriginalId(UUID.randomUUID().toString());
	    collection.setSource(getSource());
	    
	    results.add(collection);

	} else {

	    IDistributedQueryConnector conn = getConnector();

	    ResultSet<OriginalMetadata> originalRS = conn.query(message, page);

	    List<OriginalMetadata> originlas = originalRS.getResultsList();

	    IdentifierDecorator decorator = new IdentifierDecorator();

	    for (OriginalMetadata o : originlas) {

		try {

		    IResourceMapper map = getMapper(o.getSchemeURI());

		    GSResource resource = map.map(o, getSource());

		    decorator.decorateDistributedIdentifier(resource);

		    results.add(resource);

		} catch (GSException ex) {

		    GSLoggerFactory.getLogger(getClass()).warn(ex.getMessage());
		}
	    }

	}
	ResultSet<GSResource> resultSet = new ResultSet<>();

	resultSet.setResultsList(results);

	GSLoggerFactory.getLogger(getClass()).info("Query ENDED");
	return resultSet;
    }

    @Override
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Counting STARTED");

	DiscoveryCountResponse countResult;

	if (message.isOutputSources()) {
	    countResult = new DiscoveryCountResponse();
	    countResult.setCount(1);
	} else {
	    IDistributedQueryConnector conn = getConnector();
	    countResult = conn.count(message);
	}

	if (countResult.getCount() > 0) {

	    TermFrequencyItem item = new TermFrequencyItem();
	    item.setFreq(countResult.getCount());
	    item.setTerm(getSource().getUniqueIdentifier());

	    Optional<TermFrequencyMap> optMap = countResult.getTermFrequencyMap();

	    TermFrequencyMap tfMap = null;
	    TermFrequencyMapType mapType = null;

	    if (!optMap.isPresent()) {

		mapType = new TermFrequencyMapType();
		tfMap = new TermFrequencyMap(mapType);

	    } else {

		tfMap = optMap.get();
	    }

	    Optional.of(mapType).ifPresent(mt -> mt.getSourceId().add(item));

	    countResult.setTermFrequencyMap(tfMap);
	}

	GSLoggerFactory.getLogger(getClass()).info("Counting ENDED");

	return countResult;
    }

    @JsonIgnore
    public IDistributedQueryConnector getConnector() throws GSException {

	if (connector == null) {

	    connector = adkBuilder.readConfiguredComponent(this, IDistributedQueryConnector.class);
	}

	return connector;
    }

    @JsonIgnore
    public IResourceMapper getMapper(String schemeUri) throws GSException {

	return ResourceMapperFactory.getResourceMapper(schemeUri);

    }

    @JsonIgnore
    @Override
    public GSSource getSource() {

	return (GSSource) getInstantiableType();
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return accessorOptions;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	if (opt instanceof GSConfOptionSubcomponent) {

	    GSConfOptionSubcomponent co = (GSConfOptionSubcomponent) opt;

	    if (co.getKey().equalsIgnoreCase(GS_CONNECTOR_OPTION_KEY))

		adkBuilder.onOptionSet(co, this, "setConnector", IDistributedQueryConnector.class);

	    if (co.getKey().equalsIgnoreCase(GS_MAPPER_OPTION_KEY))

		adkBuilder.onOptionSet(co, this, "setMapper", IResourceMapper.class);
	}
    }

    @Override
    public void onFlush() throws GSException {
	// Nothing to do on flush action
    }

    public void setGSSource(GSSource source) throws GSException {

	GSSourceAccessor it = new Deserializer().deserialize(source.serialize(), GSSourceAccessor.class);

	it.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	it.setConfigurableAccessor(this);
	setInstantiableType(it);
    }

    public void setADKBuilder(ADKConfBuilder builder) {
	this.adkBuilder = builder;
    }

    @JsonIgnore
    public void setConnector(IDistributedQueryConnector c) {

	c.setSourceURL(((GSSourceAccessor) getInstantiableType()).getEndpoint());

	connector = c;
    }

    public void addConnector(IDistributedQueryConnector c) {

	adkBuilder.addSubComponent(GS_CONNECTOR_OPTION_KEY, c.getLabel(), c.getClass().getName(), this);
    }
}
