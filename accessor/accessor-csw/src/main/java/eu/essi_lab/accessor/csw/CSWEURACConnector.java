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

import org.apache.commons.io.IOUtils;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class CSWEURACConnector extends CSWGetConnector {

    private static final String CSWEURACCONNECTOR_EXTRACTION_ERROR = "CSWEURACCONNECTOR_EXTRACTION_ERROR";

    /**
     * 
     */
    public static final String TYPE = "CSW EURAC Connector";

    public CSWEURACConnector() {
	// nothing to do here
	setGetRecordsBinding(Binding.GET);
	// this.getRecordsURLGET = "https://edp-portal.eurac.edu/geonetwork/srv/eng/csw";

    }

    @Override
    void readCapabilitiesDocument() throws GSException {
	super.readCapabilitiesDocument();
	this.getRecordsURLGET = "https://edp-portal.eurac.edu/geonetwork/srv/eng/csw";

    }

    CSWHttpGetRecordsRequestCreator getCreator(GetRecords getRecords) throws GSException {

	return new CSWHttpGetRecordsRequestCreator(getGetRecordsBinding(), this, getRecords) {

	    public String getGetRecordsUrl() {

		//
		// instead of https://edp-portal.eurac.edu:8443/geonetwork/srv/eng/csw
		//

		return "https://edp-portal.eurac.edu/geonetwork/srv/eng/csw";
	    }

	};
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected String getReturnedMetadataSchema() {
	return CommonNameSpaceContext.MULTI;
    }

    /**
     * This fix is a first step in order to support EURAC profile.
     */
    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    str = str.replace("\"http://sdi.eurac.edu/metadata/iso19139-2/schema/gmi\"", "\"http://www.isotc211.org/2005/gmi\"");

	    str = str.replace("\"http://sdi.eurac.edu/metadata/iso19139-2/schema/gmie\"", "\"http://www.isotc211.org/2005/gmi\"");

	    str = str.replace("MIE_", "MI_");

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
		    CSWEURACCONNECTOR_EXTRACTION_ERROR, e);
	}

    }

}
