package eu.floraresearch.drm;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.floraresearch.drm.report.Report;

public class TableManager {

    private DecimalFormat dm;

    public TableManager() {

	dm = new DecimalFormat();
	dm.setGroupingUsed(true);
    }

    public String createTable(List<Report> reports) {

	String table = getTableInitTags();

	int total = 0;
	int totalGDC = 0;
	int totalElements = 0;
	int totalGDCElements = 0;

	int totalValid = 0;
	int totalNotValid = 0;
	int totalRepaired = 0;
	int totalNotRepaired = 0;

	int totalNew = 0;
	int totalDeleted = 0;
	int totalNotValidated = 0;
	int count = 1;

	for (Report report : reports) {
	    try {

		String name = report.getCompleteName();

		int totalRecords = ConfigReader.getInstance().readShowCategory(1) ? report.getCategory_1_Value() : 0;
		int gdcRecords = ConfigReader.getInstance().readShowCategory(2) ? report.getCategory_2_Value() : 0;
		int elements = ConfigReader.getInstance().readShowCategory(3) ? report.getCategory_3_Value() : 0;
		int gdcElements = ConfigReader.getInstance().readShowCategory(4) ? report.getCategory_4_Value() : 0;

		String type = ConfigReader.getInstance().readShowCategory(5) ? report.getCategory_5_Value() : "";
		int valid = ConfigReader.getInstance().readShowCategory(6) ? report.getCategory_6_Value() : 0;
		int notValid = ConfigReader.getInstance().readShowCategory(7) ? report.getCategory_7_Value() : 0;
		int repaired = ConfigReader.getInstance().readShowCategory(8) ? report.getCategory_8_Value() : 0;
		int notRepaired = ConfigReader.getInstance().readShowCategory(9) ? report.getCategory_9_Value() : 0;

		int newRecords = ConfigReader.getInstance().readShowCategory(10) ? report.getCategory_10_Value() : 0;
		int delRecords = ConfigReader.getInstance().readShowCategory(11) ? report.getCategory_11_Value() : 0;
		int notValidated = ConfigReader.getInstance().readShowCategory(12) ? report.getCategory_12_Value() : 0;

		String comments = report.getComments();
		// String href = "cat=GDC&src=" + URLEncoder.encode(name, "UTF-8");

		table += getTableRowTags(name, totalRecords, gdcRecords, elements, gdcElements, type, valid, notValid, repaired,
			notRepaired, newRecords, delRecords, notValidated, comments, count, report.underTest());

		total += totalRecords;
		totalGDC += gdcRecords;
		totalElements += elements;
		totalGDCElements += gdcElements;

		totalValid += valid;
		totalNotValid += notValid;
		totalRepaired += repaired;
		totalNotRepaired += notRepaired;

		totalNew += newRecords;
		totalDeleted += delRecords;
		totalNotValidated += notValidated;

		count++;

	    } catch (Exception ex) {
		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    }
	}

	table += getTotalTableRowTag(total, totalGDC, totalElements, totalGDCElements, totalValid, totalNotValid, totalRepaired,
		totalNotRepaired, totalNew, totalDeleted, totalNotValidated, "");

	return table;
    }

    // @formatter:off
    private String getTableInitTags() {

	return "<table id=\"table\"><caption>Capacities Under Test are shown in red</caption>"

		+ "<tr>"
		+ "<th style=\"text-align:center; \"></th>"
		+ "<th style=\"text-align:center; \">Brokered source</th>"

		+ (ConfigReader.getInstance().readShowCategory(5) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(5) + "</th>"): "")

		+ (ConfigReader.getInstance().readShowCategory(1) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(1) + "</th>"): "")
		
		+ (ConfigReader.getInstance().readShowCategory(2) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(2) + "</th>"): "")
	
			
		+ (ConfigReader.getInstance().readShowCategory(3) ? ("<th style=\"background-color:#4FBC69\">"+ ConfigReader.getInstance().readCategory_Label(3) + "</th>") : "")
	
		+ (ConfigReader.getInstance().readShowCategory(4) ? ("<th style=\"background-color:#648CCC\">"+ ConfigReader.getInstance().readCategory_Label(4) + "</th>") : "")

		
