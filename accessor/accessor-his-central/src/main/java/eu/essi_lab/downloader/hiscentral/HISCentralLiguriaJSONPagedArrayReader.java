package eu.essi_lab.downloader.hiscentral;

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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HISCentralLiguriaJSONPagedArrayReader implements Closeable {

    private JsonParser parser;
    private ObjectMapper mapper;
    private boolean hasMore;
    private List<Link> links = new ArrayList<>();
    private boolean insideValuesArray = false;

    public HISCentralLiguriaJSONPagedArrayReader(File file) throws IOException {
	this.mapper = new ObjectMapper();
	this.parser = new JsonFactory().createParser(file);
	init();
	this.parser.close();
	this.parser = new JsonFactory().createParser(file);
	forward();
    }
    private void forward() throws IOException {
   	if (parser.nextToken() != JsonToken.START_OBJECT) {
   	    throw new IllegalStateException("Expected JSON object");
   	}

   	while (parser.nextToken() != JsonToken.END_OBJECT) {
   	    String fieldName = parser.currentName();
   	    // parser.nextToken(); // Move to field value

   	    if (fieldName == null) {
   		continue;
   	    }
   	    switch (fieldName) {
   	    case "items":
   		insideValuesArray = true;
   		
   		return;

   	    default:
   		parser.nextToken();
   	    }
   	}
       }

    private void init() throws IOException {
	if (parser.nextToken() != JsonToken.START_OBJECT) {
	    throw new IllegalStateException("Expected JSON object");
	}

	while (parser.nextToken() != JsonToken.END_OBJECT) {
	    String fieldName = parser.currentName();
	    // parser.nextToken(); // Move to field value

	    if (fieldName == null) {
		continue;
	    }
	    switch (fieldName) {
	    case "hasMore":
		parser.nextToken();
		this.hasMore = parser.getBooleanValue();
		break;
	    case "links":
		if (parser.currentToken() == JsonToken.START_ARRAY) {
		    while (parser.nextToken() != JsonToken.END_ARRAY) {
			Link link = mapper.readValue(parser, Link.class);
			links.add(link);
		    }
		}
		break;

	    case "items":
		insideValuesArray = true;
		parser.skipChildren();

		break;

	    default:
	    }
	}
    }

    public boolean hasMore() {
	return hasMore;
    }

    public List<Link> getLinks() {
	return links;
    }

    public String nextValue() throws IOException {
	if (!insideValuesArray)
	    return null;

	JsonToken token = parser.nextToken();
	if (token == JsonToken.END_ARRAY) {
	    insideValuesArray = false;
	    return null;
	}

	TreeNode ret = mapper.readTree(parser);
	if (ret == null) {
	    return null;
	}

	return ret.toString();

    }

    public boolean hasNextValue() throws IOException {
	if (!insideValuesArray)
	    return false;
	JsonToken next = parser.nextToken();
	if (next == JsonToken.END_ARRAY) {
	    insideValuesArray = false;
	    return false;
	}
	return true;
    }

    @Override
    public void close() throws IOException {
	parser.close();
    }

    public static class Link {
	public String href;
	public String rel;
    }

    public static void main(String[] args) throws IOException {
	File file = new File("/tmp/HISCentralLiguriaDownloader8974474593964303459.json");
	HISCentralLiguriaJSONPagedArrayReader reader = new HISCentralLiguriaJSONPagedArrayReader(file);
	while (reader.hasNextValue()) {
	    String str = reader.nextValue();
	    System.out.println(str);

	}
	System.out.println(reader.hasMore());
	List<Link> ls = reader.getLinks();
	for (Link l : ls) {
	    System.out.println(l.href + " " + l.rel);
	}
	reader.close();
    }
}
