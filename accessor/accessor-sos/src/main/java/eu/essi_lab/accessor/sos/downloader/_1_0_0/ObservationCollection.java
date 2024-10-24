/**
 * 
 */
package eu.essi_lab.accessor.sos.downloader._1_0_0;

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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author Fabrizio
 */
public class ObservationCollection {

    private XMLDocumentReader reader;

    /**
     * @throws IOException
     * @throws SAXException
     */
    public ObservationCollection(InputStream document) throws SAXException, IOException {

	this.reader = new XMLDocumentReader(document);
    }

    /**
     * 
     */
    public ObservationCollection(XMLDocumentReader reader) {

	this.reader = reader;
    }

    /**
     * @return
     * @throws XPathExpressionException 
     */
    public List<DataRecord> getDataRecords() throws XPathExpressionException {
	
	Node[] values = reader.evaluateNodes("//*:DataArray/*:values/*:value");
	
	ArrayList<DataRecord> list = new ArrayList<DataRecord>();
	
	for (Node node : values) {
	    
	    String dateTime = reader.evaluateString(node, "@dateTime");
	    String value = reader.evaluateString(node, ".");

	    list.add(new DataRecord(dateTime, new BigDecimal(value)));	
	}

	return list;
    }
    
    

}
