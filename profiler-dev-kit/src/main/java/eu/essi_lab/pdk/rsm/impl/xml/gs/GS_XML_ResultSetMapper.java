package eu.essi_lab.pdk.rsm.impl.xml.gs;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * Basic result set mapper implementation which actually performs no mapping on the {@link GSResource}s, it just
 * serializes them to a string
 * 
 * @author Fabrizio
 */
public class GS_XML_ResultSetMapper extends DiscoveryResultSetMapper<String> {

    private static final String GS_RES_SET_MAPPER_RES_TO_STRING_ERROR = "GS_RES_SET_MAPPER_RES_TO_STRING_ERROR";
 
    @Override
    public String map(DiscoveryMessage message, GSResource resource) throws GSException {

	try {
	    return resource.asString(true);

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GS_RES_SET_MAPPER_RES_TO_STRING_ERROR);
	}
    }

    /**
     * Returns the {@link ESSILabProvider}
     */
    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    /**
     * Returns {@link MappingSchema#GS_DATA_MODEL_MAPPING_SCHEMA}
     */
    @Override
    public MappingSchema getMappingSchema() {

	return MappingSchema.GS_DATA_MODEL_MAPPING_SCHEMA;
    }
}
