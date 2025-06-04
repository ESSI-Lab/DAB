/**
 * 
 */
package eu.essi_lab.accessor.sos.downloader._1_0_0;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPathExpressionException;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.WMLDataDownloaderAdapter;
import eu.essi_lab.accessor.sos.AbstractSOSConnector;
import eu.essi_lab.accessor.sos.SOSConnector;
import eu.essi_lab.accessor.sos.SOSIdentifierMangler;
import eu.essi_lab.accessor.sos.SOSRequestBuilder;
import eu.essi_lab.accessor.sos._1_0_0.SOSSensorML;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public class SOSDownloader extends eu.essi_lab.accessor.sos.downloader.SOSDownloader {

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();

	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	String name = online.getName();

	if (name != null) {

	    SOSIdentifierMangler mangler = new SOSIdentifierMangler();
	    mangler.setMangling(name);

	    String procedureHref = mangler.getProcedure();

	    SOSSensorML sosSensorML = eu.essi_lab.accessor.sos._1_0_0.SOSConnector.PROCEDURES_MAP.get(procedureHref);

	    if (sosSensorML == null) {

		sosSensorML = describeSensor(procedureHref);

	    } else {

		eu.essi_lab.accessor.sos._1_0_0.SOSConnector.PROCEDURES_MAP.remove(procedureHref);
	    }

	    try {

		Optional<TemporalExtent> optTempExtent = sosSensorML.getTemporalExtent();

		if (optTempExtent.isPresent()) {

		    TemporalExtent tempExtent = optTempExtent.get();

		    Date beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(tempExtent.getBeginPosition()).get();
		    Date endDate = ISO8601DateTimeUtils.parseISO8601ToDate(tempExtent.getEndPosition()).get();

		    descriptor.setTemporalDimension(beginDate, endDate);
		}

		ret.add(descriptor);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		throw GSException.createException(getClass(), "SOS_1_0_0_DownloaderGetRemoteDescriptorsError", e);
	    }

	}

	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String name = online.getName();

	if (name == null) {

	    throw GSException.createException(getClass(), //
		    "Missing online name: " + online.getIdentifier(), //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SOS_1_0_0_DownloaderMissingOnlineNameError"); //
	}

	DataDimension temporalDimension = descriptor.getTemporalDimension();

	if (temporalDimension == null) {

	    throw GSException.createException(getClass(), //
		    "Missing temporal dimension name: " + online.getName(), //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SOS_1_0_0_DownloaderMissingTemporalDimentionError"); //
	}

	ContinueDimension dimension = temporalDimension.getContinueDimension();

	Number lower = dimension.getLower();
	Number upper = dimension.getUpper();

	Date begin = new Date(lower.longValue());
	Date end = new Date(upper.longValue());

	SOSIdentifierMangler mangler = new SOSIdentifierMangler();
	mangler.setMangling(name);

	String featureIdentifier = mangler.getFeature();
	String property = mangler.getObservedProperty();
	String procedure = mangler.getProcedure();

	 AbstractSOSConnector connector = getConnector();
	connector.setSourceURL(linkage);

	try {

	    SOSRequestBuilder builder = connector.createRequestBuilder();
	    String dataRequest = builder.createDataRequest(procedure, featureIdentifier, property, begin, end);

	    InputStream stream = connector.downloadStreamWithRetry(dataRequest);

	    ObservationCollection collection = new ObservationCollection(stream);

	    List<DataRecord> records = collection.getDataRecords();

	    if (records.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).warn("Missing data records");
	    }

	    TimeSeriesResponseType tsrt = createTimeSeriesResponseType(records);

	    return createTimeSeriesResponse(tsrt);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    "SOS_1_0_0_DownloaderDownloadError", //
		    e);
	}

    }

    @Override
    public AbstractSOSConnector getConnector() {
    
        return new eu.essi_lab.accessor.sos._1_0_0.SOSConnector();
    }

    @Override
    public String getSupportedProtocol() {
    
        return NetProtocols.SOS_1_0_0.getCommonURN();
    }

    /**
     * @param procedureHref
     * @return
     * @throws GSException
     */
    private SOSSensorML describeSensor(String procedureHref) throws GSException {
    
        GSLoggerFactory.getLogger(getClass()).debug("Describe sensor {} STARTED", procedureHref);
    
        SOSRequestBuilder requestBuilder = new SOSRequestBuilder(linkage, "1.0.0");
        String sensorDescRequest = requestBuilder.createProcedureDescriptionRequest(//
        	procedureHref, //
        	"text/xml;subtype=\"sensorML/1.0.1\"");
    
        Optional<String> optResponse = new Downloader().downloadOptionalString(sensorDescRequest);
    
        GSLoggerFactory.getLogger(getClass()).debug("Describe sensor {} ENDED", procedureHref);
    
        if (optResponse.isPresent()) {
    
            String describeSensor = optResponse.get();
    
            try {
    
        	XMLDocumentReader reader = new XMLDocumentReader(describeSensor);
    
        	boolean errorOccurred = reader.evaluateBoolean("exists(//*:Exception)");
    
        	if (errorOccurred) {
    
        	    GSLoggerFactory.getLogger(getClass()).warn(describeSensor);
    
        	} else {
    
        	    return new SOSSensorML(reader);
    
        	}
            } catch (Exception e) {
    
        	GSLoggerFactory.getLogger(getClass()).error(e);
    
        	throw GSException.createException(getClass(), "SOS_1_0_0_DownloaderGetRemoteDescriptorsError", e);
            }
        }
    
        throw GSException.createException(getClass(), //
        	"Unable to retrieve describe sensor response: " + sensorDescRequest, //
        	ErrorInfo.ERRORTYPE_SERVICE, //
        	ErrorInfo.SEVERITY_ERROR, //
        	"SOS_1_0_0_DownloaderDescribeSensorError"); //
    }

    /**
     * @param tsrt
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    private File createTimeSeriesResponse(TimeSeriesResponseType tsrt) throws IOException, JAXBException {

	ObjectFactory factory = new ObjectFactory();

	JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);

	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
	tmpFile.deleteOnExit();

	JAXBWML.getInstance().marshal(response, tmpFile);
	return tmpFile;
    }

    /**
     * @param records
     * @throws DatatypeConfigurationException
     * @throws GSException
     */
    private TimeSeriesResponseType createTimeSeriesResponseType(List<DataRecord> records)
	    throws DatatypeConfigurationException, GSException {

	WMLDataDownloaderAdapter adapter = new WMLDataDownloaderAdapter();
	adapter.setOnlineResource(resource, online.getIdentifier());

	TimeSeriesResponseType tsrt = adapter.getTimeSeriesTemplate();

	for (DataRecord dataRecord : records) {

	    String time = dataRecord.getTime();
	    BigDecimal value = dataRecord.getValue();

	    ValueSingleVariable var = new ValueSingleVariable();

	    var.setValue(value);

	    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	    cal.setTime(ISO8601DateTimeUtils.parseISO8601ToDate(time).get());

	    XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

	    var.setDateTimeUTC(xmlCal);
	    adapter.addValue(tsrt, var);
	}

	return tsrt;
    }
}
