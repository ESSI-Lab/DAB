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

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class GMDResourceMapper extends FileIdentifierMapper {

    private static final String GMD_RESOURCE_MAPPER_MAP_ERROR = "GMD_RESOURCE_MAPPER_MAP_ERROR";

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	MIMetadata miMetadata = null;
	try {
	    miMetadata = createMIMetatata(originalMD.getMetadata());
	} catch (Exception e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    String message = e.getMessage();
	    if (message != null && message.contains("Expected elements are")) {

		message = message.substring(0, message.indexOf("Expected elements are"));
	    }

	    throw GSException.createException( //
		    getClass(), //
		    message, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    GMD_RESOURCE_MAPPER_MAP_ERROR //
	    );
	}

	GSResource resource = null;
	String value = miMetadata.getHierarchyLevelScopeCodeListValue();
	if (value == null) {
	    resource = new Dataset();
	} else {
	    switch (value) {
	    case "series":
		resource = new DatasetCollection();
		break;
	    // case "service":
	    // resource = new DatasetService();
	    // break;
	    case "dataset":
	    default:
		resource = new Dataset();
		break;
	    }
	}

	resource.setSource(source);

	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.setMIMetadata(miMetadata);

	return resource;
    }

    protected MIMetadata createMIMetatata(String original) throws UnsupportedEncodingException, JAXBException {

	MDMetadata mdMetadata = new MDMetadata(original);
	MIMetadata miMetadata = new MIMetadata(mdMetadata.getElementType());

	return miMetadata;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.GMD_NS_URI;
    }

    @Override
    public Boolean supportsOriginalMetadata(OriginalMetadata originalMD) {
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(originalMD.getMetadata());

	    String namespace = reader.evaluateString("namespace-uri(/*[1])").toLowerCase();

	    if (namespace.equals(CommonNameSpaceContext.GMD_NS_URI)) {
		return true;
	    }

	} catch (Exception e) {

	}
	return false;
    }
}
