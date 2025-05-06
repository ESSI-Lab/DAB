/**
 * 
 */
package eu.essi_lab.comparator;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.GSResourceComparator;
import eu.essi_lab.model.resource.GSResourceComparator.ComparisonResponse;
import eu.essi_lab.model.resource.GSResourceComparator.ComparisonValues;
import eu.essi_lab.turtle.TurtleMapperTest;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class DuplicatedIdentifierTest {

    @Test
    public void test1() throws Exception {

	String filename = "online_id_duplicated.txt";
	InputStream stream = DuplicatedIdentifierTest.class.getClassLoader().getResourceAsStream(filename);
	String s = IOUtils.toString(stream);
	String[] splittedLines = s.split("\r\n");

	for (String s1 : splittedLines) {
	    String toIds = s1.split("FILE_IDENTIFIERS:")[1];
	    String[] toRemove = toIds.split(";");
	    if (toRemove.length > 1) {
		for (int k = 1; k < toRemove.length; k++) {
		    System.out.println(toRemove[k]);
		}
	    }

	}
	Assert.assertTrue(splittedLines.length == 1200);
    }

}
