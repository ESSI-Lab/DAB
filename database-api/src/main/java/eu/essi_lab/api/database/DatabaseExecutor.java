/**
 * 
 */
package eu.essi_lab.api.database;

import java.util.ArrayList;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.exceptions.GSException;
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

    /**
     * @author Fabrizio
     */
    public class WMSClusterRequest {

	private int maxResults;
	private SpatialExtent extent;
	private View view;

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
	 * @return the extent
	 */
	public SpatialExtent getExtent() {

	    return extent;
	}

	/**
	 * @param extent
	 */
	public void setExtent(SpatialExtent extent) {

	    this.extent = extent;
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
    }

    /**
     * @author Fabrizio
     */
    public class WMSClusterResponse {

	private List<String> datasets;
	private TermFrequencyMap map;
	private Integer stationsCount;
	private Integer totalCount;

	/**
	 * 
	 */
	public WMSClusterResponse() {

	    datasets = new ArrayList<String>();
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
	public void setDatasets(List<String> datasets) {

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
	public List<String> getDatasets() {

	    return datasets;
	}
    }

    /**
     * @param request
     * @return
     * @throws GSException
     */
    public WMSClusterResponse execute(WMSClusterRequest request) throws GSException;

}
