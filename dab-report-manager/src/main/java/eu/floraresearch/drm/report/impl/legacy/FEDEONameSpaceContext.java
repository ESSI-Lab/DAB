package eu.floraresearch.drm.report.impl.legacy;
 
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class FEDEONameSpaceContext implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {

	if (prefix.equals("os")) {
	    return "http://a9.com/-/spec/opensearch/1.1/";
	}

	if (prefix.equals("time")) {
	    return "http://a9.com/-/opensearch/extensions/time/1.0/";
	}

	if (prefix.equals("georss")) {
	    return "http://www.georss.org/georss";
	}

	if (prefix.equals("geo")) {
	    return "http://a9.com/-/opensearch/extensions/geo/1.0/";
	}

	if (prefix.equals("dc")) {
	    return "http://purl.org/dc/elements/1.1/";
	}

	if (prefix.equals("")) {
	    return "http://www.w3.org/2005/Atom";
	}

	if (prefix.equals("gml")) {
	    return "http://www.opengis.net/gml";
	}
	return null;
    }

    @Override
    public String getPrefix(String namespaceURI) {

	if (namespaceURI.equals("http://a9.com/-/spec/opensearch/1.1/")) {
	    return "os";
	}
	if (namespaceURI.equals("http://a9.com/-/opensearch/extensions/time/1.0/")) {
	    return "time";
	}
	if (namespaceURI.equals("http://www.georss.org/georss")) {
	    return "georss";
	}
	if (namespaceURI.equals("http://a9.com/-/opensearch/extensions/geo/1.0/")) {
	    return "geo";
	}
	if (namespaceURI.equals("http://purl.org/dc/elements/1.1/")) {
	    return "dc";
	}
	if (namespaceURI.equals("http://www.w3.org/2005/Atom")) {
	    return "";
	}
	if (namespaceURI.equals("http://www.opengis.net/gml")) {
	    return "gml";
	}
	return null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {

	ArrayList<String> list = new ArrayList<String>();
	list.add("os");
	list.add("time");
	list.add("georss");
	list.add("geo");
	list.add("dc");
	list.add("");
	list.add("gml");

	return list.iterator();
    }
}
