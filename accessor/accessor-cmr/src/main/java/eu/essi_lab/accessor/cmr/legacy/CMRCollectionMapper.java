package eu.essi_lab.accessor.cmr.legacy;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMRCollectionAtomEntry;
import eu.essi_lab.accessor.cmr.harvested.CMRCollectionEntryParser;
import eu.essi_lab.accessor.cmr.harvested.CMROriginalMDWrapper;
import eu.essi_lab.accessor.mapper.CWICCMRSecondLevelOSDDResolver;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import eu.essi_lab.ommdk.GMIResourceMapper;

/**
 * @author ilsanto
 */
public class CMRCollectionMapper extends FileIdentifierMapper {

    public static final String SCHEMA_URI = "http://essi-lab.eu/cmr/collections";
    public static final String CWIC_SECOND_LEVEL_TEMPLATE = "cwicSecondLevel";
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    protected static final String DEFAULT_CLIENT_ID = "gs-service";

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	GMIResourceMapper gmiResourceMapper = new GMIResourceMapper();

	CMROriginalMDWrapper wrapper = new CMROriginalMDWrapper();

	GSResource gmiMapped = gmiResourceMapper.map(wrapper.getOriginalMetadata(originalMD), source);

	if (skipCwic(gmiMapped)) {

	    logger.debug("Found a cwic collection with original identifier {}, skipping", gmiMapped.getOriginalId());

	    return null;
	}

	try {

	    enrichWithSecondLevelUrl(gmiMapped, wrapper, originalMD);

	} catch (GSException e) {

	    logger.warn("Unable to add CMR OSDD extension element to collection {}, this collection will not be expandible",
		    gmiMapped.getPublicId());

	    e.log();
	}

	return gmiMapped;
    }

    /**
     * @param gmiMapped
     * @param wrapper
     * @param originalMD
     * @throws GSException
     */
    public void enrichWithSecondLevelUrl(GSResource gmiMapped, CMROriginalMDWrapper wrapper, OriginalMetadata originalMD)
	    throws GSException {

	try {

	    CWICCMRCollectionAtomEntry cmrCollectionAtomEntry = new CWICCMRCollectionAtomEntry(wrapper.getUrl(originalMD));

	    InputStream stream = cmrCollectionAtomEntry.getCollectionAtom(gmiMapped.getOriginalId().get());

	    CMRCollectionEntryParser cmrCollectionEntryParser = new CMRCollectionEntryParser(stream, wrapper.getCMRBaseOSDDUrl(originalMD));

	    String url = cmrCollectionEntryParser.getSecondLevelOpenSearchDD(DEFAULT_CLIENT_ID);

	    CWICCMRSecondLevelOSDDResolver resolver = new CWICCMRSecondLevelOSDDResolver(url);

	    String baseSecondLevelURL = resolver.getSearchBaseUrl();

	    gmiMapped.getHarmonizedMetadata().getExtendedMetadata().add(CWIC_SECOND_LEVEL_TEMPLATE, baseSecondLevelURL);

	} catch (ParserConfigurationException | XPathExpressionException | SAXException | IOException e) {

	    logger.warn("Unable to add CMR OSDD extension element to collection {}, this collection will not be expandible",
		    gmiMapped.getPublicId(), e);

	}
    }

    /**
     * @param gmiMapped
     * @return
     */
    protected boolean skipCwic(GSResource gmiMapped) {

	Iterator<Keywords> keywords = gmiMapped.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getKeywords();

	Optional<Keywords> first = StreamUtils.iteratorToStream(keywords).filter(key ->

	StreamUtils.iteratorToStream(key.getKeywords()).anyMatch(kk -> kk.toLowerCase().contains("cwic"))

	).findFirst();

	return first.isPresent();
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return SCHEMA_URI;
    }

}
