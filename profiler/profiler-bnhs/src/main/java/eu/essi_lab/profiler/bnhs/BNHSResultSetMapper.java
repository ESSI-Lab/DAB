package eu.essi_lab.profiler.bnhs;

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

import java.util.Optional;

import javax.ws.rs.core.UriInfo;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * @author boldrini
 */
public class BNHSResultSetMapper extends DiscoveryResultSetMapper<String> {

    public static final String STATION_DATA_LINK_COLUMN = "Station data link";

    public BNHSResultSetMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema BNHS_MAPPING_SCHEMA = new MappingSchema();
    public static final String SEPARATOR = "\t";

    @Override
    public MappingSchema getMappingSchema() {

	return BNHS_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    Optional<String> optionalBNHS = res.getExtensionHandler().getBNHSInfo();
	    if (optionalBNHS.isPresent()) {
		String info = optionalBNHS.get();

		String link = "";

		Optional<Boolean> optionalExecutable = res.getPropertyHandler().isExecutable();
		Optional<String> optionalPlatform = res.getExtensionHandler().getUniquePlatformIdentifier();
		if (optionalExecutable.isPresent() && optionalPlatform.isPresent()) {
		    if (optionalExecutable.get().booleanValue()) {
			UriInfo uri = message.getWebRequest().getUriInfo();
			String path = message.getWebRequest().getRequestPath();
			String stationId = optionalPlatform.get();
			path = path.replace("csv", "station/" + stationId + "/");
			link = uri.getBaseUri().toString() + path;
			link = link.replace("http://", "https://");
		    }
		}

		return info + STATION_DATA_LINK_COLUMN + SEPARATOR + link + SEPARATOR;
	    } else {
		return "";

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "";

    }
}
