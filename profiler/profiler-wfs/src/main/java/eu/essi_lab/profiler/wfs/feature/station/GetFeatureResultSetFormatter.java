package eu.essi_lab.profiler.wfs.feature.station;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;
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
public class GetFeatureResultSetFormatter extends DiscoveryResultSetFormatter<Node> {

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

    public GetFeatureResultSetFormatter() {

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
		// Node beginNode = reader.evaluateNode("//*:TimePeriod[1]/*:beginPosition");
		// Node endNode = reader.evaluateNode("//*:TimePeriod[1]/*:endPosition");
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
		String title = reader
			.evaluateString("//*:identificationInfo[1]/*:MD_DataIdentification[1]/*:citation[1]/*:CI_Citation/*:title/*[1]");
		String platformName = reader.evaluateString(
			"/*:Dataset/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:acquisitionInformation[1]/*:MI_AcquisitionInformation[1]/*:platform[1]/*:MI_Platform[1]/*:citation[1]/*:CI_Citation[1]/*:title[1]/*:CharacterString[1]");
		String xml = //
			"<essi:station xmlns:essi=\"http://essi-lab.eu\" xmlns:gml=\"http://www.opengis.net/gml\" >" + //
				"<essi:the_geom>\n" + //
				"<gml:Point srsName=\"http://www.opengis.net/gml/srs/epsg.xml#" + crs.getCode() + "\" srsDimension=\"2\">\n" //
				+ "<gml:pos>" + c1 + " " + c2 + "</gml:pos>\n" //
				+ "</gml:Point>\n" + //
				"</essi:the_geom>\n" + //
				"<essi:name>" + platformName + "</essi:name>" + //
				"<essi:id>" + platformId + "</essi:id>" + //
				"</essi:station>\n"//
		;
		XMLDocumentReader r = new XMLDocumentReader(xml);

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
