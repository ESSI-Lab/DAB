/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.util.ArrayList;
import java.util.HashMap;

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
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.Buckets;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.aggregations.TopHitsAggregate;
import org.opensearch.client.opensearch._types.aggregations.TopHitsAggregation;
import org.opensearch.client.opensearch._types.aggregations.TopHitsAggregation.Builder;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class OpenSearchExecutor implements DatabaseExecutor {

    private Database database;
	private OpenSearchClient client;
	private OpenSearchWrapper wrapper;

	@Override
    public boolean supports(StorageInfo dbUri) {
    	return OpenSearchDatabase.isSupported(dbUri);
    }

    @Override
    public void setDatabase(Database database) {
    	this.database = database;
    	if (database instanceof OpenSearchDatabase) {
			OpenSearchDatabase osd = (OpenSearchDatabase) database;
			this.client = osd.getClient();
			this.wrapper = new OpenSearchWrapper(client);
		}

    }

    @Override
    public Database getDatabase() {

	return database;
    }

    @Override
    public void clearDeletedRecords() throws GSException {

    }

    @Override
    public int countDeletedRecords() throws GSException {

	return 0;
    }

    @Override
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {

	return null;
    }

    @Override
    public List<String> retrieveEiffelIds(DiscoveryMessage message, int start, int count) throws GSException {

	return null;
    }

    @Override
    public JSONObject executePartitionsQuery(DiscoveryMessage message, boolean temporalConstraintEnabled) throws GSException {

	return null;
    }

    @Override
    public List<String> getIndexValues(DiscoveryMessage message, MetadataElement element, int start, int count) throws GSException {

	return null;
    }

    @Override
    public List<WMSClusterResponse> execute(WMSClusterRequest request) throws GSException {

//    	GET data-folder-index/_search
//    	{
//    	  "size": 0,
//    	  "query": {
//    	    "bool": {
//    	      "must": [
//    	        { 
//    	          "range": { 
//    	            "tmpExtentBegin_date": { 
//    	              "gte": "2024-01-01T00:00:00", 
//    	              "lte": "2024-12-31T23:59:59" 
//    	            } 
//    	          } 
//    	        }
//    	      ]
//    	    }
//    	  },
//    	  "aggs": {
//    	    "regions": {
//    	      "filters": {
//    	        "filters": {
//    	          "region_1": { "geo_bounding_box": { "bbox": { "top_left": { "lat": 90.0, "lon": -180.0 }, "bottom_right": { "lat": 0.0, "lon": 180.0 } } } },
//    	          "region_2": { "geo_bounding_box": { "bbox": { "top_left": { "lat": 0.0, "lon": -180.0 }, "bottom_right": { "lat": -90.0, "lon": 180.0 } } } }
//    	        }
//    	      },
//    	      "aggs": {
//    	                "record_count": { "value_count": { "field": "_id" } },
//    	        "top_providers": {
//    	          "terms": {
//    	            "field": "sourceId_keyword",
//    	            "size": 5
//    	          }
//    	          },
//    	        "centroid": {
//    	          "geo_centroid": { "field": "centroid" }
//    	        }
//    	        
//    	        
//    	      }
//    	    }
//    	  }
//    	}

    	return null;

    }

}
