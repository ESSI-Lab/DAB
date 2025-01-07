package eu.essi_lab.lib.net.utils;

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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author Fabrizio
 */
public class WebConnector {

    private static XMLReader tagsoupReader;

    private Downloader downloader = new Downloader();

    static {
	try {
	    tagsoupReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
	    tagsoupReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
	} catch (Exception e) {

	}
    }

    /**
     * Returns the href linked from the web page at the given url.
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public List<String> getHrefs(String url, Date date) {

	ArrayList<String> ret = new ArrayList<String>();

	try {
	    Optional<InputStream> is = downloader.downloadOptionalStream(url);
	    if (is.isPresent()) {
		InputSource input = new InputSource(is.get());

		SAXSource source = new SAXSource(tagsoupReader, input);
		DOMResult result = new DOMResult();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(source, result);

		XMLDocumentReader xdoc = new XMLDocumentReader((Document) result.getNode());

		List<String> hrefs = xdoc.evaluateTextContent("//@href").//
			stream().//
			map(ref -> resolveHref(url, ref)).//
			filter(Objects::nonNull).//
			collect(Collectors.toList());

		if (date != null) {
		    hrefs = filterOutPreviousDate(hrefs, date);
		}

		return hrefs;
	    } else {
		return ret;
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(this.getClass()).error("Error occurred, unable to get hrefs from {} ", url);
	    GSLoggerFactory.getLogger(this.getClass()).error(e);

	    return ret;
	}
    }

    /**
     * @param hrefs
     * @param date
     * @return
     */
    private List<String> filterOutPreviousDate(List<String> hrefs, Date date) {
	List<String> ret = new ArrayList<String>();
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH) + 1;
	int day = calendar.get(Calendar.DAY_OF_MONTH);

	for (String s : hrefs) {
	    String[] splittedHref = s.split("PRISMADATA/");
	    if (splittedHref.length > 1) {
		String tmp = splittedHref[1];
		String[] splittedTime = tmp.split("/");
		if (splittedTime.length == 1) {
		    // year case
		    if (year <= Integer.valueOf(splittedTime[0])) {
			ret.add(s);
		    }
		} else if (splittedTime.length == 2) {
		    // month case
		    if (year < Integer.valueOf(splittedTime[0])) {
			ret.add(s);
		    } else if (month <= Integer.valueOf(splittedTime[1].trim().substring(0, 2))
			    && year == Integer.valueOf(splittedTime[0])) {
			ret.add(s);
		    }

		} else if (splittedTime.length == 3) {
		    // day case
		    if (year < Integer.valueOf(splittedTime[0]) || (year == Integer.valueOf(splittedTime[0])
			    && month < Integer.valueOf(splittedTime[1].trim().substring(0, 2)))) {
			ret.add(s);
		    } else if (day <= Integer.valueOf(splittedTime[2].trim())
			    && month == Integer.valueOf(splittedTime[1].trim().substring(0, 2))
			    && year == Integer.valueOf(splittedTime[0])) {
			ret.add(s);
		    }
		}
	    }
	}
	return ret;
    }

    /**
     * @param baseUrl
     * @param childUrl
     * @return
     */
    private String resolveHref(String baseUrl, String childUrl) {

	baseUrl = baseUrl.trim();
	childUrl = childUrl.trim();

	String baseURL_ = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

	if (childUrl.startsWith("/") || childUrl.startsWith("..") || childUrl.startsWith("?")) {
	    return null;
	}

	try {
	    URL url = new URL(baseURL_);
	    URL ret = new URL(url, childUrl);
	    return ret.toString();
	} catch (Exception e) {
	    return childUrl;
	}
    }

    public static void main(String[] args) throws JAXBException {
	String e = "http://90.147.167.250/data/PRISMADATA/2019/";
	String[] splitted = e.split("PRISMADATA/");
	if (splitted.length > 1) {
	    String tmp = splitted[1];
	    String[] ssad = tmp.split("/");
	    System.out.println(ssad.length);

	}

	Date today = new Date();
	Date d = new Date(today.getTime() - 24 * 60 * 60 * 1000);
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(d);
	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH) + 1;
	int day = calendar.get(Calendar.DAY_OF_MONTH);
	System.out.println(year);
	System.out.println(month);
	System.out.println(day);
	calendar.add(Calendar.DATE, -2);
	year = calendar.get(Calendar.YEAR);
	month = calendar.get(Calendar.MONTH) + 1;
	day = calendar.get(Calendar.DAY_OF_MONTH);
	System.out.println(year);
	System.out.println(month);
	System.out.println(day);

	// WebConnector c = new WebConnector();
	// List<String> urls = c.getHrefs(e);
	// for (String url : urls) {
	// System.out.println(url);
	// }

	// GMD.getInstance().getUnmarshaller();

    }

}
