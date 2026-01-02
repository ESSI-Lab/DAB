package eu.essi_lab.accessor.whos.sigedac;

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

import java.io.File;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author boldrini
 */
public class SIGEDACDownloader extends WMLDataDownloader {

    private static final String SIGEDAC_DOWNLOADER_GET_REMOTE_DESCRIPTOR_ERROR = "SIGEDAC_DOWNLOADER_GET_REMOTE_DESCRIPTOR_ERROR";
    private static final String SIGEDAC_DOWNLOADER_DOWNLOAD_ERROR = "SIGEDAC_DOWNLOADER_DOWNLOAD_ERROR";

    @Override
    public boolean canConnect() {
	return online.getLinkage().contains(CommonNameSpaceContext.SIGEDAC_PARAGUAY_URI);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.SIGEDAC_PARAGUAY_URI));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    SIGEDACIdentifierMangler mangler = new SIGEDACIdentifierMangler();
	    mangler.setMangling(name);
	    // String stationCode = mangler.getPlatformIdentifier();
	    // String parameterCode = mangler.getParameterIdentifier();
	    // AutomaticSystemClient client = new AutomaticSystemClient(online.getLinkage());
	    // client.setToken(ConfigurationWrapper.getCredentialsSetting().getDMHToken().orElse(null));

	    // DMHStation station = client.getStation(stationCode);

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    //
	    //
	    //

	    // DMHVariable variable = station.getVariable(parameterCode);
	    //
	    // if (variable == null) {
	    //
	    // GSLoggerFactory.getLogger(getClass()).warn("Unable to find variable for parameter code: " +
	    // parameterCode);
	    // return ret;
	    // }
	    //
	    // Date begin = variable.getObservationsStart();
	    // Date end = variable.getObservationsEnd();

	    TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	    Date begin = null;
	    Date end = null;
	    if (extent != null) {

		String startDate = extent.getBeginPosition();
		String endDate = extent.getEndPosition();
		if (extent.isEndPositionIndeterminate()) {
		    endDate = ISO8601DateTimeUtils.getISO8601DateTime();
		}
		begin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate).get();
		end = ISO8601DateTimeUtils.parseISO8601ToDate(endDate).get();

	    }

	    descriptor.setTemporalDimension(begin, end);

	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneHourInMilliseconds = 1000 * 60 * 60l;
	    Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;
	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);

	    ret.add(descriptor);
	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SIGEDAC_DOWNLOADER_GET_REMOTE_DESCRIPTOR_ERROR, //
		    e);
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    SIGEDACIdentifierMangler mangler = new SIGEDACIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();
	    SIGEDACClient client = new SIGEDACClient(online.getLinkage());
	    // client.setToken(ConfigurationWrapper.getCredentialsSetting().getDMHToken().orElse(null));

	    Date begin = null;
	    Date end = null;
	    DataDimension dimension = descriptor.getTemporalDimension();
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
	    }

	    // SIGEDACData observations = client..getObservations(stationCode, parameterCode, begin, end);

	    Optional<String> resolution = resource.getExtensionHandler().getTimeUnits();
	    String frequency = null;
	    if (resolution.isPresent()) {
		frequency = resolution.get();
	    }

	    List<SimpleEntry<Date, BigDecimal>> data = null;

	    if (frequency == null) {

		data = client.getObservations(stationCode, parameterCode, begin, end, null);
		// client.getRiverLevel(stationCode, begin, end, null);
	    } else {
		// client.getHourlyData(stationCode, parameterCode, begin, end, null);
		data = client.getObservations(stationCode, parameterCode, begin, end, frequency);
	    }

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    if (data != null) {
		// List<SimpleEntry<Date, BigDecimal>> observations = data.getData();

		for (int i = 0; i < data.size(); i++) {

		    ValueSingleVariable v = new ValueSingleVariable();

		    SimpleEntry<Date, BigDecimal> value = data.get(i);

		    Date parsed = value.getKey();
		    BigDecimal dValue = value.getValue();

		    v.setValue(dValue);
		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(parsed);
		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date2);
		    addValue(tsrt, v);
		}
	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SIGEDAC_DOWNLOADER_DOWNLOAD_ERROR, //
		    e);
	}

    }

}
