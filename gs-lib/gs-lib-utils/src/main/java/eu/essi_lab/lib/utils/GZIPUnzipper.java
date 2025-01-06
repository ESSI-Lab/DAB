/**
 * 
 */
package eu.essi_lab.lib.utils;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * @author Fabrizio
 */
public class GZIPUnzipper {

    protected InputStream zipStream;
    protected File folder;

    /**
     * @param zipFile
     * @throws FileNotFoundException
     */
    public GZIPUnzipper(File zipFile) throws FileNotFoundException {

	this.zipStream = new FileInputStream(zipFile);
    }

    public GZIPUnzipper(InputStream zipStream) throws FileNotFoundException {

	this.zipStream = zipStream;
    }

    /**
     * @param folder
     * @throws IOException
     */
    public void setOutputFolder(File folder) throws IOException {

	this.folder = folder;

	if (!folder.exists()) {
	    boolean mkdirs = folder.mkdirs();
	    if (!mkdirs) {
		throw new IOException("Unable to create output folder");
	    }
	}
    }

    /**
     * @return
     */
    public File getOutputFolder() {

	return this.folder;
    }

    /**
     * @return
     * @throws IOException
     */
    public File unzip() throws IOException {

	return unzip(UUID.randomUUID().toString());
    }

    /**
     * @param fileName
     * @return
     * @throws IOException
     */
    public File unzip(String fileName) throws IOException {

	if (this.folder == null) {

	    this.folder = Files.createTempDir();
	}

	ArrayList<File> out = Lists.newArrayList();

	byte[] buffer = new byte[4096];

	GZIPInputStream zis = new GZIPInputStream(this.zipStream);

	File newFile = new File(this.folder, fileName);
	out.add(newFile);

	writeToFile(newFile, buffer, zis);

	zis.close();

	return newFile;
    }

    protected void writeToFile(File newFile, byte[] buffer, InputStream zis) throws IOException {

	try (FileOutputStream fos = new FileOutputStream(newFile)) {

	    int len;
	    while ((len = zis.read(buffer)) > 0) {
		fos.write(buffer, 0, len);
	    }

	} catch (IOException e) {

	    LoggerFactory.getLogger(GZIPUnzipper.class).error("Error writing to file {}", newFile.getAbsolutePath());

	    throw e;
	}
    }
}
