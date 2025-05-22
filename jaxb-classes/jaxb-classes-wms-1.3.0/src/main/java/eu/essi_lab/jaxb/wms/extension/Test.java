package eu.essi_lab.jaxb.wms.extension;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import eu.essi_lab.jaxb.wms._1_3_0.WMSCapabilities;

public class Test {
public static void main(String[] args) throws JAXBException {
    JAXBWMS.getInstance().getMarshaller();

}
}
