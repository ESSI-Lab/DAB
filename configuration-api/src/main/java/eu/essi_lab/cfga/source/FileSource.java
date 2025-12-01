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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import com.google.common.base.Charsets;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class FileSource implements ConfigurationSource {

    private final File source;

    /**
     * Uses {@link File#createTempFile(String, String)} with "config" and ".json" as params.<br> Since according to the
     * {@link File#createTempFile(String, String)} the file name has a random prefix, use this constructor for test purpose when it is not
     * required to reuse a flushed source
     *
     * @throws IOException
     */
    public FileSource() throws IOException {

	this(File.createTempFile("config", ".json"));
    }

    /**
     * Creates a new file source in the <code>System.getProperty("java.io.tmpdir")</code> directory and creates a file having as prefix
     * <code>sourceName</code> and ".json" as suffix
     */
    public FileSource(String configFileName) {

	this(new File(//
		System.getProperty("java.io.tmpdir") + File.separator + configFileName + ".json"));
    }

    /**
     * @param source
     */
    public FileSource(File source) {

	this.source = source;

	GSLoggerFactory.getLogger(getClass()).info("Source path: {}", source.getAbsolutePath());
    }

    /**
     * @param source
     */
    public FileSource(URI uri) {

	this(new File(uri));
    }

    /**
     * @return
     */
    public File getSource() {

	return source;
    }

    /**
     * Creates a clone of the given <code>configuration</code> having a local File System source located in the given
     * <code>configFolder</code>
     *
     * @param configuration
     * @param configFolder
     * @return
     * @throws Exception
     */
    public static Configuration switchSource(Configuration configuration, File configFolder) throws Exception {

	File localFile = new File(configFolder, "gs-configuration.json");
	String configString = configuration.toString();

	FileOutputStream fileOutputStream = new FileOutputStream(localFile);

	fileOutputStream.write(configString.getBytes(Charsets.UTF_8));

	fileOutputStream.flush();
	fileOutputStream.close();

	FileSource fileSource = new FileSource(localFile);
	return new Configuration(fileSource);
    }

    /**
     * Creates a clone of the given <code>configuration</code> having a local File System source located in the user temp directory
     *
     * @param configuration
     * @return
     * @throws Exception
     */
    public static Configuration switchSource(Configuration configuration) throws Exception {

	return switchSource(configuration, File.createTempFile("gs-configuration", ".json"));
    }

    @Override
    public List<Setting> list() throws Exception {

	if (isEmptyOrMissing()) {

	    return new ArrayList<>();
	}

	GSLoggerFactory.getLogger(getClass()).info("Listing source {} STARTED", source);

	JSONArray jsonArray = new JSONArray(getJSONSource());

	ArrayList<Setting> out = new ArrayList<>();
	jsonArray.forEach(obj -> out.add(new Setting((JSONObject) obj)));

	GSLoggerFactory.getLogger(getClass()).info("Source has #{} settings", jsonArray.length());

	GSLoggerFactory.getLogger(getClass()).info("Listing source {} ENDED", source);

	return out;
    }

    @Override
    public void flush(List<Setting> settings) throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Flushing source {} STARTED", source);

	JSONArray array = new JSONArray();
	settings.forEach(item -> array.put(item.getObject()));

	flush(source, array.toString(3));

	GSLoggerFactory.getLogger(getClass()).info("Flushing source {} ENDED", source);
    }

    @Override
    public boolean isEmptyOrMissing() throws Exception {

	if (!source.exists()) {

	    GSLoggerFactory.getLogger(getClass()).info("Source {} is missing", source);

	    return true;
	}

	String json = getJSONSource();

	if (json.isEmpty() || new JSONArray(json).isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Source {} is empty", source);

	    return true;
	}

	return false;
    }

    @Override
    public LockAcquisitionResult acquireLock(String owner) throws Exception {

	Optional<String> locked = isLocked();

	if (locked.isPresent()) {

	    // locked by this owner
	    if (locked.get().equals(owner)) {

		File lockFile = createLockFile(owner);

		flush(lockFile);

		lockFile.setLastModified(System.currentTimeMillis());

		return LockAcquisitionResult.OWNED;
	    }

	    // locked by another owner
	    return LockAcquisitionResult.REJECTED;
	}

	File lockFile = createLockFile(owner);

	flush(lockFile);

	lockFile.setLastModified(System.currentTimeMillis());

	return LockAcquisitionResult.SUCCEEDED;
    }

    @Override
    public boolean releaseLock() {

	Optional<File> optional = findLockFile();

	return optional.map(File::delete).orElse(false);

    }

    @Override
    public Optional<String> isLocked() {

	Optional<File> optional = findLockFile();

	return optional.map(this::getExtension);

    }

    @Override
    public boolean orphanLockFound(long maxIdleTime) {

	Optional<File> optional = findLockFile();

	if (optional.isPresent()) {

	    File file = optional.get();

	    long lastModified = file.lastModified();

	    long idleTime = System.currentTimeMillis() - lastModified;

	    return idleTime > maxIdleTime;
	}

	return false;
    }

    @Override
    public InputStream getStream() throws Exception {

	return new FileInputStream(source);
    }

    /**
     * @return
     */
    private Optional<File> findLockFile() {

	File[] listFiles = source.getParentFile().listFiles();
	if (listFiles != null) {

	    //
	    return Arrays.stream(listFiles).//
		    filter(f -> f.getName().contains("lock")).//
		    findFirst();
	}

	return Optional.empty();
    }

    /**
     * @param file
     * @return
     */
    private String getExtension(File file) {

	return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    /**
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String getJSONSource() throws FileNotFoundException, IOException {

	return IOStreamUtils.asUTF8String(new FileInputStream(source));
    }

    /**
     * @param file
     * @throws IOException
     */
    private void flush(File file) throws IOException {

	flush(file, null);
    }

    /**
     * @param file
     * @throws IOException
     */
    private void flush(File file, String content) throws IOException {

	FileOutputStream fileOutputStream = new FileOutputStream(file);

	if (content != null) {

	    fileOutputStream.write(content.getBytes(Charsets.UTF_8));
	}

	fileOutputStream.flush();
	fileOutputStream.close();
    }

    /**
     * @return
     */
    private File createLockFile(String owner) {

	File parent = source.getParentFile();
	return new File(parent.getAbsolutePath() + File.separator + "lock." + owner);
    }

    @Override
    public FileSource backup() throws IOException {

	String extension = FilenameUtils.getExtension(source.getName());

	String name = FilenameUtils.removeExtension(source.getName());

	String date = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds().//
		replace("-", "_").//
		replace(":", "_").//
		replace(".", "");

	String backupName = name + "_" + date + "." + extension + ".backup";

	File backupFile = new File(source.getParentFile().getAbsolutePath() + File.separator + backupName);

	Files.copy(source, backupFile);

	return new FileSource(backupFile);
    }

    @Override
    public String getLocation() {

	return source.getAbsolutePath();
    }
}
