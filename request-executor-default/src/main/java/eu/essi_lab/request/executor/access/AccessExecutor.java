package eu.essi_lab.request.executor.access;

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

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.access.DataValidator;
import eu.essi_lab.access.DataValidatorFactory;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.pdk.rsm.access.AccessQueryUtils;
import eu.essi_lab.request.executor.AbstractAuthorizedExecutor;
import eu.essi_lab.request.executor.IAccessExecutor;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

/**
 * @author Fabrizio
 */
public class AccessExecutor extends AbstractAuthorizedExecutor implements IAccessExecutor {

    private static final String ACCESS_EXECUTOR_UNKNOWN_ONLINE_ID = "ACCESS_EXECUTOR_UNKNOWN_ONLINE_ID";
    private static final String ACCESS_EXECUTOR_TOO_MANY_RESOURCES = "ACCESS_EXECUTOR_TOO_MANY_RESOURCES";
    private static final String ACCESS_EXECUTOR_NO_AVAILABLE_REPORTS = "ACCESS_EXECUTOR_NO_AVAILABLE_REPORTS";
    private static final String ACCESS_EXECUTOR_NO_AVAILABLE_WORKFLOW = "ACCESS_EXECUTOR_NO_AVAILABLE_WORKFLOW";
    private static final String ACCESS_EXECUTOR_WORKFLOW_EXECUTION_ERROR = "ACCESS_EXECUTOR_WORKFLOW_EXECUTION_ERROR";
    private static final String ACCESS_EXECUTOR_PDP_ENGINE_ERROR = "ACCESS_EXECUTOR_PDP_ENGINE_ERROR";
    private static final String ACCESS_EXECUTOR_UNABLE_TO_RETRIEVE_DATA_OBJECT = "ACCESS_EXECUTOR_UNABLE_TO_RETRIEVE_DATA_OBJECT_ERROR";
    private static final String ACCESS_EXECUTOR_UNABLE_TO_FIND_DATA_DOWNLOADER = "ACCESS_EXECUTOR_UNABLE_TO_FIND_DATA_DOWNLOADER";

    private static final Double TOL = Math.pow(10, -8);

    @Override
    public CountSet count(AccessMessage message) throws GSException {

	String onlineId = message.getOnlineId();

	// GSLoggerFactory.getLogger(getClass()).info("Counting online with id " + onlineId + " STARTED");

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage(message);

	// GSLoggerFactory.getLogger(getClass()).info("Counting STARTED");

	CountSet count = executor.count(discoveryMessage);

	// GSLoggerFactory.getLogger(getClass()).info("Counting ENDED");

	return count;
    }

    /**
     * @param accessMessage
     * @return
     * @throws GSException
     */
    @Override
    public ResultSet<DataObject> retrieve(AccessMessage accessMessage) throws GSException {

	String onlineId = accessMessage.getOnlineId();
	GSLoggerFactory.getLogger(getClass()).debug("[ACCESS] Accessing online with id " + onlineId + " STARTED");

	// ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	// IDiscoveryExecutor executor = loader.iterator().next();

	// DiscoveryMessage discoveryMessage = new DiscoveryMessage(accessMessage);

	// GSLoggerFactory.getLogger(getClass()).info("Resource discovery STARTED");

	List<GSSource> sources = accessMessage.getSources();
	StorageInfo databaseURI = accessMessage.getDataBaseURI();
	ResultSet<GSResource> resultSet = AccessQueryUtils.findResource(accessMessage.getRequestId(), sources, onlineId, databaseURI);

	if (resultSet.getResultsList().isEmpty()) {

	    // --------------------------------------------------------------------------------------------
	    //
	    // we suppose that the capabilities of the access services refer
	    // only to well known online ids (by the way, the published online should all be conform to the
	    // current supported compliance level) so if no resource with such id is found,
	    // it is considered as internal error
	    //
	    throw GSException.createException(//
		    getClass(), //
		    "Unknown online Id: " + onlineId, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACCESS_EXECUTOR_UNKNOWN_ONLINE_ID);
	}

	if (resultSet.getResultsList().size() > 1) {

	    // --------------------------------------------------------------------
	    //
	    // we suppose there is exactly one resource with the supplied online id
	    //
	    throw GSException.createException(//
		    getClass(), //
		    "Too many resources with online Id: " + onlineId, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACCESS_EXECUTOR_TOO_MANY_RESOURCES);
	}

	Optional<ElasticsearchInfoPublisher> publisher = ElasticsearchInfoPublisher.create(accessMessage.getWebRequest());

	if (publisher.isPresent()) {

	    try {

		publisher.get().publish(resultSet);

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Error initializing ElasticSearch: {}", e.getMessage());
	    }
	}
    
	
	GSResource resource = resultSet.getResultsList().get(0);
	DataDescriptor targetDescriptor = accessMessage.getTargetDataDescriptor();

	return retrieve(resource, onlineId, targetDescriptor);
    }

