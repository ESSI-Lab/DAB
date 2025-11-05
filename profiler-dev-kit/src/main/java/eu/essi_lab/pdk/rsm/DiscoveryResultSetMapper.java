package eu.essi_lab.pdk.rsm;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Implementation specific to map a <code>ResultSet&ltGSResource&gt</code> in to a <code>ResultSet&ltT&gt</code>
 *
 * @param <T> the type to which to map the {@link GSResource}s of the response (e.g.: String, JSON, XML, etc.)
 * @author Fabrizio
 */
public abstract class DiscoveryResultSetMapper<T>
	implements MessageResponseMapper<DiscoveryMessage, GSResource, T, CountSet, ResultSet<GSResource>, ResultSet<T>> {

    protected MappingStrategy strategy;

    /**
     * Creates a new <code>ResultSetMapper</code> with the {@value MappingStrategy#PRIORITY_TO_CORE_METADATA}
     */
    public DiscoveryResultSetMapper() {

	setMappingStrategy(MappingStrategy.PRIORITY_TO_CORE_METADATA);
    }

    /**
     * The strategy defines how to map a {@link GSResource} in case this {@link MappingSchema#getUri()} is the same of the
     * {@link GSResource#getOriginalMetadata()} scheme URI.<br>
     * <br>
     * More formally, the mapping strategy is applied if this condition is <code>true</code>:<br>
     * <ul>
     * <li><code>this.getMappingSchmema().getUri().equals(resource.getOriginalMetadata().getSchemeURI())</code>
     * <ul>
     * <li>
     * in such case, if the selected strategy is {@link MappingStrategy#PRIORITY_TO_ORIGINAL_METADATA}, than the current
     * {@link GSResource} is mapped
     * using the original metadata</li>
     * <li>otherwise the resource is mapped using
     * <code>resoure.getHarmonizedMetadata().getCoreMetadata()</code>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * <br>
     * The default policy is
     * {@link MappingStrategy#PRIORITY_TO_CORE_METADATA}.<br>
     * <br>
     * If the above condition is <code>false</code>, than the default condition is applied
     *
     * @author Fabrizio
     */
    public enum MappingStrategy {
	/**
	 * Gives priority to the original metadata
	 */
	PRIORITY_TO_ORIGINAL_METADATA,
	/**
	 * Gives priority to the core metadata
	 */
	PRIORITY_TO_CORE_METADATA
    }

    /**
     * Provides a "one to one" mapping of the given <code>resultSet</code> of {@link GSResource}s.<br>
     * <br>
     * The returned {@link ResultSet} of <code>String</code> has the same properties of the mapped result set; in particular it has the
     * same:
     * <ul>
     * <li>{@link ResultSet#getTotalResults()}</li>
     * <li>{@link ResultSet#getTermFrequencyMap()}</li>
     * <li>{@link ResultSet#getExceptions()}</li>
     * <li>{@link ResultSet#getTotalResults()}</li>
     * <li>{@link ResultSet#getResultsList()}.size()</li>
     * </ul>
     * As consequence of the mapping, the results list contains the mapped resources serialized to a <code>String</code>
     * by calling the {@link #map(DiscoveryMessage, GSResource)} method,
     * and the content is determined by the {@link #getMappingSchema()} and by the {@link #getMappingStrategy()}
     *
     * @param the message that triggered the result set
     * @param resultSet the result set to map
     * @return a non <code>null</code> {@link ResultSet}&lt;String&gt;
     * @throws GSException if errors occur during the mapping process
     * @see #getMappingSchema()
     */
    @Override
    public ResultSet<T> map(DiscoveryMessage message, ResultSet<GSResource> resultSet) throws GSException {


	//
	// converts the incoming ResultSet<GSResource> in a ResultSet<T>
	//

	ResultSet<T> mappedResSet = new ResultSet<T>(resultSet);

	// set the property handler
	mappedResSet.setPropertyHandler(resultSet.getPropertyHandler());

	List<String> ids = ConfigurationWrapper.getGDCSourceSetting().getSelectedSourcesIds();

	//
	// parallel mapping with a thread factory of unlimited virtual threads
	//

	ThreadFactory factory = Thread.ofVirtual().//
		name(getClass().getSimpleName()+"@"+message.getRequestId(true)).//
		factory();

	ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);

	List<CompletableFuture<T>> futures = resultSet.getResultsList().//
		stream().//
		map(res -> CompletableFuture.supplyAsync(() -> map(message, res, ids), executor)).//
		toList();

	List<T> out = futures.stream().map(CompletableFuture::join).toList();
	mappedResSet.setResultsList(out);

	//
	//
	//

	return mappedResSet;
    }

    /**
     * Provides a mapping to <code>T</code> of the given <code>resource</code> according to the current {@link MappingStrategy}. This method
     * is called in the {@link #map(DiscoveryMessage, ResultSet)} method for each {@link GSResource} to map
     *
     * @param message
     * @param resource
     * @return
     * @throws GSException
     * @see #map(DiscoveryMessage, ResultSet)
     */
    public abstract T map(DiscoveryMessage message, GSResource resource) throws GSException;

    /**
     * @return
     */
    public MappingStrategy getMappingStrategy() {

	return strategy;
    }

    /**
     * @param strategy
     */
    public void setMappingStrategy(MappingStrategy strategy) {

	this.strategy = strategy;
    }

    /**
     * @param message
     * @param res
     * @param ids
     * @return
     */
    private T map(DiscoveryMessage message, GSResource res, List<String> ids) {

	GSSource source = res.getSource();

	if (source != null && ids.contains(source.getUniqueIdentifier())) {

	    res.getPropertyHandler().setIsGDC(true);

	} else if (Objects.isNull(source)) {

	    GSLoggerFactory.getLogger(getClass()).trace("Resource without source: " + res.getOriginalId());
	}

	try {
	    return map(message, res);

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    return null;
	}
    }

}
