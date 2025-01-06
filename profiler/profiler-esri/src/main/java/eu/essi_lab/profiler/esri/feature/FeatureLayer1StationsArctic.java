package eu.essi_lab.profiler.esri.feature;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.profiler.esri.feature.query.FeatureQueryResultSetFormatter;

/**
 * @author boldrini
 */
public class FeatureLayer1StationsArctic extends FeatureLayer0Stations {

    @Override
    public List<Field> getFields() {
	List<Field> ret = new ArrayList<>();
	// ret.add(new Field("stationId", ESRIFieldType.OID, "Station ID", null));
	// ret.add(new Field("datetimeStart", ESRIFieldType.START_DATE, "Date Start", null));
	// ret.add(new Field("datetimeEnd", ESRIFieldType.END_DATE, "Date End", null));
	ret.add(new Field("whosPage", ESRIFieldType.WHOS_PAGE, "WHOS page", null, null));
	for (BNHSProperty property : BNHSProperty.values()) {
	    ESRIFieldType type = ESRIFieldType.decode(property.getType());
	    if (property.equals(BNHSProperty.LATITUDE)) {
		type = ESRIFieldType.LATITUDE;
	    }
	    if (property.equals(BNHSProperty.LONGITUDE)) {
		type = ESRIFieldType.LONGITUDE;
	    }
	    String id = property.name().toLowerCase();
	    Field field = new Field(id, type, property.getLabel(), null, property.getElement());
	    field.setBNHSProperty(property);
	    ret.add(field);

	}
	return ret;
    }

    @Override
    public synchronized Long getResourceIdentifier(String view, XMLDocumentReader reader) {
	String bnhs;
	try {
	    bnhs = reader.evaluateString("//*:" + MetadataElement.BNHS_INFO_EL_NAME + "[1]");
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	    return null;
	}
	HashMap<String, String> bnhsMap = new HashMap<>();
	if (bnhs != null) {
	    String[] split = bnhs.split(FeatureQueryResultSetFormatter.BNHS_SEPARATOR);
	    for (int i = 0; i < split.length - 2; i += 2) {
		String column = split[i];
		String value = split[i + 1];
		bnhsMap.put(column, value);
	    }
	}
	String str = bnhsMap.get(BNHSProperty.HYCOSID.getLabel());

	try {
	    return Long.parseLong(str);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unparsable id: {}", str);
	}
	return null;

    }

    @Override
    public String getId() {
	return "1";
    }

    @Override
    public String getName() {
	return "Stations (WHOS-Arctic)";
    }

    @Override
    public String getDescription() {
	return "Stations from the current DAB view, with attributes for WHOS-Arctic";
    }

}
