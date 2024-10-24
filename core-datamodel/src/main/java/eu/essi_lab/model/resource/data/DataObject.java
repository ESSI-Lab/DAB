package eu.essi_lab.model.resource.data;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class DataObject {

    // private InputStream stream;
    // private ClonableInputStream clonableStream;
    private File file;
    private GSResource resource;

    public File getFile() {
	return file;
    }

    public void setFile(File file) {
	this.file = file;
    }

    /**
     * @return
     */
    public Optional<GSResource> getResource() {

	return Optional.ofNullable(resource);
    }

    /**
     * @param resource
     */
    public void setResource(GSResource resource) {

	this.resource = resource;
    }

    public File setFileFromStream(InputStream stream, String fileHint) throws IOException {
	if (stream == null) {
	    return null;
	}
	String nameHint = "DataObject";
	String extensionHint = ".bin";
	if (fileHint != null) {
	    if (fileHint.contains(".")) {
		nameHint = fileHint.substring(0, fileHint.indexOf("."));
		extensionHint = fileHint.substring(fileHint.lastIndexOf("."));
	    } else {
		nameHint = fileHint;
	    }

	}
	File tmpFile = IOStreamUtils.tempFilefromStream(stream, nameHint, extensionHint);
	tmpFile.deleteOnExit();
	if (stream != null)
	    stream.close();
	this.file = tmpFile;
	return tmpFile;
    }

    private DataDescriptor dataDescriptor;

    public DataObject() {

    }

    /**
     * @return
     */
    public DataDescriptor getDataDescriptor() {
	return dataDescriptor;
    }

    /**
     * @param dataDescriptor
     */
    public void setDataDescriptor(DataDescriptor dataDescriptor) {
	this.dataDescriptor = dataDescriptor;
    }

    // /**
    // * @return
    // */
    // public InputStream getStream() {
    //
    // return clonableStream.clone();
    // }

    // /**
    // * @param stream
    // */
    // public void setStream(InputStream stream) {
    //
    // this.stream = stream;
    // try {
    // clonableStream = new ClonableInputStream(this.stream);
    // } catch (IOException e) {
    // e.printStackTrace();
    // GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
    // }
    // }

}
