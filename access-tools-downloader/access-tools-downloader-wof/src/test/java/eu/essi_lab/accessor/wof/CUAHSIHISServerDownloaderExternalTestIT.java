package eu.essi_lab.accessor.wof;

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

import eu.essi_lab.accessor.wof.client.CUAHSIEndpoints;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.Value;
import eu.essi_lab.downloader.wof.CUAHSIHISServerDownloader;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import junit.framework.TestCase;

public class CUAHSIHISServerDownloaderExternalTestIT {
    protected CUAHSIHISServerDownloader downloader;

    @Before
    public void init() throws GSException, UnsupportedEncodingException, JAXBException {
	this.downloader = new CUAHSIHISServerDownloader();
	Dataset dataset = new Dataset();
	Online online = new Online();
	online.setIdentifier("id1");
	online.setLinkage(getEndpoint());
	online.setName("platform;LBR:USU-LBR-Mendon;parameter;LBR:USU3");
	online.setProtocol(NetProtocolWrapper.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN());
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(online);
	downloader.setOnlineResource(dataset, "id1");
    }

    protected String getEndpoint() {
	return CUAHSIEndpoints.ENDPOINT4;
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
	expected.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	expected.setTemporalDimension(new Date(1123180200000l), new Date(1470166200000l));
	expected.getTemporalDimension().getContinueDimension().setResolution(1800000l);
	expected.getFirstSpatialDimension().getContinueDimension().setSize(1l);
	expected.getSecondSpatialDimension().getContinueDimension().setSize(1l);

	expected.setVerticalDimension(1345., 1345.);
	expected.getOtherDimensions().get(0).getContinueDimension().setDatum(new Datum("NGVD29"));

	Assert.assertEquals(expected, descriptor);

	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));

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
	TestCase.assertEquals("nc", value.getCensorCode());
	TestCase.assertEquals("2005-08-04T11:30:00", value.getDateTime());
	TestCase.assertEquals("-07:00", value.getTimeOffset());
	TestCase.assertEquals("2005-08-04T18:30:00", value.getDateTimeUTC());
	TestCase.assertEquals("4", value.getMethodCode());
	TestCase.assertEquals("1", value.getSourceCode());
	TestCase.assertEquals("0", value.getQualityControlLevelCode());
	TestCase.assertEquals("13.24467", value.getValue());

    }
}
