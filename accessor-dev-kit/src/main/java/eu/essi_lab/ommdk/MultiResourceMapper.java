package eu.essi_lab.ommdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class MultiResourceMapper extends AbstractResourceMapper {

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	IResourceMapper mapper = ResourceMapperFactory.getResourceMapper(originalMD);

	if (mapper != null) {
	    
	    return mapper.map(originalMD, source);
	}

	GSLoggerFactory.getLogger(getClass()).error("Unable to find a mapper for resource with schema: " + originalMD.getSchemeURI());

	return null;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {
	
	OriginalMetadata metadata = resource.getOriginalMetadata();

	AbstractResourceMapper mapper = (AbstractResourceMapper) ResourceMapperFactory.getResourceMapper(metadata);

	if (mapper != null) {

	    return mapper.createOriginalIdentifier(resource);
	}
	
	GSLoggerFactory.getLogger(getClass()).error("Unable to find a mapper for resource with schema: " + metadata.getSchemeURI());

	return null;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.MULTI;
    }

}
