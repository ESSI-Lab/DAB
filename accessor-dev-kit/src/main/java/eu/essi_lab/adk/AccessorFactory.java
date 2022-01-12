package eu.essi_lab.adk;

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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.adk.distributed.DistributedAccessor;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.HarvestedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.ConnectorFactory;
import eu.essi_lab.cdk.IDriverConnector;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cdk.mixed.IMixedQueryConnector;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.configuration.GSSourceAccessor;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
public class AccessorFactory {

    public static final String GS_SOURCE_OPTION_KEY = "GS_SOURCE_OPTION_KEY";

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    /**
     * Discovers and instantiates an {@link IGSAccessorConfigurable} for the provided {@link Source}. The execution of
     * this method is
     * expected to be long, since all available {@link IDriverConnector}s are required to test the support for the
     * provided source.
     *
     * @param source
     * @return a {@link IGSAccessorConfigurable} for the source, or null if none is available
     */
    public IGSAccessorConfigurable getConfigurableAccessor(Source source) {

	logger.trace("Looking compatible accessosrs with source {} [{}]", source.getLabel(), source.getUniqueIdentifier());

	GSAccessor accessor = new GSAccessor();

	accessor.setKey("configurable:accessor:" + source.getUniqueIdentifier());

	IHarvestedAccessor hacc = null;
	try {
	    hacc = getHarvestingAccessor(source);
	} catch (GSException e) {

	    logger.warn("Exception retrieving harvested accessor for source {} [{}]", source.getLabel(), source.getUniqueIdentifier());

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	}

	IDistributedAccessor dacc = null;

	try {

	    dacc = getDistributedAccessor(source);

	} catch (GSException e) {

	    logger.warn("Exception retrieving distributed accessor for source {} [{}]", source.getLabel(), source.getUniqueIdentifier());

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	}
	List<GSAccessor> mixedList = null;

	try {

	    mixedList = getMixedAccessor(source);

	} catch (GSException e) {

	    logger.warn("Exception retrieving mixed accessor for source {} [{}]", source.getLabel(), source.getUniqueIdentifier());

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	}

	if (dacc == null && hacc == null && mixedList == null)
	    return null;

	logger.trace("Found not null accessors for source {} [{}]", source.getLabel(), source.getUniqueIdentifier());

	if (dacc != null)
	    accessor.setDistributed(dacc);

	if (hacc != null)
	    accessor.setOAIPMH(hacc);

	if (mixedList != null)
	    accessor.setMixed(mixedList);

	return accessor;
    }

    /**
     * Instantiates an {@link IHarvestedAccessor} for the provided {@link GSSource}.
     *
     * @param source
     * @return a {@link IHarvestedAccessor} for the source, or null if none is available.
     * @throws {@link GSException} when the required {@link IHarvestedQueryConnector} or
     *         {@link IHarvestedQueryConnector} instantiation
     *         fails.
     */
    public IHarvestedAccessor getHarvestingAccessor(GSSource source) throws GSException {

	IGSConfigurableComposed acc = ((GSSourceAccessor) source).getConfigurableAccessor();

	if (acc instanceof GSAccessor) {
	    return ((GSAccessor) acc).getHarvestedAccessor();
	}

	return (IHarvestedAccessor) acc;
    }

    /**
     * Instantiates an {@link IDistributedQueryConnector} for the provided {@link GSSource}.
     *
     * @param source
     * @return a {@link IDistributedQueryConnector} for the source, or null if none is available.
     * @throws {@link GSException} when the required {@link IHarvestedQueryConnector} or
     *         {@link IDistributedQueryConnector} instantiation
     *         fails.
     */
    public IDistributedAccessor getDistributedAccessor(GSSource source) throws GSException {

	IGSConfigurableComposed acc = ((GSSourceAccessor) source).getConfigurableAccessor();

	if (acc instanceof GSAccessor) {
	    return ((GSAccessor) acc).getDistributedAccessor();
	}

	return (IDistributedAccessor) acc;

    }

