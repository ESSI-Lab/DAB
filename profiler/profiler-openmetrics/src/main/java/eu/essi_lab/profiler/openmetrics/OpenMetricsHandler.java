package eu.essi_lab.profiler.openmetrics;

import java.io.File;
import java.io.FileInputStream;

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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;

public class OpenMetricsHandler extends StreamingRequestHandler {

    private static Optional<S3TransferWrapper> manager = null;

    public synchronized Optional<S3TransferWrapper> getS3TransferManager() {

	if (this.manager == null || this.manager.isEmpty()) {

	    this.manager = ConfigurationWrapper.getS3TransferManager();
	}

	return this.manager;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.valueOf("application/openmetrics-text");
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public OpenMetricsHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	Optional<String> optionalView = webRequest.extractViewId();
	String viewId = optionalView.isPresent() ? optionalView.get() : null;

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {
		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);
		if (viewId == null) {
		    writer.write("A view id is needed to get metrics");
		    writer.flush();
		    writer.close();
		    return;
		}
		getS3TransferManager();
		if (manager.isPresent()) {
		    File tmpFile = File.createTempFile(getClass().getSimpleName() + "-" + viewId, ".txt");
		    boolean result = manager.get().download("dabreporting", "monitoring/" + viewId + ".txt", tmpFile);
		    if (!result) {
			GSLoggerFactory.getLogger(getClass()).error("Exception occurred, please contact DAB administrator");
		    } else {
			FileInputStream fis = new FileInputStream(tmpFile);
			IOUtils.copy(fis, writer, StandardCharsets.UTF_8);
			fis.close();
			tmpFile.delete();
		    }

		}

		writer.write("# EOF");
		writer.flush();
		writer.close();
		output.close();
	    }
	};
    }

}
