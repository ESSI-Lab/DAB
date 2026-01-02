package eu.essi_lab.lib.utils;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonFileBuffer<T> {

    private final File file;
    private final ObjectMapper mapper;
    private final ObjectWriter writer;
    private final ObjectReader reader;

    public JsonFileBuffer(Class<T> type) throws IOException {
	this(File.createTempFile(JsonFileBuffer.class.getSimpleName(), ".buf"), type);
	file.deleteOnExit();
    }

    public JsonFileBuffer(File file, Class<T> type) throws IOException {
	this.file = file;
	this.mapper = new ObjectMapper();
	this.writer = mapper.writerFor(type);
	this.reader = mapper.readerFor(type);

    }

    /** Add a new POJO object to the buffer */
    public synchronized void add(T obj) throws IOException {
	try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardOpenOption.APPEND)) {
	    bw.write(writer.writeValueAsString(obj));
	    bw.newLine();
	}
    }

    /** Poll and remove the oldest N objects (memory-efficient) */
    public synchronized List<T> poll(int maxItems) throws IOException {
	List<T> results = new ArrayList<>(maxItems);
	File tempFile = Files.createTempFile(getClass().getSimpleName(), ".tmp").toFile();
	try (BufferedReader reader = Files.newBufferedReader(file.toPath());
		BufferedWriter tempWriter = Files.newBufferedWriter(tempFile.toPath(), StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING)) {
	    String line;
	    int count = 0;

	    while ((line = reader.readLine()) != null) {
		if (count < maxItems) {
		    results.add(this.reader.readValue(line));
		    count++;
		} else {
		    tempWriter.write(line);
		    tempWriter.newLine();
		}
	    }
	}

	// Atomically replace the original file with the temp file
	Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	tempFile.delete();

	return results;
    }

    public synchronized int sizeEstimate() throws IOException {
	int count = 0;
	try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
	    while (br.readLine() != null)
		count++;
	}
	return count;
    }
}
