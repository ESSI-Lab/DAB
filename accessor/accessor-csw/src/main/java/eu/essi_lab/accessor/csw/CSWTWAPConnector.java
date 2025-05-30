package eu.essi_lab.accessor.csw;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class CSWTWAPConnector extends CSWConnector {

    private static final String CSWTWAPCONNECTOR_EXTRACTION_ERROR = "CSWTWAPCONNECTOR_EXTRACTION_ERROR";

    /**
     * 
     */
    public static final String TYPE = "CSW TWAP Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    /**
     * This fix is needed to tweak the metadata records returned by GEOSS services #69. As they are not valid. In
     * particular, the
     * AbstractMD_Identification !? element is used causing a JAXB error. So, the incriminated element is removed and
     * its content merged
     * with the regular MD_DataIdentification.
     */
    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);

	    str = str.replace("<gmd:AbstractMD_Identification>", "<gmd:MD_DataIdentification>");

	    str = str.replace("</gmd:AbstractMD_Identification>", "</gmd:MD_DataIdentification>");

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
		    CSWTWAPCONNECTOR_EXTRACTION_ERROR, e);
	}

    }

}
