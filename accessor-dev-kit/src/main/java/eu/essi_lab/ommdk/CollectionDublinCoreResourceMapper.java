/**
 * 
 */
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

import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class CollectionDublinCoreResourceMapper extends DublinCoreResourceMapper {

    public static final String COLLECTION_DC_SCHEMA = "Collection-" + CommonNameSpaceContext.CSW_NS_URI;

    /**
     * @return
     */
    protected GSResource createResource() {

	return new DatasetCollection();
    }

    /**
     * 
     */
    protected List<String> getValues(XMLDocumentReader reader, String dcElement) throws XPathExpressionException {

	if (dcElement.equals("type")) {

	    return Arrays.asList("series");
	}

	return super.getValues(reader, dcElement);
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return COLLECTION_DC_SCHEMA;
    }
}
