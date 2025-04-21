package eu.essi_lab.accessor.waf.onamet;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.iso19139.gco.v_20060504.MemberNamePropertyType;
import net.opengis.iso19139.gco.v_20060504.MemberNameType;
import net.opengis.iso19139.gco.v_20060504.TypeNamePropertyType;
import net.opengis.iso19139.gco.v_20060504.TypeNameType;
import net.opengis.iso19139.gco.v_20060504.UomLengthPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDBandType;
import net.opengis.iso19139.gmd.v_20060504.MDContentInformationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDCoverageDescriptionType;
import net.opengis.iso19139.gmd.v_20060504.MDRangeDimensionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.dt.GridDataset;
import ucar.unidata.geoloc.LatLonRect;

/**
 * @author Fabrizio
 */
public class ONAMETMapper extends FileIdentifierMapper {

    /**
     * 
     */
    public static final String ONAMET_METADATA_SCHEMA = "ONAMET_METADATA_SCHEMA";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return ONAMET_METADATA_SCHEMA;
    }

    /**
     * @param ncFilePath
     * @param THREDDSUrl
     * @param THREDDSSubFolder
     * @param currentDirectoryPath
     * @param extractionTarget
     * @return
     */
    public static String createOriginalMetadata(//
	    String ncFilePath, //
	    String THREDDSUrl, //
	    String THREDDSSubFolder, //
	    String currentDirectoryPath, //
	    String extractionTarget) {

	JSONObject object = new JSONObject();

	object.put("ncPath", ncFilePath);
	object.put("THREDDSUrl", THREDDSUrl);
	object.put("THREDDSSubFolder", THREDDSSubFolder);
	object.put("extractionTarget", extractionTarget);

	currentDirectoryPath = currentDirectoryPath.substring(0, currentDirectoryPath.indexOf("_"));
	object.put("dirName", currentDirectoryPath);

	return object.toString();
    }

    /**
     * @param resource
     * @return
     */
    static String readDirectoryName(GSResource resource) {

	JSONObject object = new JSONObject(resource.getOriginalMetadata().getMetadata());

	return object.getString("dirName");
    }

    /**
     * @param resource
     * @return
     */
    static String readNcFilePath(GSResource resource) {

	return readNcFilePath(resource.getOriginalMetadata().getMetadata());
    }

    /**
     * @param metadata
     * @return
     */
    static String readNcFilePath(String metadata) {

	JSONObject object = new JSONObject(metadata);

	return object.getString("ncPath");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject object = new JSONObject(originalMD.getMetadata());

//	String dirName = object.getString("dirName");

	String ncPath = object.getString("ncPath");
	String threddsURL = object.getString("THREDDSUrl");

	String subFolder = object.getString("THREDDSSubFolder");
	if (!subFolder.isEmpty()) {
	    subFolder = subFolder + "/";
	}

	if (!threddsURL.endsWith("/")) {
	    threddsURL += "/";
	}

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	String fileName = new File(ncPath).getName();

	dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(fileName);

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");

	try {
	    NetcdfDataset netCDFdataset = NetcdfDataset.openDataset(ncPath);

	    GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(//
		    netCDFdataset.getLocation(), //
		    EnumSet.of(Enhance.ApplyScaleOffset, Enhance.CoordSystems, Enhance.ConvertEnums));

	    setTitle(netCDFdataset, dataset);

	    setTopicCategory(dataset);

	    setAbstract(netCDFdataset, dataset);

	    setSpatialExtent(dataset, gds);

	    setTemporalExtent(netCDFdataset, dataset);

	    setDistributionOnline(netCDFdataset, dataset, threddsURL, subFolder, fileName);

	    setDistributionFormat(netCDFdataset, dataset);

	    setKeywords(netCDFdataset, dataset);

	    setContentInfo(netCDFdataset, dataset);

	    //
	    //
	    //

	    gds.close();

	    netCDFdataset.release();
	    netCDFdataset.close();

	} catch (Exception e) {

	    //
	    // it seems that sometimes during the files extraction, the server goes down and
	    // this content is put in the extracted files:
	    //
	    // <!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
	    // <html><head>
	    // <title>404 Not Found</title>
	    // </head><body>
	    // <h1>Not Found</h1>
	    // <p>The requested URL was not found on this server.</p>
	    // <hr>
	    // <address>Apache/2.4.29 (Ubuntu) Server at 186.149.199.244 Port 80</address>
	    // </body></html>
	    //

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during mapping of nc file: {}", ncPath);

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    //
	    // these records cannot be discovered and the related nc file must be removed from s3
	    // here we put just the "nc file corrupted flag", then the augmenter marks the resource as deleted
	    //

	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(ncPath);

	    dataset.getHarmonizedMetadata().getCoreMetadata().setAbstract("Unable to open nc file: " + e.getMessage());

	    dataset.getExtensionHandler().setIsNCFileCorrupted();
	}

	return dataset;
    }

    /**
     * @param netCDFdataset
     * @param dataset
     */
    private void setTitle(NetcdfDataset netCDFdataset, Dataset dataset) {

	List<Attribute> globalAttributes = netCDFdataset.getGlobalAttributes();

	String title = globalAttributes.//
		stream().//
		filter(a -> a.getShortName().equals("TITLE")).map(a -> a.getStringValue().trim()).//
		findFirst().//
		get();

//	String startDate = globalAttributes.//
//		stream().//
//		filter(a -> a.getShortName().equals("START_DATE")).map(a -> a.getStringValue().trim()).//
//		findFirst().//
//		get().//
//		replace("_", " ");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.setTitle(title);
    }

    /**
     * @param dataset
     */
    private void setTopicCategory(Dataset dataset) {

	MDTopicCategoryCodePropertyType mdTopicCategoryCodePropertyType = new MDTopicCategoryCodePropertyType();
	mdTopicCategoryCodePropertyType.setMDTopicCategoryCode(MDTopicCategoryCodeType.CLIMATOLOGY_METEOROLOGY_ATMOSPHERE);

	dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getElementType()
		.setTopicCategory(Arrays.asList(mdTopicCategoryCodePropertyType));
    }

    /**
     * @param netCDFdataset
     * @param dataset
     */
    private void setAbstract(NetcdfDataset netCDFdataset, Dataset dataset) {

	List<Variable> geoVariables = NetCDFUtils.getGeographicVariables(netCDFdataset);

	List<Variable> variables = netCDFdataset.getVariables();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

    }

    /**
     * @param netCDFdataset
     * @param dataset
     * @param gds
     * @throws IOException
     */
    private void setSpatialExtent(Dataset dataset, GridDataset gds) throws IOException {

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	@SuppressWarnings("deprecation")
	LatLonRect datasetBbox = gds.getBoundingBox();

	// GridDatatype grid = gds.findGridDatatype("LU_INDEX");
	// GridCoordSystem gcs = grid.getCoordinateSystem();
	//
	// LatLonRect latLonBoundingBox = gcs.getLatLonBoundingBox();
	//
	// double west = latLonBoundingBox.getLonMin();
	// double south = latLonBoundingBox.getLatMin();
	// double east = latLonBoundingBox.getLonMax();
	// double north = latLonBoundingBox.getLatMax();

	double west = datasetBbox.getLonMin();
	double south = datasetBbox.getLatMin();
	double east = datasetBbox.getLonMax();
	double north = datasetBbox.getLatMax();

	coreMetadata.addBoundingBox(north, west, south, east);
    }

    /**
     * @param netCDFdataset
     * @param dataset
     * @throws IOException
     */
    private void setTemporalExtent(NetcdfDataset netCDFdataset, Dataset dataset) throws IOException {

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	List<CoordinateAxis> axes = netCDFdataset.getCoordinateAxes();

	CoordinateAxis coordinateAxis = axes.//
		stream().//
		filter(axe -> axe.getAxisType() != null && axe.getAxisType().equals(AxisType.Time)).//
		findFirst().//
		get();

	CoordinateAxis1DTime timeAxis = CoordinateAxis1DTime.factory(netCDFdataset, coordinateAxis, null);

	long min = timeAxis.getCalendarDateRange().getStart().getMillis();
	long max = timeAxis.getCalendarDateRange().getEnd().getMillis();

	String beginPosition = ISO8601DateTimeUtils.getISO8601DateTime(new Date(min));
	String endPosition = ISO8601DateTimeUtils.getISO8601DateTime(new Date(max));

	coreMetadata.addTemporalExtent(beginPosition, endPosition);
    }

    /**
     * @param netCDFdataset
     * @param dataset
     * @param threddsURL
     * @param threddsSubFolder
     * @param fileName
     */
    private void setDistributionOnline(//
	    NetcdfDataset netCDFdataset, //
	    Dataset dataset, //
	    String threddsURL, //
	    String threddsSubFolder, //
	    String fileName) {

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.addDistributionOnlineResource(//
		fileName, //
		threddsURL + "dodsC/data/all/" + threddsSubFolder + fileName + ".html", //
		NetProtocols.OPENDAP.getCommonURN(), //
		"download");

	coreMetadata.addDistributionOnlineResource(//
		fileName, //
		threddsURL + "fileServer/data/all/" + threddsSubFolder + fileName, //
		NetProtocols.HTTP.getCommonURN(), //
		"download");

	String wcsQuery = "service=WCS&version=1.0.0&request=GetCapabilities";

	coreMetadata.addDistributionOnlineResource(//
		fileName, //
		threddsURL + "wcs/data/all/" + threddsSubFolder + fileName + "?" + wcsQuery, //
		NetProtocols.WCS_1_0_0.getCommonURN(), //
		"download");

	// String wmsQuery = "service=WMS&version=1.3.0&request=GetCapabilities";
	//
	// coreMetadata.addDistributionOnlineResource(//
	// fileName, //
	// threddsURL + "wms/data/all/" + threddsSubFolder + fileName + "?" + wmsQuery, //
	// NetProtocols.WMS_1_3_0.getCommonURN(), //
	// "download");
	//

	////

	List<String> geoVarNames = get2DVariableNames(netCDFdataset);

	geoVarNames.forEach(name -> {

	    coreMetadata.addDistributionOnlineResource(//
		    name, //
		    threddsURL + "wms/data/all/" + threddsSubFolder + fileName + "?", //
		    NetProtocols.WMS_1_3_0.getCommonURN(), //
		    "download");
	});

	////

	coreMetadata.addDistributionOnlineResource(//
		fileName, //
		threddsURL + "ncss/grid/data/all/" + threddsSubFolder + fileName + "/dataset.html", //
		NetProtocols.NETCDFSubset.getCommonURN(), //
		"download");

	String catalogQuery = encodeURL("catalog=" + threddsURL + "catalog/data/all/catalog.html&dataset=datasetScan/" + fileName);

	// the server also adds the query part here, but it produces an invalid XML
	coreMetadata.addDistributionOnlineResource(//
		fileName, //
		threddsURL + "iso/data/all/" + threddsSubFolder + fileName, //
		NetProtocols.ISO.getCommonURN(), //
		"download");

	coreMetadata.addDistributionOnlineResource(//
		fileName, //
		threddsURL + "ncml/data/all/" + threddsSubFolder + fileName + "?" + catalogQuery, //
		NetProtocols.NCML.getCommonURN(), //
		"download");

	coreMetadata.addDistributionOnlineResource(//
		fileName, //
		threddsURL + "uddc/data/all/" + threddsSubFolder + fileName + "?" + catalogQuery, //
		NetProtocols.UDDC.getCommonURN(), //
		"download");
    }

    /**
     * @param netCDFdataset
     * @param dataset
     */
    private void setDistributionFormat(NetcdfDataset netCDFdataset, Dataset dataset) {

	List<Variable> variables = netCDFdataset.getVariables();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
    }

    /**
     * @param netCDFdataset
     * @param dataset
     */
    private void setKeywords(NetcdfDataset netCDFdataset, Dataset dataset) {

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	List<Variable> variables = netCDFdataset.getVariables();

	variables.//
		stream().//
		filter(v -> v.getDescription() != null).//
		filter(v -> !v.getDescription().isEmpty()).//
		filter(v -> !v.getDescription().equals("-")).//
		map(v -> v.getDescription().trim().replace("\n", "")).//
		forEach(d -> coreMetadata.getMIMetadata().getDataIdentification().addKeyword(d));
    }

    /**
     * @param netCDFdataset
     * @param dataset
     */
    private void setContentInfo(NetcdfDataset netCDFdataset, Dataset dataset) {

	MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	MDCoverageDescriptionType descriptionType = new MDCoverageDescriptionType();

	JAXBElement<MDCoverageDescriptionType> element = ObjectFactories.GMD().createMDCoverageDescription(descriptionType);

	List<MDRangeDimensionPropertyType> dimension = descriptionType.getDimension();

	List<Variable> variables = netCDFdataset.getVariables();

	variables.forEach(v -> {

	    MDRangeDimensionPropertyType propertyType = new MDRangeDimensionPropertyType();

	    MDBandType mdBandType = new MDBandType();

	    MemberNamePropertyType memberNamePropertyType = new MemberNamePropertyType();

	    mdBandType.setSequenceIdentifier(memberNamePropertyType);

	    propertyType.setMDRangeDimension(ObjectFactories.GMD().createMDBand(mdBandType));

	    MemberNameType memberNameType = new MemberNameType();

	    memberNamePropertyType.setMemberName(memberNameType);

	    //

	    String fullName = v.getFullName();

	    memberNameType.setAName(ISOMetadata.createCharacterStringPropertyType(fullName));

	    //

	    String dataType = v.getDataType().toString();

	    TypeNamePropertyType typeNamePropertyType = new TypeNamePropertyType();

	    TypeNameType typeNameType = new TypeNameType();

	    typeNameType.setAName(ISOMetadata.createCharacterStringPropertyType(dataType));

	    typeNamePropertyType.setTypeName(typeNameType);

	    memberNameType.setAttributeType(typeNamePropertyType);

	    //

	    String unitsString = v.getUnitsString();

	    if (unitsString != null) {

		UomLengthPropertyType uomLengthPropertyType = new UomLengthPropertyType();

		uomLengthPropertyType.setHref(unitsString);

		mdBandType.setUnits(uomLengthPropertyType);
	    }

	    dimension.add(propertyType);
	});

	MDContentInformationPropertyType type = new MDContentInformationPropertyType();
	type.setAbstractMDContentInformation(element);

	miMetadata.getElement().getValue().setContentInfo(Arrays.asList(type));
    }

    /**
     * @param netCDFdataset
     * @return
     */
    public  static List<String> get2DVariableNames(NetcdfDataset netCDFdataset) {

	Set<Variable> out = new HashSet<>();

	for (Variable variable : netCDFdataset.getVariables()) {

	    List<Dimension> dimensions = variable.getDimensions();
	    for (Dimension dimension : dimensions) {

		if (dimensions.size() >= 3) {

		    String name = dimension.getShortName();
		    if (name.equals("XLAT1D") || name.equals("XLONG1D")) {

			out.add(variable);
		    }
		}
	    }
	}

	return out.stream().//
		sorted((v1, v2) -> v1.getName().compareTo(v2.getName())).//
		map(v -> v.getName()).//
		collect(Collectors.toList());

    }

    /**
     * @param url
     * @return
     */
    private String encodeURL(String url) {

	try {
	    return URLEncoder.encode(url, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	}
	// no way
	return null;
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException {

	String file = "D:\\desktop\\nc1.nc";

	NetcdfDataset dataset = NetcdfDataset.openDataset(file);

	List<Variable> variables = dataset.getVariables();

	dataset.release();
	dataset.close();

	LinkedHashMap<String, Variable> map = new LinkedHashMap<>();

	Set<Variable> out = new HashSet<>();

	for (Variable variable : dataset.getVariables()) {

	    List<Dimension> dimensions = variable.getDimensions();
	    for (Dimension dimension : dimensions) {

		if (dimensions.size() >= 3) {

		    String name = dimension.getShortName();
		    if (name.equals("south_north") || name.equals("west_east")) {

			out.add(variable);
		    }
		}
	    }
	}

	List<Variable> collect = out.stream().sorted((v1, v2) -> v1.getName().compareTo(v2.getName())).collect(Collectors.toList());

	for (Variable variable : collect) {
	    System.out.println(variable.getName());
	}

	System.exit(0);
    }
}
