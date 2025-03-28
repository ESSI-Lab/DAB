package eu.essi_lab.turtle;

import java.net.URL;
import java.util.Collection;

import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ReportEntry;

import eu.essi_lab.gssrv.conf.task.turtle.TurtleValidator;

public class TurtleValidatorTest {
    public static void main(String[] args) throws Exception {
	// ValidationReport report = TurtleValidator.validate(new
	// File("/home/boldrini/git/asset-standards/DCAT-AP/dataset-example.ttl"));

	ValidationReport report = TurtleValidator
		.validate(new URL("https://s3.amazonaws.com/dataset.geodab.eu/dataset/emodnet-network/9f445d9e-ce24-449d-b4a6-1fcb3d3dd1cd.ttl"),true);

	// Check if the data conforms to the SHACL shapes
	if (report.conforms()) {
	    System.out.println("The data conforms to the SHACL shapes.");
	} else {
	    System.out.println("The data does NOT conform to the SHACL shapes.");
	}

	Collection<ReportEntry> entries = report.getEntries();
	for (ReportEntry entry : entries) {
	    System.out.println(entry.message());
	    System.out.println(entry.focusNode());
	    System.out.println(entry.resultPath());
	    System.out.println(entry.sourceConstraintComponent());
	    System.out.println("------------------------");
	}

    }
}
