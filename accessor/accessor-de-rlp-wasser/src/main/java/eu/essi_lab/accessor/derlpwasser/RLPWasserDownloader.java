package eu.essi_lab.accessor.derlpwasser;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * Downloads the measurements of the rolling window (last 5 days) currently published by the flood portal of Rhineland-Palatinate.
 * The temporal extent of the remote descriptor is refreshed with the first and the last value currently published (a single
 * request)
 *
 * @author boldrini
 */
public class RLPWasserDownloader extends WMLDataDownloader {

    private String linkage;

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	SimpleEntry<Date, Date> extent = getExtent();
	if (extent != null) {
	    descriptor.setTemporalDimension(extent.getKey(), extent.getValue());
	}

	ret.add(descriptor);
	return ret;
    }

    /**
     * @return the extent of the measurements currently published (falling back to the harvested extent, limited to the rolling
     *         window)
     */
    private SimpleEntry<Date, Date> getExtent() {

	RLPWasserEntity series = getSeries();
	if (series != null) {
	    try {
		Optional<SimpleEntry<Date, Date>> extent = new RLPWasserClient(online.getLinkage()).retrieveExtent(series);
		if (extent.isPresent()) {
		    return extent.get();
		}
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to retrieve current measurements of {}", series.getSeriesId());
	    }
	}

	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	if (extent == null) {
	    return null;
	}

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(extent.getBeginPosition()).orElse(null);
	Date end = extent.isEndPositionIndeterminate() ? new Date()
		: ISO8601DateTimeUtils.parseISO8601ToDate(extent.getEndPosition()).orElse(null);

	if (begin == null || end == null) {
	    return null;
	}

	// the older values are no longer published
	Date windowBegin = new Date(end.getTime() - RLPWasserClient.ROLLING_WINDOW);
	if (begin.before(windowBegin)) {
	    begin = windowBegin;
	}

	return new SimpleEntry<>(begin, end);
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	RLPWasserEntity series = getSeries();
	if (series == null) {
	    GSLoggerFactory.getLogger(getClass()).warn("Invalid series identifier {}", online.getName());
	    return null;
	}

	Date begin = null;
	Date end = null;

	DataDimension temporalDimension = descriptor.getTemporalDimension();
	if (temporalDimension != null) {
	    try {
		ContinueDimension dimension = temporalDimension.getContinueDimension();
		begin = new Date(dimension.getLower().longValue());
		end = new Date(dimension.getUpper().longValue());
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	RLPWasserClient client = new RLPWasserClient(online.getLinkage());

	// the whole rolling window is returned by a single request, then filtered
	TreeMap<Date, BigDecimal> values = (begin == null || end == null) ? client.retrieveObservations(series)
		: client.retrieveObservations(series, begin, end);

	try {

	    if (!values.isEmpty()) {

		TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

		for (Entry<Date, BigDecimal> entry : values.entrySet()) {

		    ValueSingleVariable v = new ValueSingleVariable();
		    v.setValue(entry.getValue());

		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(entry.getKey());
		    XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date);
		    addValue(tsrt, v);
		}

		return tsrt.getDataFile();
	    }

	    GSLoggerFactory.getLogger(getClass()).warn("No results found between [{}/{}]", begin, end);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    /**
     * @return
     */
    private RLPWasserEntity getSeries() {

	return Optional.ofNullable(online.getName()).map(RLPWasserClient::parseSeriesId).orElse(null);
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {

	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.RLP_WASSER.getCommonURN())
		&& getSeries() != null);
    }

    @Override
    public boolean canSubset(String dimensionName) {

	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public boolean canConnect() throws GSException {

	GSSource source = new GSSource();
	source.setEndpoint(linkage);
	return new RLPWasserConnector().supports(source);
    }
}
