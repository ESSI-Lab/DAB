package eu.essi_lab.accessor.csw;

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

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

public class CSWMCP1Connector extends CSWConnector {

    /**
     * 
     */
    public static final String TYPE = "CSW MCP 1 Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * The CSW MCP always returns MCP Metadata (even if GMD Metadata is asked)
     */

    @Override
    protected String getReturnedMetadataSchema() {
	return CommonNameSpaceContext.MCP_1_NS_URI;
    }

    @Override
    protected String getRequestedMetadataSchema() throws GSException {

	return CommonNameSpaceContext.GMD_NS_URI;
    }

    /**
     * The CSW MCP connector applies only to the CSW AODN catalogue
     */
    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("aodn.org.au")) {
	    boolean cswBaseSupport = super.supports(source);
	    return cswBaseSupport;
	} else {
	    return false;
	}

    }

}
