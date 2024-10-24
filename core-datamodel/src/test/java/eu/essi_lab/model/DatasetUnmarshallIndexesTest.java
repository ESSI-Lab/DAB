package eu.essi_lab.model;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ResourceProperty;

public class DatasetUnmarshallIndexesTest {

    /**
     * This test is done due to an unmarshalling issue of the MapWrapper. The map properties are
     * JAXBObject but after unmarshalling, they become W3c nodes.
     * See MapWrapper#getProperties
     * See MapWrapper#addEntry(String,String)
     * See {@link IndexesMetadata#remove(String)}
     */
    @Test
    public void test() throws UnsupportedEncodingException, JAXBException {

	// --------------------------------
	//
	// creates a dataset with 2 indexes
	//
	Dataset dataset1 = new Dataset();

	{
	    dataset1.setPrivateId("PID");
	    dataset1.setOriginalId("OID");

	    Assert.assertEquals("PID", dataset1.getPrivateId());
	    Assert.assertEquals("OID", dataset1.getOriginalId().get());

	    String pid = dataset1.getIndexesMetadata().read(ResourceProperty.PRIVATE_ID).get();
	    String oid = dataset1.getIndexesMetadata().read(ResourceProperty.ORIGINAL_ID).get();

	    Assert.assertEquals("PID", pid);
	    Assert.assertEquals("OID", oid);
	}
	{
	    // set the indexes again, the expected behavior is an indexes replacement
	    dataset1.setPrivateId("PID2");
	    dataset1.setOriginalId("OID2");

	    Assert.assertEquals("PID2", dataset1.getPrivateId());
	    Assert.assertEquals("OID2", dataset1.getOriginalId().get());

	    String pid = dataset1.getIndexesMetadata().read(ResourceProperty.PRIVATE_ID).get();
	    String oid = dataset1.getIndexesMetadata().read(ResourceProperty.ORIGINAL_ID).get();

	    Assert.assertEquals("PID2", pid);
	    Assert.assertEquals("OID2", oid);
	}

	// ********************************************************************************
	// the indexes map properties of dataset 1 are 2 JAXBObject<String> for PID and OID
	// ********************************************************************************

	// ------------------------------------------------------------------------
	//
	// creates a new dataset unmarshalling the previous one
	//
	Dataset dataset2 = Dataset.create(dataset1.asStream());
	{

	    // *************************************************************************************
	    // the indexes map properties of dataset2 now are no longer JAXBObject<String>, but they
	    // are W3c Node. As consequence of the unmarshalling, the JAXBObject<String>
	    // they became W3c nodes
	    // **************************************************************************************

	    Assert.assertEquals("PID2", dataset2.getPrivateId());
	    Assert.assertEquals("OID2", dataset2.getOriginalId().get());

	    dataset2.getPropertyHandler().setIsDeleted(true);

	    Assert.assertEquals(true, dataset2.getPropertyHandler().isDeleted());
	}

	{
	    // *******************************************************************************
	    // the indexes map of dataset2 has MIXED elements: 2 W3c nodes for PID
	    // and OID, and 1 JAXBObject<String> for the IS_DELETED property!
	    // ********************************************************************************

	    // ------------------------------------------------------------------------
	    //
	    // creates a new dataset unmarshalling the previous one
	    //
	    Dataset dataset3 = Dataset.create(dataset2.asStream());

	    // *************************************************************************************
	    // the indexes map properties of dataset3 now are no longer mixed, but they
	    // are all W3c nodes
	    // **************************************************************************************

	    Assert.assertEquals("PID2", dataset3.getPrivateId());
	    Assert.assertEquals("OID2", dataset3.getOriginalId().get());
	    Assert.assertEquals(true, dataset3.getPropertyHandler().isDeleted());

	    // removing the nodes elements
	    dataset3.getIndexesMetadata().remove(ResourceProperty.PRIVATE_ID.getName());
	    dataset3.getIndexesMetadata().remove(ResourceProperty.ORIGINAL_ID.getName());
	    dataset3.getIndexesMetadata().remove(ResourceProperty.IS_DELETED.getName());

	    Assert.assertNull(dataset3.getPrivateId());
	    Assert.assertFalse(dataset3.getOriginalId().isPresent());
	    Assert.assertTrue(dataset3.getIndexesMetadata().read(ResourceProperty.IS_DELETED.getName()).isEmpty());

	    // set the indexes again
	    dataset3.setPrivateId("PID3");
	    dataset3.setOriginalId("OID3");

	    // *********************************************************************
	    // the indexes map of dataset2 has 2 JAXBObject<String> for PID and OID
	    // *********************************************************************

	    // ------------------------------------------------------------------------
	    //
	    // creates a new dataset unmarshalling the previous one
	    //
	    Dataset dataset4 = Dataset.create(dataset3.asStream());

	    Assert.assertEquals("PID3", dataset4.getPrivateId());
	    Assert.assertEquals("OID3", dataset4.getOriginalId().get());

	    dataset4.getPropertyHandler().setIsDeleted(true);

	    // *******************************************************************************
	    // the indexes map of dataset4 has MIXED elements: 2 W3c nodes for PID
	    // and OID, and 1 JAXBObject<String> for the IS_DELETED property!
	    // ********************************************************************************

	    // removing the mixed elements
	    dataset4.getIndexesMetadata().remove(ResourceProperty.PRIVATE_ID.getName());
	    dataset4.getIndexesMetadata().remove(ResourceProperty.ORIGINAL_ID.getName());
	    dataset4.getIndexesMetadata().remove(ResourceProperty.IS_DELETED.getName());

	    Assert.assertNull(dataset4.getPrivateId());
	    Assert.assertFalse(dataset4.getOriginalId().isPresent());
	    Assert.assertTrue(dataset4.getIndexesMetadata().read(ResourceProperty.IS_DELETED.getName()).isEmpty());
	}
    }
}
