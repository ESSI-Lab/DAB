package eu.essi_lab.profiler.gwis;

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

public class GWISDataResultSetFormatter extends AccessResultSetFormatter<DataObject> {

    @Override
    public Response format(AccessMessage message, ResultSet<DataObject> mappedResultSet) throws GSException {

	List<ErrorInfo> errors = mappedResultSet.getException().getErrorInfoList();
	if (!errors.isEmpty()) {

	    return handleException(message, mappedResultSet, errors);
	}

	DataObject dataObject = mappedResultSet.getResultsList().get(0);

	File file = dataObject.getFile();

	try {
	    GWISDataRequestTransformer grt = new GWISDataRequestTransformer();
	    GSResource resource = grt.retrieveResource(message.getWebRequest());
	    FileInputStream fis = new FileInputStream(file);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    IOUtils.copy(fis, baos);
	    fis.close();
	    baos.close();
	    String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

	    MIMetadata metadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	    String siteCode = resource.getExtensionHandler().getUniquePlatformIdentifier().get();
	    String parameterCode = resource.getExtensionHandler().getUniqueAttributeIdentifier().get();
	    content = content.replace("SITE_CODE", siteCode).replace("PARAMETER_CODE", parameterCode);
	    ResponsibleParty poc = metadata.getDataIdentification().getPointOfContact();
	    if (poc != null) {
		String organisation = poc.getOrganisationName();
		if (organisation != null && !organisation.isEmpty()) {
		    content = content.replace("AGENCY", organisation);
		}
	    }
	    FileOutputStream fos = new FileOutputStream(file);
	    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
	    IOUtils.copy(bais, fos);
	    bais.close();
	    fos.close();

	} catch (Exception e) {
	    e.printStackTrace();
	}

	ResponseBuilder builder = getBuilder(message, file);

	return builder.build();
    }

    protected ResponseBuilder getBuilder(AccessMessage message, File file) {

	Response.status(Status.OK);

	ResponseBuilder builder = Response.status(Status.OK);

	StreamingOutput stream = new StreamingOutput() {
	    @Override
	    public void write(OutputStream out) throws IOException, WebApplicationException {
		try (FileInputStream inp = new FileInputStream(file)) {
		    byte[] buff = new byte[1024];
		    int len = 0;
		    while ((len = inp.read(buff)) >= 0) {
			out.write(buff, 0, len);
		    }
		    out.flush();
		    out.close();
		    inp.close();
		} catch (Exception e) {
		    throw new IOException("Stream error: " + e.getMessage());
		} finally {
		    file.delete();
		}
	    }

	};

	builder = builder.entity(stream).type(MediaType.TEXT_PLAIN);

	return builder;
    }

    /**
     * @param message
     * @param mappedResultSet
     * @param errors
     * @return
     */
    protected Response handleException(//
	    AccessMessage message, //
	    MessageResponse<DataObject, CountSet> mappedResultSet, //
	    List<ErrorInfo> errors) {

	return null;
    }

    @Override
    public FormattingEncoding getEncoding() {

	return null;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
