package eu.essi_lab.cfga.source;

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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class S3Source implements ConfigurationSource {

    private S3TransferWrapper wrapper;
    private String bucketName;

    public String getBucketName() {
	return bucketName;
    }

    private String configName;

    public String getConfigName() {
	return configName;
    }

    /**
     * Used to initialize an AWS S3 source
     * 
     * @param configURL s3://awsUser:awsPassword@bucket/config.json
     * @return
     * @throws URISyntaxException
     */
    public static S3Source of(String configURL) throws URISyntaxException {
	return of(configURL, null);
    }

    /**
     * Used to initialize a Minio S3 source
     * 
     * @param configURL s3://user:password@bucket/config.json
     * @param endpoint http://my-hostname:9000
     * @return
     * @throws URISyntaxException
     */
    public static S3Source of(String configURL, String endpoint) throws URISyntaxException {

	URI uri = new URI(configURL);

	String userInfo = uri.getUserInfo();
	String accessKey = userInfo.split(":")[0];
	String secretKey = userInfo.split(":")[1];

	String bucketName = uri.getHost();
	String configName = uri.getPath().substring(1, uri.getPath().length());

	S3TransferWrapper wrapper = new S3TransferWrapper();
	wrapper.setAccessKey(accessKey);
	wrapper.setSecretKey(secretKey);
	wrapper.setEndpoint(endpoint);

	return new S3Source(wrapper, bucketName, configName);
    }

    /**
     * s3://awsaccesskey:awssecretkey@bucket/config.json
     * 
     * @param url
     * @return
     */
    public static boolean check(String url) {

	return url.startsWith("s3://");
    }

    /**
     * @param wrapper
     * @param bucketName
     * @param configName must include the extension (".json")
     */
    public S3Source(S3TransferWrapper wrapper, String bucketName, String configName) {

	this.wrapper = wrapper;
	this.bucketName = bucketName;
	this.configName = configName;
    }

    @Override
    public List<Setting> list() throws Exception {

	Optional<SimpleEntry<InputStream, File>> binaryConfig = getBinaryConfig();

	if (isEmptyOrMissing(binaryConfig)) {

	    binaryConfig.get().getKey().close();
	    binaryConfig.get().getValue().delete();

	    return new ArrayList<Setting>();
	}

	GSLoggerFactory.getLogger(getClass()).trace("Converting config to string STARTED");

	String stringConfig = IOStreamUtils.asUTF8String(binaryConfig.get().getKey());

	GSLoggerFactory.getLogger(getClass()).trace("Converting config to string ENDED");

	//
	//
	//

	// GSLoggerFactory.getLogger(getClass()).trace("Listing source STARTED");

	JSONArray jsonArray = new JSONArray(stringConfig);

	ArrayList<Setting> out = new ArrayList<Setting>();
	jsonArray.forEach(obj -> out.add(new Setting((JSONObject) obj)));

	//
	//
	//

	binaryConfig.get().getKey().close();
	binaryConfig.get().getValue().delete();

	return out;
    }

    @Override
    public void flush(List<Setting> settings) throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Flushing source STARTED");

	JSONArray configArray = new JSONArray();
	settings.forEach(item -> configArray.put(item.getObject()));

	File tempFile = uploadConfig(configArray, wrapper, bucketName, configName);

	wrapper.uploadFile(tempFile.getAbsolutePath(), bucketName, configName);

	tempFile.delete();

	GSLoggerFactory.getLogger(getClass()).trace("Flushing source ENDED");
    }

    /**
     * @param binaryConfig
     * @return
     * @throws Exception
     */
    private boolean isEmptyOrMissing(Optional<SimpleEntry<InputStream, File>> binaryConfig) throws Exception {

	if (binaryConfig.isEmpty()) {

	    return true;
	}

	InputStream stream = Files.newInputStream(//
		binaryConfig.get().getValue().toPath(), //
		StandardOpenOption.READ);

	boolean out = false;

	if (binaryConfig != null) {

	    String string = IOStreamUtils.asUTF8String(stream).trim();
	    if (string.isEmpty()) {
		out = true;
	    } else {
		JSONArray jsonArray = new JSONArray(string);
		out = jsonArray.length() == 0;
	    }
	}

	stream.close();
	binaryConfig.get().getValue().delete();

	return out;
    }

    @Override
    public boolean isEmptyOrMissing() throws Exception {

	Optional<SimpleEntry<InputStream, File>> binaryConfig = getBinaryConfig();

	return isEmptyOrMissing(binaryConfig);
    }

    /**
     * @param array
     * @param manager
     */
    public static File uploadConfig(JSONArray array, S3TransferWrapper manager, String bucketName, String configName) throws Exception {

	File tempFile = File.createTempFile("config", ".json");

	InputStream configStream = IOStreamUtils.asStream(array.toString());

	Files.copy(configStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

	manager.uploadFile(tempFile.getAbsolutePath(), bucketName, configName);

	configStream.close();

	return tempFile;
    }

    /**
     * @return
     * @throws Exception
     */
    private Optional<SimpleEntry<InputStream, File>> getBinaryConfig() throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Get binary config STARTED");

	File tempFile = File.createTempFile("config", ".json");

	try {

	    wrapper.download(bucketName, configName, tempFile);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    return Optional.empty();
	}

	InputStream binaryConfig = Files.newInputStream(tempFile.toPath(), StandardOpenOption.READ);

	GSLoggerFactory.getLogger(getClass()).trace("Get binary config ENDED");

	return Optional.of(new SimpleEntry<>(binaryConfig, tempFile));
    }

    @Override
    public S3Source backup() throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Source backup STARTED");

	String date = ISO8601DateTimeUtils.getISO8601DateTime().//
		replace("-", "_").//
		replace(":", "_").//
		replace(".", "");

	Optional<SimpleEntry<InputStream, File>> clone = getBinaryConfig();

	String backupName = configName + "_" + date;

	String backupCompleteName = backupName + ".json.backup";

	wrapper.uploadFile(clone.get().getValue().getAbsolutePath(), this.bucketName, backupCompleteName);

	S3Source backupSource = new S3Source(this.wrapper, this.bucketName, backupCompleteName);

	backupSource.wrapper = this.wrapper;
	backupSource.configName = backupCompleteName;
	backupSource.bucketName = this.bucketName;

	GSLoggerFactory.getLogger(getClass()).trace("Source backup ENDED");

	return backupSource;
    }

    @Override
    public String getLocation() {

	return bucketName + "/" + configName;
    }

    @Override
    public LockAcquisitionResult acquireLock(String owner) throws Exception {

	throw new UnsupportedOperationException();
    }

    @Override
    public boolean releaseLock() throws Exception {

	throw new UnsupportedOperationException();
    }

    @Override
    public boolean orphanLockFound(long maxIdleTime) throws Exception {

	throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> isLocked() throws Exception {

	throw new UnsupportedOperationException();
    }

    /**
     * @return
     */
    public S3TransferWrapper getWrapper() {

	return wrapper;
    }

}
