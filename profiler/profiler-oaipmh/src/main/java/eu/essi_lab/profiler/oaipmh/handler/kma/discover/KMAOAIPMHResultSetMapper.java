/**
 * 
 */
package eu.essi_lab.profiler.oaipmh.handler.kma.discover;

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

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.MD_MetadataPatch;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.schemas.BooleanValidationHandler;
import eu.essi_lab.jaxb.common.schemas.CommonSchemas;
import eu.essi_lab.jaxb.common.schemas.SchemaValidator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHResultSetMapper;

/**
 * @author Fabrizio
 */
public class KMAOAIPMHResultSetMapper extends OAIPMHResultSetMapper {

    @Override
    protected boolean isValid(GSResource resource) {

	SchemaValidator sv = new SchemaValidator();

	try {

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	    MD_MetadataPatch.applyAll(miMetadata);

	    BooleanValidationHandler handler = sv.validate( //
		    miMetadata.asStream(), //
		    CommonSchemas.GMI());

	    if (!handler.isValid()) {

		GSLoggerFactory.getLogger(getClass()).trace("Event message: {}", handler.getEvent().getMessage());

		return false;
	    }

	} catch (UnsupportedEncodingException | JAXBException e) {
	    GSLoggerFactory.getLogger(getClass()).warn(e.getMessage(), e);
	    return false;
	}

	return true;
    }
}
