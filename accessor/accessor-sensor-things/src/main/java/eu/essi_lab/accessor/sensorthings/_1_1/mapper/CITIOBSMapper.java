package eu.essi_lab.accessor.sensorthings._1_1.mapper;

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

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem.Operation;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;
import eu.essi_lab.lib.sensorthings._1_1.client.response.AddressableEntityResult;
import eu.essi_lab.lib.sensorthings._1_1.model.UnitOfMeasurement;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Location;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Sensor;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.ExtensionHandler;

/**
 * @author Fabrizio
 */
public class CITIOBSMapper extends SensorThingsMapper {

    /**
     * 
     */
    private static final String SERVER_URN = "urn:citiobs:";

    /**
     * 
     */
    public static final String SENSOR_THINGS_1_1_CITIOBS_SCHEMA = "http://www.opengis.net/doc/is/sensorthings/1.1_CITIOBS";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SENSOR_THINGS_1_1_CITIOBS_SCHEMA;
    }

    @Override
    protected Datastream downloadStrem(String streamId) throws GSException {

	SensorThingsRequest sensorThingsRequest = createRequest().//
		add(EntityRef.DATASTREAMS, streamId).//
		with(SystemQueryOptions.get()
			.expand(new ExpandOption(//
				ExpandItem.get(EntityRef.THING, EntityRef.LOCATIONS), //
				ExpandItem.get(EntityRef.THING, EntityRef.of("Party")), //
				ExpandItem.get(EntityRef.SENSOR), //
				ExpandItem.get(EntityRef.OBSERVED_PROPERTY))));

	Optional<AddressableEntityResult<Datastream>> entityResponse;

	try {
	    entityResponse = sensorThingsClient.//
		    execute(sensorThingsRequest).//
		    getAddressableEntityResult(Datastream.class);

	    return entityResponse.get().getEntities().get(0);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).equals(e);

	    throw GSException.createException(getClass(), "CITIOBSMapper_DownloadStreamError", e);
	}
    }

    @Override
    protected Thing downloadThing(String thingId) throws GSException {

	SensorThingsRequest sensorThingsRequest = createRequest().//
		add(EntityRef.THINGS, thingId).//
		with(SystemQueryOptions.get()
			.expand(new ExpandOption(//
				ExpandItem.get(EntityRef.LOCATIONS), //
				ExpandItem.get(EntityRef.HISTORICAL_LOCATIONS), //
				ExpandItem.get(EntityRef.of("Party")), //
				// only phenomenonTime is required
				ExpandItem.get(EntityRef.DATASTREAMS, Operation.SELECT, "phenomenonTime"))));

	Optional<AddressableEntityResult<Thing>> entityResponse;

	try {
	    entityResponse = sensorThingsClient.//
		    execute(sensorThingsRequest).//
		    getAddressableEntityResult(Thing.class);

	    return entityResponse.get().getEntities().get(0);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).equals(e);

	    throw GSException.createException(getClass(), "CITIOBSMapper_DownloadStreamError", e);
	}
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

	    // JSONObject metadata = (JSONObject) sensor.getMetadata();
	    //
	    // //
	    // // Sensor code
	    // //
	    // String sensorCode = SERVER_URN;
	    // sensorCode += sensorName.get().trim();
	    //
	    // String methodCode = metadata.optString("methodCode");
	    // if (!methodCode.isEmpty()) {
	    // sensorCode = sensorCode + ":" + methodCode.trim();
	    // addKeyword(keywords, methodCode);
	    // }
	    // instrument.setMDIdentifierTypeCode(sensorCode);
	    //
	    // String methodType = metadata.optString("methodType");
	    // if (!methodType.isEmpty()) {
	    // instrument.setSensorType(methodType.trim());
	    // addKeyword(keywords, methodType);
	    // }
	    //
	    // String methodLink = metadata.optString("methodLink");
	    // addKeyword(keywords, methodLink);
	    //
	    // if (metadata.has("sensorModel")) {
	    //
	    // JSONObject sensorModel = metadata.getJSONObject("sensorModel");
	    //
	    // String sensorModelName = sensorModel.optString("sensorModelName");
	    // String sensorModelURL = sensorModel.optString("sensorModelURL");
	    // String sensorManufacturer = sensorModel.optString("sensorManufacturer");
	    //
	    // addKeyword(keywords, sensorModelName);
	    // addKeyword(keywords, sensorModelURL);
	    // addKeyword(keywords, sensorManufacturer);
	    // }
	}
    }

    /**
     * @param thing
     * @param dataId
     */
    @Override
    protected void addResponsibleParty(Thing thing, Datastream stream, DataIdentification dataId) {

	addResponsibleParty(thing, dataId);

	JSONObject thingParty = thing.getExtensions(EntityRef.of("Party")).get(0).getObject();
	String thingAuthId = thingParty.optString("authId");

	String streamAuthId = null;

	if (stream != null) {

	    JSONObject streamParty = stream.getExtensions(EntityRef.of("Party")).get(0).getObject();
	    streamAuthId = streamParty.optString("authId");
	}

	if (streamAuthId != null && !thingAuthId.equals(thingAuthId)) {

	    addResponsibleParty(stream, dataId);
	}
    }

    /**
     * @param entity
     * @param dataId
     */
    private void addResponsibleParty(Entity entity, DataIdentification dataId) {

	JSONObject party = entity.getExtensions(EntityRef.of("Party")).get(0).getObject();

	String role = party.optString("role");

	String displayName = party.optString("displayName");

	if (!role.isEmpty() || !displayName.isEmpty()) {

	    ResponsibleParty responsibleParty = new ResponsibleParty();

	    if (!displayName.isEmpty()) {

		if (role.equals("individual")) {

		    responsibleParty.setIndividualName(displayName.trim());

		} else {

		    responsibleParty.setOrganisationName(displayName.trim());
		}
	    }

	    responsibleParty.setRoleCode("pointOfContact");

	    dataId.addPointOfContact(responsibleParty);
	}
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

	if (!thing.getLocations().isEmpty()) {

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

	    //
	    //
	    //

	    coreMetadata.getMIMetadata().addMIPlatform(platform);
	}
    }

    /**
     * @param location
     * @param dataId
     * @param keywords
     */
    @Override
    protected void addBoundingBox(Thing thing, DataIdentification dataId, Keywords keywords) {

	if (!thing.getLocations().isEmpty()) {

	    Location location = thing.getLocations().get(0);

	    GeographicBoundingBox boundingBox = null;

	    // should be "application/geo+json"
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
    }

    @Override
    public String getProfileName() {

	return "CITIOBS";
    }

    @Override
    protected String getSupportedProtocol() {

	return NetProtocols.SENSOR_THINGS_1_1_CITIOBS.getCommonURN();
    }
}
