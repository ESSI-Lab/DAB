package eu.essi_lab.profiler.rest.handler.info;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.DataDescriptorWrapper;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;

/**
 * @author Fabrizio
 */
public class AccessInfoHandler extends RestInfoHandler {

    /**
     * @author Fabrizio
     */
    public enum ReportSubset {

	/**
	 * 
	 */
	COMPLETE("complete"),
	/**
	 * 
	 */
	PREVIEW("preview"),
	/**
	 * 
	 */
	FULL("full"),
	/**
	 * 
	 */
	IDENTIFIER("identifier");

	private String subset;

	private ReportSubset(String subset) {

	    this.subset = subset;
	}

	/**
	 * @return the format
	 */
	public String getSubset() {

	    return subset;
	}

	/**
	 * @param subset
	 * @return
	 */
	public static ReportSubset fromSubset(String subset) {

	    return Arrays.asList(values()).//
		    stream().//
		    filter(f -> f.getSubset().equals(subset)).//
		    findFirst().//
		    orElse(ReportSubset.COMPLETE);
	}
    }

    @Override
    public ValidationMessage validate(WebRequest webRequest) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());

	if (parser.isValid(RestParameter.RESPONSE_FORMAT.getName())) {

	    message = super.validateResponseFormat(parser.getValue(RestParameter.RESPONSE_FORMAT.getName()));
	    if (message.getResult() == ValidationResult.VALIDATION_FAILED) {
		return message;
	    }
	}

	if (parser.isValid(RestParameter.START_INDEX.getName())) {

	    Integer startIndex = null;

	    try {
		startIndex = Integer.valueOf(parser.getValue(RestParameter.START_INDEX.getName()));

		if (startIndex < 1) {

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError(RestParameter.START_INDEX.getName() + " parameter must be an int >= 1");
		    message.setLocator(RestParameter.START_INDEX.getName());

		    return message;
		}
	    } catch (NumberFormatException ex) {

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError(RestParameter.START_INDEX.getName() + " parameter must be an int >= 1");
		message.setLocator(RestParameter.START_INDEX.getName());

		return message;
	    }
	}

	if (parser.isValid(RestParameter.MAX_REPORTS.getName())) {

	    Integer maxReports = null;

	    try {
		maxReports = Integer.valueOf(parser.getValue(RestParameter.MAX_REPORTS.getName()));

		if (maxReports < 1) {

		    message.setResult(ValidationResult.VALIDATION_FAILED);
		    message.setError(RestParameter.MAX_REPORTS.getName() + " parameter must be an int >= 1");
		    message.setLocator(RestParameter.MAX_REPORTS.getName());

		    return message;
		}

	    } catch (NumberFormatException ex) {

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError(RestParameter.MAX_REPORTS.getName() + " parameter must be an int >= 1");
		message.setLocator(RestParameter.MAX_REPORTS.getName());

		return message;
	    }
	}

	if (parser.isValid(RestParameter.REPORT_SUBSET.getName())) {

	    String format = parser.getValue(RestParameter.REPORT_SUBSET.getName());
	    boolean anyMatch = Arrays.asList(ReportSubset.values()).//
		    stream().//
		    anyMatch(p -> p.getSubset().equals(format));

	    if (!anyMatch) {

		message.setResult(ValidationResult.VALIDATION_FAILED);
		message.setError(RestParameter.REPORT_SUBSET.getName() + " parameter must have one of the following values: " + //
			Arrays.asList(ReportSubset.values()).stream().map(s -> s.getSubset()).collect(Collectors.toList()));
		message.setLocator(RestParameter.REPORT_SUBSET.getName());

		return message;
	    }
	}

	return message;
    }

    @Override
    protected String createXMLResponse(WebRequest webRequest) throws GSException {

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	int maxReports = 10;
	int startIndex = 1;

	KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());

	if (parser.isValid("startIndex")) {

	    startIndex = Integer.valueOf(parser.getValue("startIndex"));
	}

	if (parser.isValid("maxReports")) {

	    maxReports = Integer.valueOf(parser.getValue("maxReports"));
	}

	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(webRequest.getRequestId());

	message.setPage(new Page(startIndex, maxReports));

	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	message.getResourceSelector().setSubset(ResourceSubset.EXTENDED);

	message.setSources(ConfigurationWrapper.getHarvestedSources());
	message.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	

	ResourcePropertyBond bond = BondFactory.createIsExecutableBond(true);
	message.setUserBond(bond);

	ResultSet<GSResource> resultSet = executor.retrieve(message);
	List<GSResource> resultsList = resultSet.getResultsList();

	String out = "<gs:accessInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	out += " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:gs=\"";
	out += CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "\">";
	out += "<gs:totalReports>" + resultSet.getCountResponse().getCount() + "</gs:totalReports>";
	out += "<gs:returnedReports>" + resultsList.size() + "</gs:returnedReports>";

	int lastIndex = startIndex + maxReports;
	int nextReport = lastIndex > resultSet.getCountResponse().getCount() ? 0 : startIndex + resultsList.size();

	out += "<gs:nextReport>" + nextReport + "</gs:nextReport>";

	ReportSubset reportSubset = ReportSubset.COMPLETE;
	if (parser.isValid(RestParameter.REPORT_SUBSET.getName())) {

	    reportSubset = ReportSubset.fromSubset(parser.getValue(RestParameter.REPORT_SUBSET.getName()));
	}

	for (GSResource res : resultsList) {

	    ReportsMetadataHandler handler = new ReportsMetadataHandler(res);

	    List<DataComplianceReport> reports = handler.getReports().//
		    stream().//
		    filter(r -> r.getLastSucceededTest() == DataComplianceTest.EXECUTION).//
		    collect(Collectors.toList());

	    for (DataComplianceReport report : reports) {

		String onlineId = report.getOnlineId();

		out += "<gs:report>";
		out += "<gs:identifier>" + onlineId + "</gs:identifier>";

		DataComplianceLevel level = report.getTargetComplianceLevel();
		ProcessorCapabilities cap = level.getCapabilities();

		out += wrap(cap);

		DataDescriptor preview = report.getPreviewDataDescriptor();
		DataDescriptorWrapper previewWrapper = new DataDescriptorWrapper(preview);

		DataDescriptor full = report.getFullDataDescriptor();
		DataDescriptorWrapper fullWrapper = new DataDescriptorWrapper(full);

		try {

		    switch (reportSubset) {
		    case IDENTIFIER:
			break;
		    case COMPLETE:

			out += "<gs:preview>";
			out += previewWrapper.asString(true);
			out += "</gs:preview>";

			out += "<gs:full>";
			out += fullWrapper.asString(true);
			out += "</gs:full>";

			break;

		    case FULL:

			out += "<gs:full>";
			out += fullWrapper.asString(true);
			out += "</gs:full>";

			break;

		    case PREVIEW:

			out += "<gs:preview>";
			out += previewWrapper.asString(true);
			out += "</gs:preview>";

			break;
		    }

		    out += "</gs:report>";

		} catch (UnsupportedEncodingException | JAXBException e) {

		    e.printStackTrace();
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	}

	out += "</gs:accessInfo>";

	return out;
    }

    private String wrap(ProcessorCapabilities cap) {

	String out = "<gs:capabilities>";
	out += "<gs:dataType>" + cap.getDataTypeCapability().getFirstValue() + "</gs:dataType>";
	out += "<gs:dataFormat>" + cap.getDataFormatCapability().getFirstValue().getIdentifier() + "</gs:dataFormat>";

	CRS crs = cap.getCRSCapability().getFirstValue();
	if (crs.equals(CRS.GDAL_ALL())) {
	    List<CRS> gdalCrsList = CRS.getGDALCrsList();
	    for (CRS gdalCrs : gdalCrsList) {
		out += "<gs:CRS>" + gdalCrs.getIdentifier() + "</gs:CRS>";
	    }
	} else {
	    out += "<gs:CRS>" + crs.getIdentifier() + "</gs:CRS>";
	}

	out += "<gs:SubsettingCapability>";
	out += "<gs:spatial>" + cap.getSubsettingCapability().getSpatialSubsetting().getFirstValue() + "</gs:spatial>";
	out += "<gs:temporal>" + cap.getSubsettingCapability().getTemporalSubsetting().getFirstValue() + "</gs:temporal>";
	out += "<gs:other>" + cap.getSubsettingCapability().getOtherSubsetting().getFirstValue() + "</gs:other>";
	out += "</gs:SubsettingCapability>";
	out += "<gs:ResamplingCapability>";
	out += "<gs:spatial>" + cap.getResamplingCapability().getSpatialResampling().getFirstValue() + "</gs:spatial>";
	out += "<gs:temporal>" + cap.getResamplingCapability().getTemporalResampling().getFirstValue() + "</gs:temporal>";
	out += "<gs:other>" + cap.getResamplingCapability().getOtherResampling().getFirstValue() + "</gs:other>";
	out += "</gs:ResamplingCapability>   ";
	out += "</gs:capabilities>";
	return out;
    }
}
