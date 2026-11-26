package eu.essi_lab.profiler.om;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.json.JSONObject;

/**
 * Writes bulk metadata export as a single JSON document, streaming one observation at a time.
 * <p>
 * The document has the same structure as the OM API {@code observations} JSON response (as returned by the synchronous
 * metadata download), i.e. an observation collection with the observations in the {@code member} array:
 *
 * <pre>
 * {"id":"observation collection","member":[ {...}, {...} ],"completed":true,"total":2}
 * </pre>
 */
public final class MetadataJsonWriter implements Closeable {

    private final BufferedWriter writer;
    private int count;

    /**
     * @param file the JSON file to create
     * @throws IOException if the file can not be opened
     */
    public MetadataJsonWriter(File file) throws IOException {

	this.writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
	this.writer.write("{\"id\":\"observation collection\",\"member\":[\n");
    }

    /**
     * Appends an observation to the {@code member} array.
     */
    public void append(JSONObject observation) throws IOException {

	if (count > 0) {
	    writer.write(",\n");
	}
	writer.write(observation.toString());
	count++;
    }

    /**
     * Closes the {@code member} array and the document, then closes the underlying file.
     */
    @Override
    public void close() throws IOException {

	try {
	    writer.write("\n],\"completed\":true,\"total\":" + count + "}\n");
	} finally {
	    writer.close();
	}
    }
}
