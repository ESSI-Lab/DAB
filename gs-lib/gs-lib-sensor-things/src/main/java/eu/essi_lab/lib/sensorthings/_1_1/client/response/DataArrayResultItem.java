package eu.essi_lab.lib.sensorthings._1_1.client.response;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * <pre>
 * <code>
 * {
  "@iot.nextLink": "http://example.org/v1.1/Datastreams(1)/Observations?$resultFormat=dataArray&$skip=3",
  "@iot.count": 42,
  "value": [
    {
      "Datastream@iot.navigationLink": "http://example.org/v1.1/Datastreams(1)",
      "components": [
        "id",
        "phenomenonTime",
        "resultTime",
        "result"
      ],
      "dataArray": [
        [
          1,
          "2005-08-05T12:21:13Z",
          "2005-08-05T12:21:13Z",
          20
        ],
        [
          2,
          "2005-08-05T12:22:08Z",
          "2005-08-05T12:21:13Z",
          30
        ],
        [
          3,
          "2005-08-05T12:22:54Z",
          "2005-08-05T12:21:13Z",
          0
        ]
      ]
    }
  ]
}
 * </pre>
 * </code>
 * 
 * @see https://docs.ogc.org/is/18-088/18-088.html#_response_3
 * @author Fabrizio
 */
public class DataArrayResultItem extends JSONObjectWrapper {

    /**
     * @param object
     */
    public DataArrayResultItem(JSONObject object) {

	super(object);
    }

    /**
     * @return
     */
    public String getNavigationLink() {

	return getObject().getString("Datastream@iot.navigationLink");
    }

    /**
     * @return
     */
    public JSONArray getComponents() {

	return getObject().getJSONArray("components");
    }

    /**
     * @return
     */
    public JSONArray getDataArray() {

	return getObject().getJSONArray("dataArray");

    }
}
