package eu.essi_lab.turtle;

import java.io.InputStream;

import org.junit.Test;

import eu.essi_lab.gssrv.conf.task.turtle.TurtleMapper;
import eu.essi_lab.model.resource.Dataset;

public class TurtleMapperTest {

    @Test
    public void fairEaseTest() throws Exception {
	String filename = "dataset-sdn.xml";
	InputStream stream = TurtleMapperTest.class.getClassLoader().getResourceAsStream(filename);
	TurtleMapper mapper = new TurtleMapper();
	Dataset dataset = Dataset.create(stream);
	dataset.setOriginalMetadata(null);
	String result = mapper.map(null, dataset);
	System.out.println(result);
    }

   
}