    /**
     * @param accessMessage
     * @return
     * @throws GSException
     */
    @Override
    public ResultSet<DataObject> retrieve(GSResource resource, String onlineId, DataDescriptor targetDescriptor) throws GSException {

	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);

	List<DataComplianceReport> reports = handler.getReports();
	Optional<DataComplianceReport> optReport = reports.stream().filter(r -> r.getOnlineId().equals(onlineId)).findFirst();

	if (!optReport.isPresent()) {

	    // --------------------------------------------------------------
	    //
	    // we suppose there is a report related to the supplied online id
	    //
	    throw GSException.createException(//
		    getClass(), //
		    "No available reports found for online Id: " + onlineId, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACCESS_EXECUTOR_NO_AVAILABLE_REPORTS);
	}

	DataComplianceReport report = optReport.get();

	DataDownloader downloader = DataDownloaderFactory.getDataDownloader(resource, onlineId);

	// GSLoggerFactory.getLogger(getClass()).info("Retrieving data compliance report ENDED");

	if (downloader == null) {

	    throw GSException.createException(getClass(), //
		    "Data downloader not found for resource [" + resource.getPrivateId() + "] and online resource [" + onlineId + "]", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACCESS_EXECUTOR_UNABLE_TO_FIND_DATA_DOWNLOADER);
	}

	DataObject dataObject = retrieveDataObject(downloader, report.getFullDataDescriptor(), targetDescriptor);

	if (dataObject == null) {

	    throw GSException.createException(getClass(), //
		    "Unable to retrieve data object", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACCESS_EXECUTOR_UNABLE_TO_RETRIEVE_DATA_OBJECT);
	}

	dataObject.setResource(resource);

	List<GSResource> list = new ArrayList<>();
	list.add(resource);
	ResultSet<GSResource> resultSet = new ResultSet<>(list);
	ResultSet<DataObject> out = createResultSet(resultSet, dataObject);

	GSLoggerFactory.getLogger(getClass()).debug("[ACCESS] Accessing online with id " + onlineId + " ENDED");

