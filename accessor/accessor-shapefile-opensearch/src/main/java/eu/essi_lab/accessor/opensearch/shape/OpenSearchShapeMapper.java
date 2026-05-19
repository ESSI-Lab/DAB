package eu.essi_lab.accessor.opensearch.shape;

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

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.HashMap;

import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.accessor.s3.FeatureMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourcePropertyHandler;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * @author boldrini
 */
public class OpenSearchShapeMapper extends FileIdentifierMapper {

    public static final String ONLINE_PROTOCOL = "OPENSEARCH-SHAPE";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.OPENSEARCH_SHAPEFILE;
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
	    if (title == null || title.isEmpty()) {
		title = attributes.get("entryName");
	    }
	    if (title == null || title.isEmpty()) {
		title = feature.getId();
	    }

	    coreMetadata.setTitle(title);
	    coreMetadata.setAbstract("Shape feature from OpenSearch at " + feature.getUrl());

	    for (String name : attributes.keySet()) {
		if (!"entryName".equals(name)) {
		    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(attributes.get(name));
		}
	    }

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    BigDecimal north = feature.getNorth();
	    BigDecimal west = feature.getWest();
	    BigDecimal south = feature.getSouth();
	    BigDecimal east = feature.getEast();
	    coreMetadata.addBoundingBox(north, west, south, east);

	    coreMetadata.addDistributionOnlineResource(feature.getId(), feature.getUrl(), ONLINE_PROTOCOL, "download");
	    Online online = coreMetadata.getOnline();
	    online.setIdentifier(metadataId);

	    addGridReport(dataset, metadataId, north, west, south, east);
	    addVectorReport(dataset, metadataId, north, west, south, east);

	    ResourcePropertyHandler propertyHandler = dataset.getPropertyHandler();
	    propertyHandler.setIsTransformable(true);
	    propertyHandler.setIsDownloadable(true);
	    propertyHandler.setIsExecutable(true);
	    propertyHandler.setIsVector(true);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void addGridReport(Dataset dataset, String metadataId, BigDecimal north, BigDecimal west, BigDecimal south, BigDecimal east) {

	DataComplianceReport report = new DataComplianceReport(metadataId, null);
	DataDescriptor desc = new DataDescriptor();
	desc.setCRS(CRS.EPSG_3857());
	desc.setDataFormat(DataFormat.IMAGE_PNG());
	desc.setDataType(DataType.GRID);
	desc.setEPSG4326SpatialDimensions(north.doubleValue(), east.doubleValue(), south.doubleValue(), west.doubleValue());
	report.setDescriptors(desc, desc);
	report.setTargetComplianceLevel(DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE);
	report.setLastSucceededTest(DataComplianceTest.EXECUTION);
	report.setTargetTest(DataComplianceTest.EXECUTION);
	report.setDownloadable(true);
	report.setDownloadTime(1000);
	ValidationMessage validationMessage = new ValidationMessage();
	validationMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	report.setExecutionResult(validationMessage);
	report.setExecutionTime(1000);

	ReportsMetadataHandler handler = new ReportsMetadataHandler(dataset);
	handler.clearReports();
	handler.addReport(report);

	ResourcePropertyHandler propertyHandler = dataset.getPropertyHandler();
	propertyHandler.setTestTimeStamp(report.getTimeStamp());
	propertyHandler.addDownloadTime(report.getDownloadTime().get());
	propertyHandler.addExecutionTime(report.getExecutionTime().get());
	propertyHandler.addComplianceLevel(DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE.getLabel());
    }

    private void addVectorReport(Dataset dataset, String metadataId, BigDecimal north, BigDecimal west, BigDecimal south,
	    BigDecimal east) {

	DataComplianceReport report = new DataComplianceReport(metadataId, null);
	DataDescriptor desc = new DataDescriptor();
	desc.setCRS(CRS.EPSG_4326());
	desc.setDataFormat(DataFormat.WKT());
	desc.setDataType(DataType.VECTOR);
	desc.setEPSG4326SpatialDimensions(north.doubleValue(), east.doubleValue(), south.doubleValue(), west.doubleValue());
	report.setDescriptors(desc, desc);
	report.setTargetComplianceLevel(DataComplianceLevel.VECTOR_BASIC_DATA_COMPLIANCE);
	report.setLastSucceededTest(DataComplianceTest.EXECUTION);
	report.setTargetTest(DataComplianceTest.EXECUTION);
	report.setDownloadable(true);
	report.setDownloadTime(1000);
	ValidationMessage validationMessage = new ValidationMessage();
	validationMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	report.setExecutionResult(validationMessage);
	report.setExecutionTime(1000);

	ReportsMetadataHandler handler = new ReportsMetadataHandler(dataset);
	handler.addReport(report);
	propertyHandlerCompliance(dataset, report);
    }

    private void propertyHandlerCompliance(Dataset dataset, DataComplianceReport report) {
	ResourcePropertyHandler propertyHandler = dataset.getPropertyHandler();
	propertyHandler.addDownloadTime(report.getDownloadTime().get());
	propertyHandler.addExecutionTime(report.getExecutionTime().get());
	propertyHandler.addComplianceLevel(DataComplianceLevel.VECTOR_BASIC_DATA_COMPLIANCE.getLabel());
    }
}
