package eu.essi_lab.accessor.nve;

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

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.access.augmenter.DescriptorComparator;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourcePropertyHandler;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author boldrini
 */
public class NVEMapper extends OriginalIdentifierMapper {

    private static final String NVE_URN = "urn:no.nve.hydapi:";
    private static final String NVE_MAPPER_ERROR = "NVE_MAPPER_ERROR";

    private String endpoint;

    public NVEMapper() {
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	this.endpoint = source.getEndpoint();

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.NVE_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String metadata = originalMD.getMetadata();


	JSONObject obj;
	try {
	    obj = new JSONObject(metadata);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NVE_MAPPER_ERROR, //
		    e//
	    );
	}
	NVEStation station = new NVEStation(obj);
	String stationId = station.getId();
	String stationName = station.getName();
	NVESeries series = station.getSeries().get(0);
	String parameterId = series.getParameterId();

	NVEResolution resolution = series.getResolutions().get(0);
	String restime = resolution.getResTime();
	String method = resolution.getMethod();

	NVEIdentifierMangler mangler = new NVEIdentifierMangler();
	// here the URN is not needed
	mangler.setPlatformIdentifier(stationId);
	mangler.setParameterIdentifier(parameterId);
	mangler.setResolutionIdentifier(restime);

	String id = mangler.getMangling();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(resolution.getDataFromTime());
	Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(resolution.getDataToTime());

