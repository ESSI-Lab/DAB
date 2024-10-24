package eu.essi_lab.model;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ResourcePropertyHandler;

/**
 * @author Fabrizio
 */
public class ResourcePropertyHandlerTest {

    /**
     * @throws JAXBException
     * @throws ClassCastException
     */
    @Test
    public void unmarshalTestGET1() throws ClassCastException, JAXBException {

	InputStream stream = ResourcePropertyHandlerTest.class.getClassLoader().getResourceAsStream("dataset2.xml");
	Dataset dataset = Dataset.create(stream);

	testGET(dataset);
    }

    /**
     * @throws JAXBException
     * @throws ClassCastException
     */
    @Test
    public void unmarshalTestGET2() throws ClassCastException, JAXBException {

	InputStream stream = ResourcePropertyHandlerTest.class.getClassLoader().getResourceAsStream("dataset3.xml");
	Dataset dataset = Dataset.create(stream);

	ResourcePropertyHandler handler = dataset.getPropertyHandler();

	Assert.assertFalse(handler.getEssentialVarsQuality().isPresent());

	Assert.assertFalse(handler.getAccessQuality().isPresent());

	Assert.assertTrue(handler.getComplianceLevelList().isEmpty());

	Assert.assertTrue(handler.getDownloadTimeList().isEmpty());

	Assert.assertTrue(handler.getExecutionTimeList().isEmpty());

	Assert.assertFalse(handler.getSucceededTest().isPresent());

	Assert.assertFalse(handler.getOAIPMHHeaderIdentifier().isPresent());

	Assert.assertFalse(handler.getTestTimeStamp().isPresent());

	Assert.assertFalse(handler.getResourceTimeStamp().isPresent());

	Assert.assertFalse(handler.isDownloadable().isPresent());

	Assert.assertFalse(handler.isExecutable().isPresent());

	Assert.assertFalse(handler.isISOCompliant().isPresent());

	Assert.assertFalse(handler.isTransformable().isPresent());
    }

    /**
     * @throws JAXBException
     * @throws ClassCastException
     */
    @Test
    public void unmarshalTestGET3() throws ClassCastException, JAXBException {

	InputStream stream = ResourcePropertyHandlerTest.class.getClassLoader().getResourceAsStream("dataset4.xml");
	Dataset dataset = Dataset.create(stream);

	ResourcePropertyHandler handler = dataset.getPropertyHandler();

	Assert.assertFalse(handler.getEssentialVarsQuality().isPresent());

	Assert.assertFalse(handler.getAccessQuality().isPresent());

	Assert.assertTrue(handler.getComplianceLevelList().isEmpty());

	Assert.assertTrue(handler.getDownloadTimeList().isEmpty());

	Assert.assertTrue(handler.getExecutionTimeList().isEmpty());

	Assert.assertFalse(handler.getSucceededTest().isPresent());

	Assert.assertFalse(handler.getOAIPMHHeaderIdentifier().isPresent());

	Assert.assertFalse(handler.getTestTimeStamp().isPresent());

	Assert.assertFalse(handler.getResourceTimeStamp().isPresent());

	Assert.assertFalse(handler.isDownloadable().isPresent());

	Assert.assertFalse(handler.isExecutable().isPresent());

	Assert.assertFalse(handler.isISOCompliant().isPresent());

	Assert.assertFalse(handler.isTransformable().isPresent());
    }

    @Test
    public void setTest() throws ClassCastException, JAXBException {

	Dataset dataset = new Dataset();

	ResourcePropertyHandler handler = dataset.getPropertyHandler();

	handler.setEssentialVarsQuality(20);
	handler.setAccessQuality(25);
	handler.addComplianceLevel("GRID-B");
	handler.addComplianceLevel("TS-B");
	handler.addDownloadTime(5000);
	handler.addExecutionTime(7000);
	handler.setSucceededTest("EXECUTION");
	handler.setOAIPMHHeaderIdentifier("OAI-HEADER-ID");
	handler.setTestTimeStamp("TEST-TIMESTAMP");
	handler.setResourceTimeStamp("RES-TIMESTAMP");
	handler.setIsDeleted(true);
	handler.setIsDownloadable(true);
	handler.setIsExecutable(true);
	handler.setIsGDC(true);
	handler.setIsISOCompliant(true);
	handler.setIsTransformable(true);

	testGET(dataset);
    }

    private void testGET(Dataset dataset) {

	ResourcePropertyHandler handler = dataset.getPropertyHandler();

	Integer evq = handler.getEssentialVarsQuality().get();
	Assert.assertEquals(20, (int) evq);

	Integer aq = handler.getAccessQuality().get();
	Assert.assertEquals(25, (int) aq);

	String cLevel1 = handler.getComplianceLevelList().get(1);
	Assert.assertEquals("TS-B", cLevel1);
	
	String cLevel2 = handler.getComplianceLevelList().get(0);
	Assert.assertEquals("GRID-B", cLevel2);

	Long dTime = handler.getDownloadTimeList().get(0);
	Assert.assertEquals((long) dTime, 5000);

	Long eTime = handler.getExecutionTimeList().get(0);
	Assert.assertEquals((long) eTime, 7000);

	String lTest = handler.getSucceededTest().get();
	Assert.assertEquals(lTest, "EXECUTION");

	String oId = handler.getOAIPMHHeaderIdentifier().get();
	Assert.assertEquals(oId, "OAI-HEADER-ID");

	String testTimeStamp = handler.getTestTimeStamp().get();
	Assert.assertEquals(testTimeStamp, "TEST-TIMESTAMP");

	String timeStamp = handler.getResourceTimeStamp().get();
	Assert.assertEquals(timeStamp, "RES-TIMESTAMP");

	boolean deleted = handler.isDeleted();
	Assert.assertEquals(deleted, true);

	Boolean downloadable = handler.isDownloadable().get();
	Assert.assertEquals(downloadable, true);

	Boolean executable = handler.isExecutable().get();
	Assert.assertEquals(executable, true);

	boolean gdc = handler.isGDC();
	Assert.assertEquals(gdc, true);

	Boolean isoCompliant = handler.isISOCompliant().get();
	Assert.assertEquals(isoCompliant, true);

	Boolean transformable = handler.isTransformable().get();
	Assert.assertEquals(transformable, true);
    }
}
