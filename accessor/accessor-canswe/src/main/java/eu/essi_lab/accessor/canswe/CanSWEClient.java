package eu.essi_lab.accessor.canswe;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.StringUtils;

/**
 * Resolves and downloads the CanSWE NetCDF file published on Zenodo. The endpoint can be either the Zenodo record page
 * (e.g. https://zenodo.org/records/10835278, the NetCDF file of the record is found with the Zenodo API) or the URL of
 * the NetCDF file itself.
 *
 * @author boldrini
 */
public class CanSWEClient {

    /**
     * Zenodo record of CanSWE v6 (1928-2023)
     */
    public static final String DEFAULT_ENDPOINT = "https://zenodo.org/records/10835278";

    private static final String NETCDF_EXTENSION = ".nc";

    private static final Pattern RECORD_PATTERN = Pattern.compile("^https?://(?:www\\.)?zenodo\\.org/(?:records|record|api/records)/(\\d+)/?(?:[?#].*)?$");

    /**
     * Zenodo record versions are immutable: the downloaded file is reused for one week
     */
    private static final long CACHE_AGE = 7 * 24 * 60 * 60 * 1000l;

    private final String endpoint;

    /**
     * @param endpoint the Zenodo record page or the NetCDF file URL
     */
    public CanSWEClient(String endpoint) {

	this.endpoint = endpoint == null || endpoint.trim().isEmpty() ? DEFAULT_ENDPOINT : endpoint.trim();
    }

    /**
     * @return the URL of the NetCDF file
     * @throws IOException
     */
    public String getFileURL() throws IOException {

	Matcher matcher = RECORD_PATTERN.matcher(endpoint);
	if (!matcher.matches()) {
	    return endpoint;
	}

	String api = "https://zenodo.org/api/records/" + matcher.group(1);

	Optional<String> response = new Downloader().downloadOptionalString(api);
	if (response.isEmpty()) {
	    throw new IOException("Unable to read Zenodo record " + api);
	}

	JSONArray files = new JSONObject(response.get()).optJSONArray("files");
	if (files != null) {
	    for (int i = 0; i < files.length(); i++) {
		JSONObject file = files.getJSONObject(i);
		String key = file.optString("key", "");
		if (key.toLowerCase().endsWith(NETCDF_EXTENSION)) {
		    // the stable download URL of the record file
		    return "https://zenodo.org/records/" + matcher.group(1) + "/files/" + key + "?download=1";
		}
	    }
	}

	throw new IOException("No NetCDF file found in Zenodo record " + api);
    }

    /**
     * @return the dataset of the NetCDF file
     * @throws IOException
     */
    public CanSWEDataset getDataset() throws IOException {

	return CanSWEDataset.get(getFile(getFileURL()));
    }

    /**
     * Downloads the given file, reusing a local copy downloaded less than one week ago
     *
     * @param fileURL
     * @return
     * @throws IOException
     */
    public static synchronized File getFile(String fileURL) throws IOException {

	String name;
	try {
	    name = StringUtils.hashSHA256messageDigest(fileURL) + NETCDF_EXTENSION;
	} catch (Exception e) {
	    throw new IOException(e);
	}

	File file = new File(IOStreamUtils.getUserTempDirectory(), "canswe-" + name);

	if (file.exists() && file.length() > 0 && file.lastModified() > System.currentTimeMillis() - CACHE_AGE) {
	    return file;
	}

	GSLoggerFactory.getLogger(CanSWEClient.class).info("Downloading {}", fileURL);

	Optional<InputStream> stream = new Downloader().downloadOptionalStream(fileURL);
	if (stream.isEmpty()) {
	    if (file.exists() && file.length() > 0) {
		GSLoggerFactory.getLogger(CanSWEClient.class).warn("Unable to download {}, using the previous copy", fileURL);
		return file;
	    }
	    throw new IOException("Unable to download " + fileURL);
	}

	Path tmp = Files.createTempFile(file.toPath().getParent(), "canswe", ".tmp");
	try (InputStream input = stream.get()) {
	    Files.copy(input, tmp, StandardCopyOption.REPLACE_EXISTING);
	    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	} finally {
	    Files.deleteIfExists(tmp);
	}

	return file;
    }

    /**
     * @param endpoint
     * @return true if the endpoint is a CanSWE Zenodo record or file
     */
    public static boolean isCanSWE(String endpoint) {

	if (endpoint == null) {
	    return false;
	}

	String lower = endpoint.toLowerCase();
	return lower.contains("zenodo.org") && (lower.contains("canswe") || lower.contains("10835278"));
    }
}
