package eu.essi_lab.gssrv.conf.task.turtle;

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
