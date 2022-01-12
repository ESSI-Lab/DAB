package eu.essi_lab.accessor.wcs.connector;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.Source;

public class WCSConnector_201 extends WCSConnector {

    @Override
    public boolean supports(Source source) {

	try {

	    XMLDocumentReader capabilities = getCapabilities(source.getEndpoint(), getVersion());
	    String srvType = capabilities.evaluateString("//*:ServiceType");
	    if (!srvType.toLowerCase().contains("wcs")) {
		return false;
	    }

	    String version = capabilities.evaluateString("//*:Capabilities/@version");
	    if (version != null && !version.equals("")) {
		if (version.equals(getVersion()) || !version.equals("2.0.1")) {
		    return true;
		}
	    }

	    List<String> list = capabilities.evaluateTextContent("//*:ServiceTypeVersion/text()");
	    if (!list.contains(getVersion())) {
		return false;
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    return false;
	}

	return true;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -1531847775576882224L;

    @Override
    public List<String> getCoverageIdentifiers(XMLDocumentReader capabilities) {

	try {
	    return capabilities.evaluateTextContent("//*:Contents/*:CoverageSummary/*:CoverageId/text()");
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected boolean checkCapabilities(XMLDocumentReader capabilities) {

	try {
	    return capabilities.evaluateBoolean("exists(//*:Capabilities)");
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return false;
    }

    @Override
    protected String getVersion() {

	return "2.0.1";
    }

    @Override
    protected String getIdentifierParameter(String id) {

	return "COVERAGEID=" + id;
    }
}
