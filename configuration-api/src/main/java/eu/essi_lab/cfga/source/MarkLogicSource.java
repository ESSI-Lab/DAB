package eu.essi_lab.cfga.source;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.exceptions.XccConfigException;

import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class MarkLogicSource implements ConfigurationSource {

    private MarkLogicWrapper wrapper;
    private String configUri;
    private String lockUri;
    private String configFolder;
    private String configName;

    /**
     * xdbc://user:password@hostname:8000,8004/dbName/defaultConf/
     * 
     * @param xdbc
     * @param configName
     * @throws XccConfigException
     * @throws URISyntaxException
     */
    public MarkLogicSource(String xdbc, String configName) throws XccConfigException, URISyntaxException {

	StorageInfo uri = MarkLogicWrapper.fromXDBC(xdbc);

	this.wrapper = new MarkLogicWrapper(uri);

	init(uri, configName);
    }

    /**
     * @param uri
     * @throws XccConfigException
     * @throws URISyntaxException
     */
    public MarkLogicSource(StorageInfo uri, String configName) throws XccConfigException, URISyntaxException {

	this.wrapper = new MarkLogicWrapper(uri.getUri(), uri.getUser(), uri.getPassword(), uri.getName());

	init(uri, configName);
    }

    /**
     * 
     */
    private MarkLogicSource() {

    }

    /**
     * @param uri
     * @param configName
     */
    private void init(StorageInfo uri, String configName) {

	this.configName = configName;

	this.configFolder = uri.getIdentifier();

	this.configUri = "/" + configFolder + "/" + configName + ".json";

	this.lockUri = "/" + configFolder + "/lock.json";
    }

    /**
     * @return the wrapper
     */
    public MarkLogicWrapper getWrapper() {

	return wrapper;
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

	boolean replaced = wrapper.replaceBinary(configUri, IOStreamUtils.asStream(array.toString(3)));
	GSLoggerFactory.getLogger(getClass()).trace("Source replaced: " + replaced);

	boolean stored = false;
	if (!replaced) {

	    stored = wrapper.storeBinary(configUri, IOStreamUtils.asStream(array.toString(3)));
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

		boolean replaced = wrapper.replaceBinary(lockUri, inputStream);

		if (!replaced) {

		    throw new IOException("Unable to replace lock file to update the timestamp");
		}

		return LockAcquisitionResult.OWNED;
	    }

	    return LockAcquisitionResult.REJECTED;
	}

	InputStream lockFile = createLockFile(owner);

	boolean stored = wrapper.storeBinary(lockUri, lockFile);

	if (!stored) {

	    throw new IOException("Unable to store lock file");
	}

	return LockAcquisitionResult.SUCCEEDED;
    }

    @Override
    public boolean releaseLock() throws Exception {

	if (isLocked().isPresent()) {

	    return wrapper.remove(lockUri);
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

	ResultItem next = wrapper.submit(getConfigQuery()).next();

	if (next != null) {

	    return next.asInputStream();
	}

	return null;
    }

    /**
     * @return
     */
    private String getConfigQuery() {

	String query = "xquery version \"1.0-ml\"; \n";

	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\";  \n";

	// query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at
	// \"/gs-modules/functions-module.xqy\"; \n";

	query += "fn:document('" + this.configUri + "')\n";

	return query;
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

	InputStream binary = wrapper.getBinary(lockUri);

	if (binary != null) {

	    String string = IOStreamUtils.asUTF8String(binary);
	    return Optional.of(new JSONObject(string));
	}

	return Optional.empty();
    }

    @Override
    public MarkLogicSource backup() throws Exception {

	String date = ISO8601DateTimeUtils.getISO8601DateTime().//
		replace("-", "_").//
		replace(":", "_").//
		replace(".", "");

	InputStream clone = new ClonableInputStream(getBinaryConfig()).clone();

	String backupName = configName + "_" + date;

	String backupUri = "/" + this.configFolder + "/" + backupName + ".json.backup";

	boolean stored = wrapper.storeBinary(backupUri, clone);

	GSLoggerFactory.getLogger(getClass()).trace("Source stored: " + stored);

	if (!stored) {
	    throw new RuntimeException("Source backup not stored");
	}

	MarkLogicSource backupSource = new MarkLogicSource();

	backupSource.wrapper = this.wrapper;
	backupSource.configName = backupName;
	backupSource.configUri = backupUri;
	backupSource.configFolder = this.configFolder;
	backupSource.lockUri = this.lockUri;

	return backupSource;
    }

    @Override
    public String getLocation() {

	return this.configUri;
    }
}
