package eu.essi_lab.jaxb.common.schemas;

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

import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class CommonSchemas {

    private static Schema GMD;
    private static Schema GMI;
    private static Schema CSW_Discovery;

    static {
	SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	LSResourceResolver resourceResolver = new LSResourceResolver() {

	    @Override
	    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

		try {
		    if (baseURI == null) {
			if (namespaceURI.equals("http://www.isotc211.org/2005/gmd")) {
			    baseURI = "http://www.isotc211.org/2005/gmd/gmd.xsd";
			} else if (namespaceURI.equals("http://www.isotc211.org/2005/gmi")) {
			    baseURI = "http://www.isotc211.org/2005/gmi/gmi.xsd";
			} else if (namespaceURI.equals("http://www.opengis.net/cat/csw/2.0.2")) {
			    baseURI = "http://schemas.opengis.net/csw/2.0.2/csw.xsd";
			} else {
			    return null;
			}
		    }
		    URL baseURL = new URL(baseURI);
		    URL url = new URL(baseURL, systemId);
		    String localPath = url.toString().replace("http://", "schemas/").replace("local://", "schemas/");
		    if (localPath.contains("xlink.xsd") || localPath.contains("xlinks.xsd")) {
			localPath = "schemas/xlink-modified/xlink.xsd";
		    }
		    InputStream resourceStream = CommonSchemas.class.getClassLoader().getResourceAsStream(localPath);

		    if (resourceStream != null) {
			String finalBaseURI = baseURI;
			return new LSInput() {

			    @Override
			    public void setSystemId(String systemId) {
			    }

			    @Override
			    public void setStringData(String stringData) {
			    }

			    @Override
			    public void setPublicId(String publicId) {
			    }

			    @Override
			    public void setEncoding(String encoding) {
			    }

			    @Override
			    public void setCharacterStream(Reader characterStream) {
			    }

			    @Override
			    public void setCertifiedText(boolean certifiedText) {
			    }

			    @Override
			    public void setByteStream(InputStream byteStream) {
			    }

			    @Override
			    public void setBaseURI(String baseURI) {
			    }

			    @Override
			    public String getSystemId() {
				return systemId;
			    }

			    @Override
			    public String getStringData() {
				return null;
			    }

			    @Override
			    public String getPublicId() {
				return publicId;
			    }

			    @Override
			    public String getEncoding() {
				return null;
			    }

			    @Override
			    public Reader getCharacterStream() {
				return null;
			    }

			    @Override
			    public boolean getCertifiedText() {
				return false;
			    }

			    @Override
			    public InputStream getByteStream() {
				return resourceStream;
			    }

			    @Override
			    public String getBaseURI() {
				return finalBaseURI;
			    }
			};
		    } else {
			System.out.println("Schema not found: " + localPath);
			System.out.println(type + " " + namespaceURI + " " + publicId + " " + systemId + " " + baseURI);
		    }
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		}

		return null;
	    }
	};
	sf.setResourceResolver(resourceResolver);
	try {
	    GMD = sf.newSchema(new StreamSource(
		    CommonSchemas.class.getClassLoader().getResourceAsStream("schemas/www.isotc211.org/2005/gmd/gmd.xsd")));
	    GMI = sf.newSchema(new StreamSource(
		    CommonSchemas.class.getClassLoader().getResourceAsStream("schemas/www.isotc211.org/2005/gmi/gmi.xsd")));
	    CSW_Discovery = sf.newSchema(new StreamSource(
		    CommonSchemas.class.getClassLoader().getResourceAsStream("schemas/schemas.opengis.net/csw/2.0.2/csw.xsd")));
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(CommonSchemas.class).error(e.getMessage(), e);
	}
    }

    public static Schema GMD() {

	return GMD;
    }

    public static Schema GMI() {

	return GMI;
    }

    public static Schema CSW_Discovery() {

	return CSW_Discovery;
    }
}
