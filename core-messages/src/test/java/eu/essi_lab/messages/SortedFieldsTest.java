/**
 * 
 */
package eu.essi_lab.messages;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class SortedFieldsTest {

    @Test
    public void test() {

	SortedFields fields = new SortedFields(MetadataElement.IDENTIFIER, SortOrder.ASCENDING);
	List<SimpleEntry<Queryable, SortOrder>> list = fields.getFields();
	Assert.assertEquals(1, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());

	//
	//
	//

	fields = SortedFields.of(MetadataElement.IDENTIFIER, SortOrder.ASCENDING);
	list = fields.getFields();
	Assert.assertEquals(1, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());

	//
	//
	//

	fields.setFields(Arrays.asList(new SimpleEntry<>(MetadataElement.TITLE, SortOrder.DESCENDING)));
	list = fields.getFields();
	Assert.assertEquals(1, list.size());

	Assert.assertEquals(MetadataElement.TITLE, list.get(0).getKey());
	Assert.assertEquals(SortOrder.DESCENDING, list.get(0).getValue());
    }

    @Test
    public void test1() {

	SortedFields fields = new SortedFields(MetadataElement.IDENTIFIER, SortOrder.ASCENDING);
	List<SimpleEntry<Queryable, SortOrder>> list = fields.getFields();
	Assert.assertEquals(1, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());

	//
	//
	//

	fields = SortedFields.of(MetadataElement.IDENTIFIER, SortOrder.ASCENDING);
	list = fields.getFields();
	Assert.assertEquals(1, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());
    }

    @Test
    public void test2() {

	SortedFields fields = new SortedFields(Arrays.asList(//
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.IDENTIFIER, SortOrder.ASCENDING), //
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.ABSTRACT, SortOrder.DESCENDING)));

	List<SimpleEntry<Queryable, SortOrder>> list = fields.getFields();
	Assert.assertEquals(2, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());

	Assert.assertEquals(MetadataElement.ABSTRACT, list.get(1).getKey());
	Assert.assertEquals(SortOrder.DESCENDING, list.get(1).getValue());

	//
	//
	//

	fields = SortedFields.of(Arrays.asList(//
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.IDENTIFIER, SortOrder.ASCENDING), //
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.ABSTRACT, SortOrder.DESCENDING)));

	list = fields.getFields();

	Assert.assertEquals(2, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());

	Assert.assertEquals(MetadataElement.ABSTRACT, list.get(1).getKey());
	Assert.assertEquals(SortOrder.DESCENDING, list.get(1).getValue());
    }

    @Test
    public void test3() {

	SortedFields fields = new SortedFields(//
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.IDENTIFIER, SortOrder.ASCENDING), //
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.ABSTRACT, SortOrder.DESCENDING));

	List<SimpleEntry<Queryable, SortOrder>> list = fields.getFields();
	Assert.assertEquals(2, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());

	Assert.assertEquals(MetadataElement.ABSTRACT, list.get(1).getKey());
	Assert.assertEquals(SortOrder.DESCENDING, list.get(1).getValue());

	//
	//
	//

	fields = SortedFields.of(//
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.IDENTIFIER, SortOrder.ASCENDING), //
		new SimpleEntry<Queryable, SortOrder>(MetadataElement.ABSTRACT, SortOrder.DESCENDING));

	list = fields.getFields();

	Assert.assertEquals(2, list.size());

	Assert.assertEquals(MetadataElement.IDENTIFIER, list.get(0).getKey());
	Assert.assertEquals(SortOrder.ASCENDING, list.get(0).getValue());

	Assert.assertEquals(MetadataElement.ABSTRACT, list.get(1).getKey());
	Assert.assertEquals(SortOrder.DESCENDING, list.get(1).getValue());
    }
}
