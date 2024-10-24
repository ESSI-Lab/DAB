package eu.essi_lab.accessor.ckan;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.ckan.datamodel.CKANDataset;
import junit.framework.TestCase;

public class CKANBoundingBoxTest {

    private CKANParser parser;

    @Before
    public void init() {
	this.parser = new CKANParser();
    }

    /**
     * Tests the Bounding Box mapping from several CKAN datasets taken from GEOSS
     * 
     * @throws Exception
     */
    
    
    
    /**
     * CCCA
     * 
     * @throws Exception
     */
    @Test
    public void testBoundingBoxFromCCCAExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("ccca_RO_Temp_052d16a1-4970-4f88-be4f-867a181a600f.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("8.4375", dataset.getBboxWestLongitude());
	TestCase.assertEquals("23.564", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("10.5469", dataset.getBboxEastLongitude());
	TestCase.assertEquals("60.0648", dataset.getBboxNorthLatitude());
	
    }
    
    /**
     * USDataGov
     * @throws Exception
     */
    @Test
    public void testBoundingBoxFromUSDataGovEmptyExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("usdatagov_8a311c18-3060-438e-a8c7-9e37bcde6529.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals(null, dataset.getBboxWestLongitude());
	TestCase.assertEquals(null, dataset.getBboxSouthLatitude());
	TestCase.assertEquals(null, dataset.getBboxEastLongitude());
	TestCase.assertEquals(null, dataset.getBboxNorthLatitude());
	
    }
    
    
    /**
     * 
     * USDataGov
     * 
     * This is a very interesting case. The BBox appear as spatial Polygon coordinates and also as single bbox terms:
     * - "value": "{\"type\": \"Polygon\", \"coordinates\": [[[130.0, -15.0], [-60.0, -15.0], [-60.0, 72.0], [130.0, 72.0], [130.0, -15.0]]]}"
     * - {"key": "bbox-west-long","value": "130.0"}, {"key": "bbox-east-long","value": "-60.0"}
     * 
     * Note that calculating bbox with polygon we get the following values	 (minlon,minlat,maxlon,maxlat): -60,-15,130,72
     * Note that calculating bbox with bbox-west-long we expected the following values(minlon,minlat,maxlon,maxlat): 130,-15,-60,72 but at the end we found
     * 							130, -15, 130, 72
     * 
     * The mapping for extra fields doesn't consider the order of bbox values, so it can happen that some values are overwritten in a wrong way.
     * 
     * POSSIBLE SOLUTION: 1) check if values are already set before write it ?
     * 			  2) check if west > east and south > north ?  	
     * 
     * @throws Exception
     */
    @Test
    public void testBoundingBoxFromUSDataGovExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("usdatagov_b2e6cc0e-9f0f-4fba-b4f6-a32f2f771665.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("130.0", dataset.getBboxWestLongitude());
	TestCase.assertEquals("-15.0", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("130.0", dataset.getBboxEastLongitude());
	TestCase.assertEquals("72.0", dataset.getBboxNorthLatitude());
	
    }
    
    /**
     * EARTH2OBSERVE
     * @throws Exception
     */
    @Test
    public void testBoundingBoxFromEarth2ObserveExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("earth2observe_ea09a35f-fa47-460d-a51b-03827c1ccfc2.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("-180", dataset.getBboxWestLongitude());
	TestCase.assertEquals("-90", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("180", dataset.getBboxEastLongitude());
	TestCase.assertEquals("90", dataset.getBboxNorthLatitude());
	
    }
    
    /**
     * ENVIDAT: 
     * @throws Exception
     */
    //TODO: check why the south and north finish with "7" and original file is "8"
    @Test
    public void testBoundingBoxFromEnvidatExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("envidat_38112bee-f865-4b29-ba19-47a99ba78833.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("9.809568", dataset.getBboxWestLongitude());
	TestCase.assertEquals("46.829598", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("9.809568", dataset.getBboxEastLongitude());
	TestCase.assertEquals("46.829598", dataset.getBboxNorthLatitude());
	
    }
    
    
    /**
     * JRC CATALOG: 
     * @throws Exception
     */
    
    @Test
    public void testBoundingBoxFromJRCExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("jrc_0026aa70-cc6d-4f6f-8c2f-554a2f9b17f2.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("-29.0571", dataset.getBboxWestLongitude());
	TestCase.assertEquals("26.9813", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("41.5208", dataset.getBboxEastLongitude());
	TestCase.assertEquals("71.3589", dataset.getBboxNorthLatitude());
	
    }
    
    /**
     * The Humanitarian Data Exchange CATALOG:
     * 
     *  The BBOX is null for all records
     * @throws Exception
     */
    
    @Test
    public void testBoundingBoxFromHumanitarianDataExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("humanitariandata_141121-sierra-leone-health-facilities.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals(null, dataset.getBboxWestLongitude());
	TestCase.assertEquals(null, dataset.getBboxSouthLatitude());
	TestCase.assertEquals(null, dataset.getBboxEastLongitude());
	TestCase.assertEquals(null, dataset.getBboxNorthLatitude());
	
    }
    
    /**
     * The UK Data GOV CATALOG:
     * 
     * @throws Exception
     */
    
    @Test
    public void testBoundingBoxFromUKDataGovExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("ukdatagov_9f387d52-9044-4c4a-ab9d-26461a3e43f3.json");
	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("5.0", dataset.getBboxWestLongitude());
	TestCase.assertEquals("65.0", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("55.0", dataset.getBboxEastLongitude());
	TestCase.assertEquals("80.0", dataset.getBboxNorthLatitude());
	
    }
    

    
    /**
     * World Resources Institute (WRI) CATALOG:
     * 
     * @throws Exception
     */
    
    @Test
    public void testBoundingBoxFromWRIExample() throws Exception {
	InputStream stream = CKANBoundingBoxTest.class.getClassLoader().getResourceAsStream("wri_079feba589a34c5caed11dc4a5c03a47.json");
	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("21.487", dataset.getBboxWestLongitude());
	TestCase.assertEquals("42.487", dataset.getBboxSouthLatitude());
	TestCase.assertEquals("28.513", dataset.getBboxEastLongitude());
	TestCase.assertEquals("49.513", dataset.getBboxNorthLatitude());
	
    }

}
