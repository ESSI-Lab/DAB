package eu.essi_lab.accessor.geuspromice;

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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.thredds.THREDDSCrawler;
import eu.essi_lab.accessor.thredds.THREDDSDataset;
import eu.essi_lab.accessor.thredds.THREDDSPage;
import eu.essi_lab.accessor.thredds.THREDDSReference;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.StringUtils;

/**
 * Lists the site NetCDF files of a GEUS THREDDS catalog (using the THREDDS accessor crawler) and downloads them
 *
 * @author boldrini
 */
public class PROMICEClient {

    /**
     * The L3 sites catalog with daily values; the hourly and monthly catalogs are in the sibling "hour" and "month"
     * folders
     */
    public static final String DEFAULT_ENDPOINT = "https://thredds.geus.dk/thredds/catalog/aws/l3sites/netcdf/day/catalog.xml";

    public static final String HTTP_SERVICE = "HTTPServer";

    private static final String NETCDF_EXTENSION = ".nc";

    /**
     * Downloaded files are reused for one hour
     */
    private static final long CACHE_AGE = 60 * 60 * 1000l;

    private final String endpoint;

    /**
     * @param endpoint a THREDDS catalog listing PROMICE / GC-Net site NetCDF files
     */
    public PROMICEClient(String endpoint) {

	this.endpoint = endpoint == null || endpoint.isEmpty() ? DEFAULT_ENDPOINT : endpoint;
    }

    /**
     * @return the NetCDF datasets of the catalog, sorted by name
     * @throws Exception
     */
    public List<THREDDSDataset> listDatasets() throws Exception {

	THREDDSCrawler crawler = new THREDDSCrawler(endpoint);
	THREDDSPage page = crawler.crawl(new THREDDSReference((String) null));

	List<THREDDSDataset> ret = new ArrayList<>();
	for (THREDDSDataset dataset : page.getDatasets()) {
	    if (dataset.getName() != null && dataset.getName().endsWith(NETCDF_EXTENSION) && getFileURL(dataset).isPresent()) {
		ret.add(dataset);
	    }
	}

	ret.sort(Comparator.comparing(THREDDSDataset::getName));

	return ret;
    }

    /**
     * @param dataset
     * @return the HTTP download URL of the dataset file
     */
    public static Optional<String> getFileURL(THREDDSDataset dataset) {

	URL url = dataset.getServices().get(HTTP_SERVICE);
	return url == null ? Optional.empty() : Optional.of(url.toExternalForm());
    }

    /**
     * Downloads the given file, reusing a local copy downloaded less than one hour ago
     *
     * @param fileURL
     * @return
     * @throws IOException
     */
    public static File getFile(String fileURL) throws IOException {

	String name;
	try {
	    name = StringUtils.hashSHA256messageDigest(fileURL) + NETCDF_EXTENSION;
	} catch (Exception e) {
	    throw new IOException(e);
	}

	File file = new File(IOStreamUtils.getUserTempDirectory(), "promice-" + name);

	if (file.exists() && file.lastModified() > System.currentTimeMillis() - CACHE_AGE) {
	    return file;
	}

	GSLoggerFactory.getLogger(PROMICEClient.class).debug("Downloading {}", fileURL);

	Optional<InputStream> stream = new Downloader().downloadOptionalStream(fileURL);
	if (stream.isEmpty()) {
	    throw new IOException("Unable to download " + fileURL);
	}

	Path tmp = Files.createTempFile(file.toPath().getParent(), "promice", ".tmp");
	try (InputStream input = stream.get()) {
	    Files.copy(input, tmp, StandardCopyOption.REPLACE_EXISTING);
	    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	} finally {
	    Files.deleteIfExists(tmp);
	}

	return file;
    }
}
