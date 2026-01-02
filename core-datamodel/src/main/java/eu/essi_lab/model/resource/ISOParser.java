package eu.essi_lab.model.resource;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class ISOParser {

    /**
     * Copy and paste the output in {@link Country}!
     * @throws Exception
     */
    public static void generateCountryEnum() throws Exception {

	XMLDocumentReader reader = new XMLDocumentReader(ISOParser.class.getClassLoader().getResourceAsStream("ISO/countries.xml"));
	Node[] rows = reader.evaluateNodes("//*:tr[*:td]");
	String enumName = "Country";
	System.out.println("package eu.essi_lab.model.resource;");
	System.out.println("public enum " + enumName + "{");
	for (int i = 0; i < rows.length; i++) {
	    Node row = rows[i];
	    String shortName = reader.evaluateString(row, "*:td[@class='shortname']/*:a");
	    String officialName = reader.evaluateString(row, "*:td[@class='official']");
	    String iso3 = reader.evaluateString(row, "*:td[@class='iso3']");
	    String iso2 = reader.evaluateString(row, "*:td[@class='iso2']");
	    System.out.print(
		    shortName.replace(" ", "_").replace("'", "").replace("(", "").replace(")", "").replace("-", "").trim().toUpperCase()
			    + "(\"" + shortName + "\",\"" + officialName + "\",\"" + iso3 + "\",\"" + iso2 + "\")");

	    if (i == rows.length - 1) {
		System.out.println(";");
	    } else {
		System.out.println(", //");
	    }

	}
	System.out.println("    private String shortName;\r\n" + 
		"    private String officialName;\r\n" + 
		"    private String iso3;\r\n" + 
		"    private String iso2;\r\n" + 
		"\r\n" + 
		"    public String getShortName() {\r\n" + 
		"	return shortName;\r\n" + 
		"    }\r\n" + 
		"\r\n" + 
		"    public String getOfficialName() {\r\n" + 
		"	return officialName;\r\n" + 
		"    }\r\n" + 
		"\r\n" + 
		"    public String getISO3() {\r\n" + 
		"	return iso3;\r\n" + 
		"    }\r\n" + 
		"\r\n" + 
		"    public String getISO2() {\r\n" + 
		"	return iso2;\r\n" + 
		"    }\r\n" + 
		"\r\n" + 
		"    private "+enumName+"(String shortName, String officialName, String iso3, String iso2) {\r\n" + 
		"	this.shortName = shortName;\r\n" + 
		"	this.officialName = officialName;\r\n" + 
		"	this.iso3 = iso3;\r\n" + 
		"	this.iso2 = iso2;\r\n" + 
		"    }\r\n" + 
		"\r\n" + 
		"    public static Country decode(String country) {\r\n" + 
		"	if (country == null) {\r\n" + 
		"	    return null;\r\n" + 
		"	}\r\n" + 
		"	for (Country c : values()) {\r\n" + 
		"	    if (country.equalsIgnoreCase(c.getShortName()) || country.equalsIgnoreCase(c.getOfficialName())\r\n" + 
		"		    || country.equalsIgnoreCase(c.getISO3()) || country.equalsIgnoreCase(c.getISO2())) {\r\n" + 
		"		return c;\r\n" + 
		"	    }\r\n" + 
		"	}\r\n" + 
		"	return null;\r\n" + 
		"    }");//
	System.out.println("}");

    }

    public static void main(String[] args) throws Exception {

	ISOParser.generateCountryEnum();

    }
}
