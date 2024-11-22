package eu.essi_lab.accessor.sensorthings._1_1;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import eu.essi_lab.accessor.sensorthings._1_1.mapper.HydroServer2Mapper;
import eu.essi_lab.accessor.sensorthings._1_1.mapper.SensorThingsMapper;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.sensorthings._1_1.client.SensorThingsClient;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.FluentSensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem.Operation;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SelectOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;
import eu.essi_lab.lib.sensorthings._1_1.client.response.AddressableEntityResult;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Observation;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class SensorThingsConnector extends HarvestedQueryConnector<SensorThingsConnectorSetting> {

    private int targetThingscount;
    private int thingsCount;
    private int streamsCount;
    private int observationsCount;
    private int totalMappingRecords;// things + streams
    private int discardedThings; // things without streams
    private int discardedStreams; // streams without observations

    /**
     * 
     */
    public static final String TYPE = "SensorThingsConnector_1_1";

    /**
     * 
     */
    private static final int DEFAULT_TOP_VALUE = 100;

    /**
     * 
     */
    private SensorThingsClient client;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Creating SensorThingsClient STARTED");

	if (client == null) {
	    try {
		client = new SensorThingsClient(new URL(getSourceURL()));
	    } catch (MalformedURLException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
		throw GSException.createException(getClass(), "SensorThings_1_1_Connector_Client_Creation_Error", e);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating SensorThingsClient ENDED");

	//
	//
	//

	String schema = getSetting().getProfileSchema();
	GSLoggerFactory.getLogger(getClass()).debug("Selected mapping schema: {}", schema);
	
	Boolean discardStation = isDiscardStationsWithNoData();

	//
	//
	//

	SystemQueryOptions options = createThingsOptions(schema);

	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    options = options.skip(Integer.valueOf(resumptionToken));
	}

	//
	//
	//

	boolean quoteIdentifiers = getSetting().isQuoteIdentifiersSet();
	GSLoggerFactory.getLogger(getClass()).debug("Set quote identifiers:{}", quoteIdentifiers);

	SensorThingsRequest thingsRequest = FluentSensorThingsRequest.//
		get().//
		quoteIdentifiers(quoteIdentifiers).//
		add(EntityRef.THINGS).//
		with(options);

	ListRecordsResponse<OriginalMetadata> listRecordsResponse = new ListRecordsResponse<>();

	try {

	    AddressableEntityResult<Thing> thingsResponse = client.//
		    execute(thingsRequest).//
		    getAddressableEntityResult(Thing.class).//
		    get();

	    //
	    //
	    //

	    targetThingscount = thingsResponse.getCount().get();
	    GSLoggerFactory.getLogger(getClass()).debug("Number of processed things: [{}/{}]", thingsCount, targetThingscount);

	    //
	    //
	    //

	    List<Thing> things = thingsResponse.getEntities();

	    for (Thing thing : things) {

		if (checkMaxRecords()) {

		    break;
		}

		List<Datastream> datastreams = thing.getDatastreams();

		GSLoggerFactory.getLogger(getClass()).debug("Number of Datastreams of Thing({}):{}", //
			thing.getIdentifier().get(), datastreams.size());

		if (!datastreams.isEmpty()) { // discards empty things

		    // true if at least one stream with Observations is found
		    boolean validStreams = false;

		    for (Datastream stream : datastreams) {

			if (checkMaxRecords()) {

			    break;
			}

			//
			// expanded entities miss the navigation
			// properties (unless specified in the expanded call),
			// so the call to the method stream.getObservations() returns an empty list.
			// to get the number of related Observations, we use the client
			//
			SensorThingsRequest observationsRequest = FluentSensorThingsRequest.//
				get().//
				quoteIdentifiers(quoteIdentifiers).//
				add(EntityRef.DATASTREAMS, stream.getIdentifier().get()).//
				add(EntityRef.OBSERVATIONS).//
				with(SystemQueryOptions.//
					get().//
					count().// we only need the count
					top(0));// no entities need

			try {

			    AddressableEntityResult<Observation> observationsResponse = client.//
				    execute(observationsRequest).//
				    getAddressableEntityResult(Observation.class).//
				    get();

			    observationsCount += observationsResponse.getCount().get();

			    GSLoggerFactory.getLogger(getClass()).debug("Number of Observations of Datastream({}):{}", //
				    stream.getIdentifier().get(), observationsResponse.getCount().get());

			    Optional<String> phenomenonTime = stream.getPhenomenonTime().filter(t -> !t.isEmpty());

			    //
			    // discards streams without observations and without time
			    //
			    if (observationsResponse.getCount().get() > 0 && phenomenonTime.isPresent()) {

				validStreams = true;

				//
				// adds the Datastreams (mapped as datasets)
				//

				addOriginalMetadata(//
					stream, //
					listRecordsResponse, //
					schema, //
					EntityRef.DATASTREAMS, //
					thing);

				streamsCount++;
				totalMappingRecords++;

			    } else {
				if(!discardStation) {
				    addOriginalMetadata(stream, listRecordsResponse, schema, EntityRef.DATASTREAMS, thing);
				}
				discardedStreams++;
			    }
			} catch (Exception e) {

			    GSLoggerFactory.getLogger(getClass()).error(e);
			    throw GSException.createException(getClass(), "SensorThings_1_1_Connector_Client_Datastream/Observations_Error",
				    e);
			}
		    }

		    if (validStreams) {

			//
			// adds the Thing (mapped as collection)
			//

			addOriginalMetadata(thing, listRecordsResponse, schema, EntityRef.THINGS, null);
			thingsCount++;
			totalMappingRecords++;
		    } else {
			if(!discardStation) {
			    addOriginalMetadata(thing, listRecordsResponse, schema, EntityRef.THINGS, null);
			}
		    }

		} else {
		    if(!discardStation) {
			addOriginalMetadata(thing, listRecordsResponse, schema, EntityRef.THINGS, null);
		    }
		    discardedThings++;
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Discarded Things: {}", discardedThings);
	    GSLoggerFactory.getLogger(getClass()).debug("Discarded Datastreams: {}", discardedStreams);
	    GSLoggerFactory.getLogger(getClass()).debug("Valid Things: {}", thingsCount);
	    GSLoggerFactory.getLogger(getClass()).debug("Valid Datastreams: {}", streamsCount);
	    GSLoggerFactory.getLogger(getClass()).debug("Observations: {}", observationsCount);
	    GSLoggerFactory.getLogger(getClass()).debug("Mapped records: {}", totalMappingRecords);

	    //
	    //
	    //

	    if (checkMaxRecords()) {

		GSLoggerFactory.getLogger(getClass()).debug("Limit of {} entities reached (or exceeded), ending process",
			getSetting().getMaxRecords().get());

	    } else {

		thingsResponse.getNextLink().ifPresent(link -> {

		    KeyValueParser parser = new KeyValueParser(link);
		    String skip = parser.getValue("$skip");
		    if (skip != null) {

			GSLoggerFactory.getLogger(getClass()).debug("Setting resumption token: {}", skip);

			listRecordsResponse.setResumptionToken(skip);
		    }
		});
	    }

	    if (listRecordsResponse.getResumptionToken() == null && request.getStatus().isPresent()) {

		request.getStatus().get().addInfoMessage("----");
		request.getStatus().get().addInfoMessage("Empty Things: " + discardedThings);
		request.getStatus().get().addInfoMessage("Empty Datastreams: " + discardedStreams);
		request.getStatus().get().addInfoMessage("Valid Things: " + thingsCount);
		request.getStatus().get().addInfoMessage("Valid Datastreams: " + streamsCount);
		request.getStatus().get().addInfoMessage("Observations: " + observationsCount);
		request.getStatus().get().addInfoMessage("Mapped records: " + totalMappingRecords);
		request.getStatus().get().addInfoMessage("---");
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw GSException.createException(getClass(), "SensorThings_1_1_Connector_Client_Execute_Error", e);
	}

	return listRecordsResponse;
    }

    /**
     * @param schema
     * @return
     */
    private SystemQueryOptions createThingsOptions(String schema) {

	if (schema.equals(HydroServer2Mapper.SENSOR_THINGS_1_1_HYDRO_SERVER_2_SCHEMA)) {

	    return SystemQueryOptions.//
		    get().//
		    count().//
		    top(DEFAULT_TOP_VALUE).//
		    expand(new ExpandOption(//
			    EntityRef.DATASTREAMS));
	}

	return SystemQueryOptions.//
		get().//
		count().//
		top(DEFAULT_TOP_VALUE).//
		// select only things id and selfLink
		select(new SelectOption("@iot.selfLink,id")).
		// expanded streams and selecting only some properties
		expand(new ExpandOption(//
			EntityRef.DATASTREAMS, //
			Operation.SELECT, //
			"@iot.selfLink,id,phenomenonTime"));
    }

    /**
     * @return
     */
    private boolean checkMaxRecords() {

	return getSetting().getMaxRecords().isPresent() && totalMappingRecords >= getSetting().getMaxRecords().get();
    }

    /**
     * @param entity
     * @param response
     * @param schemeURI
     * @param addrEntity
     * @param parentThing
     */
    private void addOriginalMetadata(//
	    Entity entity, //
	    ListRecordsResponse<OriginalMetadata> response, //
	    String schemeURI, //
	    EntityRef addrEntity, //
	    Thing parentThing) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setMetadata(//
		SensorThingsMapper.creatOriginalMedatata(//
			entity.getSelfLink().get(), //
			entity.getIdentifier().get()).toString(3));

	originalMetadata.setSchemeURI(schemeURI);

	GSPropertyHandler handler = GSPropertyHandler.of(new GSProperty<EntityRef>("entitySet", addrEntity));

	handler.add(//
		new GSProperty<Boolean>("quoteIdentifiers", getSetting().isQuoteIdentifiersSet()));

	if (parentThing != null) {

	    handler.add(//
		    new GSProperty<String>("parentId", SensorThingsMapper.createOriginalIdentifier(SensorThingsMapper.creatOriginalMedatata(//
			    parentThing.getSelfLink().get(), //
			    parentThing.getIdentifier().get()).toString(3))));
	}
	boolean discardStation = isDiscardStationsWithNoData();
	handler.add(new GSProperty<Boolean>("discardStation", discardStation));


	originalMetadata.setAdditionalInfo(handler);

	response.addRecord(originalMetadata);
    }

    @Override
    public boolean supports(GSSource source) {

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	Optional<String> optional = downloader.downloadOptionalString(source.getEndpoint());
	if (optional.isPresent()) {
	    String response = optional.get();
	    try {
		JSONObject objectResponse = new JSONObject(response);
		return objectResponse.has("value") && objectResponse.has("serverSettings");
	    } catch (Exception ex) {
		return false;
	    }
	}

	return false;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return SensorThingsMapper.getSchemas();
    }

    @Override
    protected SensorThingsConnectorSetting initSetting() {

	return new SensorThingsConnectorSetting();
    }
    
    /**
     * @return
     */

    public boolean isDiscardStationsWithNoData() {
	return getSetting().isDiscardStationsWithNoData();
    }


    @Override
    public String getType() {

	return TYPE;
    }
}
