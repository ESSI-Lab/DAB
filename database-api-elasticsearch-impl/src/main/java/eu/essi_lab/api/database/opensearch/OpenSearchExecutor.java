/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

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
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseExecutor.WMSClusterResponse;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class OpenSearchExecutor implements DatabaseExecutor {

    @Override
    public boolean supports(StorageInfo dbUri) {
return dbUri.getType().isPresent()&&dbUri.getType().get().equals("osl");
//	return false;
    }

    @Override
    public void setDatabase(Database dataBase) {

    }

    @Override
    public Database getDatabase() {

	return null;
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

//	return null;
    	try {

    	    ArrayList<WMSClusterResponse> responseList = new ArrayList<WMSClusterResponse>();

//    	    DecimalFormat format = new DecimalFormat();
//    	    format.setMaximumFractionDigits(5);

//    	    String template = IOStreamUtils.asUTF8String(//
//    		    getClass().getClassLoader().getResourceAsStream("wms-cluster-query-template.txt"));

    	    List<SpatialExtent> extents = request.getExtents();

//    	    String bboxes = extents.//
//    		    stream().//
//    		    map(e -> format.format(e.getSouth()) + "," + format.format(e.getWest()) + "," + format.format(e.getNorth()) + ","
//    			    + format.format(e.getEast()))
//    		    .//
//    		    collect(Collectors.joining("§", "'", "'"));

//    	    template = template.replace("MAX_RESULTS", String.valueOf(request.getMaxResults()));
//
//    	    template = template.replace("BBOXES", bboxes);

//    	    String viewQuery = ConfigurationWrapper.getViewSources(request.getView()).//
//    		    stream().//
//    		    map(v -> "gs:siq('" + v.getUniqueIdentifier() + "','preprodenvconf')\n").//
//    		    collect(Collectors.joining(",", "gs:orq((", "))"));

//    	    template = template.replace("VIEW_QUERY", viewQuery);

    	    Bond constraints = request.getConstraints();
    	    DiscoveryMessage message = new DiscoveryMessage();
    	    message.setUserBond(constraints);
//    	    MarkLogicDiscoveryBondHandler bondHandler = new MarkLogicDiscoveryBondHandler(message, getDatabase());
    	    DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getUserBond().get());
//    	    bondParser.parse(bondHandler);
//    	    template = template.replace("PARAMS_QUERY", bondHandler.getParsedQuery());

    	    //
    	    //
    	    //

//    	    String responseString = getDatabase().getWrapper().submit(template).asString();

//    	    XMLDocumentReader reader = new XMLDocumentReader(responseString);

    	    //
    	    // estimate responses
    	    //

//    	    List<Node> estimateNodes = Arrays.asList(reader.evaluateNodes("//*:response/*:estimate"));

//    	    for (Node estimateNode : estimateNodes) {
    		WMSClusterResponse response = new WMSClusterResponse();
//    		String bbox = reader.evaluateString(estimateNode, "@*:bbox");
//    		response.setBbox(bbox);

//    		Integer stationsCount = Integer.valueOf(reader.evaluateString(estimateNode, "*:stationsCount/text()"));
//    		Integer totalCount = Integer.valueOf(reader.evaluateString(estimateNode, "*:totalCount/text()"));

    		// term frequency
//    		Node termFrequency = reader.evaluateNode(estimateNode, "*:termFrequency");

//    		TermFrequencyMap map = TermFrequencyMap.create(termFrequency);

//    		response.setTotalCount(totalCount);
//    		response.setStationsCount(stationsCount);

//    		response.setMap(map);

    		// average extent
//    		Node avgBbox = reader.evaluateNode(estimateNode, "*:avgBbox");
//    		double south = Double.valueOf(reader.evaluateString(avgBbox, "*:south"));
//    		double west = Double.valueOf(reader.evaluateString(avgBbox, "*:west"));
//    		double north = Double.valueOf(reader.evaluateString(avgBbox, "*:north"));
//    		double east = Double.valueOf(reader.evaluateString(avgBbox, "*:east"));

//    		response.setAvgBbox(new SpatialExtent(south, west, north, east));

//    		responseList.add(response);
//    	    }

    	    //
    	    // datasets responses
    	    //

//    	    List<Node> datasetsNodes = Arrays.asList(reader.evaluateNodes("//*:response/*:datasets"));

//    	    for (Node datasetsNode : datasetsNodes) {
//    		WMSClusterResponse response = new WMSClusterResponse();

//    		String bbox = reader.evaluateString(datasetsNode, "@*:bbox");
//    		response.setBbox(bbox);

//    		List<Dataset> datasets = Arrays.asList(reader.evaluateNodes(datasetsNode, "*:Dataset")).//
//    			stream().map(n -> {
//    			    try {
//    				return (Dataset) Dataset.create(n);
//    			    } catch (Exception ex) {
//    				GSLoggerFactory.getLogger(getClass()).error(ex);
//    			    }
//    			    return null;
//    			}).//
//    			filter(Objects::nonNull).//
//    			collect(Collectors.toList());

//    		response.setDatasets(datasets);

//    		responseList.add(response);
//    	    }

    	    return responseList;
    	} catch (Exception e) {

    	    GSLoggerFactory.getLogger(getClass()).error(e);

    	    throw GSException.createException(getClass(), "OpenSearchExecutorWMSClusterError", e);
    	}

    }

}
