package eu.essi_lab.accessor.cmr.legacy;

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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMRCollectionAtomEntry;
import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMRCollectionEntryParser;
import eu.essi_lab.accessor.cmr.harvested.CMROriginalMDWrapper;
import eu.essi_lab.accessor.mapper.CWICCMRSecondLevelOSDDResolver;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CWICCMRCollectionMapper extends CMRCollectionMapper {

    public static final String SCHEMA_URI = "http://essi-lab.eu/cmr/collections/cwic";
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    @Override
    protected boolean skipCwic(GSResource gmiMapped) {

	return false;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CWICCMRCollectionMapper.SCHEMA_URI;
    }

    @Override
    public void enrichWithSecondLevelUrl(GSResource gmiMapped, CMROriginalMDWrapper wrapper, OriginalMetadata originalMD)
	    throws GSException {

	try {

	    CWICCMRCollectionAtomEntry cmrCollectionAtomEntry = new CWICCMRCollectionAtomEntry(wrapper.getUrl(originalMD));

	    InputStream stream = cmrCollectionAtomEntry.getCollectionAtom(gmiMapped.getOriginalId().get());

	    CWICCMRCollectionEntryParser cmrCollectionEntryParser = new CWICCMRCollectionEntryParser(stream);

	    String url = cmrCollectionEntryParser.getSecondLevelOpenSearchDD(DEFAULT_CLIENT_ID);

	    CWICCMRSecondLevelOSDDResolver resolver = new CWICCMRSecondLevelOSDDResolver(url);

	    String baseSecondLevelURL = resolver.getSearchBaseUrl();

	    gmiMapped.getHarmonizedMetadata().getExtendedMetadata().add(CWIC_SECOND_LEVEL_TEMPLATE, baseSecondLevelURL);

	} catch (ParserConfigurationException | XPathExpressionException | SAXException | IOException e) {

	    logger.warn("Unable to add CMR OSDD extension element to collection {}, this collection will not be expandible",
		    gmiMapped.getPublicId(), e);

	}
    }
}