//		+ (ConfigReader.getInstance().readShowCategory(6) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(6) + "</th>")
//			: "")
//		+ (ConfigReader.getInstance().readShowCategory(7) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(7) + "</th>")
//			: "")
//		+ (ConfigReader.getInstance().readShowCategory(8) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(8) + "</th>")
//			: "")
//		+ (ConfigReader.getInstance().readShowCategory(9) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(9) + "</th>")
//			: "")
//
//		+ (ConfigReader.getInstance().readShowCategory(10) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(10) + " "+ ConfigReader.getInstance().readNewFromDate() + "</th>") : "")
//		
//		+ (ConfigReader.getInstance().readShowCategory(11) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(11) + "</th>") : "")

//		+ (ConfigReader.getInstance().readShowCategory(12) ? ("<th>" + ConfigReader.getInstance().readCategory_Label(12) + "</th>") : "")

		+ (ConfigReader.getInstance().readShowComments() ? ("<th>Comments</th>") : "") + "</tr> ";
    }

    private String getTableRowTags(String source, int total, int gdc, int elements, int gdcElements, String type, int valid, int notValid,
	    int repaired, int notRepaired, int newRecords, int delRecords, int notValidated, String comment, int count, boolean test) {

	String gran = elements > 0 ? dm.format(elements) : "0";

	return "<tr>"
		
		+ "<td>"+count+"</td>"

		+ "<td style=\"color: "
		+ (test ? "red" : "black")
		+ ";\">"

		+ source
		+ "</td>"

		+ (ConfigReader.getInstance().readShowCategory(5) ? ("<td>" + type + "</td>") : "")

		+ (ConfigReader.getInstance().readShowCategory(1) ? ("<td>" + dm.format(total) + "</td>"): "")
		
		+ (ConfigReader.getInstance().readShowCategory(2) ? ("<td>" + dm.format(gdc) + "</td>"): "") 
		
		+ (ConfigReader.getInstance().readShowCategory(3) ? ("<td style=\"background-color:#98EBAC\">" + gran + "</td>") : "")
		
		+ (ConfigReader.getInstance().readShowCategory(4) ? ("<td style=\"background-color:#BEC2EF\">" + dm.format(gdcElements) + "</td>") : "")

//		+ (ConfigReader.getInstance().readShowCategory(6) ? ("<td>" + dm.format(valid) + "</td>") : "")
//		+ (ConfigReader.getInstance().readShowCategory(7) ? ("<td>" + dm.format(notValid) + "</td>") : "")
//		+ (ConfigReader.getInstance().readShowCategory(8) ? ("<td>" + dm.format(repaired) + "</td>") : "")
//		+ (ConfigReader.getInstance().readShowCategory(9) ? ("<td>" + dm.format(notRepaired) + "</td>") : "")
//
//		+ (ConfigReader.getInstance().readShowCategory(10) ? ("<td>" + dm.format(newRecords) + "</td>") : "")
//		+ (ConfigReader.getInstance().readShowCategory(11) ? ("<td>" + dm.format(delRecords) + "</td>") : "")
		
//		+ (ConfigReader.getInstance().readShowCategory(12) && notValidated > 0? ("<td>" + dm.format(notValidated) + "</td>") : "")

		+ (ConfigReader.getInstance().readShowComments() ? ("<td>" + comment + "</td>") : "") + "</tr>";
    }

    private String getTotalTableRowTag(int total, int totalGdc, int elements, int totalElements, int totalValid, int totalNotValid,

    int totalRepaired, int totalNotRepaired, int totalNew, int totalDeleted, int totalNotValidated, String comment) {

	return "<tr style=\"border:2px solid white;border-top:12px solid white;padding:3px 7px 6px 7px;\">"
		+ "<td></td>"
		+ "<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px; \">Total</td>"

		+ (ConfigReader.getInstance().readShowCategory(5) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\"></td>"): "")

		+ (ConfigReader.getInstance().readShowCategory(1) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(total) + "</td>")
			: "")
		+ (ConfigReader.getInstance().readShowCategory(2) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalGdc) + "</td>")
			: "")
		+ (ConfigReader.getInstance().readShowCategory(3) ? ("<td style=\"background-color:#98EBAC; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(elements) + "</td>")
			: "")
		+ (ConfigReader.getInstance().readShowCategory(4) ? ("<td style=\"background-color:#BEC2EF; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalElements) + "</td>")
			: "")

		+ (ConfigReader.getInstance().readShowCategory(6) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalValid) + "</td>")
			: "")
		+ (ConfigReader.getInstance().readShowCategory(7) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalNotValid) + "</td>")
			: "")
		+ (ConfigReader.getInstance().readShowCategory(8) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalRepaired) + "</td>")
			: "")
		+ (ConfigReader.getInstance().readShowCategory(9) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalNotRepaired) + "</td>")
			: "")

		+ (ConfigReader.getInstance().readShowCategory(10) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalNew) + "</td>")
			: "")
		+ (ConfigReader.getInstance().readShowCategory(11) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalDeleted) + "</td>")
			: "")
			
		+ (ConfigReader.getInstance().readShowCategory(12) ? ("<td style=\"background-color:#A7C942; font-weight:bold;padding:6px 7px 6px 7px;\">"
			+ dm.format(totalNotValidated) + "</td>")
			: "")

		+ (ConfigReader.getInstance().readShowComments() ? ("<td style=\"background-color:#A7C942; padding:6px 7px 6px 7px;\">"
			+ comment + "</td>") : "")

		+ "</tr></table>";
    }

    public String getUOSPublishedTag() {
	int uosLogoMargin = ConfigReader.getInstance().readUOSLogoMargin();
	String label = "<div><label style=\"font-style: italic; font-weight: bold; margin-top: 10px; margin-left: " + uosLogoMargin
		+ "px; font-style: italic\">Published by </label>";
	String img = "<a href=\"http://www.uos-firenze.iia.cnr.it/\" target=\"_blank\"><img src=\"img/UOS.png\"></img></a></div>";

	return label + img;
    }

