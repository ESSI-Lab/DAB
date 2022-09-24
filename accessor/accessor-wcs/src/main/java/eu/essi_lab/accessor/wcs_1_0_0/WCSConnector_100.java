/**
 * 
 */
package eu.essi_lab.accessor.wcs_1_0_0;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class WCSConnector_100 extends WCSConnector {

    /**
     * 
     */
    public static final String TYPE = "WCS Connector 1.0.0";

    @Override
    public boolean supports(GSSource source) {

	try {

	    XMLDocumentReader capabilities = getCapabilities(source.getEndpoint(), getVersion());
	    String srvName = capabilities.evaluateString("/*:WCS_Capabilities/*:Service/*:name");
	    Boolean isWcs = capabilities.evaluateBoolean("exists(/*:WCS_Capabilities[1])");

	    if (isWcs || srvName.toLowerCase().contains("wcs")) {

		String version = capabilities.evaluateString("/*:WCS_Capabilities/@version");
		if (version == null || (!version.equals(getVersion()) && !version.equals("1.0"))) {
		    return false;
		}

	    }
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    return false;
	}

	return true;
    }

    @Override
    public List<String> getCoverageIdentifiers(XMLDocumentReader capabilities) {

	try {
	    List<String> ret = capabilities
		    .evaluateTextContent("/*:WCS_Capabilities/*:ContentMetadata/*:CoverageOfferingBrief/*:name/text()");
	    // ret = ret.subList(0, 2);
	    return ret;
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected boolean checkCapabilities(XMLDocumentReader capabilities) {

	try {
	    return capabilities.evaluateBoolean("exists(/*:WCS_Capabilities[1])");
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return false;
    }

    @Override
    protected String getVersion() {

	return "1.0.0";
    }

    @Override
    protected String getIdentifierParameter(String id) {

	return "COVERAGE=" + id;
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
