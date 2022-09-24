package eu.essi_lab.accessor.wof.info.datamodel;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

/**
 * @author boldrini
 */
public class ArrayOfSeriesRecord {

    private XMLDocumentReader reader;
    private XMLDocumentWriter writer;

    public ArrayOfSeriesRecord() throws Exception {
	this.reader = new XMLDocumentReader(
		ArrayOfSeriesRecord.class.getClassLoader().getResourceAsStream("cuahsi/ArrayOfSeriesRecord.xml"));
	this.writer = new XMLDocumentWriter(reader);
    }

    public void addSeriesRecord(String serverCode, String serverURL, String location, String varCode, String varName, String beginDate,
	    String endDate, String valueCount, String siteName, String latitude, String longitude, String dataType, String valueType,
	    String sampleMedium, String timeUnits, String conceptKeyword, String genCategory, String timeSupport) throws Exception {
	String xmlString = "<SeriesRecord xmlns=\"http://hiscentral.cuahsi.org/20100205/\">" + //
		"<ServCode>" + serverCode + "</ServCode>" + //
		"<ServURL>" + serverURL + "</ServURL>" + //
		"<location>" + location + "</location>" + //
		"<VarCode>" + varCode + "</VarCode>" + //
		"<VarName>" + varName + "</VarName>" + //
		"<beginDate>" + beginDate + "</beginDate>" + //
		"<endDate>" + endDate + "</endDate>" + //
		"<ValueCount>" + valueCount + "</ValueCount>" + //
		"<Sitename>" + siteName + "</Sitename>" + //
		"<latitude>" + latitude + "</latitude>" + //
		"<longitude>" + longitude + "</longitude>" + //
		"<datatype >" + dataType + "</datatype>" + //
		"<valuetype>" + valueType + "</valuetype>" + //
		"<samplemedium>" + sampleMedium + "</samplemedium>" + //
		"<timeunits>" + timeUnits + "</timeunits>" + //
		"<conceptKeyword>" + conceptKeyword + "</conceptKeyword>" + //
		"<genCategory>" + genCategory + "</genCategory>" + //
		"<TimeSupport>" + timeSupport + "</TimeSupport>" + //
		"</SeriesRecord>";
	XMLDocumentReader reader = new XMLDocumentReader(xmlString);
	this.writer.addNode("/*[1]", reader.getDocument().getDocumentElement());
    }

    public String asString() throws UnsupportedEncodingException, TransformerException {
	return this.reader.asString();
    }
}
