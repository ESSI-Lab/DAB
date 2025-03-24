package eu.essi_lab.turtle;

import java.net.URL;

import org.apache.jena.shacl.ValidationReport;

import eu.essi_lab.gssrv.conf.task.turtle.TurtleValidator;

public class SHACLValidator {
    public static void main(String[] args) throws Exception {
	// ValidationReport report = TurtleValidator.validate(new
	// File("/home/boldrini/git/asset-standards/DCAT-AP/dataset-example.ttl"));

	ValidationReport report = TurtleValidator
		.validate(new URL("https://s3.amazonaws.com/dataset.geodab.eu/dataset/easydata/860c980a-d107-4f57-aa04-f49a94833d95.ttl"));

	// Check if the data conforms to the SHACL shapes
	if (report.conforms()) {
	    System.out.println("The data conforms to the SHACL shapes.");
	} else {
	    System.out.println("The data does NOT conform to the SHACL shapes.");
	}

	// Print the validation report (optional)
	report.getEntries().forEach(entry -> {
	    System.out.println(entry.message());
	    System.out.println(entry.focusNode());
	    System.out.println(entry.resultPath());
	    System.out.println(entry.sourceConstraintComponent());
	    System.out.println("------------------------");
	});
    }
}
