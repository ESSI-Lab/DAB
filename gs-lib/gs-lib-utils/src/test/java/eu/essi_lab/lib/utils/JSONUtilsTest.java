package eu.essi_lab.lib.utils;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.junit.Test;

public class JSONUtilsTest {

    @Test
    public void test() {
	String jString = "    {\n" + 
		"        \"id\": 1005704398,\n" + 
		"        \"nombre\": null,\n" + 
		"        \"id_externo\": \"1005704398\",\n" + 
		"        \"geom\": {\n" + 
		"            \"type\": \"Point\",\n" + 
		"            \"coordinates\": [\n" + 
		"                -58.5911004093614,\n" + 
		"                -27.2741854977372\n" + 
		"            ]\n" + 
		"        },\n" + 
		"        \"cero_ign\": null\n" + 
		"    }";
	JSONObject json = new JSONObject(jString);
	BigDecimal bd = JSONUtils.getBigDecimal(json, "geom/coordinates[0]");
	assertEquals(new BigDecimal("-58.5911004093614"), bd);
    }

}
