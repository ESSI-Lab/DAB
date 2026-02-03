package eu.essi_lab.profiler.os.handler.discover.datahub;

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

import javax.ws.rs.core.MediaType;

import eu.essi_lab.accessor.datahub.DatahubToJsonMapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * Mapper for DataHub JSON output format (v3.8 metadata model).
 * Maps GSResource to DataHub JSON via {@link DatahubToJsonMapper}.
 */
public class DatahubJsonMapper extends DiscoveryResultSetMapper<String> {

    /**
     * Encoding name of {@link #DATAHUB_JSON_MAPPING_SCHEMA}
     */
    public static final String DATAHUB_JSON_ENCODING_NAME = "datahub-json-dm-enc";

    /**
     * Encoding version of {@link #DATAHUB_JSON_MAPPING_SCHEMA}
     */
    public static final String DATAHUB_JSON_ENCODING_VERSION = "1.0";

    /**
     * Media type for the DataHub JSON output format.
     */
    public static final String DATAHUB_JSON_MEDIA_TYPE = "application/vnd.essi.datahub+json";

    /**
     * The {@link MappingSchema} for this DataHub JSON format.
     */
    public static final MappingSchema DATAHUB_JSON_MAPPING_SCHEMA = new MappingSchema();

    static {
	DATAHUB_JSON_MAPPING_SCHEMA.setUri("https://www.essi-lab.eu/datahub-json/1.0");
	DATAHUB_JSON_MAPPING_SCHEMA.setName("datahub-json");
	DATAHUB_JSON_MAPPING_SCHEMA.setVersion(DATAHUB_JSON_ENCODING_VERSION);
	DATAHUB_JSON_MAPPING_SCHEMA.setEncoding(DATAHUB_JSON_ENCODING_NAME);
	DATAHUB_JSON_MAPPING_SCHEMA.setEncodingVersion(DATAHUB_JSON_ENCODING_VERSION);
	DATAHUB_JSON_MAPPING_SCHEMA.setEncodingMediaType(MediaType.valueOf(DATAHUB_JSON_MEDIA_TYPE));
    }

    @Override
    public String map(DiscoveryMessage message, GSResource resource) {

	return DatahubToJsonMapper.toJson(resource);
    }

    @Override
    public MappingSchema getMappingSchema() {

	return DATAHUB_JSON_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
