/**
 * 
 */
package eu.essi_lab.accessor.satellite.common;

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

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * @author Fabrizio
 */
public class SatelliteCollectionMapper extends FileIdentifierMapper {

    public static final String SATELLITE_COLLECTION_SCHEME_URI = "sat-collection-scheme-uri";

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String metadata = originalMD.getMetadata();
	GSResource collection = null;
	try {
	    collection = GSResource.create(metadata);
	    // set the correct sources
	    collection.setSource(source);
	    // set the correct name space
	    collection.getOriginalMetadata().setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI);
	    // set the correct original md
	    collection.getOriginalMetadata().setMetadata(//
		    collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().asString(false));

	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return collection;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SATELLITE_COLLECTION_SCHEME_URI;
    }
}
