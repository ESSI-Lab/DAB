/**
 * 
 */
package eu.essi_lab.lib.net.nominatim;

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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;

/**
 * @author Fabrizio
 */
public class NominatimResponse {

    private int placeId; // 68400600
    private String osmType; // relation
    private int osmId; // 41485
    private double lat; // 41.8933203
    private double lon; // 12.4829321
    private String category; // boundary
    private String type; // administrative
    private int placeRank; // 16,
    private double importance; // 0.8439277725492005,
    private String addresstype; // city"
    private String name; // Rome
    private String displayName; // Rome, Roma Capitale, Lazio, Italy
    private GeographicBoundingBox bbox;

    /**
     * @param object
     */
    public NominatimResponse(JSONObject object) {

	placeId = object.getInt("place_id");
	osmType = object.getString("osm_type");

	osmId = object.getInt("osm_id");

	lat = object.getDouble("lat");
	lon = object.getDouble("lon");

	category = object.getString("category");
	type = object.getString("type");
	placeRank = object.getInt("place_rank");
	importance = object.getDouble("importance");
	addresstype = object.getString("addresstype");
	name = object.getString("name");
	displayName = object.getString("display_name");

	JSONArray bboxArray = object.getJSONArray("boundingbox");

	bbox = new GeographicBoundingBox();
	
	bbox.setNorth(bboxArray.getDouble(1));
	bbox.setEast(bboxArray.getDouble(3));
	
	bbox.setSouth(bboxArray.getDouble(0));
	bbox.setWest(bboxArray.getDouble(2));
    }

    @Override
    public String toString() {

	return "- Place id: " + placeId + "\n" + "- OSM type: " + osmType + "\n" + "- OSM id: " + osmId + "\n" + "- Lat: " + lat + "\n"
		+ "- Lon: " + lon + "\n" + "- Category: " + category + "\n" + "- Type: " + type + "\n" + "- Place rank: " + placeRank + "\n"
		+ "- Address type: " + addresstype + "\n" + "- Name: " + name + "\n" + bbox + "\n";

    }

    /**
     * @return the placeId
     */
    public int getPlaceId() {
	return placeId;
    }

    /**
     * @return the osmType
     */
    public String getOsmType() {
	return osmType;
    }

    /**
     * @return the osmId
     */
    public int getOsmId() {
	return osmId;
    }

    /**
     * @return the lat
     */
    public double getLat() {
	return lat;
    }

    /**
     * @return the lon
     */
    public double getLon() {
	return lon;
    }

    /**
     * @return the category
     */
    public String getCategory() {
	return category;
    }

    /**
     * @return the type
     */
    public String getType() {
	return type;
    }

    /**
     * @return the placeRank
     */
    public int getPlaceRank() {
	return placeRank;
    }

    /**
     * @return the importance
     */
    public double getImportance() {
	return importance;
    }

    /**
     * @return the addresstype
     */
    public String getAddresstype() {
	return addresstype;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @return the display_name
     */
    public String getDisplayName() {
	return displayName;
    }

    /**
     * @return the bbox
     */
    public GeographicBoundingBox getBbox() {
	return bbox;
    }

}
