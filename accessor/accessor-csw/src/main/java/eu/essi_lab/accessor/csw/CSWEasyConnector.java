package eu.essi_lab.accessor.csw;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

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

public class CSWEasyConnector extends CSWGetConnector {

    /**
     * 
     */
    public static final String TYPE = "CSW Easy Connector";

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected File fixGetRecordsResponse(File file) throws GSException {

	XMLDocumentReader reader = null;
	try {

	    byte[] bytes = Files.readAllBytes(file.toPath());

	    file.delete();

	    String str = new String(bytes, StandardCharsets.UTF_8);
	    str = str.replace("gco:nilReason=\"missing\"", "");
	    str = str.replace("xsi:type=\"lan:", "xsi:type=\"gmd:");
	    reader = new XMLDocumentReader(str);
	    Map<String, String> map = new HashMap<>();
	    map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	    reader.setNamespaces(map);
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    writer.remove("//*[@xsi:type]", "http://www.w3.org/2001/XMLSchema-instance", "type");
	} catch (Exception e1) {
	    GSLoggerFactory.getLogger(getClass()).error(e1);

	    throw GSException.createException( //
		    getClass(), //
		    "XPathExpressionException fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "FIXING_XML", e1);
	}

	try {

	    File ret = File.createTempFile(getClass().getSimpleName(), ".xml");

	    FileOutputStream fos = new FileOutputStream(ret);

	    ByteArrayInputStream stream = reader.asStream();

	    IOUtils.copy(stream, fos);

	    stream.close();
	    fos.close();

	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException( //
		    getClass(), //
		    "Exception fixing GetRecords response", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "FIXING_XML", e);
	}

    }

}