	return out;
    }

    @Override
    public boolean isAuthorized(AccessMessage message) throws GSException {

	return isAuthorized(message, ACCESS_EXECUTOR_PDP_ENGINE_ERROR);
    }

    /**
     * Retrieves (if possible) a data object from a downloader, according to a given reportDescriptor (a previously
     * tested working descriptor) and a targetDescriptor (as requested by the user)
     * 
     * @param downloader
     * @param reportDescriptor
     * @param targetDescriptor
     * @return
     * @throws GSException
     */
    public DataObject retrieveDataObject(DataDownloader downloader, DataDescriptor reportDescriptor, DataDescriptor targetDescriptor)
	    throws GSException {

	// first, we ask the downloader which are the updated remote descriptors that it can download.
	// This is needed as remote descriptors might change over time (e.g. in case of
	// frequently updated sources, such as real time sensors. In this case updated data
	// is continuously being created, hence the temporal dimension of the descriptor is always increasing)
	List<DataDescriptor> remoteDescriptors = downloader.getRemoteDescriptors();
	// hence, among the obtained remote descriptors, we choose the one that is most similar to the report descriptor
	// that we know it can be safely used for access, as it has been passed the execution tests
	DataDescriptor remoteDescriptor = chooseDescriptor(remoteDescriptors, reportDescriptor);

	if (remoteDescriptor == null) {

	    return null;
	}

	// GSLoggerFactory.getLogger(getClass()).debug("Data download STARTED");

	// In this step we modify the chosen remote descriptor according to the user required target descriptor
	// and the downloader capabilities (e.g. if user has requested a subset of the entire data and the downloader
	// can subset the data, then the remote descriptor is modified setting the subset interval from the target
	// descriptor)
	enhanceRemoteDescriptor(downloader, remoteDescriptor, targetDescriptor);

	if (downloader.getOnline().getLinkage().contains("http://services.sentinel-hub.com/")) {
	    if (targetDescriptor.getDataType() == null) {
		remoteDescriptor.setIsPreview(true);
	    }
	}
	// then we download the possibly modified remote descriptor
	File dataFile = downloader.download(remoteDescriptor);
	dataFile.deleteOnExit();

	// GSLoggerFactory.getLogger(getClass()).debug("Data download ENDED");

	DataObject dataObject = new DataObject();
	dataObject.setDataDescriptor(remoteDescriptor);
	dataObject.setFile(dataFile);

	// a data validator is here used to assure that the downloaded file corresponds to the descriptor
	Optional<DataValidator> opt = DataValidatorFactory.create(remoteDescriptor.getDataFormat(), remoteDescriptor.getDataType());
	if (opt.isPresent()) {
	    DataValidator dataValidator = opt.get();
	    CRS originalCRS = remoteDescriptor.getCRS();
	    String originalCRSIdentifier = originalCRS.getIdentifier();
	    // the validator provides a most accurate descriptor. It might indeed happen that we ask a downloader to
	    // download a specific descriptor, but the obtained data is slightly different
	    DataDescriptor validatedDescriptor = dataValidator.readDataAttributes(dataObject);
	    // hence the remote descriptor is overwritten with the validated descriptor
	    remoteDescriptor = validatedDescriptor;
	    dataObject.setDataDescriptor(remoteDescriptor);
	    if (targetDescriptor != null) {

		// The target descriptor might have some blank fields
		// e.g. a target descriptor might have only the format field specified if user requests only to download
		// the dataset with a specific format, regardless of the other settings, such as resolution, subset, and
		// so on.
		// the blank fields must be filled, in order for the workflow builder to work. These are filled with the
		// fields that are present in the remote descriptor.
		remoteDescriptor.fillMissingInformationOf(targetDescriptor);
	    }
	    CRS validatedCRS = remoteDescriptor.getCRS();
	    if(validatedCRS == null) {
		validatedCRS = CRS.EPSG_4326();
		remoteDescriptor.setCRS(validatedCRS);
	    }
	    String validatedCRSIdentifier = validatedCRS.getIdentifier();
	    if (validatedCRSIdentifier == null || validatedCRSIdentifier.isEmpty()) {
		// the CRS identifier is set from the original CRS identifier, in case where the validator didn't
		// recognized
		// a CRS identifier (in cases where at least projection has been recognized)
		if (validatedCRSIdentifier == null || validatedCRSIdentifier.isEmpty()) {
		    // the CRS identifier is set from the original identifier, in case where the validator didn't
		    // recognized
		    // a CRS identifier (in cases where at least projection has been recognized)
		    if (originalCRS.getProjection().equals(validatedCRS.getProjection())) {
			validatedCRS.setIdentifier(originalCRSIdentifier);
		    }

		}

	    }

	} else {
	    String msg = "The downloaded file is not in expected format. Remote service error?";
	    GSLoggerFactory.getLogger(getClass()).error(msg);
	    throw GSException.createException(//
		    getClass(), //
		    msg, //
		    msg, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACCESS_EXECUTOR_WORKFLOW_EXECUTION_ERROR//
	    );
	}

	if (targetDescriptor == null) {

	    GSLoggerFactory.getLogger(getClass()).info("No transformation requested, returning downloaded data");

	    return dataObject;

	} else {

	    // GSLoggerFactory.getLogger(getClass()).info("Workflow execution STARTED");

	    WorkflowBuilder builder = WorkflowBuilder.createLoadedBuilder();
	    Optional<Workflow> optWorkflow = builder.buildPreferred(remoteDescriptor, targetDescriptor);

	    if (!optWorkflow.isPresent()) {

		throw GSException.createException(//
			getClass(), //
			"No available workflow found for online Id: " + downloader.getOnline().getIdentifier(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			ACCESS_EXECUTOR_NO_AVAILABLE_WORKFLOW);
	    }

	    Workflow workflow = optWorkflow.get();
	    try {

		// here we copy range values from the previously downloaded descriptor
		// ... if not already set
		if (remoteDescriptor.getRangeMinimum() == null) {
		    remoteDescriptor.setRangeMinimum(reportDescriptor.getRangeMinimum());
		}
		if (remoteDescriptor.getRangeMaximum() == null) {
		    remoteDescriptor.setRangeMaximum(reportDescriptor.getRangeMaximum());
		}
		if (targetDescriptor.getRangeMinimum() == null) {
		    targetDescriptor.setRangeMinimum(reportDescriptor.getRangeMinimum());
		}
		if (targetDescriptor.getRangeMaximum() == null) {
		    targetDescriptor.setRangeMaximum(reportDescriptor.getRangeMaximum());
		}

		DataObject result = workflow.execute(dataObject, targetDescriptor);

		// GSLoggerFactory.getLogger(getClass()).info("Workflow execution ENDED");

		return result;

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			ACCESS_EXECUTOR_WORKFLOW_EXECUTION_ERROR, //
			e);
	    }
	}

    }

    /**
     * Choose from the just obtained remote descriptor one that is similar in terms of format, crs and type to the
     * descriptor tested successfully during access test. This method is useful, because in the meantime (after the
     * access test has been done, it can be that dimensions have been increased due to new data has become available
     * (e.g. in case of real time data)
     * 
     * @param remoteDescriptors
     * @param fullDataDescriptor
     * @return
     */
    private DataDescriptor chooseDescriptor(List<DataDescriptor> remoteDescriptors, DataDescriptor fullDataDescriptor) {
	CRS testedCrs = fullDataDescriptor.getCRS();
	DataFormat testedFormat = fullDataDescriptor.getDataFormat();
	DataType testedType = fullDataDescriptor.getDataType();
	for (DataDescriptor dataDescriptor : remoteDescriptors) {
	    CRS crs = dataDescriptor.getCRS();
	    DataFormat format = dataDescriptor.getDataFormat();
	    DataType type = dataDescriptor.getDataType();
	    if (Objects.equals(crs, testedCrs) && //
		    Objects.equals(format, testedFormat) && //
		    Objects.equals(type, testedType)) {
		return dataDescriptor;
	    }
	}

	if (remoteDescriptors.isEmpty()) {

	    return null;
	}

	return remoteDescriptors.get(0);
    }

    /**
     * The full descriptor gets optimized according to the target descriptor, depending on the downloader
     * capabilities. In particular Subset/resolution will be possibly modified.
     * TODO: the downloader.canSubset invocation should be replaced in the future with a read on the access report
     * access report should be modified to report downloader subset capabilities (i.e. the subsettable dimensions)
     * 
     * @param downloader
     * @param fullDescriptor
     * @param targetDescriptor
     */
    private void enhanceRemoteDescriptor(DataDownloader downloader, DataDescriptor fullDescriptor, DataDescriptor tmpDescriptor) {

	if (tmpDescriptor == null) {
	    return;
	}

	DataDescriptor targetDescriptor = tmpDescriptor.clone();

	CRS sourceCRS = fullDescriptor.getCRS();
	CRS targetCRS = targetDescriptor.getCRS();

	Double lowerTarget1 = null;
	Double lowerTarget2 = null;
	Double upperTarget1 = null;
	Double upperTarget2 = null;

	if (targetCRS == null || targetCRS.getDecodedCRS() == null) {
	    targetCRS = sourceCRS;
	}

	if (!sourceCRS.equals(targetCRS)) {
	    DataDimension spatial1 = targetDescriptor.getFirstSpatialDimension();
	    DataDimension spatial2 = targetDescriptor.getSecondSpatialDimension();
	    if (spatial1 != null && spatial2 != null) {
		SimpleEntry<Double, Double> lower = new SimpleEntry<Double, Double>(
			spatial1.getContinueDimension().getLower().doubleValue(), spatial2.getContinueDimension().getLower().doubleValue());
		SimpleEntry<Double, Double> upper = new SimpleEntry<Double, Double>(
			spatial1.getContinueDimension().getUpper().doubleValue(), spatial2.getContinueDimension().getUpper().doubleValue());
		SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>>(
			lower, upper);
		try {
		    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> targetCorners = CRSUtils
			    .translateBBOX(sourceCorners, targetCRS, sourceCRS);
		    SimpleEntry<Double, Double> lowerTarget = targetCorners.getKey();
		    SimpleEntry<Double, Double> upperTarget = targetCorners.getValue();
		    lowerTarget1 = lowerTarget.getKey();
		    lowerTarget2 = lowerTarget.getValue();
		    upperTarget1 = upperTarget.getKey();
		    upperTarget2 = upperTarget.getValue();

		    switch (sourceCRS.getAxisOrder()) { //
		    case UNAPPLICABLE:
		    case UNKNOWN:
			if (sourceCRS.equals(CRS.fromIdentifier("EPSG:3031"))) {
			    break;
			}
			break;

		    case EAST_NORTH:

			// because geotools always returns x y
			// (commented because it seems geotools is returning in the correct order depending on the CRS
			// now)
			// GSLoggerFactory.getLogger(getClass()).trace("Switching dimensions");
			// Double tmp = lowerTarget1;
			// lowerTarget1 = lowerTarget2;
			// lowerTarget2 = tmp;
			// tmp = upperTarget1;
			// upperTarget1 = upperTarget2;
			// upperTarget2 = tmp;
			// break;

		    default:
			break;
		    }

		    Double sourceLower1 = fullDescriptor.getFirstSpatialDimension().getContinueDimension().getLower().doubleValue();
		    Double sourceUpper1 = fullDescriptor.getFirstSpatialDimension().getContinueDimension().getUpper().doubleValue();
		    Double sourceLower2 = fullDescriptor.getSecondSpatialDimension().getContinueDimension().getLower().doubleValue();
		    Double sourceUpper2 = fullDescriptor.getSecondSpatialDimension().getContinueDimension().getUpper().doubleValue();

		    // the THREDDS case... it has coverages with longitude from 0 to 360 instead of -180 180
		    if (sourceCRS.equals(CRS.EPSG_4326()) && Math.abs(sourceUpper2 - 180.0) > TOL && upperTarget2 < TOL) {
			lowerTarget2 += 360.0;
			upperTarget2 += 360.0;
		    }

		    if (lowerTarget1 < sourceLower1) {
			lowerTarget1 = sourceLower1;
		    }
		    if (upperTarget1 > sourceUpper1) {
			upperTarget1 = sourceUpper1;
		    }
		    if (lowerTarget2 < sourceLower2) {
			lowerTarget2 = sourceLower2;
		    }
		    if (upperTarget2 > sourceUpper2) {
			upperTarget2 = sourceUpper2;
		    }

		    targetDescriptor.setCRS(sourceCRS);
		    targetDescriptor.getSpatialDimensions().get(0).getContinueDimension().setLower(lowerTarget1);
		    targetDescriptor.getSpatialDimensions().get(1).getContinueDimension().setLower(lowerTarget2);
		    targetDescriptor.getSpatialDimensions().get(0).getContinueDimension().setUpper(upperTarget1);
		    targetDescriptor.getSpatialDimensions().get(1).getContinueDimension().setUpper(upperTarget2);

		    Long size1 = targetDescriptor.getSpatialDimensions().get(0).getContinueDimension().getSize();
		    if (size1 != null) {
			targetDescriptor.getSpatialDimensions().get(0).getContinueDimension().setSize(size1 * 2);
		    }
		    Long size2 = targetDescriptor.getSpatialDimensions().get(1).getContinueDimension().getSize();
		    if (size2 != null) {
			targetDescriptor.getSpatialDimensions().get(1).getContinueDimension().setSize(size2 * 2);
		    }
		    targetDescriptor.getSpatialDimensions().get(0).getContinueDimension().setResolution(null);
		    targetDescriptor.getSpatialDimensions().get(1).getContinueDimension().setResolution(null);

		    targetCRS = sourceCRS;

		} catch (Exception e) {
		    e.printStackTrace();
		    GSLoggerFactory.getLogger(getClass()).error("Converting " + sourceCRS + " to " + targetCRS);
		}

	    }
	}
	if (sourceCRS.equals(targetCRS)) {
	    for (int i = 0; i < fullDescriptor.getSpatialDimensions().size(); i++) {
		DataDimension dataDimension = fullDescriptor.getSpatialDimensions().get(i);
		if (downloader.canSubset(dataDimension.getName())) { //
		    DataDimension targetDimension = null;
		    List<DataDimension> spatialDimensions = targetDescriptor.getSpatialDimensions();
		    if (spatialDimensions != null && !spatialDimensions.isEmpty()) {
			targetDimension = targetDescriptor.getSpatialDimensions().get(i);
		    }
		    if (targetDimension != null) {
			fullDescriptor.getSpatialDimensions().set(i, getTargetDimension(dataDimension, targetDimension));
		    }
		}
	    }
	}

	DataDimension temporalDimension = fullDescriptor.getTemporalDimension();

	if (temporalDimension != null && downloader.canSubset(temporalDimension.getName())) {

	    DataDimension targetDimension = getTargetDimension(temporalDimension, targetDescriptor.getTemporalDimension());
	    fullDescriptor.setTemporalDimension(targetDimension);
	    tmpDescriptor.setTemporalDimension(targetDescriptor.getTemporalDimension());

	}

	for (int i = 0; i < fullDescriptor.getOtherDimensions().size(); i++) {

	    DataDimension dataDimension = fullDescriptor.getOtherDimensions().get(i);
	    if (downloader.canSubset(dataDimension.getName())) {
		DataDimension targetDimension = targetDescriptor.findDimension(dataDimension.getName());
		if (targetDimension != null) {
		    fullDescriptor.getOtherDimensions().set(i, getTargetDimension(dataDimension, targetDimension));
		}
	    }
	}
    }

    private DataDimension getTargetDimension(DataDimension currentDimension, DataDimension requiredDimension) {
	if (requiredDimension == null) {
	    return currentDimension;
	}
	Number lowerTolerance = currentDimension.getContinueDimension().getLowerTolerance();
	Number currentLower = currentDimension.getContinueDimension().getLower();
	LimitType currentLowerType = currentDimension.getContinueDimension().getLowerType();

	Number upperTolerance = currentDimension.getContinueDimension().getUpperTolerance();
	Number currentUpper = currentDimension.getContinueDimension().getUpper();
	LimitType currentUpperType = currentDimension.getContinueDimension().getUpperType();

	Number requiredLower = requiredDimension.getContinueDimension().getLower();
	LimitType requiredLowerType = requiredDimension.getContinueDimension().getLowerType();

	Number requiredUpper = requiredDimension.getContinueDimension().getUpper();
	LimitType requiredUpperType = requiredDimension.getContinueDimension().getUpperType();

	Number targetLower = null;
	Number targetUpper = null;
	LimitType targetLowerType = LimitType.ABSOLUTE;
	LimitType targetUpperType = LimitType.ABSOLUTE;
	Long requiredSize = requiredDimension.getContinueDimension().getSize();

	switch (currentLowerType) {
	case ABSOLUTE:
	    switch (requiredLowerType) {
	    case ABSOLUTE:
	    case CONTAINS:
		if (requiredLower != null) {
		    // targetLower = difference(requiredLower, lowerTolerance); // possibly more data is asked, this is
		    // a
		    // // safe approach. The subsetter will cut
		    // // it.
		    targetLower = requiredLower;
		} else {
		    if (requiredSize == 1) {
			// the lower limit is choosen as the upper if size = 1 and no information about required lower
			// is given
			targetLower = currentUpper;
			requiredDimension.getContinueDimension().setLower(currentUpper);
		    } else {
			targetLower = currentLower;
		    }
		}
		targetLowerType = LimitType.ABSOLUTE;
		break;
	    case MAXIMUM:
		targetLowerType = LimitType.MAXIMUM;
		targetLower = requiredLower; // an absolute value is also added for downloaders that doesn't understand
					     // maximum requests
		break;
	    case MINIMUM:
		targetLowerType = LimitType.MINIMUM;
		targetLower = requiredLower; // an absolute value is also added for downloaders that doesn't understand
					     // minimum requests
		break;
	    default:
		GSLoggerFactory.getLogger(getClass()).error("This shouldn't happen: unexpected case");
	    }
	    break;
	case CONTAINS:
	case MAXIMUM:
	case MINIMUM:
	    GSLoggerFactory.getLogger(getClass())
		    .error("This shouldn't happen: " + currentLowerType + " is only to be used in data requests, not in capabilities");
	    break;
	default:
	    GSLoggerFactory.getLogger(getClass()).error("This shouldn't happen: unexpected case");
	    break;
	}

	switch (currentUpperType) {
	case ABSOLUTE:
	    switch (requiredUpperType) {
	    case ABSOLUTE:
	    case CONTAINS:
		if (requiredUpper != null) {
		    // targetUpper = sum(requiredUpper, upperTolerance); // possibly more data is asked, this is a
		    // safe approach. The subsetter will cut it.
		    targetUpper = requiredUpper;
		} else {
		    if (requiredSize == 1) {
			targetUpper = currentUpper;
			requiredDimension.getContinueDimension().setUpper(currentUpper);
		    } else {
			targetUpper = currentUpper;
		    }
		}
		targetUpperType = LimitType.ABSOLUTE;
		break;
	    case MAXIMUM:
		targetUpperType = LimitType.MAXIMUM;
		targetUpper = requiredUpper; // an absolute value is also added for downloaders that doesn't understand
					     // maximum requests
		break;
	    case MINIMUM:
		targetUpperType = LimitType.MINIMUM;
		targetUpper = requiredUpper; // an absolute value is also added for downloaders that doesn't understand
					     // minimum requests
		break;
	    default:
		GSLoggerFactory.getLogger(getClass()).error("This shouldn't happen: unexpected case");
	    }
	    break;
	case CONTAINS:
	case MAXIMUM:
	case MINIMUM:
	    GSLoggerFactory.getLogger(getClass())
		    .error("This shouldn't happen: " + currentUpperType + " is only to be used in data requests, not in capabilities");
	    break;
	default:
	    GSLoggerFactory.getLogger(getClass()).error("This shouldn't happen: unexpected case");
	    break;
	}

	ContinueDimension targetDimension = new ContinueDimension(currentDimension.getName());
	targetDimension.setType(currentDimension.getType());
	targetDimension.setLower(targetLower);
	targetDimension.setDatum(requiredDimension.getContinueDimension().getDatum());
	targetDimension.setUom(requiredDimension.getContinueDimension().getUom());
	targetDimension.setLowerType(targetLowerType);
	targetDimension.setUpper(targetUpper);
	targetDimension.setUpperType(targetUpperType);

	targetDimension.setSize(requiredSize);
	Number requiredResolution = requiredDimension.getContinueDimension().getResolution();
	targetDimension.setResolution(requiredResolution);
	Number requiredResolutionTolerance = requiredDimension.getContinueDimension().getResolutionTolerance();
	targetDimension.setResolutionTolerance(requiredResolutionTolerance);
	return targetDimension;
    }

    private Number difference(Number n1, Number n2) {
	if (n1 instanceof Long) {
	    return n1.longValue() - n2.longValue();
	} else {
	    return n1.doubleValue() - n2.doubleValue();
	}
    }

    private Number sum(Number n1, Number n2) {
	if (n1 instanceof Long) {
	    return n1.longValue() + n2.longValue();
	} else {
	    return n1.doubleValue() + n2.doubleValue();
	}
    }

    protected static ResultSet<DataObject> createResultSet(ResultSet<GSResource> resultSet, DataObject result) {

	ResultSet<DataObject> out = new ResultSet<>();
	out.setResultsList(Arrays.asList(result));

	out.setCountResponse(resultSet.getCountResponse());
	out.getException().getErrorInfoList().addAll(resultSet.getException().getErrorInfoList());

	return out;
    }
}
