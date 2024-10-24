package eu.essi_lab.validator.wof.test;

import java.io.InputStream;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.validator.wof.WML_1_1Validator;

public class WML_1_1ValidatorTest {

    private WML_1_1Validator validator;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {
	this.validator = new WML_1_1Validator();
    }

    private void test(String resource, DataDescriptor descriptor, Boolean expectedToPass, DataValidatorErrorCode expectedErrorCode)
	    throws Exception {
	DataObject dataObject = new DataObject();
	try {
	    InputStream stream = WML_1_1ValidatorTest.class.getClassLoader().getResourceAsStream(resource);
	    dataObject.setFileFromStream(stream, "WML_1_1ValidatorTest");
	    dataObject.setDataDescriptor(descriptor);
	    ValidationMessage result = validator.validate(dataObject);
	    if (dataObject.getFile() != null) {
		dataObject.getFile().delete();
	    }
	    if (expectedToPass) {
		Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result.getResult());
	    } else {
		Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result.getResult());
		Assert.assertEquals(expectedErrorCode.toString(), result.getErrorCode());
	    }
	} catch (Exception e) {
	    if (dataObject.getFile() != null) {
		dataObject.getFile().delete();
	    }
	    throw e;
	}
    }

    @Test
    public void testNullDescriptor() throws Exception {
	DataDescriptor descriptor = null;
	test("", descriptor, false, DataValidatorErrorCode.INVALID_DESCRIPTOR);
    }

    @Test
    public void testNullFormat() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	test("", descriptor, false, DataValidatorErrorCode.INVALID_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedFormat1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.GML_3_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedFormat2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testNullDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.GRID);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedCRS() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_3857());
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testCorruptedXML() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("wml-1.xml", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    }

    @Test
    public void testInvalidRootXML() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("wml-2.xml", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    }

    @Test
    public void testDontCareValidation() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("wml-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidation1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	test("wml-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidation2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	test("wml-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidation3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	test("wml-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidation4() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidationFailedBadDescriptor1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.518473, -111.946402); // changed spatial dimensions
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1342, 1345); // changed vertical dimension
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200001l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7)))); // changed
															       // temporal
															       // dimension
	test("wml-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-4.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-5.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

// @Test
    public void testValidationFailedBadDocument3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-6.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument4() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-7.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument5() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-8.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument6() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("wml-9.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

//    @Test
    public void testValidationPassedARGENTINADocument() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(-27.4375, -54.3553);
	descriptor.setTemporalDimension(new Date(1144911600000l), new Date(1556506800000l));
	test("wml-argentina.xml", descriptor, true, null);
    }

}
