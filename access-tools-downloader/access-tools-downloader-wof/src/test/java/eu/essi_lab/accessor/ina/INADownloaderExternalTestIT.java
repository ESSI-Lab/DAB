package eu.essi_lab.accessor.ina;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.Value;
import eu.essi_lab.downloader.ina.INADownloader;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import junit.framework.TestCase;

public class INADownloaderExternalTestIT {
    protected INADownloader downloader;

    @Before
    public void init() throws GSException, UnsupportedEncodingException, JAXBException {
	this.downloader = new INADownloader();
	Dataset dataset = new Dataset();
	Online online = new Online();
	online.setIdentifier("id1");
	online.setLinkage(getEndpoint());
	online.setName("parameter;INA:29;platform;alturas_bdhi:2");
	online.setProtocol(NetProtocols.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN());
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(online);
	downloader.setOnlineResource(dataset, "id1");
    }

    protected String getEndpoint() {
	return "https://alerta.ina.gob.ar/wml";
    }

    @Test
    public void testDownloader() throws Exception {
	// can download
	Assert.assertTrue(downloader.canDownload());

	// available data descriptor
	List<DataDescriptor> descriptors = downloader.getRemoteDescriptors();

	Assert.assertEquals(1, descriptors.size());

	DataDescriptor descriptor = descriptors.get(0);

	DataDescriptor expected = new DataDescriptor();
	expected.setDataType(DataType.TIME_SERIES);
	expected.setDataFormat(DataFormat.WATERML_1_1());
	expected.setCRS(CRS.EPSG_4326());
	expected.setEPSG4326SpatialDimensions(-32.3085, -59.0768055555556);
	expected.setTemporalDimension(new Date(596293200000l), new Date(1468422000000l));
	//no resolution
	expected.getTemporalDimension().getContinueDimension().setResolution(null);
	// no vertical extent
	//expected.setVerticalDimension(null, null);
	//no other dimension
	//expected.getOtherDimensions().get(0).getContinueDimension().setDatum(new Datum("NGVD29"));

	Assert.assertEquals(expected, descriptor);

	//descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));

	double elevationDouble = 1345.;
	descriptor.setVerticalDimension(elevationDouble, elevationDouble);
	String datum = "NGVD29";
	ContinueDimension verticalDimension = descriptor.getOtherDimensions().get(0).getContinueDimension();
	verticalDimension.setDatum(new Datum(datum));

	// data download
	File tmpFile = downloader.download(descriptor);

	FileInputStream fis = new FileInputStream(tmpFile);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(fis, baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

	fis.close();
	tmpFile.delete();
	baos.close();

	TimeSeriesResponseDocument timeSeriesResponse = new TimeSeriesResponseDocument(bais);
	TimeSeries timeSeries = timeSeriesResponse.getTimeSeries().get(0);

	Value value = timeSeries.getValues().get(0);
	//TestCase.assertEquals("nc", value.getCensorCode());
	TestCase.assertEquals("1988-11-23T10:00:00", value.getDateTime());
	TestCase.assertEquals("-03:00", value.getTimeOffset());
	TestCase.assertEquals("1988-11-23T13:00:00", value.getDateTimeUTC());
	TestCase.assertEquals("", value.getMethodCode());
	TestCase.assertEquals("alturas_bdhi", value.getSourceCode());
	TestCase.assertEquals("", value.getQualityControlLevelCode());
	TestCase.assertEquals("13.77", value.getValue());

    }
}
