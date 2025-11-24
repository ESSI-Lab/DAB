package eu.essi_lab.messages;

import java.math.BigDecimal;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;
import eu.essi_lab.messages.DataDescriptorRuntimeInfo.TargetProvider;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;

/**
 * A result set represents the result of a discovery or access query. The query
 * results can be get and set respectively with the methods
 * {@link #getResultsList()} and {@link #setResultsList(List)}.<br>
 * The parametric type <code>T</code> determines the Java type of the results;
 * subclasses <i>should</i> only set the appropriate type
 *
 * @param <T> the type of the query results
 * @author Fabrizio
 * @see {@link #getResultsList()}
 * @see {@link #setResultsList(List)}
 * @see ResultSet
 * @see DiscoveryMessage
 * @see AccessMessage
 */
public class ResultSet<T> extends MessageResponse<T, CountSet> {

    /**
     * 
     */
    public ResultSet() {
	super();
    }

    /**
     * @param results
     */
    public ResultSet(List<T> results) {
	setException(GSException.createException());
	setResultsList(results);
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = super.provideInfo();

	map.put(//
		RuntimeInfoElement.RESULT_SET_TIME_STAMP.getName(), //
		Arrays.asList(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds()));
	map.put(//
		RuntimeInfoElement.RESULT_SET_TIME_STAMP_MILLIS.getName(), //
		Arrays.asList(String.valueOf(System.currentTimeMillis())));

	CountSet countResponse = getCountResponse();
	if (countResponse != null) {

	    int count = countResponse.getCount();
	    map.put(RuntimeInfoElement.RESULT_SET_MATCHED.getName(), Arrays.asList(String.valueOf(count)));
	}

	List<? extends T> resultsList = getResultsList();
	if (resultsList != null) {

	    int size = resultsList.size();
	    map.put(RuntimeInfoElement.RESULT_SET_RETURNED.getName(), Arrays.asList(String.valueOf(size)));

	    if (!resultsList.isEmpty()) {

		List<String> discResourceTitlesList = new ArrayList<>();
		List<String> discResourceAttributeTitlesList = new ArrayList<>();
		List<String> discResourceIdsList = new ArrayList<>();

		List<String> discSourceIdsList = new ArrayList<>();
		List<String> discSourceLabelsList = new ArrayList<>();

		List<String> discBboxNorthList = new ArrayList<>();
		List<String> discBboxSouthList = new ArrayList<>();
		List<String> discBboxEastList = new ArrayList<>();
		List<String> discBboxWestList = new ArrayList<>();

		List<String> accessSourceIdsList = new ArrayList<>();
		List<String> accessSourceLabelsList = new ArrayList<>();

		resultsList.forEach(item -> {

		    if (item instanceof GSResource resource) {

			String title = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();

			if (StringUtils.isNotEmptyAndNotNull(title)) {
			    discResourceTitlesList.add(title);
			}

			discResourceIdsList.add(resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier());

			try {

			    discResourceAttributeTitlesList.add(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
				    .getCoverageDescription().getAttributeTitle());

			} catch (Exception e) {
			}

			//
			//
			//

			GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
			if (bbox != null) {
			    BigDecimal optEast = bbox.getBigDecimalEast();
			    BigDecimal optWest = bbox.getBigDecimalEast();
			    BigDecimal optSouth = bbox.getBigDecimalEast();
			    BigDecimal optNorth = bbox.getBigDecimalEast();
			    discBboxNorthList.add(optNorth.toString());
			    discBboxSouthList.add(optSouth.toString());
			    discBboxEastList.add(optEast.toString());
			    discBboxWestList.add(optWest.toString());
			}

			GSSource source = resource.getSource();

			discSourceIdsList.add(source.getUniqueIdentifier());
			discSourceLabelsList.add(source.getLabel());

		    } else if (item instanceof DataObject object) {

			DataDescriptor descriptor = object.getDataDescriptor();

			DataDescriptorRuntimeInfo.publishDataDescriptorInfo(TargetProvider.RESULT_SET, descriptor, map);

			Optional<GSResource> resource = object.getResource();

			if (resource.isPresent()) {

			    GSSource source = resource.get().getSource();

			    GSLoggerFactory.getLogger(getClass()).trace("Accessed source: " + source.getLabel());
			    GSLoggerFactory.getLogger(getClass()).trace("Accessed source id: " + source.getUniqueIdentifier());

			    accessSourceIdsList.add(source.getUniqueIdentifier());
			    accessSourceLabelsList.add(source.getLabel());
			    GeographicBoundingBox bbox = resource.get().getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
			    if (bbox != null) {
				BigDecimal optEast = bbox.getBigDecimalEast();
				BigDecimal optWest = bbox.getBigDecimalEast();
				BigDecimal optSouth = bbox.getBigDecimalEast();
				BigDecimal optNorth = bbox.getBigDecimalEast();
				discBboxNorthList.add(optNorth.toString());
				discBboxSouthList.add(optSouth.toString());
				discBboxEastList.add(optEast.toString());
				discBboxWestList.add(optWest.toString());
			    }

			}
		    } else if (item instanceof String strJSON) {

			try {
			    JSONObject json = new JSONObject(strJSON);

			    if (json.has("title")) {

				discResourceTitlesList.add(json.getString("title"));
			    }

			    if (json.has("id")) {

				discResourceIdsList.add(json.getString("id"));
			    }

			    if (json.has("attributeTitle")) {

				discResourceAttributeTitlesList.add(json.getString("attributeTitle"));
			    }

			    if (json.has("source")) {

				JSONObject jsonSource = json.getJSONObject("source");

				String sourceId = jsonSource.getString("id");
				String sourceLabel = jsonSource.getString("title");

				discSourceIdsList.add(sourceId);
				discSourceLabelsList.add(sourceLabel);
			    }

			} catch (Exception e) {

			    if (strJSON.startsWith("<entry")) {

				try {

				    StAXDocumentParser parser = new StAXDocumentParser(strJSON);

				    parser.add(Arrays.asList(new QName("entry"), new QName("id")), discResourceIdsList::add);

				    parser.add(Arrays.asList(new QName("entry"), new QName("title")), discResourceTitlesList::add);

				    parser.add(Arrays.asList(new QName("entry"), new QName("sourceId")), discSourceIdsList::add);

				    parser.add(Arrays.asList(new QName("entry"), new QName("sourceTitle")), discSourceLabelsList::add);

				    parser.parse();

				} catch (Exception ex) {

				    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
				}
			    }
			}
		    }
		});

		if (!discResourceIdsList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_RESOURCE_ID.getName(), discResourceIdsList);
		}

		if (!discResourceTitlesList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_RESOURCE_TITLE.getName(), discResourceTitlesList);
		}

		if (!discResourceAttributeTitlesList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_ATTRIBUTE_TITLE.getName(), discResourceAttributeTitlesList);
		}

