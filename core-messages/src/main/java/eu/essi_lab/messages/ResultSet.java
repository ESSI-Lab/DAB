package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.DataDescriptorRuntimeInfo.TargetProvider;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
public class ResultSet<T> extends MessageResponse<T, CountSet> {

    /**
     * 
     */
    public ResultSet() {
	super();
    }

    public ResultSet(List<T> results) {
	setException(new GSException());
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

		List<String> titlesList = new ArrayList<String>();

		resultsList.forEach(item -> {

		    if (item instanceof GSResource) {

			GSResource resource = (GSResource) item;
			String title = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
			if (StringUtils.isNotEmptyAndNotNull(title)) {
			    titlesList.add(title);
			}

			GSSource source = resource.getSource();
			map.put(RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_ID.getName(), //
				Arrays.asList(source.getUniqueIdentifier()));
			map.put(RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_LABEL.getName(), //
				Arrays.asList(source.getLabel()));

			map.put(RuntimeInfoElement.RESULT_SET_RESOURCE_ID.getName(),
				Arrays.asList(resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier()));
			try {
			    map.put(RuntimeInfoElement.RESULT_SET_ATTRIBUTE_TITLE.getName(), Arrays.asList(resource.getHarmonizedMetadata()
				    .getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeTitle()));
			} catch (Exception e) {
			    // TODO: handle exception
			}

		    } else if (item instanceof DataObject) {

			DataObject object = (DataObject) item;
			DataDescriptor descriptor = object.getDataDescriptor();

			DataDescriptorRuntimeInfo.publishDataDescriptorInfo(TargetProvider.RESULT_SET, descriptor, map);

			Optional<GSResource> resource = object.getResource();
			if (resource.isPresent()) {
			    GSSource source = resource.get().getSource();
			    GSLoggerFactory.getLogger(getClass()).trace("Accessed source: " + source.getLabel());
			    GSLoggerFactory.getLogger(getClass()).trace("Accessed source id: " + source.getUniqueIdentifier());

			    map.put(RuntimeInfoElement.RESULT_SET_ACCESS_SOURCE_ID.getName(), //
				    Arrays.asList(source.getUniqueIdentifier()));
			    map.put(RuntimeInfoElement.RESULT_SET_ACCESS_SOURCE_LABEL.getName(), //
				    Arrays.asList(source.getLabel()));
			}
		    } else if (item instanceof String) {
			String strJSON = (String) item;
			try {
			    JSONObject json = new JSONObject(strJSON);
			    String title = json.getString("title");
			    titlesList.add(title);
			    map.put(RuntimeInfoElement.RESULT_SET_RESOURCE_ID.getName(), Arrays.asList(json.getString("id")));
			    map.put(RuntimeInfoElement.RESULT_SET_ATTRIBUTE_TITLE.getName(),
				    Arrays.asList(json.getString("attributeTitle")));
			} catch (Exception e) {
			}
		    }
		});

		if (!titlesList.isEmpty()) {
		    map.put(RuntimeInfoElement.RESULT_SET_RESOURCE_TITLE.getName(), titlesList);
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
