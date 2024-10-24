/**
 * 
 */
package eu.essi_lab.accessor.csw;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;

/**
 * @author Fabrizio
 */
public class CSWCMEMSConnector extends CSWConnector {

    private static final String CSWCMEMSCONNECTOR_EXTRACTION_ERROR = "CSWCMEMSCONNECTOR_EXTRACTION_ERROR";

    /**
     * 
     */
    public static final String TYPE = "CSW CMEMS Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    CSWHttpGetRecordsRequestCreator getCreator(GetRecords getRecords) throws GSException {

	return new CSWHttpGetRecordsRequestCreator(getGetRecordsBinding(), this, getRecords) {

	    //
	    // instead of http://cmems-catalog-ro.cls.fr/geonetwork/srv/eng/csw-MYOCEAN-CORE-PRODUCTS?
	    // of the capabilities
	    //
	    public String getGetRecordsUrl() {

		return "https://cmems-catalog-ro.cls.fr/geonetwork/srv/eng/csw-MYOCEAN-CORE-PRODUCTS";
	    }
	};
    }

    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    // str = str.replace("\"http://sdi.eurac.edu/metadata/iso19139-2/schema/gmi\"",
	    // "\"http://www.isotc211.org/2005/gmi\"");
	    //
	    // str = str.replace("\"http://sdi.eurac.edu/metadata/iso19139-2/schema/gmie\"",
	    // "\"http://www.isotc211.org/2005/gmi\"");
	    //
	    // str = str.replace("MIE_", "MI_");

	    str = str.replace("\"http://www.opengis.net/gml/3.2\"", "\"http://www.opengis.net/gml\"");
	    // xmlns:gml="http://www.opengis.net/gml/3.2"

	    File ret = File.createTempFile(getClass().getSimpleName(), ".xml");

	    ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

	    FileOutputStream fos = new FileOutputStream(ret);

	    IOUtils.copy(stream, fos);

	    stream.close();
	    fos.close();

	    return ret;
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error fixing GetRecords response", e);

	    throw GSException.createException( //
		    getClass(), //
		    "Error fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSWCMEMSCONNECTOR_EXTRACTION_ERROR, e);
	}

    }

    @Override
    public void filterResults(ListRecordsResponse<OriginalMetadata> ret) {

	Iterator<OriginalMetadata> iterator = ret.getRecords();
	List<OriginalMetadata> metadatas = new ArrayList<>();
	int countRecord = 0;
	while (iterator.hasNext()) {
	    countRecord++;
	    OriginalMetadata originalMetadata = (OriginalMetadata) iterator.next();
	    String metadata = originalMetadata.getMetadata();

	    try {
		MDMetadata mdMetadata = new MDMetadata(metadata);
		MIMetadata miMetadata = new MIMetadata(mdMetadata.getElementType());
		Iterator<DataIdentification> identificationIterator = miMetadata.getDataIdentifications();

		while (identificationIterator.hasNext()) {
		    DataIdentification identification = identificationIterator.next();
		    Iterator<Keywords> keywords = identification.getKeywords();
		    while (keywords.hasNext()) {
			Keywords keyword = keywords.next();
			String typeCode = keyword.getTypeCode();
			String href = null;
			String paramValue = null;
			if (typeCode != null && !typeCode.isEmpty()) {

//			    Citation thesaurus = keyword.getThesaurusCitation();
//			    if (thesaurus != null) {
//				CharacterStringPropertyType citType = thesaurus.getElementType().getTitle();
//				href = ISOMetadata.getHREFStringFromCharacterString(citType);
//			    }
			    List<CharacterStringPropertyType> listKey = keyword.getElement().getValue().getKeyword();

			    for (CharacterStringPropertyType c : listKey) {
				href = ISOMetadata.getHREFStringFromCharacterString(c);
				paramValue = ISOMetadata.getStringFromCharacterString(c);
				if (paramValue != null && !paramValue.isEmpty()) {
				    if (typeCode.toLowerCase().contains("parameter")) {

					CoverageDescription description = new CoverageDescription();
					description.setAttributeDescription(paramValue);
					description.setAttributeTitle(paramValue);
					if (href != null)
					    description.setAttributeIdentifier(href);
					miMetadata.addCoverageDescription(description);
					mdMetadata.addCoverageDescription(description);

				    } else if (typeCode.toLowerCase().contains("instrument")) {
					MIInstrument myInstrument = new MIInstrument();
					myInstrument.setDescription(paramValue);
					myInstrument.setTitle(paramValue);
					if (href != null)
					    myInstrument.setMDIdentifierTypeCode(href);
					miMetadata.addMIInstrument(myInstrument);
					// mdMetadata.addMIInstrument(myInstrument);
				    } else if (typeCode.toLowerCase().contains("platform")) {
					MIPlatform platform = new MIPlatform();
					platform.setDescription(paramValue);
					Citation platformCitation = new Citation();
					platformCitation.setTitle(paramValue);
					if (href != null)
					    platform.setMDIdentifierCode(href);
					platform.setCitation(platformCitation);
					miMetadata.addMIPlatform(platform);
					// mdMetadata.addMIPlatform(platform);
				    }
				}
			    }
			}
		    }
		}
		// MDMetadata newMDMetadata = new MDMetadata()
		OriginalMetadata omd = new OriginalMetadata();
		omd.setMetadata(mdMetadata.asString(true));
		omd.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
		metadatas.add(omd);

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	GSLoggerFactory.getLogger(getClass()).error("Number of records: ", countRecord);
	ret.clearRecords();
	for (OriginalMetadata originalMetadata : metadatas) {
	    ret.addRecord(originalMetadata);
	}

    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("cmems-catalog-ro.cls.fr")) {
	    return super.supports(source);
	} else {
	    return false;
	}
    }
}
