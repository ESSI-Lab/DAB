package eu.essi_lab.workflow.processor.timeseries;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo.DefaultTimeZone;
import org.cuahsi.waterml._1.SourceInfoType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;

public class WML11_Subset_Processor extends AbstractTimeSubsetProcessor {

    public File subset(File inFile, Date timeBegin, LimitType beginType, Date timeEnd, LimitType endType) throws Exception {

	TimeSeriesResponseType trt;

	trt = JAXBWML.getInstance().parseTimeSeries(inFile);

	List<TimeSeriesType> series = trt.getTimeSeries();

	for (TimeSeriesType serie : series) {

	    TimeZone timeZone = TimeZone.getTimeZone("GMT");
	    SourceInfoType sourceInfo = serie.getSourceInfo();
	    if (sourceInfo != null) {
		if (sourceInfo instanceof SiteInfoType) {
		    SiteInfoType siteInfo = (SiteInfoType) sourceInfo;
		    TimeZoneInfo timeZoneInfo = siteInfo.getTimeZoneInfo();
		    if (timeZoneInfo != null) {
			DefaultTimeZone defaultTimeZone = timeZoneInfo.getDefaultTimeZone();
			if (defaultTimeZone != null) {
			    String timeZoneCode = defaultTimeZone.getZoneAbbreviation();
			    if (timeZoneCode != null) {
				timeZone = TimeZone.getTimeZone(timeZoneCode);
			    }
			}
		    }

		}
	    }

	    List<TsValuesSingleVariableType> values = serie.getValues();
	    for (TsValuesSingleVariableType value : values) {
		List<ValueSingleVariable> innerValues = value.getValue();
		Iterator<ValueSingleVariable> iterator = innerValues.iterator();
		TreeMap<Date, ValueSingleVariable> map = new TreeMap<>();
		while (iterator.hasNext()) {
		    ValueSingleVariable innerValue = (ValueSingleVariable) iterator.next();
		    XMLGregorianCalendar utcTime = innerValue.getDateTimeUTC();
		    XMLGregorianCalendar defaultTime = innerValue.getDateTime();
		    Date date = null;
		    if (utcTime != null) {
			date = utcTime.toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null).getTime();
		    } else if(defaultTime != null){
			date = defaultTime.toGregorianCalendar(timeZone, null, null).getTime();
		    }
		    if (date != null) {
			map.put(date, innerValue);
		    }
		}
		switch (beginType) {
		case MINIMUM:
		    timeBegin = map.firstKey();
		    break;
		case MAXIMUM:
		    timeBegin = map.lastKey();
		    break;
		case ABSOLUTE:
		    break;
		default:
		    break;
		}
		switch (endType) {
		case MINIMUM:
		    timeEnd = map.firstKey();
		    break;
		case MAXIMUM:
		    timeEnd = map.lastKey();
		    break;
		case ABSOLUTE:
		    break;
		default:
		    break;
		}

		innerValues.clear();
		for (Date date : map.keySet()) {
		    if (!(date.before(timeBegin) || date.after(timeEnd))) {
			innerValues.add(map.get(date));
		    }
		}

	    }

	}

	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	JAXBWML.getInstance().marshal(trt, tmpFile);
	return tmpFile;

    }

}
