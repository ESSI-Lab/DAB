/**
 * 
 */
package eu.essi_lab.api.database;

import java.util.ArrayList;

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

import org.json.JSONObject;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public interface DatabaseExecutor extends DatabaseProvider {

    /**
     * @throws GSException
     */
    public void clearDeletedRecords() throws GSException;

    /**
     * @return
     */
    public int countDeletedRecords() throws GSException;

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public StatisticsResponse compute(StatisticsMessage message) throws GSException;

    /**
     * @param message
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    public List<String> retrieveEiffelIds(DiscoveryMessage message, int start, int count) throws GSException;

    /**
     * @param message
     * @param temporalConstraintEnabled
     * @return
     * @throws GSException
     */
    public JSONObject executePartitionsQuery(DiscoveryMessage message, boolean temporalConstraintEnabled) throws GSException;

    /**
     * @param message
     * @param element
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    public List<String> getIndexValues(DiscoveryMessage message, MetadataElement element, int start, int count) throws GSException;
    
    public ResultSet<TermFrequencyItem> getIndexValues(DiscoveryMessage message, Queryable element, int count, String resumptionToken) throws GSException;

    /**
     * @author Fabrizio
     */
    public class WMSClusterRequest {

	private int maxResults;
	private int maxTermFrequencyItems = 50;

	public int getMaxTermFrequencyItems() {
	    return maxTermFrequencyItems;
	}

	public void setMaxTermFrequencyItems(int maxTermFrequencyItems) {
	    this.maxTermFrequencyItems = maxTermFrequencyItems;
	}

	private List<SpatialExtent> extents;
	private View view;
	private Bond constraints;

	/**
	 * 
	 */
	public WMSClusterRequest() {

	    extents = new ArrayList<SpatialExtent>();
	}

	/**
	 * @return
	 */
	public int getMaxResults() {

	    return maxResults;
	}

	/**
	 * @param maxResults
	 */
	public void setMaxResults(int maxResults) {

	    this.maxResults = maxResults;
	}

	/**
	 * @return
	 */
	public List<SpatialExtent> getExtents() {

	    return extents;
	}

	/**
	 * @param extent
	 */
	public void addExtent(SpatialExtent extent) {

	    this.extents.add(extent);
	}

	/**
	 * @return
	 */
	public View getView() {

	    return view;
	}

	/**
	 * @param view
	 */
	public void setView(View view) {

	    this.view = view;
	}

	/**
	 * @param requestBond
	 */
	public void setConstraints(Bond requestBond) {

	    this.constraints = requestBond;
	}

	/**
	 * @return
	 */
	public Bond getConstraints() {

	    return constraints;
	}
    }

    /**
     * @author Fabrizio
     */
    public class WMSClusterResponse {

	private List<Dataset> datasets;
	private TermFrequencyMap map;
	private Integer stationsCount;
	private Integer totalCount;
	private SpatialExtent bbox;
	private SpatialExtent avgBbox;

	/**
	 * 
	 */
	public WMSClusterResponse() {

	    datasets = new ArrayList<>();
	}

	/**
	 * @return
	 */
	public Optional<SpatialExtent> getAvgBbox() {

	    return Optional.ofNullable(avgBbox);
	}

	/**
	 * @param avgBbox
	 */
	public void setAvgBbox(SpatialExtent avgBbox) {

	    this.avgBbox = avgBbox;
	}

	/**
	 * @return
	 */
	public SpatialExtent getBbox() {

	    return bbox;
	}

	/**
	 * @param bbox
	 */
	public void setBbox(String bbox) {

	    double south = Double.valueOf(bbox.split(",")[0]);
	    double west = Double.valueOf(bbox.split(",")[1]);
	    double north = Double.valueOf(bbox.split(",")[2]);
	    double east = Double.valueOf(bbox.split(",")[3]);

	    this.bbox = new SpatialExtent(south, west, north, east);
	}

	public void setBbox(SpatialExtent bbox) {

	    this.bbox = bbox;
	}

	/**
	 * @return
	 */
	public Optional<Integer> getStationsCount() {

	    return Optional.ofNullable(stationsCount);
	}

	/**
	 * @param stationsCount
	 */
	public void setStationsCount(int stationsCount) {

	    this.stationsCount = stationsCount;
	}

	/**
	 * @return the totalCount
	 */
	public Optional<Integer> getTotalCount() {

	    return Optional.ofNullable(totalCount);
	}

	/**
	 * @param totalCount
	 */
	public void setTotalCount(int totalCount) {

	    this.totalCount = totalCount;
	}

	/**
	 * @param datasets
	 */
	public void setDatasets(List<Dataset> datasets) {

	    this.datasets = datasets;
	}

	/**
	 * @param map
	 */
	public void setMap(TermFrequencyMap map) {

	    this.map = map;
	}

	/**
	 * @return
	 */
	public Optional<TermFrequencyMap> getMap() {

	    return Optional.ofNullable(map);
	}

	/**
	 * @return
	 */
	public List<Dataset> getDatasets() {

	    return datasets;
	}
    }

    /**
     * @param request
     * @return
     * @throws GSException
     */
    public List<WMSClusterResponse> execute(WMSClusterRequest request) throws GSException;

    /**
     * @param message
     * @return
     * @throws Exception
     */
    public ResultSet<String> discoverDistinctStrings(DiscoveryMessage message) throws Exception;

}
