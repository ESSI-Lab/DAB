package eu.essi_lab.gssrv.conf.task;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;

public class TurtleTaskTest {

    @Test
    public void test() throws Exception {
	// TurtleMapper mapper = new TurtleMapper();
	// Dataset res = new Dataset();
	// res.fromStream(null)
	// String turtle = mapper.map(null, res );
	// System.out.println(turtle);
	String fusekiURL = "http://localhost:3030/dataset/data";
	String turtleFilePath = "path_to_your_file.ttl";

	// Load Turtle file into Jena model
	Model model = ModelFactory.createDefaultModel();
	RDFDataMgr.read(model, turtleFilePath);

	// Connect to Fuseki dataset
	org.apache.jena.query.Dataset dataset = TDBFactory.createDataset(fusekiURL);

	// Upload model to Fuseki dataset
	dataset.begin(ReadWrite.WRITE);
	try {
	    dataset.setDefaultModel(model);
	    dataset.commit();
	} finally {
	    dataset.end();
	}

    }

}
