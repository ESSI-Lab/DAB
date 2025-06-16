package eu.essi_lab.accessor.s3;

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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;

import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
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
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourcePropertyHandler;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author boldrini
 */
public class ShapeMapper extends FileIdentifierMapper {

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HIS_CENTRAL_SHAPEFILE;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;

    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {
	ByteArrayInputStream bais = new ByteArrayInputStream(originalMD.getMetadata().getBytes());
	try {
	    FeatureMetadata feature = FeatureMetadata.unmarshal(bais);
	    String metadataId = feature.getUrl() + ":" + feature.getId();
	    dataset.setOriginalId(metadataId);
	    dataset.setPrivateId(metadataId);
	    dataset.setPublicId(metadataId);

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.setIdentifier(metadataId);

	    HashMap<String, String> attributes = feature.getAttributes();

	    String title = attributes.get("distretti");
	    if (title == null || title.isEmpty()) {
		title = attributes.get("euuomname");
	    }

	    coreMetadata.setTitle(title);
	    coreMetadata.setAbstract("Shape file at " + feature.getUrl());

	    for (String name : attributes.keySet()) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(attributes.get(name));
	    }

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    BigDecimal north = feature.getNorth();
	    BigDecimal west = feature.getWest();
	    BigDecimal south = feature.getSouth();
	    BigDecimal east = feature.getEast();
	    coreMetadata.addBoundingBox(north, west, south, east);

	    coreMetadata.addDistributionOnlineResource(feature.getId(), feature.getUrl(), "HTTP-SHAPE", "download");
	    Online online = coreMetadata.getOnline();
	    online.setIdentifier(metadataId);

	    DataComplianceReport report = new DataComplianceReport(metadataId, null);
	    DataDescriptor desc = new DataDescriptor();
	    desc.setCRS(CRS.EPSG_3857());
	    desc.setDataFormat(DataFormat.IMAGE_PNG());
	    desc.setDataType(DataType.GRID);
	    desc.setEPSG4326SpatialDimensions(north.doubleValue(), east.doubleValue(), south.doubleValue(), west.doubleValue());
	    report.setDescriptors(desc , desc);

	    report.setTargetComplianceLevel(DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE);
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

	    ResourcePropertyHandler propertyHandler = dataset.getPropertyHandler();
	    propertyHandler.setTestTimeStamp(timeStamp);

	    propertyHandler.setIsTransformable(true);
	    propertyHandler.setIsDownloadable(true);
	    propertyHandler.setIsExecutable(true);

	    propertyHandler.addDownloadTime(report.getDownloadTime().get());
	    propertyHandler.addExecutionTime(report.getExecutionTime().get());

	    propertyHandler.addComplianceLevel(DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE.getLabel());

	    propertyHandler.setIsVector(true);

	    //
	    // adds the report
	    //

	    ReportsMetadataHandler handler = new ReportsMetadataHandler(dataset);

	    handler.clearReports();

	    handler.addReport(report);

	    DataComplianceReport report2 = new DataComplianceReport(metadataId, null);
	    DataDescriptor desc2 = new DataDescriptor();
	    desc2.setCRS(CRS.EPSG_4326());
	    desc2.setDataFormat(DataFormat.WKT());
	    desc2.setDataType(DataType.VECTOR);
	    desc2.setEPSG4326SpatialDimensions(north.doubleValue(), east.doubleValue(), south.doubleValue(), west.doubleValue());
	    report2.setDescriptors(desc2 , desc2);

	    report2.setTargetComplianceLevel(DataComplianceLevel.VECTOR_BASIC_DATA_COMPLIANCE);
	    report2.setLastSucceededTest(DataComplianceTest.EXECUTION);
	    report2.setTargetTest(DataComplianceTest.EXECUTION);

	    report2.setDownloadable(true);
	    report2.setDownloadTime(1000);

	    ValidationMessage validationMessage2 = new ValidationMessage();
	    validationMessage2.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	    report2.setExecutionResult(validationMessage2);
	    report2.setExecutionTime(1000);
	    
	    handler.addReport(report2);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

}
