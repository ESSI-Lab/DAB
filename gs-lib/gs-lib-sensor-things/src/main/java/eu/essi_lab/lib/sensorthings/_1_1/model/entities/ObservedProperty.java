package eu.essi_lab.lib.sensorthings._1_1.model.entities;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;

/**
 * https://docs.ogc.org/is/18-088/18-088.html#observedproperty
 * 
 * @author Fabrizio
 */
public class ObservedProperty extends Entity {

    /**
     * 
     */
    protected ObservedProperty() {
    }

    /**
     * @param entity
     */
    public ObservedProperty(JSONObject entity) {

	super(entity);
    }

    /**
     * @param entityUrl
     */
    public ObservedProperty(URL entityUrl) {

	super(entityUrl);
    }

    /**
     * @return
     * @throws URISyntaxException
     * @throws JSONException
     */
    public URI getDefinition() throws JSONException, URISyntaxException {

	return new URI(getObject().getString("definition"));
    }

    /**
     * @return
     */
    public List<Datastream> getDatastreams() {

	return getEntities(EntityRef.DATASTREAMS, Datastream.class);
    }
}
