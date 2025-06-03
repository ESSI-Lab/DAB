package eu.essi_lab.profiler.om;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class DataDownloaderTool {

    public static void main(String[] args) {
	DataDownloaderTool tool = new DataDownloaderTool();
	String token = "my-token";
	File zip = tool.download("http://localhost:9090/gs-service/services/essi/token/" + token
		+ "/view/his-central/om-api/observations?includeData=true&asynchDownload=true&observedProperty=precipitation&ontology=his-central&north=42.492&south=42.252&east=11.094&west=10.693");
	System.out.println("Downloaded to: " + zip.getAbsolutePath());
    }

    public DataDownloaderTool() {

    }

    /**
     * @param folder
     * @param baseURL something like
     *        "http://localhost:9090/gs-service/services/essi/token/my-token/view/his-central/om-api/observations?includeData=true&asynchDownload=true&observedProperty=precipitation&ontology=his-central&north=42.492&south=42.252&east=11.094&west=10.693&beginPosition=2024-01-01&endPosition=2025-01-01"
     */
    public File download(String baseURL) {

	GSLoggerFactory.getLogger(getClass()).info("Started asynch download of {}", baseURL);

	File zipFile = null;
	try {
	    zipFile = File.createTempFile(getClass().getSimpleName(), ".zip");
	} catch (IOException e) {
	    e.printStackTrace();
	}

	Path tempDir = null;
	try {
	    tempDir = Files.createTempDirectory(getClass().getSimpleName());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	GSLoggerFactory.getLogger(getClass()).info("Temporary directory created at: {}", tempDir.toAbsolutePath());

	// Optionally, delete on exit (not recursive!)
	tempDir.toFile().deleteOnExit(); // Note: doesn't delete contents

	File userRequestFile = new File(tempDir.toFile(), "log.txt");
	Date dateStart = new Date();

	// remove download parameters to find out base URL
	baseURL = removeParameter(baseURL, "asynchDownload", "true");
	baseURL = removeParameter(baseURL, "includeData", "true");

	Downloader downloader = new Downloader();
	boolean firstLoop = true;
	String resumptionToken = null;
	int blocks = 0;

	while (firstLoop || resumptionToken != null) {
	    firstLoop = false;

	    GSLoggerFactory.getLogger(getClass()).info("Listing block {}", ++blocks);

	    String listURL = baseURL;
	    if (resumptionToken != null) {
		listURL = listURL + "&resumptionToken=" + resumptionToken;
		listURL = listURL.replace("?&", "?");
	    }
	    Optional<String> string = downloader.downloadOptionalString(listURL);
	    if (string.isPresent()) {
		String str = string.get();
		JSONObject json = new JSONObject(str);
		if (json.has("completed") && json.getBoolean("completed") == false && json.has("resumptionToken")) {
		    resumptionToken = json.getString("resumptionToken");
		}
		if (json.has("member")) {

		    JSONArray members = json.getJSONArray("member");
		    GSLoggerFactory.getLogger(getClass()).info("Retrieved block of size {}, resumption token {}", members.length(),
			    resumptionToken);
		    if (members.length() == 0) {
			break;
		    }
		    // for each observation
		    for (int i = 0; i < members.length(); i++) {
			JSONObject member = members.getJSONObject(i);
			String id = member.getString("id");
			String source = "unknown";

			JSONArray parameters = member.getJSONArray("parameter");
			for (int j = 0; j < parameters.length(); j++) {
			    JSONObject parameter = parameters.getJSONObject(j);
			    String name = parameter.getString("name");
			    String value = parameter.getString("value");
			    if (name.equals("source")) {
				source = value;
			    }
			}

			// download single observation URL
			String downloadURL = baseURL;
			downloadURL = addParameter(downloadURL, "observationIdentifier", id);
			downloadURL = addParameter(downloadURL, "includeData", "true");

			// write
			File sourceDir = new File(tempDir.toFile(), source);

			if (!sourceDir.exists()) {
			    sourceDir.mkdir();
			}

			File idDir = new File(sourceDir, id);

			if (!idDir.exists()) {
			    idDir.mkdir();
			}

			String extension = ".json";
			if (downloadURL.contains("csv")) {
			    extension = ".csv";
			}
			Optional<HttpResponse<InputStream>> response = downloader.downloadOptionalResponse(downloadURL);
			File logFile = new File(idDir, "log.txt");
			File dataFile = new File(idDir, "data" + extension);
			if (response.isPresent()) {
			    HttpResponse<InputStream> r = response.get();
			    int status = r.statusCode();
			    write("request: " + downloadURL + "\nstatus: " + status + "\ndate: "
				    + ISO8601DateTimeUtils.getISO8601DateTime(), logFile);
			    InputStream body = r.body();
			    FileOutputStream fos;
			    try {
				fos = new FileOutputStream(dataFile);
				IOUtils.copy(body, fos);
				body.close();
				fos.close();
			    } catch (Exception e) {
				// TODO Auto-generated catch block
				write("status: " + status + "\nexception " + e.getMessage(), logFile);
				e.printStackTrace();
			    }

			} else {
			    write("No response", logFile);
			}

		    }
		} else {
		    break;
		}
	    }

	}

	write("request: " + baseURL + "\ndate start: " + ISO8601DateTimeUtils.getISO8601DateTime(dateStart) + "\ndate end: "
		+ ISO8601DateTimeUtils.getISO8601DateTime(), userRequestFile);

	try {
	    GSLoggerFactory.getLogger(getClass()).info("Zipping folder {}", tempDir);
	    zipFolder(tempDir, zipFile.toPath());
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error zipping folder");
	}

	try {
	    GSLoggerFactory.getLogger(getClass()).info("Removing folder");

	    deleteFolder(tempDir);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	GSLoggerFactory.getLogger(getClass()).info("Ended asynch download of {} to file: {}", baseURL, zipFile.getAbsolutePath());

	return zipFile;

    }

    public static void deleteFolder(Path path) throws IOException {
	if (!Files.exists(path))
	    return;

	Files.walkFileTree(path, new SimpleFileVisitor<>() {
	    @Override
	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.delete(file); // Delete files
		return FileVisitResult.CONTINUE;
	    }

	    @Override
	    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		Files.delete(dir); // Delete directories after files are gone
		return FileVisitResult.CONTINUE;
	    }
	});
    }

    public static void zipFolder(Path sourceFolderPath, Path zipPath) throws IOException {
	try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
	    Files.walk(sourceFolderPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
		ZipEntry zipEntry = new ZipEntry(sourceFolderPath.relativize(path).toString().replace("\\", "/"));
		try (InputStream inputStream = Files.newInputStream(path)) {
		    zipOutputStream.putNextEntry(zipEntry);
		    byte[] buffer = new byte[1024];
		    int length;
		    while ((length = inputStream.read(buffer)) > 0) {
			zipOutputStream.write(buffer, 0, length);
		    }
		    zipOutputStream.closeEntry();
		} catch (IOException e) {
		    throw new UncheckedIOException(e);
		}
	    });
	}
    }

    private String removeParameter(String baseURL, String parameter, String value) {
	baseURL = baseURL.replace("&" + parameter + "=" + value, "");
	baseURL = baseURL.replace("?" + parameter + "=" + value, "?");
	baseURL = baseURL.replace("?&", "?");
	return baseURL;
    }

    private String addParameter(String baseURL, String parameter, String value) {
	if (baseURL.endsWith("?") || baseURL.endsWith("&")) {
	    return baseURL + parameter + "=" + value;
	}
	if (!baseURL.contains("?")) {
	    return baseURL + "?" + parameter + "=" + value;
	} else {
	    return baseURL + "&" + parameter + "=" + value;
	}
    }

    private void write(String string, File logFile) {
	try {
	    FileOutputStream fos = new FileOutputStream(logFile);
	    fos.write(string.getBytes(StandardCharsets.UTF_8));
	    fos.close();
	} catch (Exception e) {

	    e.printStackTrace();
	}

    }
}
