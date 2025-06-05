package eu.essi_lab.ommdk;

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

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class GMIResourceMapper extends GMDResourceMapper {

    protected MIMetadata createMIMetatata(String original) throws UnsupportedEncodingException, JAXBException {

	return new MIMetadata(original);
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.GMI_NS_URI;
    }

    @Override
    public Boolean supportsOriginalMetadata(OriginalMetadata originalMD) {
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(originalMD.getMetadata());

	    String namespace = reader.evaluateString("namespace-uri(/*[1])").toLowerCase();

	    if (namespace.equals(CommonNameSpaceContext.GMI_NS_URI)) {
		return true;
	    }

	} catch (Exception e) {

	}
	return false;
    }
}
