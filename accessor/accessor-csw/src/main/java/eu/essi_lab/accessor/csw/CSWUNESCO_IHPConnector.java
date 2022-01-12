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

import java.util.Arrays;

import eu.essi_lab.model.Source;
import eu.essi_lab.model.configuration.option.GSConfOptionInteger;

/**
 * @author Fabrizio
 *
 */
public class CSWUNESCO_IHPConnector  extends CSWGetConnector {

    public CSWUNESCO_IHPConnector() {
 	
	GSConfOptionInteger option = (GSConfOptionInteger) getSupportedOptions().get(CSW_CONNECTOR_PAGESIZE_OPTION_KEY);
	option.setValue(20);
	option.setAllowedValues(Arrays.asList(10));
    }

    @Override
    public boolean supports(Source source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("ihp-wins.unesco.org")) {
	    return super.supports(source);
	} else {
	    return false;
	}
    }

    /**
     * @return
     */
    protected GSConfOptionInteger getPageSizeOption() {

	GSConfOptionInteger option = (GSConfOptionInteger) getSupportedOptions().get(CSW_CONNECTOR_PAGESIZE_OPTION_KEY);
	option.setValue(20);
	option.setAllowedValues(Arrays.asList(20));
	return option;
    }

    @Override
    public String getLabel() {

	return "CSW UNESCO-IHP Connector";
    }
}
