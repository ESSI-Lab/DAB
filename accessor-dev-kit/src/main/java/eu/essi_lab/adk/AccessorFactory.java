package eu.essi_lab.adk;

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
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This factory loads all the available {@link IHarvestedAccessor} and {@link IDistributedAccessor} once in a static clause, 
 * then reuses them in order to improve performances. This is safe, since no accessors are added and/or removed during 
 * the application lif cycle.  
 * 
 * @author Fabrizio
 */
public class AccessorFactory {

    @SuppressWarnings("rawtypes")
    private static List<IHarvestedAccessor> harvestedAccessors;
    @SuppressWarnings("rawtypes")
    private static List<IDistributedAccessor> distributedAccessors;

    static {

	{

	    @SuppressWarnings("rawtypes")
	    ServiceLoader<IHarvestedAccessor> loader = ServiceLoader.load(IHarvestedAccessor.class);

	    harvestedAccessors = StreamUtils.iteratorToStream(loader.iterator()).//
		    collect(Collectors.toList());
	}

	{
	    @SuppressWarnings("rawtypes")
	    ServiceLoader<IDistributedAccessor> loader = ServiceLoader.load(IDistributedAccessor.class);

	    distributedAccessors = StreamUtils.iteratorToStream(loader.iterator()).//
		    collect(Collectors.toList());
	}
    }

    /**
     * @author Fabrizio
     */
    public enum LookupPolicy {

	/**
	 * Both mixed and specific
	 */
	ALL,
	/**
	 * Only mixed accessors
	 */
	MIXED,
	/**
	 * Only non mixed
	 */
	SPECIFIC;

	@SuppressWarnings("rawtypes")
	public boolean filterIn(IHarvestedAccessor accessor) {

	    switch (this) {
	    case MIXED:
		return accessor.isMixed();
	    case SPECIFIC:
		return !accessor.isMixed();
	    case ALL:
	    default:
	    }

	    return true;
	}

	@SuppressWarnings("rawtypes")
	public boolean filterIn(IDistributedAccessor accessor) {

	    switch (this) {
	    case MIXED:
		return accessor.isMixed();
	    case SPECIFIC:
		return !accessor.isMixed();
	    case ALL:
	    default:
	    }

	    return true;
	}
    }

    /**
     * Retrieves all the available harvested accessors according to the given <code>policy</code>
     * 
     * @param policy
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List<IHarvestedAccessor> getHarvestedAccessors(LookupPolicy policy) {

	return harvestedAccessors.//
		stream().//
		filter(acc -> policy.filterIn(acc)).//
		collect(Collectors.toList());
    }

    /**
     * Retrieves all the available distributed accessors according to the given <code>policy</code>
     * 
     * @param policy
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List<IDistributedAccessor> getDistributedAccessors(LookupPolicy policy) {

	return distributedAccessors.//
		stream().//
		filter(acc -> policy.filterIn(acc)).//
		collect(Collectors.toList());
    }

    /**
     * Retrieves an {@link IHarvestedAccessor} accessor which supports the given <code>source</code>, according to the
     * {@link IHarvestedQueryConnector#supports(GSSource)}
     *
     * @param source
     * @return
     * @throws GSException
     */
    @SuppressWarnings("rawtypes")
    public static Optional<IHarvestedAccessor> getHarvestedAccessor(GSSource source) {

	Optional<IHarvestedAccessor> accessor = harvestedAccessors.//
		stream().//
		filter(acc -> acc.getConnector().supports(source)).//
		findFirst();

	return accessor;

    }

    /**
     * Retrieves an {@link IDistributedAccessor} accessor which supports the given <code>source</code>, according to the
     * {@link IHarvestedQueryConnector#supports(GSSource)}
     *
     * @param source
     * @return
     * @throws GSException
     */
    @SuppressWarnings("rawtypes")
    public static Optional<IDistributedAccessor> getDistributedAccessor(GSSource source) {

	Optional<IDistributedAccessor> accessor = distributedAccessors.//
		stream().//
		filter(acc -> acc.getConnector().supports(source)).//
		findFirst();

	return accessor;
    }

    /**
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IHarvestedAccessor getConfiguredHarvestedAccessor(AccessorSetting setting) throws Exception {

	IHarvestedAccessor accessor = null;

	if (setting.getBrokeringStrategy() == BrokeringStrategy.MIXED) {

//	    GSLoggerFactory.getLogger(AccessorFactory.class).debug("Creating mixed harvested accessor STARTED");

	    String accessorType = setting.getHarvestedAccessorType();

	    accessor = harvestedAccessors.//
		    stream().//
		    filter(c -> c.getType().equals(accessorType)).//
		    findFirst().//
		    get();

	    accessor.configure(setting);

//	    GSLoggerFactory.getLogger(AccessorFactory.class).info("Creating mixed harvested accessor ENDED");

	} else if (setting.getBrokeringStrategy() == BrokeringStrategy.HARVESTED) {

//	    GSLoggerFactory.getLogger(AccessorFactory.class).debug("Creating harvested accessor STARTED");

	    accessor = setting.createConfigurable();

//	    GSLoggerFactory.getLogger(AccessorFactory.class).debug("Creating harvested accessor ENDED");

	} else {

	    throw new UnsupportedOperationException("Attempting to create harvested accessor from distributed setting");
	}

	GSLoggerFactory.getLogger(AccessorFactory.class).debug("Created accessor type: " + accessor.getType());

	return accessor;
    }

    /**
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IDistributedAccessor getConfiguredDistributedAccessor(AccessorSetting setting) throws Exception {

	IDistributedAccessor accessor = null;

	if (setting.getBrokeringStrategy() == BrokeringStrategy.MIXED) {

//	    GSLoggerFactory.getLogger(AccessorFactory.class).debug("Creating mixed distributed accessor STARTED");

	    String accessorType = setting.getDistributedAccessorType();

	    accessor = distributedAccessors.//
		    stream().//
		    filter(c -> c.getType().equals(accessorType)).//
		    findFirst().//
		    get();

	    accessor.configure(setting);

//	    GSLoggerFactory.getLogger(AccessorFactory.class).info("Creating mixed distributed accessor ENDED");

	} else if (setting.getBrokeringStrategy() == BrokeringStrategy.DISTRIBUTED) {

//	    GSLoggerFactory.getLogger(AccessorFactory.class).debug("Creating distributed accessor STARTED");

	    accessor = setting.createConfigurable();

//	    GSLoggerFactory.getLogger(AccessorFactory.class).debug("Creating distributed accessor ENDED");

	} else {

	    throw new UnsupportedOperationException("Attempting to create distributed accessor from harvested setting");
	}

	GSLoggerFactory.getLogger(AccessorFactory.class).debug("Created accessor type: " + accessor.getType());

	return accessor;
    }
}
