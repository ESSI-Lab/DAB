package eu.essi_lab.profiler.sos.sensor;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;

import eu.essi_lab.jaxb.sos._2_0.swes_2.DescribeSensorResponseType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * @author boldrini
 */
public class DescribeSensorFormatter extends DiscoveryResultSetFormatter<String> {

    public static final String SOS_DESCRIBE_SENSOR_ENCODING = "SOS_DESCRIBE_SENSOR_ENCODING";

    public static final String SOS_DESCRIBE_SENSOR_ENCODING_VERSION = "2.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding SOS_DESCRIBE_SENSOR_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	SOS_DESCRIBE_SENSOR_FORMATTING_ENCODING.setEncoding(SOS_DESCRIBE_SENSOR_ENCODING);
	SOS_DESCRIBE_SENSOR_FORMATTING_ENCODING.setEncodingVersion(SOS_DESCRIBE_SENSOR_ENCODING_VERSION);
	SOS_DESCRIBE_SENSOR_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);
    }

    private static final String SOS_DESCRIBE_SENSOR_FORMATTER_ERROR = "SOS_FOI_FORMATTER_ERROR";

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> mappedResultSet) throws GSException {

	try {

	    DescribeSensorResponseType describeSensorResponseType = new DescribeSensorResponseType();
	    describeSensorResponseType.setProcedureDescriptionFormat("http://www.opengis.net/sensorml/2.0");

	    List<String> sensors = mappedResultSet.getResultsList();

	    for (String sensor : sensors) {

		Object jaxbElement = JAXBSOS.getInstance().unmarshal(sensor);
		if (jaxbElement instanceof JAXBElement<?>) {
		    JAXBElement<?> inner = (JAXBElement<?>) jaxbElement;
		    jaxbElement = inner.getValue();
		}
		if (jaxbElement instanceof DescribeSensorResponseType) {
		    DescribeSensorResponseType childDescription = (DescribeSensorResponseType) jaxbElement;
		    describeSensorResponseType.getDescription().addAll(childDescription.getDescription());
		}

	    }

	    JAXBElement<DescribeSensorResponseType> jaxbElement = JAXBSOS.getInstance().getSWESFactory()
		    .createDescribeSensorResponse(describeSensorResponseType);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    JAXBSOS.getInstance().marshal(jaxbElement, baos);
	    baos.close();

	    ByteArrayInputStream response = new ByteArrayInputStream(baos.toByteArray());
	    Response ret = buildResponse(response);
	    response.close();
	    return ret;

	} catch (

	Exception ex) {
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_DESCRIBE_SENSOR_FORMATTER_ERROR);
	}
    }

    @Override
    public FormattingEncoding getEncoding() {

	return SOS_DESCRIBE_SENSOR_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private Response buildResponse(InputStream response) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(response);
	builder = builder.type(MediaType.APPLICATION_XML);

	return builder.build();
    }
}
