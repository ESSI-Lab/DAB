/**
 * 
 */
package eu.essi_lab.database.ftp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class DatabaseFtpFile implements FtpFile {

    private static final Object FILE_LOCK = new Object();

    private static final int DEFAULT_MAX_LIST_SIZE = 10;

    private Database database;
    private String dir;
    private String _file;
    private User user;
    private File file;
    private String tempSTORdir;

    private static Integer listSize;
    private static Integer startIndex;

    /**
     * @param database
     * @param source
     * @param user
     */
    DatabaseFtpFile(Database database, String source, User user) {

	this(database, source, null, user, null);
    }

    /**
     * @param database
     * @param file
     * @param user
     */
    DatabaseFtpFile(Database database, String dir, String file, User user, String tempSTORdir) {

	this.database = database;
	this.dir = dir;
	this._file = file;
	this.user = user;
	this.tempSTORdir = tempSTORdir;
    }

    /**
     * @param listSize
     */
    public static void setMaxListSize(Integer listSize) {

	DatabaseFtpFile.listSize = listSize;

	GSLoggerFactory.getLogger(DatabaseFtpFile.class).info("List size -> {}", listSize);
    }

    /**
     * @param startIndex
     */
    public static void setStartIndex(Integer startIndex) {

	DatabaseFtpFile.startIndex = startIndex;

	GSLoggerFactory.getLogger(DatabaseFtpFile.class).info("Start index -> {}", startIndex);
    }

    @Override
    public String getAbsolutePath() {

	return isFile() ? "/" + dir + "_" + _file : "/" + dir;
    }

    @Override
    public String getName() {

	return isFile() ? _file.replace(dir + "_", "") : dir;
    }

    @Override
    public boolean isHidden() {

	return false;
    }

    @Override
    public boolean isDirectory() {

	return !isFile();
    }

    @Override
    public boolean isFile() {

	return _file != null && //
		!_file.equals(DatabaseFileSystemView.SELF) && //
		!_file.equals(DatabaseFileSystemView.PARENT);
    }

    @Override
    public boolean doesExist() {

	return true;
    }

    @Override
    public boolean isReadable() {

	return true;
    }

    @Override
    public boolean isWritable() {

	return true;
    }

    @Override
    public boolean isRemovable() {

	return false;
    }

    @Override
    public String getOwnerName() {

	return "user";
    }

    @Override
    public String getGroupName() {

	return "group";
    }

    @Override
    public int getLinkCount() {

	return isDirectory() ? 3 : 1;
    }

    @Override
    public long getLastModified() {

	return isFile() ? getFile().map(f -> f.lastModified()).orElse((long) 0) : 0;
    }

    @Override
    public boolean setLastModified(long time) {

	return isFile() ? getFile().map(f -> f.setLastModified(time)).orElse(false) : false;
    }

    @Override
    public long getSize() {

	return isFile() ? getFile().map(f -> f.length()).orElse((long) 0) : 0;
    }

    @Override
    public File getPhysicalFile() {

	return isFile() ? getFile().orElse(null) : null;
    }

    @Override
    public boolean mkdir() {

	return false;
    }

    @Override
    public boolean delete() {

	return false;
    }

    @Override
    public boolean move(FtpFile destination) {

	return false;
    }

    @Override
    public List<? extends FtpFile> listFiles() {

	try {

	    List<FtpFile> list = new ArrayList<>();

	    if (dir.equals(DatabaseFileSystemView.ROOT) || dir.equals(DatabaseFileSystemView.PARENT)) {

		database.getMetaFolders().//
			parallelStream().//
			map(f -> new DatabaseFtpFile(database, f.getName(), user)).//
			forEach(f -> list.add(f));

		database.getDataFolders().//
			parallelStream().//
			map(f -> new DatabaseFtpFile(database, f.getName(), user)).//
			forEach(f -> list.add(f));

		DatabaseFolder usersFolder = database.getUsersFolder();
		if (usersFolder != null) {
		    list.add(new DatabaseFtpFile(database, usersFolder.getName(), user));
		}
		
		DatabaseFolder viewFolder = database.getViewFolder(false);
		if (viewFolder != null) {
		    list.add(new DatabaseFtpFile(database, viewFolder.getName(), user));
		}

		DatabaseFolder cacheFolder = database.getCacheFolder();
		if (cacheFolder != null) {
		    list.add(new DatabaseFtpFile(database, cacheFolder.getName(), user));
		}

	    } else {

		DatabaseFolder folder = database.getFolder(this.dir);

		List<String> keys = Arrays.asList(folder.listKeys());

		int start = 0;
		int size = 0;

		if (startIndex == null) {

		    start = 0;

		} else {

		    start = startIndex > keys.size() - 1 ? 0 : startIndex;
		}

		if (listSize == null) {

		    size = Math.min(keys.size(), DEFAULT_MAX_LIST_SIZE);

		} else {

		    size = Math.min(keys.size(), listSize);
		}

		GSLoggerFactory.getLogger(getClass()).info("Listing {}/{}", start, size);

		keys = keys.subList(start, size);

		keys.forEach(key -> list.add(new DatabaseFtpFile(//
			database, //
			folder.getName(), //
			key, //
			user, //
			tempSTORdir)));
	    }

	    return list;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    /**
     * @return
     */
    public DatabaseFolder getFolder() {

	try {
	    return database.getFolder(this.dir);
	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    /**
     * @return
     */
    public String get_fileName() {

	return _file;
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {

	FileInputStream fileInputStream = new FileInputStream(new File(tempSTORdir, _file));

	FolderEntry entry = null;
	EntryType type = null;

	try {

	    if (_file.equals(HarvestingProperties.FILE_NAME)) {

		entry = FolderEntry.of(fileInputStream);
		type = EntryType.HARVESTING_PROPERTIES;

	    } else if (_file.endsWith(SourceStorageWorker.DATA_FOLDER_POSTFIX)) {

		entry = FolderEntry.of(new DataFolderIndexDocument(fileInputStream).getDocument());
		type = EntryType.DATA_FOLDER_INDEX_DOC;

	    } else {

		GSResource gsResource = GSResource.create(fileInputStream);

		entry = FolderEntry.of(gsResource.asDocument(false));
		type = EntryType.GS_RESOURCE;
	    }

	    getFolder().replace(_file, entry, type);

	} catch (Exception e) {

	    throw new IOException(e.getMessage());
	}

	RandomAccessFile raf = new RandomAccessFile(new File(tempSTORdir, _file), "rw");
	raf.setLength(offset);
	raf.seek(offset);

	return new FileOutputStream(raf.getFD()) {
	    @Override
	    public void close() throws IOException {
		super.close();
		raf.close();
	    }
	};
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {

	final RandomAccessFile raf = new RandomAccessFile(getFile().orElse(null), "r");
	raf.seek(offset);

	return new FileInputStream(raf.getFD()) {
	    @Override
	    public void close() throws IOException {
		super.close();
		raf.close();
	    }
	};
    }

    @Override
    public boolean equals(Object obj) {

	if (obj instanceof DatabaseFtpFile) {

	    return this.getAbsolutePath().equals(((DatabaseFtpFile) obj).getAbsolutePath());
	}

	return false;
    }

    @Override
    public int hashCode() {

	return this.getAbsolutePath().hashCode();
    }

    /**
     * @return
     * @throws GSException
     */
    private File download() throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Downloading file {} STARTED", getName());

	DatabaseFolder folder = database.getFolder(this.dir);
	InputStream binary = folder.getBinary(getName());

	File tempFile = File.createTempFile(getName(), null);
	tempFile.deleteOnExit();

	Files.copy(binary, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

	GSLoggerFactory.getLogger(getClass()).debug("File downloded to: {}", tempFile.getAbsolutePath());

	GSLoggerFactory.getLogger(getClass()).debug("Downloading file {} ENDED", getName());

	DatabaseFileSystemView.TEMP_FILES.add(tempFile);

	return tempFile;
    }

    /**
     * @return
     * @throws Exception
     */
    private Optional<File> getFile() {

	synchronized (FILE_LOCK) {

	    if (file == null) {

		try {

		    file = download();

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error(ex);
		}
	    }
	}

	return Optional.ofNullable(file);
    }
}
