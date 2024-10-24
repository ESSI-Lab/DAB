package eu.essi_lab.accessor.ckan;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.ckan.datamodel.CKANDataset;
import junit.framework.TestCase;

public class CKANTemporalExtentTest {

    private CKANParser parser;

    @Before
    public void init() {
	this.parser = new CKANParser();
    }

    /**
     * Test the Temporal Extent mapping from several CKAN datasets taken from GEOSS
     * 
     */
    
    
    
    /**
     * CCCA
     * 
     * @throws Exception
     */
    @Test
    public void testTemporalExtentFromCCCAExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("ccca_RO_Temp_052d16a1-4970-4f88-be4f-867a181a600f.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("2012-08-16T12:00:00", dataset.getDatasetStartDate());
	TestCase.assertEquals("2016-08-17T12:00:00", dataset.getDatasetEndDate());
	
    }
    
    /**
     * Danube
     * 
     * @throws Exception
     */
    @Test
    public void testTemporalExtentFromDanubeExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("danube_package_show_06-08-2015-administrative-units.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("2014-10-02", dataset.getDatasetStartDate());
	TestCase.assertEquals(null, dataset.getDatasetEndDate());
	
    }
    
    /**
     * USDataGov
     * @throws Exception
     */
    @Test
    public void testTemporalExtentFromUSDataGovEmptyExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("usdatagov_8a311c18-3060-438e-a8c7-9e37bcde6529.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals(null, dataset.getDatasetStartDate());
	TestCase.assertEquals(null, dataset.getDatasetEndDate());
	
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
    public void testTemporalExtentFromUSDataGovExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("usdatagov_b2e6cc0e-9f0f-4fba-b4f6-a32f2f771665.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("1940-01-01", dataset.getDatasetStartDate());
	TestCase.assertEquals(null, dataset.getDatasetEndDate());
	
    }
    
    /**
     * EARTH2OBSERVE
     * @throws Exception
     */
    @Test
    public void testTemporalExtentFromEarth2ObserveExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("earth2observe_ea09a35f-fa47-460d-a51b-03827c1ccfc2.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals(null, dataset.getDatasetStartDate());
	TestCase.assertEquals(null, dataset.getDatasetEndDate());
	

	
    }
    
    /**
     * ENVIDAT: 
     * @throws Exception
     */
    //TODO: check why the south and north finish with "7" and original file is "8"
    @Test
    public void testTemporalExtentFromEnvidatExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("envidat_38112bee-f865-4b29-ba19-47a99ba78833.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);

	
    }
    
    
    /**
     * JRC CATALOG: 
     * @throws Exception
     */
    
    @Test
    public void testTemporalExtentFromJRCExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("jrc_0026aa70-cc6d-4f6f-8c2f-554a2f9b17f2.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	

	TestCase.assertEquals("1969-12-01", dataset.getDatasetStartDate());
	TestCase.assertEquals("2004-11-30", dataset.getDatasetEndDate());
	
    }
    
    /**
     * The Humanitarian Data Exchange CATALOG:
     * 
     *  The BBOX is null for all records
     * @throws Exception
     */
    
    @Test
    public void testTemporalExtentFromHumanitarianDataExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("humanitariandata_141121-sierra-leone-health-facilities.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	

	
    }
    
    /**
     * The UK Data GOV CATALOG:
     * 
     * @throws Exception
     */
    
    @Test
    public void testTemporalExtentFromUKDataGovExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("ukdatagov_9f387d52-9044-4c4a-ab9d-26461a3e43f3.json");
	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("1957-01-19T00:00:00.000Z", dataset.getDatasetStartDate());
	TestCase.assertEquals("1957-12-15T00:00:00.000Z", dataset.getDatasetEndDate());

    }
    

    
    /**
     * World Resources Institute (WRI) CATALOG:
     * 
     * @throws Exception
     */
    
    @Test
    public void testTemporalExtentFromWRIExample() throws Exception {
	InputStream stream = CKANTemporalExtentTest.class.getClassLoader().getResourceAsStream("wri_540dcf46-f287-47ac-985d-269b04bea4c6.json");
	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	CKANDataset dataset = parser.parseDataset(string);
	
	TestCase.assertEquals("2010", dataset.getDatasetStartDate());
	TestCase.assertEquals("2018", dataset.getDatasetEndDate());

	
    }

}
