package eu.essi_lab.messages.bond.jaxb;

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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.spatial.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.lib.xml.*;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.messages.bond.ViewBond;
import org.json.*;

import java.io.*;
import java.nio.charset.*;

/**
 *
 */
public class ViewFactory {

    /**
     *
     */
    private static JAXBContext jaxbContext;

    /**
     *
     */
    public static void initContext() {

	try {
	    jaxbContext = JAXBContext.newInstance(//
		    View.class, //
		    WKT.class, //
		    ViewBond.class, //
		    LogicalBond.class, //
		    ResourcePropertyBond.class, //
		    SimpleValueBond.class, //
		    SpatialBond.class);

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(ViewFactory.class).error(e);
	}
    }

    /**
     *
     */
    private ViewFactory() {
    }

    /**
     * @return
     */
    public static Marshaller createMarshaller() {

	synchronized (ViewFactory.class) {

	    try {

		Marshaller m = getContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());

		return m;

	    } catch (JAXBException e) {

		GSLoggerFactory.getLogger(ViewFactory.class).error(e);
	    }
	    return null;
	}
    }

    /**
     * @return
     */
    public static Unmarshaller createUnmarshaller() {

	synchronized (ViewFactory.class) {

	    try {
		return getContext().createUnmarshaller();

	    } catch (JAXBException e) {

		GSLoggerFactory.getLogger(ViewFactory.class).error(e);
	    }
	    return null;
	}
    }

    /**
     * @param obj
     * @return
     * @throws JsonProcessingException
     */
    public static View fromJSONObject(String obj) throws JsonProcessingException {

	ObjectMapper jsonMapper = new ObjectMapper();
	jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	return jsonMapper.readValue(obj, View.class);
    }

    /**
     * @param obj
     * @return
     * @throws JsonProcessingException
     */
    public static View fromJSONObject(JSONObject obj) throws JsonProcessingException {

	ObjectMapper jsonMapper = new ObjectMapper();
	jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	return jsonMapper.readValue(obj.toString(), View.class);
    }

    /**
     * @param stream
     * @return
     * @throws JsonProcessingException
     */
    public static View fromJSONStream(InputStream stream) throws IOException {

	ObjectMapper jsonMapper = new ObjectMapper();
	jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	String utf8String = IOStreamUtils.asUTF8String(stream);

	return jsonMapper.readValue(utf8String, View.class);
    }

    /**
     * @param view
     * @return
     */
    public static JSONObject toJSONObject(View view) throws JsonProcessingException {

	ObjectMapper jsonMapper = new ObjectMapper();
	jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	return new JSONObject(jsonMapper.writeValueAsString(view));
    }

    /**
     * @param view
     * @return
     */
    public static String toXMLString(View view) throws JAXBException {

	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	ViewFactory.createMarshaller().marshal(view, stream);
	return stream.toString(StandardCharsets.UTF_8);
    }

    /**
     * @param is
     * @return
     * @throws JAXBException
     */
    public static View fromXMLString(String stringView) throws JAXBException {

	Unmarshaller unmarshaller = createUnmarshaller();

	return (View) unmarshaller.unmarshal(new ByteArrayInputStream(stringView.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * @param streamView
     * @return
     * @throws JAXBException
     */
    public static View fromXMLStream(InputStream streamView) throws JAXBException {

	Unmarshaller unmarshaller = createUnmarshaller();

	return (View) unmarshaller.unmarshal(streamView);
    }

    /**
     * @param id
     * @param label
     * @param bond
     * @return
     */
    public static View createView(String id, String label, Bond bond) {

	return createView(id, label, bond, null, null, null, null);
    }

    /**
     * @param id
     * @param label
     * @param bond
     * @return
     */
    public static View createView(String id, Bond bond) {

	return createView(id, null, bond, null, null, null, null);
    }

    /**
     * @param id
     * @param label
     * @param creator
     * @param bond
     * @return
     */
    public static View createView(String id, String label, String creator, Bond bond) {

	return createView(id, label, bond, creator, null, null, null);
    }

    /**
     * @param id
     * @param label
     * @param creator
     * @param bond
     * @param viewVisibility
     * @return
     */
    public static View createView(String id, String label, String creator, Bond bond, ViewVisibility viewVisibility) {

	return createView(id, label, bond, creator, null, viewVisibility, null);
    }

    /**
     * @param id
     * @param label
     * @param creator
     * @param owner
     * @param bond
     * @param viewVisibility
     * @return
     */
    public static View createView(String id, String label, String creator, String owner, Bond bond, ViewVisibility viewVisibility) {

	return createView(id, label, bond, creator, owner, viewVisibility, null);
    }

    /**
     * @param id
     * @param label
     * @param bond
     * @param creator
     * @param owner
     * @param viewVisibility
     * @return
     */
    public static View createView(//
	    String id, //
	    String label, //
	    Bond bond, //
	    String creator, //
	    String owner, //
	    ViewVisibility viewVisibility, //
	    String sourceDeployment) {

	View ret = new View();
	ret.setId(id);
	if (label != null) {
	    ret.setLabel(label);
	}
	ret.setBond(bond);
	ret.setCreator(creator);
	ret.setOwner(owner);
	if (viewVisibility != null) {
	    ret.setVisibility(viewVisibility);
	}
	if (sourceDeployment != null) {
	    ret.setSourceDeployment(sourceDeployment);
	}
	return ret;
    }

    /**
     * @return
     */
    private static JAXBContext getContext() {

	synchronized (ViewFactory.class) {
	    if (jaxbContext == null) {

		initContext();
	    }

	    return jaxbContext;

	}
    }
}
