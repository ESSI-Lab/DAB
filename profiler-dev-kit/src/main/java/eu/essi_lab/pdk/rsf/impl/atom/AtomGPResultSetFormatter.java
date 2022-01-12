package eu.essi_lab.pdk.rsf.impl.atom;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.json.XML;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

/**
 * @author Fabrizio
 */
public class AtomGPResultSetFormatter extends DiscoveryResultSetFormatter<String> {

    /**
     * Max items of the merged tf map
     */
    private static final int MAX_ITEMS_COUNT = 100;

    private static final String USGS_EARTHQUAKE_SOURCE_ID = "UUID-e101622c-6ae1-4b68-bfea-8acf067e31dd";

    @Override
    public Response format(DiscoveryMessage message, ResultSet<String> messageResponse) throws GSException {

	String scheme = message.getWebRequest().getUriInfo().getRequestUri().getScheme();
	GSLoggerFactory.getLogger(getClass()).info("Request scheme: {}", scheme);

	StringBuilder out = new StringBuilder();

	out.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n ");
	out.append("xsi:schemaLocation=\"http://www.w3.org/2005/Atom http://essi-lab.eu/schemas/geo-rss/atom_1.0_specification.xsd  \n");
	out.append("http://www.georss.org/georss http://essi-lab.eu/schemas/geo-rss/geo_rss_1.1_specification.xsd  \n");
	out.append("http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd\">  \n");
	out.append("<title>Search results</title>  \n");
	out.append("<id>" + UUID.randomUUID().toString() + "</id>  \n");
	out.append("<link href=\"" + message.getWebRequest().getUriInfo().getRequestUri().//
		toString().//
		replace("&", "&amp;").//
		replace("http://", "https://") + "\"  \n");
	out.append(" rel=\"search\" title=\"Content Search\" type=\"application/atom+xml\"/>  \n");

	out.append("<totalResults xmlns=\"http://a9.com/-/spec/opensearch/1.1/\">" + messageResponse.getCountResponse().getCount()
		+ "</totalResults>  \n");

	out.append("<startIndex xmlns=\"http://a9.com/-/spec/opensearch/1.1/\">" + message.getPage().getStart() + "</startIndex>  \n");
	out.append("<itemsPerPage xmlns=\"http://a9.com/-/spec/opensearch/1.1/\">" + message.getPage().getSize() + "</itemsPerPage>  \n");

	for (String result : messageResponse.getResultsList()) {
	    out.append(result);
	}

	List<GSSource> sources = message.getSources();

	boolean usgsSource = sources.size() == 1 && //
		sources.get(0).getUniqueIdentifier().equals(USGS_EARTHQUAKE_SOURCE_ID);

	if (!message.isOutputSources() && !usgsSource) {// the portal do not expects any map from a USGS Earthquake
							// query

	    Optional<TermFrequencyMap> map = messageResponse.getCountResponse().mergeTermFrequencyMaps(MAX_ITEMS_COUNT);

	    if (map.isPresent()) {

		List<TermFrequencyItem> items = map.get().getItems(TermFrequencyTarget.SOURCE);
		//
		// update for GIP-186
		//
		for (TermFrequencyItem termFrequencyItem : items) {
		    Optional<String> firstTF = sources.stream().//
			    filter(s -> s.getUniqueIdentifier().equals(termFrequencyItem.getTerm())).//
			    map(s -> s.getLabel()).//
			    findFirst();
		    if (firstTF.isPresent())
			termFrequencyItem.setDecodedTerm(XML.escape(firstTF.get()));
		}

		try {
		    String mapString = map.get().asString(true);
		    mapString = mapString.replace("<gs:", "<dm:");
		    mapString = mapString.replace("</gs:", "</dm:");
		    mapString = mapString.replace("sourceId", "source");
		    mapString = mapString.replace("result", "item");

		    for (GSSource gsSource : sources) {
			String id = XML.escape(gsSource.getUniqueIdentifier());
			String label = XML.escape(gsSource.getLabel());
			String sourceId = "<dm:sourceId>";
			mapString = mapString.replace("<dm:term>" + id + "</dm:term>", sourceId + id + "</dm:sourceId>");
			mapString = mapString.replace(sourceId + id + "</dm:sourceId>",
				sourceId + id + "</dm:sourceId>\n<dm:term>" + label + "</dm:term>");
		    }

		    mapString = mapString.replace("xmlns:gs", "xmlns:dm");
		    mapString = mapString.replace("http://flora.eu/gi-suite/1.0/dataModel/schema",
			    "http://floraresearch.eu/sdi/services/7.0/dataModel/schema");

		    mapString = mapString.replace("<dm:instrumentId/>", "");
		    mapString = mapString.replace("<dm:platformId/>", "");
		    mapString = mapString.replace("<dm:origOrgId/>", "");
		    mapString = mapString.replace("<dm:attributeId/>", "");

		    mapString = mapString.replace(
			    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"", "");

		    out.append(mapString);

		} catch (UnsupportedEncodingException | JAXBException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	}

	out.append("</feed> ");

	ResponseBuilder builder = Response.status(Status.OK);
	builder = builder.entity(out.toString());
	builder = builder.type(MediaType.APPLICATION_XML_TYPE);

	return builder.build();
    }
    
    /**
     * 
     * @return
     */
    public static String getEmptyFeed(String query) {
	
	return getEmptyFeed(query, null);
    }
    
    /**
     * 
     * @return
     */
    public static String getEmptyFeed(String query, String error) {
	
	String errorElement = error == null ? "" : "<error>"+error+"</error>";
	
	return "<feed xmlns='http://www.w3.org/2005/Atom' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.w3.org/2005/Atom http://essi-lab.eu/schemas/geo-rss/atom_1.0_specification.xsd http://www.georss.org/georss http://essi-lab.eu/schemas/geo-rss/geo_rss_1.1_specification.xsd http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd'>\n"+
	"<title>Search results</title>\n"+
	"<id>a4a4e2d9-c81d-4167-8556-b40464d638b6</id>\n"+
	"<link href='"+query+"' rel='search' title='Content Search' type='application/atom+xml'/>\n"+
	"<totalResults xmlns='http://a9.com/-/spec/opensearch/1.1/'>0</totalResults>\n"+
	"<startIndex xmlns='http://a9.com/-/spec/opensearch/1.1/'>1</startIndex>\n"+
	"<itemsPerPage xmlns='http://a9.com/-/spec/opensearch/1.1/'>10</itemsPerPage>\n"+
	errorElement+"\n"+
	"</feed>";
    }

    @Override
    public FormattingEncoding getEncoding() {
	return null;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

}
