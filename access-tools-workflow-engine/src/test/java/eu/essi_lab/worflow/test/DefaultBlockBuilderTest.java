package eu.essi_lab.worflow.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.workflow.builder.WorkblockBuilder;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.IdentityProcessor;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;

public class DefaultBlockBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void test1() throws Exception {

	WorkblockBuilder builder = new WorkblockBuilder() {
	    @Override
	    public void init() {

		setCRS(CapabilityElement.anyFromCRS(CRS.EPSG_4326()), CapabilityElement.anyFromCRS(CRS.EPSG_3857()));
		setFormat(CapabilityElement.anyFromDataFormat(DataFormat.GML_3_1()), CapabilityElement.anyFromDataFormat(DataFormat.GML_3_2()));

		setSubsetting(SubsettingCapability.NO_SUBSETTING(), SubsettingCapability.NO_SUBSETTING());
		setResampling(ResamplingCapability.NO_RESAMPLING(), ResamplingCapability.NO_RESAMPLING());

	    }

	    @Override
	    protected DataProcessor createProcessor() {

		return new IdentityProcessor();
	    }
	};

	builder.build();
    }

    @Test
    public void test2() throws Exception {

	exception.expect(IllegalArgumentException.class);

	WorkblockBuilder builder = new WorkblockBuilder() {

	    @Override
	    public void init() {

		// builder.setCrs(CapabilityElement.fromCRS(CRS.EPSG_4326()),CapabilityElement.fromCRS(CRS.EPSG_3857()));
		setFormat(CapabilityElement.anyFromDataFormat(DataFormat.GML_3_1()), CapabilityElement.anyFromDataFormat(DataFormat.GML_3_2()));

		setSubsetting(SubsettingCapability.NO_SUBSETTING(), SubsettingCapability.NO_SUBSETTING());
		setResampling(ResamplingCapability.NO_RESAMPLING(), ResamplingCapability.NO_RESAMPLING());

	    }

	    @Override
	    protected DataProcessor createProcessor() {

		return new IdentityProcessor();
	    }
	};

	builder.build();
    }

    @Test
    public void test3() throws Exception {
	exception.expect(IllegalArgumentException.class);

	WorkblockBuilder builder = new WorkblockBuilder() {
	    @Override
	    public void init() {

		setCRS(CapabilityElement.anyFromCRS(CRS.EPSG_4326()), CapabilityElement.anyFromCRS(CRS.EPSG_3857()));
		// builder.setFormat(CapabilityElement.fromDataFormat(DataFormat.GML_3_1),new
		// CapabilityElement<DataFormat>(DataFormat.GML_3_2));

		setSubsetting(SubsettingCapability.NO_SUBSETTING(), SubsettingCapability.NO_SUBSETTING());
		setResampling(ResamplingCapability.NO_RESAMPLING(), ResamplingCapability.NO_RESAMPLING());

	    }

	    @Override
	    protected DataProcessor createProcessor() {

		return new IdentityProcessor();
	    }
	};

	builder.build();
    }

    @Test
    public void test4() throws Exception {
	exception.expect(IllegalArgumentException.class);

	WorkblockBuilder builder = new WorkblockBuilder() {
	    @Override
	    public void init() {
		setCRS(CapabilityElement.anyFromCRS(CRS.EPSG_4326()), CapabilityElement.anyFromCRS(CRS.EPSG_3857()));
		setFormat(CapabilityElement.anyFromDataFormat(DataFormat.GML_3_1()), CapabilityElement.anyFromDataFormat(DataFormat.GML_3_2()));

		// builder.setSubsetting(SubsettingCapability.NO_SUBSETTING(), SubsettingCapability.NO_SUBSETTING());
		setResampling(ResamplingCapability.NO_RESAMPLING(), ResamplingCapability.NO_RESAMPLING());

	    }

	    @Override
	    protected DataProcessor createProcessor() {

		return new IdentityProcessor();
	    }
	};

	builder.build();
    }

    @Test
    public void test5() throws Exception {
	exception.expect(IllegalArgumentException.class);

	WorkblockBuilder builder = new WorkblockBuilder() {
	    @Override
	    public void init() {
		setCRS(CapabilityElement.anyFromCRS(CRS.EPSG_4326()), CapabilityElement.anyFromCRS(CRS.EPSG_3857()));
		setFormat(CapabilityElement.anyFromDataFormat(DataFormat.GML_3_1()), CapabilityElement.anyFromDataFormat(DataFormat.GML_3_2()));

		setSubsetting(SubsettingCapability.NO_SUBSETTING(), SubsettingCapability.NO_SUBSETTING());
		// builder.setResampling(ResamplingCapability.NO_RESAMPLING(), ResamplingCapability.NO_RESAMPLING());
	    }

	    @Override
	    protected DataProcessor createProcessor() {

		return new IdentityProcessor();
	    }
	};

	builder.build();
    }
}
