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

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This fake client is useful only for test purposes! It is shipped in src/main to be able to be shared between
 * different projects. It
 * simulates the responses of actual services, it can be initialized with the endpoint of the service to simulate.
 *
 * @author boldrini
 */
public class FakeHISServerClient extends CUAHSIHISServerClient1_1 {

    private static final String SITES_XML_READ_ERR = "SITES_XML_READ_ERR";
    private static final String AUGMENTED_TS_XML_READ_ERR = "AUGMENTED_TS_XML_READ_ERR";
    private static final String ERR_READING_DOC = "Error reading document from {}";

    public FakeHISServerClient(String endpoint) {
	super(endpoint);
    }

    @Override
    public Iterator<SiteInfo> getSites() throws GSException {

	String path = null;
	switch (endpoint) {
	case CUAHSIEndpoints.ENDPOINT1:
	    path = "cuahsi/mock/his1sites.xml";
	    break;
	case CUAHSIEndpoints.ENDPOINT2:
	    path = "cuahsi/mock/his2sites.xml";
	    break;
	case CUAHSIEndpoints.ENDPOINT3:
	    path = "cuahsi/mock/his3sites.xml";
	    break;
	case CUAHSIEndpoints.ENDPOINT4:
	    path = "cuahsi/mock/his4sites.xml";
	    break;
	default:
	    throw new RuntimeException("Not able to fake a client to this URL.");
	}

	InputStream res1 = FakeHISServerClient.class.getClassLoader().getResourceAsStream(path);
	SitesResponseDocument xml1 = null;
	try {
	    xml1 = new SitesResponseDocument(res1);

	    return xml1.getSitesInfo().iterator();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(ERR_READING_DOC, path);

	    throw GSException.createException(getClass(), "Can't read from " + path, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, SITES_XML_READ_ERR, e);

	}

    }

    public Iterator<SiteInfo> getSitesObjectStAX() throws GSException {
	return getSites();
    }

    @Override
    public Iterator<SiteInfo> getSitesObject() throws GSException {
	return getSites();
    }

    @Override
    public Iterator<SiteInfo> getSitesSTaX() throws GSException {
	return getSites();
    }

    @Override
    public SitesResponseDocument getSiteInfo(String networkName, String siteCode) throws GSException {

	String path = null;
	switch (endpoint) {

	case CUAHSIEndpoints.ENDPOINT2:
	    path = "cuahsi/mock/his2siteInfo.xml";
	    break;
	case CUAHSIEndpoints.ENDPOINT3:
	    path = "cuahsi/mock/his3siteInfo.xml";
	    break;
	case CUAHSIEndpoints.ENDPOINT4:
	    path = "cuahsi/mock/his4siteInfo.xml";
	    break;

	case CUAHSIEndpoints.ENDPOINT1:
	default:
	    path = "cuahsi/mock/his1siteInfo.xml";
	    break;
	}

	InputStream res1 = FakeHISServerClient.class.getClassLoader().getResourceAsStream(path);
	SitesResponseDocument xml1 = null;
	try {
	    xml1 = new SitesResponseDocument(res1);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(ERR_READING_DOC, path);

	}
	return xml1;

    }

    @Override
    public TimeSeriesResponseDocument getValues(String networkName, String siteCode, String variableCode, String methodId,
	    String qualityControlLevelCode, String sourceId, String timeBegin, String timeEnd) throws GSException {

	String path = null;
	switch (endpoint) {

	case CUAHSIEndpoints.ENDPOINT2:
	    break;
	case CUAHSIEndpoints.ENDPOINT3:
	    break;
	case CUAHSIEndpoints.ENDPOINT4:
	    path = "cuahsi/mock/his4values.xml";
	    break;

	case CUAHSIEndpoints.ENDPOINT1:
	default:
	    path = "cuahsi/mock/his1values.xml";
	    break;
	}

	InputStream res1 = FakeHISServerClient.class.getClassLoader().getResourceAsStream(path);
	TimeSeriesResponseDocument xml1 = null;
	try {
	    xml1 = new TimeSeriesResponseDocument(res1);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(ERR_READING_DOC, path);
	}
	return xml1;
    }

    @Override
    public TimeSeriesResponseDocument getValues(String networkName, String siteCode, String variableCode, String methodId,
	    String qualityControlLevelCode, String sourceId, Date timeBegin, Date timeEnd) throws GSException {
	return getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId, "", "");
    }

    @Override
    public TimeSeries getAugmentedTimeSeries(SiteInfo siteInfo, String variableCode, String methodId, String qualityControlLevelCode,
	    String sourceId) throws GSException {
	String path = null;
	switch (endpoint) {

	case CUAHSIEndpoints.ENDPOINT2:
	    break;
	case CUAHSIEndpoints.ENDPOINT3:
	    break;
	case CUAHSIEndpoints.ENDPOINT4:
	    path = "cuahsi/mock/his4wmlTS.xml";
	    break;

	case CUAHSIEndpoints.ENDPOINT1:
	default:
	    break;
	}

	InputStream res1 = FakeHISServerClient.class.getClassLoader().getResourceAsStream(path);
	TimeSeriesResponseDocument xml1 = null;
	try {
	    xml1 = new TimeSeriesResponseDocument(res1);
	    return xml1.getTimeSeries().get(0);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(ERR_READING_DOC, path);

	    throw GSException.createException(getClass(), "Can't read from " + path, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, AUGMENTED_TS_XML_READ_ERR, e);
	}

    }

}
