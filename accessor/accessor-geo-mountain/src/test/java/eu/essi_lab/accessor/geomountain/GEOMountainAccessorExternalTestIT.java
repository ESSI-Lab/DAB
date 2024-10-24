package eu.essi_lab.accessor.geomountain;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jsonldjava.shaded.com.google.common.base.Charsets;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.IOStreamUtils;
import junit.framework.TestCase;

public class GEOMountainAccessorExternalTestIT {

    Downloader d;
    String csvURL;
    private List<String> csvRecords;

    @Before
    public void init() {
	this.d = new Downloader();
	this.csvURL = "https://geomountain.s3.amazonaws.com/GEO_Mountains_In_Situ_Inventory_v2.0.csv";
	this.csvRecords = new ArrayList<String>();
    }

    @Test
    public void test() throws Exception {
	Optional<InputStream> optStream = d.downloadOptionalStream(csvURL);
	if (optStream.isPresent()) {

	    CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
	    try (CSVParser parser = new CSVParser(new InputStreamReader(optStream.get(), Charsets.ISO_8859_1), format)) {

		for (CSVRecord record : parser) {
		    if (record.size() != 20) {
			System.out.println("!!!NOT VALID!!!!");
		    }
		    int fieldCount = record.size();
		        StringJoiner joiner = new StringJoiner("|");
		    for (int i = 0; i < fieldCount; i++) {
			String field = record.get(i);
			if(field == null || field.isEmpty()) {
			    //System.out.println("CHECK");
			}
			field = field.contains("|") ? field.replace("|", "/") : field;
			joiner.add(field == null || field.isEmpty() ? null : field);
		    }
		    // StringJoiner joiner = new StringJoiner("~");
		    // record.forEach(field -> joiner.add(field == null || field.isEmpty() || field.isBlank() ||
		    // field.equals("") ? "" : field));
		    csvRecords.add(joiner.toString());

		}

	    }

	    int i = 0;
	    for (String s : csvRecords) {
		
		String[] splittedString = s.split("\\|");
		int k=0;
		for(String s2 : splittedString) {
		    if(s2.contains(",")) {
			System.out.println("INDEX " + k);
			System.out.println("Record with COMMA in the text. ID:" + splittedString[0]);
		    }
		    k++;
		}
		
		Assert.assertTrue(splittedString.length == 20);
		System.out.println(s);
		
	    }
	    /**
	     * 1)GEO Mountains ID,
	     * 2)Name,
	     * 3)Category,
	     * 4)Latitude,
	     * 5)Longitude,
	     * 6)Elevation and/or range (m a.s.l),
	     * 7)Country,
	     * 8)Purpose,
	     * 9)Operating Organization(s),
	     * 10)URL(s) of site/station webpage,
	     * 11)Email address or other contact,
	     * 12)Parameters measured,
	     * 13)Temporal coverage,
	     * 14)Temporal Frequency,
	     * 15)Measurement method(s) / protocol(s) followed,
	     * 16)Instrumentation Deployed,
	     * 17)Data freely available for download (including via fast online inscription process)?,
	     * 18)URL(s) to data repository and download (if applicable) ,
	     * 19)DOI(s) of associated publication(s),
	     * 20)Parent network and / or other comment(s)
	     */
	    // int breakLoop = 0;
	    //
	    // for (int j=0; j < 200 ; j++) {
	    // String[] values = new String[20];
	    // values = list.get(j).values();
	    //
	    // System.out.println(list.get(j).values());
	    // String id = list.get(j).get("GEO Mountains ID");
	    // String stationName = list.get(j).get("Name");
	    // String category = list.get(j).get("Category");
	    // String lat = list.get(j).get("Latitude");
	    // String lon = list.get(j).get("Longitude");
	    // String elevation = list.get(j).get("Elevation and/or range (m a.s.l)");
	    // String country = list.get(j).get("Country");
	    // String purpose = list.get(j).get("Purpose");
	    // String organization = list.get(j).get("Operating Organization(s)");
	    // String urls = list.get(j).get("URL(s) of site/station webpage");
	    // String email = list.get(j).get("Email address or other contact");
	    // String parameters = list.get(j).get("Parameters measured");
	    // String temporalCoverage = list.get(j).get("Temporal coverage");
	    // String frequency = list.get(j).get("Temporal Frequency");
	    // String mesurements = list.get(j).get("Measurement method(s) / protocol(s) followed");
	    // String instr = list.get(j).get("Instrumentation Deployed");
	    // String dataAvailable = list.get(j).get("Data freely available for download (including via fast online
	    // inscription process)?");
	    // String dataURL = list.get(j).get("URL(s) to data repository and download (if applicable) ");
	    // String doiURL = list.get(j).get("DOI(s) of associated publication(s)");
	    // String parentNetwork = list.get(j).get("Parent network and / or other comment(s)");
	    // System.out.println(id + "," + stationName + "," + category + "," + lat + "," + lon + "," + elevation +
	    // "," + country + "," + purpose + "," + organization + "," + urls + "," + email + "," + parameters + "," +
	    // temporalCoverage + "," + frequency + "," + mesurements + "," + instr + "," + dataAvailable + ",");
	    // breakLoop++;
	    // if(breakLoop > 100)
	    // break;
	    // }
	}
    }

}
