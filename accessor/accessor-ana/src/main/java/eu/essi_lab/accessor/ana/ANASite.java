package eu.essi_lab.accessor.ana;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class ANASite extends XMLDocumentReader {

	public ANASite(InputStream stream) throws SAXException, IOException {
		super(stream);
	}

	public String getState() {
		return getProperty("nmEstado");
	}

	public String getCity() {
		return getProperty("nmMunicipio");
	}

	public String getResponsible() {
		return getProperty("ResponsavelSigla");
	}

	public String getResponsibleUnit() {
		return getProperty("ResponsavelUnidade");
	}

	public String getResponsibleJurisdiction() {
		return getProperty("ResponsavelJurisdicao");
	}

	public String getOperator() {
		return getProperty("OperadoraSigla");
	}

	public String getOperatorUnit() {
		return getProperty("OperadoraUnidade");
	}

	public String getOperatorSubUnit() {
		return getProperty("OperadoraSubUnidade");
	}

	public String getStationType() {
		return getProperty("TipoEstacao");
	}

	public String getStationName() {
		return getProperty("nmEstado");
	}

	public String getLatitude() {
		return getProperty("Latitude");
	}

	public String getLongitue() {
		return getProperty("Longitude");
	}

	public String getAltitude() {
		return getProperty("Altitude");
	}

	public String getDescription() {
		return getProperty("Descricao");
	}

	private String getProperty(String property) {
		try {
			return evaluateString("*:DataSet/*:diffgram/*:Estacoes/*:Table/*:" + property);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}

}
