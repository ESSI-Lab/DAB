/**
 * 
 */
package eu.essi_lab.lib.utils.zip;

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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Fabrizio
 */
public class Zipper {

    private File targetDir;
    private File zipFile;
    private FileFilter filter;

    /**
     * 
     */
    public Zipper() {

	setFilter(f -> f.isFile() && !f.getName().endsWith(".zip"));
    }

    /**
     * @param targetDir
     * @param zipFile
     */
    public Zipper(File targetDir, File zipFile) {

	this();

	this.targetDir = targetDir;
	this.zipFile = zipFile;
    }

    /**
     * @return the targetDir
     */
    public File getTargetDir() {

	return targetDir;
    }

    /**
     * @param targetDir the targetDir to set
     */
    public void setTargetDir(File targetDir) {

	this.targetDir = targetDir;
    }

    /**
     * @return
     */
    public File getZipFile() {

	return zipFile;
    }

    /**
     * @param zipFile
     */
    public void setZipFile(File zipFile) {

	this.zipFile = zipFile;
    }

    /**
     * @param filter
     */
    public void setFilter(FileFilter filter) {

	this.filter = filter;
    }

    /**
     * @return
     */
    public FileFilter getPredicate() {

	return filter;
    }

    /**
     * @throws IOException
     */
    public void zip() throws IOException {

	ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8);

	File[] listFiles = targetDir.listFiles(filter);

	for (int i = 0; i < listFiles.length; i++) {

	    File file = listFiles[i];

	    FileInputStream fin = new FileInputStream(file);
	    ZipEntry zipEntry = new ZipEntry(file.getName());

	    zos.putNextEntry(zipEntry);

	    int length;
	    byte[] buffer = new byte[4096];
	    while ((length = fin.read(buffer)) > 0) {
		zos.write(buffer, 0, length);
	    }

	    zos.closeEntry();
	    fin.close();
	}

	zos.finish();
	zos.close();
    }

}
