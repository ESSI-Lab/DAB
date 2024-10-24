package eu.essi_lab.descriptor;

import org.junit.Assert;
import org.junit.Test;

import com.beust.jcommander.internal.Lists;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;
import eu.essi_lab.worflow.test.TestUtils;
import eu.essi_lab.workflow.processor.BooleanCapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;
import eu.essi_lab.workflow.processor.DescriptorUtils;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;

/**
 * @author Fabrizio
 */
public class DescriptorUtilsTest {

    @Test
    public void simulateDescriptorTest1() {

	DataDescriptor dataDescriptor = createDescriptor();

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.NO_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.NO_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	Assert.assertEquals(dataDescriptor.getSpatialDimensions(), simulatedDescriptor.getSpatialDimensions());
	Assert.assertEquals(dataDescriptor.getTemporalDimension(), simulatedDescriptor.getTemporalDimension());
	Assert.assertEquals(dataDescriptor.getOtherDimensions(), simulatedDescriptor.getOtherDimensions());

    }

    /**
     * Spatial subsetting
     * No resampling
     */
    @Test
    public void simulateDescriptorTest2() {

	DataDescriptor dataDescriptor = createDescriptor();

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.SPATIAL_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.NO_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	// ---------------------------
	//
	// checking spatial dimensions
	//
	//
	ContinueDimension originalDim1 = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	ContinueDimension simDim1 = simulatedDescriptor.getSpatialDimensions().get(0).getContinueDimension();

	Assert.assertNotEquals(originalDim1.getUpper(), simDim1.getUpper());
	// the algorithm changes only the upper
	Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());

	// resampling not requested
	Assert.assertEquals(originalDim1.getResolution(), simDim1.getResolution());

	// ----------------------------
	//
	// checking temporal dimensions
	//
	//
	Assert.assertEquals(dataDescriptor.getTemporalDimension(), simulatedDescriptor.getTemporalDimension());

