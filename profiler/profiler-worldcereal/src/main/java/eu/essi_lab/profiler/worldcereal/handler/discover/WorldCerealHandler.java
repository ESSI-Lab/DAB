package eu.essi_lab.profiler.worldcereal.handler.discover;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationException;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.parser.ParentIdBondHandler;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.discover.BondReducer;
import eu.essi_lab.request.executor.discover.QueryInitializer;

public class WorldCerealHandler extends DiscoveryHandler<String> {

	private static final String WORLDCEREAL_GRANULES_NO_PARENT_ERR_ID = "WORLDCEREAL_GRANULES_NO_PARENT_ERR_ID";
	private static final String WORLDCEREAL_GET_ITEMS_ERROR = "WORLDCEREAL_GET_ITEMS_ERROR";
	public final static String WORLDCEREAL_BASE_URL = "https://ewoc-rdm-api.iiasa.ac.at/";
	public final static String AGROSTAC_BASE_URL = "https://agrostac-test.containers.wur.nl/agrostac/";

	private int DEFAULT_MAX_SIZE = 10000;

	private int DEFAULT_STEP_SIZE = 1000;

	// by default it waits five second
	private int waitTime = 5000;

	public WorldCerealHandler() {
		super();
	}

	@Override
	public Response handleMessageRequest(DiscoveryMessage message) throws GSException {

		String rid = message.getWebRequest().getRequestId();

		// Optional<WebRequest> owr = Optional.ofNullable(message.getWebRequest());
		//
		// GSLoggerFactory.getLogger(getClass()).info("[2/2] Message authorization check STARTED");
		// PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MESSAGE_AUTHORIZATION, rid,
		// owr);
		//
		// boolean authorized = getExecutor().isAuthorized(message);
		//
		// pl.logPerformance(GSLoggerFactory.getLogger(getClass()));
		// GSLoggerFactory.getLogger(getClass()).info("[2/2] Message authorization check ENDED");
		//
		// GSLoggerFactory.getLogger(getClass()).info("Message authorization {}", (authorized ? "approved" : "denied"));
		//
		// if (!authorized) {
		//
		// return handleNotAuthorizedRequest((DiscoveryMessage) message);
		// }

		return handleWorldCerealAPIDiscovery(message, rid);
	}

	private Response handleWorldCerealAPIDiscovery(DiscoveryMessage message, String rid) throws GSException {

		ValidationMessage ret = new ValidationMessage();
		ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

		QueryInitializer queryInitializer = new QueryInitializer();
		queryInitializer.initializeQuery(message);

		GSLoggerFactory.getLogger(getClass()).info("Handling WorldCereal API discovery query STARTED");

		BondReducer bondReducer = new BondReducer();
		Bond normalizedBond = message.getNormalizedBond();

		Bond reducedBond = null;
		boolean isWordCereal;
		try {
			reducedBond = bondReducer.getReducedBond(normalizedBond, "worldcereal");
		} catch (Exception e) {
			// try agrostac
			reducedBond = bondReducer.getReducedBond(normalizedBond, "agrostac");
			//isWordCereal = false;
		}

		ReducedDiscoveryMessage reducedMessage = new ReducedDiscoveryMessage(message, reducedBond);

		String parentid = getParentId(reducedMessage);

		ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
		IDiscoveryExecutor executor = loader.iterator().next();
		DiscoveryMessage discMessage = new DiscoveryMessage();
		discMessage.setRequestId(rid);
		discMessage.setUserBond(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, parentid));

		discMessage.setQueryRegistrationEnabled(false);

		discMessage.setSources(ConfigurationWrapper.getAllSources());

		Page page = new Page(1, 1);
		discMessage.setPage(page);

		discMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
		discMessage.getResourceSelector().setSubset(ResourceSubset.FULL);

		discMessage.setSources(ConfigurationWrapper.getMixedSources());
		discMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

		GSLoggerFactory.getLogger(getClass()).info("Resource discovery STARTED");

		Map<String, String> map = new LinkedHashMap<String, String>();
		List<JSONObject> listObj = new ArrayList<JSONObject>();
		JSONObject errorDuringExecution = null;

		String uniqueJSONName = "";

		String metadataString = null;

		ResultSet<GSResource> resultSet = executor.retrieve(discMessage);

