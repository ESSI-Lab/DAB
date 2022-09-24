package eu.essi_lab.accessor.wof;

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

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.resource.Country;

public class WML_1_1_ISPRAMapper extends WML_1_1Mapper {

    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.WML1_ISPRA_NS_URI;
    }

    public String getSourceOrganization(TimeSeries series) {
	return "ISPRA, Italian Environment Protection and Technical Services Agency - Monitoring Network";
    }

    public String getCountryProperty(SiteInfo siteInfo) {
	return Country.ITALY.getShortName();
    }

}
