package eu.essi_lab.harvester.component;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.ConstraintViolation;

import eu.essi_lab.harvester.HarvestingComponent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
public class ResourceValidatorComponent extends HarvestingComponent {

    private static final long serialVersionUID = -5221977749437102778L;
    private static final String RESOURCE_VALIDATION_ERROR = "RESOURCE_VALIDATION_ERROR";
    private static final String NULL_RESOURCE_TO_VALIDATE_ERROR = "NULL_RESOURCE_TO_VALIDATE_ERROR";
    private final String label = "Resource validator";

    public ResourceValidatorComponent() {
	/**
	 * Empty constructor. Mandatory due serialization.
	 */
    }

    @Override
    public String getLabel() {
	return label;
    }

    /**
     * Check HarmonizedMetadata object. If object is not valid a
     * {@link GSException} is raised.
     */
    @Override
    public void apply(GSResource resource) throws HarvesterComponentException {

	if (Objects.isNull(resource)) {

	    throw new HarvesterComponentException(//
		    GSException.createException(//
			    getClass(), //
			    "Resource to validate is null", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    NULL_RESOURCE_TO_VALIDATE_ERROR));
	}

	if (resource.getPropertyHandler().isDeleted()) {
	    return;
	}

	List<ConstraintViolation<GSResource>> validate = resource.validate();
	if (!validate.isEmpty()) {

	    GSException gsException = new GSException();

	    for (ConstraintViolation<GSResource> constraintViolation : validate) {

		String message = constraintViolation.getMessage();
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorDescription(message);
		errorInfo.setErrorId(RESOURCE_VALIDATION_ERROR);
		errorInfo.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
		errorInfo.setSeverity(ErrorInfo.SEVERITY_ERROR);

		GSLoggerFactory.getLogger(getClass()).error(message);

		gsException.getErrorInfoList().add(errorInfo);
	    }

	    throw new HarvesterComponentException(gsException);
	}
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	/**
	 * It holds no options, so won't return any.
	 */
	return new HashMap<>();
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	/**
	 * It holds no options, so won't set any.
	 */
	return;
    }

    @Override
    public void onFlush() throws GSException {
	// TODO Auto-generated method stub

    }
}