		if (resultSet.getResultsList().isEmpty()) {

			Optional<String> queryString = message.getWebRequest().getFormData();
			String error = "Unable to find resource with identifier: " + parentid;
			JSONObject json = new JSONObject();
			json.put("message", error);
			json.put("code", "ResourceNotFound");
			return createErrorResponse(json);

		} else {

			// found parent identifier
			GSResource parentGSResource = resultSet.getResultsList().get(0);
			String collectionID = "";
			if (parentGSResource.getSource().getEndpoint().contains(WORLDCEREAL_BASE_URL)) {
				isWordCereal = true;
			} else {
				isWordCereal = false;
			}
			Optional<String> optionalCollectionId = readCollectionIdFromParent(parentGSResource);
			if (optionalCollectionId.isPresent()) {
				collectionID = optionalCollectionId.get();
				uniqueJSONName = collectionID;
				if (!isWordCereal) {
					uniqueJSONName = "Agrostac_dataset_id_" + collectionID;
				}
			}

			GSLoggerFactory.getLogger(getClass()).trace("WorldCereal Parent Node id {}", collectionID);
			GSLoggerFactory.getLogger(getClass()).trace("Downloading WorldCereal items STARTED");
			try {

				metadataString = parentGSResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().asString(true);

				Integer code = null;

				Page requestPage = reducedMessage.getPage();

				int requestSize = requestPage.getSize();

				Downloader downloader = new Downloader();
				// downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
				int start = 1;
				page = new Page(start, DEFAULT_STEP_SIZE);

				boolean isNotFinished = true;
				int tries = 3;

				// String type = parentGSResource.getSource().getEndpoint();

				while (((code == null || code > 400) || isNotFinished) && tries > 0) {

					List<String> requests = createRequest(reducedMessage, page, collectionID, isWordCereal);

					int numberMatched = 0;
					int numberReturned = 0;

					for (String request : requests) {
						GSLoggerFactory.getLogger(getClass()).debug("Searching for query '{}' STARTED", request);

						HttpResponse<InputStream> response = downloader.downloadResponse(request);

						code = response.statusCode();
						InputStream body = response.body();

						if (code == 200 && body != null) {

							String stringResponse = IOStreamUtils.asUTF8String(body);

							JSONObject jsonResponse = new JSONObject(stringResponse);
							listObj.add(jsonResponse);

							numberMatched = jsonResponse.optInt("NumberMatched");
							numberReturned = jsonResponse.optInt("NumberReturned");

						} else {
							GSLoggerFactory.getLogger(getClass()).debug("ERROR RECEIVED FROM WORLDCEREAL SERVICE: {}", request);
							tries--;

							if (tries > 0) {
								// retry
								GSLoggerFactory.getLogger(getClass()).debug("TRY AGAIN");
								Thread.sleep(waitTime);
							} else {
								// return error
								GSLoggerFactory.getLogger(getClass()).debug("NUMBER OF TRIES EXPIRED: {}", request);
								errorDuringExecution = new JSONObject();
								errorDuringExecution.put("message", "Error received from remote WorldCereal service request: " + request);
								errorDuringExecution.put("code", "WorldCereal Internal Server Error");
								isNotFinished = false;

							}

						}

					}

					String fileName = collectionID + "_" + start;
					start = start + DEFAULT_STEP_SIZE;
					page = new Page(start, DEFAULT_STEP_SIZE);

					if (numberReturned < DEFAULT_STEP_SIZE || start > DEFAULT_MAX_SIZE) {
						tries = 0;
						isNotFinished = false;
						int finalNumber = numberReturned + start - 1;
						fileName = fileName + "to" + finalNumber;
					} else {
						fileName = fileName + "to" + start;
					}

					GSLoggerFactory.getLogger(getClass()).debug("Searching for query ENDED");

				}

				// HttpResponse response = retrieve(reducedMessage, page, collectionID);

			} catch (Exception e) {
				e.printStackTrace();
			}

			GSLoggerFactory.getLogger(getClass()).trace("Downloading WorldCereal items ENDED");

		}

		// return a ZIP file
		// ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		//
		// try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
		// map.entrySet().stream().forEach(entry -> {
		// String name = entry.getKey();
		// JSONObject obj = entry.getValue();
		// String jsonString = obj.toString(4);
		// try {
		// ZipEntry zipEntry = new ZipEntry(name);
		// zipOutputStream.putNextEntry(zipEntry);
		//
		// zipOutputStream.write(jsonString.getBytes());
		// zipOutputStream.closeEntry();
		// } catch (IOException e) {
		// throw new RuntimeException("Error writing JSON to ZIP", e);
		// }
		// });
		//
		// } catch (RuntimeException | IOException e) {
		// e.printStackTrace();
		// return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating ZIP file").build();
		// }
		//
		// // Return the ZIP file as a Response
		// return Response.ok(byteArrayOutputStream.toByteArray(), "application/zip")
		// .header("Content-Disposition", "attachment; filename=\"json_files.zip\"").build();

		/**
		 * alternative to return a unique JSONArray combining all the JSONObjects
		 */

		if (!listObj.isEmpty()) {
			// Create a JSONArray from the list of JSONObjects
			JSONArray jsonArray = new JSONArray(listObj);

			// Convert the JSONArray to a string
			String jsonString = jsonArray.toString(4);

			map.put(uniqueJSONName + ".json", jsonString);
			map.put(uniqueJSONName + ".xml", metadataString);

			//TODO generate this file on-the-fly
			String docFileName = "Codes_" + uniqueJSONName + ".pdf";
			if (!isWordCereal) {
				map.put(docFileName, "https://vlab-test-data.s3.us-east-1.amazonaws.com/AgrostacDoc.pdf");
			}
			// return a ZIP file
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
				map.entrySet().stream().forEach(entry -> {
					String name = entry.getKey();
					String objString = entry.getValue();
					try {
						ZipEntry zipEntry = new ZipEntry(name);
						zipOutputStream.putNextEntry(zipEntry);

						if (name != docFileName) {
							zipOutputStream.write(objString.getBytes());

						} else {
							InputStream pdfBody = new Downloader().downloadResponse(objString).body();
							pdfBody.transferTo(zipOutputStream);

						}

						zipOutputStream.closeEntry();

					} catch (IOException e) {
						throw new RuntimeException("Error writing JSON to ZIP", e);
					} catch (URISyntaxException e) {
						throw new RuntimeException("Error writing PDF to ZIP", e);
					} catch (InterruptedException e) {
						throw new RuntimeException("Error writing PDF to ZIP", e);
					}
				});

			} catch (RuntimeException | IOException e) {
				e.printStackTrace();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating ZIP file").build();
			}

