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
public class CSWCEDACCIConnector extends CSWConnector {

    /**
     * 
     */
    private static final long serialVersionUID = -694562388968171405L;

    @Override
    public String getLabel() {

	return "CSW CEDA-CCI Connector";
    }

    CSWHttpGetRecordsRequestCreator getCreator(GetRecords getRecords) throws GSException {

	return new CSWHttpGetRecordsRequestCreator(getGetRecordsBinding(), this, getRecords) {

	    public String getGetRecordsUrl() {

		//
		// instead of https://csw.ceda.ac.uk:8080/geonetwork/srv/eng/csw-CEDA-CCI
		//
		return "https://csw.ceda.ac.uk/geonetwork/srv/eng/csw-CEDA-CCI";
	    }
	};
    }

    @Override
    public boolean supports(Source source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("csw.ceda.ac.uk")) {
	    return super.supports(source);
	} else {
	    return false;
	}
    }

}