//    private String getLogoTableTag() {
//
//	return "<table>"
//		+ "<tr>"
//		+ "<td><a href=\"http://www.earthobservations.org/\" target=\"_blank\"><img src=\"img/GEO.png\"></img></a></td>"
//		+ "<td><a href=\"https://www.earthobservations.org/geoss.shtml\" target=\"_blank\"><img style=\"margin-left: 600px\" src=\"img/GEO_DAB.png\"></img></a></td>"
//		+ "</tr>"
//		+
//
//		"<tr>"
//		+ "<td><label style=\"font-size: 60px;\">Date "
//		+ ISO8601DateTimeUtils.getISO8601DateTime(Calendar.getInstance().getTime())
//		+ "</label></td>"
//		+
//		//		    "<td></td>"+
//		"<td>"
//		+ "<a href=\"http://ec.europa.eu/dgs/jrc/\" target=\"_blank\"><img style=\"margin-left: 555px;\" src=\"img/JRC.png\"></img></a>"
//		+ "<a href=\"http://www.esa.int/ESA\" target=\"_blank\"><img style=\"margin-left: 5px;\" src=\"img/ESA.png\"></img></a> "
//		+ "<a href=\"http://www.usgs.gov/\" target=\"_blank\"><img style=\"margin-left: 5px;\" src=\"img/USGS.png\"></img></a> "
//		+ "<a href=\"http://www.u-tokyo.ac.jp/en/\" target=\"_blank\"><img style=\"margin-left: 5px;\" src=\"img/TOKIO.png\"></img></a> "
//		+ "<a href=\"http://www.iia.cnr.it/\" target=\"_blank\"><img style=\"margin-left: 5px;\" src=\"img/CNR.png\"></img></a></td>"
//		+ "</tr>" + "</table>";
//    }
//
//    private String getRotateStyle() {
//
//	return ".rotation {" + "font-size: 100px;" + "width: 750px;" + "position: absolute;" + "margin-left: -330px;"
//		+ "margin-top: 315px;" + "-webkit-transform:  rotate(90deg);" + "-moz-transform: rotate(90deg);"
//		+ "-o-transform: rotate(90deg);" + "writing-mode: lr-tb;" + "}";
//    }

    public String getTableStyle() {

	return "<style>"

	+ "#table" + "{" + "	font-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif;" + "	width:"
		+ ConfigReader.getInstance().readGraphWidth() + "px;" + "	border-collapse:collapse;" + "	margin-top: 10px;" + "}"

		+ "#table td, #table th " + "{" + "	font-size:0.8em;" + "	border:2px solid white;" + "	padding:3px 7px 2px 7px;" + "}"

		+ "#table th " + "{" + "	font-size:1em;" + "	text-align:left;" + "	padding:3px;" + "	background-color:#A7C942;"
		+ "	color:#fff;" + "}"

		+ "#table td " + "{" + "	color:#000;" + "	background-color:#F5F9EA;" + "}	\n\n"
		//		+ getRotateStyle()		
		+ "</style>";
    }

}
