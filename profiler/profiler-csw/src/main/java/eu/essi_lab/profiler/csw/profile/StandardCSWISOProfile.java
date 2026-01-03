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
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMD_ResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMI_ResultSetMapper;

/**
 * @author Fabrizio
 */
public class StandardCSWISOProfile extends CSWProfile {

    @Override
    public List<String> getSupportedOutputSchemas() {

	List<String> supportedOutputSchemas = new ArrayList<>();
	supportedOutputSchemas.add(CommonNameSpaceContext.GMD_NS_URI);
	supportedOutputSchemas.add(CommonNameSpaceContext.GMI_NS_URI);

	return supportedOutputSchemas;
    }

    @Override
    public List<SchemaComponentType> getSupportedSchemaComponents() {

	List<QName> supportedTypeNames = getSupportedTypeNames();
	ArrayList<SchemaComponentType> out = new ArrayList<>();
	for (QName qName : supportedTypeNames) {
	    out.add(createSchemaComponent(qName.getPrefix() + ":" + qName.getLocalPart()));
	}

	SchemaComponentType component = createSchemaComponent("srv:ServiceMetadata");
	out.add(component);

	return out;
    }

    @Override
    public List<QName> getSupportedTypeNames() {

	List<QName> supportedTypeNames = new ArrayList<>();
	supportedTypeNames.add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));
	supportedTypeNames.add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MI_Metadata", "gmi"));

	return supportedTypeNames;
    }

    @Override
    public DiscoveryResultSetMapper<Element> getResultSetMapper(String outputSchema, ElementSetType setType, List<QName> elementNames) {
	switch (outputSchema) {

	case CommonNameSpaceContext.GMD_NS_URI:

	    if (setType != null) {
		return new GMD_ResultSetMapper(setType);
	    } else if (elementNames != null) {
		return new GMD_ResultSetMapper(elementNames);
	    } else {
		return new GMD_ResultSetMapper();
	    }

	case CommonNameSpaceContext.GMI_NS_URI:

	    if (setType != null) {
		return new GMI_ResultSetMapper(setType);
	    } else if (elementNames != null) {
		return new GMI_ResultSetMapper(elementNames);
	    } else {
		return new GMI_ResultSetMapper();
	    }
	}

	return null;
    }

    @Override
    public SchemaComponentType createSchemaComponent(String typeName) {

	String targetNameSpaceUri = null;
	switch (typeName) {
	case "csw:Record":
	    targetNameSpaceUri = CommonNameSpaceContext.CSW_NS_URI;
	    break;
	case "gmd:MD_Metadata":
	    targetNameSpaceUri = CommonNameSpaceContext.GMD_NS_URI;
	    break;
	case "gmi:MI_Metadata":
	    targetNameSpaceUri = CommonNameSpaceContext.GMI_NS_URI;
	    break;
	case "srv:ServiceMetadata":
	    targetNameSpaceUri = CommonNameSpaceContext.SRV_NS_URI;
	    break;
	}

	return createSchemaComponentTypeFromNS(targetNameSpaceUri);
    }

    private SchemaComponentType createSchemaComponentTypeFromNS(String targetNameSpaceUri) {

	String name = null;

	switch (targetNameSpaceUri) {
	case CommonNameSpaceContext.GMD_NS_URI:
	    name = "GMD";
	    break;

	case CommonNameSpaceContext.GMI_NS_URI:
	    name = "GMI";
	    break;

	case CommonNameSpaceContext.SRV_NS_URI:
	    name = "SRV";
	    break;
	}

	try {
	    return CommonContext.unmarshal(
		    CSWProfile.class.getClassLoader().getResourceAsStream("templates/" + name + "SchemaComponent.xml"),
		    SchemaComponentType.class);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
}
