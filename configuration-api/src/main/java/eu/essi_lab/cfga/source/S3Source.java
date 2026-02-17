package eu.essi_lab.cfga.source;

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

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.net.s3.*;
import eu.essi_lab.lib.utils.*;
import org.json.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.AbstractMap.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class S3Source implements ConfigurationSource {

    private S3TransferWrapper wrapper;
    private String bucketName;
    private String configName;

    /**
     * Used to initialize an S3 source with or without the service endpoint.<br>
     * <ol>
     * <li>If the service endpoint is omitted, the default AWS endpoint 'https://s3.amazonaws.com'
     * is used.<br>
     * E.g: s3://awsUser:awsPassword@bucket/config.json
     * </li>
     * <li>If the service endpoint is provided, as in the case of a Minio S3 service, then the config URL should be like this:<br>
     * E.g: s3://awsUser:awsPassword@http:endpoint/bucket/config.json <br>
     * E.g: s3://awsUser:awsPassword@https:endpoint/bucket/config.json
     * </li>
     * </ol>
     *
     * @param configURL
     * @return
     * @throws URISyntaxException
     */
    public static S3Source of(String configURL) throws URISyntaxException {

	String[] split = configURL.split("@")[1].split("/");

	String endpoint = split.length == 3 ? split[0] : null;

	if (endpoint != null) {

	    configURL = configURL.replace(endpoint + "/", "");

	    endpoint = endpoint.replace("https:", "https://");
	    endpoint = endpoint.replace("http:", "http://");
	}

	return of(configURL, endpoint);
    }

    /**
     * Used to initialize an S3 source with Minio S3 source
     *
     * @param configURL configuration.url -> s3://user:password@bucket/config.json
     * @param endpoint s3Endpoint -> http://my-hostname:9000
     * @return
     * @throws URISyntaxException
     * @see eu.essi_lab.messages.JVMOption#CONFIGURATION_URL
     * @see eu.essi_lab.messages.JVMOption#S3_ENDPOINT
     */
    @Deprecated
    public static S3Source of(String configURL, String endpoint) throws URISyntaxException {

	URI uri = new URI(configURL);

	String userInfo = uri.getUserInfo();
	String accessKey = userInfo.split(":")[0];
	String secretKey = userInfo.split(":")[1];

	String bucketName = uri.getHost();
	String configName = uri.getPath().substring(1);

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

	    return new ArrayList<>();
	}

	GSLoggerFactory.getLogger(getClass()).trace("Converting config to string STARTED");

	String stringConfig = IOStreamUtils.asUTF8String(binaryConfig.get().getKey());

	GSLoggerFactory.getLogger(getClass()).trace("Converting config to string ENDED");

	//
	//
	//

	// GSLoggerFactory.getLogger(getClass()).trace("Listing source STARTED");

	JSONArray jsonArray = new JSONArray(stringConfig);

	ArrayList<Setting> out = new ArrayList<>();
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

	String string = IOStreamUtils.asUTF8String(stream).trim();
	if (string.isEmpty()) {
	    out = true;
	} else {
	    JSONArray jsonArray = new JSONArray(string);
	    out = jsonArray.isEmpty();
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
    public InputStream getStream() throws Exception {

	return getBinaryConfig().get().getKey();
    }

    @Override
    public S3Source backup() throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Source backup STARTED");

	String date = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds().//
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

    /**
     * @return
     */
    public String getBucketName() {

	return bucketName;
    }

    /**
     * @return
     */
    public String getConfigName() {

	return configName;
    }

    @Override
    public LockAcquisitionResult acquireLock(String owner) {

	throw new UnsupportedOperationException();
    }

    @Override
    public boolean releaseLock() {

	throw new UnsupportedOperationException();
    }

    @Override
    public boolean orphanLockFound(long maxIdleTime) {

	throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> isLocked() {

	throw new UnsupportedOperationException();
    }

    /**
     * @return
     */
    public S3TransferWrapper getWrapper() {

	return wrapper;
    }

}
