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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.sensorthings._1_1.SensorThingsMangler;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.sensorthings._1_1.client.SensorThingsClient;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.FluentSensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem.Operation;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;
import eu.essi_lab.lib.sensorthings._1_1.client.response.AddressableEntityResult;
import eu.essi_lab.lib.sensorthings._1_1.model.ObservedArea;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.ObservedProperty;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * @author Fabrizio
 */
public abstract class SensorThingsMapper extends AbstractResourceMapper {

    /**
     * 
     */
    protected SensorThingsClient sensorThingsClient;
    /**
     * 
     */
    protected Boolean quoteIdentifiers;

    /**
     * @return
     */
    public static List<String> getNames() {

	return StreamUtils.iteratorToStream(//
		ServiceLoader.load(IResourceMapper.class).iterator()).//
		filter(m -> m instanceof SensorThingsMapper).//
		map(m -> (SensorThingsMapper) m).//
		map(m -> m.getProfileName()).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public static List<String> getSchemas() {

	return StreamUtils.iteratorToStream(//
		ServiceLoader.load(IResourceMapper.class).iterator()).//
		filter(m -> m instanceof SensorThingsMapper).//
		map(m -> (SensorThingsMapper) m).//
		map(m -> m.getSupportedOriginalMetadataSchema()).//
		collect(Collectors.toList());
    }

    /**
     * @param profileName
     * @return
     */
    public static String getSchema(String profileName) {

	return StreamUtils.iteratorToStream(//
		ServiceLoader.load(IResourceMapper.class).iterator()).//
		filter(m -> m instanceof SensorThingsMapper).//
		map(m -> (SensorThingsMapper) m).//
		filter(m -> m.getProfileName().equals(profileName)).//
		map(m -> m.getSupportedOriginalMetadataSchema()).//
		findFirst().//
		get();
    }

    /**
     * @param selfLink
     * @param id
     * @return
     */
    public static JSONObject creatOriginalMedatata(String selfLink, String id) {

	JSONObject object = new JSONObject();
	object.put("selfLink", selfLink);
	object.put("id", id);

	return object;
    }

    /**
     * @param stream
     * @return
     */
    public static Optional<String[]> mapStreamPhenomenomTime(Datastream stream) {

	Optional<String> phenomenonTime = stream.getPhenomenonTime();
	if (phenomenonTime.isPresent()) {

	    String time = phenomenonTime.get();
	    String beginPosition = time.split("/")[0].replace("000Z", "00Z");
	    String endPosition = time.split("/")[1].replace("000Z", "00Z");

	    return Optional.of(new String[] { beginPosition, endPosition });
	}

	return Optional.empty();
    }

    /**
     * @param originalMetadata
     * @return
     */
    public static String createOriginalIdentifier(String originalMetadata) {

	String link = readSelfLink(originalMetadata);
	String id = null;

	try {
	    id = StringUtils.hashSHA1messageDigest(link);

	} catch (Exception e) {

	    id = link.replace("https:", "");
	    id = link.replace("http:", "");
	    id = link.replace("//", "");
	    id = link.replace("/", "");
	    id = link.replace("'", "");
	    id = link.replace(".", "");
	    id = link.replace("(", "");
	    id = link.replace(")", "");
	    id = StringUtils.encodeUTF8(id);
	}

	return id;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	if (sensorThingsClient == null) {
	    try {
		sensorThingsClient = new SensorThingsClient(new URL(source.getEndpoint()));
	    } catch (MalformedURLException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
		throw GSException.createException(getClass(), "SensorThings_1_1_Mapper_Client_Creation_Error", e);
	    }
	}

	quoteIdentifiers = originalMD.getAdditionalInfo().get("quoteIdentifiers", Boolean.class);

	String entityId = readId(originalMD.getMetadata());

	EntityRef entitySet = originalMD.getAdditionalInfo().get("entitySet", EntityRef.class);

	Boolean discardStations = originalMD.getAdditionalInfo().get("discardStation", Boolean.class);

	GSResource resource = null;

	if (entitySet == EntityRef.THINGS) {

	    resource = new DatasetCollection();
	    resource.setSource(source);
	    resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("series");
	    resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	    mapCollection(entityId, (DatasetCollection) resource, discardStations);

	} else {

	    resource = new Dataset();
	    resource.setSource(source);
	    resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	    resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    String parentId = originalMD.getAdditionalInfo().get("parentId", String.class);
	    resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier(parentId);

	    mapDataset(entityId, (Dataset) resource, discardStations);
	}

	return resource;
    }

    /**
     * @param stream
     * @param coreMetadata
     * @param keywords
     */
    protected abstract void addInstrument(Datastream stream, CoreMetadata coreMetadata, Keywords keywords);

    /**
     * @param stream
     * @param coreMetadata
     * @param keywords
     */
    protected void addCoverageDescription(Datastream stream, CoreMetadata coreMetadata, Keywords keywords) {

	Optional<ObservedProperty> optObservedProperty = stream.getObservedProperty();

	CoverageDescription coverageDescription = new CoverageDescription();

	if (optObservedProperty.isPresent()) {

	    ObservedProperty observedProperty = optObservedProperty.get();

	    Optional<String> name = observedProperty.getName();
	    if (name.isPresent()) {

		addKeyword(keywords, name.get().trim());
		coverageDescription.setAttributeTitle(normalize(name.get()));
	    }

	    Optional<String> description = observedProperty.getDescription();
	    if (description.isPresent()) {

		addKeyword(keywords, description.get().trim());
		coverageDescription.setAttributeDescription(normalize(description.get()));
	    }

	    String definition = observedProperty.getObject().optString("definition");
	    if (!definition.isEmpty()) {

		addKeyword(keywords, definition);
		coverageDescription.setAttributeIdentifier(normalize(definition));
	    }

	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
	}
    }

    /**
     * @param thing
     * @param dataId
     */
    protected abstract void addResponsibleParty(Thing thing, Datastream stream, DataIdentification dataId);

    /**
     * @param handler
     * @param stream
     */
    protected abstract void addExtensions(Datastream stream, ExtensionHandler handler);

    /**
     * @param thing
     * @param keywords
     * @param dataId
     */
    protected abstract void addVerticalExtent(Thing thing, Keywords keywords, DataIdentification dataId);

    /**
     * @param coreMetadata
     * @param location
     * @param keywords
     * @param dataId
     * @return
     */
    protected abstract void addPlatform(Thing thing, CoreMetadata coreMetadata, DataIdentification dataId, Keywords keywords,
	    ExtensionHandler handler);

    /**
     * @param location
     * @param dataId
     * @param keywords
     */
    protected abstract void addBoundingBox(Thing thing, DataIdentification dataId, Keywords keywords);

    /**
     * @return
     */
    public abstract String getProfileName();

    /**
     * @return
     */
    protected abstract String getSupportedProtocol();

    /**
     * @param stream
     * @param coreMetadata
     * @param sourceUrl
     */
    protected void addDistributionInfo(Datastream stream, CoreMetadata coreMetadata, String sourceUrl) {

	SensorThingsMangler mangler = new SensorThingsMangler();
	mangler.setStreamIdentifer(stream.getIdentifier().get());
	mangler.setQuoteIdentifiers(quoteIdentifiers.toString());

	coreMetadata.addDistributionOnlineResource(//
		mangler.getMangling(), //
		sourceUrl, //
		getSupportedProtocol(), //
		"download");
    }

    /**
     * @param optResultTime
     * @param dataset
     */
    protected void handleResultTime(Optional<String> optResultTime, Dataset dataset) {

	if (optResultTime.isPresent() && !optResultTime.get().isEmpty() && !optResultTime.get().equals("null")) {

	    // String resultTime = optResultTime.get();
	}
    }

    /**
     * @param optObservedArea
     * @param dataset
     */
    protected void handleObservedArea(Optional<ObservedArea> optObservedArea, Dataset dataset) {

	if (optObservedArea.isPresent()) {

	    // ObservedArea observedArea = optObservedArea.get();
	}
    }

    /**
     * @param streamId
     * @param dataset
     * @throws GSException
     */
    protected void mapDataset(String streamId, Dataset dataset, Boolean discardStations) throws GSException {

	Datastream stream = downloadStrem(streamId);
	// if (discardStations) {
	// stream = downloadStrem(streamId);
	// } else {
	// try {
	// String optString = SensorThingsConnector.getFakeStream();
	// if (optString != null) {
	// JSONObject jsonEntity = new JSONObject(optString);
	// stream = new Datastream(jsonEntity);
	// }
	// } catch (Exception e) {
	//
	// GSLoggerFactory.getLogger(getClass()).equals(e);
	//
	// throw GSException.createException(getClass(), getDownloadStreamErrorMessage(), e);
	// }
	//
	// }

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	// should be "OM_Measurement"
	String observationType = stream.getObservationType();
	if (observationType != null && !observationType.isEmpty()) {

	    keywords.addKeyword(observationType);
	}

	//
	// File Identifier
	//
	setFileIDentifier(stream, dataset);

	//
	// Title
	//

	setTitle(stream, coreMetadata);

	//
	// Abstract
	//
	setAbstract(stream, coreMetadata);

	//
	// Temporal extent (e.g.: 2023-11-02T21:47:21Z/2023-11-15T00:36:58Z )
	//

	addTemporalExtent(stream, dataId);

	Thing thing = stream.getThing().get();

	//
	// Platform, Vertical extent and keywords (from the expanded Thing)
	//

	addPlatform(thing, coreMetadata, dataId, keywords, dataset.getExtensionHandler());

	//
	// Responsible party
	//

	addResponsibleParty(thing, stream, dataId);

	//
	// Vertical extent
	//

	addVerticalExtent(thing, keywords, dataId);

	//
	// Spatial extent (from the expanded Thing)
	//

	addBoundingBox(thing, dataId, keywords);

	HashMap<String, String> tags = thing.getTags();

	String mail = tags.getOrDefault("email", tags.get("mail"));
	String organizationLabel = tags.getOrDefault("organization_label", tags.get("organization"));
	String role = tags.getOrDefault("role", tags.get("project_role"));

	if (organizationLabel != null) {

	    ResponsibleParty responsibleParty = new ResponsibleParty();

	    if (!organizationLabel.isEmpty()) {

		responsibleParty.setOrganisationName(organizationLabel.trim());

	    }

	    responsibleParty.setRoleCode(role);

	    if (mail != null) {
		Contact contact = new Contact();
		Address address = new Address();
		address.addElectronicMailAddress(mail);
		contact.setAddress(address);
		responsibleParty.setContactInfo(contact);
	    }
	    dataId.addPointOfContact(responsibleParty);

	}

	Optional<JSONObject> properties = thing.getProperties();

	if (properties.isPresent()) {
	    JSONObject prop = properties.get();
	    JSONArray relatedArray = prop.optJSONArray("relatedTo");
	    if (relatedArray != null) {
		for (int i = 0; i < relatedArray.length(); i++) {
		    JSONObject relatedProperty = relatedArray.optJSONObject(i);
		    if (relatedProperty != null) {
			String href = relatedProperty.optString("href");
			String title = relatedProperty.optString("title");
			if (href != null && title != null) {
			    switch (title.toLowerCase()) {
			    case "watershed":
			    case "bacino":
			    case "basin":
				dataset.getExtensionHandler().setRiverBasin(href);
				break;
			    case "watercourse":
				dataset.getExtensionHandler().setRiver(href);
				break;
			    default:
				GSLoggerFactory.getLogger(getClass()).warn("Unmapped property: {}", title);
				break;
			    }
			}
		    }
		}
	    }
	}

	//
	// MI_Instument
	//
	addInstrument(stream, coreMetadata, keywords);

	//
	// Coverage description
	//

	addCoverageDescription(stream, coreMetadata, keywords);

	//
	// Distribution info
	//
	addDistributionInfo(stream, coreMetadata, dataset.getSource().getEndpoint());

	//
	// Extensions
	//
	addExtensions(stream, dataset.getExtensionHandler());

	//
	// Result time?
	//
	handleResultTime(stream.getResultTime(), dataset);

	//
	// Observed area?
	//
	Optional<ObservedArea> optObservedArea = stream.getObservedArea();
	if (optObservedArea.isPresent()) {

	    // ObservedArea observedArea = optObservedArea.get();
	}

	if (!discardStations) {
	    TemporalExtent temporalExtent = dataId.getTemporalExtent();
	    if (temporalExtent != null) {
		String start = temporalExtent.getBeginPosition();
		if (start == null || start.isEmpty()) {
		    dataId.addTemporalExtent("2024-01-01T00:00:00Z", "2024-11-21T00:00:00Z");
		}
	    } else {
		dataId.addTemporalExtent("2024-01-01T00:00:00Z", "2024-11-21T00:00:00Z");
	    }
	}
    }

    /**
     * @param thingId
     * @param collection
     * @throws GSException
     */
    protected void mapCollection(String thingId, DatasetCollection collection, Boolean discardStations) throws GSException {

	Thing thing = downloadThing(thingId);

	CoreMetadata coreMetadata = collection.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	//
	// File Identifier
	//
	setFileIDentifier(thing, collection);

	//
	// Title
	//
	setTitle(thing, coreMetadata);

	//
	// Abstract
	//
	setAbstract(thing, coreMetadata);

	//
	// Platform, Vertical extent and keywords
	//

	addPlatform(thing, coreMetadata, dataId, keywords, collection.getExtensionHandler());

	//
	// Responsible party
	//

	addResponsibleParty(thing, null, dataId);

	//
	// Vertical extent
	//

	addVerticalExtent(thing, keywords, dataId);

	//
	// Spatial extent
	//

	addBoundingBox(thing, dataId, keywords);

	//
	// Temporal extent (from linked streams)
	//

	List<Datastream> datastreams = thing.getDatastreams();

	addTemporalExtent(datastreams, dataId);

	if (!discardStations) {
	    TemporalExtent temporalExtent = dataId.getTemporalExtent();
	    if (temporalExtent != null) {
		String start = temporalExtent.getBeginPosition();
		if (start == null || start.isEmpty()) {
		    dataId.addTemporalExtent("2024-01-01T00:00:00Z", "2024-11-21T00:00:00Z");
		}
	    } else {
		dataId.addTemporalExtent("2024-01-01T00:00:00Z", "2024-11-21T00:00:00Z");
	    }
	}
    }

    /**
     * @param streamId
     * @return
     * @throws GSException
     */
    protected Datastream downloadStrem(String streamId) throws GSException {

	SensorThingsRequest sensorThingsRequest = createRequest().//
		add(EntityRef.DATASTREAMS, streamId).//
		with(SystemQueryOptions.get().expand(new ExpandOption(//
			ExpandItem.get(EntityRef.THING, EntityRef.LOCATIONS), //
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

	    throw GSException.createException(getClass(), getDownloadStreamErrorMessage(), e);
	}
    }

    /**
     * @param thingId
     * @return
     * @throws GSException
     */
    protected Thing downloadThing(String thingId) throws GSException {

	SensorThingsRequest sensorThingsRequest = createRequest().//
		add(EntityRef.THINGS, thingId).//
		with(SystemQueryOptions.get().expand(new ExpandOption(//
			ExpandItem.get(EntityRef.LOCATIONS), //
			ExpandItem.get(EntityRef.HISTORICAL_LOCATIONS), //
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

	    throw GSException.createException(getClass(), getDownloadThingErrorMessage(), e);
	}
    }

    /**
     * @return
     */
    protected String getDownloadStreamErrorMessage() {

	return getClass().getSimpleName() + "_DownloadStreamError";
    }

    /**
     * @return
     */

    protected String getDownloadThingErrorMessage() {

	return getClass().getSimpleName() + "_DownloadThingError";
    }

    /**
     * @param stream
     * @param dataset
     */
    protected void setFileIDentifier(Datastream stream, Dataset dataset) {

	dataset.setPublicId(stream.getIdentifier().get());
    }

    /**
     * @param thing
     * @param collection
     */
    protected void setFileIDentifier(Thing thing, DatasetCollection collection) {

	collection.setPublicId(thing.getIdentifier().get());
    }

    /**
     * @param stream
     * @param coreMetadata
     */
    protected void setTitle(Datastream stream, CoreMetadata coreMetadata) {

	Optional<String> streamName = stream.getName();
	coreMetadata.setTitle(streamName.isPresent() ? streamName.get().replace("'", " ") : stream.getIdentifier().get());
    }

    /**
     * @param thing
     * @param coreMetadata
     */
    protected void setTitle(Thing thing, CoreMetadata coreMetadata) {

	Optional<String> thingName = thing.getName();
	coreMetadata.setTitle(thingName.isPresent() ? thingName.get().replace("'", " ") : thing.getIdentifier().get());
    }

    /**
     * @param stream
     * @param coreMetadata
     */
    protected void setAbstract(Datastream stream, CoreMetadata coreMetadata) {

	Optional<String> desc = stream.getDescription();
	if (desc.isPresent()) {

	    coreMetadata.setAbstract(desc.get().replace("'", " "));
	}
    }

    /**
     * @param thing
     * @param coreMetadata
     */
    protected void setAbstract(Thing thing, CoreMetadata coreMetadata) {

	Optional<String> desc = thing.getDescription();
	if (desc.isPresent()) {

	    coreMetadata.setAbstract(desc.get().replace("'", " "));
	}
    }

    /**
     * @param stream
     * @param dataId
     */
    protected void addTemporalExtent(Datastream stream, DataIdentification dataId) {

	Optional<String[]> phenomenomTime = SensorThingsMapper.mapStreamPhenomenomTime(stream);
	if (phenomenomTime.isPresent()) {

	    dataId.addTemporalExtent(phenomenomTime.get()[0], phenomenomTime.get()[1]);
	}
    }

    /**
     * @param streams
     * @param dataId
     */
    protected void addTemporalExtent(List<Datastream> datastreams, DataIdentification dataId) {

	Optional<String> beginPosition = datastreams.//
		stream().//
		filter(s -> s.getPhenomenonTime().isPresent()).//
		map(s -> s.getPhenomenonTime().get().split("/")[0]).//
		sorted((t1, t2) -> t1.compareTo(t2)).//
		findFirst();

	Optional<String> endPosition = datastreams.//
		stream().//
		filter(s -> s.getPhenomenonTime().isPresent()).//
		map(s -> s.getPhenomenonTime().get().split("/")[1]).//
		sorted((t1, t2) -> t2.compareTo(t1)).//
		findFirst();

	if (beginPosition.isPresent() && endPosition.isPresent()) {

	    dataId.addTemporalExtent(beginPosition.get(), endPosition.get());
	}
    }

    /**
     * @param locationName
     * @param coordinates
     * @return
     */
    protected GeographicBoundingBox createBoundingBox(Optional<String> locationName, JSONArray coordinates) {

	GeographicBoundingBox boundingBox = new GeographicBoundingBox();

	if (locationName.isPresent()) {
	    boundingBox.setId(locationName.get());
	}

	boundingBox.setBigDecimalNorth(coordinates.getBigDecimal(1));
	boundingBox.setBigDecimalSouth(coordinates.getBigDecimal(1));

	boundingBox.setBigDecimalWest(coordinates.getBigDecimal(0));
	boundingBox.setBigDecimalEast(coordinates.getBigDecimal(0));

	return boundingBox;
    }

    /**
     * @param keywords
     * @param keyword
     */
    protected void addKeyword(Keywords keywords, String keyword) {

	if (keyword != null && !keyword.isEmpty()) {

	    keywords.addKeyword(keyword.trim());
	}
    }

    /**
     * @return
     */
    protected FluentSensorThingsRequest createRequest() {

	return FluentSensorThingsRequest.//
		get().//
		quoteIdentifiers(quoteIdentifiers);

    }

    /**
     * @param originalMetadata
     * @return
     */
    protected static String readSelfLink(String originalMetadata) {

	JSONObject object = new JSONObject(originalMetadata);
	return object.getString("selfLink");
    }

    /**
     * @param originalMetadata
     * @return
     */
    protected static String readId(String originalMetadata) {

	JSONObject object = new JSONObject(originalMetadata);
	return object.getString("id");
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	return createOriginalIdentifier(resource.getOriginalMetadata().getMetadata());
    }

    /**
     * @param value
     * @return
     */
    protected String normalize(String value) {

	return value.trim();
    }
}
