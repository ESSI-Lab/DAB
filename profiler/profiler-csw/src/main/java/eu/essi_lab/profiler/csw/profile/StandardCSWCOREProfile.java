package eu.essi_lab.profiler.csw.profile;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.SchemaComponentType;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.dc.DublinCore_Brief_ResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.dc.DublinCore_Full_ResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.dc.DublinCore_Summary_ResultSetMapper;

/**
 * @author Fabrizio
 */
public class StandardCSWCOREProfile extends CSWProfile {

    @Override
    public List<String> getSupportedOutputSchemas() {

	ArrayList<String> supportedOutputSchemas = new ArrayList<>();
	supportedOutputSchemas.add(CommonNameSpaceContext.CSW_NS_URI);

	return supportedOutputSchemas;
    }

    @Override
    public List<SchemaComponentType> getSupportedSchemaComponents() {

	List<QName> supportedTypeNames = getSupportedTypeNames();
	ArrayList<SchemaComponentType> out = new ArrayList<>();
	for (QName qName : supportedTypeNames) {
	    out.add(createSchemaComponent(qName.getPrefix() + ":" + qName.getLocalPart()));
	}
	return out;
    }

    @Override
    public List<QName> getSupportedTypeNames() {

	List<QName> supportedTypeNames = new ArrayList<>();
	supportedTypeNames.add(new QName(CommonNameSpaceContext.CSW_NS_URI, "Record", "csw"));

	return supportedTypeNames;
    }

    @Override
    public DiscoveryResultSetMapper<Element> getResultSetMapper(String outputSchema, ElementSetType setType, List<QName> elementNames) {

	switch (outputSchema) {
	case CommonNameSpaceContext.CSW_NS_URI:

	    if (setType == null) {
		setType = ElementSetType.FULL;
	    }

	    switch (setType) {
	    case BRIEF:
		return new DublinCore_Brief_ResultSetMapper();

	    case SUMMARY:
		return new DublinCore_Summary_ResultSetMapper();

	    case FULL:

		return new DublinCore_Full_ResultSetMapper(elementNames);
	    }
	}

	return null;
    }

    @Override
    public SchemaComponentType createSchemaComponent(String typeName) {

	if (typeName.equals("csw:Record")) {

	    try {
		return CommonContext.unmarshal(CSWProfile.class.getClassLoader().getResourceAsStream("templates/CSWSchemaComponent.xml"),
			SchemaComponentType.class);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	return null;
    }
}
