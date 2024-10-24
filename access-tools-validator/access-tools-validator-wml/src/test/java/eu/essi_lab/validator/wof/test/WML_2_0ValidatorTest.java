package eu.essi_lab.validator.wof.test;

import java.io.InputStream;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.validator.wof.WML_2_0Validator;

public class WML_2_0ValidatorTest {

    private WML_2_0Validator validator;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {
	this.validator = new WML_2_0Validator();
    }

    private void test(String resource, DataDescriptor descriptor, Boolean expectedToPass, DataValidatorErrorCode expectedErrorCode)
	    throws Exception {
	DataObject dataObject = new DataObject();
	try {
	    InputStream stream = WML_2_0ValidatorTest.class.getClassLoader().getResourceAsStream(resource);
	    dataObject.setFileFromStream(stream, getClass().getSimpleName());
	    dataObject.setDataDescriptor(descriptor);
	    ValidationMessage result = validator.validate(dataObject);
	    if (dataObject.getFile() != null) {
		dataObject.getFile().delete();
	    }
	    if (expectedToPass) {
		if (result.getError() != null) {
		    GSLoggerFactory.getLogger(getClass()).info(result.getError());
		}
		if (result.getErrorCode() != null) {
		    GSLoggerFactory.getLogger(getClass()).info(result.getErrorCode());
		}
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
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testNullDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.GRID);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testCorruptedXML() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("wml2-1.xml", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    }

    @Test
    public void testInvalidRootXML() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("wml2-2.xml", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    }

    @Test
    public void testDontCareValidation() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("wml2-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidation1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	test("wml2-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidation2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	test("wml2-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidation3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-3.xml", descriptor, true, null);
    }

    @Test
    public void testValidationFailedBadDescriptor1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setCRS(CRS.EPSG_3857()); // CHANGED
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.518473, -111.946402); // changed spatial dimensions
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(4320000l); // CHANGED
	// descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor4() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	// descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(32l); // CHANGED
	test("wml2-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor5() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200001l)); // CHANGED
	// descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	// descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor6() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200001l), new Date(275893200000l)); // CHANGED
	// descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	// descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-3.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-4.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-5.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-6.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument4() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-7.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument5() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-8.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDocument6() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_2_0());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(40.08566628, -75.13517478);
	descriptor.setTemporalDimension(new Date(273301200000l), new Date(275893200000l));
	descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000l);
	descriptor.getTemporalDimension().getContinueDimension().setSize(31l);
	test("wml2-9.xml", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

}
