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

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * This class is useful to stream parse arrays of JSON objects such as "[\n" + "
 * {\"name\": \"Alice\", \"age\": [23, 39]},\n" + " {\"name\": {\"first\":
 * \"Bob\", \"last\": [\"Smith\",[]]}, \"age\": 25},\n" + " {\"name\":
 * \"Charlie\", \"age\": 40}\n" + "]"
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

			@Override
			public void notifyJSONArray(JSONArray object) {

			}

		};
		parser.parse(bis, listener);

	}

	public void parse(InputStream bis, JSONArrayStreamParserListener listener) throws Exception {
		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jsonParser = jsonFactory.createParser(bis);

		// Move to the outer START_ARRAY
		if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
			throw new IOException("Expected START_ARRAY");
		}

		// Read each element inside the array
		while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
			JsonToken currentToken = jsonParser.getCurrentToken();
			if (currentToken == JsonToken.START_OBJECT) {
				String jsonObjectString = extractJsonObjectAsString(jsonParser);
				JSONObject json = new JSONObject(jsonObjectString);
				listener.notifyJSONObject(json);
			} else if (currentToken == JsonToken.START_ARRAY) {
				String jsonArrayString = extractJsonArrayAsString(jsonParser);
				JSONArray array = new JSONArray(jsonArrayString);
				listener.notifyJSONArray(array);
			} else {
				// handle primitives if needed (e.g., numbers, strings in array)
				// skip or wrap as JSONArray if required
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
	
	private static String extractJsonArrayAsString(JsonParser jsonParser) throws IOException {
		StringWriter stringWriter = new StringWriter();
		JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stringWriter);

		int depth = 0;
		JsonToken token = jsonParser.getCurrentToken();

		if (token != JsonToken.START_ARRAY) {
			throw new IOException("Expected START_ARRAY but found: " + token);
		}

		// Start copying events
		do {
			if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
				depth++;
			} else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
				depth--;
			}

			jsonGenerator.copyCurrentEvent(jsonParser);

			if (depth == 0) {
				break; // we’ve reached the end of this array
			}

			token = jsonParser.nextToken();
			if (token == null) {
				throw new IOException("Unexpected end of JSON while parsing array");
			}

		} while (true);

		jsonGenerator.flush();
		return stringWriter.toString();
	}


	
	
}
