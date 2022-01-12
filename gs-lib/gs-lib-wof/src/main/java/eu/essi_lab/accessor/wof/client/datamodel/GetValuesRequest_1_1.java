
package eu.essi_lab.accessor.wof.client.datamodel;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

public class GetValuesRequest_1_1 extends GetValuesRequest {

    public GetValuesRequest_1_1() {
	super(GetValuesRequest_1_1.class.getClassLoader().getResourceAsStream("cuahsi/1.1/GetValuesRequest.xml"));
    }
    
    public GetValuesRequest_1_1(InputStream stream) {
	super(stream);
    }

    public static void main(String[] args) throws Exception {
	GetValuesRequest gvr = new GetValuesRequest_1_1();
	gvr.setLocation("location");
	gvr.setVariable("variable");
	System.out.println(gvr.getReader().asString());
    }

    public void setVariable(String string) {
	try {
	    writer.setText("//*:variable", string);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    public void setLocation(String string) {
	try {
	    writer.setText("//*:location", string);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    @Override
    public void setStartDate(String string) {
	try {
	    writer.setText("//*:startDate", string);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    @Override
    public void setEndDate(String string) {
	try {
	    writer.setText("//*:endDate", string);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}

    }

    @Override
    public String getLocation() {
	try {
	    return reader.evaluateString("//*:location");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public String getVariable() {
	try {
	    return reader.evaluateString("//*:variable");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public String getStartDate() {
	try {
	    return reader.evaluateString("//*:startDate");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public String getEndDate() {
	try {
	    return reader.evaluateString("//*:endDate");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }
}
