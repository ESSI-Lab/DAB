/**
 * This file is part of SDI HydroServer Accessor. SDI HydroServer Accessor is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version. SDI HydroServer Accessor is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with SDI HydroServer Accessor. If not, see <http:www.gnu.org/licenses/>. Copyright (C)
 * 2009-2011 ESSI-Lab <info@essi-lab.eu>
 */

package eu.essi_lab.accessor.wof.client.datamodel;

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

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLNodeReader;

public class ServiceInfo  {

    private XMLNodeReader reader;

    private Logger logger = GSLoggerFactory.getLogger(ServiceInfo.class);

    public ServiceInfo(XMLNodeReader reader) {
	this.reader = reader;
    }
    
    public String getServiceURL() {
	try {
	    return reader.evaluateString("*:servURL");
	} catch (XPathExpressionException e) {

	    logger.warn("Service URL not found", e);
	    return null;
	}
    }

    public String getTitle() {
	try {
	    return reader.evaluateString("*:Title").trim();
	} catch (XPathExpressionException e) {

	    logger.warn("Title not found", e);
	    return null;
	}
    }

    public String getServiceDescriptionURL() {
	try {
	    return reader.evaluateString("*:ServiceDescriptionURL");
	} catch (XPathExpressionException e) {

	    logger.warn("Service description URL not found", e);
	    return null;
	}
    }
    

    public String getName() {
	try {
	    return reader.evaluateString("*:name");
	} catch (XPathExpressionException e) {

	    logger.warn("Email not found", e);
	    return null;
	}
    }


    public String getEmail() {
	try {
	    return reader.evaluateString("*:Email");
	} catch (XPathExpressionException e) {

	    logger.warn("Email not found", e);
	    return null;
	}
    }

    public String getPhone() {
	try {
	    return reader.evaluateString("*:phone");
	} catch (XPathExpressionException e) {

	    logger.warn("Phone not found", e);
	    return null;
	}
    }

    public String getOrganization() {
	try {
	    return reader.evaluateString("*:organization");
	} catch (XPathExpressionException e) {

	    logger.warn("Organization not found", e);
	    return null;
	}
    }

    public String getOrganizationWebsite() {
	try {
	    return reader.evaluateString("*:orgwebsite");
	} catch (XPathExpressionException e) {

	    logger.warn("Organization web site not found", e);
	    return null;
	}
    }

    public String getCitation() {
	try {
	    return reader.evaluateString("*:citation");
	} catch (XPathExpressionException e) {

	    logger.warn("Citation not found", e);
	    return null;
	}
    }

    public String getAbstract() {
	try {
	    return reader.evaluateString("*:aabstract");
	} catch (XPathExpressionException e) {

	    logger.warn("Abstract not found", e);
	    return null;
	}
    }

    public String getValueCount() {
	try {
	    return reader.evaluateString("*:valuecount");
	} catch (XPathExpressionException e) {

	    logger.warn("Value count not found", e);
	    return null;
	}
    }

    public String getVariableCount() {
	try {
	    return reader.evaluateString("*:variablecount");
	} catch (XPathExpressionException e) {

	    logger.warn("Variable count not found", e);
	    return null;
	}
    }

    public String getSiteCount() {
	try {
	    return reader.evaluateString("*:sitecount");
	} catch (XPathExpressionException e) {

	    logger.warn("Site count not found", e);
	    return null;
	}
    }

    public String getServiceID() {
	try {
	    return reader.evaluateString("*:ServiceID").trim();
	} catch (XPathExpressionException e) {

	    logger.warn("Service ID not found", e);
	    return null;
	}
    }

    public String getNetworkName() {
	try {
	    return reader.evaluateString("*:NetworkName");
	} catch (XPathExpressionException e) {

	    logger.warn("Network name not found", e);
	    return null;
	}
    }

    public String getMinx() {
	try {
	    return reader.evaluateString("*:minx");
	} catch (XPathExpressionException e) {

	    logger.warn("Min X not found", e);
	    return null;
	}
    }

    public String getMaxx() {
	try {
	    return reader.evaluateString("*:maxx");
	} catch (XPathExpressionException e) {

	    logger.warn("Max X not found", e);
	    return null;
	}
    }

    public String getMiny() {
	try {
	    return reader.evaluateString("*:miny");
	} catch (XPathExpressionException e) {

	    logger.warn("Min Y not found", e);
	    return null;
	}
    }

    public String getMaxy() {
	try {
	    return reader.evaluateString("*:maxy");
	} catch (XPathExpressionException e) {

	    logger.warn("Max Y not found", e);
	    return null;
	}
    }

    public String getServiceStatus() {
	try {
	    return reader.evaluateString("*:serviceStatus");
	} catch (XPathExpressionException e) {

	    logger.warn("Service status not found", e);
	    return null;
	}
    }

}
