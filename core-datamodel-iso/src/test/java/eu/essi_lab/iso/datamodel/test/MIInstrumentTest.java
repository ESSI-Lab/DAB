/**
 * 
 */
package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIInstrumentType;

/**
 * @author Fabrizio
 */
public class MIInstrumentTest extends MetadataTest<MIInstrument, MIInstrumentType> {

    public MIInstrumentTest() {
	super(MIInstrument.class, MIInstrumentType.class);
    }

    @Override
    public void setProperties(MIInstrument metadata) {

	metadata.setDescription("description");
	metadata.setMDIdentifierTypeCode("mdIdCode");
	metadata.setMDIdentifierTypeIdentifier("mdIdTypeId");
	metadata.setSensorType("sensorType");
	metadata.setTitle("title");
    }

    @Override
    public void checkProperties(MIInstrument metadata) {

	Assert.assertEquals("description", metadata.getDescription());
	Assert.assertEquals("mdIdCode", metadata.getMDIdentifierCode());
	Assert.assertEquals("mdIdTypeId", metadata.getMDIdentifierType().getId());
	Assert.assertEquals("sensorType", metadata.getSensorType());
	Assert.assertEquals("title", metadata.getTitle());
    }

    @Override
    public void clearProperties(MIInstrument metadata) {

    }

    @Override
    public void checkNullProperties(MIInstrument metadata) {

    }

}
