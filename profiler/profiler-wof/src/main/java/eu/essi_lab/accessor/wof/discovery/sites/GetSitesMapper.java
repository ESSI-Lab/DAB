package eu.essi_lab.accessor.wof.discovery.sites;

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

import eu.essi_lab.accessor.wof.WOFMapperUtils;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

public class GetSitesMapper extends DiscoveryResultSetMapper<String> {

    private static final String HIS_RES_SET_MAPPER_ERROR = "HIS_RES_SET_MAPPER_ERROR";

    public GetSitesMapper() {
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
    public String map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    // MDMetadata metadata = coreMetadata.getMDMetadata();
	    MIMetadata metadata = coreMetadata.getMIMetadata();

	    String platformCode = "";
	    String platformName = "";
	    try {
		platformCode = res.getExtensionHandler().getUniquePlatformIdentifier().get();
		platformName = metadata.getMIPlatform().getCitation().getTitle();
	    } catch (Exception e) {
	    }

	    String latitude = "";// 41.5724
	    try {
		latitude = metadata.getDataIdentification().getGeographicBoundingBox().getNorth().toString();
	    } catch (Exception e) {
	    }
	    String longitude = "";// -111.8551
	    try {
		longitude = metadata.getDataIdentification().getGeographicBoundingBox().getEast().toString();
	    } catch (Exception e) {
	    }

	    GSSource source = res.getSource();
	    String servURL = WOFMapperUtils.getServiceUrl(message, source);

	    String ret = "	<Site>\n" + //
		    "			<SiteName>" + platformName + "</SiteName>\n" + //
		    "			<SiteCode>" + platformCode + "</SiteCode>\n" + //
		    "			<Latitude>" + latitude + "</Latitude>\n" + //
		    "			<Longitude>" + longitude + "</Longitude>\n" + //
		    // " <HUC>" + longitude + "</HUC>\n" + //
		    "			<HUCnumeric>0</HUCnumeric>\n" + //
		    "			<servCode>" + source.getLabel() + "</servCode>\n" + //
		    "			<servURL>" + servURL + "</servURL>\n" + //
		    "		</Site>\n";
	    return ret;

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
