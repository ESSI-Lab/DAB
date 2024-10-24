package eu.essi_lab.iso.datamodel.test;

import org.junit.Assert;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TransferOptions;
import net.opengis.iso19139.gmd.v_20060504.MDDigitalTransferOptionsType;

public class TransferOptionsTest extends MetadataTest<TransferOptions, MDDigitalTransferOptionsType> {

    public TransferOptionsTest() {
	super(TransferOptions.class, MDDigitalTransferOptionsType.class);
    }

    @Override
    public void setProperties(TransferOptions metadata) {
	metadata.setTransferSize(34.);
	Online onLine = new Online();
	onLine.setDescription("desc");
	metadata.addOnline(onLine);

    }

    @Override
    public void checkProperties(TransferOptions metadata) {
	Assert.assertEquals(34.0, metadata.getTransferSize(), 10E-13);
	Assert.assertEquals("desc", metadata.getOnlines().next().getDescription());

    }

    @Override
    public void clearProperties(TransferOptions metadata) {
	metadata.setTransferSize(null);
	metadata.clearOnlines();

    }

    @Override
    public void checkNullProperties(TransferOptions metadata) {
	Assert.assertNull(metadata.getTransferSize());
	Assert.assertFalse(metadata.getOnlines().hasNext());

    }

}
