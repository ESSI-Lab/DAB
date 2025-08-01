package eu.essi_lab.lib.utils;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * This class is useful to stream parse arrays of JSON objects such as "[\n" +
 * " {\"name\": \"Alice\", \"age\": [23, 39]},\n" +
 * " {\"name\": {\"first\": \"Bob\", \"last\": [\"Smith\",[]]}, \"age\": 25},\n" +
 * " {\"name\": \"Charlie\", \"age\": 40}\n" +
 * "]"
 * 
 * @author boldrini
 */
public class JSONArrayStreamParser {

    public static void main(String[] args) throws Exception {

	String jsonString = "[\n"
		+ "    {\"name\": {\"name\": {\"first\": \"Bob\", \"last\": [\"Smith\",[]]}, \"age\": 25}},{ \"age\": [23, 39]},\n"
		+ "    {\"name\": {\"first\": \"Bob\", \"last\": [\"Smith\",[]]}, \"age\": 25},\n"
		+ "    {\"name\": \"Charlie\", \"age\": 40}\n" + "]";

	ByteArrayInputStream bis = new ByteArrayInputStream(jsonString.getBytes());

	JSONArrayStreamParser parser = new JSONArrayStreamParser();

	JSONArrayStreamParserListener listener = new JSONArrayStreamParserListener() {

	    @Override
	    public void finished() {
		System.out.println("finished");

	    }

	    @Override
	    public void notifyJSONObject(JSONObject object) {
		System.out.println(object.toString());

	    }

	};
	parser.parse(bis, listener);

    }

    public JSONObject parseFirstObject(InputStream inputStream) throws IOException {
	JsonFactory jsonFactory = new JsonFactory();
	JsonParser jsonParser = jsonFactory.createParser(inputStream);

	if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
	    throw new IOException("Expected JSON array");
	}

	while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
	    if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
		String jsonObjectString = extractJsonObjectAsString(jsonParser);
		return new JSONObject(jsonObjectString);
	    }
	}

	throw new IOException("No JSON object found in array");
    }

    public void parse(InputStream bis, JSONArrayStreamParserListener listener) throws Exception {
	JsonFactory jsonFactory = new JsonFactory();
	JsonParser jsonParser = jsonFactory.createParser(bis);

	while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
	    // Check if the token is the start of an object
	    if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
		// Extract the JSON object as a string and process it
		String jsonObjectString = extractJsonObjectAsString(jsonParser);
		JSONObject json = new JSONObject(jsonObjectString);
		listener.notifyJSONObject(json);
	    }
	}
	listener.finished();
	jsonParser.close();

    }

    private static String extractJsonObjectAsString(JsonParser jsonParser) throws IOException {
	StringWriter stringWriter = new StringWriter();
	JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stringWriter);

	Deque<JsonToken> stack = new ArrayDeque<>();
	int openBraces = 1; // Count opening braces to ensure the object is complete

	// We are at the START_OBJECT token, so let's write the initial '{'
	jsonGenerator.writeStartObject();
	stack.push(JsonToken.START_OBJECT);

	while (openBraces > 0) {
	    JsonToken token = jsonParser.nextToken();
	    if (token == null) {
		throw new IOException("Incomplete JSON object");
	    }

	    if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
		openBraces++;
		stack.push(token);
	    } else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
		openBraces--;
		stack.pop();
	    }

	    // We only copy tokens when inside an object or array context
	    if (!stack.isEmpty() && (stack.peek() == JsonToken.START_OBJECT || stack.peek() == JsonToken.START_ARRAY)) {
		jsonGenerator.copyCurrentEvent(jsonParser);
	    }
	}
	if (stack.isEmpty()) {
	    jsonGenerator.writeEndObject();
	}

	jsonGenerator.flush();

	return stringWriter.toString();
    }
}
