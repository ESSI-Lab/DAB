package eu.essi_lab.iso.datamodel.classes;

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

import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.lib.xml.*;
import org.w3c.dom.*;

import javax.xml.bind.*;
import javax.xml.namespace.*;
import java.io.*;
import java.nio.charset.*;

/**
 * @author Fabrizio
 */
public class EXT_Online extends Online {

    private static JAXBContext context;

    static {
	try {
	    context = JAXBContext.newInstance(EXT_CIOnlineResourceType.class);
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(EXT_Online.class).error(e);
	}
    }

    /**
     * @param online
     * @throws JAXBException
     */
    public EXT_Online(Online online) {

	super(new EXT_CIOnlineResourceType());

	if (online.getLinkage() != null) {

	    setLinkage(online.getLinkage());
	}

	if (online.getName() != null) {

	    setName(online.getName());
	}

	if (online.getDescription() != null) {

	    setDescription(online.getDescription());
	}

	if (online.getIdentifier() != null) {

	    setIdentifier(online.getIdentifier());
	}

	if (online.getProtocol() != null) {

	    setProtocol(online.getProtocol());
	}

	if (online.getFunctionCode() != null) {

	    setFunctionCode(online.getFunctionCode());
	}

	if (online.getApplicationProfile() != null) {

	    setApplicationProfile(online.getApplicationProfile());
	}

	if (online.getDescriptionGmxAnchor() != null) {

	    setDescriptionGmxAnchor(online.getDescriptionGmxAnchor());
	}

	if (online.getProtocolGmxAnchorHref() != null) {

	    setProtocolAnchor(online.getProtocolGmxAnchorHref(), online.getProtocolGmxAnchorTitle());
	}
    }

    /**
     * @param stream
     * @throws JAXBException
     */
    public EXT_Online(InputStream stream) throws JAXBException {

	super(stream);
    }

    /**
     * @param type
     */
    public EXT_Online(EXT_CIOnlineResourceType type) {

	super(type);
    }

    /**
     *
     */
    public EXT_Online() {

	super(new EXT_CIOnlineResourceType());
    }

    @Override
    public EXT_CIOnlineResourceType getElementType() {

	return (EXT_CIOnlineResourceType) this.type;
    }

    /**
     * @return
     */
    public JAXBElement<EXT_CIOnlineResourceType> getExtendedElement() {

	QName qName = new QName(//
		"http://www.isotc211.org/2005/gmd", //
		"EXT_CIOnlineResource");//

	return new JAXBElement<EXT_CIOnlineResourceType>( //
		qName, //
		EXT_CIOnlineResourceType.class, //
		null,//
		getElementType());//
    }

    @Override
    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {

	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty("jaxb.formatted.output", true);
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());
	return marshaller;
    }

    @Override
    public void toStream(OutputStream out, boolean omitXMLdeclaration) throws JAXBException {

	Marshaller marshaller = createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, omitXMLdeclaration);
	marshaller.marshal(getExtendedElement(), out);
    }

    @Override
    public String asString(boolean omitXMLdeclaration) throws JAXBException, UnsupportedEncodingException {

	Marshaller marshaller = createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, omitXMLdeclaration);

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	marshaller.marshal(getExtendedElement(), outputStream);

	return outputStream.toString(StandardCharsets.UTF_8).trim();
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public EXT_Online ext_fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return new EXT_Online((EXT_CIOnlineResourceType) unmarshaller.unmarshal(stream));
    }

    /**
     * @param node
     * @return
     * @throws JAXBException
     */
    public EXT_Online ext_fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return new EXT_Online((EXT_CIOnlineResourceType) unmarshaller.unmarshal(node));
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static EXT_Online create(InputStream stream) throws JAXBException {

	return new EXT_Online().ext_fromStream(stream);
    }

    /**
     * @param stream
     * @return
     * @throws JAXBException
     */
    public static EXT_Online create(Node node) throws JAXBException {

	return new EXT_Online().ext_fromNode(node);
    }

}
