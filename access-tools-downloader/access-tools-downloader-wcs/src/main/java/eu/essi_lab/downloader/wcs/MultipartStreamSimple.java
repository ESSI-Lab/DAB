package eu.essi_lab.downloader.wcs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Simple multipart extractor.
 * 
 * @author boldrini
 */
public class MultipartStreamSimple {

    private HashMap<String, File> parts;

    public HashMap<String, File> getParts() {
	return parts;
    }

    public MultipartStreamSimple(InputStream body, String boundary) throws IOException  {
	boundary = "--" + boundary;

	List<Byte> boundaryBytes = new ArrayList<>();
	List<Byte> buffer = new ArrayList<>();
	for (Byte b : boundary.getBytes()) {
	    boundaryBytes.add(b);
	}

	this.parts = new HashMap<String, File>(); // hashmap content type / file

	// this is to skip the preamble
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	copyUpToNextSeparator(body, baos, boundaryBytes);

	parts = new HashMap<String, File>();

	List<Byte> lflfBytes = new ArrayList<>();
	for (Byte b : "\n\n".getBytes(StandardCharsets.UTF_8)) {
	    lflfBytes.add(b);
	}

	List<Byte> crlfcrlfBytes = new ArrayList<>();
	for (Byte b : "\r\n\r\n".getBytes(StandardCharsets.UTF_8)) {
	    crlfcrlfBytes.add(b);
	}

	int a;
	while (true) {

	    ByteArrayOutputStream headers = new ByteArrayOutputStream();

	    int written = copyUpToNextSeparator(body, headers, lflfBytes, crlfcrlfBytes);
	    if (written == 0) {
		break;
	    }

	    String headerString = new String(headers.toByteArray(), StandardCharsets.UTF_8);

	    // reached the epilogue
	    if (headerString.trim().equals("--")) {
		break;
	    }

	    headerString = headerString.replace("\r\n", "\n");
	    String[] split = headerString.split("\n");
	    HashMap<String, String> headerMap = new HashMap<>();
	    for (String s : split) {
		String[] split2 = s.split(":");
		if (split2.length == 2) {
		    headerMap.put(split2[0].trim(), split2[1].trim());
		}
	    }
	    String contentType = headerMap.get("Content-Type");
	    File file = File.createTempFile("multipart-simple", ".bin");
	    file.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(file);
	    written = copyUpToNextSeparator(body, fos, boundaryBytes);
	    if (written == 0) {
		break;
	    }
	    parts.put(contentType, file);

	}

    }

    /**
     * Writes to the output stream reading from the given input stream, until one of the given separators is found
     * (separator is not written).
     * 
     * @param input
     * @param output
     * @param separators
     * @throws IOException
     */
    private int copyUpToNextSeparator(InputStream input, OutputStream output, List<Byte>... separators) throws IOException {

	int maxSeparatorSize = 0;
	for (List<Byte> separator : separators) {
	    int size = separator.size();
	    if (maxSeparatorSize < size) {
		maxSeparatorSize = size;
	    }
	}
	int written = 0;

	List<Byte> buffer = new ArrayList<>();
	int a;
	while ((a = input.read()) != -1) {

	    buffer.add((byte) a);

	    int bufferSize = buffer.size();

	    for (List<Byte> separator : separators) {

		int separatorSize = separator.size();

		if (bufferSize >= separatorSize && //
			buffer.subList(bufferSize - separatorSize, bufferSize).equals(separator)) {
		    List<Byte> rest = buffer.subList(0, bufferSize - separatorSize);
		    for (Byte b : rest) {
			written++;
			output.write(b);
		    }
		    output.close();
		    return written;
		}
	    }

	    // buffer filled, let's remove one element to make space
	    if (bufferSize == maxSeparatorSize) {
		Byte b = buffer.remove(0);
		written++;
		output.write(b);
	    }

	}
	return written;
    }

}
