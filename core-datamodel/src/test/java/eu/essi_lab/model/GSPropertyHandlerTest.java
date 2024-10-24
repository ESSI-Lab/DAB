package eu.essi_lab.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabrizio
 */
public class GSPropertyHandlerTest {

    @Test
    public void test() {

	GSPropertyHandler handler = GSPropertyHandler.of(//
		new GSProperty<String>("p1", "v1"), //
		new GSProperty<String>("p2", "v2"), //
		new GSProperty<String>("p3", "v3")

	);

	//
	//
	//

	List<String> names = handler.getNames();
	Assert.assertEquals(3, names.size());

	names.sort(String::compareTo);

	Assert.assertEquals("p1", names.get(0));
	Assert.assertEquals("p2", names.get(1));
	Assert.assertEquals("p3", names.get(2));

	String toString = handler.toString();
	Assert.assertTrue(toString.contains("p1:v1"));
	Assert.assertTrue(toString.contains("p2:v2"));
	Assert.assertTrue(toString.contains("p3:v3"));

	//
	//
	//

	names.forEach(name -> {

	    switch (name) {
	    case "p1":
		Assert.assertEquals("v1", handler.get(name, String.class));
		break;
	    case "p2":
		Assert.assertEquals("v2", handler.get(name, String.class));
		break;
	    case "p3":
		Assert.assertEquals("v3", handler.get(name, String.class));
		break;
	    }
	});

	String stringValue = handler.get("p4", String.class); // do not exists
	Assert.assertNull(stringValue);

	Integer intValue = handler.get("p1", Integer.class); // wrong class type
	Assert.assertNull(intValue);

	//
	//
	//

	Class<?> type = handler.getType("p1");
	Assert.assertEquals(String.class, type);

	type = handler.getType("p2");
	Assert.assertEquals(String.class, type);

	type = handler.getType("p3");
	Assert.assertEquals(String.class, type);

	type = handler.getType("p4"); // do not exists
	Assert.assertNull(type);

	//
	//
	//

	boolean added = handler.add(new GSProperty<Integer>("p1", 1));
	Assert.assertFalse(added);

	handler.remove("p1");

	names = handler.getNames();
	Assert.assertEquals(2, names.size());

	names.sort(String::compareTo);

	Assert.assertEquals("p2", names.get(0));
	Assert.assertEquals("p3", names.get(1));

	handler.remove("p4"); // do not exists

	names = handler.getNames();
	Assert.assertEquals(2, names.size());

	names.sort(String::compareTo);

	Assert.assertEquals("p2", names.get(0));
	Assert.assertEquals("p3", names.get(1));

	//
	//
	//

	added = handler.add(new GSProperty<Integer>("p1", 1));
	Assert.assertTrue(added);

	added = handler.add(new GSProperty<Integer>("p1", 1));
	Assert.assertFalse(added);

	//
	//
	//

	toString = handler.toString();
	Assert.assertTrue(toString.contains("p1:1"));
	Assert.assertTrue(toString.contains("p2:v2"));
	Assert.assertTrue(toString.contains("p3:v3"));

	//
	//
	//

	GSProperty<String> property = new GSProperty<>();

	String value = property.getValue();
	Assert.assertNull(value);

	String name = property.getName();
	Assert.assertNull(name);

	property.setName("name");
	property.setValue("value");

	value = property.getValue();
	Assert.assertEquals("value", value);

	name = property.getName();
	Assert.assertEquals("name", name);
	
	//
	//
	//
	
	GSProperty<String> property2 = new GSProperty<>("name2","value2");
	
	Assert.assertFalse(property.equals(property2));

	Assert.assertTrue(property.equals(property));
	Assert.assertTrue(property2.equals(property2));

	GSProperty<String> property3 = new GSProperty<>("name3","value2");
	Assert.assertFalse(property2.equals(property3));
	
	GSProperty<String> property4 = new GSProperty<>("name2","value3");
	Assert.assertFalse(property2.equals(property4));
	
	GSProperty<Integer> property5 = new GSProperty<>("name4",0);
	Assert.assertFalse(property2.equals(property5));
	
	//
	//
	//
	
	toString = property2.toString();
	Assert.assertTrue(toString.contains("name2:value2"));
    }
}
