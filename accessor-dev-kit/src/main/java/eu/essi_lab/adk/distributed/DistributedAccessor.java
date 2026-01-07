package eu.essi_lab.adk.distributed;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
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
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.IResourceMapper;
import eu.essi_lab.ommdk.ResourceMapperFactory;

/**
 * @author ilsanto
 */
@SuppressWarnings("rawtypes")
public abstract class DistributedAccessor<C extends IDistributedQueryConnector> implements IDistributedAccessor<C> {

    private AccessorSetting setting;

    public DistributedAccessor() {

	configure();
    }

    /**
     * 
     */
    protected void configure() {

	AccessorSetting setting = AccessorSetting.createDistributed( //

		initAccessorType(), //
		initDistributedConnectorSetting(), //
		initSettingName());//

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
    protected abstract DistributedConnectorSetting initDistributedConnectorSetting();

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

	    @SuppressWarnings("unchecked")
	    ResultSet<OriginalMetadata> originalRS = conn.query(message, page);

	    List<OriginalMetadata> originals = originalRS.getResultsList();

	    IdentifierDecorator decorator = new IdentifierDecorator();

	    for (OriginalMetadata o : originals) {

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
		mapType = tfMap.getElement();

	    }

	    Optional.of(mapType).ifPresent(mt -> mt.getSourceId().add(item));

	    countResult.setTermFrequencyMap(tfMap);
	}

	GSLoggerFactory.getLogger(getClass()).info("Counting ENDED");

	return countResult;
    }

    public C getConnector() {

	DistributedConnectorSetting connectorSetting = getSetting().getDistributedConnectorSetting();

	C connector = null;

	try {
	    connector = connectorSetting.createConfigurable();

	} catch (Exception e) {

	    e.printStackTrace();

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	connector.setSourceURL(getSetting().getGSSourceSetting().getSourceEndpoint());

	return connector;
    }

    public IResourceMapper getMapper(String schemeUri) throws GSException {

	return ResourceMapperFactory.getResourceMapper(schemeUri);
    }

    @Override
    public GSSource getSource() {

	return getSetting().getGSSourceSetting().asSource();
    }

    @Override
    public boolean isMixed() {

	return getSetting().getBrokeringStrategy() == BrokeringStrategy.MIXED;
    }

    @Override
    public void configure(AccessorSetting setting) {

	this.setting = setting;
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
