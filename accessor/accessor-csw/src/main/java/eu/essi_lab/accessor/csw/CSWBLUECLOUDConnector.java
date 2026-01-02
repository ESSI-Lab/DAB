package eu.essi_lab.accessor.csw;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CSWBLUECLOUDConnector extends CSWConnector {

    private static final String CSWBLUECLOUDCONNECTOR_EXTRACTION_ERROR = "CSWBLUECLOUDCONNECTOR_EXTRACTION_ERROR";

    /**
     * 
     */
    public static final String TYPE = "CSW BLUECLOUD Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * @return
     */
    public boolean supportsExpectedRecordsCount() {

	return true;
    }

    /**
     * The CSW BLUECLOUD requires a specific mapper
     */

    @Override
    public void filterResults(ListRecordsResponse<OriginalMetadata> ret) {
	// TODO Auto-generated method stub
	List<OriginalMetadata> originalMetadataList = ret.getRecordsAsList();
	for (OriginalMetadata om : originalMetadataList) {
	    om.setSchemeURI(CommonNameSpaceContext.BLUECLOUD_NS_URI);
	}

    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.BLUECLOUD_NS_URI);
	return toret;
    }

    /**
     * The CSW MCP connector applies only to the CSW AODN catalogue
     */
    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (endpoint.contains("csw-EMODNET_Chemistry") || endpoint.contains("csw-SEADATANET")) {
	    boolean cswBaseSupport = super.supports(source);
	    return cswBaseSupport;
	} else {
	    return false;
	}

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
		    CSWBLUECLOUDCONNECTOR_EXTRACTION_ERROR, e);
	}

    }

}
