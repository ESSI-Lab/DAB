package eu.essi_lab.profiler.wfs.feature.emodpace;

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

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.wfs.JAXBWFS;
import eu.essi_lab.profiler.wfs.WFSRequest.Parameter;
import eu.essi_lab.profiler.wfs.feature.WFSGetFeatureRequest;
import net.opengis.gml.v_3_1_1.FeatureArrayPropertyType;
import net.opengis.wfs.v_1_1_0.FeatureCollectionType;
import net.opengis.wfs.v_1_1_0.ObjectFactory;

/**
 * @author boldrini
 */
public class ThematicDatasetResultSetFormatter extends DiscoveryResultSetFormatter<Node> {

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding WFS_GET_FEATURE_FORMATTING_ENCODING = new FormattingEncoding();

    static {
	WFS_GET_FEATURE_FORMATTING_ENCODING.setEncoding("WFS_GET_FEATURE");
	WFS_GET_FEATURE_FORMATTING_ENCODING.setEncodingVersion("1_1_0");
	WFS_GET_FEATURE_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);

    }

    private static final String WFS_GET_FEATURE_RESULT_SET_FORMATTER_ERROR = "WFS_GET_FEATURE_RESULT_SET_FORMATTER_ERROR";
    private ThematicDataset thematicDataset;

    public ThematicDatasetResultSetFormatter(ThematicDataset thematicDataset) {
	this.thematicDataset = thematicDataset;
    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<Node> resultSet) throws GSException {

	try {

	    WFSGetFeatureRequest wfsRequest = new WFSGetFeatureRequest(message.getWebRequest());

	    String srs = wfsRequest.getParameterValue(Parameter.SRS_NAME);
	    CRS crs = srs == null ? CRS.EPSG_4326() : CRS.fromIdentifier(srs);

	    // result set cycle
	    List<Node> nodes = resultSet.getResultsList();
	    ObjectFactory factory = new ObjectFactory();

	    net.opengis.gml.v_3_1_1.ObjectFactory gmlFactory = new net.opengis.gml.v_3_1_1.ObjectFactory();

	    FeatureArrayPropertyType fapt = new FeatureArrayPropertyType();
	    List<Object> features = new ArrayList<>();

	    JAXBElement<FeatureArrayPropertyType> members = gmlFactory.createFeatureMembers(fapt);
	    for (Node node : nodes) {
		XMLDocumentReader reader = new XMLDocumentReader(node.getOwnerDocument());

		String platformId = reader.evaluateString("//*:extension/*:uniquePlatformId");
		Double s = reader.evaluateNumber("//*:southBoundLatitude[1]/*:Decimal").doubleValue();
		Double w = reader.evaluateNumber("//*:westBoundLongitude[1]/*:Decimal").doubleValue();
		Double n = null;
		Double e = null;
		try {
		    n = reader.evaluateNumber("//*:northBoundLatitude[1]/*:Decimal").doubleValue();
		    e = reader.evaluateNumber("//*:eastBoundLongitude[1]/*:Decimal").doubleValue();
		} catch (Exception re) {
		    // TODO: handle exception
		}
		// Node beginNode = reader.evaluateNode("//*:TimePeriod[1]/*:beginPosition");
		// Node endNode = reader.evaluateNode("//*:TimePeriod[1]/*:endPosition");

		if (n == null) {
		    n = s;
		}
		if (e == null) {
		    e = w;
		}
		String geometry = "";

		Double tol = 0.000001;
		if (Math.abs(s - n) < tol) {
		    // point
		    Double c1, c2;
		    if (crs.equals(CRS.EPSG_3857())) {
			SimpleEntry<Double, Double> ret = CRSUtils.translatePoint(new SimpleEntry<Double, Double>(s, w), CRS.EPSG_4326(),
				CRS.EPSG_3857());
			c1 = ret.getKey();
			c2 = ret.getValue();
		    } else {
			// 4326
			c1 = s;
			c2 = w;
		    }
		    geometry = "<gml:Point srsName=\"http://www.opengis.net/gml/srs/epsg.xml#" + crs.getCode() + "\" srsDimension=\"2\">\n" //
			    + "<gml:pos>" + c1 + " " + c2 + "</gml:pos>\n" //
			    + "</gml:Point>\n" //
		    ;
		} else {
		    // BBOX
		    Double minx, miny, maxx, maxy;
		    if (crs.equals(CRS.EPSG_3857())) {
			SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> corners = new SimpleEntry<AbstractMap.SimpleEntry<Double, Double>, AbstractMap.SimpleEntry<Double, Double>>(
				//
				new SimpleEntry<Double, Double>(s, w), // lower corner
				new SimpleEntry<Double, Double>(n, e));// upper corner
			SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> ret = CRSUtils.translateBBOX(corners,
				CRS.EPSG_4326(), CRS.EPSG_3857());
			minx = ret.getKey().getKey();
			miny = ret.getKey().getValue();
			maxx = ret.getValue().getKey();
			maxy = ret.getValue().getValue();
		    } else {
			// 4326
			minx = s;
			miny = w;
			maxx = n;
			maxy = e;
		    }
		    if (Double.isFinite(minx) && Double.isFinite(miny) && Double.isFinite(maxx) && Double.isFinite(maxy)) {
			geometry = "<gml:MultiSurface srsName=\"urn:x-ogc:def:crs:EPSG:" + crs.getCode() + "\" srsDimension=\"2\">\n" + //
				"<gml:surfaceMember>\n" + //
				"<gml:Polygon>\n" + //
				"<gml:exterior>\n" + //
				"<gml:LinearRing>\n" + //
				"<gml:posList>" + //
				minx + " " + miny + " "//
				+ minx + " " + maxy + " "//
				+ maxx + " " + maxy + " "//
				+ maxx + " " + miny + " "//
				+ minx + " " + miny + //
				"</gml:posList>\n" + //
				"</gml:LinearRing>\n" + //
				"</gml:exterior>\n" + //
				"</gml:Polygon>\n" + //
				"</gml:surfaceMember>\n" + "</gml:MultiSurface>" //
			;//
		    }

		}
		String title = reader.evaluateString(
			"/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:identificationInfo[1]/*:MD_DataIdentification[1]/*:citation[1]/*:CI_Citation/*:title/*[1]");
		String abstracz = reader.evaluateString(
			"/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:identificationInfo[1]/*:MD_DataIdentification[1]/*:abstract[1]/*[1]");

		String organization = "";

		organization = reader
			.evaluateString("/*/*:harmonizedMetadata[1]/*:extendedMetadata[1]/*:extension/*:OriginatorOrganisationDescription");

		if (organization == null || organization.isEmpty()) {

		    Node[] orgNodes = reader.evaluateNodes("//*:organisationName/*");
		    HashSet<String> orgs = new HashSet<>();
		    for (Node orgNode : orgNodes) {
			String org = reader.evaluateString(orgNode, ".");
			orgs.add(org.trim());
		    }
		    Iterator<String> orgIterator = orgs.iterator();
		    while (orgIterator.hasNext()) {
			String org = (String) orgIterator.next();
			organization += org + "|";
		    }
		    if (!organization.isEmpty()) {
			organization = organization.substring(0, organization.length() - 1);
		    }
		}

		String platformName = reader.evaluateString(
			"/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:acquisitionInformation[1]/*:MI_AcquisitionInformation[1]/*:platform[1]/*:MI_Platform[1]/*:citation[1]/*:CI_Citation[1]/*:title[1]/*[1]");

		String downloadURL = reader.evaluateString(
			"/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:distributionInfo[1]/*:MD_Distribution[1]/*:transferOptions/*:MD_DigitalTransferOptions/*:onLine/*:CI_OnlineResource[*:function/*/@codeListValue='download']/*:linkage[1]/*[1]");

		String fileIdentifier = reader.evaluateString(
			"/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:fileIdentifier[1]/*[1]");

		String metadataURL = null;
		if (fileIdentifier != null) {
		    metadataURL = "https://seadatanet.geodab.eu/gs-service/services/essi/view/emod-pace/csw?service=CSW&version=2.0.2&request=GetRecordById&id="
			    + fileIdentifier + "&outputschema=http://www.isotc211.org/2005/gmi&elementSetName=full";
		}

		String parameter = reader.evaluateString(
			"/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:contentInfo[1]/*:MD_CoverageDescription[1]/*:attributeDescription[1]/*:RecordType[1]");

		QName root = thematicDataset.getQName();
		String rootString = root.getLocalPart().contains(" ") ? root.getLocalPart().replace(" ", "-") : root.getLocalPart();
		String xml = //
			"<essi:" + rootString + " xmlns:essi=\"http://essi-lab.eu\" xmlns:gml=\"http://www.opengis.net/gml\" >" + //
				"<essi:the_geom>" + geometry + //
				"</essi:the_geom>\n" + //
				"<essi:title/>" + //
				"<essi:abstract/>" + //
				"<essi:theme/>" + //
				"<essi:organization/>" + //
				"<essi:platform/>" + //
				"<essi:parameter/>" + //
				"<essi:downloadURL/>" + //
				"<essi:metadataURL/>" + //
				"</essi:" + rootString + ">\n";//
		XMLDocumentReader r = new XMLDocumentReader(xml);
		XMLDocumentWriter writer = new XMLDocumentWriter(r);
		if (title != null) {
		    writer.setText("/*/*:title", title);
		}
		if (abstracz != null) {
		    writer.setText("/*/*:abstract", abstracz);
		}
		writer.setText("/*/*:theme", thematicDataset.getTheme());
		if (organization != null) {
		    writer.setText("/*/*:organization", organization);
		}
		if (platformName != null) {
		    writer.setText("/*/*:platform", platformName);
		}
		if (parameter != null) {
		    writer.setText("/*/*:parameter", parameter);
		}
		if (downloadURL != null) {
		    writer.setText("/*/*:downloadURL", downloadURL);
		}
		if (metadataURL != null) {
		    writer.setText("/*/*:metadataURL", metadataURL);
		}
		features.add(r.getDocument().getDocumentElement());

	    }
	    fapt.setFeature(features);
	    FeatureCollectionType fct = new FeatureCollectionType();

	    fct.setFeatureMembers(members);
	    JAXBElement<FeatureCollectionType> fc = factory.createFeatureCollection(fct);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBWFS.getInstance().getMarshaller().marshal(fc, baos);
	    String ret = new String(baos.toByteArray());

	    return buildResponse(ret);

	} catch (

	Exception ex) {
	    ex.printStackTrace();
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WFS_GET_FEATURE_RESULT_SET_FORMATTER_ERROR);
	}
    }

    @Override
    public FormattingEncoding getEncoding() {

	return WFS_GET_FEATURE_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private static Response buildResponse(String response) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(response);
	builder = builder.type(MediaType.APPLICATION_XML);

	return builder.build();
    }
}