		if (!discBboxNorthList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_BBOX_NORTH.getName(), discBboxNorthList);
		}
		if (!discBboxSouthList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_BBOX_SOUTH.getName(), discBboxSouthList);
		}
		if (!discBboxWestList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_BBOX_WEST.getName(), discBboxWestList);
		}
		if (!discBboxEastList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_BBOX_EAST.getName(), discBboxEastList);
		}

		//
		//
		//

		if (!discSourceIdsList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_ID.getName(), discSourceIdsList);
		}

		if (!discSourceLabelsList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_LABEL.getName(), discSourceLabelsList);
		}

		//
		//
		//

		if (!accessSourceIdsList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_ACCESS_SOURCE_ID.getName(), accessSourceIdsList);
		}

		if (!accessSourceLabelsList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_ACCESS_SOURCE_LABEL.getName(), accessSourceLabelsList);
		}
	    }
	}

	return map;
    }

    /**
     * Creates a new {@link ResultSet} which is a clone of the supplied
     * <code>resultSet</code> but without {@link #getResultsList()}
     * 
     * @param resultSet a non <code>null</code> {@link ResultSet} to clone
     */
    public ResultSet(ResultSet<?> resultSet) {
	setException(resultSet.getException());
	setCountResponse(resultSet.getCountResponse());
    }

    /**
     * Creates a new {@link ResultSet} which is a clone of the supplied
     * <code>resultSet</code> but without {@link #getResultsList()}
     * 
     * @param resultSet a non <code>null</code> {@link ResultSet} to clone
     */
    public ResultSet(MessageResponse<?, CountSet> resultSet) {
	setException(resultSet.getException());
	setCountResponse(resultSet.getCountResponse());
    }

    @Override
    public String getName() {

	return "RESULT_SET";
    }
}
