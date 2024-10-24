package eu.essi_lab.gssrv.conf.task.turtle;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class TurtleAggregator {

    public TurtleAggregator() {
	// TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
	TurtleAggregator aggregator = new TurtleAggregator();
	aggregator.aggregate("/home/boldrini/fab/fair-ease.ttl", //
		"/home/boldrini/fab/tmp1.ttl",
		"/home/boldrini/fab/tmp2.ttl"
	//
	);
    }

	public void aggregate(String targetPathSource, List<File> files) {
	String[] fileNames = new String[files.size()];
	for (int i = 0; i < files.size(); i++) {
	    fileNames[i] = files.get(i).getAbsolutePath();
	}
	aggregate(targetPathSource, fileNames);
    }

    public void aggregate(String targetPathSource, String... filePathSource) {
	GSLoggerFactory.getLogger(getClass()).info("Aggregating files to {}", targetPathSource);

	try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetPathSource))) {
	    for (String inputFile : filePathSource) {
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
			bw.write(line);
			bw.newLine(); // Write a new line after each line read
		    }
		} catch (IOException e) {
		    GSLoggerFactory.getLogger(getClass()).error("Error reading from file: " + inputFile);
		    e.printStackTrace();
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("File aggregation successful!");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
