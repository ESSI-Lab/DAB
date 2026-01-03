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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.accessor.wof.client.datamodel.Value;

public class ANAValue extends Value {

	private String parameter;

	public ANAValue(Node node, String parameter) {
		super(node);
		this.parameter = parameter;
	}

	public String getValue() {
		try {
			return getReader().evaluateString("*:" + parameter);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getTimeUTC() throws ParseException {
		try {
			String date = getReader().evaluateString("*:DataHora");
			SimpleDateFormat toParse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			toParse.setTimeZone(TimeZone.getTimeZone("GMT-3"));
			Date parsed = toParse.parse(date.trim());
			return toParse.format(parsed.getTime());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getDateTime() {
		try {
			String date = getReader().evaluateString("*:DataHora");
			SimpleDateFormat toParse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			toParse.setTimeZone(TimeZone.getTimeZone("GMT-3"));
			Date parsed;
			try {
				parsed = toParse.parse(date.trim());
				return toParse.format(parsed.getTime());
			} catch (ParseException e) {	
				e.printStackTrace();
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

}
