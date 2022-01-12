package eu.essi_lab.access.compliance.wrapper;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.iso.datamodel.DOMSerializer;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;
import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.processor.IdentityProcessor;
@XmlRootElement(name = "report", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
@XmlType(propOrder = { //
	"onlineId", //
	"timeStamp", //
	"targetLevelLabel", //
	"targetTest", //
	"lastSucceededTest", //
	"previewDescriptor", //
	"fullDescriptor", //
	"basicTestResult", //
	"downloadTestResult", //
	"validationTestResult", //
	"executionTestResult" //
})
public class ReportWrapper extends DOMSerializer {

    private static JAXBContext context;
    private DataComplianceReport report;
    private static final String EXECUTION_TEST = ":executionTest/";

    static {
	try {
	    context = JAXBContext.newInstance(ReportWrapper.class);
	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(ReportWrapper.class).error("Can't get jaxb context", e);

	}
    }

    /**
     *
     */
    public ReportWrapper() {
    }

    /**
     * @param node
     * @return
     * @throws ParseException
     */
    public static DataComplianceReport wrap(Node node) throws ParseException {

	XMLDocumentReader reader = new XMLDocumentReader(node.getOwnerDocument());
	reader.setNamespaceContext(new CommonNameSpaceContext());
	DataComplianceReport report = null;
	try {

	    // ------------------------------
	    //
	    // data descriptors
	    //

	    DataDescriptor previewDescriptor = wrapDescriptor(true, reader, node);
	    DataDescriptor fullDescriptor = wrapDescriptor(false, reader, node);

	    // tests
	    String lastSuccededTest = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":lastSucceededTest");
	    String targetTest = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":targetTest");

	    // basic test
	    String workflowsCount = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":basicTest/"
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":workflowsCount");

	    String workflowsLength = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":basicTest/"
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":workflowsLength");

	    // download test
	    String isDownloadable = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":downloadTest/"
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":downloadable");
	    String downloadTimeLong = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":downloadTest/"
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":downloadTimeLong");

	    // validation test
	    String validationResult = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":validationTest/"
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":validationResult");

	    String validationError = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":validationTest/"
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":validationError");

	    // execution test
	    String executionResult = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + EXECUTION_TEST
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":executionResult");

	    String executionTimeLong = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + EXECUTION_TEST
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":executionTimeLong");

	    String executionError = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + EXECUTION_TEST
		    + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":validationError");

	    String onlineId = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":onlineId");

	    String targetLevel = reader.evaluateString(node, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":targetComplianceLevel");

	    report = new DataComplianceReport(onlineId, null);

	    report.setDescriptors(previewDescriptor, fullDescriptor);

	    report.setLastSucceededTest(DataComplianceTest.valueOf(lastSuccededTest));
	    report.setTargetTest(DataComplianceTest.valueOf(targetTest));

	    if (!isDownloadable.equals("")) {
		report.setDownloadable(Boolean.valueOf(isDownloadable));
	    }

	    if (!validationResult.equals("")) {

		ValidationMessage validationMessage = new ValidationMessage();
		validationMessage.setResult(ValidationResult.valueOf(validationResult));

		if (!validationError.equals("")) {
		    validationMessage.setError(validationError);
		}
		report.setValidationMessage(validationMessage);
	    }

	    if (!executionResult.equals("")) {

		ValidationMessage execResult = new ValidationMessage();
		execResult.setResult(ValidationResult.valueOf(executionResult));

		if (!executionError.equals("")) {
		    execResult.setError(executionError);
		}
		report.setExecutionResult(execResult);
	    }

	    if (!downloadTimeLong.equals("")) {
		report.setDownloadTime(Long.valueOf(downloadTimeLong));
	    }
	    if (!executionTimeLong.equals("")) {
		report.setExecutionTime(Long.valueOf(executionTimeLong));
	    }

	    // this is a simulation to match the original values
	    for (int i = 0; i < Integer.valueOf(workflowsCount); i++) {

		Workflow workflow = new Workflow();
		for (int j = 0; j < Integer.valueOf(workflowsLength); j++) {
		    Workblock workblock = new Workblock(new IdentityProcessor(), null);
		    workflow.getWorkblocks().add(workblock);
		}
		report.getWorkflows().add(workflow);
	    }

	    report.setTargetComplianceLevel(DataComplianceLevel.fromLabel(targetLevel));

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(ReportWrapper.class).error("Error wrapping DataComplianceReport", e);
	}

	return report;
    }

    private static DataDescriptor wrapDescriptor(boolean preview, XMLDocumentReader reader, Node node)
	    throws XPathExpressionException, ParseException {

	DataDescriptor descriptor = new DataDescriptor();

	String xPath = preview ? ":previewDescriptor/" : ":fullDescriptor/";

	String dataFormat = reader.evaluateString(node,
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":dataFormat");
	String dataType = reader.evaluateString(node,
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":dataType");
	String crs = reader.evaluateString(node,
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":CRS");

	descriptor.setDataFormat(DataFormat.fromIdentifier(dataFormat));
	descriptor.setDataType(DataType.valueOf(dataType));
	descriptor.setCRS(CRS.fromIdentifier(crs));

	Number rangeMin = reader.evaluateNumber(node,
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":rangeMin");
	descriptor.setRangeMinimum(rangeMin);

	Number rangeMax = reader.evaluateNumber(node,
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath + NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":rangeMax");
	descriptor.setRangeMaximum(rangeMax);

	List<DataDimension> spatialDimensions = Lists.newArrayList();
	List<DataDimension> otherDimensions = Lists.newArrayList();

	// sized spatial dimensions
	List<Node> sizedSpatials = Arrays.asList(reader.evaluateNodes(node, //
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":continueSpatialDimensions/" //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":continueDimension" //
	));

	if (!sizedSpatials.isEmpty()) {

	    for (Node sizedDim : sizedSpatials) {

		ContinueDimension sizedDimension = wrapContinueDimension(reader, sizedDim);

		DataDimension dataDimension = sizedDimension;

		spatialDimensions.add(dataDimension);
	    }
	}

	// sized other dimensions
	List<Node> sizedOthers = Arrays.asList(reader.evaluateNodes(node, //
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":continueOtherDimensions/" //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":continueDimension" //
	));

	if (!sizedOthers.isEmpty()) {

	    for (Node sizedDim : sizedOthers) {

		ContinueDimension sizedDimension = wrapContinueDimension(reader, sizedDim);

		DataDimension dataDimension = sizedDimension;

		otherDimensions.add(dataDimension);
	    }
	}

	// sized temporal dimension
	List<Node> sizedTemporal = Arrays.asList(reader.evaluateNodes(node, //
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":continueTemporalDimension"));

	if (!sizedTemporal.isEmpty()) {

	    ContinueDimension sizedDimension = wrapContinueDimension(reader, sizedTemporal.get(0));

	    DataDimension dataDimension = sizedDimension;

	    descriptor.setTemporalDimension(dataDimension);
	}

	// discrete spatial dimensions
	List<Node> discreteSpatials = Arrays.asList(reader.evaluateNodes(node, //
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":finiteSpatialDimensions/" //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":finiteDimension" //
	));

	if (!discreteSpatials.isEmpty()) {

	    for (Node discDim : discreteSpatials) {

		FiniteDimension discreteDimension = wrapFiniteDimension(reader, discDim);

		DataDimension dataDimension = discreteDimension;

		spatialDimensions.add(dataDimension);
	    }
	}

	// discrete other dimensions
	List<Node> discreteOthers = Arrays.asList(reader.evaluateNodes(node, //
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":finiteOtherDimensions/" //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":finiteDimension" //
	));

	if (!discreteOthers.isEmpty()) {

	    for (Node discDim : discreteOthers) {

		FiniteDimension discreteDimension = wrapFiniteDimension(reader, discDim);

		DataDimension dataDimension = discreteDimension;

		otherDimensions.add(dataDimension);
	    }
	}

	// discrete temporal dimension

	List<Node> discreteTemporal = Arrays.asList(reader.evaluateNodes(node, //
		NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + xPath //
			+ NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":finiteTemporalDimension"));

	if (!discreteTemporal.isEmpty()) {

	    FiniteDimension discreteDimension = wrapFiniteDimension(reader, discreteTemporal.get(0));

	    DataDimension dataDimension = discreteDimension;

	    descriptor.setTemporalDimension(dataDimension);
	}

	// set the dimensions
	descriptor.setSpatialDimensions(spatialDimensions);
	descriptor.setOtherDimensions(otherDimensions);

	return descriptor;

    }

    /**
     * @param report
     */
    public ReportWrapper(DataComplianceReport report) {

	this.report = report;
    }

    /**
     * @return
     */
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public String getTimeStamp() {

	return report.getTimeStamp();
    }

    /**
     * @return the onlineId
     */
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public String getOnlineId() {

	return report.getOnlineId();
    }

    /**
     * @return
     */
    @XmlElement(name = "targetComplianceLevel", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public String getTargetLevelLabel() {

	return report.getTargetComplianceLevel() != null ? report.getTargetComplianceLevel().getLabel() : "undefined";
    }

    /**
     * @return
     */
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public String getLastSucceededTest() {

	return report.getLastSucceededTest().toString();
    }

    /**
     * @return
     */
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public String getTargetTest() {

	return report.getTargetTest().toString();
    }

    @XmlElement(name = "previewDescriptor", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public DataDescriptorWrapper getPreviewDescriptor() {

	return new DataDescriptorWrapper(report.getPreviewDataDescriptor());

    }

    @XmlElement(name = "fullDescriptor", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public DataDescriptorWrapper getFullDescriptor() {

	return new DataDescriptorWrapper(report.getFullDataDescriptor());

    }

    /**
     * @return
     */
    @XmlElement(name = "basicTest", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public BasicTestResult getBasicTestResult() {

	BasicTestResult testResult = new BasicTestResult();
	testResult.setWorkflowsCount(report.getWorkflowsCount());
	testResult.setWorkflowsLength(report.getWorkflowsLength());

	return testResult;
    }

    /**
     * @return
     */
    @XmlElement(required = false, name = "downloadTest", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public DownloadTestResult getDownloadTestResult() {

	DownloadTestResult testResult = new DownloadTestResult();
	if (report.isDownloadable().isPresent()) {

	    report.isDownloadable().ifPresent(downloadable -> testResult.setDownloadable(downloadable.toString()));

	    report.getDownloadTime().ifPresent(time -> {
		testResult.setDownloadTimeLong(time);
		testResult.setDownloadTime(DataComplianceReport.formatTime(time));
	    });

	    return testResult;
	}

	return null;
    }

    /**
     * @return
     */
    @XmlElement(required = false, name = "validationTest", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public ValidationTestResult getValidationTestResult() {

	ValidationTestResult testResult = new ValidationTestResult();
	Optional<ValidationMessage> validationMessage = report.getValidationMessage();

	if (validationMessage.isPresent()) {
	    testResult.setValidationResult(validationMessage.get().getResult().toString());
	    testResult.setValidationError(validationMessage.get().getError());

	    return testResult;
	}

	return null;
    }

    /**
     * @return
     */
    @XmlElement(required = false, name = "executionTest", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    public ExecutionTestResult getExecutionTestResult() {

	ExecutionTestResult testResult = new ExecutionTestResult();
	Optional<ValidationMessage> validationMessage = report.getExecutionResult();

	if (validationMessage.isPresent()) {

	    testResult.setExecutionResult(validationMessage.get().getResult().toString());
	    testResult.setValidationError(validationMessage.get().getError());

	    report.getExecutionTime().ifPresent(executionTime -> {
		testResult.setExecutionTime(DataComplianceReport.formatTime(executionTime));
		testResult.setExecutionTimeLong(executionTime);
	    });

	    return testResult;
	}

	return null;
    }

    @Override
    public ReportWrapper fromStream(InputStream stream) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (ReportWrapper) unmarshaller.unmarshal(stream);
    }

    @Override
    public ReportWrapper fromNode(Node node) throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	return (ReportWrapper) unmarshaller.unmarshal(node);
    }

    private static FiniteDimension wrapFiniteDimension(XMLDocumentReader reader, Node discreteDim) throws XPathExpressionException {

	String name = reader.evaluateString(discreteDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":name");
	String type = reader.evaluateString(discreteDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":type");

	List<Node> points = Arrays.asList(reader.evaluateNodes(discreteDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":points"));
	List<String> pointsString = points.stream().map(n -> n.getTextContent()).collect(Collectors.toList());

	FiniteDimension discreteDimension = new FiniteDimension(name);

	if (!type.equals("")) {
	    discreteDimension.setType(new DimensionType(type));
	}

	discreteDimension.setPoints(pointsString);

	return discreteDimension;
    }

    private static ContinueDimension wrapContinueDimension(XMLDocumentReader reader, Node sizedDim)
	    throws XPathExpressionException, ParseException {

	String name = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":name");
	String type = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":type");
	String datum = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":datum");
	String size = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":size");
	String lower = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":lower");
	String upper = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":upper");
	String uom = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":uom");
	String resolution = reader.evaluateString(sizedDim, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX + ":resolution");

	ContinueDimension sizedDimension = new ContinueDimension(name);

	if (!type.equals("")) {
	    sizedDimension.setType(new DimensionType(type));
	}

	if (!datum.equals("")) {
	    sizedDimension.setDatum(new Datum(datum));
	}

	if (!size.equals("")) {
	    sizedDimension.setSize(Long.valueOf(size));
	}

	if (!lower.equals("")) {
	    sizedDimension.setLower(DecimalFormat.getInstance(Locale.US).parse(lower));
	}

	if (!upper.equals("")) {
	    sizedDimension.setUpper(DecimalFormat.getInstance(Locale.US).parse(upper));
	}

	if (!resolution.equals("")) {
	    sizedDimension.setResolution(DecimalFormat.getInstance(Locale.US).parse(resolution));
	}

	if (!uom.equals("")) {
	    sizedDimension.setUom(new Unit(uom));
	}

	return sizedDimension;
    }

    @Override
    protected Unmarshaller createUnmarshaller() throws JAXBException {

	return context.createUnmarshaller();
    }

    @Override
    protected Marshaller createMarshaller() throws JAXBException {

	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty("jaxb.formatted.output", true);
	marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CommonNameSpaceContext());

	return marshaller;
    }

    @Override
    protected Object getElement() throws JAXBException {

	return this;
    }

}