			// Return the ZIP file as a Response
			return Response.ok(byteArrayOutputStream.toByteArray(), "application/zip")
					.header("Content-Disposition", "attachment; filename=\"" + uniqueJSONName + ".zip\"").build();

			//
			// // Return the JSON file as a Response
			// return Response.ok(inputStream, MediaType.APPLICATION_JSON)
			// .header("Content-Disposition", "attachment; filename=\"" + uniqueJSONName + ".json\"").build();
		} else {

			if (errorDuringExecution == null) {
				errorDuringExecution = new JSONObject();
				String error = "Unable to find resource with identifier: " + parentid;
				errorDuringExecution.put("message", error);
				errorDuringExecution.put("code", "ResourceNotFound");
			}

			return createErrorResponse(errorDuringExecution);
		}
	}

	// Optional<GSResource> parent = reducedMessage.getParentGSResource(parentid);
	//
	// if (parent.isPresent()) {
	//
	// GSResource parentGSResource = parent.get();
	//
	// String collectionID = "";
	//
	// Optional<String> optionalCollectionId = readCollectionIdFromParent(parentGSResource);
	// if (optionalCollectionId.isPresent()) {
	// collectionID = optionalCollectionId.get();
	// }
	//
	// }

	// KeyValueParser keyValueParser = new KeyValueParser(queryString.get());
	// OSRequestParser parser = new OSRequestParser(keyValueParser);
	//
	// // DiscoveryBondParser bondParser = getParser(message);
	//
	// String searchTerms = parser.parse(OSParameters.SEARCH_TERMS);
	//
	// WorldCerealBondHandler bondHandler = new WorldCerealBondHandler();

	// if (searchTerms == null || searchTerms.isEmpty()) {
	//
	// GSLoggerFactory.getLogger(getClass()).warn("No search terms provided!");
	//
	// return createEmptyResponse(message);
	// }

	private Optional<String> readCollectionIdFromParent(GSResource parentGSResource) {

		return parentGSResource.getExtensionHandler().getSTACSecondLevelInfo();
	}

	private String getParentId(ReducedDiscoveryMessage message) throws GSException {

		DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getReducedBond());

		ParentIdBondHandler parentIdBondHandler = new ParentIdBondHandler();

		bondParser.parse(parentIdBondHandler);

		if (!parentIdBondHandler.isParentIdFound())
			throw GSException.createException(getClass(), "No Parent Identifier specified to cmr second-level search", null,
					ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR, WORLDCEREAL_GRANULES_NO_PARENT_ERR_ID);

		return parentIdBondHandler.getParentValue();
	}

	private List<String> createRequest(ReducedDiscoveryMessage message, Page page, String datasetId, boolean isWorldCereal) {

		List<String> requests = new ArrayList<>();

		WorldCerealBondHandler bondHandler = parse(message, page.getStart(), page.getSize());

		List<String> qStrings = bondHandler.getQueryString(isWorldCereal, datasetId);

		for (String r : qStrings) {
			StringBuilder builder;
			if (isWorldCereal) {
				builder = new StringBuilder(WORLDCEREAL_BASE_URL + "collections/" + datasetId + "/items?");

			} else {
				builder = new StringBuilder(AGROSTAC_BASE_URL + "cropdatabyarea/");
				// String queryS = bondHandler.getQueryString(type);
			}
			builder.append(r);
			if (!isWorldCereal) {
				builder.append("&datasetid=" + datasetId);
			}

			requests.add(builder.toString());
		}

		return requests;
	}

	private WorldCerealBondHandler parse(ReducedDiscoveryMessage message, int start, int size) {
		WorldCerealBondHandler bondHandler = new WorldCerealBondHandler();

		bondHandler.setStart(start);

		bondHandler.setCount(size);

		DiscoveryBondParser bondParser = getParser(message);

		GSLoggerFactory.getLogger(getClass()).trace("Parsing reduced discovery message");

		bondParser.parse(bondHandler);

		GSLoggerFactory.getLogger(getClass()).trace("Parsed reduced discovery message");

		return bondHandler;
	}

	private DiscoveryBondParser getParser(ReducedDiscoveryMessage message) {
		return new DiscoveryBondParser(message.getReducedBond());
	}

	/**
	 * @param error
	 * @return
	 */
	private Response createErrorResponse(JSONObject error) {

		return Response.status(500).type(MediaType.APPLICATION_JSON).entity(error.toString()).build();

	}

}
