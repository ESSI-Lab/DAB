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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * @author Fabrizio
 */
public class Unzipper extends GZIPUnzipper {

    private Predicate<ZipEntry> predicate;

    /**
     * @throws FileNotFoundException
     */
    public Unzipper(File zipFile) throws FileNotFoundException {

	super(zipFile);
	this.predicate = entry -> true;
    }

    /**
     * @throws FileNotFoundException
     */
    public Unzipper(InputStream zipStream) throws FileNotFoundException {

	super(zipStream);
	this.predicate = entry -> true;
    }

    /**
     * @param predicate
     */
    public void setFilter(Predicate<ZipEntry> predicate) {

	this.predicate = predicate;
    }

    /**
     * @return
     * @throws IOException
     */
    public File unzip() throws IOException {

	List<File> list = unzipAll();
	if (!list.isEmpty()) {
	    return list.get(0);
	}

	return null;
    }

    /**
     * @param etryFilePrefix
     * @return
     * @throws IOException
     */
    public File unzip(String etryFilePrefix) throws IOException {

	List<File> list = unzipAll(etryFilePrefix);
	if (!list.isEmpty()) {
	    return list.get(0);
	}

	return null;
    }

    /**
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<File> unzipAll() throws IOException {

	return unzipAll(UUID.randomUUID().toString());
    }

    /**
     * @param etryFilePrefix
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<File> unzipAll(String etryFilePrefix) throws IOException {

	if (this.folder == null) {

	    this.folder = Files.createTempDir();
	}

	ArrayList<File> out = Lists.newArrayList();

	byte[] buffer = new byte[4096];

	ZipInputStream zis = new ZipInputStream(this.zipStream);
	ZipEntry ze = null;

	while ((ze = zis.getNextEntry()) != null) {

	    if (predicate.test(ze)) {

		String fileName = ze.getName();

		File newFile = new File(this.folder, etryFilePrefix + fileName);
		out.add(newFile);

		writeToFile(newFile, buffer, zis);
	    }
	}

	zis.closeEntry();
	zis.close();

	return out;
    }
}
