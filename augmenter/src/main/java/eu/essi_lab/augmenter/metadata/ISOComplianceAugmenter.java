package eu.essi_lab.augmenter.metadata;

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

import eu.essi_lab.augmenter.*;
import eu.essi_lab.cfga.gs.setting.augmenter.*;
import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.jaxb.common.schemas.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class ISOComplianceAugmenter extends ResourceAugmenter<AugmenterSetting> {

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	try {

	    SchemaValidator sv = new SchemaValidator();

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	    BooleanValidationHandler handler = sv.validate( //
		    miMetadata.asStream(), //
		    CommonSchemas.GMI()); //

	    resource.getPropertyHandler().setIsISOCompliant(handler.isValid());

	    GSLoggerFactory.getLogger(getClass()).trace("Resource validation {}", handler.isValid() ? "succeeded" : "failed");

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return Optional.of(resource);
    }

    @Override
    protected String initName() {

	return "ISO Compliance augmenter";
    }

    @Override
    public String getType() {

	return "ISOComplianceAugmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }
}
