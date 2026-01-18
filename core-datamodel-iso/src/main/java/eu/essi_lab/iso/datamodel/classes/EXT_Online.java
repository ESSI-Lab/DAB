package eu.essi_lab.iso.datamodel.classes;

import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.lib.xml.*;
import org.w3c.dom.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
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