    /**
     * Discovers and instantiates an {@link IHarvestedAccessor} for the provided {@link Source}. The execution of this
     * method is expected to
     * be long, since all available {@link IDriverConnector}s are required to test the support for the provided source.
     *
     * @param source
     * @return a {@link IHarvestedAccessor} for the source, or null if none is available
     */
    public IHarvestedAccessor getHarvestingAccessor(Source source) throws GSException {

	List<IHarvestedQueryConnector> connectors = getHarvestedConnector(source);

	if (connectors.isEmpty())
	    return null;

	return createHarvestingAccessor(source, connectors, true);

    }

    private IHarvestedAccessor createHarvestingAccessor(Source source, List<IHarvestedQueryConnector> connectors,
	    boolean addSourceInstantiable) throws GSException {

	HarvestedAccessor accessor = new HarvestedAccessor();

	if (addSourceInstantiable)
	    accessor.setGSSource(initGSSource(source));

	for (IHarvestedQueryConnector connector : connectors) {

	    accessor.addConnector(connector);
	}

	return accessor;
    }

    public List<GSAccessor> getMixedAccessor(Source source) throws GSException {

	List<IMixedQueryConnector> connectors = ConnectorFactory.getInstance().getMixedConnector(source);

	if (connectors.isEmpty())
	    return null;

	List<GSAccessor> accessors = new ArrayList<>();

	for (IMixedQueryConnector connector : connectors) {

	    GSAccessor accessor = new GSAccessor();

	    accessor.setLabel(connector.getLabel());

	    accessor.setKey("configurable:accessor:mixed:" + source.getUniqueIdentifier());

	    IHarvestedQueryConnector hConnector = connector.getHarvesterConnector();

	    IHarvestedAccessor hAccessor = createHarvestingAccessor(source, Arrays.asList(new IHarvestedQueryConnector[] { hConnector }),
		    true);

	    hAccessor.getSupportedOptions().get(HarvestedAccessor.GS_CONNECTOR_OPTION_KEY).setLabel("Select Harvester Connector");

	    accessor.setOAIPMH(hAccessor);

	    IDistributedQueryConnector dConnector = connector.getDistributedConnector();

	    IDistributedAccessor dAccessor = createDistributedAccessor(source,
		    Arrays.asList(new IDistributedQueryConnector[] { dConnector }), true);

	    dAccessor.getSupportedOptions().get(DistributedAccessor.GS_CONNECTOR_OPTION_KEY).setLabel("Select Distributed Connector");

	    accessor.setDistributed(dAccessor);

	    accessor.setGSSource(initGSSource(source));

	    accessors.add(accessor);
	}

	return accessors;

    }

    /**
     * Discovers and instantiates an {@link IDistributedAccessor} for the provided {@link Source}. The execution of this
     * method is expected
     * to be long, since all available {@link IDriverConnector}s are required to test the support for the provided
     * source.
     *
     * @param source
     * @return a {@link IDistributedAccessor} for the source, or null if none is available
     * @throws GSException when accessor fails to create its own {@link eu.essi_lab.configuration.GSSourceAccessor}
     */

    public IDistributedAccessor getDistributedAccessor(Source source) throws GSException {

	List<IDistributedQueryConnector> connectors = getDistributedConnector(source);

	if (connectors.isEmpty())
	    return null;

	return createDistributedAccessor(source, connectors, true);
    }

    private IDistributedAccessor createDistributedAccessor(Source source, List<IDistributedQueryConnector> connectors,
	    boolean addSourceInstantiable) throws GSException {
	DistributedAccessor accessor = new DistributedAccessor();

	GSSource gsSource = initGSSource(source);

	if (addSourceInstantiable)
	    accessor.setGSSource(gsSource);

	for (IDistributedQueryConnector connector : connectors) {

	    accessor.addConnector(connector);
	}

	return accessor;
    }

    private GSSource initGSSource(Source source) {

	GSSource gsSource = new GSSource();
	gsSource.setUniqueIdentifier(source.getUniqueIdentifier());
	gsSource.setEndpoint(source.getEndpoint());

	gsSource.setLabel(source.getLabel());

	return gsSource;
    }

    List<IHarvestedQueryConnector> getHarvestedConnector(Source source) {

	return ConnectorFactory.getInstance().getHarvesterConnector(source);
    }

    List<IDistributedQueryConnector> getDistributedConnector(Source source) {

	return ConnectorFactory.getInstance().getDistributedConnector(source);
    }

}
