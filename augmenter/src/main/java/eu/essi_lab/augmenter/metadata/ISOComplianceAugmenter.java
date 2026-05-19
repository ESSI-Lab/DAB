package eu.essi_lab.augmenter.metadata;

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
