/**
 * 
 */
package eu.essi_lab.model;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.BNHSPropertyReader;
import eu.essi_lab.model.resource.Dataset;

/**
 * @author Fabrizio
 */
public class BNHSPropertyTest {

    /**
     * 
     */
    @Test
    public void test() {

	String s = "HYCOSID" + BNHSPropertyReader.SEPARATOR + "3" + BNHSPropertyReader.SEPARATOR + "StationID"
		+ BNHSPropertyReader.SEPARATOR + "05LJ025" + BNHSPropertyReader.SEPARATOR + "Country" + BNHSPropertyReader.SEPARATOR
		+ "Canada" + BNHSPropertyReader.SEPARATOR + "Institute" + BNHSPropertyReader.SEPARATOR + "Water Survey of Canada"
		+ BNHSPropertyReader.SEPARATOR + "StationName" + BNHSPropertyReader.SEPARATOR + "MOSSY RIVER BELOW OUTLET OF DAUPHIN LAKE"
		+ BNHSPropertyReader.SEPARATOR + "River" + BNHSPropertyReader.SEPARATOR + "MOSSY RIVER" + BNHSPropertyReader.SEPARATOR
		+ "Lake Station" + BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "GRDC ID"
		+ BNHSPropertyReader.SEPARATOR + "4213675" + BNHSPropertyReader.SEPARATOR + "GRDC ARDB" + BNHSPropertyReader.SEPARATOR + "1"
		+ BNHSPropertyReader.SEPARATOR + "WMO Region" + BNHSPropertyReader.SEPARATOR + "4" + BNHSPropertyReader.SEPARATOR
		+ "Latitude" + BNHSPropertyReader.SEPARATOR + "51.4503" + BNHSPropertyReader.SEPARATOR + "Longitude"
		+ BNHSPropertyReader.SEPARATOR + "-99.9711" + BNHSPropertyReader.SEPARATOR + "Latitude of discharge estimate"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Longitude of discharge estimate"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Status" + BNHSPropertyReader.SEPARATOR + "A"
		+ BNHSPropertyReader.SEPARATOR + "Drainage Area" + BNHSPropertyReader.SEPARATOR + "8740" + BNHSPropertyReader.SEPARATOR
		+ "Effective Drainage Area" + BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Drainage Shapefile"
		+ BNHSPropertyReader.SEPARATOR + "Y" + BNHSPropertyReader.SEPARATOR + "Datum Name" + BNHSPropertyReader.SEPARATOR
		+ "GEODETIC SURVEY OF CANADA DATUM (MAN. GOVT. EXT.)" + BNHSPropertyReader.SEPARATOR + "Datum Altitude"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Flow to Ocean" + BNHSPropertyReader.SEPARATOR
		+ "No" + BNHSPropertyReader.SEPARATOR + "Downstream HYCOS station" + BNHSPropertyReader.SEPARATOR + "NONE"
		+ BNHSPropertyReader.SEPARATOR + "Regulation" + BNHSPropertyReader.SEPARATOR + "Yes" + BNHSPropertyReader.SEPARATOR
		+ "Regulation Start Date" + BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Regulation End Date"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Land Use Change" + BNHSPropertyReader.SEPARATOR
		+ "NONE" + BNHSPropertyReader.SEPARATOR + "Surface Cover" + BNHSPropertyReader.SEPARATOR + "NONE"
		+ BNHSPropertyReader.SEPARATOR + "Data_quality_ice" + BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR
		+ "Data_quality_open" + BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Discharge Availability"
		+ BNHSPropertyReader.SEPARATOR + "Yes" + BNHSPropertyReader.SEPARATOR + "Water Level Availability"
		+ BNHSPropertyReader.SEPARATOR + "Yes" + BNHSPropertyReader.SEPARATOR + "Water Temperature Availability"
		+ BNHSPropertyReader.SEPARATOR + "No" + BNHSPropertyReader.SEPARATOR + "Ice On / Off Dates Availability"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Ice Thickness Availability"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Snow Depth Availability"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR
		+ "Measurement Method - Discharge Measurement / Observing Method" + BNHSPropertyReader.SEPARATOR + "NONE"
		+ BNHSPropertyReader.SEPARATOR + "Measurement Method - Water Temperature (computed vs. observed)"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Measurement Method - Ice On / Off Dates"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Station / Platform Model Equipment"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Station Notes (INTERNAL USE ONLY from here on)"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "StartYear" + BNHSPropertyReader.SEPARATOR + "1936"
		+ BNHSPropertyReader.SEPARATOR + "EndYear" + BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "NYears"
		+ BNHSPropertyReader.SEPARATOR + "82" + BNHSPropertyReader.SEPARATOR + "RealTime" + BNHSPropertyReader.SEPARATOR + "Yes"
		+ BNHSPropertyReader.SEPARATOR + "Water Temp start date" + BNHSPropertyReader.SEPARATOR + "NONE"
		+ BNHSPropertyReader.SEPARATOR + "Water Temp end date" + BNHSPropertyReader.SEPARATOR + "NONE"
		+ BNHSPropertyReader.SEPARATOR + "Sampling interval (temporal frequency)" + BNHSPropertyReader.SEPARATOR + "NONE"
		+ BNHSPropertyReader.SEPARATOR + "Script-link for Archived data" + BNHSPropertyReader.SEPARATOR + "NONE"
		+ BNHSPropertyReader.SEPARATOR + "Notes for archived data" + BNHSPropertyReader.SEPARATOR
		+ "MSC GeoMet Geospatial Web Services: https://github.com/ECCC-MSC/Wateroffice/blob/master/docs/MSC%20GeoMet2_UserGuide_English.pdf"
		+ BNHSPropertyReader.SEPARATOR + "Script-link for Real-time data" + BNHSPropertyReader.SEPARATOR
		+ "http://dd.weather.gc.ca/hydrometric/csv/" + BNHSPropertyReader.SEPARATOR + "Notes for real-time data"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Script-link for Water Temp data"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Notes for water temp data"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Notes for ice on/off dates and thickness"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR + "Notes for snow depth"
		+ BNHSPropertyReader.SEPARATOR + "NONE" + BNHSPropertyReader.SEPARATOR;

	Dataset dataset = new Dataset();
	dataset.getExtensionHandler().setBNHSInfo(s);

	Assert.assertEquals("Water Survey of Canada", BNHSPropertyReader.readProperty(dataset, BNHSProperty.INSTITUTE).get());

	Assert.assertFalse(BNHSPropertyReader.readProperty(dataset, BNHSProperty.LATITUDE_OF_DISCHARGE).isPresent());

    }
}
