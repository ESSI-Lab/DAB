package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import javax.xml.stream.XMLInputFactory;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;
import eu.essi_lab.messages.DataDescriptorRuntimeInfo.TargetProvider;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;

/**
 * A result set represents the result of a discovery or access query. The query results
 * can be get and set respectively with the methods {@link #getResultsList()} and {@link #setResultsList(List)}.<br>
 * The parametric type <code>T</code> determines the Java type of the results; subclasses <i>should</i> only
 * set the appropriate type
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

    private XMLInputFactory factory;

    /**
     * 
     */
    public ResultSet() {
	super();
    }

    public ResultSet(List<T> results) {
	setException(GSException.createException(new ErrorInfo()));
	setResultsList(results);
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = new HashMap<>();

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

		List<String> discResourceTitlesList = new ArrayList<String>();
		List<String> discResourceAttributeTitlesList = new ArrayList<String>();
		List<String> discResourceIdsList = new ArrayList<String>();

		List<String> discSourceIdsList = new ArrayList<String>();
		List<String> discSourceLabelsList = new ArrayList<String>();

		List<String> accessSourceIdsList = new ArrayList<String>();
		List<String> accessSourceLabelsList = new ArrayList<String>();

		resultsList.forEach(item -> {

		    if (item instanceof GSResource) {

			GSResource resource = (GSResource) item;

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

			GSSource source = resource.getSource();

			discSourceIdsList.add(source.getUniqueIdentifier());
			discSourceLabelsList.add(source.getLabel());

		    } else if (item instanceof DataObject) {

			DataObject object = (DataObject) item;
			DataDescriptor descriptor = object.getDataDescriptor();

			DataDescriptorRuntimeInfo.publishDataDescriptorInfo(TargetProvider.RESULT_SET, descriptor, map);

			Optional<GSResource> resource = object.getResource();

			if (resource.isPresent()) {

			    GSSource source = resource.get().getSource();

			    GSLoggerFactory.getLogger(getClass()).trace("Accessed source: " + source.getLabel());
			    GSLoggerFactory.getLogger(getClass()).trace("Accessed source id: " + source.getUniqueIdentifier());

			    accessSourceIdsList.add(source.getUniqueIdentifier());
			    accessSourceLabelsList.add(source.getLabel());
			}
		    } else if (item instanceof String) {

			String strJSON = (String) item;

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

				String xmlEntry = strJSON;
				
				try {

				    StAXDocumentParser parser = new StAXDocumentParser(xmlEntry);

				    parser.add(Arrays.asList(new QName("entry"), new QName("id")), v -> discResourceIdsList.add(v));

				    parser.add(Arrays.asList(new QName("entry"), new QName("title")), v -> discResourceTitlesList.add(v));

				    parser.add(Arrays.asList(new QName("entry"), new QName("sourceId")), v -> discSourceIdsList.add(v));

				    parser.add(Arrays.asList(new QName("entry"), new QName("sourceTitle")), v -> discSourceLabelsList.add(v));

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
     * Creates a new {@link ResultSet} which is a clone of the supplied <code>resultSet</code>
     * but without {@link #getResultsList()}
     * 
     * @param resultSet a non <code>null</code> {@link ResultSet} to clone
     */
    public ResultSet(ResultSet<?> resultSet) {
	setException(resultSet.getException());
	setCountResponse(resultSet.getCountResponse());
    }

    /**
     * Creates a new {@link ResultSet} which is a clone of the supplied <code>resultSet</code>
     * but without {@link #getResultsList()}
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

    public static void main(String[] args) {
	JSONObject test = new JSONObject();
	test.put("key", "test':{{\" \b \f \n \r \t \\");
	System.out.println(test.toString());
    }
}
