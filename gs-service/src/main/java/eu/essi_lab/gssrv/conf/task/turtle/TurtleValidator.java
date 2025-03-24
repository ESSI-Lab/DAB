package eu.essi_lab.gssrv.conf.task.turtle;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.net.URL;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.util.FileManager;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class TurtleValidator {
    private static final String MODEL_LOCATION = "https://raw.githubusercontent.com/fair-ease/asset-standards/refs/heads/main/DCAT-AP/FE-DCAT-AP-SHACLshapes.ttl";

    private static Model model = null;

    private static File downloadFile(String url, String filename) throws Exception {
	Downloader downloader = new Downloader();
	Optional<InputStream> response = downloader.downloadOptionalStream(url);
	if (response.isEmpty()) {
	    GSLoggerFactory.getLogger(TurtleValidator.class).error("missing file: {}", url);
	    return null;
	} else {
	    InputStream stream = response.get();
	    File sourceFile = File.createTempFile(TurtleValidator.class.getSimpleName(), filename);
	    FileOutputStream fos = new FileOutputStream(sourceFile);
	    IOUtils.copy(stream, fos);
	    stream.close();
	    fos.close();
	    return sourceFile;

	}
    }

    public static ValidationReport validate(File turtle) {
	Model datasetModel = FileManager.get().loadModel(turtle.getAbsolutePath());
	if (model == null) {
	    File tmpModelFile;
	    try {
		tmpModelFile = downloadFile(MODEL_LOCATION, "latestModel.ttl");
		model = FileManager.get().loadModel(tmpModelFile.getAbsolutePath());
		tmpModelFile.delete();
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(TurtleValidator.class).error(e);
	    }

	}
	ValidationReport report = ShaclValidator.get().validate(datasetModel.getGraph(), model.getGraph());
	return report;
    }

    public static ValidationReport validate(URL url) throws Exception {
	Downloader d = new Downloader();
	InputStream stream = d.downloadOptionalStream(url.toString()).get();
	File tmp = File.createTempFile(TurtleValidator.class.getClassLoader().getName(), ".ttl");
	FileOutputStream fos = new FileOutputStream(tmp);
	IOUtils.copy(stream, fos);
	stream.close();
	fos.close();
	ValidationReport ret = validate(tmp);
	tmp.delete();
	return ret;	
    }
}
