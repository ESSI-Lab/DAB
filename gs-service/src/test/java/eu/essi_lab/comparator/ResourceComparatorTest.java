/**
 * 
 */
package eu.essi_lab.comparator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.ArrayStack;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.GSResourceComparator;
import eu.essi_lab.model.resource.GSResourceComparator.ComparisonResponse;
import eu.essi_lab.model.resource.GSResourceComparator.ComparisonValues;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class ResourceComparatorTest {

    @Test
    public void test1() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	List<Queryable> listValues = new ArrayList<Queryable>(ResourceProperty.listQueryables());
	listValues.addAll(MetadataElement.listQueryables());

	ComparisonResponse response = GSResourceComparator.compare(listValues, res1, res2);

	Assert.assertTrue(response.getProperties().isEmpty());
    }

    @Test
    public void bboxTest1() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res1.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().clearGeographicBoundingBoxes();
	res1.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(//
		new BigDecimal(0), //
		new BigDecimal(0), //
		new BigDecimal(0), //
		new BigDecimal(0));

	IndexedElementsWriter.write(res1);

	List<Queryable> listValues = new ArrayList<Queryable>(ResourceProperty.listQueryables());
	listValues.addAll(MetadataElement.listQueryables());

	ComparisonResponse response = GSResourceComparator.compare(listValues, res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(3, properties.size());

	Assert.assertEquals(MetadataElement.ANY_TEXT, properties.get(0));
	Assert.assertEquals(MetadataElement.BOUNDING_BOX, properties.get(1));
	Assert.assertEquals(ResourceProperty.RESOURCE_TIME_STAMP, properties.get(2));

	Optional<ComparisonValues> values = response.getComparisonValues(MetadataElement.BOUNDING_BOX);
	Assert.assertFalse(values.get().getValues1().isEmpty());
	Assert.assertFalse(values.get().getValues2().isEmpty());
    }

    @Test
    public void bboxTest2() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res1.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().clearGeographicBoundingBoxes();
	res1.getIndexesMetadata().remove(MetadataElement.BOUNDING_BOX.getName());

	IndexedElementsWriter.write(res1);

	List<Queryable> listValues = new ArrayList<Queryable>(ResourceProperty.listQueryables());
	listValues.addAll(MetadataElement.listQueryables());

	ComparisonResponse response = GSResourceComparator.compare(listValues, res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(3, properties.size());

	Assert.assertEquals(MetadataElement.ANY_TEXT, properties.get(0));
	Assert.assertEquals(MetadataElement.BOUNDING_BOX, properties.get(1));
	Assert.assertEquals(ResourceProperty.RESOURCE_TIME_STAMP, properties.get(2));

	Optional<ComparisonValues> values = response.getComparisonValues(MetadataElement.BOUNDING_BOX);
	Assert.assertFalse(values.get().getValues1().isEmpty());
	Assert.assertTrue(values.get().getValues2().isEmpty());
    }

    @Test
    public void bboxTest3() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res2.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().clearGeographicBoundingBoxes();
	res2.getIndexesMetadata().remove(MetadataElement.BOUNDING_BOX.getName());

	IndexedElementsWriter.write(res2);

	List<Queryable> listValues = new ArrayList<Queryable>(ResourceProperty.listQueryables());
	listValues.addAll(MetadataElement.listQueryables());

	ComparisonResponse response = GSResourceComparator.compare(listValues, res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(3, properties.size());

	Assert.assertEquals(MetadataElement.ANY_TEXT, properties.get(0));
	Assert.assertEquals(MetadataElement.BOUNDING_BOX, properties.get(1));
	Assert.assertEquals(ResourceProperty.RESOURCE_TIME_STAMP, properties.get(2));

	Optional<ComparisonValues> values = response.getComparisonValues(MetadataElement.BOUNDING_BOX);
	Assert.assertFalse(values.get().getValues1().isEmpty());
	Assert.assertTrue(values.get().getValues2().isEmpty());
    }

    @Test
    public void test2() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res1.getHarmonizedMetadata().getCoreMetadata().setTitle("Title");
	IndexedElementsWriter.write(res1);

	List<Queryable> listValues = new ArrayList<Queryable>(ResourceProperty.listQueryables());
	listValues.addAll(MetadataElement.listQueryables());

	//
	//
	//

	ComparisonResponse response = GSResourceComparator.compare(listValues, res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(3, properties.size());

	Assert.assertEquals(MetadataElement.ANY_TEXT, properties.get(0));
	Assert.assertEquals(ResourceProperty.RESOURCE_TIME_STAMP, properties.get(1));
	Assert.assertEquals(MetadataElement.TITLE, properties.get(2));

	//
	//
	//

	ComparisonValues comparisonValues = response.getComparisonValues(MetadataElement.TITLE).get();

	List<String> values1 = comparisonValues.getValues1();
	Assert.assertEquals(1, values1.size());

	Assert.assertEquals("Title", values1.get(0));
    }

    @Test
    public void test2_1() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res1.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().clearKeywords();
	res1.getIndexesMetadata().remove(MetadataElement.KEYWORD.getName());

	IndexedElementsWriter.write(res1);

	List<Queryable> listValues = Arrays.asList(MetadataElement.KEYWORD);

	//
	//
	//

	ComparisonResponse response = GSResourceComparator.compare(listValues, res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(1, properties.size());

	Assert.assertEquals(MetadataElement.KEYWORD, properties.get(0));

	//
	//
	//

	ComparisonValues comparisonValues = response.getComparisonValues(MetadataElement.KEYWORD).get();
	
	Assert.assertFalse(comparisonValues.getValues1().isEmpty());
	Assert.assertTrue(comparisonValues.getValues2().isEmpty());	
    }
    
    @Test
    public void test2_2() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res2.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().clearKeywords();
	res2.getIndexesMetadata().remove(MetadataElement.KEYWORD.getName());

	IndexedElementsWriter.write(res2);

	List<Queryable> listValues = Arrays.asList(MetadataElement.KEYWORD);

	//
	//
	//

	ComparisonResponse response = GSResourceComparator.compare(listValues, res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(1, properties.size());

	Assert.assertEquals(MetadataElement.KEYWORD, properties.get(0));

	//
	//
	//

	ComparisonValues comparisonValues = response.getComparisonValues(MetadataElement.KEYWORD).get();
	
	Assert.assertFalse(comparisonValues.getValues1().isEmpty());
	Assert.assertTrue(comparisonValues.getValues2().isEmpty());	
    }

    @Test
    public void test3() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res1.getHarmonizedMetadata().getCoreMetadata().setTitle("Title1");
	IndexedElementsWriter.write(res1);

	res2.getHarmonizedMetadata().getCoreMetadata().setTitle("Title2");
	IndexedElementsWriter.write(res2);

	//
	//
	//

	ComparisonResponse response = GSResourceComparator.compare(Arrays.asList(MetadataElement.TITLE), res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(1, properties.size());

	Assert.assertEquals(MetadataElement.TITLE, properties.get(0));

	//
	//
	//

	ComparisonValues comparisonValues = response.getComparisonValues(MetadataElement.TITLE).get();

	List<String> values1 = comparisonValues.getValues1();
	Assert.assertEquals(1, values1.size());

	Assert.assertEquals("Title1", values1.get(0));

	List<String> values2 = comparisonValues.getValues2();
	Assert.assertEquals(1, values2.size());

	Assert.assertEquals("Title2", values2.get(0));
    }

    @Test
    public void test4() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addKeyword("keyword1");
	IndexedElementsWriter.write(res1);

	//
	//
	//

	ComparisonResponse response = GSResourceComparator.compare(Arrays.asList(MetadataElement.KEYWORD), res1, res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(1, properties.size());

	Assert.assertEquals(MetadataElement.KEYWORD, properties.get(0));

	//
	//
	//

	ComparisonValues comparisonValues = response.getComparisonValues(MetadataElement.KEYWORD).get();

	List<String> values1 = comparisonValues.getValues1();
	Assert.assertTrue(values1.contains("keyword1"));

	List<String> values2 = comparisonValues.getValues2();
	Assert.assertFalse(values2.contains("keyword1"));
    }

    @Test
    public void test5() throws JAXBException {

	GSResource res1 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));
	GSResource res2 = GSResource.create(getClass().getClassLoader().getResourceAsStream("comparator/res.xml"));

	res1.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().addKeyword("keyword1");
	res2.getHarmonizedMetadata().getCoreMetadata().setAbstract("ABSTRACT");

	IndexedElementsWriter.write(res1);
	IndexedElementsWriter.write(res2);

	//
	//
	//

	ComparisonResponse response = GSResourceComparator.compare(Arrays.asList( //
		MetadataElement.KEYWORD, MetadataElement.ABSTRACT, ResourceProperty.RESOURCE_TIME_STAMP), //
		res1, //
		res2);

	List<Queryable> properties = response.getProperties();

	Assert.assertEquals(3, properties.size());

	Assert.assertEquals(MetadataElement.ABSTRACT, properties.get(0));
	Assert.assertEquals(MetadataElement.KEYWORD, properties.get(1));
	Assert.assertEquals(ResourceProperty.RESOURCE_TIME_STAMP, properties.get(2));

	//
	//
	//

	{
	    ComparisonValues comparisonValues = response.getComparisonValues(MetadataElement.ABSTRACT).get();

	    List<String> values1 = comparisonValues.getValues1();
	    Assert.assertFalse(values1.contains("ABSTRACT"));

	    List<String> values2 = comparisonValues.getValues2();
	    Assert.assertTrue(values2.contains("ABSTRACT"));
	}

	//
	//
	//

	{
	    ComparisonValues comparisonValues = response.getComparisonValues(MetadataElement.KEYWORD).get();

	    List<String> values1 = comparisonValues.getValues1();
	    Assert.assertTrue(values1.contains("keyword1"));

	    List<String> values2 = comparisonValues.getValues2();
	    Assert.assertFalse(values2.contains("keyword1"));
	}

	//
	//
	//

	{
	    Assert.assertTrue(response.getComparisonValues(ResourceProperty.RESOURCE_TIME_STAMP).isPresent());
	}
    }
}
