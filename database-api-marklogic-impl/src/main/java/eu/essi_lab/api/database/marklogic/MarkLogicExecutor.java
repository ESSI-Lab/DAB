/**
 * 
 */
package eu.essi_lab.api.database.marklogic;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.w3c.dom.Node;

import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.marklogic.executor.CoveringModeBondHandler;
import eu.essi_lab.api.database.marklogic.executor.ExecutorUtils;
import eu.essi_lab.api.database.marklogic.executor.eiffel.EiffelBondHandler;
import eu.essi_lab.api.database.marklogic.executor.eiffel.EiffelSpatialExtentHandler;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class MarkLogicExecutor extends MarkLogicReader implements DatabaseExecutor {

    @Override
    public WMSClusterResponse execute(WMSClusterRequest request) throws GSException {

	try {
	    String template = IOStreamUtils.asUTF8String(//
		    getClass().getClassLoader().getResourceAsStream("wms-cluster-query-template.txt"));

	    template = template.replace("MAX_RESULTS", String.valueOf(request.getMaxResults()));

	    template = template.replace("SOUTH", String.valueOf(request.getExtent().getSouth()));
	    template = template.replace("WEST", String.valueOf(request.getExtent().getWest()));
	    template = template.replace("NORTH", String.valueOf(request.getExtent().getNorth()));
	    template = template.replace("EAST", String.valueOf(request.getExtent().getEast()));

	    String viewQuery = ConfigurationWrapper.getViewSources(request.getView()).//
		    stream().//
		    map(v -> "gs:siq('" + v.getUniqueIdentifier() + "','preprodenvconf')\n").//
		    collect(Collectors.joining(",", "gs:orq((", "))"));

	    template = template.replace("VIEW_QUERY", viewQuery);

	    String responseString = getDatabase().getWrapper().submit(template).asString();

	    WMSClusterResponse response = new WMSClusterResponse();

	    if (!responseString.isEmpty()) {

		XMLDocumentReader reader = new XMLDocumentReader(responseString);

		boolean estimateResponse = reader.evaluateBoolean("exists(//*:estimate)");
		boolean datasetsResponse = reader.evaluateBoolean("exists(//*:datasets)");

		if (estimateResponse) {

		    Integer stationsCount = Integer.valueOf(reader.evaluateTextContent("//*:estimate/*:stationsCount/text()").get(0));
		    Integer totalCount = Integer.valueOf(reader.evaluateTextContent("//*:estimate/*:totalCount/text()").get(0));

		    Node termFrequency = reader.evaluateNode("//*:termFrequency");

		    TermFrequencyMap map = TermFrequencyMap.create(termFrequency);

		    response.setTotalCount(totalCount);
		    response.setStationsCount(stationsCount);

		    response.setMap(map);

		} else if (datasetsResponse) {

		    List<String> list = new ArrayList<String>(Arrays.asList(responseString.split("<gs:Dataset")));
		    list.remove(0); // removes <gs:datasets>

		    List<String> datasets = list.stream().map(s -> {

			String out = "<gs:Dataset " + s;
			out = out.replace("</gs:datasets>", "");

			return out;

		    }).collect(Collectors.toList());

		    response.setDatasets(datasets);
		}
	    }

	    return response;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "MarkLogicExecutorWMSClusterError", e);
	}
    }

    @Override
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {

	try {

	    // GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_STATS_COMPUTING_STARTED);

	    StatisticsResponse response = getDatabase().compute(message);

	    // GSLoggerFactory.getLogger(MarkLogicReader.class).trace(MARKLOGIC_READER_STATS_COMPUTING_ENDED);

	    return response;

	} catch (GSException e) {

	    throw e;
	}
    }

    /**
     * @param message
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    @Override
    public List<String> retrieveEiffelIds(DiscoveryMessage message, int start, int count) throws GSException {

	//
	//
	//

	View eiffelView = message.getView().get();

	Bond bond = null;

	if (message.getUserBond().isPresent()) {

	    bond = message.getUserBond().get();
	}

	LogicalBond userBond = BondFactory.createAndBond(bond, eiffelView.getBond());

	//
	// extracts the spatial bond of the Eiffel view. this is required to retrieve only ids of the Eiffel view
	// while other spatial extents provided are ignored by the EiffelAPIBondHandler
	//
	EiffelSpatialExtentHandler extentParser = new EiffelSpatialExtentHandler();

	DiscoveryBondParser bondParser = new DiscoveryBondParser(userBond);
	bondParser.parse(extentParser);

	SpatialExtent eiffelViewExtent = extentParser.getExtent();
	BondOperator eiffelViewOperator = extentParser.getOperator();

	//
	// this handler includes only search terms and the bonds of the Eiffel view
	//

	EiffelBondHandler handler = new EiffelBondHandler(//
		message, //
		getDatabase(), //
		eiffelViewExtent, //
		eiffelViewOperator);

	bondParser = new DiscoveryBondParser(userBond);
	bondParser.parse(handler);

	String eiffelViewAndUserMergedQuery = handler.getCTSSearchQuery(false);

	//
	//
	//

	List<String> list = new ArrayList<String>();

	MarkLogicWrapper wrapper = getDatabase().getWrapper();

	String query = ExecutorUtils.buildIndexValuesQuery(MetadataElement.IDENTIFIER, eiffelViewAndUserMergedQuery, start, count);

	try {

	    ResultSequence result = wrapper.submit(query);

	    list = Arrays.asList(result.asStrings()).stream().map(s -> s.trim()).collect(Collectors.toList());

	} catch (Exception ex) {

	    throw GSException.createException(getClass(), "MARK_LOGIC_RETRIEVE_EIFFEL_IDS_ERROR", ex);
	}

	return list;
    }

    @Override
    public List<String> getIndexValues(DiscoveryMessage message, MetadataElement element, int start, int count) throws GSException {

	try {

	    MarkLogicDiscoveryBondHandler handler = new MarkLogicDiscoveryBondHandler(message, getDatabase());

	    Bond bond = null;

	    if (message.getUserBond().isPresent()) {

		bond = message.getUserBond().get();
	    }

	    DiscoveryBondParser bondParser = new DiscoveryBondParser(bond);
	    bondParser.parse(handler);

	    String userQuery = handler.getCTSSearchQuery(false);

	    String indexValuesQuery = ExecutorUtils.buildIndexValuesQuery(element, userQuery, start, count);

	    MarkLogicWrapper wrapper = getDatabase().getWrapper();

	    ResultSequence result = wrapper.submit(indexValuesQuery);

	    return Arrays.asList(result.asStrings()).stream().map(s -> s.trim()).collect(Collectors.toList());

	} catch (Exception ex) {

	    throw GSException.createException(getClass(), "MARK_LOGIC_GET_INDEX_VALUES_ERROR", ex);
	}
    }

    @Override
    public JSONObject executePartitionsQuery(DiscoveryMessage message, boolean temporalConstraintEnabled) throws GSException {

	CoveringModeBondHandler handler = new CoveringModeBondHandler(//
		message, //
		getDatabase(), temporalConstraintEnabled);

	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getUserBond().get());
	bondParser.parse(handler);

	String ctsSearchQuery = handler.getCTSSearchQuery(false);

	String query = ExecutorUtils.buildPartitionQuery(ctsSearchQuery, 1);

	try {
	    ResultSequence response = this.getDatabase().getWrapper().submit(query);

	    return new JSONObject(response.asString());

	} catch (Exception ex) {

	    throw GSException.createException(getClass(), "MARK_LOGIC_EXECUTE_PARTITIONS_QUERY_ERROR", ex);
	}
    }

    @Override
    public void clearDeletedRecords() throws GSException {

	try {

	    getDatabase().execXQuery(ExecutorUtils.getClearDeletedQuery());

	} catch (RequestException e) {

	    throw GSException.createException(getClass(), "MARK_LOGIC_COUNT_DELETED_RECORDS_ERROR", e);
	}
    }

    @Override
    public int countDeletedRecords() throws GSException {

	try {
	    ResultSequence resultSequence = getDatabase().execXQuery(ExecutorUtils.getCountDeletedQuery());
	    return Integer.valueOf(resultSequence.asString());

	} catch (RequestException e) {

	    throw GSException.createException(getClass(), "MARK_LOGIC_COUNT_DELETED_RECORDS_ERROR", e);
	}
    }
}
