package eu.essi_lab.profiler.thredds.dds;

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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatterInline;

public class DDSAccessResultSetFormatterInline extends AccessResultSetFormatterInline {

    private String path = "";

    @Override
    public Response format(AccessMessage message, ResultSet<DataObject> mappedResultSet) throws GSException {
	this.path = message.getWebRequest().getRequestPath().replace(".dds", "");
	return super.format(message, mappedResultSet);
    }

    @Override
    public void refineDataObject(DataObject dataObject) {
	File file = dataObject.getFile();
	try {
	    byte[] bytes = Files.readAllBytes(file.toPath());
	    String str = new String(bytes, StandardCharsets.UTF_8);
	    str = str.replace("} ;", "} " + path + ";");
	    FileUtils.writeStringToFile(file, str, StandardCharsets.UTF_8);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
