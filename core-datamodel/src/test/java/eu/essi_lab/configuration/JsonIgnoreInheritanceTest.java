/**
 * 
 */
package eu.essi_lab.configuration;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonIgnoreInheritanceTest implements JsonIgnoreInheritance {

    private String field1;
    private String field2;

    public JsonIgnoreInheritanceTest() {
    }

    @Override
    public String getField1() {

	return field1;
    }

    @Override
    public void setField1(String password) {

	this.field1 = password;
    }

    @Override
    public String getField2() {

	return field2;
    }

    @Override
    public void setField2(String field2) {

	this.field2 = field2;
    }

    @Test
    public void test() {

	JsonIgnoreInheritanceTest ignore = new JsonIgnoreInheritanceTest();
	ignore.setField1("value1");
	ignore.setField2("value2");

	ObjectMapper mapper = new ObjectMapper();
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	try {
	    mapper.writeValue(outputStream, ignore);
	    JSONObject jsonObject = new JSONObject(outputStream.toString("UTF-8"));

	    Assert.assertFalse(jsonObject.has("field"));
	    Assert.assertTrue(jsonObject.has("field2"));

	} catch (Exception ex) {

	    ex.printStackTrace();
	}
    }
}
