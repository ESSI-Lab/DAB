package eu.essi_lab.accessor.csw;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.ows._1_0_0.DomainType;
import eu.essi_lab.jaxb.ows._1_0_0.Operation;
import eu.essi_lab.jaxb.ows._1_0_0.OperationsMetadata;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class NODCGetCapabilitiesTest {

    @Test
    public void test() throws JAXBException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("GetCapabilitiesNODC.xml");

	String capString = IOStreamUtils.asUTF8String(stream);

	capString = capString.replace("<ows:AllowedValues>", "");
	capString = capString.replace("</ows:AllowedValues>", "");

	stream = IOStreamUtils.asStream(capString);

	Capabilities capabilities = CommonContext.unmarshal(stream, Capabilities.class);

	OperationsMetadata operationsMetadata = capabilities.getOperationsMetadata();

	List<Operation> operation = operationsMetadata.getOperation();

	operation.forEach(op -> {

	    String name = op.getName();

	    if (name.equals("GetRecords")) {

		List<DomainType> parameter = op.getParameter();

		parameter.forEach(p -> {

		    String parameterName = p.getName();

		    List<String> parameterValues = p.getValue();

		    Assert.assertFalse(parameterValues.isEmpty());

		});
	    }
	});
    }

}
