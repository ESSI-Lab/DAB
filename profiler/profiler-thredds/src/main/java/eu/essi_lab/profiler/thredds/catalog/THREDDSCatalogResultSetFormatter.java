package eu.essi_lab.profiler.thredds.catalog;

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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.thredds._1_0_6.Catalog;
import eu.essi_lab.thredds._1_0_6.DatasetType;
import eu.essi_lab.thredds._1_0_6.Metadata;
import eu.essi_lab.thredds._1_0_6.Service;
import eu.essi_lab.thredds._1_0_6.factory.JAXBTHREDDS;

public class THREDDSCatalogResultSetFormatter extends DiscoveryResultSetFormatter<JAXBElement<DatasetType>> {

    private static final String THREDDS_CATALOG_RESULT_SET_FORMATTER_ERROR = "THREDDS_CATALOG_RESULT_SET_FORMATTER_ERROR";

    @Override
    public Response format(DiscoveryMessage message, ResultSet<JAXBElement<DatasetType>> messageResponse) throws GSException {

	String path = message.getWebRequest().getServletRequest().getContextPath() + message.getWebRequest().getServicesPath()
		+ message.getWebRequest().getRequestPath();

	String id = message.getWebRequest().getQueryString();
	if (id != null && id.contains("id=")) {
	    id = id.replace("id=", "");
	} else {
	    id = null;
	}

	boolean html = false;
	if (path.endsWith(".html")) {
	    html = true;
	}
	String relativePath = path.substring(1, path.lastIndexOf("/"));

	try {
	    Catalog cat = new Catalog();
	    Service services = new Service();
	    services.setName("VirtualServices");
	    services.setBase("");
	    services.setServiceType("Compound");
	    Service http = new Service();
	    http.setName("HTTPServer");
	    http.setServiceType("HTTPServer");
	    http.setBase(relativePath + "/fileServer/");
	    services.getService().add(http);
	    Service wms = new Service();
	    wms.setName("wmsServer");
	    wms.setServiceType("WMS");
	    wms.setBase(relativePath.substring(0, relativePath.length() - 7) + "wms/");
	    services.getService().add(wms);
	    Service ncss = new Service();
	    ncss.setName("ncss");
	    ncss.setServiceType("NetcdfSubset");
	    ncss.setBase(relativePath + "/ncss/");
	    services.getService().add(ncss);
	    Service dods = new Service();
	    dods.setName("ncdods");
	    dods.setServiceType("OPENDAP");
	    dods.setBase(relativePath + "/dodsC/");
	    services.getService().add(dods);
	    cat.getService().add(services);

	    List<JAXBElement<DatasetType>> results = messageResponse.getResultsList();
	    DatasetType catDataset = new DatasetType();
	    String msg = "Brokered datasets";
	    Optional<View> optionalView = message.getView();
	    if (optionalView.isPresent()) {
		View view = optionalView.get();
		msg += " from view: " + view.getLabel();
	    }
	    catDataset.setName(msg);
	    Metadata metadata = JAXBTHREDDS.getInstance().getFactory().createMetadata();
	    metadata.setInherited(true);
	    metadata.getAny().add(JAXBTHREDDS.getInstance().getFactory().createDatasetTypeServiceName("VirtualServices"));
	    catDataset.getThreddsMetadataGroup().add(metadata);
	    cat.getDataset().add(JAXBTHREDDS.getInstance().getFactory().createDataset(catDataset));
	    for (JAXBElement<DatasetType> result : results) {
		catDataset.getDataset().add(result);
	    }
	    cat.setName("root-catalog");
	    cat.setVersion("1.0.1");

	    ResponseBuilder builder = Response.status(Status.OK);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    Marshaller marshaller = JAXBTHREDDS.getInstance().getMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
		    "http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0 http://www.unidata.ucar.edu/schemas/thredds/InvCatalog.1.0.6.xsd");
	    marshaller.marshal(cat, baos);

	    // IOUtils.copy(THREDDSCatalogResultSetFormatter.class.getClassLoader().getResourceAsStream("demo.xml"),
	    // baos);
	    String response = baos.toString("UTF-8");
	    baos.close();

	    if (html) {

		response = transformToHtml(response, id);

		response = response.replace("${URL}", path);

		String hostname = message.getWebRequest().getUriInfo().getBaseUri().toString();
		String hostname2 = hostname.replace("//", "__");
		hostname = hostname.substring(0, hostname2.indexOf("/") + 1);

		response = response.replace("${HOSTNAME}", hostname);

		builder = builder.type(MediaType.TEXT_HTML);
	    } else {
		builder = builder.type(MediaType.APPLICATION_XML);
	    }

	    builder = builder.entity(response);

	    return builder.build();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    THREDDS_CATALOG_RESULT_SET_FORMATTER_ERROR, //
		    e);
	}
    }

    private String transformToHtml(String catalog, String id) {
	try {
	    // Create transformer factory
	    TransformerFactory factory = TransformerFactory.newInstance();

	    // Use the factory to create a template containing the xsl file
	    InputStream xslStream;
	    if (id == null) {
		xslStream = getClass().getClassLoader().getResourceAsStream("xslt/catalog-1.0.1.xsl");
	    } else {
		xslStream = getClass().getClassLoader().getResourceAsStream("xslt/dataset-1.0.1.xsl");
	    }
	    Templates template = factory.newTemplates(new StreamSource(xslStream));

	    // Use the template to create a transformer
	    Transformer xformer = template.newTransformer();
	    if (id != null) {
		xformer.setParameter("id", id);
	    }

	    // Prepare the input and output files
	    ByteArrayInputStream bis = new ByteArrayInputStream(catalog.getBytes());
	    Source source = new StreamSource(bis);
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    Result result = new StreamResult(bos);
	    // Apply the xsl file to the source file and write the result
	    // to the output file
	    xformer.transform(source, result);
	    xslStream.close();
	    bis.close();
	    bos.close();
	    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
	} catch (Exception e) {
	    return catalog;
	}
    }

    @Override
    public FormattingEncoding getEncoding() {
	FormattingEncoding ret = new FormattingEncoding();
	ret.setEncoding("THREDDS");
	ret.setEncodingVersion("1.0.6");
	ret.setMediaType(MediaType.APPLICATION_XML_TYPE);
	return ret;
    }

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

}
