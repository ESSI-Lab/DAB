package eu.essi_lab.accessor.wof.discovery.sites;

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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLStreamWriterUtils;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;

public class GetSitesObjectHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    private String west;
	    private String east;
	    private String north;
	    private String south;
	    private String platformName;
	    private String platformCode;
	    private String sourceID;
	    private String country;

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		try {

		    GetSitesObjectFastTransformer transformer = new GetSitesObjectFastTransformer();
		    DiscoveryMessage discoveryMessage = transformer.transform(webRequest);
		    discoveryMessage.setDistinctValuesElement(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

		    String cuahsiNS = "http://www.cuahsi.org/his/1.1/ws/";
		    String wmlNS = "http://www.cuahsi.org/waterML/1.1/";

		    XMLStreamWriter writer = XMLStreamWriterUtils.getSOAPWriter(output);

		    writer.writeStartElement("", "GetSitesObjectResponse", cuahsiNS);
		    writer.writeNamespace("", cuahsiNS);

		    UriInfo uri = webRequest.getUriInfo();
		    String path = webRequest.getRequestPath();
		    String url = GetSitesResultSetFormatter.connect(uri.getBaseUri().toString(), path);

		    writer.writeStartElement("", "sitesResponse", wmlNS);
		    writer.writeNamespace("", wmlNS);

		    writer.writeStartElement("", "queryInfo", wmlNS);
		    writer.writeStartElement("", "creationTime", wmlNS);
		    writer.writeCharacters(ISO8601DateTimeUtils.getISO8601DateTime());
		    writer.writeEndElement();
		    writer.writeStartElement("", "queryURL", wmlNS);
		    writer.writeCharacters(url);
		    writer.writeEndElement();
		    writer.writeStartElement("", "criteria", wmlNS);
		    writer.writeAttribute("MethodCalled", "GetSitesObject");
		    writer.writeStartElement("", "parameter", wmlNS);
		    writer.writeAttribute("name", "format");
		    writer.writeAttribute("value", "WML1");
		    writer.writeEndElement();
		    writer.writeEndElement();
		    writer.writeStartElement("", "note", wmlNS);
		    writer.writeCharacters("Discovery and Access Broker");
		    writer.writeEndElement();
		    writer.writeEndElement();

		    Page userPage = discoveryMessage.getPage();
		    int userStart = userPage.getStart();
		    int userSize = userPage.getSize();

		    int maxPageSize = 1000;
		    Page tmpPage = new Page(userStart, Math.min(userSize, maxPageSize));
		    discoveryMessage.setPage(tmpPage);
		    int totalExpected = 0;
		    int currentSize = 0;
		    int totalSize = 0;
		    
		    Optional<Bond> optUserBond = discoveryMessage.getUserBond();
		    Bond userBond = null;
		    
		    if(optUserBond.isPresent()){
			
			userBond = optUserBond.get();
		    }
		    
		    do {

			try {
			    
			    discoveryMessage.setUserBond(userBond);
			    
			    ResultSet<String> resultSet = exec(discoveryMessage);

			    List<String> results = resultSet.getResultsList();

			    for (String result : results) {

				StAXDocumentParser parser = new StAXDocumentParser(result);

				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "west"), v -> west = v);
				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "south"), v -> south = v);
				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "east"), v -> east = v);
				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "north"), v -> north = v);

				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "platformTitle"), v -> platformName = v);
				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "uniquePlatformId"), v -> platformCode = v);
				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "sourceId"), v -> sourceID = v);
				parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "Country"), v -> country = v);

				parser.parse();

				if (west != null && !west.equals("") && east != null && !east.equals("") && north != null
					&& !north.equals("") && south != null && !south.equals("") && platformName != null
					&& !platformName.equals("")//
					&& platformCode != null && !platformCode.equals("")//
				// && sourceID != null && !sourceID.equals("")//
				) {
				    writer.writeStartElement("", "site", wmlNS);
				    writer.writeStartElement("", "siteInfo", wmlNS);
				    writer.writeStartElement("", "siteName", wmlNS);
				    writer.writeCharacters(platformName);
				    writer.writeEndElement();
				    writer.writeStartElement("", "siteCode", wmlNS);
				    if (sourceID != null && !sourceID.isEmpty()) {
					writer.writeAttribute("network", sourceID);
				    } else {
					writer.writeAttribute("network", "default");
				    }
				    writer.writeAttribute("siteID", "1");
				    writer.writeCharacters(platformCode);
				    writer.writeEndElement();

				    writer.writeStartElement("", "geoLocation", wmlNS);
				    writer.writeStartElement("", "geogLocation", wmlNS);
				    writer.writeAttribute("srs", "EPSG:4326");
				    writer.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "LatLonPointType");
				    writer.writeStartElement("", "latitude", wmlNS);
				    writer.writeCharacters(south);
				    writer.writeEndElement();
				    writer.writeStartElement("", "longitude", wmlNS);
				    writer.writeCharacters(west);
				    writer.writeEndElement();
				    writer.writeEndElement();
				    writer.writeEndElement();

				    if (country != null && !country.isEmpty()) {
					writer.writeStartElement("", "siteProperty", wmlNS);
					writer.writeAttribute("name", "Country");
					writer.writeCharacters(country);
					writer.writeEndElement();
				    }

				    writer.writeEndElement();
				    writer.writeEndElement();
				    writer.flush();
				}

			    }
			    tmpPage.setStart(tmpPage.getStart() + maxPageSize);
			    totalExpected = resultSet.getCountResponse().getCount();
			    currentSize = results.size();
			    totalSize += currentSize;
			    tmpPage.setSize(Math.min(maxPageSize, userSize - totalSize));

			} catch (Exception e) {
			    e.printStackTrace();
			}

			// tmpPage.setSize(defaultPageSize)

		    } while (totalSize < userSize && currentSize != 0);

		    writer.writeEndDocument();
		    writer.flush();
		    writer.close();
		    output.close();

		} catch (Exception ex) {
		    throw new WebApplicationException(ex.getMessage());
		}

	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_XML_TYPE;
    }

}
