package eu.essi_lab.model.configuration;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class GSJSONSerializable implements Serializable {

    private static final String ERR_ID_SERIALIZATION = "ERR_ID_SERIALIZATION";

    // private void writeObject(ObjectOutputStream out) throws IOException {
    // try {
    // InputStream stream = serializeToInputStream();
    // int i;
    // while ((i = stream.read()) != -1) {
    // out.writeInt(i);
    // }
    // } catch (GSException e) {
    // e.printStackTrace();
    // }
    //
    // }
    //
    // private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // int i;
    // while ((i = in.read()) != -1) {
    // baos.write(i);
    // }
    //
    // }

    public String serialize() throws GSException {

	try {
	    Writer jsonWriter = new StringWriter();

	    try (JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter)) {

		new ObjectMapper().writer().writeValue(jsonGenerator, this);

		jsonGenerator.flush();
	    }

	    return jsonWriter.toString();

	} catch (Exception e) {

	    GSException ex = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setCause(e);

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_SERIALIZATION);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);

	    ex.addInfo(ei);
	    throw ex;

	}
    }

    public InputStream serializeToInputStream() throws GSException {

	try {

	    byte[] bytes = new ObjectMapper().writer().writeValueAsBytes(this);

	    return new ByteArrayInputStream(bytes);

	} catch (Exception e) {

	    GSException ex = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setCause(e);

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_SERIALIZATION);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);

	    ex.addInfo(ei);
	    throw ex;

	}
    }
}
