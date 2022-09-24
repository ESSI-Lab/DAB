package eu.essi_lab.accessor.csw;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDKeywordsPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;

public class CSWCMRConnector extends CSWConnector {

    private static final String CSWCMRCONNECTOR_EXTRACTION_ERROR = "CSWCMRCONNECTOR_EXTRACTION_ERROR";

    /**
     * 
     */
    public static final String TYPE = "CSW CMR Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * The CSW NODC always returns GMI Metadata according to the NODC profile (even if GMD Metadata is asked)
     */

    @Override
    protected String getReturnedMetadataSchema() {

	return CommonNameSpaceContext.CMR_NS_URI;
    }

    @Override
    protected String getRequestedMetadataSchema() throws GSException {

	return CommonNameSpaceContext.GMD_NS_URI;
    }

    /**
     * The CSW CMR connector applies only to the NASA CMR catalogue
     */
    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("cmr")) {
	    return super.supports(source);

	} else {
	    return false;
	}
    }

    /**
     * This fix is needed to tweak the metadata records returned by CMR, as they are not valid according the ususal
     * APISO schemas. In particular, the GML used is the 3.2, but with a different namespace (which includes the
     * version); so they are
     * skipped by JAXB. So, the incriminated namespace is "replaced" all over the document.
     */
    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    str = str.replace("\"http://www.opengis.net/gml/3.2\"", "\"http://www.opengis.net/gml\"");

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
		    CSWCMRCONNECTOR_EXTRACTION_ERROR, e);
	}

    }

    /**
     * Sub connectors might implement this method to reduce (e.g. filtering out some results from the returned list)
     * 
     * @param ret
     */
    public void filterResults(ListRecordsResponse<OriginalMetadata> ret) {
	Iterator<OriginalMetadata> iterator = ret.getRecords();
	List<OriginalMetadata> metadatas = new ArrayList<>();
	int cwicRecords = 0;
	while (iterator.hasNext()) {
	    OriginalMetadata originalMetadata = (OriginalMetadata) iterator.next();
	    String metadata = originalMetadata.getMetadata();
	    try {
		@SuppressWarnings("rawtypes")
		JAXBElement je = CommonContext.unmarshal(metadata, JAXBElement.class);
		Object obj = je.getValue();
		if (obj instanceof MDMetadataType) {
		    MDMetadataType md = (MDMetadataType) obj;
		    List<MDKeywordsPropertyType> keys = md.getIdentificationInfo().get(0).getAbstractMDIdentification().getValue()
			    .getDescriptiveKeywords();
		    boolean isFromCWIC = false;
		    for (MDKeywordsPropertyType key : keys) {
			List<CharacterStringPropertyType> keywords = key.getMDKeywords().getKeyword();
			for (CharacterStringPropertyType keyword : keywords) {
			    String k = ISOMetadata.getStringFromCharacterString(keyword).toLowerCase();
			    if (k.equals("cwic") || k.equals("ceos")) {
				isFromCWIC = true;
				cwicRecords++;
			    }
			}

		    }
		    if (!isFromCWIC) {
			metadatas.add(originalMetadata);
		    }
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info(metadatas.size() + " records harvested (" + cwicRecords + " from CWIC rejected)");
	ret.clearRecords();
	for (OriginalMetadata originalMetadata : metadatas) {
	    ret.addRecord(originalMetadata);
	}

    }

}
