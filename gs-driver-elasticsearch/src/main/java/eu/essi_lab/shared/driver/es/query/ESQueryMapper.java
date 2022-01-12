package eu.essi_lab.shared.driver.es.query;

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

import eu.essi_lab.messages.Page;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.messages.SharedContentTimeContraintCollection;
import eu.essi_lab.shared.model.SharedContentType;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
public class ESQueryMapper {

    private Page defaultPage;
    private static final String FROM_KEY = "from";
    private static final String SIZE_KEY = "size";
    private static final String QUERY_KEY = "query";
    private static final String MATCH_ALL_KEY = "match_all";

    private static final String RANGE_KEY = "range";
    private static final String BOOL_KEY = "bool";
    private static final String MUST_KEY = "must";

    public ESQueryMapper() {
	defaultPage = new Page(1, 10);
    }

    public JSONObject mapToQuery(SharedContentType type, SharedContentQuery query) {

	JSONObject json = new JSONObject();

	addPage(json, Optional.ofNullable(query.getPage()));

	addQuery(json, query);

	return json;

    }

    private void addQuery(JSONObject json, SharedContentQuery query) {

	json.put(QUERY_KEY, new JSONObject());

	Optional<SharedContentTimeContraintCollection> optional = query.getTimeConstraints();

	if (optional.isPresent())
	    doMapping(json, optional.get());
	else
	    addMatchAll(json);

    }

    private void doMapping(JSONObject json, SharedContentTimeContraintCollection timeContraintCollection) {

	addBool(json.getJSONObject(QUERY_KEY));

	addMust(json.getJSONObject(QUERY_KEY).getJSONObject(BOOL_KEY));

	//multiple ranges implemented as in https://stackoverflow.com/questions/20610999/query-in-elasticsearch-with-multiple-ranges-on-multiple-dates
	timeContraintCollection.getList().forEach(tc -> {

	    ESTimeRangeBuilder builder = new ESTimeRangeBuilder(tc.getTimeAxis());

	    JSONObject dateRange = builder.withFrom(tc.getFrom()).withTo(tc.getTo()).build();

	    appendRange(json.getJSONObject(QUERY_KEY).getJSONObject(BOOL_KEY).getJSONArray(MUST_KEY), dateRange);

	});

    }

    private void addMust(JSONObject json) {
	json.put(MUST_KEY, new JSONArray());
    }

    private void addBool(JSONObject json) {
	json.put(BOOL_KEY, new JSONObject());
    }

    private void appendRange(JSONArray array, JSONObject rangeContent) {

	JSONObject jsonObject = new JSONObject();

	jsonObject.put(RANGE_KEY, rangeContent);

	array.put(jsonObject);

    }

    private void addMatchAll(JSONObject json) {

	json.getJSONObject(QUERY_KEY).put(MATCH_ALL_KEY, new JSONObject());

    }

    private void addPage(JSONObject json, Optional<Page> page) {

	Page p = page.orElse(defaultPage);

	json.put(FROM_KEY, p.getStart() - 1);
	json.put(SIZE_KEY, p.getSize());

    }

}
