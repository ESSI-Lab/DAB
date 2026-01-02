package eu.essi_lab.json;

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

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONArrayReader {

    private JsonParser parser;
    private ObjectMapper mapper;

    public JSONArrayReader(File jsonFile) throws JsonParseException, IOException {
	JsonFactory jsonFactory = new JsonFactory();
	this.parser = jsonFactory.createParser(jsonFile);
	this.mapper = new ObjectMapper();
	if (parser.nextToken() != JsonToken.START_ARRAY) {
	    throw new IllegalStateException("Expected start of top-level array");
	}

    }

    /**
     * @return next item as string or null
     * @throws Exception
     * @throws JsonProcessingException
     */
    public String readNextItem() throws Exception {
	if (parser.isClosed()) {
	    return null;
	}
	if (parser.nextToken() != JsonToken.END_ARRAY) {
	    JsonNode subArray = mapper.readTree(parser);
	    String rawJson = mapper.writeValueAsString(subArray);
	    return rawJson;
	} else {
	    parser.close();
	    return null;
	}

    }

}
