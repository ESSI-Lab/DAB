package eu.essi_lab.api.configuration.storage.fs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class LocalFileSystemConfigurationStorage implements IGSConfigurationStorage {

    public static final String ERR_ID_IO_EXCEPTION = "ERR_ID_FILE_NOT_FOUND";
    private static final String ERR_ID_SERIALIZATION_ERROR = "ERR_ID_SERIALIZATION_ERROR";
    private static final String ERR_ID_CANT_WRITE_FILE = "ERR_ID_CANT_WRITE_FILE";
    private String uriString;
    private StorageUri storageUri;

    @Override
    public void transactionUpdate(GSConfiguration conf) throws GSException {

	String cstring = conf.serialize();

	File file = new File(URI.create(uriString));

	try (FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel()) {

	    FileLock lock = fileChannel.lock();
	    fileChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap(cstring)));

	    fileChannel.force(false);

	    lock.release();
	    fileChannel.close();

	} catch (Throwable e) {

	    GSException ex = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setCause(e);

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_CANT_WRITE_FILE);

	    ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    ei.setErrorDescription("Can not serialize configuration object.");

	    ex.addInfo(ei);

	    throw ex;

	}
    }

    @Override
    public GSConfiguration read() throws GSException {

	try (FileInputStream fis = new FileInputStream(new File(URI.create(uriString)))) {

	    GSConfiguration deserialized = new Deserializer().deserialize(fis, GSConfiguration.class);

	    fis.close();

	    return deserialized;

	} catch (IOException e) {

	    GSException ex = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setCause(e);

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_IO_EXCEPTION);

	    ei.setErrorType(ErrorInfo.ERRORTYPE_SERVICE);
	    ei.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    ei.setUserErrorDescription("Can not read configuration file.");

	    ex.addInfo(ei);

	    throw ex;
	}
    }

    @Override
    public boolean supports(StorageUri url) {

	String path = url.getUri();

	return path.toLowerCase().startsWith("file://");
    }

    @Override
    public boolean validate(StorageUri url) {

	return execValidation(new File(URI.create(url.getUri())));
    }

    public boolean execValidation(File f) {

	return !f.isDirectory() && f.canWrite() && f.canRead();
    }

    @Override
    public void setStorageUri(StorageUri storageUri) {

	this.storageUri = storageUri;
	uriString = storageUri.getUri();
    }

    @Override
    public StorageUri getStorageUri() {

	return this.storageUri;
    }
}
