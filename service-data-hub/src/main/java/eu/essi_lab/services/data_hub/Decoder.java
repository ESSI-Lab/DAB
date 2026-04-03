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

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import org.apache.avro.*;
import org.apache.avro.generic.*;
import org.apache.avro.io.*;
import org.apache.kafka.clients.consumer.*;

import java.net.*;
import java.net.http.*;
import java.nio.*;
import java.util.*;

/**
 * @author Fabrizio
 */
class Decoder {

    /**
     *
     */
    private final ObjectMapper mapper;
    private final Map<Integer, Schema> schemaCache;
    private final DataHubService service;

    /**
     * @param service
     */
    Decoder(DataHubService service) {

	this.service = service;
	this.mapper = new ObjectMapper();
	this.schemaCache = new HashMap<>();
    }

    /**
     * @param consumerRecord
     * @param raw
     * @param schemaRegistryURL
     * @param token
     * @return
     */
    Optional<Map<String, Object>> decode( //

	    ConsumerRecord<byte[], byte[]> consumerRecord,  //
	    String schemaRegistryURL, //
	    String token) {

	try {

	    GenericRecord record = deserialize(consumerRecord.value(), schemaRegistryURL, token);

	    String json = record.toString();
	    Map<String, Object> map = mapper.readValue(json, new TypeReference<>() {
	    });

	    if (map.containsKey("aspect")) {

		Object aspectObj = map.get("aspect");

		if (aspectObj instanceof Map) {

		    @SuppressWarnings("unchecked")
		    Map<String, Object> aspect = (Map<String, Object>) aspectObj;

		    Object value = aspect.get("value");

		    if (value instanceof byte[] bytes) {

			String str = new String(bytes);

			try {

			    aspect.put("value", mapper.readValue(str, Object.class));

			} catch (Exception e) {

			    aspect.put("value", str);
			}
		    }

		    if (value instanceof String str) {

			try {
			    aspect.put("value", mapper.readValue(str, Object.class));
			} catch (Exception ignored) {
			}
		    }
		}
	    }

	    Map<String, Object> output = new HashMap<>();

	    output.put("timestamp", consumerRecord.timestamp());
	    output.put("data", map);
	    output.put("record", consumerRecord);

	    return Optional.of(output);

	} catch (Exception e) {

	    service.error("Error decoding message:" + e.getMessage(), e, true);
	}

	return Optional.empty();
    }

    /**
     * @param raw
     * @param schemaRegistryUrl
     * @param token
     * @return
     * @throws Exception
     */
    private GenericRecord deserialize(byte[] raw, String schemaRegistryUrl, String token) throws Exception {

	ByteBuffer buffer = ByteBuffer.wrap(raw);

	buffer.get(); // magic byte
	int schemaId = buffer.getInt();

	Schema schema = getSchema(schemaRegistryUrl, schemaId, token);

	byte[] avroBytes = new byte[buffer.remaining()];
	buffer.get(avroBytes);

	BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);

	GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);

	return reader.read(null, decoder);
    }

    /**
     * @param schemaRegistryUrl
     * @param schemaId
     * @param token
     * @return
     * @throws Exception
     */
    private Schema getSchema(String schemaRegistryUrl, int schemaId, String token) throws Exception {

	if (!schemaCache.containsKey(schemaId)) {

	    HttpResponse<String> response;

	    try (HttpClient client = HttpClient.newHttpClient()) {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(schemaRegistryUrl + "/schemas/ids/" + schemaId))
			.header("Authorization", "Bearer " + token).GET().build();

		response = client.send(request, HttpResponse.BodyHandlers.ofString());
	    }

	    JsonNode json = mapper.readTree(response.body());

	    String schemaStr = json.get("schema").asText();

	    Schema schema = new Schema.Parser().parse(schemaStr);

	    schemaCache.put(schemaId, schema);
	}

	return schemaCache.get(schemaId);
    }

    /**
     * @return
     */
    ObjectMapper getMapper() {

	return mapper;
    }
}
