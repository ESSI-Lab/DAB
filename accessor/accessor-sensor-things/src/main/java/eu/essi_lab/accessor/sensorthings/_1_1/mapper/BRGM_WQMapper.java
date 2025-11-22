package eu.essi_lab.accessor.sensorthings._1_1.mapper;

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

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.sensorthings._1_1.model.UnitOfMeasurement;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Location;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Sensor;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.ExtensionHandler;

/**
 * @author Fabrizio
 */
public class BRGM_WQMapper extends SensorThingsMapper {

    /**
    * 
    */
    public static final String SENSOR_THINGS_1_0_BRGM_WQ_SCHEMA = "http://www.opengis.net/doc/is/sensorthings/1.0_BRGM_WQ";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SENSOR_THINGS_1_0_BRGM_WQ_SCHEMA;
    }

    /**
     * @param stream
     * @param coreMetadata
     * @param keywords
     */
    @Override
    protected void addInstrument(Datastream stream, CoreMetadata coreMetadata, KeywordsCollector keywords) {

	MIInstrument instrument = null;
	Optional<Sensor> optSensor = stream.getSensor();

	if (optSensor.isPresent()) {

	    instrument = new MIInstrument();
	    coreMetadata.getMIMetadata().addMIInstrument(instrument);

	    Sensor sensor = optSensor.get();

	    Optional<String> sensorName = sensor.getName();
	    instrument.setTitle(sensorName.get().trim());

	    Optional<String> sensorDesc = sensor.getDescription();
	    instrument.setDescription(sensorDesc.get().trim());

	    // e.g: "http://id.eaufrance.fr/nsa/519#2"
	    // unusable
	    // JSONObject metadata = (JSONObject) sensor.getMetadata();
	}
    }

    /**
     * @param thing
     * @param dataId
     */
    @Override
    protected void addResponsibleParty(Thing thing, Datastream stream, DataIdentification dataId) {

    }

    /**
     * @param handler
     * @param stream
     */
    @Override
    protected void addExtensions(Datastream stream, ExtensionHandler handler) {

	UnitOfMeasurement unitOfMeasurement = stream.getUnitOfMeasurement();

	//
	// Attribute units
	//
	String unitName = unitOfMeasurement.getName();
	if (!unitName.isEmpty()) {
	    handler.setAttributeUnits(unitName);
	}
	// String unitDef = unitOfMeasurement.getDefinition();

	//
	// Attribute units abbreviation
	//
	String unitSymbol = unitOfMeasurement.getSymbol();
	if (!unitSymbol.isEmpty()) {
	    handler.setAttributeUnitsAbbreviation(unitSymbol);
	}

	// Example:
	// {"relatedTo.FeaturesOfInterest@iot.id":35}
	// https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/FeaturesOfInterest(35)
	// unusable
	// JSONObject properties = stream.getProperties().get();
    }

    /**
     * @param thing
     * @param keywords
     * @param dataId
     */
    @Override
    protected void addVerticalExtent(Thing thing, KeywordsCollector keywords, DataIdentification dataId) {

	if (thing.getProperties().isPresent()) {

	    if (thing.getProperties().get().has("altitude")) {

		JSONObject altitude = thing.getProperties().get().getJSONObject("altitude");
		double elevation = altitude.optInt("height", Integer.MAX_VALUE);

		//
		// Vertical extent
		//

		VerticalExtent verticalExtent = new VerticalExtent();

		if (elevation != Integer.MAX_VALUE) {

		    verticalExtent.setMinimumValue(elevation);
		    verticalExtent.setMaximumValue(elevation);

		    // String elevationDatum = properties.optString("srs");
		    // if (!elevationDatum.isEmpty()) {
		    //
		    // VerticalCRS verticalCRS = new VerticalCRS();
		    // verticalCRS.setId(elevationDatum);
		    //
		    // verticalExtent.setVerticalCRS(verticalCRS);
		    // }

		    dataId.addVerticalExtent(verticalExtent);
		}
	    }
	}
    }

    /**
     * @param coreMetadata
     * @param location
     * @param keywords
     * @param dataId
     * @return
     */
    @Override
    protected void addPlatform(Thing thing, CoreMetadata coreMetadata, DataIdentification dataId, KeywordsCollector keywords,
	    ExtensionHandler handler) {

	Location location = thing.getLocations().get(0);

	MIPlatform platform = new MIPlatform();

	//
	// Platform Title
	//

	if (location.getName().isPresent()) {

	    String locName = location.getName().get();
	    locName = normalize(locName);

	    Citation citation = new Citation();
	    citation.setTitle(locName);
	    platform.setCitation(citation);
	}

	//
	// Platform description
	//

	if (location.getDescription().isPresent()) {

	    String locDesc = location.getDescription().get();
	    locDesc = normalize(locDesc);

	    platform.setDescription(locDesc);
	}

	if (thing.getProperties().isPresent()) {

	    if (thing.getProperties().get().has("network")) {

		JSONArray network = thing.getProperties().get().getJSONArray("network");
		network.forEach(n -> keywords.addKeyword(((JSONObject) n).getString("libelle"), "network"));
	    }
	}

	//
	// Platform id
	//

	platform.setMDIdentifierCode(thing.getSelfLink().get());
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	//
	//
	//

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param location
     * @param dataId
     * @param keywords
     */
    @Override
    protected void addBoundingBox(Thing thing, DataIdentification dataId, KeywordsCollector keywords) {

	Location location = thing.getLocations().get(0);

	GeographicBoundingBox boundingBox = null;

	if (location.getLocation().has("coordinates")) {

	    JSONArray coordinates = location.getLocation().getJSONArray("coordinates");

	    boundingBox = createBoundingBox(//
		    location.getName(), //
		    coordinates);

	    dataId.addGeographicBoundingBox(boundingBox);
	}
    }

    @Override
    public String getProfileName() {

	return "BRGM - WQ";
    }

    @Override
    protected String getSupportedProtocol() {

	return NetProtocolWrapper.SENSOR_THINGS_1_0_BRGM_WQ.getCommonURN();
    }
}
