/**
 * 
 */
package eu.essi_lab.api.database.cfg;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class DatabaseSource implements ConfigurationSource {

    private String lockFileName;
    private String configName;
    private String completeConfigName;
    protected DatabaseFolder folder;

    /**
     * @param storageInfo
     * @param folder
     * @param configName
     * @throws GSException
     */
    private DatabaseSource(StorageInfo storageInfo, String folder, String configName) throws GSException {

	Database database = DatabaseFactory.get(storageInfo);

	this.folder = database.getFolder(folder, true).get();

	init(storageInfo, configName);
    }

    /**
     * E.g: "xdbc://user:password@hostname:8000,8004/dbName/folder/"
     * E.g.: "osm://awsaccesskey:awssecretkey@https:productionhost/prod/prodConfig"<br>
     * E.g.: "oss://awsaccesskey:awssecretkey@https:preproductionhost/preprod/preProdConfig"<br>
     * E.g.: "osl://awsaccesskey:awssecretkey@http:localhost:9200/test/testConfig"<br>
     * 
     * @see Database#build(String)
     * @see Database#check(String)
     * @param impl
     * @param url
     * @return
     * @throws GSException
     * @throws URISyntaxException
     */
    @SuppressWarnings("incomplete-switch")
    public static DatabaseSource of(String url) throws GSException, URISyntaxException {

	StorageInfo info = DatabaseSourceUrl.build(url);

	DatabaseImpl impl = DatabaseSourceUrl.detectImpl(url);

	switch (impl) {
	case MARK_LOGIC:

	    return new DatabaseSource(info, info.getIdentifier(), "gs-configuration");

	case OPENSEARCH:

	    return new DatabaseSource(info, Database.CONFIGURATION_FOLDER, info.getName());
	}

	return null;
    }

    /**
     * @param impl
     * @param configName
     * @return
     * @throws GSException
     */
    @SuppressWarnings("incomplete-switch")
    public static DatabaseSource of(DatabaseImpl impl, StorageInfo storageInfo, String configName) throws GSException {

	switch (impl) {
	case MARK_LOGIC:

	    return new DatabaseSource(storageInfo, storageInfo.getIdentifier(), configName);

	case OPENSEARCH:

	    return new DatabaseSource(storageInfo, Database.CONFIGURATION_FOLDER, configName);
	}

	return null;
    }

    /**
     * 
     */
    protected DatabaseSource() {

    }

    /**
     * @param storageInfo
     * @param configName
     */
    protected void init(StorageInfo storageInfo, String configName) {

	this.configName = configName;

	this.completeConfigName = configName + ".json";

	this.lockFileName = "lock.json";
    }

    @Override
    public List<Setting> list() throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Get binary config STARTED");

	InputStream binaryConfig = getBinaryConfig();

	GSLoggerFactory.getLogger(getClass()).trace("Get binary config ENDED");

	if (binaryConfig == null) {

	    return new ArrayList<Setting>();
	}

	GSLoggerFactory.getLogger(getClass()).trace("Converting config to string STARTED");

	String stringConfig = IOStreamUtils.asUTF8String(binaryConfig);

	GSLoggerFactory.getLogger(getClass()).trace("Converting config to string ENDED");

	//
	//
	//

	// GSLoggerFactory.getLogger(getClass()).trace("Listing source STARTED");

	JSONArray jsonArray = new JSONArray(stringConfig);

	ArrayList<Setting> out = new ArrayList<Setting>();
	jsonArray.forEach(obj -> out.add(new Setting((JSONObject) obj)));

	// GSLoggerFactory.getLogger(getClass()).trace("Source has #{} settings", jsonArray.length());

	// GSLoggerFactory.getLogger(getClass()).trace("Listing source ENDED");

	//
	//
	//

	return out;
    }

    @Override
    public void flush(List<Setting> settings) throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Flushing source STARTED");

	JSONArray array = new JSONArray();
	settings.forEach(item -> array.put(item.getObject()));

	boolean replaced = folder.replace(//
		this.completeConfigName, //
		FolderEntry.of(IOStreamUtils.asStream(array.toString(3))), //
		EntryType.CONFIGURATION);

	GSLoggerFactory.getLogger(getClass()).trace("Source replaced: " + replaced);

	boolean stored = false;
	if (!replaced) {

	    stored = folder.store(//
		    this.completeConfigName, //
		    FolderEntry.of(IOStreamUtils.asStream(array.toString(3))), //
		    EntryType.CONFIGURATION);

	    GSLoggerFactory.getLogger(getClass()).trace("Source stored: " + stored);
	}

	if (!replaced && !stored) {
	    throw new RuntimeException("Source not replaced nor stored");
	}

	GSLoggerFactory.getLogger(getClass()).trace("Flushing source ENDED");
    }

    @Override
    public boolean isEmptyOrMissing() throws Exception {

	InputStream binaryConfig = getBinaryConfig();
	if (binaryConfig != null) {

	    String string = IOStreamUtils.asUTF8String(binaryConfig);
	    JSONArray jsonArray = new JSONArray(string);
	    return jsonArray.length() == 0;
	}

	return true;
    }

    @Override
    public LockAcquisitionResult acquireLock(String owner) throws Exception {

	Optional<JSONObject> optionalLockFile = getLockFile();

	Optional<String> locked = isLocked(optionalLockFile);

	if (locked.isPresent()) {

	    if (locked.get().equals(owner)) {

		if (!optionalLockFile.isPresent()) {

		    throw new IOException("Unable to retrieve lock file to update the timestamp");
		}

		JSONObject lockFile = optionalLockFile.get();

		InputStream inputStream = updateLockTimestamp(lockFile);

		boolean replaced = folder.replace(//
			lockFileName, //
			FolderEntry.of(inputStream), //
			EntryType.CONFIGURATION_LOCK);

		if (!replaced) {

		    throw new IOException("Unable to replace lock file to update the timestamp");
		}

		return LockAcquisitionResult.OWNED;
	    }

	    return LockAcquisitionResult.REJECTED;
	}

	InputStream lockFile = createLockFile(owner);

	boolean stored = folder.store(//
		lockFileName, //
		FolderEntry.of(lockFile), //
		EntryType.CONFIGURATION_LOCK);

	if (!stored) {

	    throw new IOException("Unable to store lock file");
	}

	return LockAcquisitionResult.SUCCEEDED;
    }

    @Override
    public boolean releaseLock() throws Exception {

	if (isLocked().isPresent()) {

	    return folder.remove(lockFileName);
	}

	return false;
    }

    @Override
    public boolean orphanLockFound(long maxIdleTime) throws Exception {

	Optional<JSONObject> optionalLockFile = getLockFile();

	if (optionalLockFile.isPresent()) {

	    JSONObject lockFile = optionalLockFile.get();

	    long lastModified = readLockTimestamp(lockFile);

	    long idleTime = System.currentTimeMillis() - lastModified;

	    return idleTime > maxIdleTime;
	}

	return false;
    }

    @Override
    public Optional<String> isLocked() throws Exception {

	Optional<JSONObject> optionalLockFile = getLockFile();

	return isLocked(optionalLockFile);
    }

    @Override
    public InputStream getStream() throws Exception {

	return getBinaryConfig();
    }

    /**
     * @param optionalLockFile
     * @return
     * @throws Exception
     */
    private Optional<String> isLocked(Optional<JSONObject> optionalLockFile) throws Exception {

	if (optionalLockFile.isPresent()) {

	    JSONObject lockFile = optionalLockFile.get();
	    return Optional.of(readLockOwner(lockFile));
	}

	return Optional.empty();
    }

    /**
     * @return
     * @throws Exception
     */
    private InputStream getBinaryConfig() throws Exception {

	return folder.getBinary(completeConfigName);
    }

    /**
     * @param lockFile
     * @return
     */
    private String readLockOwner(JSONObject lockFile) {

	return lockFile.getString("owner");
    }

    /**
     * @param lockFile
     * @return
     */
    private long readLockTimestamp(JSONObject lockFile) {

	return lockFile.getLong("timeStamp");
    }

    /**
     * @param lockFile
     */
    private InputStream updateLockTimestamp(JSONObject lockFile) {

	lockFile.put("timeStamp", System.currentTimeMillis());

	return IOStreamUtils.asStream(lockFile.toString(3));
    }

    /**
     * @param owner
     * @return
     */
    private InputStream createLockFile(String owner) {

	JSONObject lockFile = new JSONObject();
	lockFile.put("owner", owner);
	lockFile.put("timeStamp", System.currentTimeMillis());

	return IOStreamUtils.asStream(lockFile.toString(3));
    }

    /**
     * @return
     * @throws Exception
     */
    private Optional<JSONObject> getLockFile() throws Exception {

	InputStream binary = folder.getBinary(lockFileName);

	if (binary != null) {

	    String string = IOStreamUtils.asUTF8String(binary);
	    return Optional.of(new JSONObject(string));
	}

	return Optional.empty();
    }

    @Override
    public DatabaseSource backup() throws Exception {

	String date = ISO8601DateTimeUtils.getISO8601DateTime().//
		replace("-", "_").//
		replace(":", "_").//
		replace(".", "");

	InputStream clone = getBinaryConfig();

	String backupName = configName + "_" + date;

	String backupCompleteName = backupName + ".json.backup";

	boolean stored = folder.store(backupCompleteName, FolderEntry.of(clone), EntryType.CONFIGURATION);

	GSLoggerFactory.getLogger(getClass()).trace("Source stored: " + stored);

	if (!stored) {
	    throw new RuntimeException("Source backup not stored");
	}

	DatabaseSource backupSource = new DatabaseSource();

	backupSource.configName = backupName;
	backupSource.completeConfigName = backupCompleteName;
	backupSource.lockFileName = this.lockFileName;
	backupSource.folder = this.folder;

	return backupSource;
    }

    @Override
    public String getLocation() {

	return folder.getName() + "\\" + this.completeConfigName;
    }
}
