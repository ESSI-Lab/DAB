/**
 * This file is part of SDI HydroServer Accessor. SDI HydroServer Accessor is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version. SDI HydroServer Accessor is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with SDI HydroServer Accessor. If not, see <http://www.gnu.org/licenses/>. Copyright (C)
 * 2009-2011 ESSI-Lab <info@essi-lab.eu>
 */

package eu.essi_lab.accessor.wof.client.datamodel;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLNodeReader;

public class SiteInfo implements ISiteInfo {

    private XMLNodeReader reader;

    public XMLNodeReader getReader() {
	return reader;
    }

    private Logger logger = GSLoggerFactory.getLogger(SiteInfo.class);

    public SiteInfo(Node node) {
	this.reader = new XMLNodeReader(node);
    }

    public String getSiteName() {
	try {
	    return reader.evaluateString("*:siteName");
	} catch (XPathExpressionException e) {

	    logger.warn("Site name not found", e);
	    return null;
	}
    }

    public String getSiteCode() {
	try {
	    return reader.evaluateString("*:siteCode");
	} catch (XPathExpressionException e) {
	    logger.warn("Site code not found", e);
	    return null;
	}
    }

    public String getSiteCodeNetwork() {
	try {
	    return reader.evaluateString("*:siteCode/@network");
	} catch (XPathExpressionException e) {
	    logger.warn("Site network not found", e);
	    return null;
	}
    }

    public String getSiteId() {
	try {
	    return reader.evaluateString("*:siteCode/@siteID");
	} catch (XPathExpressionException e) {
	    logger.warn("Site id not found", e);
	    return null;
	}
    }

    public String getElevationMetres() {
	try {
	    return reader.evaluateString("*:elevation_m");
	} catch (XPathExpressionException e) {
	    logger.warn("Elevation not found", e);
	    return null;
	}
    }

    public String getVerticalDatum() {
	try {
	    return reader.evaluateString("*:verticalDatum");
	} catch (XPathExpressionException e) {
	    logger.warn("Vertical not found", e);
	    return null;
	}
    }

    // Site Properties

    public enum SiteProperty {

	COUNTY("County"),
	//
	COUNTRY("Country"),
	//
	STATE("State"),
	//
	SITE_COMMENTS("Site Comments"),
	//
	POS_ACCURACY_M("PosAccuracy_m"),
	//
	SITE_TYPE("Site Type"),
	//
	;

	private String propertyName;

	public String getPropertyName() {
	    return propertyName;
	}

	SiteProperty(String propertyName) {
	    this.propertyName = propertyName;
	}

	/**
	 * Returns a known site property or null if the property is not known
	 *
	 * @param propertyName
	 * @return
	 */
	public static SiteProperty decode(String propertyName) {
	    for (SiteProperty sp : SiteProperty.values()) {
		if (sp.getPropertyName().equals(propertyName)) {
		    return sp;
		}
	    }
	    return null;
	}

    }

    public String getCounty() {
	return getProperty(SiteProperty.COUNTY);
    }

    public String getState() {
	return getProperty(SiteProperty.STATE);
    }

    public String getSiteComments() {
	return getProperty(SiteProperty.SITE_COMMENTS);
    }

    /**
     * Gets the property with the given name
     *
     * @param property
     * @return
     * @throws XPathExpressionException
     */
    public String getProperty(String property) {
	try {
	    return reader.evaluateString("*:siteProperty[lower-case(@name)='" + property.toLowerCase() + "' or lower-case(@title)='" + property.toLowerCase() + "']");
	} catch (XPathExpressionException e) {
	    logger.warn("siteProperty {} not found", property, e);
	}
	return "";
    }

    /**
     * Gets the property with the given name
     *
     * @param property
     * @return
     * @throws XPathExpressionException
     */
    public String getProperty(SiteProperty property) {

	return getProperty(property.getPropertyName());

    }

    public String getLatitude() {
	try {
	    return reader.evaluateString("*:geoLocation/*:geogLocation/*:latitude");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getLongitude() {
	try {
	    return reader.evaluateString("*:geoLocation/*:geogLocation/*:longitude");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    public String getSRS() {
	try {
	    return reader.evaluateString("*:geoLocation/*:geogLocation/@srs");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    @Override
    public String getLocalSiteXYProjectionInformation() {
	try {
	    return reader.evaluateString("*:geoLocation/*:localSiteXY/@projectionInformation");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    @Override
    public String getLocalSiteX() {
	try {
	    return reader.evaluateString("*:geoLocation/*:localSiteXY/*:X");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    @Override
    public String getLocalSiteY() {
	try {
	    return reader.evaluateString("*:geoLocation/*:localSiteXY/*:Y");
	} catch (XPathExpressionException e) {
	    return "";
	}
    }

    /*
     * SERIES
     */
    public TimeSeries getSeries(String variableCode, String methodId, String qualityControlLevelCode, String sourceId) {
	try {
	    String constraints = "[*:variable/*:variableCode='" + variableCode + "']";
	    if (methodId != null && !methodId.isEmpty()) {
		constraints += "[*:method/@methodID='" + methodId + "']";
	    }
	    if (qualityControlLevelCode != null && !qualityControlLevelCode.isEmpty()) {
		constraints += "[*:qualityControlLevel/@qualityControlLevelID='" + qualityControlLevelCode + "']";
	    }
	    if (sourceId != null && !sourceId.isEmpty()) {
		constraints += "[*:source/@sourceID='" + sourceId + "']";
	    }
	    Node[] result = reader.evaluateNodes("//*:series" + constraints);
	    if (result.length == 0) {
		return null;
	    } else {
		return new TimeSeries(result[0]);
	    }
	} catch (XPathExpressionException e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    
	    return null;
	}
    }

    public List<TimeSeries> getSeries() {
	List<TimeSeries> ret = new ArrayList<>();
	try {
	    Node[] result = reader.evaluateNodes("//*:series");
	    for (Node node : result) {
		ret.add(new TimeSeries(node));
	    }
	} catch (XPathExpressionException e) {

	    logger.warn("Can't read series", e);

	}
	return ret;
    }

    @Override
    public String toString() {
	return getSiteCodeNetwork() + ":" + getSiteCode() + " (" + getSiteName() + ")";

    }

    /**
     * This method does nothing more than checking if the properties reported by this site are known properties.
     * In case a property is unknown than a log is generated.
     */
    public void checkProperties() {

	try {
	    Node[] propertyNodes = reader.evaluateNodes("*:siteProperty/@name");
	    for (Node propertyNode : propertyNodes) {
		String propertyName = propertyNode.getTextContent();
		SiteProperty decoded = SiteProperty.decode(propertyName);
		if (decoded == null) {
		    logger.error("Found new property not considered in the mapping: ", propertyName);
		}
	    }
	} catch (Exception e) {
	    logger.error("Error checking properties", e);
	}

    }

}
