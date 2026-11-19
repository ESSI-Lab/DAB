package eu.essi_lab.downloader.hiscentral;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.sicilia.HISCentralSiciliaMapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * The rain totals are requested grouped by intervals (elab=RainIntervals), since the API returns them cumulated from
 * the begin of the requested period
 */
public class HISCentralSiciliaDownloaderTest {

    private static GSResource map(String quantity, String elementName, String unit, int dtr) throws Exception {

	JSONObject station = new JSONObject("{ \"i\": 1035400, \"n\": \"Linguaglossa Etna Nord [SIAS]\", \"x\": 15.07, \"y\": 37.81, \"s\": 0 }");
	JSONObject element = new JSONObject();
	element.put("elementId", 79545);
	element.put("elementName", elementName);
	element.put("stationId", 1035400);
	element.put("quantity", quantity);
	element.put("measUnit", unit);
	element.put("startDate", "2020-01-01T00:00:00");
	element.put("dtr", dtr);

	JSONObject json = new JSONObject();
	json.put("station-info", station);
	json.put("element-info", element);

	OriginalMetadata original = new OriginalMetadata();
	original.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI);
	original.setMetadata(json.toString());

	GSSource source = new GSSource();
	source.setEndpoint("https://siciliadrpc-aegis.caedns.it/platform/");

	return new HISCentralSiciliaMapper().map(original, source);
    }

    @Test
    public void rainIntervalTest() throws Exception {

	GSResource increments = map("Incrementi di Pioggia", "Precipitazione totale", "mm", 300);
	Assert.assertEquals(InterpolationType.TOTAL, increments.getExtensionHandler().getTimeInterpolation().get());
	Assert.assertEquals(Integer.valueOf(5), HISCentralSiciliaDownloader.getRainIntervalMinutes(increments).get());

	GSResource cumulated = map("Pioggia Cumulata", "Pioggia Cumulata", "mm", 1800);
	Assert.assertEquals(Integer.valueOf(30), HISCentralSiciliaDownloader.getRainIntervalMinutes(cumulated).get());

	GSResource noResolution = map("Pioggia Cumulata", "Pioggia Cumulata", "mm", 0);
	Assert.assertEquals(Integer.valueOf(60), HISCentralSiciliaDownloader.getRainIntervalMinutes(noResolution).get());

	// the intensity is not a total
	GSResource intensity = map("Intensità di Pioggia", "Intensità di Pioggia", "mm/h", 60);
	Assert.assertEquals(InterpolationType.CONTINUOUS, intensity.getExtensionHandler().getTimeInterpolation().get());
	Assert.assertFalse(HISCentralSiciliaDownloader.getRainIntervalMinutes(intensity).isPresent());

	GSResource temperature = map("Temperatura Aria", "Temperatura Aria", "°C", 600);
	Assert.assertFalse(HISCentralSiciliaDownloader.getRainIntervalMinutes(temperature).isPresent());
    }
}
