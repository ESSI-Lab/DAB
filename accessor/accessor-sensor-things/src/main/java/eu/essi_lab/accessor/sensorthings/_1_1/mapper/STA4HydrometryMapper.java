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

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.lib.net.protocols.NetProtocols;
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
public class STA4HydrometryMapper extends SensorThingsMapper {

    /**
     * 
     */
    private static final String SERVER_URN = "urn:sta4hydro:";
    /**
     * 
     */
    public static final String SENSOR_THINGS_1_1_STA_4_HIDROMETRY_SCHEMA = "http://www.opengis.net/doc/is/sensorthings/1.1_STA4";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SENSOR_THINGS_1_1_STA_4_HIDROMETRY_SCHEMA;
    }

    /**
     * @param stream
     * @param coreMetadata
     * @param keywords
     */
    @Override
    protected void addInstrument(Datastream stream, CoreMetadata coreMetadata, Keywords keywords) {

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
	    // e.g. millimeters
	    handler.setAttributeUnits(unitName);
	}
	//
	// Attribute units abbreviation
	//
	String unitSymbol = unitOfMeasurement.getSymbol();
	if (!unitSymbol.isEmpty()) {
	    // e.g. mm
	    handler.setAttributeUnitsAbbreviation(unitSymbol);
	}
	
	String unitDefinition= unitOfMeasurement.getDefinition();
	if (!unitDefinition.isEmpty()) {
	    // e.g. https://data.geoscience.fr/ncl/uom/491
	    handler.setAttributeUnitsURI(unitDefinition);
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
    protected void addVerticalExtent(Thing thing, Keywords keywords, DataIdentification dataId) {

    }

    /**
     * @param coreMetadata
     * @param location
     * @param keywords
     * @param dataId
     * @return
     */
    @Override
    protected void addPlatform(Thing thing, CoreMetadata coreMetadata, DataIdentification dataId, Keywords keywords,ExtensionHandler handler) {

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

	//
	// Platform id
	//

	platform.setMDIdentifierCode(thing.getSelfLink().get());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param location
     * @param dataId
     * @param keywords
     */
    @Override
    protected void addBoundingBox(Thing thing, DataIdentification dataId, Keywords keywords) {

	Location location = thing.getLocations().get(0);

	GeographicBoundingBox boundingBox = null;

	// should be "application/vnd.geo+json"
	Optional<String> locationEncodingType = location.getEncodingType();
	locationEncodingType.ifPresent(enc -> addKeyword(keywords, enc));

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

	return "STA 4";
    }

    @Override
    protected String getSupportedProtocol() {

	return NetProtocols.SENSOR_THINGS_1_1_STA_4_HYDRO.getCommonURN();
    }
}
