/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Buckets;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;

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

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchBondHandler;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OpenSearchFinder implements DatabaseFinder {

    private OpenSearchDatabase database;
    private OpenSearchWrapper wrapper;

    @Override
    public void setDatabase(Database database) {

	this.database = (OpenSearchDatabase) database;
	this.wrapper = new OpenSearchWrapper(this.database.getClient());
    }

    @Override
    public OpenSearchDatabase getDatabase() {

	return (OpenSearchDatabase) database;
    }

    @Override
    public boolean supports(StorageInfo info) {

	return OpenSearchDatabase.isSupported(info);
    }

    @Override
    public DiscoveryCountResponse count(DiscoveryMessage message) throws GSException {

	try {

	    SearchResponse<Object> searchResponse = discover_(message, true);

	    int total = (int) searchResponse.hits().total().value();

	    DiscoveryCountResponse response = new DiscoveryCountResponse();

	    if (message.isOutputSources()) {

		response.setCount(message.getSources().size());

	    } else {

		response.setCount(total);
	    }

	    Map<String, Aggregate> aggregations = searchResponse.aggregations();

	    TermFrequencyMapType mapType = fromAgg(aggregations);

	    TermFrequencyMap tfMap = new TermFrequencyMap(mapType);

	    response.setTermFrequencyMap(tfMap);

	    return response;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderCountError", ex);
	}
    }

    @Override
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {

	ResultSet<GSResource> resultSet = new ResultSet<>();

	try {

	    SearchResponse<Object> response = discover_(message, false);

	    PerformanceLogger pl = new PerformanceLogger(//
		    PerformanceLogger.PerformancePhase.OPENSEARCH_FINDER_RESOURCES_CREATION, //
		    message.getRequestId(), //
		    Optional.ofNullable(message.getWebRequest()));

	    List<GSResource> resources = ConversionUtils.toBinaryList(response).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	    //
	    //
	    //

	    if (message.isOutputSources()) {

		List<GSResource> collect = resources.//
			stream().//
			filter(StreamUtils.distinctBy(GSResource::getSource)).//
			collect(Collectors.toList());

		resultSet.setResultsList(collect);

	    } else {

		resultSet.setResultsList(resources);
	    }

	    return resultSet;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    @Override
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException {

	ResultSet<Node> resultSet = new ResultSet<>();

	try {

	    SearchResponse<Object> response = discover_(message, false);

	    List<Node> nodes = ConversionUtils.toNodeList(response);

	    resultSet.setResultsList(nodes);

	    return resultSet;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    @Override
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	ResultSet<String> resultSet = new ResultSet<>();

	try {

	    SearchResponse<Object> response = discover_(message, false);

	    List<String> nodes = ConversionUtils.toStringList(response);

	    resultSet.setResultsList(nodes);

	    return resultSet;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    /**
     * @param aggs
     * @return
     */
    private TermFrequencyMapType fromAgg(Map<String, Aggregate> aggs) {
    
        TermFrequencyMapType mapType = new TermFrequencyMapType();
    
        aggs.keySet().forEach(target -> {
    
            Aggregate aggregate = aggs.get(target);
            StringTermsAggregate sterms = aggregate.sterms();
    
            Buckets<StringTermsBucket> buckets = sterms.buckets();
            List<StringTermsBucket> array = buckets.array();
    
            for (StringTermsBucket bucket : array) {
    
        	int count = (int) bucket.docCount();
        	String term = bucket.key();
    
        	TermFrequencyItem item = new TermFrequencyItem();
        	item.setTerm(term);
        	item.setDecodedTerm(term);
        	item.setFreq(count);
        	item.setLabel(target);
    
        	switch (TermFrequencyTarget.fromValue(target)) {
        	case ATTRIBUTE_IDENTIFIER:
        	    mapType.getAttributeId().add(item);
        	    break;
        	case ATTRIBUTE_TITLE:
        	    mapType.getAttributeTitle().add(item);
        	    break;
        	case FORMAT:
        	    mapType.getFormat().add(item);
        	    break;
        	case INSTRUMENT_IDENTIFIER:
        	    mapType.getInstrumentId().add(item);
        	    break;
        	case INSTRUMENT_TITLE:
        	    mapType.getAttributeTitle().add(item);
        	    break;
        	case KEYWORD:
        	    mapType.getKeyword().add(item);
        	    break;
        	case OBSERVED_PROPERTY_URI:
        	    mapType.getObservedPropertyURI().add(item);
        	    break;
        	case ORGANISATION_NAME:
        	    mapType.getOrganisationName().add(item);
        	    break;
        	case ORIGINATOR_ORGANISATION_DESCRIPTION:
        	    mapType.getOrigOrgDescription().add(item);
        	    break;
        	case ORIGINATOR_ORGANISATION_IDENTIFIER:
        	    mapType.getOrigOrgId().add(item);
        	    break;
        	case PLATFORM_IDENTIFIER:
        	    mapType.getPlatformId().add(item);
        	    break;
        	case PLATFORM_TITLE:
        	    mapType.getPlatformTitle().add(item);
        	    break;
        	case PROD_TYPE:
        	    mapType.getProdType().add(item);
        	    break;
        	case PROTOCOL:
        	    mapType.getProtocol().add(item);
        	    break;
        	case S3_INSTRUMENT_IDX:
        	    mapType.getS3InstrumentIdx().add(item);
        	    break;
        	case S3_PRODUCT_LEVEL:
        	    mapType.getS3ProductLevel().add(item);
        	    break;
        	case S3_TIMELINESS:
        	    mapType.getS3Timeliness().add(item);
        	    break;
        	case SAR_POL_CH:
        	    mapType.getSarPolCh().add(item);
        	    break;
        	case SENSOR_OP_MODE:
        	    mapType.getSensorOpMode().add(item);
        	    break;
        	case SENSOR_SWATH:
        	    mapType.getSensorSwath().add(item);
        	    break;
        	case SOURCE:
        	    mapType.getSourceId().add(item);
        	    break;
        	case SSC_SCORE:
        	    mapType.getSSCScore().add(item);
        	    break;
        	}
            }
        });
    
        return mapType;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     * @throws Exception
     */
    private HashMap<String, String> getSourceDataFolderMap(RequestMessage message) throws GSException {

	HashMap<String, String> out = new HashMap<>();

	List<String> ids = message.getSources().//
		stream().//
		map(s -> s.getUniqueIdentifier()).//
		collect(Collectors.toList());

	Query query = OpenSearchQueryBuilder.buildDataFolderQuery(getDatabase().getIdentifier(), ids);

	try {

	    wrapper.search(query, 0, ids.size()).//
		    hits().//
		    hits().//
		    stream().//
		    map(hit -> ConversionUtils.toJSONObject(hit.source()))//
		    .forEach(obj -> {

			out.put(obj.getString(MetaFolderMapping.SOURCE_ID), //
				obj.getString(MetaFolderMapping.DATA_FOLDER));
		    });

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	    throw GSException.createException(getClass(), "OpenSearchFinderSourceDataFolderMapError", ex);
	}

	return out;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private SearchResponse<Object> discover_(DiscoveryMessage message, boolean count) throws GSException {

	HashMap<String, String> map = getSourceDataFolderMap(message);

	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getPermittedBond());

	OpenSearchBondHandler handler = new OpenSearchBondHandler(wrapper, message, map);

	bondParser.parse(handler);

	Query query = handler.getQuery(count);
	System.out.println(ConversionUtils.toJSONObject(query).toString(3));

	try {

	    int start = message.getPage().getStart() - 1;
	    int size = message.getPage().getSize();

	    GSLoggerFactory.getLogger(getClass()).debug("\n\n{}\n\n",
		    new JSONObject(ConversionUtils.toJSONObject(query).toString(3)).toString(3));

	    SearchResponse<Object> response = count ? //
		    wrapper.count(query, message.getTermFrequencyTargets(), message.getMaxFrequencyMapItems()) : //
		    wrapper.search(query, start, size);

	    return response;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscover_Error", ex);
	}
    }
}
