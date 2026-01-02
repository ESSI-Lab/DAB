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
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.Value;
import eu.essi_lab.model.resource.data.Unit;


public class ANATimeSeriesResponse extends TimeSeriesResponseDocument {

	private StationDocument station;
	private String parameter;

	public ANATimeSeriesResponse(InputStream stream) throws SAXException, IOException {
		super(stream);
	}

	public ANATimeSeriesResponse(InputStream stream, StationDocument station, String parameter)
			throws SAXException, IOException {
		super(stream);
		this.station = station;
		this.parameter = parameter;
	}

	public String getSize() throws XPathExpressionException {

		return getReader().evaluateString("count(//*:DadosHidrometereologicos)");

	}

	public String getSiteName() throws XPathExpressionException {

		return station.getStationName();

	}

	public String getSiteCode() throws XPathExpressionException {

		return station.getStationCode();

	}

	public String getSiteNetwork() throws XPathExpressionException {

		return "ANA";

	}

	public String getLatitude() throws XPathExpressionException {

		return station.getLatitude();

	}

	public String getLongitude() throws XPathExpressionException {

		return station.getLongitude();

	}

	public String getSiteProperty(String propertyName) throws XPathExpressionException {

		return "";
	}

	public String getVariableCode() throws XPathExpressionException {

		return parameter;

	}

	public String getVariableVocabulary() throws XPathExpressionException {

		return "ANA";

	}

	public String getVariableName() throws XPathExpressionException {

		return parameter;

	}

	public String getValueType() throws XPathExpressionException {

		return "Unknown";

	}

	public String getDataType() throws XPathExpressionException {

		return "Unknown";

	}

	public String getGeneralCategory() throws XPathExpressionException {

		return "Hydrology";

	}

	public String getSampleMedium() throws XPathExpressionException {

		return "Unknown";

	}

	public List<Unit> getUnits() throws XPathExpressionException {

		List<Unit> ret = new ArrayList<>();
		Unit unit = new ANAUnit(parameter);
		ret.add(unit);
		return ret;

	}

	public List<? extends Value> getValues() {

		List<ANAValue> ret = new ArrayList<ANAValue>();

		List<Node> nodes;
		try {
			nodes = getReader().evaluateOriginalNodesList(("//*:DadosHidrometereologicos"));
			for (Node node : nodes) {
				ANAValue v = new ANAValue(node, parameter);
				ret.add(v);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return ret;

	}

	public String getOrganization() throws XPathExpressionException {

		return station.getResponsible();

	}

	public String getSourceDescription() throws XPathExpressionException {

		return station.getResponsible();

	}

	public String getContactName() throws XPathExpressionException {

		return station.getResponsible();

	}

	public String getEmail() throws XPathExpressionException {

		return "";

	}

	public String getPhone() throws XPathExpressionException {

		return "";

	}

	public String getAddress() throws XPathExpressionException {

		return "";

	}

	/**
	 * Return the first non temporal unit
	 * 
	 * @return
	 */
	public Unit getMainUnit() {
		try {
			return getUnits().get(0);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

}
