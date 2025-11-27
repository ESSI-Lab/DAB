package eu.essi_lab.gssrv.servlet.wmscache;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class WMSCacheStorageOnDisk implements WMSCacheStorage {

    private final Path rootDir;
    private String cacheSubFolder;
    private int maxSize = 1000;

    public String getCacheSubFolder() {
	return cacheSubFolder;
    }

    public Path getRootDir() {
	return rootDir;
    }

    /**
     * Create a cache storage in the system temporary folder
     * under a specific subfolder name (e.g., "wms-cache").
     */
    public WMSCacheStorageOnDisk(String cacheSubFolder) {
	this.cacheSubFolder = cacheSubFolder;
	this.rootDir = Paths.get(System.getProperty("java.io.tmpdir"), cacheSubFolder);
	try {
	    Files.createDirectories(rootDir);
	} catch (IOException e) {
	    throw new RuntimeException("Failed to create cache folder", e);
	}
    }

    private Path getCacheFilePath(String view, String layer, String hash) {
	return rootDir.resolve(Paths.get(view, layer, hash + ".cache"));
    }

    private Path getTimestampFilePath(String view, String layer, String hash) {
	return rootDir.resolve(Paths.get(view, layer, hash + ".timestamp"));
    }

    @Override
    public Response getCachedResponse(String view, String layer, String hash) {
	Path filePath = getCacheFilePath(view, layer, hash);
	File ret = Files.exists(filePath) ? filePath.toFile() : null;
	if (ret != null && ret.exists()) {
	    try (InputStream is = new FileInputStream(ret)) {
		Response.ResponseBuilder builder = Response.ok(is);
		builder.type(MediaType.valueOf("image/png")); // set MIME type
		builder.header("Content-Disposition", "inline; filename=\"" + hash + ".png\"");
		builder.entity(ret);
		return builder.build();
	    } catch (IOException e) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to read image").build();
	    }
	}
	return null;
    }

    @Override
    public void putCachedResponse(String view, String layer, String hash, File file) {
	int currentSize = getSize(view, layer);
	if (currentSize > maxSize) {
	    return;
	}
	Path dirPath = rootDir.resolve(Paths.get(view, layer));
	try {
	    Files.createDirectories(dirPath);
	    Path destPath = getCacheFilePath(view, layer, hash);
	    Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

	    // write timestamp
	    Path tsPath = getTimestampFilePath(view, layer, hash);
	    Files.write(tsPath, Long.toString(System.currentTimeMillis()).getBytes());
	} catch (IOException e) {
	    throw new RuntimeException("Failed to cache response", e);
	}
    }

    @Override
    public Date getCachedResponseDate(String view, String layer, String hash) {
	Path tsPath = getTimestampFilePath(view, layer, hash);
	if (!Files.exists(tsPath))
	    return null;

	try {
	    String content = new String(Files.readAllBytes(tsPath));
	    long timestamp = Long.parseLong(content.trim());
	    return new Date(timestamp);
	} catch (IOException | NumberFormatException e) {
	    return null;
	}
    }

    @Override
    public void deleteCachedResponse(String view, String layer, String hash) {
	try {
	    Path filePath = getCacheFilePath(view, layer, hash);
	    Path tsPath = getTimestampFilePath(view, layer, hash);
	    Files.deleteIfExists(filePath);
	    Files.deleteIfExists(tsPath);
	} catch (IOException e) {
	    throw new RuntimeException("Failed to delete cached response", e);
	}
    }

    @Override
    public Integer getSize() {
	Integer ret = 0;
	List<String> views = getViews();
	for (String view : views) {
	    List<String> layers = getLayers(view);
	    for (String layer : layers) {
		Integer size = getSize(view, layer);
		ret += size;
	    }
	}
	return ret;
    }

    @Override
    public Integer getSize(String view, String layer) {
	Integer ret = 0;
	if (rootDir == null) {
	    return ret;
	}
	File file = rootDir.toFile();
	if (file.exists()) {
	    File viewFolder = new File(file, view);
	    if (viewFolder.exists() && viewFolder.isDirectory()) {
		File layerFolder = new File(viewFolder, layer);
		if (layerFolder.exists() && layerFolder.isDirectory()) {
		    File[] files = layerFolder.listFiles();
		    for (File f : files) {
			if (f.isFile() && f.getName().endsWith(".cache")) {
			    ret++;
			}
		    }
		}
	    }
	}

	return ret;
    }

    @Override
    public Integer getMaxSize() {
	return maxSize;
    }

    @Override
    public void setMaxSize(Integer size) {
	this.maxSize = size;

    }

    @Override
    public List<String> getViews() {
	List<String> ret = new ArrayList<String>();
	if (rootDir == null) {
	    return ret;
	}
	File file = rootDir.toFile();
	if (file.exists()) {
	    File[] files = file.listFiles();

	    for (File f : files) {
		if (f.isDirectory()) {
		    ret.add(f.getName());
		}
	    }
	}

	return ret;
    }

    @Override
    public List<String> getLayers(String view) {
	List<String> ret = new ArrayList<String>();
	if (rootDir == null) {
	    return ret;
	}
	File file = rootDir.toFile();
	if (file.exists()) {
	    File viewFolder = new File(file, view);
	    if (viewFolder.exists() && viewFolder.isDirectory()) {
		File[] files = viewFolder.listFiles();

		for (File f : files) {
		    if (f.isDirectory()) {
			ret.add(f.getName());
		    }
		}
	    }
	}

	return ret;
    }

}
