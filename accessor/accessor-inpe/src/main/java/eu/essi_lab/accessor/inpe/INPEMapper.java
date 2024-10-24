package eu.essi_lab.accessor.inpe;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

public class INPEMapper extends OriginalIdentifierMapper {

    private static final String INPE_MAPPER_ERROR = "INPE_MAPPER_ERROR";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.INPE_URI;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {
	    String scene = resource.getOriginalMetadata().getMetadata();
	    XMLDocumentReader reader = new XMLDocumentReader(scene);
	    SatelliteSceneMD satellite = new SatelliteSceneMD(reader.getDocument());
	    return satellite.getId();

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	try {
	    String scene = originalMD.getMetadata();
	    XMLDocumentReader reader = new XMLDocumentReader(scene);
	    SatelliteSceneMD satellite = new SatelliteSceneMD(reader.getDocument());
	    String sceneid = satellite.getId(); // evaluateString("//*[local-name()='id']").asString();
	    Dataset ds = satellite.toDataset(sceneid, false);
	    return ds;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INPE_MAPPER_ERROR, //
		    e);
	}
    }
}
