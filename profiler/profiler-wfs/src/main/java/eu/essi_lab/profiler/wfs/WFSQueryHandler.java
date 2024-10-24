package eu.essi_lab.profiler.wfs;

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

import java.util.List;

import javax.xml.namespace.QName;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.wfs.WFSRequest.Parameter;
import eu.essi_lab.profiler.wfs.feature.FeatureType;
import eu.essi_lab.profiler.wfs.feature.WFSGetFeatureRequest;

public abstract class WFSQueryHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	WFSGetFeatureRequest wfsRequest = new WFSGetFeatureRequest(request);

	String type = wfsRequest.getParameterValue(Parameter.TYPE_NAME);

	FeatureType featureType = findFeatureType(type);

	if (featureType == null) {
	    ValidationMessage ret = new ValidationMessage();
	    ret.setError("Feature type " + type + " unknown");
	    ret.setErrorCode(WFSProfiler.ERROR_CODE_INVALID_PARAMETER_VALUE);
	    ret.setLocator(Parameter.TYPE_NAME.getKeys()[0]);
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	    return ret;
	}

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public FeatureType findFeatureType(String type) {
	List<FeatureType> featureTypes = FeatureType.getFeatureTypes(null);
	for (FeatureType featureType : featureTypes) {
	    QName name = featureType.getQName();
	    String extended = name.getPrefix() + ":" + name.getLocalPart();
	    if (type.equals(extended)) {
		return featureType;
	    }
	}
	return null;
    }

    public StorageInfo getStorageURI(DiscoveryMessage message) throws GSException {
	StorageInfo storageUri = ConfigurationWrapper.getDatabaseURI();
	if (storageUri != null) {

	    message.setDataBaseURI(storageUri);

	} else {

	    GSException exception = GSException.createException(getClass(), //
		    "Data Base storage URI not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_WARNING, //
		    "DB_STORAGE_URI_NOT_FOUND");

	    message.getException().getErrorInfoList().add(exception.getErrorInfoList().get(0));

	    GSLoggerFactory.getLogger(this.getClass()).warn("Data Base storage URI not found");
	}
	return storageUri;
    }
}
