package eu.essi_lab.services.data_hub;

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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.services.message.*;
import org.apache.kafka.clients.consumer.*;
import org.json.*;

import java.util.*;

/**
 * @param timeStamp
 * @param entityURN
 * @param aspect
 * @param record
 * @param type
 * @author Fabrizio
 */
record DecodedRecord(//
	String timeStamp,//
	String entityURN, //
	JSONObject aspect,//
	ConsumerRecord<byte[], byte[]> record,//
	ChangeType type) {

    /**
     * @return
     */
    public Optional<JSONObject> optAspectValue() {

	return Optional.ofNullable(aspect);
    }

    /**
     * @param service
     * @param mapper
     * @param rawRecord
     * @return
     */
    static DecodedRecord of(DataHubService service, ObjectMapper mapper, Map<String, Object> rawRecord) {

	try {

	    Object data = rawRecord.get("data");

	    String json = mapper.writeValueAsString(data);

	    JSONObject jsonObject = new JSONObject(json);

	    JSONObject aspect = jsonObject.optJSONObject("aspect");

	    JSONObject aspectValue = null;

	    if (aspect != null) {

		aspectValue = aspect.optJSONObject("value");
	    }

	    String timeStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();
	    String entityURN = jsonObject.optString("entityUrn", "missing entityURN");
	    String changeType = jsonObject.optString("changeType", "missing changeType");

	    //noinspection unchecked
	    return new DecodedRecord(timeStamp, //
		    entityURN, //
		    aspectValue, //
		    (ConsumerRecord<byte[], byte[]>) rawRecord.get("record"), //
		    ChangeType.valueOf(changeType));

	} catch (JsonProcessingException e) {

	    service.publish(MessageChannel.MessageLevel.ERROR, "Error serializing data: " + e.getMessage());

	    GSLoggerFactory.getLogger(DataHubService.class).error(e);
	}

	return null;
    }
}
