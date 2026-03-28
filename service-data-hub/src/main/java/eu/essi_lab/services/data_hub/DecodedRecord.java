package eu.essi_lab.services.data_hub;

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
    static DecodedRecord of(DataHUBService service, ObjectMapper mapper, Map<String, Object> rawRecord) {

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

	    service.publish(MessageChannel.MessageLevel.INFO, "Message: " + timeStamp + "/" + entityURN + "/" + changeType);

	    return new DecodedRecord(timeStamp, //
		    entityURN, //
		    aspectValue, //
		    ((ConsumerRecord<byte[], byte[]>) rawRecord.get("record")), //
		    ChangeType.valueOf(changeType));

	} catch (JsonProcessingException e) {

	    service.publish(MessageChannel.MessageLevel.ERROR, "Error serializing data: " + e.getMessage());

	    GSLoggerFactory.getLogger(DataHUBService.class).error(e);
	}

	return null;
    }
}