	// ----------------------------
	//
	// checking other dimensions
	//
	//
	Assert.assertEquals(dataDescriptor.getOtherDimensions(), simulatedDescriptor.getOtherDimensions());

    }

    /**
     * Spatial subsetting
     * Spatial resampling
     */
    @Test
    public void simulateDescriptorTest3() {

	DataDescriptor dataDescriptor = createDescriptor();

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.SPATIAL_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.SPATIAL_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	// ---------------------------
	//
	// checking spatial dimensions
	//
	//
	ContinueDimension originalDim1 = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	ContinueDimension simDim1 = simulatedDescriptor.getSpatialDimensions().get(0).getContinueDimension();

	Assert.assertNotEquals(originalDim1.getUpper(), simDim1.getUpper());
	// the algorithm changes only the upper
	Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());

	// resampling requested
	Assert.assertNotEquals(originalDim1.getResolution(), simDim1.getResolution());

	// ----------------------------
	//
	// checking temporal dimensions
	//
	//
	Assert.assertEquals(dataDescriptor.getTemporalDimension(), simulatedDescriptor.getTemporalDimension());

	// ----------------------------
	//
	// checking other dimensions
	//
	//
	Assert.assertEquals(dataDescriptor.getOtherDimensions(), simulatedDescriptor.getOtherDimensions());

    }

    /**
     * Spatial subsetting
     * Spatial resampling (with null resolution)
     */
    @Test
    public void simulateDescriptorTest3_2() {

	DataDescriptor dataDescriptor = createDescriptor();

	ContinueDimension dim = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	dim.setResolution(null);

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.SPATIAL_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.SPATIAL_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	// ---------------------------
	//
	// checking spatial dimensions
	//
	//
	ContinueDimension originalDim1 = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	ContinueDimension simDim1 = simulatedDescriptor.getSpatialDimensions().get(0).getContinueDimension();

	Assert.assertNotEquals(originalDim1.getUpper(), simDim1.getUpper());
	// the algorithm changes only the upper
	Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());

	// resampling requested
	Assert.assertNotEquals(originalDim1.getResolution(), simDim1.getResolution());

	// ----------------------------
	//
	// checking temporal dimensions
	//
	//
	Assert.assertEquals(dataDescriptor.getTemporalDimension(), simulatedDescriptor.getTemporalDimension());

	// ----------------------------
	//
	// checking other dimensions
	//
	//
	Assert.assertEquals(dataDescriptor.getOtherDimensions(), simulatedDescriptor.getOtherDimensions());

    }

    /**
     * Temporal subsetting
     * No resampling
     */
    @Test
    public void simulateDescriptorTest4() {

	DataDescriptor dataDescriptor = createDescriptor();

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.TEMPORAL_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.NO_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	// ---------------------------
	//
	// checking spatial dimensions
	//
	//
	{
	    ContinueDimension originalDim1 = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	    ContinueDimension simDim1 = simulatedDescriptor.getSpatialDimensions().get(0).getContinueDimension();

	    Assert.assertEquals(originalDim1.getUpper(), simDim1.getUpper());
	    Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());
	    Assert.assertEquals(originalDim1.getResolution(), simDim1.getResolution());
	}

	// ----------------------------
	//
	// checking temporal dimensions
	//
	//
	{
	    ContinueDimension originalDim1 = dataDescriptor.getTemporalDimension().getContinueDimension();
	    ContinueDimension simDim1 = simulatedDescriptor.getTemporalDimension().getContinueDimension();

	    // temporal subsetting required
	    Assert.assertNotEquals(originalDim1.getUpper(), simDim1.getUpper());
	    // the algorithm changes only the upper
	    Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());

	    // no resampling required
	    Assert.assertEquals(originalDim1.getResolution(), simDim1.getResolution());
	}

	// ----------------------------
	//
	// checking other dimensions
	//
	//
	Assert.assertEquals(dataDescriptor.getOtherDimensions(), simulatedDescriptor.getOtherDimensions());

    }

    /**
     * Temporal subsetting
     * Temporal resampling
     */
    @Test
    public void simulateDescriptorTest5() {

	DataDescriptor dataDescriptor = createDescriptor();

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.TEMPORAL_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.TEMPORAL_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	// ---------------------------
	//
	// checking spatial dimensions
	//
	//
	{
	    ContinueDimension originalDim1 = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	    ContinueDimension simDim1 = simulatedDescriptor.getSpatialDimensions().get(0).getContinueDimension();

	    Assert.assertEquals(originalDim1.getUpper(), simDim1.getUpper());
	    Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());
	    Assert.assertEquals(originalDim1.getResolution(), simDim1.getResolution());
	}

	// ----------------------------
	//
	// checking temporal dimensions
	//
	//
	{
	    ContinueDimension originalDim1 = dataDescriptor.getTemporalDimension().getContinueDimension();
	    ContinueDimension simDim1 = simulatedDescriptor.getTemporalDimension().getContinueDimension();

	    // temporal subsetting required
	    Assert.assertNotEquals(originalDim1.getUpper(), simDim1.getUpper());
	    // the algorithm changes only the upper
	    Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());

	    // temporal resampling required
	    Assert.assertNotEquals(originalDim1.getResolution(), simDim1.getResolution());
	}
    }

    /**
     * Other subsetting
     * No resampling
     */
    @Test
    public void simulateDescriptorTest6() {

	DataDescriptor dataDescriptor = createDescriptor();

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.OTHER_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.NO_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	Assert.assertEquals(dataDescriptor.getSpatialDimensions(), simulatedDescriptor.getSpatialDimensions());
	Assert.assertEquals(dataDescriptor.getTemporalDimension(), simulatedDescriptor.getTemporalDimension());

	// ------------------------
	//
	// checking other dimension
	//
	//
	{
	    ContinueDimension originalDim1 = dataDescriptor.getOtherDimensions().get(0).getContinueDimension();
	    ContinueDimension simDim1 = simulatedDescriptor.getOtherDimensions().get(0).getContinueDimension();

	    // other subsetting required
	    Assert.assertNotEquals(originalDim1.getUpper(), simDim1.getUpper());
	    // the algorithm changes only the upper
	    Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());

	    // other resampling not required
	    Assert.assertEquals(originalDim1.getResolution(), simDim1.getResolution());

	    FiniteDimension originalDim2 = dataDescriptor.getOtherDimensions().get(1).getFiniteDimension();
	    FiniteDimension simDim2 = simulatedDescriptor.getOtherDimensions().get(1).getFiniteDimension();

	    // other subsetting required
	    Assert.assertNotEquals(originalDim2.getPoints(), simDim2.getPoints());
	}
    }

    /**
     * Other subsetting
     * Other resampling
     */
    @Test
    public void simulateDescriptorTest7() {

	DataDescriptor dataDescriptor = createDescriptor();

	// ------------------------------------------------

	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setDataTypeCapability(CapabilityElement.anyFromDataType(DataType.GRID));
	capabilities.setDataFormatCapability(CapabilityElement.anyFromDataFormat(DataFormat.IMAGE_PNG()));
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));

	capabilities.setSubsettingCapability(SubsettingCapability.OTHER_SUBSETTING());
	capabilities.setResamplingCapability(ResamplingCapability.OTHER_RESAMPLING());

	// ------------------------------------------------

	DataDescriptor simulatedDescriptor = DescriptorUtils.simulateDescriptor(dataDescriptor, capabilities);

	Assert.assertEquals(DataType.GRID, simulatedDescriptor.getDataType());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), simulatedDescriptor.getDataFormat());
	Assert.assertEquals(CRS.EPSG_3857(), simulatedDescriptor.getCRS());

	Assert.assertEquals(dataDescriptor.getSpatialDimensions(), simulatedDescriptor.getSpatialDimensions());
	Assert.assertEquals(dataDescriptor.getTemporalDimension(), simulatedDescriptor.getTemporalDimension());

	// ------------------------
	//
	// checking other dimension
	//
	//
	{
	    ContinueDimension originalDim1 = dataDescriptor.getOtherDimensions().get(0).getContinueDimension();
	    ContinueDimension simDim1 = simulatedDescriptor.getOtherDimensions().get(0).getContinueDimension();

	    // other subsetting required
	    Assert.assertNotEquals(originalDim1.getUpper(), simDim1.getUpper());
	    // the algorithm changes only the upper
	    Assert.assertEquals(originalDim1.getLower(), simDim1.getLower());

	    // other resampling required
	    Assert.assertNotEquals(originalDim1.getResolution(), simDim1.getResolution());

	    FiniteDimension originalDim2 = dataDescriptor.getOtherDimensions().get(1).getFiniteDimension();
	    FiniteDimension simDim2 = simulatedDescriptor.getOtherDimensions().get(1).getFiniteDimension();

	    // other subsetting required
	    Assert.assertNotEquals(originalDim2.getPoints(), simDim2.getPoints());
	}
    }

    @Test
    public void capFromInputTest() {

	DataDescriptor descriptor = createDescriptor();
	ProcessorCapabilities cap = DescriptorUtils.fromInputDescriptor(descriptor);

	Assert.assertEquals(DataType.GRID, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_GEOTIFF(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_4326(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(SubsettingCapability.NO_SUBSETTING(), cap.getSubsettingCapability());
	Assert.assertEquals(ResamplingCapability.NO_RESAMPLING(), cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest1() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	//
	// here any subsetting and any resampling is expected because the dimensions
	// are all equals, so no subsetting and no resampling is expected
	//

	Assert.assertEquals(SubsettingCapability.ANY_SUBSETTING(), cap.getSubsettingCapability());
	Assert.assertEquals(ResamplingCapability.ANY_RESAMPLING(), cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest2() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ContinueDimension dim = target.getSpatialDimensions().get(0).getContinueDimension();
	dim.setUpper(dim.getUpper().doubleValue() - 10.0);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(new SubsettingCapability(//
		new BooleanCapabilityElement(true), //
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(PresenceType.ANY)), //
		cap.getSubsettingCapability());

	Assert.assertEquals(ResamplingCapability.ANY_RESAMPLING(), cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest3() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ContinueDimension dim = target.getSpatialDimensions().get(0).getContinueDimension();
	dim.setUpper(dim.getUpper().doubleValue() - 10.0);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(new SubsettingCapability(//
		new BooleanCapabilityElement(true), //
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(PresenceType.ANY)), //
		cap.getSubsettingCapability());

	Assert.assertEquals(ResamplingCapability.ANY_RESAMPLING(), cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest4() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ContinueDimension dim = target.getTemporalDimension().getContinueDimension();
	dim.setLower(dim.getLower().doubleValue() - 10.0);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(new SubsettingCapability(//
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(true), //
		new BooleanCapabilityElement(PresenceType.ANY)), //
		cap.getSubsettingCapability());

	Assert.assertEquals(ResamplingCapability.ANY_RESAMPLING(), cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest5() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ContinueDimension dim = target.getOtherDimensions().get(0).getContinueDimension();
	dim.setUpper(dim.getLower().doubleValue() - 10.0);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(new SubsettingCapability(//
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(true)), //
		cap.getSubsettingCapability());

	Assert.assertEquals(ResamplingCapability.ANY_RESAMPLING(), cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest6() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	FiniteDimension dim = target.getOtherDimensions().get(1).getFiniteDimension();
	dim.getPoints().set(0, "10");

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(new SubsettingCapability(//
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(true)), //
		cap.getSubsettingCapability());

	Assert.assertEquals(ResamplingCapability.ANY_RESAMPLING(), cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest7() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ContinueDimension dim = target.getSpatialDimensions().get(0).getContinueDimension();
	dim.setResolution(10);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(SubsettingCapability.ANY_SUBSETTING(), cap.getSubsettingCapability());

	Assert.assertEquals(new ResamplingCapability(//

		new BooleanCapabilityElement(true), //
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(PresenceType.ANY)), //

		cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest8() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ContinueDimension dim = target.getTemporalDimension().getContinueDimension();
	dim.setResolution(10);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(SubsettingCapability.ANY_SUBSETTING(), cap.getSubsettingCapability());

	Assert.assertEquals(new ResamplingCapability(//

		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(true), //
		new BooleanCapabilityElement(PresenceType.ANY)), //

		cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest9() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	ContinueDimension dim = target.getOtherDimensions().get(0).getContinueDimension();
	dim.setResolution(10);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(SubsettingCapability.ANY_SUBSETTING(), cap.getSubsettingCapability());

	Assert.assertEquals(new ResamplingCapability(//

		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(PresenceType.ANY), //
		new BooleanCapabilityElement(true)), //

		cap.getResamplingCapability());
    }

    @Test
    public void capFromTargetTest10() {

	DataDescriptor descriptor = createDescriptor();
	DataDescriptor target = createTargetDescriptor();

	descriptor.setSpatialDimensions(Lists.newArrayList());
	target.setSpatialDimensions(Lists.newArrayList());

	descriptor.setOtherDimensions(Lists.newArrayList());
	target.setOtherDimensions(Lists.newArrayList());

	descriptor.setTemporalDimension(null);
	target.setTemporalDimension(null);

	ProcessorCapabilities cap = DescriptorUtils.fromTargetDescriptor(descriptor, target);

	Assert.assertEquals(DataType.TIME_SERIES, cap.getDataTypeCapability().getFirstValue());
	Assert.assertEquals(DataFormat.IMAGE_PNG(), cap.getDataFormatCapability().getFirstValue());
	Assert.assertEquals(CRS.EPSG_3857(), cap.getCRSCapability().getFirstValue());

	Assert.assertEquals(SubsettingCapability.ANY_SUBSETTING(), cap.getSubsettingCapability());
	Assert.assertEquals(ResamplingCapability.ANY_RESAMPLING(), cap.getResamplingCapability());
    }

    private DataDescriptor createDescriptor() {

	return TestUtils.create(DataType.GRID, CRS.EPSG_4326(), DataFormat.IMAGE_GEOTIFF());
    }

    private DataDescriptor createTargetDescriptor() {

	return TestUtils.create(DataType.TIME_SERIES, CRS.EPSG_3857(), DataFormat.IMAGE_PNG());
    }
}
