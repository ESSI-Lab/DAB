package eu.essi_lab.accessor.eurobis.ld;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public abstract class RDFResource {
    public static final String SEPARATOR1 = "§1§";
    public static final String SEPARATOR2 = "§2§";
    private HashMap<RDFElement, List<String>> map = new HashMap<>();

    public abstract RDFElement[] getElements();

    public String getElement(RDFElement element) {
	List<String> ret = map.get(element);
	if (ret == null || ret.isEmpty()) {
	    return null;
	}
	String r = ret.get(0);
	if (r != null) {
	    if (r.endsWith("@en")) {
		r = r.substring(0, r.length() - 3);
	    }
	}
	return r;
    }

    public List<String> getElements(RDFElement element) {
	List<String> ret = map.get(element);
	if (ret == null || ret.isEmpty()) {
	    return new ArrayList<String>();
	}
	return ret;
    }

    public List<List<String>> getElementsList(RDFElement element) {
	List<String> ret = map.get(element);
	if (ret == null || ret.isEmpty()) {
	    return new ArrayList<List<String>>();
	}
	List<List<String>> r = new ArrayList<List<String>>();
	for (String string : ret) {
	    String[] split = string.split(SEPARATOR2);
	    List<String> list = new ArrayList<String>();
	    for (String s : split) {
		list.add(s);
	    }
	    r.add(list);
	}
	return r;
    }

    public RDFResource(String url) throws IOException {
	this(getStream(url));

    }

    public static InputStream getStream(String url) {
	Downloader downloader = new Downloader();
	Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url);
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    return stream;
	}
	return null;
    }

    private static String fixDate(String dateStr) {
	String[] parts = dateStr.split("-");
	String year = parts[0];
	String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
	String day = parts[2].length() == 1 ? "0" + parts[2] : parts[2];
	return year + "-" + month + "-" + day;
    }

    public RDFResource(InputStream s) throws IOException {
	File tempTempFile = File.createTempFile(getClass().getSimpleName(), ".ttl");
	FileOutputStream fos = new FileOutputStream(tempTempFile);
	IOUtils.copy(s, fos);
	s.close();
	fos.close();

	// File tempFile = File.createTempFile(getClass().getSimpleName(), ".ttl");
	// TurtleFixer.fixFile(tempTempFile, tempFile);
	// tempTempFile.delete();
	Model model = ModelFactory.createDefaultModel();

	// Load data from a Turtle file into the model
	GSLoggerFactory.getLogger(getClass()).info("Parsing turtle");
	model.read(tempTempFile.getAbsolutePath(), "TURTLE");
	GSLoggerFactory.getLogger(getClass()).info("...parsed");

	for (RDFElement element : getElements()) {
	    String queryString = element.getQuery();
	    // System.out.println(queryString);
	    Query query = QueryFactory.create(queryString);

	    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		// Obtain the result set
		// GSLoggerFactory.getLogger(getClass()).info("Query executing");
		ResultSet results = qexec.execSelect();
		// GSLoggerFactory.getLogger(getClass()).info("Query executed");
		while (results.hasNext()) {
		    // GSLoggerFactory.getLogger(getClass()).info("Result");
		    QuerySolution soln = results.nextSolution();
		    Iterator<String> it = soln.varNames();
		    while (it.hasNext()) {
			// GSLoggerFactory.getLogger(getClass()).info("Var");
			String type = (String) it.next();
			RDFNode node = soln.get(type);
			String value = null;
			if (node.isLiteral()) {
			    value = node.asLiteral().toString();
			    // workaround fix
			    if (node.asLiteral().getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#date")) {
				value = fixDate(value);
			    }
			} else {
			    value = node.asResource().getURI();
			}
			if (value != null && !value.isEmpty()) {
			    RDFElement el = RDFElement.decode(type);
			    if (el != null) {
				List<String> values = new ArrayList<String>();
				if (el.isMultiple()) {
				    values = getList(value);
				} else {
				    if (value.contains("^^")) {
					value = value.substring(0, value.indexOf("^^"));
				    }
				    values.add(value);
				}
				if (values != null && !values.isEmpty()) {
				    map.put(el, values);
				}
			    }

			}
		    }

		}
		// GSLoggerFactory.getLogger(getClass()).info("Finished");
	    }

	}

	model.close();
	tempTempFile.delete();
    }

    private List<String> getList(String string) {
	if (string == null) {
	    return null;
	}
	String[] splitK = string.split(SEPARATOR1);
	List<String> ret = new ArrayList<String>();
	for (String k : splitK) {
	    ret.add(k);
	}
	return ret;
    }
    
    @Override
    public String toString() {
	String ret = "";
	for (RDFElement element : getElements()) {
	    Object values = null;
	    if (element.isMultiple()) {
		if (element.isList()) {
		    List<List<String>> lists = getElementsList(element);
		    String s = "[";
		    for (List<String> list : lists) {
			for (String obj : list) {
			    s += obj + " ; ";
			}
			s += "\n";
		    }
		    s += "]";
		    values = s;
		} else {
		    values = getElements(element);
		}
	    } else {
		values = getElement(element);
	    }
	    ret = element.name() + ": " + values;
	}	
	return ret ;
    }
    

    public void print() {
	System.out.println(toString());
    }

}
