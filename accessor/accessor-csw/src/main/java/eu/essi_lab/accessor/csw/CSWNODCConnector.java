package eu.essi_lab.accessor.csw;

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

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.exceptions.GSException;
public class CSWNODCConnector extends CSWGetConnector {

    /**
     * 
     */
    private static final long serialVersionUID = -1439425775754595788L;

    public CSWNODCConnector() {
	// a #100 page results in error code 500
	getPageSizeOption().setValue(20);
    }

    @Override
    public String getLabel() {

	return "CSW NODC Connector";
    }

    /**
     * The CSW NODC always returns GMI Metadata according to the NODC profile (even if GMD Metadata is asked)
     */
    @JsonIgnore
    @Override
    protected String getReturnedMetadataSchema() {

	return CommonNameSpaceContext.NODC_NS_URI;
    }

    @JsonIgnore
    @Override
    protected String getRequestedMetadataSchema() throws GSException {

	return CommonNameSpaceContext.GMD_NS_URI;
    }

    @Override
    protected String getConstraintLanguageParameter() {
	return "";
    }

    /**
     * The CSW NODC connector applies only to the NODC catalogue
     */
    @Override
    public boolean supports(Source source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("nodc")) {
	    return super.supports(source);
	} else {
	    return false;
	}

    }

}
