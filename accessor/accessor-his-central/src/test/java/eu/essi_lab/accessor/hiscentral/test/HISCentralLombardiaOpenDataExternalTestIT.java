package eu.essi_lab.accessor.hiscentral.test;


/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.lombardia.opendata.HISCentralLombardiaOpenDataAccessor;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.HISCentralLombardiaOpenDataMangler;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaOpenDataClient;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaOpenDataProperty;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaWFSClient;
import eu.essi_lab.downloader.hiscentral.HISCentralLombardiaOpenDataDownloader;
import eu.essi_lab.downloader.hiscentral.HISCentralLombardiaOpenDataScaleDeflussoDownloader;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;

/**
 * Harvests and downloads from the Regione Lombardia open data portal and the ARPA Lombardia WFS
 *
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataExternalTestIT {

    /**
     * River level sensor of Caslino d'Erba (CO), with archive and real time data
     */
    private static final String LEVEL_SENSOR = "8124";

    /**
     * River level sensor of Asola (MN), with rating curves
     */
    private static final String RATING_CURVE_SENSOR = "11165";

    @Test
    public void listRecordsTest() throws Exception {

	HISCentralLombardiaOpenDataAccessor accessor = new HISCentralLombardiaOpenDataAccessor();
	accessor.getSetting().getGSSourceSetting().setSourceEndpoint(LombardiaOpenDataClient.DEFAULT_ENDPOINT);
	accessor.getSetting().getGSSourceSetting().setSourceIdentifier("lombardia-open-data");
	accessor.getConnector().getSetting().setPageSize(1000);

	List<GSResource> resources = new ArrayList<>();
	ListRecordsRequest request = new ListRecordsRequest();
	do {
	    ListRecordsResponse<GSResource> response = accessor.listRecords(request);
	    resources.addAll(response.getRecordsAsList());
	    request.setResumptionToken(response.getResumptionToken());
	    // as the harvester does, so that the connector (and its record cache) is reused for the next pages
	    request.setFirst(false);
	} while (request.getResumptionToken() != null);

	int ratingCurves = 0;
	int levelSeries = 0;
	int levelSeriesWithZero = 0;
	for (GSResource resource : resources) {
	    Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	    if (online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_SCALE_DEFLUSSO_NS_URI)) {
		ratingCurves++;
		Assert.assertTrue(resource.getPropertyHandler().isRatingCurve());
	    } else {
		Assert.assertEquals(CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_NS_URI, online.getProtocol());
		HISCentralLombardiaOpenDataMangler mangler = new HISCentralLombardiaOpenDataMangler();
		mangler.setMangling(online.getName());
		if (mangler.getProperty().equals(LombardiaOpenDataProperty.LIVELLO_IDROMETRICO.name())) {
		    levelSeries++;
		    if (resource.getExtensionHandler().getHydrometricZero().isPresent()) {
			levelSeriesWithZero++;
		    }
		}
	    }
	}

	System.out.println("Records: " + resources.size() + ", rating curves: " + ratingCurves + ", river level series: " + levelSeries
		+ " (" + levelSeriesWithZero + " with hydrometric zero)");

	Assert.assertTrue(resources.size() > 1000);
	Assert.assertTrue(ratingCurves > 40);
	Assert.assertTrue(levelSeries > 70);
	Assert.assertTrue(levelSeriesWithZero > 30);
    }

    @Test
    public void downloadArchiveAndRealTimeTest() throws Exception {

	HISCentralLombardiaOpenDataMangler mangler = new HISCentralLombardiaOpenDataMangler();
	mangler.setSensorIdentifier(LEVEL_SENSOR);
	mangler.setProperty(LombardiaOpenDataProperty.LIVELLO_IDROMETRICO.name());

	HISCentralLombardiaOpenDataDownloader downloader = new HISCentralLombardiaOpenDataDownloader();
	downloader.setOnlineResource(dataset(mangler.getMangling(), LombardiaOpenDataClient.DEFAULT_ENDPOINT,
		CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_NS_URI), "online");
	Assert.assertTrue(downloader.canDownload());

	// validated archive, one day at 10 minutes
	List<ValueSingleVariable> archive = download(downloader, ISO8601DateTimeUtils.parseISO8601ToDate("2024-06-01T00:00:00Z").get(),
		ISO8601DateTimeUtils.parseISO8601ToDate("2024-06-01T23:59:59Z").get());
	System.out.println("Archive values: " + archive.size());
	Assert.assertTrue(archive.size() > 100);

	// real time data, last two days
	Date end = new Date();
	List<ValueSingleVariable> realTime = download(downloader, new Date(end.getTime() - 2 * 24 * 60 * 60 * 1000L), end);
	System.out.println("Real time values: " + realTime.size());
	Assert.assertTrue(realTime.size() > 100);
    }

    @Test
    public void downloadRatingCurveTest() throws Exception {

	HISCentralLombardiaOpenDataMangler mangler = new HISCentralLombardiaOpenDataMangler();
	mangler.setSensorIdentifier(RATING_CURVE_SENSOR);
	mangler.setProperty(HISCentralLombardiaOpenDataMangler.RATING_CURVE);

	HISCentralLombardiaOpenDataScaleDeflussoDownloader downloader = new HISCentralLombardiaOpenDataScaleDeflussoDownloader();
	downloader.setOnlineResource(dataset(mangler.getMangling(), //
		LombardiaWFSClient.getRatingCurvesURL(LombardiaWFSClient.DEFAULT_ENDPOINT, RATING_CURVE_SENSOR), //
		CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_SCALE_DEFLUSSO_NS_URI), "online");
	Assert.assertTrue(downloader.canDownload());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	File file = downloader.download(descriptor);
	String waterML = new String(Files.readAllBytes(file.toPath()));
	file.delete();

	Assert.assertTrue(waterML.contains("rgs:ConversionPeriod"));
	Assert.assertTrue(waterML.contains("rgs:TableTuple"));
    }

    private static Dataset dataset(String name, String linkage, String protocol) {

	Dataset dataset = new Dataset();
	GSSource source = new GSSource();
	source.setEndpoint(LombardiaOpenDataClient.DEFAULT_ENDPOINT);
	source.setUniqueIdentifier("lombardia-open-data");
	dataset.setSource(source);

	Online online = new Online();
	online.setIdentifier("online");
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(name);
	online.setProtocol(protocol);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(online);
	return dataset;
    }

    private static List<ValueSingleVariable> download(HISCentralLombardiaOpenDataDownloader downloader, Date begin, Date end) throws Exception {

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setTemporalDimension(begin, end);
	File file = downloader.download(descriptor);

	List<ValueSingleVariable> ret = new ArrayList<>();
	try (FileInputStream stream = new FileInputStream(file)) {
	    TimeSeriesResponseType response = JAXBWML.getInstance().parseTimeSeries(stream);
	    response.getTimeSeries().forEach(ts -> ts.getValues().forEach(v -> ret.addAll(v.getValue())));
	}
	file.delete();
	return ret;
    }
}
