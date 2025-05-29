package eu.essi_lab.accessor.polytope;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import eu.essi_lab.lib.utils.ClonableInputStream;

public class PolytopeUtils {

    private static final InputStream ALL_STATIONS = PolytopeUtils.class.getClassLoader().getResourceAsStream("amsterdam/ams_full.csv");

    public static void main(String[] args) throws Exception {

	ClonableInputStream cis = new ClonableInputStream(ALL_STATIONS);
	Reader headerIn = new InputStreamReader(cis.clone());
	Reader in = new InputStreamReader(cis.clone());
	// = CSVFormat.RFC4180.wigetHeader();
	CSVParser parser = new CSVParser(headerIn, CSVFormat.RFC4180.withHeader());
	Map<String, Integer> headerMap = parser.getHeaderMap();
	List<String> headerList = new ArrayList<String>();
	headerMap.entrySet().stream().forEach(e -> {
	    headerList.add(e.getKey());
	});
	Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

	List<ArrayList<String>> csvLines = new ArrayList<ArrayList<String>>();
	ArrayList<String> firstLine = new ArrayList<String>();

	for (CSVRecord record : records) {

	    Iterator<String> line = record.iterator();
	    ArrayList<String> stringLine = new ArrayList<String>();
	    while (line.hasNext()) {
		stringLine.add(line.next());
	    }
	    csvLines.add(stringLine);
	    // String stationName = record.get("stationid@hdr");
	    // String lat = record.get("lat@hdr");
	    // String lon = record.get("lon@hdr");
	    // String date = record.get("andate@desc");
	    // String time = record.get("antime@desc");
	    // // String dateTime = buildDate(date, time);
	    // String varName = record.get("varno@body");
	    // String alt = record.get("stalt@hdr");

	}
	System.out.println(csvLines.size());

	String field = "stationid@hdr";

	/**
	 * SORT CSV FILE 
	 * 
	 */
	
	// sort by stationid
	Comparator<ArrayList<String>> comp1 = new Comparator<ArrayList<String>>() {
	    public int compare(ArrayList<String> csvLine1, ArrayList<String> csvLine2) {
		// compare string values
		return csvLine1.get(23).compareTo(csvLine2.get(23));
	    }
	};
	Collections.sort(csvLines, comp1);

	// sort by varno
	Comparator<ArrayList<String>> comp2 = new Comparator<ArrayList<String>>() {
	    public int compare(ArrayList<String> csvLine1, ArrayList<String> csvLine2) {
		// compare integer values
		return Integer.valueOf(csvLine1.get(18)).compareTo(Integer.valueOf(csvLine2.get(18)));
	    }
	};
	Collections.sort(csvLines, comp2);

	
	/**
	 * SAVE THE ORDERED FILE IN A NEW TMP FILE 
	 */
	
	File tmpFile = File.createTempFile("allDataStation", ".tmp");
	tmpFile.deleteOnExit();
	// write local file
	FileWriter out2 = new FileWriter(tmpFile);
	CSVPrinter printer = CSVFormat.RFC4180.print(out2);
	printer.printRecord(headerList);
	for (int k = 0; k < csvLines.size(); k++) {
	    printer.printRecord(csvLines.get(k));
	}
	// flush the stream
	printer.flush();
	// close the writer
	out2.close();

	
	
	/**
	 * READ THE ORDERED CSV FILE
	 * 
	 */
	InputStream is = FileUtils.openInputStream(tmpFile);
	ClonableInputStream cis2 = new ClonableInputStream(is);
	Reader inFile = new InputStreamReader(cis2.clone());
	Iterable<CSVRecord> orderedRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(inFile);
	Map<String, String> mapStations = new HashMap<>();
	List<ArrayList<String>> csvFileLines = new ArrayList<ArrayList<String>>();
	List<File> tempList = new ArrayList<File>();
	int i = 0;
	for (CSVRecord oRecord : orderedRecords) {

	    String stationName = oRecord.get("stationid@hdr");
	    String varName = oRecord.get("varno@body");

	    Iterator<String> line = oRecord.iterator();
	    ArrayList<String> stringLine = new ArrayList<String>();

	    String variable = mapStations.get(stationName);
	    if (variable != null) {
		// check varName
		if (variable.equals(varName)) {
		    // ok: go ahead in collecting lines
		    while (line.hasNext()) {
			stringLine.add(line.next());
		    }
		    csvFileLines.add(stringLine);

		} else {
		    // new variable for the station -- close files

		    File tmpFile2 = File.createTempFile(stationName, ".tmp");
		    tmpFile2.deleteOnExit();
		    System.out.println("LINES: " + csvFileLines.size());
		    // write local file
		    FileWriter tmpOut2 = new FileWriter(tmpFile2);
		    CSVPrinter printers = CSVFormat.RFC4180.print(tmpOut2);
		    printers.printRecord(headerList);
		    for (int k = 0; k < csvFileLines.size(); k++) {
			printers.printRecord(csvFileLines.get(k));
		    }

		    // flush the stream
		    printers.flush();

		    // Iterable<CSVRecord> rec = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(printer);
		    // close the writer
		    tmpOut2.close();
		    tempList.add(tmpFile2);
		    mapStations.put(variable, varName);
		}
	    } else {

		if (csvFileLines.size() > 0) {
		    i++;
		    File tmpFile2 = File.createTempFile("tmpFile" + i, ".csv");
		    tmpFile2.deleteOnExit();
		    System.out.println("LINES: " + csvFileLines.size());
		    // write local file
		    FileWriter tmpOut2 = new FileWriter(tmpFile2);
		    CSVPrinter printers = CSVFormat.RFC4180.print(tmpOut2);
		    printers.printRecord(headerList);
		    for (int k = 0; k < csvFileLines.size(); k++) {
			printers.printRecord(csvFileLines.get(k));
		    }

		    // flush the stream
		    printers.flush();

		    // Iterable<CSVRecord> rec = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(printer);
		    // close the writer
		    tmpOut2.close();
		    tempList.add(tmpFile2);
		    csvFileLines = new ArrayList<ArrayList<String>>();
		}

		mapStations.put(stationName, varName);
		while (line.hasNext()) {
		    stringLine.add(line.next());
		}
		csvFileLines.add(stringLine);
	    }

	}

	// String tmpDirsLocation = System.getProperty("java.io.tmpdir");
	// String filename = "test-name";
	// File tmpFile = new File(tmpDirsLocation, "netcdf-connector-" + filename);
	// if (tmpFile.exists()) {
	// // already downloaded
	// // return tmpFile;
	// }
	// FileOutputStream fos = new FileOutputStream(tmpFile);
	// IOUtils.copy(netCDF.get(), fos);
	// fos.close();

	// File tmpFile = File.createTempFile(PolytopeUtils.class.getClass().getSimpleName(), pathName + ".html");
	// tmpFile.deleteOnExit();
	// System.out.println("Writing table to: " + tmpFile.getAbsolutePath());
	// FileOutputStream outputStream = new FileOutputStream(tmpFile);
	// outputStream.write(htmlTable.getBytes(StandardCharsets.UTF_8));
	// outputStream.flush();
	// outputStream.close();
	//
	// if (uploadToS3) {
	// uploadToS3(tmpFile, pathName + ".html");
	// }

	System.out.println("DONE");

    }

    // private static <T extends Comparable<? super T>> Comparator<List<T>> createComparator(int... indices) {
    // return createComparator(PolytopeUtils.<T> naturalOrder(), indices);
    // }
    //
    // private static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
    // return new Comparator<T>() {
    // @Override
    // public int compare(T t0, T t1) {
    // return t0.compareTo(t1);
    // }
    // };
    // }
    //
    // private static <T> Comparator<List<T>> createComparator(final Comparator<? super T> delegate, final int...
    // indices) {
    // return new Comparator<List<T>>() {
    // @Override
    // public int compare(List<T> list0, List<T> list1) {
    // for (int i = 0; i < indices.length; i++) {
    // T element0 = list0.get(indices[i]);
    // T element1 = list1.get(indices[i]);
    // int n = delegate.compare(element0, element1);
    // if (n != 0) {
    // return n;
    // }
    // }
    // return 0;
    // }
    // };
    // }

}
