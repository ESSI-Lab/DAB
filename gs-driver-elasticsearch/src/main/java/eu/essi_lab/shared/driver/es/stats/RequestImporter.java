package eu.essi_lab.shared.driver.es.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class RequestImporter {

    private String index;
    private ElasticsearchClient client;

    public RequestImporter(String host, String db, String index) throws IOException {
	this.client = new ElasticsearchClient(host);
	client.setDbName(db);
	client.init(index);
	this.index = index;

    }

    public void upload(String jsonl) throws Exception {
	File file = new File(jsonl);
	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    String line;
	    int batch = 10;

	    HashMap<String, String> jsons = new HashMap<>();

	    long i = 0;
	    while ((line = br.readLine()) != null) {
		i++;
		jsons.put(file.getName() + "-" + i, line);
		if (jsons.size() == batch) {
		    push(jsons);
		    jsons.clear();
		}
	    }
	    if (!jsons.isEmpty()) {
		push(jsons);
	    }

	}

    }

    private void push(HashMap<String, String> jsons) {
	client.write(index, jsons);
    }

    public static void main(String[] args) {
	RequestImporter importer = null;
	try {
	    importer = new RequestImporter("http://149.139.19.216:9200", "production", "request");
	    importer.upload("/home/boldrini/requests2.json");
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (importer != null)
		try {
		    importer.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
    }

    private void close() throws IOException {
	client.close();

    }
}
