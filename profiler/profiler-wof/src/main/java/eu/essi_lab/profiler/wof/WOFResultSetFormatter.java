package eu.essi_lab.profiler.wof;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;

import org.cuahsi.waterml._1.essi.JAXBWML;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLStreamWriterUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * @author boldrini
 */
public abstract class WOFResultSetFormatter<T> extends DiscoveryResultSetFormatter<T> {

    @Override
    public Response format(DiscoveryMessage message, ResultSet<T> mappedResultSet) throws GSException {

	try {

	    String cuahsiNS = "http://www.cuahsi.org/his/1.1/ws/";
	    String wmlNS = "http://www.cuahsi.org/waterML/1.1/";

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    XMLStreamWriter writer = XMLStreamWriterUtils.getSOAPWriter(baos);

	    writer.writeStartElement("", getResponseName(), cuahsiNS);
	    writer.writeNamespace("", cuahsiNS);

	    WOFRequest req = getWOFRequest(message.getWebRequest());

	    UriInfo uri = message.getWebRequest().getUriInfo();
	    String path = message.getWebRequest().getRequestPath();
	    String url = connect(uri.getBaseUri().toString(), path);

	    JAXBElement jaxbElement = getResult(url, req, mappedResultSet);

	    Marshaller marshaller = JAXBWML.getInstance().getContext().createMarshaller();

	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

	    final NamespacePrefixMapper mapper = JAXBWML.getInstance()
		    .getNamespacePrefixMapper(new String[] { "xsi", "http://www.w3.org/2001/XMLSchema-instance" });

	    marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, mapper);

	    if (req.getClass().getSimpleName().toLowerCase().contains("object")) {
		marshaller.marshal(jaxbElement, writer);
	    } else {

		writer.writeStartElement("", getResultName(), cuahsiNS);
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		marshaller.marshal(jaxbElement, baos2);
		baos2.close();
		writer.writeCharacters(new String(baos2.toByteArray()));
	    }

	    writer.writeEndDocument();
	    writer.close();
	    return buildResponse(new String(baos.toByteArray()));

	} catch (

	Exception ex) {
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "HYDRO_SERVER_FORMATTER_ERROR");
	}
    }

    public static String connect(String path1, String path2) {
	String connector = "";
	if (!path1.endsWith("/") && !path2.startsWith("/")) {
	    connector = "/";
	}
	return path1 + connector + path2;
    }

    protected abstract WOFRequest getWOFRequest(WebRequest webRequest);

    protected abstract JAXBElement getResult(String url, WOFRequest request, ResultSet<T> mappedResultSet) throws Exception;

    public abstract String getResponseName();

    protected String getResultName() {
	return "Result";
    }

    @Override
    public FormattingEncoding getEncoding() {

	FormattingEncoding ret = new FormattingEncoding();
	ret.setEncoding("HYD-SER-ENC");
	return ret;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private Response buildResponse(String response) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(response);
	builder = builder.type(MediaType.APPLICATION_XML);

	return builder.build();
    }

}
