package eu.essi_lab.accessor.chslf;

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
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author boldrini
 */
public class SLFDownloader extends WMLDataDownloader {

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	SimpleEntry<Date, Date> extent = SLFClient.getDefaultExtent();
	descriptor.setTemporalDimension(extent.getKey(), extent.getValue());

	ret.add(descriptor);
	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String timeseriesId = online.getName();

	SimpleEntry<String, SLFParameter> parsed = SLFClient.parseTimeseriesId(timeseriesId);
	if (parsed == null) {
	    GSLoggerFactory.getLogger(getClass()).error("Invalid SLF timeseries identifier: {}", timeseriesId);
	    return null;
	}

	String stationCode = parsed.getKey();
	SLFParameter parameter = parsed.getValue();

	SimpleEntry<Date, Date> extent = SLFClient.getDefaultExtent();
	Date begin = extent.getKey();
	Date end = extent.getValue();

	DataDimension temporalDimension = descriptor.getTemporalDimension();
	if (temporalDimension != null && temporalDimension.getContinueDimension() != null) {
	    ContinueDimension dimension = temporalDimension.getContinueDimension();
	    if (dimension.getLower() != null) {
		begin = new Date(dimension.getLower().longValue());
	    }
	    if (dimension.getUpper() != null) {
		end = new Date(dimension.getUpper().longValue());
	    }
	}

	SLFClient client = new SLFClient(online.getLinkage());
	List<JSONObject> measurements = client.retrieveMeasurements(parameter.getNetwork(), stationCode, SLFClient.getPeriodInDays(begin));

	try {
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    for (JSONObject measurement : measurements) {

		if (!measurement.has(parameter.getCode()) || measurement.isNull(parameter.getCode())) {
		    continue;
		}

		Optional<Date> date = SLFClient.parseDate(measurement.optString(SLFClient.MEASUREMENT_DATE, null));
		if (date.isEmpty() || date.get().before(begin) || date.get().after(end)) {
		    continue;
		}

		ValueSingleVariable v = new ValueSingleVariable();
		v.setValue(new BigDecimal(measurement.get(parameter.getCode()).toString()));

		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		c.setTime(date.get());
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		v.setDateTimeUTC(date2);
		addValue(tsrt, v);
	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.SLF.getCommonURN()));
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
	source.setEndpoint(online.getLinkage());
	return new SLFConnector().supports(source);
    }
}
