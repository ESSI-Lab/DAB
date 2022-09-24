package eu.essi_lab.validator.wof;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.CoordType;
import org.cuahsi.waterml._1.GeogLocationType;
import org.cuahsi.waterml._1.LatLonBoxType;
import org.cuahsi.waterml._1.LatLonLineString;
import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo.DefaultTimeZone;
import org.cuahsi.waterml._1.SourceInfoType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;

public class WML_1_1Validator extends DataValidatorImpl {

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    @Override
    public DataFormat getFormat() {
	return DataFormat.WATERML_1_1();
    }

    @Override
    public DataType getType() {
	return DataType.TIME_SERIES;
    }

    @Override
    public ValidationMessage checkSupportForDescriptor(DataDescriptor descriptor) {
	ValidationMessage ret = super.checkSupportForDescriptor(descriptor);
	if (ret.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
	    return ret;
	}

	if (!descriptor.getDataType().equals(DataType.TIME_SERIES)) {
	    return unsupportedDescriptor("Not a time series descriptor - unable to validate");
	}
	if (descriptor.getCRS() != null && !descriptor.getCRS().equals(CRS.EPSG_4326())) {
	    return unsupportedDescriptor("Not a EPSG:4326 descriptor - unable to validate");
	}

	ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    @Override
    public DataDescriptor readDataAttributes(DataObject dataObject) {

	DataDescriptor ret = new DataDescriptor();

	TimeSeriesResponseType trt = null;
	try {
	    FileInputStream stream = new FileInputStream(dataObject.getFile());
	    trt = JAXBWML.getInstance().parseTimeSeries(stream);
	    stream.close();
	} catch (Exception e) {	    
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}
	if (trt==null) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}

	ret.setDataFormat(DataFormat.WATERML_1_1());
	ret.setDataType(DataType.TIME_SERIES);

	List<TimeSeriesType> series = trt.getTimeSeries();

	Double n = null;
	Double e = null;
	Double s = null;
	Double w = null;

	Long timeBegin = null;
	Long timeEnd = null;
	Long timeResolution = null;
	Long timeResolutionTolerance = null;
	Long previousTime = null;

	Double verticalBegin = null;
	Double verticalEnd = null;

	HashSet<Long> times = new HashSet<>();

	String zoneAbbreviation = null;

	for (TimeSeriesType serie : series) {

	    SourceInfoType sourceInfo = serie.getSourceInfo();
	    if (sourceInfo != null) {
		if (sourceInfo instanceof SiteInfoType) {

		    SiteInfoType siteInfo = (SiteInfoType) sourceInfo;
		    GeoLocation geoLocation1 = siteInfo.getGeoLocation();
		    if (geoLocation1 != null) {
			GeogLocationType geoLocation = geoLocation1.getGeogLocation();
			if (geoLocation != null) {
			    if (geoLocation instanceof LatLonPointType) {
				LatLonPointType point = (LatLonPointType) geoLocation;
				n = point.getLatitude();
				s = point.getLatitude();
				e = point.getLongitude();
				w = point.getLongitude();
				if (n == null || n < point.getLatitude()) {
				    n = point.getLatitude();
				}
				if (s == null || s > point.getLatitude()) {
				    s = point.getLatitude();
				}
				if (e == null || e < point.getLongitude()) {
				    e = point.getLongitude();
				}
				if (w == null || w > point.getLongitude()) {
				    w = point.getLongitude();
				}

			    } else if (geoLocation instanceof LatLonBoxType) {
				LatLonBoxType box = (LatLonBoxType) geoLocation;
				if (n == null || n < box.getNorth()) {
				    n = box.getNorth();
				}
				if (s == null || s > box.getSouth()) {
				    s = box.getSouth();
				}
				if (e == null || e < box.getEast()) {
				    e = box.getEast();
				}
				if (w == null || w > box.getWest()) {
				    w = box.getWest();
				}

			    } else if (geoLocation instanceof LatLonLineString) {
				LatLonLineString line = (LatLonLineString) geoLocation;
				List<CoordType> coords = line.getCoordLatLong();
				for (CoordType coord : coords) {
				    double tmpLat = coord.getLatitude();
				    double tmpLon = coord.getLongitude();
				    if (n == null || n < tmpLat) {
					n = tmpLat;
				    }
				    if (s == null || s > tmpLat) {
					s = tmpLat;
				    }
				    if (e == null || e < tmpLon) {
					e = tmpLon;
				    }
				    if (w == null || w > tmpLon) {
					w = tmpLon;
				    }
				}
			    } else {
				// unexpected geometry
				throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
			    }
			}
		    }
		    Double tmpElevation = siteInfo.getElevationM();
		    if (tmpElevation != null) {
			if (verticalBegin == null || verticalBegin > tmpElevation) {
			    verticalBegin = tmpElevation;
			}
			if (verticalEnd == null || verticalEnd < tmpElevation) {
			    verticalEnd = tmpElevation;
			}
		    }

		    // TIME ZONE
		    TimeZoneInfo timeZoneInfo = siteInfo.getTimeZoneInfo();
		    if (timeZoneInfo != null) {
			DefaultTimeZone defaultTimezone = timeZoneInfo.getDefaultTimeZone();
			if (defaultTimezone != null) {
			    zoneAbbreviation = defaultTimezone.getZoneAbbreviation();
			}
		    }
		}
	    }

	    List<Long> tmpResolutions = new ArrayList<>();

	    if (timeBegin == null && timeEnd == null) {
		List<TsValuesSingleVariableType> values = serie.getValues();
		for (TsValuesSingleVariableType value : values) {
		    List<ValueSingleVariable> innerValues = value.getValue();
		    for (ValueSingleVariable innerValue : innerValues) {

			XMLGregorianCalendar time = innerValue.getDateTimeUTC();
			String timeZone = "GMT";
			if (time == null) {
			    time = innerValue.getDateTime();
			    if (zoneAbbreviation != null && !zoneAbbreviation.equals("")) {
				timeZone = zoneAbbreviation;
			    }
			}

			if (time != null) {

			    Long milliseconds = time.toGregorianCalendar(TimeZone.getTimeZone(timeZone), null, null).getTime().getTime();

			    if (milliseconds != null) {
				times.add(milliseconds);
				if (previousTime != null) {
				    long tmpResolution = Math.abs(milliseconds - previousTime);
				    tmpResolutions.add(tmpResolution);
				}
				previousTime = milliseconds;
				if (timeBegin == null || timeBegin > milliseconds) {
				    timeBegin = milliseconds;
				}
				if (timeEnd == null || timeEnd < milliseconds) {
				    timeEnd = milliseconds;
				}
			    }
			} else {

			    GSLoggerFactory.getLogger(getClass())
				    .warn("Unable to find time from DataDescriptor: " + dataObject.getDataDescriptor());
			}
		    }
		}
	    }

	    if (!tmpResolutions.isEmpty()) {
		Long min = tmpResolutions.get(0);
		Long max = tmpResolutions.get(0);
		for (Long l : tmpResolutions) {
		    if (l < min) {
			min = l;
		    }
		    if (l > max) {
			max = l;
		    }
		}
		timeResolutionTolerance = (max - min) / 2;
		if (times.size() > 1 && timeEnd != null && timeBegin != null) {
		    timeResolution = (timeEnd - timeBegin) / (times.size() - 1);
		} else {
		    timeResolution = min + timeResolutionTolerance;
		}

	    }

	}

	ret.setCRS(CRS.EPSG_4326());
	if (n != null && e != null && s != null && w != null) {
	    ret.setEPSG4326SpatialDimensions(n, e, s, w);
	    if (Math.abs(n - s) < TOL) {
		ret.getFirstSpatialDimension().getContinueDimension().setSize(1l);
	    }
	    if (Math.abs(w - e) < TOL) {
		ret.getSecondSpatialDimension().getContinueDimension().setSize(1l);
	    }
	}
	if (timeBegin != null && timeEnd != null) {
	    ret.setTemporalDimension(new Date(timeBegin), new Date(timeEnd));
	    ret.getTemporalDimension().getContinueDimension().setResolution(timeResolution);
	    ret.getTemporalDimension().getContinueDimension().setResolutionTolerance(timeResolutionTolerance);
	    ret.getTemporalDimension().getContinueDimension().setSize((long) times.size());
	}
	if (verticalBegin != null && verticalEnd != null) {
	    ret.setVerticalDimension(verticalBegin, verticalEnd);
	    if (Math.abs(verticalEnd - verticalBegin) < TOL) {
		ret.getOtherDimension(DimensionType.VERTICAL).getContinueDimension().setSize(1l);
	    }
	}
	return ret;

    }

}
