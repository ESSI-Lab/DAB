package eu.essi_lab.model.configuration;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
public class SubcomponentListDeserializer extends JsonDeserializer<List<Subcomponent>> {

    @Override
    public List<Subcomponent> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

	List<Subcomponent> list = new ArrayList<>();

	JsonNode node = p.readValueAsTree();

	for (JsonNode nn : node) {

	    JsonNode v = nn.get("value");

	    String value = "";
	    if (v != null)
		value = v.asText();

	    JsonNode l = nn.get("label");

	    String label = "";
	    if (l != null)
		label = l.asText();

	    list.add(new Subcomponent(label, value));

	}

	return list;

    }
}
