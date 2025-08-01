package eu.essi_lab.lib.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONArrayStreamParserTest {

	public static void main(String[] args) throws Exception {
		JSONArrayStreamParser parser = new JSONArrayStreamParser();
		ByteArrayInputStream fis = new ByteArrayInputStream(new String("[[{\"a\":3},{\"b\":2}],[2]]").getBytes());
//		ByteArrayInputStream fis = new ByteArrayInputStream(new String("[{\"a\":3},{\"b\":2}]").getBytes());

		parser.parse(fis, new JSONArrayStreamParserListener() {

			@Override
			public void notifyJSONObject(JSONObject object) {
				System.out.println("object: " + object.toString());
			}

			@Override
			public void notifyJSONArray(JSONArray object) {
				System.out.println("array: " + object.toString());

			}

			@Override
			public void finished() {
				System.out.println("finished");

			}
		});
	}
}
