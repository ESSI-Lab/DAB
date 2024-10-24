package eu.essi_lab.worflow.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.IdentityProcessor;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;

public class TestUtils {

    private static boolean enable = false;

    public static final int RANDOM_ITERATIONS = 1000;

    public static void enablePrinting(boolean enable) {

	TestUtils.enable = enable;
    }

    public static void printWorkflows(List<Workflow> workflows) {

	if (TestUtils.enable) {

	    workflows.forEach(v -> System.out.println(v.getWorkblocks()));
	}
    }

    public static Workblock createBlock(String id, String inputCrs, String intputFormat, String outputCrs, String outputFormat) {

	ProcessorCapabilities input = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsString(inputCrs), //
		CapabilityElement.anyFromDataFormat(intputFormat), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	ProcessorCapabilities output = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsString(outputCrs), //
		CapabilityElement.anyFromDataFormat(outputFormat), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	IdentityProcessor process = new IdentityProcessor(input, output);

	return new Workblock(id, process, null);
    }

    public static Workblock createBlock(String id, String inputCrs, String outputCrs) {

	ProcessorCapabilities input = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsString(inputCrs), //
		CapabilityElement.anyFromDataFormat(""), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	ProcessorCapabilities output = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsString(outputCrs), //
		CapabilityElement.anyFromDataFormat(""), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	IdentityProcessor process = new IdentityProcessor(input, output);

	return new Workblock(id, process, null);
    }

    public static Workblock createBlock(String id, List<String> inputCrs, List<String> inputFormats, String outputCrs,
	    String outputFormat) {

	ProcessorCapabilities input = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsStrings(inputCrs), //
		CapabilityElement.anyFromDataFormatAsString(inputFormats), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	ProcessorCapabilities output = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsString(outputCrs), //
		CapabilityElement.anyFromDataFormat(outputFormat), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	IdentityProcessor process = new IdentityProcessor(input, output);

	return new Workblock(id, process, null);
    }

    public static Workblock createBlock(String id, List<String> inputCrs, List<String> inputFormats, List<String> outputCrs,
	    List<String> outputFormats) {

	ProcessorCapabilities input = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsStrings(inputCrs), //
		CapabilityElement.anyFromDataFormatAsString(inputFormats), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	List<String> outF = outputFormats.stream().//
		filter(f -> f != null && !f.equals("")).//
		collect(Collectors.toList());//

	List<String> outC = outputCrs.stream().//
		filter(c -> c != null && !c.equals("")).//
		collect(Collectors.toList());

	ProcessorCapabilities output = ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(DataType.POINT), //
		CapabilityElement.anyFromCRSAsStrings(outC), //
		CapabilityElement.anyFromDataFormatAsString(outF), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());

	IdentityProcessor process = new IdentityProcessor(input, output);

	return new Workblock(id, process, null);
    }

    /**
     * @param dataType
     * @param crs
     * @param format
     * @return
     */
    public static DataDescriptor create(DataType dataType, String crs, String format) {

	return create(dataType, CRS.fromIdentifier(crs), DataFormat.fromIdentifier(format));
    }

    /**
     * @param crs
     * @param format
     * @return
     */
    public static DataDescriptor create(DataType dataType, CRS crs, DataFormat format) {

	DataDescriptor dataDescriptor = new DataDescriptor();
	dataDescriptor.setDataType(dataType);

	if (crs != null) {
	    dataDescriptor.setCRS(crs);
	}

	if (format != null) {
	    dataDescriptor.setDataFormat(format);
	}

	// ------------------
	//
	// spatial dimensions
	//
	dataDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d1 = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d1.setResolution(1.);
	d1.setSize(21l);

	ContinueDimension d2 = dataDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d2.setResolution(1.);
	d2.setSize(21l);

	// ------------------
	//
	// temporal dimension
	//
	ContinueDimension timeDimension = new ContinueDimension("time");
	timeDimension.setUom(Unit.SECOND);
	timeDimension.setLower(5);
	timeDimension.setUpper(10);
	timeDimension.setResolution(1.0);
	timeDimension.setType(DimensionType.TIME);
	timeDimension.setSize(6l);

	dataDescriptor.setTemporalDimension(timeDimension);

	// ------------------
	//
	// other dimensions
	//
	ContinueDimension verticalDimension = new ContinueDimension("vertical");
	verticalDimension.setUom(Unit.METRE);
	verticalDimension.setLower(5);
	verticalDimension.setUpper(10);
	verticalDimension.setResolution(1.0);
	verticalDimension.setSize(6l);
	verticalDimension.setType(DimensionType.COLUMN);

	FiniteDimension lineDimension = new FiniteDimension("line");
	lineDimension.setPoints(Arrays.asList("0", "1", "2", "3"));
	lineDimension.setType(DimensionType.LINE);

	dataDescriptor.setOtherDimensions(//
		Arrays.asList(//
			verticalDimension, //
			lineDimension));

	return dataDescriptor;
    }
}
