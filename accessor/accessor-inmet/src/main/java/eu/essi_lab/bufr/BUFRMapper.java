package eu.essi_lab.bufr;

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
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.bufr.datamodel.BUFRCollection;
import eu.essi_lab.bufr.datamodel.BUFRElement;
import eu.essi_lab.bufr.datamodel.BUFRRecord;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author boldrini
 */
public class BUFRMapper extends OriginalIdentifierMapper {

    private static final String BUFR_MAPPER_NO_VARIABLES_FOUND_ERROR = "BUFR_MAPPER_NO_VARIABLES_FOUND_ERROR";
    private static final String BUFR_MAPPER_MAPPING_ERROR = "BUFR_MAPPER_MAPPING_ERROR";
    
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private String endpoint;

    public BUFRMapper() {
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
	return CommonNameSpaceContext.BUFR_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	try {
	    String originalMetadata = originalMD.getMetadata();

	    ByteArrayInputStream bais = new ByteArrayInputStream(originalMetadata.getBytes());
	    BUFRCollection collection = BUFRCollection.unmarshal(bais);
	    try {
		bais.close();
	    } catch (IOException e1) {
		e1.printStackTrace();
	    }

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    List<BUFRRecord> records = collection.getRecords();
	    BUFRRecord first = records.get(0);
	    BUFRRecord last = records.get(collection.getRecords().size() - 1);

	    // String units = null;
	    String kvpValues = "";

	    for (BUFRRecord record : records) {
		try {
		    BUFRElement element = record.identifyVariables().get(0);
		    // units = element.getUnits();
		    BigDecimal dataValue = new BigDecimal(element.getValue());
		    kvpValues += dataValue + ";";
		    Date date = record.getTime();
		    kvpValues += date.getTime() + ";";
		} catch (Exception e) {
		}
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().setSupplementalInformation(kvpValues);

	    TemporalExtent extent = new TemporalExtent();
	    extent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(first.getTime()));
	    extent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(last.getTime()));
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	    BUFRElement heightElement = first.getStationHeight();
	    if (heightElement != null) {
		VerticalExtent verticalExtent = new VerticalExtent();
		Double vertical = Double.parseDouble(heightElement.getValue());
		verticalExtent.setMinimumValue(vertical);
		verticalExtent.setMaximumValue(vertical);

		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	    }

	    //
	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    try {
		String expectedSize = "" + collection.getRecords().size();
		time.setDimensionSize(new BigInteger(expectedSize));
		ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		extensionHandler.setDataSize(Long.valueOf(expectedSize));
	    } catch (Exception e) {
	    }
	    grid.addAxisDimension(time);
	    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	    List<BUFRElement> variables = first.identifyVariables();
	    if (variables.isEmpty()) {

		throw GSException.createException(//
			getClass(), //
			"No variables found", //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			BUFR_MAPPER_NO_VARIABLES_FOUND_ERROR); //
	    }

	    BUFRElement variable = variables.get(0); // out of bounds
	    String variableName = variable.getName();

	    String variableUnits = variable.getUnits();

	    dataset.getExtensionHandler().setAttributeUnits(variableUnits);

	    coreMetadata.setTitle((variableName + " at station " + first.getStationOrSiteName()));
	    coreMetadata.setAbstract("This dataset contains a hydrology time series of a specific variable (" + variableName
		    + ") acquired at station " + first.getStationOrSiteName() + " WMO station number " + first.getWMOStationNumber()
		    + " (WMO Block number " + first.getWMOBlockNumber() + ")");

	    dataset.getExtensionHandler().setTimeInterpolation(variable.getInterpolationType());
	    if (variable.getTimeSupport() != null) {
		dataset.getExtensionHandler().setTimeSupport(variable.getTimeSupport().getValue());
		dataset.getExtensionHandler().setTimeUnits(variable.getTimeSupport().getUnits());
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("WMO station number " + first.getWMOStationNumber());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("WMO block number " + first.getWMOBlockNumber());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Type of station: " + first.getTypeOfStation());

	    BUFRIdentifierMangler mangler = new BUFRIdentifierMangler();
	    mangler.setPlatformIdentifier("urn:wmo:" + first.getWMOBlockNumber() + "-" + first.getWMOStationNumber());
	    mangler.setParameterIdentifier(variableName);

	    String id = mangler.getMangling();

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    Double latitude = first.getLatitude();
	    Double longitude = first.getLongitude();

	    if (latitude > 90 || longitude > 360) {

		logger.error("Wrong coordinates! Printing record.");

		first.marshal(System.out);

		logger.error("Print end");
	    }

	    coreMetadata.addBoundingBox(latitude, longitude, latitude, longitude);

	    ResponsibleParty creatorContact = new ResponsibleParty();

	    creatorContact.setOrganisationName(first.getBUFRCenterName());
	    creatorContact.setRoleCode("author");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	    /**
	     * MIPLATFORM
	     **/

	    MIPlatform platform = new MIPlatform();

	    String platformIdentifier = "urn:wmo:" + first.getWMOBlockNumber() + first.getWMOStationNumber();

	    platform.setMDIdentifierCode(platformIdentifier);

	    platform.setDescription(platformIdentifier);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(first.getStationOrSiteName());
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    /**
	     * COVERAGEDescription
	     **/

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier("urn:wmo:" + variableName);
	    coverageDescription.setAttributeTitle(variableName);

	    coverageDescription.setAttributeDescription(variableName);
	    dataset.getExtensionHandler().setAttributeUnits(variable.getUnits());
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    /**
	     * ONLINE
	     */

	    Online onlineValues = new Online();
	    onlineValues.setLinkage(endpoint);
	    onlineValues.setName(id);
	    onlineValues.setProtocol(CommonNameSpaceContext.BUFR_URI);
	    onlineValues.setFunctionCode("download");

	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(onlineValues);
	    
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null,
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BUFR_MAPPER_MAPPING_ERROR, //
		    e); //
	}
    }

}
