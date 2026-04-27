package eu.essi_lab.profiler.geodcat;

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

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * Pass-through mapping for JSON-LD formatters (harmonized metadata on {@link GSResource}).
 */
public class GeoDcatResultSetMapper extends DiscoveryResultSetMapper<GSResource> {

    public static final MappingSchema GEO_DCAT_MAPPING_SCHEMA = new MappingSchema();

    public GeoDcatResultSetMapper() {

	setMappingStrategy(MappingStrategy.PRIORITY_TO_CORE_METADATA);
    }

    @Override
    public MappingSchema getMappingSchema() {

	return GEO_DCAT_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public GSResource map(DiscoveryMessage message, GSResource resource) throws GSException {

	return resource;
    }
}
