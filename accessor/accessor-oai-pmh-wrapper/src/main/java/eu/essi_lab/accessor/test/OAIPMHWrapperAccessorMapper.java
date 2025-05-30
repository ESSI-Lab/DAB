/**
 * 
 */
package eu.essi_lab.accessor.test;

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

import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * @author Fabrizio
 */
public class OAIPMHWrapperAccessorMapper extends FileIdentifierMapper {

    /**
     * 
     */
    public static final String SCHEMA = "oaipmh.wrapper.accessor.schema";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SCHEMA;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMd, GSSource source) throws GSException {

	String metadata = originalMd.getMetadata();

	try {
	    MIMetadata miMetadata = new MIMetadata(metadata);

	    Dataset dataset = new Dataset();
	    dataset.setPublicId(miMetadata.getFileIdentifier());
	    dataset.setSource(source);

	    dataset.setOriginalMetadata(originalMd);
	    dataset.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);

	    return dataset;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	return null;
    }

}
