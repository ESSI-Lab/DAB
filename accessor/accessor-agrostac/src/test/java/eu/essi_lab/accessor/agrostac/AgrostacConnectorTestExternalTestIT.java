package eu.essi_lab.accessor.agrostac;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 *
 */
public class AgrostacConnectorTestExternalTestIT {

   
    Downloader d;
    private Logger logger; 
    public static String baseURL = "https://agrostac-test.containers.wur.nl/agrostac/";
    public static String token = System.getProperty("agrostac.token");
    public static String cropURL = "crops?";
    public static String quantityURL = "cropquantities/";
    public static String datasetURL = "datasets";
    public static String granuleURL = "locations2?objecttypecode=CROP_CULTIVATION&";
    
    public static String specificCropLocation = "cropcultivationlocation/";
    
    private String endpoint = "https://agrostac-test.containers.wur.nl/agrostac/cropquantities/";
    private Downloader dowloader = new Downloader();
    
    
        
    @Before
    public void init() throws Exception {
	this.d = new Downloader();
	this.logger = GSLoggerFactory.getLogger(getClass());
    }
    
    @Test
    public void TestRequests() throws Exception {
	

	/**
	 * CROP TYPES
	 */
	String getCropList= baseURL + cropURL + "accesstoken=" + token;
	Optional<String> optCrop = d.downloadOptionalString(getCropList);
	if(optCrop.isPresent()) {
	    JSONObject cropObj = new JSONObject(optCrop.get());
	    JSONArray arrayCrop = cropObj.optJSONArray("Crops");
	    logger.info("***CROPS LIST***");
	    logger.info("Number of crops: " + arrayCrop.length());
	    Assert.assertTrue(arrayCrop.length() > 0);
	    for(int i=0; i < arrayCrop.length(); i++) {
		JSONObject cropObject = arrayCrop.getJSONObject(i);
		String cropCode = cropObject.optString("crop_code");
		String cropName = cropObject.optString("crop_name");
		logger.info("Crop code: {}. Crop name: {}", cropCode, cropName);
		String cropQuantitiesURL = baseURL + quantityURL + cropCode + "?" + "accesstoken=" + token;
		Optional<String> optCropQuantities = d.downloadOptionalString(cropQuantitiesURL);
		if(optCropQuantities.isPresent()){
		    JSONObject cropQObj = new JSONObject(optCropQuantities.get());
		    JSONArray arrayQCrop = cropQObj.optJSONArray("Cropquantities");
		    logger.info("***CROPS QUANTITIES LIST***");
		    logger.info("Number of qunatity crops: " + arrayQCrop.length());
		    Assert.assertTrue(arrayQCrop.length() > 0);
		    for(int j=0; j < arrayQCrop.length(); j++) {
			JSONObject cropQObject = arrayQCrop.getJSONObject(j);
			String quantityCode = cropQObject.optString("quantitycode");
			String quantityDescription = cropQObject.optString("quantitydescriptionuk");
			String quantityId = cropQObject.optString("quantityid");
			logger.info("Quantity code: {}. Quantity id: {}.  Quantity description: {}", quantityCode, quantityId, quantityDescription);
		    }
		}		
	    }
	    logger.info("******");
	}
	
	/**
	 * DATASETS
	 */
	
	String getDatasetCollection = baseURL + datasetURL;
	Optional<String> optColl = d.downloadOptionalString(getDatasetCollection);
	if(optColl.isPresent()) {
	    JSONObject collObj = new JSONObject(optColl.get());
	    JSONArray array = collObj.optJSONArray("Datasets");
	    logger.info("***DATASETS***");
	    logger.info("Number of datasets: " + array.length());
	    Assert.assertTrue(array.length() > 0);
	    for(int i=0; i < array.length(); i++) {
		JSONObject datasetObj = array.getJSONObject(i);
		String datasetCode = datasetObj.optString("dataset_code");
		String datasetId = datasetObj.optString("datasetid");
		logger.info("Dataset code: {}. Dataset id: {}", datasetCode, datasetId);
		
	    }
	    logger.info("******");
	}
	
	/**
	 * GRANULES??
	 */
	
	String getGranulesLocation = baseURL + granuleURL + "accesstoken=" + token;
	Optional<String> optGranulesLocation = d.downloadOptionalString(getGranulesLocation);
	if(optGranulesLocation.isPresent()) {
	    JSONObject granuleObj = new JSONObject(optGranulesLocation.get());
	    JSONArray arrayLocations = granuleObj.optJSONArray("Locations");
	    logger.info("***CROP LOCATIONS***");
	    logger.info("Number of datasets: " + arrayLocations.length());
	    Assert.assertTrue(arrayLocations.length() > 0);
	    Map<String, Integer> map = new HashMap<String, Integer>();
	    for(int i=0; i < arrayLocations.length(); i++) {
		JSONObject locationObj = arrayLocations.getJSONObject(i);
		String datasetCode = locationObj.optString("dataset_code");
		String datasetId = locationObj.optString("datasetid");
		int days = locationObj.optInt("numberofdatarows");
		if(!map.containsKey(datasetCode)) {
		    map.put(datasetCode, 1);
		} else {
		    int count = map.get(datasetCode) + 1;
		    map.put(datasetCode, count);
		}
		logger.info("Dataset code: {}. Dataset id: {}", datasetCode, datasetId);
		
	    }
	    for (Map.Entry<String, Integer> entry : map.entrySet()) {
	        logger.info("Dataset code: {}. Count: {}" , entry.getKey() , entry.getValue());
	    }
	    logger.info("******");
	}
	
    }
    
    
    @Test
    public void test1() throws IOException, GSException {

	InputStream stream = AgrostacConnectorTestExternalTestIT.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/agrostac/test/cropTypes.json");

	Assert.assertNotNull(stream);

	String cropTypes = IOStreamUtils.asUTF8String(stream);

	JSONObject obj = new JSONObject(cropTypes);

	JSONArray cropsArray = obj.optJSONArray("Crops");

	Map<String, List<String>> map = new HashMap<String, List<String>>();
	
	for (int i = 0; i < cropsArray.length(); i++) {
	    String cropCode = cropsArray.getJSONObject(i).optString("crop_code");
	    String request = endpoint + cropCode + "?accesstoken=" + token;
	    Optional<String> quantityResp = dowloader.downloadOptionalString(request);
	    List<String> quantitiesForCrop = new ArrayList<String>();
	    if (quantityResp.isPresent()) {
		JSONObject quantityObj = new JSONObject(quantityResp.get());
		JSONArray quantityArray = quantityObj.optJSONArray("Cropquantities");
		
		for (int k = 0; k < quantityArray.length(); k++) {
		    String quantityCode = quantityArray.getJSONObject(k).optString("quantitycode");
		    String dateMin = quantityArray.getJSONObject(k).optString("datemin");
		    String dateMax = quantityArray.getJSONObject(k).optString("datemax");
		    quantitiesForCrop.add(quantityCode);
		}

	    }
	    map.put(cropCode, quantitiesForCrop);
	}
	for (Map.Entry<String, List<String>> entry : map.entrySet()) {
	    System.out.println("CROP CODE: " + entry.getKey());
	    System.out.print("QUANTITIES: " + entry.getValue());
	    System.out.println("");
	}

	// Mockito.doReturn(md).when(om).getMetadata();

	// Mockito.verify(mapper, Mockito.times(0)).enrichWithSecondLevelUrl(Mockito.any(), Mockito.any(),
	// Mockito.any());

    }


}
