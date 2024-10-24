/**
 * 
 */
package eu.essi_lab.accessor.gbif.harvested;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.CollectionDublinCoreResourceMapper;

/**
 * @author Fabrizio
 */
public class GBIFCollectionMapper extends CollectionDublinCoreResourceMapper {

    /**
     * 
     */
    public static final String GBIF_COLLECTION_MAPPER_SCHEME_URI = "GBIF_COLLECTION_MAPPER_SCHEME_URI";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return GBIF_COLLECTION_MAPPER_SCHEME_URI;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	GSResource resource = super.execMapping(originalMD, source);

	Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	Format format = new Format();
	format.setName("EML");

	distribution.addFormat(format);

	format = new Format();
	format.setName("GBIF annotated archive");

	distribution.addFormat(format);

	return resource;
    }

    /**
     * 
     */
    protected List<String> getValues(XMLDocumentReader reader, String dcElement) throws XPathExpressionException {

	if (dcElement.equals("link")) {

	    ArrayList<String> links = new ArrayList<>();

	    List<String> identifiers = super.getValues(reader, "identifier");

	    if (!identifiers.isEmpty()) {

		String identifier = identifiers.get(0);
		String datasetKey = identifier.substring(identifier.lastIndexOf("/") + 1, identifier.length());

		//
		// EML link and dataset page link
		//
		// https://www.gbif.org/dataset/8a863029-f435-446a-821e-275f4f641165 -->
		// https://api.gbif.org/v1/dataset/8a863029-f435-446a-821e-275f4f641165/document
		//
		String emlLink = "https://api.gbif.org/v1/dataset/" + datasetKey + "/document";

		links.add(emlLink);
		links.add(identifier);

		//
		// GBIF annotated archive (requires username and password)
		//
		// https://www.gbif.org/dataset/8a863029-f435-446a-821e-275f4f641165 -->
		// https://www.gbif.org/occurrence/download?dataset_key=8a863029-f435-446a-821e-275f4f641165
		//
		String annotatedLink = "https://www.gbif.org/occurrence/download?dataset_key=" + datasetKey;
		links.add(annotatedLink);
	    }

	    return links;
	}

	return super.getValues(reader, dcElement);
    }
}