	if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
	    TemporalExtent extent = new TemporalExtent();
	    extent.setBeginPosition(resolution.getDataFromTime());
	    extent.setEndPosition(resolution.getDataToTime());
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	    setIndeterminatePosition(dataset);

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    Long longResTime = null;
	    try {

		boolean instantaneus = false;
		if (restime == null || restime.isEmpty()) {
		    longResTime = 1440l;
		} else {
		    longResTime = Long.parseLong(restime);
		    if (longResTime == 0) {
			instantaneus = true;
			longResTime = 1440l;
		    }
		}
		Date begin = optionalBegin.get();
		Date end = optionalEnd.get();

		long totalMinutes = (end.getTime() - begin.getTime()) / (60000);

		long expectedSize = totalMinutes / longResTime;

		time.setDimensionSize(new BigInteger("" + expectedSize));
		ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		extensionHandler.setDataSize(Long.valueOf(expectedSize));
		grid.addAxisDimension(time);
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

		String variableName = series.getParameterName();
		String resInfo = "";
		if (longResTime != null) {
		    if (instantaneus) {
			resInfo = " (resolution not available)";
		    } else if (longResTime == 1440l) {
			resInfo = " (1 day resolution)";
		    } else {
			resInfo = " (" + longResTime + " minutes resolution)";
		    }

		}
		coreMetadata.setTitle((variableName + " acquisitions at NVE station " + station.getName() + resInfo));
		coreMetadata.setAbstract("This dataset contains a hydrology time series of a specific variable (" + variableName
			+ ") acquired by NVE station " + station.getName() + ". " + resInfo);

		coreMetadata.getMIMetadata().getDataIdentification().addKeyword("NVE station " + station.getId());

		coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

		coreMetadata.addBoundingBox(station.getLatitude(), station.getLongitude(), station.getLatitude(), station.getLongitude());

		ResponsibleParty creatorContact = new ResponsibleParty();

		creatorContact.setOrganisationName("The Norwegian Water Resources and Energy Directorate");
		creatorContact.setRoleCode("author");
		Contact contact = new Contact();
		Address address = new Address();
		address.addElectronicMailAddress("hydrology@nve.no");
		contact.setAddress(address);
		creatorContact.setContactInfo(contact);
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

		/**
		 * MIPLATFORM
		 **/

		MIPlatform platform = new MIPlatform();

		platform.setMDIdentifierCode(NVE_URN + stationId);

		platform.setDescription(stationName);

		Citation platformCitation = new Citation();
		platformCitation.setTitle(stationName);
		platform.setCitation(platformCitation);

		coreMetadata.getMIMetadata().addMIPlatform(platform);

		/**
		 * COVERAGEDescription
		 **/

		CoverageDescription coverageDescription = new CoverageDescription();

		coverageDescription.setAttributeIdentifier(NVE_URN + "variable:" + parameterId + ":" + restime);
		coverageDescription.setAttributeTitle(variableName);

		String attributeDescription = variableName + " Units: " + series.getUnit() + " Resolution: " + restime + " seconds";

		coverageDescription.setAttributeDescription(attributeDescription);
		coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

		if (method == null) {
		    method = "";
		}
		switch (method) {
		case "Mean":
		    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.AVERAGE);
		    break;
		case "Minimum":
		    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.MIN);
		    break;
		case "Maximum":
		    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.MAX);
		    break;
		case "Instantaneous":
		    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);
		    break;
		default:
		    GSLoggerFactory.getLogger(getClass()).warn("Unexpected method: " + method);
		    break;
		}

		if (restime != null && !restime.equals("")) {
		    dataset.getExtensionHandler().setTimeSupport(restime);
		    dataset.getExtensionHandler().setTimeUnits("minutes");
		    dataset.getExtensionHandler().setTimeUnitsAbbreviation("min");
		}

		dataset.getExtensionHandler().setAttributeUnits(series.getUnit());

		dataset.getExtensionHandler().setAttributeUnitsAbbreviation(series.getUnit());

		/**
		 * ONLINE
		 */

		Online online = new Online();
		online.setIdentifier();
		online.setLinkage(endpoint);
		online.setName(id);
		online.setProtocol(CommonNameSpaceContext.NVE_URI);
		online.setFunctionCode("download");

		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

		LegalConstraints lc = new LegalConstraints();
		lc.addOtherConstraints("Norwegian Licence for Open Government Data (NLOD)");
		coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(lc);
		dataset.getExtensionHandler().setCountry(Country.NORWAY.getShortName());

		addStaticAccesReport(dataset, online.getIdentifier());
	    } catch (Exception e) {
	    }

	}

    }

    private void addStaticAccesReport(Dataset resource, String onlineId) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Current online id: {}", onlineId);

	DataDownloader downloader = DataDownloaderFactory.getDataDownloader(resource, onlineId);

	if (downloader == null) {

	    GSLoggerFactory.getLogger(getClass()).error("No DataDownloader found");
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Static augmentation STARTED");

	//
	// get the descriptors
	//
	List<DataDescriptor> fullList = downloader.getRemoteDescriptors();
	List<DataDescriptor> previewList = downloader.getPreviewRemoteDescriptors(fullList);

	fullList.sort(new DescriptorComparator());
	previewList.sort(new DescriptorComparator());

	DataDescriptor full = fullList.get(0);
	DataDescriptor preview = previewList.get(0);

	//
	// creates the static report
	//
	DataComplianceReport report = new DataComplianceReport(onlineId, null);
	report.setDescriptors(preview, full);

	report.setTargetComplianceLevel(DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);
	report.setLastSucceededTest(DataComplianceTest.EXECUTION);
	report.setTargetTest(DataComplianceTest.EXECUTION);

	report.setDownloadable(true);
	report.setDownloadTime(1000);

	ValidationMessage validationMessage = new ValidationMessage();
	validationMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	report.setExecutionResult(validationMessage);
	report.setExecutionTime(1000);

	//
	// set the properties
	//
	String timeStamp = report.getTimeStamp();

	ResourcePropertyHandler propertyHandler = resource.getPropertyHandler();
	propertyHandler.setTestTimeStamp(timeStamp);

	resource.getPropertyHandler().setIsTransformable(true);
	resource.getPropertyHandler().setIsDownloadable(true);
	resource.getPropertyHandler().setIsExecutable(true);

	resource.getPropertyHandler().addDownloadTime(report.getDownloadTime().get());
	resource.getPropertyHandler().addExecutionTime(report.getExecutionTime().get());

	resource.getPropertyHandler().addComplianceLevel(DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE.getLabel());

	resource.getPropertyHandler().setIsTimeseries(true);

	//
	// adds the report
	//

	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);

	handler.clearReports();

	handler.addReport(report);

	GSLoggerFactory.getLogger(getClass()).debug("Static augmentation ENDED");

    }

}
