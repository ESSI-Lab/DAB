/**
 * This file is part of SDI HydroServer Accessor. SDI HydroServer Accessor is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version. SDI HydroServer Accessor is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with SDI HydroServer Accessor. If not, see <http://www.gnu.org/licenses/>. Copyright (C)
 * 2009-2011 ESSI-Lab <info@essi-lab.eu>
 */

package eu.essi_lab.accessor.wof.client;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;

import eu.essi_lab.accessor.wof.client.datamodel.ServiceInfo;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.exceptions.GSException;

public class CUAHSIHISCentralClient {

    public static HashMap<String, Boolean> concepts = new HashMap<String, Boolean>();

    public static final String defaultEndpoint = "http://hiscentral.cuahsi.org/webservices/hiscentral.asmx";

    public String endpoint = null;

    static String defaultBeginTime = "1900";
    static String defaultEndTime = "2020";

    public CUAHSIHISCentralClient() {
	this(defaultEndpoint);
    }

    public CUAHSIHISCentralClient(String endpoint) {
	this.endpoint = endpoint;
    }

    // public SeriesArrayDocument getSeries(AreaBond area, TimeBond time, String conceptKeyword, String networkIds,
    // String page, int timeout)
    // throws Exception {
    // String minx = area == null ? "-180" : "" + area.getWest();
    // String maxx = area == null ? "180" : "" + area.getEast();
    // String miny = area == null ? "-90" : "" + area.getSouth();
    // String maxy = area == null ? "90" : "" + area.getNorth();
    //
    // String timeBegin = time == null ? defaultBeginTime : time.getSince8601Time();
    // String timeEnd = time == null ? defaultEndTime : time.getTo8601Time();
    //
    // String url = endpoint + "/getSeriesCatalogInBoxPaged" + //
    // "?xmin=" + addValue(minx) + //
    // "&xmax=" + addValue(maxx) + //
    // "&ymin=" + addValue(miny) + //
    // "&ymax=" + addValue(maxy) + //
    // "&conceptKeyword=" + addValue(conceptKeyword) + //
    // "&networkIDs=" + addValue(networkIds) + //
    // "&beginDate=" + addValue(timeBegin) + //
    // "&endDate=" + addValue(timeEnd) + //
    // "&pageno=" + addValue(page);
    //
    // CommonLogger.getInstance().detail(this, "Sending request: " + url);
    // final GetRequest get = new GetRequest(url);
    // get.setTimeout(timeout);
    //
    // TimeoutOperation<SeriesArrayDocument> operation = new TimeoutOperation<SeriesArrayDocument>(timeout) {
    //
    // SeriesArrayDocument ret = null;
    //
    // @Override
    // public SeriesArrayDocument getResult() {
    // return ret;
    // }
    //
    // @Override
    // protected void doOperation() throws Exception {
    // // boolean responseReceived = false;
    // // while (!responseReceived) {
    // // try {
    // Response response = get.execRequest();
    // InputStream stream = response.getResponseBodyAsStream();
    // ret = new SeriesArrayDocument(stream);
    //
    // if (ret != null && ret.evaluateXPath("local-name(/*[1])='ArrayOfSeriesRecord'").asBoolean()) {
    // // responseReceived = true;
    // } else {
    // CommonLogger.getInstance().detail(this, "not array series document at: " + get.toString());
    // Thread.sleep(1000);
    // throw new IllegalArgumentException("not a correct response");
    // }
    // // } catch (Exception e) {
    // // CommonLogger.getInstance().debug(this,e.getMessage());
    // // CommonLogger.getInstance().debug(this,"error at: " + get.toString());
    // // Thread.sleep(1000);
    // // }
    // // }
    // }
    //
    // @Override
    // public void timeOutAction() {
    // get.abort();
    // }
    // };
    // operation.start();
    // SeriesArrayDocument ret = operation.getResult();
    // if (operation.errorOccurred()) {
    // return null;
    // }
    // if (operation.timeoutOccurred()) {
    // throw new org.apache.http.conn.ConnectTimeoutException("timeout receiving response from his central");
    // }
    // return ret;
    //
    // }

    // public OntologyNodeDocument getOntologyTree(String conceptKeyword) throws Exception {
    //
    // String url = endpoint + "/getOntologyTree" + //
    // "?conceptKeyword=" + addValue(conceptKeyword);
    //
    // CommonLogger.getInstance().detail(this, "Sending request: " + url);
    // GetRequest get = new GetRequest(url);
    // Response response = get.execRequest();
    // InputStream stream = response.getResponseBodyAsStream();
    // OntologyNodeDocument document = new OntologyNodeDocument(stream);
    // return document;
    // }
    //
    // public boolean conceptExists(String concept) throws Exception {
    // return !getOntologyTree(concept).getConceptId().equals("0");
    // }

    public String getEndpoint() {
	return endpoint;
    }

    public List<ServiceInfo> getServicesInBox(String minx, String miny, String maxx, String maxy) throws GSException {
	List<ServiceInfo> ret = new ArrayList<ServiceInfo>();

	Downloader downloader = new Downloader();
	String request = endpoint;
	if (!request.endsWith("/")) {
	    request += "/";
	}
	request += "GetServicesInBox2?xmin=" + minx + "&ymin=" + miny + "&xmax=" + maxx + "&ymax=" + maxy;
	try {
	    String response = downloader.downloadOptionalString(request).get();
	    XMLDocumentReader documentReader = new XMLDocumentReader(response);
	    Node[] result = null;
	    result = documentReader.evaluateNodes("//*[local-name(.)='ServiceInfo']");
	    for (Node node : result) {
		XMLNodeReader reader = new XMLNodeReader(node);
		ret.add(new ServiceInfo(reader));
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;
    }

}
