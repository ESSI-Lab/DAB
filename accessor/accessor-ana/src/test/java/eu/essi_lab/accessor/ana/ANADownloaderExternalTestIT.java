package eu.essi_lab.accessor.ana;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.ana.download.ANADownloader;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.validator.wof.WML_1_1Validator;

public class ANADownloaderExternalTestIT {
    protected ANADownloader downloader;

    @Before
    public void init() throws GSException, UnsupportedEncodingException, JAXBException {
	this.downloader = new ANADownloader();
	Dataset dataset = new Dataset();
	GSSource source = new GSSource();
	source.setEndpoint(getEndpoint());
	dataset.setSource(source);
	Online online = new Online();
	online.setIdentifier("parameter;Nivel;station;87168000");
	online.setLinkage(getEndpoint());
	online.setName("Nivel@87168000@SÃO VENDELINO");
	online.setProtocol(CommonNameSpaceContext.ANA_URI);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(online);
	//dataset.getSource().setEndpoint(getEndpoint());
	downloader.setOnlineResource(dataset, "parameter;Nivel;station;87168000");
    }

    protected String getEndpoint() {
	return "http://telemetriaws1.ana.gov.br/ServiceANA.asmx";
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
//	expected.setEPSG4326SpatialDimensions(-29.38280, -51.36890);
	//expected.setTemporalDimension(new Date(1554415500000l), new Date(1557179700000l));
	descriptor.setTemporalDimension(null);
	//expected.getTemporalDimension().getContinueDimension().setResolution(1800000l);

//	expected.setVerticalDimension(64.91, 64.91);
	//expected.getOtherDimensions().get(0).getContinueDimension().setDatum(new Datum("NGVD29"));

	Assert.assertEquals(expected, descriptor);

//	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
//
//	double elevationDouble = 1345.;
//	descriptor.setVerticalDimension(elevationDouble, elevationDouble);
//	String datum = "NGVD29";
//	ContinueDimension verticalDimension = descriptor.getOtherDimensions().get(0).getContinueDimension();
//	verticalDimension.setDatum(new Datum(datum));
//
//	// data download
	
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
}
