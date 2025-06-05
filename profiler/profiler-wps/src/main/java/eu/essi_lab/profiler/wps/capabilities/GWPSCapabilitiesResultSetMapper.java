package eu.essi_lab.profiler.wps.capabilities;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Envelope;

import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;

public class GWPSCapabilitiesResultSetMapper extends DiscoveryResultSetMapper<String> {

    private static final String GWPS_RES_SET_MAPPER_ERROR = "GWPS_RES_SET_MAPPER_ERROR";

    public GWPSCapabilitiesResultSetMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema GWPS_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {

	return GWPS_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String map(DiscoveryMessage message, GSResource res) throws GSException {

	ReportsMetadataHandler handler = new ReportsMetadataHandler(res);

	Optional<Bond> bond = message.getUserBond();
	String onlineId = null;
	if (bond.isPresent()) {

	    DiscoveryBondParser parser = new DiscoveryBondParser(bond.get());
	    OnlineResourceHandler h = new OnlineResourceHandler();
	    parser.parse(h);
	    onlineId = h.getOnlineId();
	}

	final String finalId = onlineId;

	List<DataComplianceReport> reports = handler.getReports().//
		stream().//
		filter(r -> r.getLastSucceededTest() == DataComplianceTest.EXECUTION).//
		filter(r -> r.getOnlineId().equals(finalId)).//
		collect(Collectors.toList());

	String out = "";

	Online onlineResource = res.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution()
		.getDistributionOnline(finalId);

	if (!reports.isEmpty()) {
	    DataComplianceReport report = reports.get(0);

	    DataDescriptor fullDescriptor = report.getFullDataDescriptor();

	    DataDescriptor reducedDescriptor = report.getPreviewDataDescriptor();
	    List<DataDimension> reducedSpatialDimensions = reducedDescriptor.getSpatialDimensions();

	    // SPATIAL
	    Double fullLower1 = null, fullLower2 = null, fullUpper1 = null, fullUpper2 = null;
	    Double reducedLower1 = null, reducedLower2 = null, reducedUpper1 = null, reducedUpper2 = null;

	    Long fullSize1 = 1l;
	    Long fullSize2 = 1l;
	    Long reducedSize1 = 1l;
	    Long reducedSize2 = 1l;

	    List<DataDimension> fullSpatialDimensions = fullDescriptor.getSpatialDimensions();
	    if (fullSpatialDimensions != null && fullSpatialDimensions.size() >= 2) {
		DataDimension fullDimension1 = fullSpatialDimensions.get(0);
		DataDimension fullDimension2 = fullSpatialDimensions.get(1);
		if (fullDimension1.getContinueDimension() != null && fullDimension2.getContinueDimension() != null) {
		    ContinueDimension cd1 = null;
		    ContinueDimension cd2 = null;
		    cd1 = (ContinueDimension) fullDimension1;
		    cd2 = (ContinueDimension) fullDimension2;
		    fullLower1 = cd1.getLower().doubleValue();
		    fullLower2 = cd2.getLower().doubleValue();
		    fullUpper1 = cd1.getUpper().doubleValue();
		    fullUpper2 = cd2.getUpper().doubleValue();
		    fullSize1 = cd1.getSize();
		    fullSize2 = cd2.getSize();
		}

		DataDimension reducedDimension1 = reducedSpatialDimensions.get(0);
		DataDimension reducedDimension2 = reducedSpatialDimensions.get(1);

		if (reducedDimension1.getContinueDimension() != null && reducedDimension2.getContinueDimension() != null) {
		    ContinueDimension cd1 = null;
		    ContinueDimension cd2 = null;
		    cd1 = (ContinueDimension) reducedDimension1;
		    cd2 = (ContinueDimension) reducedDimension2;
		    reducedLower1 = cd1.getLower().doubleValue();
		    reducedLower2 = cd2.getLower().doubleValue();
		    reducedUpper1 = cd1.getUpper().doubleValue();
		    reducedUpper2 = cd2.getUpper().doubleValue();
		    reducedSize1 = cd1.getSize();
		    reducedSize2 = cd2.getSize();
		}
	    }
	    // TEMPORAL
	    Long fullBeginPosition = null, fullEndPosition = null;
	    Long reducedBeginPosition = null, reducedEndPosition = null;

	    DataDimension fullTemporalDimension = fullDescriptor.getTemporalDimension();
	    if (fullTemporalDimension != null) {
		fullBeginPosition = fullTemporalDimension.getContinueDimension().getLower().longValue();
		fullEndPosition = fullTemporalDimension.getContinueDimension().getUpper().longValue();
	    }
	    DataDimension reducedTemporalDimension = reducedDescriptor.getTemporalDimension();
	    if (reducedTemporalDimension != null) {
		reducedBeginPosition = reducedTemporalDimension.getContinueDimension().getLower().longValue();
		reducedEndPosition = reducedTemporalDimension.getContinueDimension().getUpper().longValue();
	    }

	    DataComplianceLevel level = report.getTargetComplianceLevel();
	    ProcessorCapabilities cap = level.getCapabilities();

	    out += "<ns2:capabilityCubes xmlns:ns2=\"http://floraresearch.eu/sdi/services/giaxe/1.0/datamodel/schema\">";
	    out += "<ns2:directLink></ns2:directLink>";
	    out += "<ns2:thumbnailLink></ns2:thumbnailLink>";
	    out += "<ns2:capabilityCube>";
	    out += "<ns2:protocol>gi-axe</ns2:protocol>";

	    switch (level) {
	    case TIME_SERIES_BASIC_DATA_COMPLIANCE:
		out += "<ns2:format>" + DataFormat.NETCDF().getIdentifier() + "</ns2:format>";
		out += "<ns2:format>" + DataFormat.WATERML_1_1().getIdentifier() + "</ns2:format>";
		out += "<ns2:format>" + DataFormat.WATERML_2_0().getIdentifier() + "</ns2:format>";
		break;
	    case GRID_BASIC_DATA_COMPLIANCE:
		if(!onlineResource.getProtocol().equals(CommonNameSpaceContext.SENTINEL2_URI)){
		    out += "<ns2:format>" + DataFormat.NETCDF().getIdentifier() + "</ns2:format>";
		    out += "<ns2:format>" + DataFormat.IMAGE_PNG().getIdentifier() + "</ns2:format>";
		} else {
		    out += "<ns2:format>" + DataFormat.IMAGE_PNG().getIdentifier() + "</ns2:format>";
		}
		break;
	    default:
		break;
	    }

	    out += "<ns2:name>" + onlineResource.getName() + "</ns2:name>";

	    switch (level) {
	    case TIME_SERIES_BASIC_DATA_COMPLIANCE:
	    case GRID_BASIC_DATA_COMPLIANCE:
		CRS crs = fullDescriptor.getCRS();

		if (fullLower1 != null && fullLower2 != null && reducedLower1 != null && reducedLower2 != null) {

		    // SOURCE CRS. Include or not include?
		    if (!crs.equals(CRS.EPSG_4326())) {
			out = out + getSpatialInfo(crs, fullLower1, fullLower2, fullUpper1, fullUpper2, reducedLower1, reducedLower2,
				reducedUpper1, reducedUpper2, fullSize1, fullSize2);
		    }

		    // EPSG 4326 CRS is here included.
		    if (!crs.equals(CRS.EPSG_4326())) {
			try {

			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>>(
				    new SimpleEntry<Double, Double>(fullLower1, fullLower2),
				    new SimpleEntry<Double, Double>(fullUpper1, fullUpper2));

			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> fullTargetCorners = CRSUtils
				    .translateBBOX(sourceCorners, crs, CRS.EPSG_4326());

			    SimpleEntry<Double, Double> fullLowerCorner = fullTargetCorners.getKey();
			    SimpleEntry<Double, Double> fullUpperCorner = fullTargetCorners.getValue();

			    fullLower1 = fullLowerCorner.getKey();
			    fullLower2 = fullLowerCorner.getValue();
			    fullUpper1 = fullUpperCorner.getKey();
			    fullUpper2 = fullUpperCorner.getValue();

			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceReducedCorners = new SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>>(
				    new SimpleEntry<Double, Double>(reducedLower1, reducedLower2),
				    new SimpleEntry<Double, Double>(reducedUpper1, reducedUpper2));

			    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> reducedTargetCorners = CRSUtils
				    .translateBBOX(sourceReducedCorners, crs, CRS.EPSG_4326());

			    SimpleEntry<Double, Double> reducedLowerCorner = reducedTargetCorners.getKey();
			    SimpleEntry<Double, Double> reducedUpperCorner = reducedTargetCorners.getValue();

			    reducedLower1 = reducedLowerCorner.getKey();
			    reducedLower2 = reducedLowerCorner.getValue();
			    reducedUpper1 = reducedUpperCorner.getKey();
			    reducedUpper2 = reducedUpperCorner.getValue();

			    out = out + getSpatialInfo(CRS.EPSG_4326(), fullLower1, fullLower2, fullUpper1, fullUpper2, reducedLower1,
				    reducedLower2, reducedUpper1, reducedUpper2, fullSize1, fullSize2);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    } else {
			out = out + getSpatialInfo(CRS.EPSG_4326(), fullLower1, fullLower2, fullUpper1, fullUpper2, reducedLower1,
				reducedLower2, reducedUpper1, reducedUpper2, fullSize1, fullSize2);
		    }

		    List<CRS> otherCRSes = new ArrayList<CRS>();
		    // additional CRSes are included from this list
		    otherCRSes.add(CRS.EPSG_3857());
		    for (CRS otherCRS : otherCRSes) {
			try {
			    CoordinateReferenceSystem sourceCRS = CRS.EPSG_4326().getDecodedCRS();
			    CoordinateReferenceSystem targetCRS = otherCRS.getDecodedCRS();
			    MathTransform transform = org.geotools.referencing.CRS.findMathTransform(sourceCRS, targetCRS);

			    Envelope fullEnvelope = new Envelope(fullLower1, fullUpper1, fullLower2, fullUpper2);
			    // Sample 10 points around the envelope
			    Envelope fullTransformed = JTS.transform(fullEnvelope, null, transform, 10);

			    double outFullLower1 = fullTransformed.getMinX();
			    double outFullLower2 = fullTransformed.getMinY();
			    double outFullUpper1 = fullTransformed.getMaxX();
			    double outFullUpper2 = fullTransformed.getMaxY();

			    Envelope reducedEnvelope = new Envelope(reducedLower1, reducedUpper1, reducedLower2, reducedUpper2);
			    // Sample 10 points around the envelope
			    Envelope reducedTransformed = JTS.transform(reducedEnvelope, null, transform, 10);

			    double outReducedLower1 = reducedTransformed.getMinX();
			    double outReducedLower2 = reducedTransformed.getMinY();
			    double outReducedUpper1 = reducedTransformed.getMaxX();
			    double outReducedUpper2 = reducedTransformed.getMaxY();

			    out = out + getSpatialInfo(otherCRS, outFullLower1, outFullLower2, outFullUpper1, outFullUpper2,
				    outReducedLower1, outReducedLower2, outReducedUpper1, outReducedUpper2, fullSize1, fullSize2);
			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).error("Unable to add CRS info for CRS: " + otherCRS.getIdentifier());
			}
		    }

		}

		if (fullBeginPosition != null && fullEndPosition != null && reducedBeginPosition != null && reducedEndPosition != null) {
		    // TIME
		    out += "<ns2:temporalGrid>";
		    out += "<ns2:temporalAxis xsi:type=\"resolutionAxis\"  >";
		    out += "<ns2:label>time</ns2:label>";

		    out += "</ns2:temporalAxis>";
		    out += "<ns2:totalExtent>";
		    out += "<ns2:beginPosition>" + ISO8601DateTimeUtils.getISO8601DateTime(new Date(fullBeginPosition))
			    + "</ns2:beginPosition>";
		    out += "<ns2:beginPositionType>ABSOLUTE</ns2:beginPositionType>";
		    out += "<ns2:endPosition>" + ISO8601DateTimeUtils.getISO8601DateTime(new Date(fullEndPosition)) + "</ns2:endPosition>";
		    out += "<ns2:endPositionType>ABSOLUTE</ns2:endPositionType>";
		    out += "</ns2:totalExtent>";

		    out += "<ns2:defaultExtent>";
		    out += "<ns2:beginPosition>" + ISO8601DateTimeUtils.getISO8601DateTime(new Date(reducedBeginPosition))
			    + "</ns2:beginPosition>";
		    out += "<ns2:beginPositionType>ABSOLUTE</ns2:beginPositionType>";
		    out += "<ns2:endPosition>" + ISO8601DateTimeUtils.getISO8601DateTime(new Date(reducedEndPosition))
			    + "</ns2:endPosition>";
		    out += "<ns2:endPositionType>ABSOLUTE</ns2:endPositionType>";
		    out += "</ns2:defaultExtent>";

		    out += "</ns2:temporalGrid>";
		} else {
		    out += "<ns2:temporalGrid/>";
		}
		out += "<ns2:verticalGrid/>";
		break;

	    default:
		break;
	    }

	    out += "</ns2:capabilityCube>";
	    out += "</ns2:capabilityCubes>";

	}
	return out;

    }

    private String getSpatialInfo(CRS crs, Double fullLower1, Double fullLower2, Double fullUpper1, Double fullUpper2, Double reducedLower1,
	    Double reducedLower2, Double reducedUpper1, Double reducedUpper2, Long fullSize1, Long fullSize2) {
	String out = "";

	out += "<ns2:spatialGrid>";
	// LAT
	out += "<spatialAxis xsi:type=\"sizedAxis\"  >";
	out += "<abbreviation>Lat</abbreviation>";
	out += "<label>Geodetic latitude</label>";
	out += "<units>DEGREES</units>";
	out += "<direction>NORTH</direction>";
	out += "<numberOfPoints>" + (fullSize1 == null ? "" : fullSize1) + "</numberOfPoints>";
	out += "</spatialAxis>";
	// LON
	out += "<spatialAxis xsi:type=\"sizedAxis\"  >";
	out += "<abbreviation>Lon</abbreviation>";
	out += "<label>Geodetic longitude</label>";
	out += "<units>DEGREES</units>";
	out += "<direction>EAST</direction>";
	out += "<numberOfPoints>" + (fullSize2 == null ? "" : fullSize2) + "</numberOfPoints>";
	out += "</spatialAxis>";
	// CRS
	out += getCRS(crs);

	out += "<totalExtent>";
	out += "<lowerCorner>";
	out += getCRS(crs);
	out += "<coordinates>" + fullLower1 + "</coordinates>";
	out += "<coordinates>" + fullLower2 + "</coordinates>";
	out += "</lowerCorner>";

	out += "<upperCorner>";
	out += getCRS(crs);
	out += "<coordinates>" + fullUpper1 + "</coordinates>";
	out += "<coordinates>" + fullUpper2 + "</coordinates>";
	out += "</upperCorner>";

	out += "</totalExtent>";

	out += "<defaultExtent>";
	out += "<lowerCorner>";
	out += getCRS(crs);
	out += "<coordinates>" + reducedLower1 + "</coordinates>";
	out += "<coordinates>" + reducedLower2 + "</coordinates>";
	out += "</lowerCorner>";

	out += "<upperCorner>";
	out += getCRS(crs);
	out += "<coordinates>" + reducedUpper1 + "</coordinates>";
	out += "<coordinates>" + reducedUpper2 + "</coordinates>";
	out += "</upperCorner>";

	out += "</defaultExtent>";

	out += "</ns2:spatialGrid>";
	return out;
    }

    private String getCRS(CRS crs) {
	String out = "";
	if (crs != null) {

	    String dir1 = "";
	    String dir2 = "";
	    switch (crs.getAxisOrder()) {
	    case NORTH_EAST:
		dir1 = "NORTH";
		dir2 = "EAST";
		break;
	    case EAST_NORTH:
	    default:
		dir1 = "EAST";
		dir2 = "NORTH";
		break;
	    }

	    out += "<crs>";
	    out += "<authority>" + crs.getAuthority().getIdentifier() + "</authority>";
	    out += "<code>" + crs.getCode() + "</code>";
	    out += "<identifier>" + crs.getIdentifier() + "</identifier>";
	    out += "<axisInformation>";
	    out += "<abbreviation>" + crs.getFirstAxisName() + "</abbreviation>";
	    out += "<label>" + crs.getFirstAxisName() + "</label>";
	    out += "<units>" + crs.getUOM().getIdentifier() + "</units>";
	    out += "<direction>" + dir1 + "</direction>";
	    out += "</axisInformation>";
	    out += "<axisInformation>";
	    out += "<abbreviation>" + crs.getSecondAxisName() + "</abbreviation>";
	    out += "<label>" + crs.getSecondAxisName() + "</label>";
	    out += "<units>" + crs.getUOM().getIdentifier() + "</units>";
	    out += "<direction>" + dir2 + "</direction>";
	    out += "</axisInformation>";
	    out += "</crs>";
	}
	return out;
    }

}
