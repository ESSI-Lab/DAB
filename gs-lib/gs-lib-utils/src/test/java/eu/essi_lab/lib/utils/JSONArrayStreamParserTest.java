package eu.essi_lab.lib.utils;

import java.io.File;
import java.io.FileInputStream;

import org.json.JSONObject;

public class JSONArrayStreamParserTest {

    public static void main(String[] args) throws Exception {
	JSONArrayStreamParser parser = new JSONArrayStreamParser();
	FileInputStream fis = new FileInputStream(new File("/home/boldrini/test/puntual.json"));
	parser.parse(fis, new JSONArrayStreamParserListener() {
	    
	    @Override
	    public void notifyJSONObject(JSONObject object) {
		System.out.println(object.toString());		
	    }
	    
	    @Override
	    public void finished() {
		System.out.println("finished");
		
	    }
	});
    }
}
