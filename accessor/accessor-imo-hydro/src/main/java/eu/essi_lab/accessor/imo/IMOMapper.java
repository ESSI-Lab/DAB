package eu.essi_lab.accessor.imo;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author boldrini
 */
public class IMOMapper extends OriginalIdentifierMapper {

    private static final String IMO_URN = "urn:is:vedur:customer:hycos";
    private static final String IMO_MAPPING_ERROR = "IMO_MAPPING_ERROR";
    private Logger logger = GSLoggerFactory.getLogger(getClass());
    private String endpoint;

    public IMOMapper() {
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
	return CommonNameSpaceContext.IMO_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String metadata = originalMD.getMetadata();

	List<ZRXPBlock> blocks;

	try {
	    File file = File.createTempFile(getClass().getSimpleName(), ".zrxp");

	    ByteArrayInputStream bais = new ByteArrayInputStream(metadata.getBytes());
	    FileOutputStream fos;

	    fos = new FileOutputStream(file);

	    IOUtils.copy(bais, fos);
	    fos.close();
	    bais.close();

	    ZRXPDocument doc = new ZRXPDocument(file);

	    blocks = doc.getBlocks();

	    file.deleteOnExit();
	    file.delete();

	} catch (IOException e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IMO_MAPPING_ERROR, //
		    e);
	}

	Date begin = null;
	Date end = null;

	long size = 0;

	for (ZRXPBlock block : blocks) {
	    SimpleEntry<Date, Double> first = block.getDataValues().get(0);
	    SimpleEntry<Date, Double> last = block.getDataValues().get(block.getDataValues().size() - 1);
	    if (begin == null || begin.after(first.getKey())) {
		begin = first.getKey();
	    }
	    if (end == null || end.before(last.getKey())) {
		end = last.getKey();
	    }
	    size += block.getDataValues().size();
	}

	ZRXPBlock block = blocks.get(0);

	String parameterName = block.getParameterName();
	String riverName = block.getRiverName();
	String stationId = block.getStationIdentifier();
	String stationName = block.getStationName();
	String unit = block.getUnit();

	IMOIdentifierMangler mangler = new IMOIdentifierMangler();
	mangler.setPlatformIdentifier(stationId);
	mangler.setParameterIdentifier(parameterName);

	String id = mangler.getMangling();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	if (begin != null && end != null) {
	    TemporalExtent extent = new TemporalExtent();
	    extent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(begin));
	    extent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(end));
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	    setIndeterminatePosition(dataset);

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    try {

		time.setDimensionSize(new BigInteger("" + size));
		ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		extensionHandler.setDataSize(size);
		grid.addAxisDimension(time);
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	    } catch (Exception e) {
	    }

	}

	coreMetadata.setTitle((parameterName + " acquisitions at IMO station " + stationName));
	coreMetadata.setAbstract("This dataset contains a hydrology time series of a specific variable (" + parameterName
		+ ") acquired by IMO station " + stationName + ". The river is " + riverName);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("IMO station " + stationName);

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	Double lat = Double.parseDouble(block.getLatitude());
	Double lon = Double.parseDouble(block.getLongitude());
	coreMetadata.addBoundingBox(lat, lon, lat, lon);

	ResponsibleParty creatorContact = new ResponsibleParty();

	creatorContact.setOrganisationName("Icelandic Meteorological Office");
	creatorContact.setRoleCode("author");
	Contact contact = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress("jorunn@vedur.is");
	contact.setAddress(address);
	creatorContact.setContactInfo(contact);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(IMO_URN + stationId);

	platform.setDescription(stationId);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(IMO_URN + ":variable:" + parameterName);
	coverageDescription.setAttributeTitle(parameterName);

	String attributeDescription = parameterName + " Units: " + unit;

	dataset.getExtensionHandler().setAttributeUnits(unit);
	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unit);

	dataset.getExtensionHandler().setCountry(Country.ICELAND.getShortName());

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	// dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);

	/**
	 * ONLINE
	 */

	Online onlineValues = new Online();
	onlineValues.setLinkage(endpoint);
	onlineValues.setName(id);
	onlineValues.setProtocol(CommonNameSpaceContext.IMO_URI);
	onlineValues.setFunctionCode("download");

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(onlineValues);

	// LegalConstraints lc = new LegalConstraints();
	// lc.addOtherConstraints("Norwegian Licence for Open Government Data (NLOD)");
	// coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(lc);

    }

}
