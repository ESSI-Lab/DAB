package eu.essi_lab.adk.timeseries;

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

import java.util.Date;

import javax.xml.bind.JAXBElement;

import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.VariableCode;

import eu.essi_lab.model.resource.GSResource;

public class TimeSeriesUtils {

    /**
     * Creates a template from the given resource, to be later filled with data by the caller
     * 
     * @param resource
     * @return
     */
    public static JAXBElement<TimeSeriesResponseType> createTimeSeriesTemplate(GSResource resource) {

	String uniqueSiteCode = getUniqueSiteCode(resource);
	String siteCode = getSiteCode(resource);
	String siteName = getSiteName(resource);

	String uniqueParameterCode = getUniqueParameterCode(resource);
	String parameterCode = getParameterCode(resource);
	String parameterName = getParameterName(resource);
	String parameterDescription = getParameterDescription(resource);

	Double lat = getLatitude(resource);
	Double lon = getLongitude(resource);

	String temporalBegin = getTemporalBegin(resource);
	String temporalEnd = getTemporalEnd(resource);

	TimeSeriesResponseType tsrs = new TimeSeriesResponseType();

	TimeSeriesType ts = new TimeSeriesType();
	VariableInfoType vinfo = new VariableInfoType();

	vinfo.setVariableName(parameterName);
	VariableCode variableCode = new VariableCode();
	variableCode.setDefault(true);
	// vc.setVariableID(7);
	variableCode.setVocabulary("");

	variableCode.setValue(uniqueParameterCode);
	vinfo.getVariableCode().add(variableCode);
	vinfo.setVariableDescription(parameterDescription);
	ts.setVariable(vinfo);

	UnitsType units = new UnitsType();
	// units.setUnitName(v.get().getVariableName());
	// units.setUnitAbbreviation(v.get().getVariableCode());
	// units.setUnitDescription(v.get().getDescription());
	vinfo.setUnit(units);
	TsValuesSingleVariableType values = new TsValuesSingleVariableType();
	// CensorCodeType censorCode = new CensorCodeType();
	// censorCode.setCensorCode("nc");
	// censorCode.setCensorCodeDescription("not censored");
	// values.getCensorCode().add(censorCode);
	SiteInfoType sourceInfo = new SiteInfoType();
	GeoLocation geog = new GeoLocation();
	LatLonPointType location = new LatLonPointType();
	location.setLatitude(lat);
	location.setLongitude(lon);
	location.setSrs("EPSG:4326");

	geog.setGeogLocation(location);
	sourceInfo.setGeoLocation(geog);
	SiteCode siteCodeObject = new SiteCode();
	siteCodeObject.setValue(uniqueSiteCode);
	sourceInfo.getSiteCode().add(siteCodeObject);
	sourceInfo.setSiteName(siteName);
	ts.setSourceInfo(sourceInfo);
	ts.getValues().add(values);
	tsrs.getTimeSeries().add(ts);
	ObjectFactory factory = new ObjectFactory();
	JAXBElement<TimeSeriesResponseType> ret = factory.createTimeSeriesResponse(tsrs);

	return ret;
    }

    // STATION

    private static String getUniqueSiteCode(GSResource resource) {
	try {
	    return resource.getExtensionHandler().getUniquePlatformIdentifier().get();
	} catch (Exception e) {
	    return null;
	}
    }

    private static String getSiteCode(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getMDIdentifierCode();
	} catch (Exception e) {
	}
	return getUniqueSiteCode(resource);
    }

    private static String getSiteName(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle();
	} catch (Exception e) {
	}
	return null;
    }

    // PARAMETER

    private static String getUniqueParameterCode(GSResource resource) {
	try {
	    return resource.getExtensionHandler().getUniqueAttributeIdentifier().get();
	} catch (Exception e) {
	    return null;
	}
    }

    private static String getParameterCode(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeIdentifier();
	} catch (Exception e) {
	    return getUniqueParameterCode(resource);
	}
    }

    private static String getParameterName(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeTitle();
	} catch (Exception e) {
	    return null;
	}
    }

    private static String getParameterDescription(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeDescription();
	} catch (Exception e) {
	    return null;
	}
    }

    // LOCATION

    private static Double getLatitude(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
		    .getNorth();
	} catch (Exception e) {
	    return null;
	}
    }

    private static Double getLongitude(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getGeographicBoundingBox()
		    .getEast();
	} catch (Exception e) {
	    return null;
	}
    }

    // TIME

    private static String getTemporalBegin(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent()
		    .getBeginPosition();
	} catch (Exception e) {
	    return null;
	}
    }

    private static String getTemporalEnd(GSResource resource) {
	try {
	    return resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getTemporalExtent()
		    .getEndPosition();
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Provides an estimate of the data size given the temporal extent
     * 
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long estimateSize(Date beginDate, Date endDate) {
	return estimateSize(beginDate, endDate, 3.0);
    }

    /**
     * Provides an estimate of the data size given the temporal extent and the expected number of values per day
     * 
     * @param beginDate
     * @param endDate
     * @param expectedValuesPerDay
     * @return
     */
    public static long estimateSize(Date beginDate, Date endDate, double expectedValuesPerDay) {
	long extent = endDate.getTime() - beginDate.getTime();
	double extentInDays = ((((extent / 1000.0) / 60.0) / 60.0) / 24.0);

	long expectedSize = (long) (extentInDays * expectedValuesPerDay);
	return expectedSize;
    }

}
