package eu.essi_lab.lib.sensorthings._1_1.model.entities;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * @author Fabrizio
 */
public class Entity extends JSONObjectWrapper {

    /**
     * 
     */
    private Downloader downloader;

    /**
     * 
     */
    public Entity() {

	downloader = new Downloader();
    }

    /**
     * @param entity
     */
    public Entity(JSONObject entity) {
    
        super(entity);
    
        downloader = new Downloader();
    }

    /**
     * @param entityUrl
     */
    protected Entity(URL entityUrl) {
    
        downloader = new Downloader();
    
        String entityString = downloader.downloadOptionalString(entityUrl.toString()).get();
    
        setObject(new JSONObject(entityString));
    }

    /**
     * @param object
     * @param type
     * @return
     */
    public static <T extends Entity> T create(JSONObject entity, Class<T> type) {

	T newInstance = null;
	try {
	    newInstance = type.getDeclaredConstructor().newInstance();
	    newInstance.setObject(entity);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(Entity.class).error(e);
	}

	return newInstance;
    }

    /**
     * @return
     */
    public Optional<String> getIdentifier() {

	return getObject().has("@iot.id") ? Optional.of(getObject().get("@iot.id").toString()) : Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> getSelfLink() {

	return getOptionalString("@iot.selfLink");
    }

    /**
     * @return
     */
    public Optional<String> getName() {

	return getOptionalString("name");
    }

    /**
     * @return
     */
    public Optional<String> getDescription() {

	return getOptionalString("description");
    }

    /**
     * @return
     */
    public Optional<JSONObject> getProperties() {

	return getObject().has("properties") ? Optional.of(getObject().getJSONObject("properties")) : Optional.empty();
    }

    /**
     * @param entityRef
     * @return
     */
    public List<Entity> getExtensions(EntityRef entityRef) {

	return getEntities(entityRef, Entity.class);
    }

    /**
     * @param entitySet
     * @return
     */
    public Optional<Boolean> isInline(EntityRef entitySet) {

	if (getObject().has(entitySet.getName())) {

	    return Optional.of(true);

	} else if (getObject().has(entitySet.getLink())) {

	    return Optional.of(false);
	}

	return Optional.empty();
    }

    /**
     * @param entitySet
     */
    public boolean clearInlineEntities(EntityRef entitySet) {

	Optional<Boolean> inline = isInline(entitySet);

	if (inline.isPresent() && inline.get()) {

	    getObject().remove(entitySet.getName());

	    return true;
	}

	return false;
    }

    /**
     * @param url
     * @return
     */
    protected Optional<String> download(String url) {

	return downloader.downloadOptionalString(url);
    }

    /**
     * @return
     */
    protected Optional<String> getOptionalString(String key) {

	return getObject().optString(key).isEmpty() ? Optional.empty() : Optional.of(getObject().getString(key));
    }

    /**
     * @return
     */
    protected <T extends Entity> List<T> getEntities(EntityRef entityRef, Class<T> type) {

	return getEntities(entityRef.getName(), entityRef.getLink(), type);
    }

    /**
     * @param entityName
     * @param entityLink
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T extends Entity> List<T> getEntities(String entityName, String entityLink, Class<T> type) {

	//
	// inline entities
	//
	if (getObject().has(entityName)) {

	    Object entities = getObject().get(entityName);

	    if (entities instanceof JSONArray) {

		JSONArray entitiesArray = (JSONArray) entities;

		return entitiesArray.//
			toList().//
			stream().//
			map(m -> new JSONObject((HashMap<String, String>) m)).//
			map(e -> create(e, type)).//
			collect(Collectors.toList());

	    } else {

		JSONObject entity = getObject().getJSONObject(entityName);
		return Arrays.asList(create(entity, type));
	    }
	}
	
	//
	// linked entities
	//
	else if (getObject().has(entityLink)) {

	    Optional<String> optEntities = download(getObject().getString(entityLink));

	    if (optEntities.isPresent()) {

		String entityResponse = optEntities.get();

		List<JSONObject> value = getValue(entityResponse);

		return value.stream().map(v -> create(v, type)).collect(Collectors.toList());
	    }
	}

	return new ArrayList<>();
    }

    /**
     * @param locationsResponse
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<JSONObject> getValue(String locationsResponse) {

	JSONObject jsonObject = new JSONObject(locationsResponse);

	if (jsonObject.has("value")) {

	    JSONArray array = jsonObject.getJSONArray("value");

	    return array.//
		    toList().//
		    stream().//
		    map(m -> new JSONObject((HashMap<String, String>) m)).//
		    collect(Collectors.toList());

	} else if (jsonObject.has("@iot.selfLink")) {

	    return Arrays.asList(jsonObject);

	} else {

	    return new ArrayList<>();
	}
    }
}
