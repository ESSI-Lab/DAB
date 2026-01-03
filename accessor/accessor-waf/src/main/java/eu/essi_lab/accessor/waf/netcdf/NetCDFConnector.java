/**
 * 
 */
package eu.essi_lab.accessor.waf.netcdf;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.imageio.netcdf.GeoToolsNetCDFReader;
import org.geotools.imageio.netcdf.utilities.NetCDFCRSUtilities;
import org.xml.sax.SAXException;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * @author boldrini
 */
public class NetCDFConnector extends HarvestedQueryConnector<NetCDFConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "NetCDFConnector";
    public static final String S3_NETCDF_PROTOCOL = "S3-NetCDF";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<OriginalMetadata>();

	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(getSourceURL());

	if (stream.isPresent()) {

	    InputStream inputStream = stream.get();
	    final String url = getSourceURL().endsWith("/") ? getSourceURL() : getSourceURL() + "/";

	    try {
		XMLDocumentReader reader = new XMLDocumentReader(inputStream);

		Arrays.asList(reader.evaluateNodes("//*:Key/text()")).//
			stream().//

			map(node -> {
			    String filename = node.getNodeValue();

			    if (!filename.toLowerCase().endsWith(".nc")) {
				return null;
			    }

			    OriginalMetadata originalMetadata = null;
			    NetcdfDataset ncDataset = null;

			    try {

				File tmpFile = getLocalCopy(url, filename);

				Dataset dataset = new Dataset();
				GSSource source = new GSSource();
				source.setEndpoint(getSourceURL());
				dataset.setSource(source);

				// read NETCDF

				ncDataset = NetcdfDataset.openDataset(tmpFile.getAbsolutePath());

				List<Variable> mainVariables = NetCDFUtils.getGeographicVariables(ncDataset);
				Variable mainVariable = mainVariables.get(0);
				CoordinateReferenceSystem decodedCRS = GeoToolsNetCDFReader.extractCRS(ncDataset, mainVariable);

				CRS crs;
				if (decodedCRS != null) {
				    // this workaround is because by default GeoTools NetcdfUtils uses OGC CRS 84
				    // for NetCDF latitude
				    // longitude crs
				    if (decodedCRS.equals(NetCDFCRSUtilities.WGS84)) {
					decodedCRS = org.geotools.referencing.CRS.decode("EPSG:4326");
				    }

				    crs = CRS.fromGeoToolsCRS(decodedCRS);

				}

				List<CoordinateAxis> axes = ncDataset.getCoordinateAxes();

				ContinueDimension spatialEast = null;
				ContinueDimension spatialNorth = null;
				ContinueDimension time = null;

				for (CoordinateAxis axe : axes) {
				    ContinueDimension dimension = new ContinueDimension(NetCDFUtils.getAxisName(axe));
				    double min = axe.getMinValue();
				    double max = axe.getMaxValue();
				    long size = axe.getSize();
				    Double resolution = NetCDFUtils.readResolution(axe.read());
				    Unit uom = Unit.fromIdentifier(NetCDFUtils.getAxisUnit(axe));
				    Datum datum = null;

				    AxisType type = axe.getAxisType();
				    if (type == null) {
				    } else {
					switch (type) {
					case GeoX:
					case Lon:
					    spatialEast = dimension;
					    dimension.setType(DimensionType.ROW);
					    break;
					case GeoY:
					case Lat:
					    spatialNorth = dimension;
					    dimension.setType(DimensionType.COLUMN);
					    break;
					case Time:
					    CoordinateAxis1DTime timeAxis = CoordinateAxis1DTime.factory(ncDataset, axe, null);
					    time = dimension;
					    dimension.setType(DimensionType.TIME);
					    resolution = NetCDFUtils.readMinimumResolutionInMilliseconds(timeAxis);
					    min = timeAxis.getCalendarDateRange().getStart().getMillis();
					    max = timeAxis.getCalendarDateRange().getEnd().getMillis();
					    uom = Unit.MILLI_SECOND;
					    datum = Datum.UNIX_EPOCH_TIME();
					    // }
					    break;
					default:
					    break;
					}
				    }
				    dimension.setLower(min);
				    dimension.setUpper(max);
				    dimension.setSize(size);
				    dimension.setResolution(resolution);
				    dimension.setDatum(datum);
				    dimension.setUom(uom);
				}

				CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

				String observedParameter = mainVariable.findAttribute("description").getStringValue();
				String observedParameterUnits = mainVariable.findAttribute("units").getStringValue();
				coreMetadata.setTitle("Average monthly temperature (" + observedParameter + ") - Europe ");
				coreMetadata.setAbstract("Average monthly temperature (" + observedParameter + ") - Europe ");
				CoverageDescription coverageDescription = new CoverageDescription();
				coverageDescription.setAttributeIdentifier(mainVariable.getShortName());
				coverageDescription.setAttributeTitle(observedParameter);
				coverageDescription.setAttributeDescription("");
				coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

				dataset.getExtensionHandler().setAttributeUnits(observedParameterUnits);
				double n = spatialNorth.getUpper().doubleValue();
				double w = spatialEast.getLower().doubleValue();
				double s = spatialNorth.getLower().doubleValue();
				double e = spatialEast.getUpper().doubleValue();
				coreMetadata.addBoundingBox(n, w, s, e);
				Date start = new Date(time.getLower().longValue());
				Date end = new Date(time.getUpper().longValue());
				String startString = ISO8601DateTimeUtils.getISO8601DateTime(start);
				String endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
				coreMetadata.addTemporalExtent(startString, endString);

				coreMetadata.getMIMetadata().getElementType().getContentInfo().get(0).getAbstractMDContentInformation();

				GridSpatialRepresentation grid = new GridSpatialRepresentation();

				Dimension timeDimension = new Dimension();
				timeDimension.setResolution(time.getUom().getIdentifier(), time.getResolution().doubleValue());
				grid.addAxisDimension(timeDimension);
				dataset.getExtensionHandler().setTimeUnits("milliseconds");
				dataset.getExtensionHandler().setTimeResolution("" + time.getResolution().longValue());
				Dimension latDimension = new Dimension();
				latDimension.setResolution(spatialNorth.getUom().getIdentifier(),
					spatialNorth.getResolution().doubleValue());
				grid.addAxisDimension(latDimension);

				Dimension lonDimension = new Dimension();
				lonDimension.setResolution(spatialEast.getUom().getIdentifier(), spatialEast.getResolution().doubleValue());
				grid.addAxisDimension(lonDimension);

				coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

				MIPlatform platform = new MIPlatform();
				Citation citation = new Citation();
				citation.setTitle(filename);
				platform.setMDIdentifierCode(filename);
				platform.setCitation(citation);
				coreMetadata.getMIMetadata().addMIPlatform(platform);

				coreMetadata.addDistributionOnlineResource(//
					filename, //
					url, //
					S3_NETCDF_PROTOCOL, //
					"download");

				String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, filename);

				coreMetadata.getOnline().setIdentifier(resourceIdentifier);

				String str = dataset.asString(true);
				originalMetadata = new OriginalMetadata();
				originalMetadata.setMetadata(str);
				originalMetadata.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);

			    } catch (Exception e) {
				GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
			    } finally {
				try {
				    ncDataset.close();
				} catch (IOException e) {
				    e.printStackTrace();
				}
			    }

			    return originalMetadata;
			}).//
			filter(Objects::nonNull).//
			forEach(o -> response.addRecord(o));

	    } catch (SAXException | IOException | XPathExpressionException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	return response;
    }

    public static File getLocalCopy(String url, String filename) throws Exception {
	String u = url + filename;

	String tmpDirsLocation = System.getProperty("java.io.tmpdir");
	File tmpFile = new File(tmpDirsLocation, "netcdf-connector-" + filename);
	if (tmpFile.exists()&&tmpFile.length()>0) {
	    // already downloaded
	    return tmpFile;
	}
	Downloader down = new Downloader();
	Optional<InputStream> netCDF = down.downloadOptionalStream(u);

	if (netCDF.isPresent()) {

	    // coverageDescription.setAttributeIdentifier("urn:ca:qc:gouv:cehq:depot:variable:" +
	    // var.name());
	    // coverageDescription.setAttributeTitle(var.getLabel());

	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(netCDF.get(), fos);
	    fos.close();
	    return tmpFile;
	}
	return null;

    }

    @Override
    public boolean supports(GSSource source) {
	try {
	    String endpoint = source.getEndpoint();
	    Downloader downloader = new Downloader();
	    Optional<InputStream> stream = downloader.downloadOptionalStream(endpoint);
	    if (stream.isPresent()) {
		InputStream inputStream = stream.get();
		XMLDocumentReader reader = new XMLDocumentReader(inputStream);
		String rootName = reader.getDocument().getDocumentElement().getLocalName();
		if (rootName != null && rootName.equals("ListBucketResult")) {
		    return true;
		}
	    }
	} catch (Exception e) {
	}
	return false;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CommonNameSpaceContext.GMD_NS_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected NetCDFConnectorSetting initSetting() {

	return new NetCDFConnectorSetting();
    }
}
