package eu.essi_lab.accessor.hiscentral.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.friuli.HISCentralFriuliConnector;
import eu.essi_lab.downloader.hiscentral.HISCentralFriuliDownloader;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;

/**
 * External integration test for {@link HISCentralFriuliDownloader}.
 * It downloads a specific time series from the Friuli service.
 */
public class HISCentralFriuliDownloaderExternalTestIT {

    private static final String DEFAULT_BASE_URL = "https://api.meteo.fvg.it/";
    private HISCentralFriuliDownloader downloader;

    @Before
    public void init() throws GSException {
	this.downloader = new HISCentralFriuliDownloader();
	HISCentralFriuliConnector.USER = System.getProperty("user");
	HISCentralFriuliConnector.PASSWORD = System.getProperty("password");
	//
	// Build a minimal dataset/online resource similar to what
	// HISCentralFriuliMapper would create for a single measure
	//
	Dataset dataset = new Dataset();

	GSSource source = new GSSource();
	source.setEndpoint(DEFAULT_BASE_URL);
	dataset.setSource(source);


//	String id = "19347";//Cleulis
	String id = "17991";//Vivaro
	String station = "my-station";

	String onlineResourceId = station;
	Online online = new Online();
	online.setIdentifier(onlineResourceId);
	online.setLinkage(DEFAULT_BASE_URL + "data?measure_id="+id);
	online.setFunctionCode("download");
	online.setName(station+"_"+id);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_FRIULI_NS_URI);

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(online);

	downloader.setOnlineResource(dataset, onlineResourceId);
    }

    /**
     * Downloads a specific series (measure_id=17968) using the Friuli downloader
     * and validates that the returned file is a valid WaterML 1.1 document.
     */
    @Test
    public void downloadSpecificSeriesTest() throws Exception {

	// can download
	Assert.assertTrue(downloader.canDownload());

	// use a minimal descriptor; the downloader will choose
	// a sensible default temporal window when none is provided
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	Date start = ISO8601DateTimeUtils.parseISO8601ToDate("2025-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2026-01-01T00:00:00Z").get();
	descriptor.setTemporalDimension(start, end);
	File tmp = downloader.download(descriptor);
	tmp.deleteOnExit();

	// parse WaterML file using TimeSeriesResponseType and count values
	FileInputStream fis = new FileInputStream(tmp);
	TimeSeriesResponseType timeSeriesResponse = JAXBWML.getInstance().parseTimeSeries(fis);
	IOUtils.closeQuietly(fis);

	int valueCount = 0;
	if (timeSeriesResponse != null) {
	    for (TimeSeriesType ts : timeSeriesResponse.getTimeSeries()) {
		for (TsValuesSingleVariableType values : ts.getValues()) {
		    for (ValueSingleVariable v : values.getValue()) {
			valueCount++;
		    }
		}
	    }
	}

	System.out.println("Number of values in downloaded series: " + valueCount);
	Assert.assertTrue("Expected at least one value in the downloaded series", valueCount > 0);

	tmp.delete();
    }


}
