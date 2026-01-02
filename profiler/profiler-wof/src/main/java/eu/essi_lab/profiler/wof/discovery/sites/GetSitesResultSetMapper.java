package eu.essi_lab.profiler.wof.discovery.sites;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.cuahsi.waterml._1.SiteInfoResponseType.Site;
import org.cuahsi.waterml._1.SiteInfoType;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * @author boldrini
 */
public class GetSitesResultSetMapper extends DiscoveryResultSetMapper<Site> {

    private static final String HIS_RES_SET_MAPPER_ERROR = "HIS_RES_SET_MAPPER_ERROR";

    public GetSitesResultSetMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema HYDRO_SERVER_SITES_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {

	return HYDRO_SERVER_SITES_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public Site map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    SiteInfoType siteInfo = new SiteInfoType();
	    WMLDataDownloader.augmentSiteInfo(siteInfo, res);
	    Site site = new Site();
	    site.setSiteInfo(siteInfo);
	    return site;

	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_RES_SET_MAPPER_ERROR);
	}

    }
}
