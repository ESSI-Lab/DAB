package eu.essi_lab.accessor.ana;

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

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class StationDocument extends XMLDocumentReader {
	
	
	private ANAVariable variable = null;

	public StationDocument(InputStream stream) throws SAXException, IOException {
		super(stream);
	}
	
	public StationDocument(InputStream stream, ANAVariable variable) throws SAXException, IOException {
		super(stream);
		this.setVariable(variable);
	}

//	public StationDocument(Node node) {
//		super(node);
//	}
//	
//	public StationDocument(String nodeString) {
//		//super(node);
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
//        DocumentBuilder builder; 
//        try  
//        {  
//            builder = factory.newDocumentBuilder();  
//            Document doc = builder.parse( new InputSource( new StringReader( nodeString ) ) );
//            this.targetNode = doc.getDocumentElement(); 
//            		//doc.cloneNode(true);
//           
//        } catch (Exception e) {  
//            e.printStackTrace();  
//        } 
//
// 		
//	}

	public String getStationName() {
		return getProperty("//*:NomeEstacao");
	}

	public String getStationCode() {
		return getProperty("//*:CodEstacao");
	}

	public String getBasin() {
		return getProperty("//*:Bacia");
	}

	public String getSubBasin() {
		return getProperty("//*:SubBacia");
	}

	public String getOperator() {
		return getProperty("//*:Operadora");
	}

	public String getResponsible() {
		return getProperty("//*:Responsavel");
	}

	public String getCity() {
		return getProperty("//*:Municipio-UF");
	}

	public String getLatitude() {
		return getProperty("//*:Latitude");
	}

	public String getLongitude() {
		return getProperty("//*:Longitude");
	}

	public String getAltitude() {
		return getProperty("//*:Altitude");
	}

	public String getRiverCode() {
		return getProperty("//*:CodRio");
	}

	public String getRiverName() {
		return getProperty("//*:NomeRio");
	}

	public String getOrigin() {
		return getProperty("//*:Origem");
	}

	public String getStationStatus() {
		return getProperty("//*:StatusEstacao");
	}

	private String getProperty(String property) {
		try {
			return evaluateString(property);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public ANAVariable getVariable() {
		return variable;
	}

	public void setVariable(ANAVariable variable) {
		this.variable = variable;
	}

}
