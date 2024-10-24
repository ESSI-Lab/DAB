package eu.essi_lab.accessor.inmet;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.accessor.inmet.download.INMETDownloader;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.validator.wof.WML_1_1Validator;

public class INMETDownloaderExternalTestIT {

    private INMETConnector connector;
    private INMETDownloader downloader;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() throws Exception {
	this.connector = new INMETConnector();
	this.downloader = new INMETDownloader();
    }

    @Test
    public void test() throws Exception {

	Dataset dataset = new Dataset();
	String onlineResourceId = "online-id";
	Online onLine = getOnline(onlineResourceId);

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(onLine);
	GSSource source = new GSSource();
	source.setEndpoint("ftp://ftp.inmet.gov.br/");
	dataset.setSource(source);
	downloader.setOnlineResource(dataset, onlineResourceId);
	List<DataDescriptor> descriptors = downloader.getRemoteDescriptors();

	Assert.assertTrue(descriptors.size() >= getMinimumExpectedDescriptors());

	DataDescriptor descriptor = descriptors.get(0);
	//
	// assertNotNull(descriptor);
	//
	// assertGoodDescriptor(descriptor);
	//
	Assert.assertEquals(DataFormat.WATERML_1_1(), descriptor.getDataFormat());

	File tmp = downloader.download(descriptor);

	tmp.deleteOnExit();

	validateDownload(tmp, descriptor);

	tmp.delete();

    }

    private void validateDownload(File tmp, DataDescriptor descriptor) {
	try {
	    DataObject dataObject = new DataObject();
	    dataObject.setFile(tmp);
	    dataObject.setDataDescriptor(descriptor);
	    WML_1_1Validator validator = new WML_1_1Validator();
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

    private Online getOnline(String onlineResourceId) {
	Online onLine = new Online();
	onLine.setLinkage("ftp://ftp.inmet.gov.br/");
	onLine.setName("Precipitation_HOURLY@BRASILIA@PREC_A001_20180927.HIS.CSV");
	onLine.setIdentifier(onlineResourceId);
	onLine.setProtocol(CommonNameSpaceContext.INMET_CSV_URI);
	return onLine;
    }

    public int getMinimumExpectedDescriptors() {
	return 1;
    }

}
