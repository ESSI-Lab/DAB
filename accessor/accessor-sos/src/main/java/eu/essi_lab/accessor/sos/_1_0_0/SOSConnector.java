package eu.essi_lab.accessor.sos._1_0_0;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.geotools.api.geometry.BoundingBox;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.sos.SOSProperties;
import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;
import eu.essi_lab.accessor.sos.SOSRequestBuilder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ParallelTaskHandler;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class SOSConnector extends eu.essi_lab.accessor.sos.SOSConnector {

    /**
     * 
     */
    public static final String TYPE = "SOS 1.0.0 Connector";
    private SOSCapabilities capabilities;
    private List<SOSProcedure> procedures;
    private int observedPropertiesCount;
    private List<String> offeringNames;

    public static final HashMap<String, SOSSensorML> PROCEDURES_MAP = new HashMap<>();
    private int parallelTasks;
    private int errorsCount;
    private static final int MAX_ERRORS = 10;
    private static final int DEFAULT_PARALLEL_TASKS = 3;
    private static final int ATTEMPTS = 30;
    private static final int RETRY_DELAY = 30;

    /**
     * 
     */
    public SOSConnector() {

	parallelTasks = DEFAULT_PARALLEL_TASKS;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<OriginalMetadata>();

	//
	// retrieves the capabilities
	//
	if (capabilities == null) {
	    try {

		GSLoggerFactory.getLogger(getClass()).debug("Retrieving capabilities STARTED");

		SOSRequestBuilder requestBuilder = new SOSRequestBuilder(getSourceURL(), "1.0.0");

		capabilities = new SOSCapabilities(requestBuilder.createCapabilitiesRequest());

		GSLoggerFactory.getLogger(getClass()).debug("Retrieving capabilities ENDED");

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		throw GSException.createException(getClass(), "SOSConnector_1_0_0_GetCapabilitiesError", e);
	    }
	}

	//
	// retrieve the procedures href
	//
	if (procedures == null) {

	    offeringNames = capabilities.getOfferingNames();

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} offering", StringUtils.format(offeringNames.size()));

	    procedures = new ArrayList<>();

	    offeringNames.forEach(offeringName -> {

		try {

		    procedures.addAll(capabilities.//
			    getProcedures(offeringName).//
			    stream().//
			    map(procHref -> new SOSProcedure(procHref, offeringName)).//
			    collect(Collectors.toList()));

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    });

	    GSLoggerFactory.getLogger(getClass()).debug("Found {} procedures", StringUtils.format(procedures.size()));
	}

	//
	//
	//

	int proceduresIndex = 0;

	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    proceduresIndex = Integer.valueOf(resumptionToken);
	}

	ConfigurationWrapper.getSystemSettings().//
		getKeyValueOptions().ifPresent(o -> parallelTasks = Integer.valueOf(//
			o.getProperty(KeyValueOptionKeys.SOS_100_PARALLEL_TASKS.getLabel(), String.valueOf(DEFAULT_PARALLEL_TASKS))));

	boolean exit = proceduresIndex + parallelTasks > procedures.size();

	if (!exit) {

	    response.setResumptionToken(String.valueOf(proceduresIndex + parallelTasks));

	} else {

	    capabilities.release();
	}

	int end = exit ? procedures.size() : proceduresIndex + parallelTasks;

	//
	// creates the input list with the subset of the procedures to handle
	//
	ArrayList<SOSProcedure> inputProcedures = new ArrayList<>();

	for (; proceduresIndex < end; proceduresIndex++) {

	    inputProcedures.add(procedures.get(proceduresIndex));
	}

	//
	// creates and set the parallel handler
	//
	ParallelTaskHandler<SOSProcedure, Optional<SOSProcedure>> handler = new ParallelTaskHandler<>();

	handler.set(inputProcedures, procedure -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Describe sensor {} STARTED", procedure.getHref());

	    SOSRequestBuilder requestBuilder = new SOSRequestBuilder(getSourceURL(), "1.0.0");
	    String sensorDescRequest = requestBuilder.createProcedureDescriptionRequest(//
		    procedure.getHref(), //
		    "text/xml;subtype=\"sensorML/1.0.1\"");

	    Downloader downloader = new Downloader();
	    downloader.setRetryPolicy(ATTEMPTS, TimeUnit.SECONDS, RETRY_DELAY);

	    Optional<String> optResponse = downloader.downloadOptionalString(sensorDescRequest);

	    GSLoggerFactory.getLogger(getClass()).debug("Describe sensor {} ENDED", procedure.getHref());

	    if (optResponse.isPresent()) {

		procedure.setSensorML(optResponse.get());
		return Optional.of(procedure);
	    }

	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve procedure: {}", procedure.getHref());

	    return Optional.empty();
	});

	//
	// executes the requests in parallel
	//
	List<Optional<SOSProcedure>> responseList = null;

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Executing get procedures [{}/{}-{}] STARTED",

		    StringUtils.format(end - parallelTasks), //
		    StringUtils.format(end), //
		    StringUtils.format(procedures.size())//
	    );

	    responseList = handler.run();

	    GSLoggerFactory.getLogger(getClass()).debug("Executing get procedures [{}/{}-{}] ENDED",

		    StringUtils.format(end - parallelTasks), //
		    StringUtils.format(end), //
		    StringUtils.format(procedures.size())//
	    );

	} catch (InterruptedException | ExecutionException e) {

	    throw GSException.createException(getClass(), "SOS_100_Connector_ParallelDescribeSensorError", e);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Handling procedures [{}/{}-{}] STARTED",

		StringUtils.format(end - parallelTasks), //
		StringUtils.format(end), //
		StringUtils.format(procedures.size())//
	);

	//
	// TO BE MAPPED ?
	//
	Optional<String> title = capabilities.getTitle(); // SOSProperty missing

	Optional<String> abstract_ = capabilities.getAbstract(); // SOSProperty missing

	Optional<String> postalCode = capabilities.getPostalCode(); // SOSProperty missing

	Optional<String> positionName = capabilities.getPositionName();

	//
	//
	//

	Optional<String> city = capabilities.getCity();

	Optional<String> country = capabilities.getCountry();

	Optional<String> adArea = capabilities.getAdministrativeArea();

	Optional<String> deliveryPoint = capabilities.getDeliveryPoint();

	List<String> keywords = capabilities.getKeywords();

	Optional<String> mailAddress = capabilities.getMailAddress();

	Optional<String> phoneNumber = capabilities.getPhoneNumber();

	Optional<String> providerName = capabilities.getProviderName();

	Optional<String> individualName = capabilities.getIndividualName();

	Optional<String> providerSite = capabilities.getProviderSite();

	Optional<Integer> maxRecords = getSetting().getMaxRecords();

	//
	// handles the responses
	//
	for (Optional<SOSProcedure> optProcedure : responseList) {

	    if (optProcedure.isPresent()) {

		boolean errorOccurred = false;
		XMLDocumentReader reader = null;

		try {

		    reader = new XMLDocumentReader(optProcedure.get().getSensorML());

		    errorOccurred = reader.evaluateBoolean("exists(//*:Exception)");

		} catch (SAXException | IOException | XPathExpressionException e) {

		    errorOccurred = true;

		    GSLoggerFactory.getLogger(getClass()).error("Unable to handle procedure XML: " + e.getMessage(), e);
		}

		if (errorOccurred) {

		    GSLoggerFactory.getLogger(getClass()).warn("Error message retrieved: {}", optProcedure.get().getSensorML());

		    errorsCount++;

		    if (errorsCount == MAX_ERRORS) {

			errorsCount = 0;

			throw GSException.createException(//
				getClass(), //
				"Too many GetProcedure errors!", //
				ErrorInfo.ERRORTYPE_CLIENT, //
				ErrorInfo.SEVERITY_ERROR, //
				"SOS_100_Connector_TooManyGetProcedureErrors");
		    }

		} else {

		    String offering = optProcedure.get().getOffering();

		    String procHref = optProcedure.get().getHref();

		    SOSSensorML sosSensorML = new SOSSensorML(reader);

		    PROCEDURES_MAP.put(procHref, sosSensorML);

		    Optional<String> name = sosSensorML.getName();

		    if (name.isEmpty()) {

			GSLoggerFactory.getLogger(getClass()).error("Missing name of procedure {}", procHref);

			continue;
		    }

		    Optional<String> sensorCountry = sosSensorML.getCountry();

		    Optional<Double> elevation = sosSensorML.getElevation();

		    Optional<BoundingBox> location = sosSensorML.getLocation();

		    Optional<TemporalExtent> temporalExtent = sosSensorML.getTemporalExtent();

		    Optional<String> uniqueIdentifier = sosSensorML.getUniqueIdentifier();

		    Optional<String> elevationUOM = sosSensorML.getElevationUOM();

		    Optional<String> licenseName = sosSensorML.getLicenseName();

		    Optional<String> licenseDescription = sosSensorML.getLicenseDescription();

		    Optional<String> licenseSummary = sosSensorML.getLicenseSummary();

		    Optional<String> restriction = sosSensorML.getRestriction();

		    Optional<String> sensorType = sosSensorML.getSensorType();

		    Optional<String> systemType = sosSensorML.getSystemType();

		    Optional<String> originatingOrganization = sosSensorML.getOriginatingOrganization();

		    Optional<String> photoLink = sosSensorML.getPhotoLink();

		    List<String> acquiferKeywords = sosSensorML.getAcquiferKeywords();
		    keywords.addAll(acquiferKeywords);

		    List<SOSObservedProperty> observervedProperties = sosSensorML.getObservervedProperties();

		    GSLoggerFactory.getLogger(getClass()).debug("Found {} observed properties", observervedProperties.size());

		    for (SOSObservedProperty obsProperty : observervedProperties) {

			if (maxRecords.isPresent() && observedPropertiesCount >= maxRecords.get()) {

			    GSLoggerFactory.getLogger(getClass()).debug("Reached limit of {} records, exit!", maxRecords.get());

			    response.setResumptionToken(null);

			    capabilities.release();

			    return response;

			} else {

			    SOSProperties sosProperties = new SOSProperties();

			    sosProperties.setProperty(SOSProperty.OFFERING, offering);

			    String propertyName = obsProperty.getName();
			    String propertyDef = obsProperty.getDefinition();
			    String propertyUom = obsProperty.getUom();

			    sosProperties.setProperty(SOSProperty.OBSERVED_PROPERTY_ID, propertyDef);
			    sosProperties.setProperty(SOSProperty.OBSERVED_PROPERTY_NAME, propertyName);
			    sosProperties.setProperty(SOSProperty.OBSERVED_PROPERTY_UOM_CODE, propertyUom);

			    sosProperties.setProperty(SOSProperty.PROCEDURE_TITLE, name.get());

			    sosProperties.setProperty(SOSProperty.PROCEDURE_HREF, procHref);
			    sosProperties.setProperty(SOSProperty.PROCEDURE_IDENTIFIER, uniqueIdentifier.orElse(null));

			    //
			    // from capabilities
			    //

			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_AddressCountry, country.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_PositionName, positionName.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_AddressCity, city.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_AddressAdministrativeArea, adArea.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_AddressDeliveryPoint, deliveryPoint.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_Keywords, keywords.//
				    stream().//
				    map(k -> k.trim().strip()).//
				    filter(k -> !k.isBlank() && !k.isEmpty()).//
				    distinct().//
				    collect(Collectors.joining(";")));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_AddressEmailAddress, mailAddress.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_Phone, phoneNumber.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_NAME, providerName.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_IndividualName, individualName.orElse(null));
			    sosProperties.setProperty(SOSProperty.SERVICE_PROVIDER_SITE, providerSite.orElse(null));

			    //
			    //
			    //

			    if (temporalExtent.isPresent()) {
				sosProperties.setProperty(SOSProperty.TEMP_EXTENT_BEGIN, temporalExtent.get().getBeginPosition());
				sosProperties.setProperty(SOSProperty.TEMP_EXTENT_END, temporalExtent.get().getEndPosition());
			    } else {
				GSLoggerFactory.getLogger(getClass()).warn("Missing temporal extent!");
				continue;
			    }

			    if (location.isPresent()) {
				sosProperties.setProperty(SOSProperty.LATITUDE, String.valueOf(location.get().getMinY()));
				sosProperties.setProperty(SOSProperty.LONGITUDE, String.valueOf(location.get().getMinX()));
			    }

			    sosProperties.setProperty(SOSProperty.FOI_NAME, name.get());
			    sosProperties.setProperty(SOSProperty.FOI_ID, uniqueIdentifier.orElse(null));

			    sosProperties.setProperty(SOSProperty.SENSOR_ContactOwnerAddressCountry, sensorCountry.orElse(null));

			    // probably not accurate!
			    sosProperties.setProperty(SOSProperty.SENSOR_Name, sensorType.orElse(null));

			    sosProperties.setProperty(SOSProperty.ELEVATION, elevation.map(v -> String.valueOf(v)).orElse(null));

			    originatingOrganization
				    .ifPresent(org -> sosProperties.setProperty(SOSProperty.SENSOR_ContactOwnerOrganization, org));

			    photoLink.ifPresent(ph -> sosProperties.setProperty(SOSProperty.SENSOR_DocumentationImage, ph));

			    //
			    //
			    //

			    sosProperties.setProperty(SOSProperty.SENSOR_ContactOwnerAddressCity, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_ContactManufacturerOrganization, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_ContactOwnerAddressDeliveryPoint, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_ContactOwnerAddressEmail, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_ContactOwnerHomepage, null);

			    sosProperties.setProperty(SOSProperty.SENSOR_ContactOwnerPhone, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_Description, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_DocumentationImageDescription, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_DocumentationImageFormat, null);

			    sosProperties.setProperty(SOSProperty.SENSOR_Keywords, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_LongName, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_ManufacturerName, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_Material, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_Mobile, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_ShortName, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_Status, null);
			    sosProperties.setProperty(SOSProperty.SENSOR_UniqueId, null);

			    //
			    //
			    //

			    try {

				OriginalMetadata metadataRecord = new OriginalMetadata();

				metadataRecord.setMetadata(sosProperties.asString());

				metadataRecord.setSchemeURI(CommonNameSpaceContext.SOS_1_0_0);
				response.addRecord(metadataRecord);

				observedPropertiesCount++;

			    } catch (IOException e) {

				GSLoggerFactory.getLogger(getClass()).error(e);
			    }
			}
		    }
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Total number of records: {}", StringUtils.format(observedPropertiesCount));

	GSLoggerFactory.getLogger(getClass()).debug("Handling procedures [{}/{}-{}] ENDED",

		StringUtils.format(end - parallelTasks), //
		StringUtils.format(end), //
		StringUtils.format(procedures.size())//
	);

	//
	//
	//

	return response;
    }

    /**
     * This should be reimplemented. At the moment the only SOS 100 ggis.un-igrac.org/istsos
     * has very huge capabilities, no way to use them in this method!
     */
    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();

	return endpoint.contains("ggis.un-igrac.org/istsos");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CommonNameSpaceContext.SOS_1_0_0);
    }

    public String getDownloadProtocol() {

	return NetProtocolWrapper.SOS_1_0_0.getCommonURN();
    }

    /**
     * @return
     */
    @Override
    public SOSRequestBuilder createRequestBuilder() {

	return new SOSRequestBuilder(getSourceURL(), "1.0.0");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    public static void main(String[] args) throws GSException {

	SOSConnector sosConnector = new SOSConnector();
	sosConnector.setSourceURL("https://ggis.un-igrac.org/istsos?api-key=724df1163f0a99f81af89f3b446d76281d07d3ac");

	sosConnector.listRecords(new ListRecordsRequest());

    }
}
