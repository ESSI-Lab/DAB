package eu.essi_lab.gssrv.rest;

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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Parses multipart form requests for the support REST API.
 */
public class MultipartSupport {

    /**
     * @param servletRequest
     * @return form fields and optional file part
     */
    public static ParsedMultipart parse(HttpServletRequest servletRequest) throws Exception {

	if (!JakartaServletFileUpload.isMultipartContent(servletRequest)) {

	    throw new IllegalArgumentException("Expected multipart form data");
	}

	DiskFileItemFactory factory = DiskFileItemFactory.builder().get();

	JakartaServletFileUpload upload = new JakartaServletFileUpload(factory);

	List<DiskFileItem> items = upload.parseRequest(servletRequest);

	Map<String, String> fields = new HashMap<>();

	String fileName = null;
	InputStream fileStream = null;

	for (DiskFileItem item : items) {

	    if (item.isFormField()) {

		fields.put(item.getFieldName(), item.getString(StandardCharsets.UTF_8));

	    } else if ("file".equals(item.getFieldName())) {

		fileName = item.getName();
		fileStream = item.getInputStream();
	    }
	}

	return new ParsedMultipart(fields, fileName, fileStream);
    }

    /**
     * Parsed multipart body.
     */
    public static class ParsedMultipart {

	private final Map<String, String> fields;
	private final String fileName;
	private final InputStream fileStream;

	/**
	 * @param fields
	 * @param fileName
	 * @param fileStream
	 */
	public ParsedMultipart(Map<String, String> fields, String fileName, InputStream fileStream) {

	    this.fields = fields;
	    this.fileName = fileName;
	    this.fileStream = fileStream;
	}

	public Map<String, String> getFields() {

	    return fields;
	}

	public Optional<String> getField(String name) {

	    return Optional.ofNullable(fields.get(name));
	}

	public String getFileName() {

	    return fileName;
	}

	public InputStream getFileStream() {

	    return fileStream;
	}
    }
}
