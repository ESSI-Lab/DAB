package eu.essi_lab.model.configuration.option;

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
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
public class DateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

	try {

	    return DateDeserializers.DateDeserializer.instance.deserialize(p, ctxt);

	} catch (Throwable thr) {
	}

	TreeNode tree = p.readValueAsTree();

	if (tree.isArray()) {

	    int s = tree.size();

	    for (int i = 0; i < s; i++) {

		TreeNode obj = tree.get(i);

		if (obj.isValueNode()) {

		    if (obj.asToken().isNumeric()) {

			return new Date(Long.valueOf(obj.toString()));

		    }

		}

	    }

	}

	JsonToken token = tree.asToken();

	if (token.isNumeric())
	    return new Date(Long.valueOf(token.asString()));

	throw new IOException("Can't deserialize Date");
    }

    public Date deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {

	return deserialize(p, ctxt);

    }
}
