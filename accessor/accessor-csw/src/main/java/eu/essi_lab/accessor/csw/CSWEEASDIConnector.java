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

import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class CSWEEASDIConnector extends CSWConnector {

    /**
     * 
     */
    private static final long serialVersionUID = -1421212555178249262L;

    @Override
    public String getLabel() {

	return "CSW EEA SDI Connector";
    }

    CSWHttpGetRecordsRequestCreator getCreator(GetRecords getRecords) throws GSException {

	return new CSWHttpGetRecordsRequestCreator(getGetRecordsBinding(), this, getRecords) {

	    public String getGetRecordsUrl() {

		//
		// instead of http://sdi.eea.europa.eu/catalogue/srv/eng/csw-geoss
		//
		return "https://sdi.eea.europa.eu/catalogue/geoss/eng/csw";
	    }
	};
    }

    @Override
    public boolean supports(Source source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("sdi.eea.europa.eu")) {
	    return super.supports(source);
	} else {
	    return false;
	}
    }
}
