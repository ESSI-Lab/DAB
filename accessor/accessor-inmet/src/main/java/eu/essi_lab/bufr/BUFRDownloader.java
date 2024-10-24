package eu.essi_lab.bufr;

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

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

public class BUFRDownloader extends WMLDataDownloader {

    private static final String INMET_DOWNLOAD_ERROR = "INMET_DOWNLOAD_ERROR";
    private String linkage;

    private static final String user = "broker";

    private static final String psw = "obsolete accessor... for the password check sources document on confluence";

    private BUFRConnector connector = new BUFRConnector();

    @Override
    public boolean canConnect() {
	return true;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	String baseEndpoint = online.getLinkage();

	if (!baseEndpoint.contains("ftp")) {
	    return;
	}

	String[] splitted = baseEndpoint.split("ftp://");

	String url = "ftp://" + user + ":" + psw + "@" + splitted[1];

	this.linkage = url;
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.BUFR_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	try {

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	    Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(temporalExtent.getBeginPosition()).get();
	    Date end = ISO8601DateTimeUtils.parseISO8601ToDate(temporalExtent.getEndPosition()).get();
	    descriptor.setTemporalDimension(begin, end);

	    ret.add(descriptor);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INMET_DOWNLOAD_ERROR, //
		    e);

	}
	return ret;

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {

	    CoreMetadata metadata = resource.getHarmonizedMetadata().getCoreMetadata();

	    String values = metadata.getDataIdentification().getSupplementalInformation();

	    String[] kvps = values.split(";");

	    CoverageDescription coverageDescription = metadata.getMIMetadata().getCoverageDescription();

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

	    for (int i = 0; i < kvps.length; i += 2) {
		String valueString = kvps[i];
		String dateString = kvps[i + 1];

		try {
		    ValueSingleVariable v = new ValueSingleVariable();

		    BigDecimal dataValue = new BigDecimal(valueString);
		    v.setValue(dataValue);
		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(new Date(Long.parseLong(dateString)));
		    XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(xmlDate);
		    addValue(tsrt, v);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
	    tmpFile.deleteOnExit();
	    JAXBWML.getInstance().marshal(response, tmpFile);

	    return tmpFile;

	} catch (

	Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading ");

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INMET_DOWNLOAD_ERROR, //
		    e);
	}

    }

}
