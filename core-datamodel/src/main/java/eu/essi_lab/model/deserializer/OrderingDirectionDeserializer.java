package eu.essi_lab.model.deserializer;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import eu.essi_lab.model.OrderingDirection;

/**
 * @author Fabrizio
 */
public class OrderingDirectionDeserializer extends JsonDeserializer<OrderingDirection> {

    @Override
    public OrderingDirection deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

	JsonNode node = p.readValueAsTree();
	String value = node.get("value").asText();

	OrderingDirection[] vals = OrderingDirection.values();

	for (OrderingDirection v : vals) {
	    if (value.equals(v.getName()))
		return v;
	}

	throw new JsonMappingException("value " + value + " does not match any value in enum OrderingDirection.");
    }
}
