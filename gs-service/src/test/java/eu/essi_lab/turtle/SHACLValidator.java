package eu.essi_lab.turtle;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.util.FileManager;

public class SHACLValidator {
    public static void main(String[] args) {
	// Load the RDF data model (data.ttl) and SHACL shapes model (shapes.ttl)
	String base = "/home/boldrini/git/asset-standards/Dataset/";
//	Model dataModel = FileManager.get().loadModel(base + "original.ttl");
	Model dataModel = FileManager.get().loadModel(base + "dataset.ttl");
//	Model dataModel = FileManager.get().loadModel("/home/boldrini/git/asset-standards/examples/bc_bdi_dab_seadatanet.ttl");
	Model shapesModel = FileManager.get().loadModel(base + "FE-DCAT-AP-SHACLshapes.ttl");

	// Perform SHACL validation
	ValidationReport report = ShaclValidator.get().validate(shapesModel.getGraph(), dataModel.getGraph());

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
