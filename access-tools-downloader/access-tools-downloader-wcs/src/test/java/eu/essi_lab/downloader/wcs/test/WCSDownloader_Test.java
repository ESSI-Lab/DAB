package eu.essi_lab.downloader.wcs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Assert;

import eu.essi_lab.downloader.wcs.WCSDownloader;
import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader;
import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader_100;
import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader_111;
import eu.essi_lab.downloader.wcs.test.mocked.WCSMockedDownloader_201;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataFormat.FormatType;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.validator.geotiff.GeoTIFFValidator;

public abstract class WCSDownloader_Test {

    public abstract NetProtocol getProtocol();

    public static Double TOL = Math.pow(10, -10);

    protected WCSDownloader downloader;

    public boolean isMockedDownload() {
	return false;
    }

    public void test() throws Exception {

	Dataset dataset = new Dataset();
	String onlineResourceId = "online-id";
	Online onLine = getOnline(onlineResourceId);

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(onLine);
	downloader.setOnlineResource(dataset, onlineResourceId);
	List<DataDescriptor> descriptors = downloader.getRemoteDescriptors();

	Assert.assertTrue(descriptors.size() >= getMinimumExpectedDescriptors());

	DataDescriptor descriptor = chooseDescriptor(descriptors);

	assertNotNull(descriptor);

	assertGoodDescriptor(descriptor);

	Assert.assertEquals(DataFormat.IMAGE_GEOTIFF(), descriptor.getDataFormat());

	// reduce descriptor before downloading to prevent out of memory error on full coverage request
	if (reduceDimensions()) {
	    reduceDimension(descriptor.getFirstSpatialDimension());
	    reduceDimension(descriptor.getSecondSpatialDimension());
	}

	URL downloadURL = downloader.getDownloadURL(descriptor);

	assertEquals(getDownloadURL(), downloadURL.toString());

	// Downloads and validates the descriptor
	File tmp = downloader.download(descriptor);

	tmp.deleteOnExit();

	validateDownload(tmp, descriptor);

	tmp.delete();

    }

    public boolean reduceDimensions() {
	return false;
    }

    public abstract String getDownloadURL();

    public DataDescriptor chooseDescriptor(List<DataDescriptor> descriptors) {
	DataFormat desiredFormat = new DataFormat(FormatType.IMAGE_GEOTIFF);

	DataDescriptor chosenDescriptor = null;

	for (DataDescriptor descriptor : descriptors) {
	    DataFormat format = descriptor.getDataFormat();
	    if (format.equals(desiredFormat)) {
		return descriptor;
	    }
	    // to choose GeoTIFF or TIFF if present
	    if (desiredFormat.isSubTypeOf(format)) {
		chosenDescriptor = descriptor;
	    }
	}

	return chosenDescriptor;
    }

    public int getMinimumExpectedDescriptors() {
	return 1;
    }

    protected abstract void assertGoodDescriptor(DataDescriptor descriptor) throws Exception;

    public abstract Online getOnline(String onlineResourceId);

    public void assertDimension(DataDimension dimension, Double origin, Double res, Long size, String name) {
	double otherPoint = origin + res * size - res;
	double min = Math.min(origin, otherPoint);
	double max = Math.max(origin, otherPoint);
	Assert.assertEquals(Math.abs(res), dimension.getContinueDimension().getResolution().doubleValue(), TOL);
	Assert.assertEquals(min, dimension.getContinueDimension().getLower().doubleValue(), TOL);
	Assert.assertEquals(max, dimension.getContinueDimension().getUpper().doubleValue(), TOL);
	if (name != null) {
	    Assert.assertTrue(dimension.getContinueDimension().getName().toLowerCase().contains(name));
	}
	Assert.assertEquals(size, dimension.getContinueDimension().getSize());

    }

    protected void initMockedDownloader(WCSMockedDownloader mock) {

	NetProtocol protocol = getProtocol();
	if (protocol.equals(NetProtocolWrapper.WCS_1_0_0.get())) {
	   this.downloader = new WCSMockedDownloader_100(mock);
	} else if (protocol.equals(NetProtocolWrapper.WCS_1_1_1.get())) {
	    this.downloader = new WCSMockedDownloader_111(mock);
	} else if (protocol.equals(NetProtocolWrapper.WCS_2_0_1.get())) {
	    this.downloader = new WCSMockedDownloader_201(mock);
	} else {
	    fail();
	}

    }

    public void validateDownload(File tmp, DataDescriptor descriptor) {
	try {
	    DataObject dataObject = new DataObject();
	    dataObject.setFile(tmp);
	    dataObject.setDataDescriptor(descriptor);
	    GeoTIFFValidator validator = new GeoTIFFValidator();
	    ValidationMessage result = validator.validate(dataObject);
	    tmp.delete();
	    System.out.println("Validation result: " + result.getResult());
	    if (!result.getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL)) {
		System.out.println("Error: " + result.getError());
		System.out.println("Error code: " + result.getErrorCode());
	    }

	    assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result.getResult());
	} catch (Exception e) {
	    tmp.delete();
	}

    }

    public void reduceDimension(DataDimension dimension) {
	Double min = dimension.getContinueDimension().getLower().doubleValue();
	Double max = dimension.getContinueDimension().getUpper().doubleValue();
	Double res = dimension.getContinueDimension().getResolution().doubleValue();
	Long size = dimension.getContinueDimension().getSize();
	double envelopeMin = min - res / 2.0;
	double envelopeMax = max + res / 2.0;
	size = size / 100;
	res = (envelopeMax - envelopeMin) / (size);
	min = envelopeMin + res / 2.0;
	max = envelopeMax - res / 2.0;

	dimension.getContinueDimension().setLower(min);
	dimension.getContinueDimension().setUpper(max);
	dimension.getContinueDimension().setResolution(res);
	dimension.getContinueDimension().setSize(size);

    }

}
