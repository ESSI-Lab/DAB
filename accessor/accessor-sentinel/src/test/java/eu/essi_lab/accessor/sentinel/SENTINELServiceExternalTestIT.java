package eu.essi_lab.accessor.sentinel;

import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class SENTINELServiceExternalTestIT {

    private String SENTINELAPIKEY = "3591ef6d-5c7f-4461-9cc8-153e7abd2639";
    private String SENTINEL_PATH;
    private String SENTINELAPIKEY_EXPIRED = "b7b5e3ef-5a40-4e2a-9fd3-75ca2b81cb32";
    private String SENTINEL_PATH_EXPIRED;
    private String SENTINEL_PATH_FAIL_LAYER;
    private String LAYER_NAME;
    private String placeHolder = "$LAYER_NAME$";

    @Before
    public void init() {

	SENTINEL_PATH = "https://services.sentinel-hub.com/ogc/wms/" + SENTINELAPIKEY + "?showLogo=false&service=WMS&request=GetMap&layers="
		+ placeHolder
		+ "&styles=&format=image%2Fpng&transparent=true&version=1.1.1&name=sentinel2&prettyName=Sentinel%202&height=256&width=256&srs=EPSG%3A4326&maxcc=100&priority=mostRecent&gain=1&evalscript=&COLCOR=&CLOUDCORRECTION=none&time=2019-11-21T01:37:11.024%2F2019-11-21T02:56:56.000&bbox=125.15625,-33.046875,125.859375,-32.34375";

	SENTINEL_PATH_FAIL_LAYER = "https://services.sentinel-hub.com/ogc/wms/01669a33-080d-4c5e-87f9-3a71918e3c96"
		+ "?showLogo=false&service=WMS&request=GetMap&layers=FALSE_COLOR&styles=&format=image%2Fpng&transparent=true&version=1.1.1&name=sentinel2&prettyName=Sentinel%202&height=256&width=256&srs=EPSG%3A4326&maxcc=100&priority=mostRecent&gain=1&evalscript=&COLCOR=&CLOUDCORRECTION=none&time=2019-11-21T01:37:11.024%2F2019-11-21T02:56:56.000&bbox=125.15625,-33.046875,125.859375,-32.34375";

	SENTINEL_PATH_EXPIRED = "https://services.sentinel-hub.com/ogc/wms/" + SENTINELAPIKEY_EXPIRED
		+ "?showLogo=false&service=WMS&request=GetMap&layers=FALSE_COLOR&styles=&format=image%2Fpng&transparent=true&version=1.1.1&name=sentinel2&prettyName=Sentinel%202&height=256&width=256&srs=EPSG%3A4326&maxcc=100&priority=mostRecent&gain=1&evalscript=&COLCOR=&CLOUDCORRECTION=none&time=2019-11-21T01:37:11.024%2F2019-11-21T02:56:56.000&bbox=125.15625,-33.046875,125.859375,-32.34375";
    }

    @Test
    public void testAPIKEYSupported() {
	Downloader downloader = new Downloader();
	SatelliteLayers[] vals = SatelliteLayers.values();
	for (SatelliteLayers v : vals) {
	    LAYER_NAME = v.getWMSLayer();
	    String wmsRequest = SENTINEL_PATH.replace(placeHolder, LAYER_NAME);
	    System.out.println("Get Map Request:  " + wmsRequest);
	    Optional<Integer> code;
	    try {
		code = HttpConnectionUtils.getOptionalResponseCode(wmsRequest);
		if (code.isPresent())
		    Assert.assertTrue(code.get().intValue() == 200);
	    } catch (URISyntaxException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	}

    }

    @Test
    public void testAPIKEYExpired() {

	System.out.println("Get Map Request:  " + SENTINEL_PATH_EXPIRED);
	Optional<Integer> code;
	try {
	    code = HttpConnectionUtils.getOptionalResponseCode(SENTINEL_PATH_EXPIRED);
	    if (code.isPresent())
		Assert.assertTrue(code.get().intValue() == 400);
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    @Test
    // this test fails with 01669a33-080d-4c5e-87f9-3a71918e3c96 api key
    public void testAPIKEYLayerFail() {

	System.out.println("Get Map Request:  " + SENTINEL_PATH_FAIL_LAYER);
	try {
	    Optional<Integer> code = HttpConnectionUtils.getOptionalResponseCode(SENTINEL_PATH_FAIL_LAYER);
	    if (code.isPresent())
		Assert.assertTrue(code.get().intValue() >= 400);
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

}
