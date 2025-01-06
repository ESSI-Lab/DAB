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
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class GSResourceMapper extends FileIdentifierMapper {

    private static final String GS_RESOURCE_MAPPER_ERROR = "GS_RESOURCE_MAPPER_ERROR";

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	GSResource resource;
	try {
	    resource = GSResource.create(originalMD.getMetadata());
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GS_RESOURCE_MAPPER_ERROR, //
		    e);
	}
	
	if (source != null) {
	    resource.setSource(source);
	}

	return resource;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE;
    }

    @Override
    public Boolean supportsOriginalMetadata(OriginalMetadata originalMD) {
	try {
	    GSResource.create(originalMD.getMetadata());
	    return true;
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}

    }
}
