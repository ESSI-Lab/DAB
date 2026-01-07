package eu.essi_lab.accessor.wof.client.datamodel;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TimeSeriesINAResponseDocument extends TimeSeriesResponseDocument{

    public TimeSeriesINAResponseDocument(Document doc) {
	super(doc);
    }
    
    public TimeSeriesINAResponseDocument(InputStream inputStream) throws SAXException, IOException {
	super(inputStream);
    }

    public void reduceValues(String methodId, String qualityControlLevelCode, String sourceId) {
  	List<String> constraints = new ArrayList<>();
  	if (methodId != null && !methodId.isEmpty()) {
  	    constraints.add("@methodID!='" + methodId + "'");
  	}
  	if (qualityControlLevelCode != null && !qualityControlLevelCode.isEmpty()) {
  	    constraints.add("@qualityControlLevelCode!='" + qualityControlLevelCode + "'");
  	}
  	if (sourceId != null && !sourceId.isEmpty()) {
  	    constraints.add("@sourceID!='" + sourceId + "'");
  	}
  	if (!constraints.isEmpty()) {

  	    String c = "";
  	    String or = " or ";
  	    for (int i = 0; i < constraints.size(); i++) {
  		c += constraints.get(i) + or;
  	    }
  	    c = c.substring(0, c.lastIndexOf(or));

  	    try {
  		writer.remove("//*:value[" + c + "]");
  	    } catch (XPathExpressionException e) {
  		e.printStackTrace();
  	    }
  	}

      }

}
