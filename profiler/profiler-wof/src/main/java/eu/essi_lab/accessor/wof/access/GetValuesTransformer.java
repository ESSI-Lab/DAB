package eu.essi_lab.accessor.wof.access;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBElement;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.access.wml.WMLDataDownloaderAdapter;
import eu.essi_lab.accessor.wof.HydroServerProfiler;
import eu.essi_lab.accessor.wof.TimeFormatConverter;
import eu.essi_lab.accessor.wof.WOFQueryUtils;
import eu.essi_lab.accessor.wof.WOFRequest;
import eu.essi_lab.accessor.wof.WOFRequest.Parameter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationException;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsm.access.AccessQueryUtils;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
import eu.essi_lab.pdk.wrt.AccessRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

/**
 * @author boldrini
 */
public class GetValuesTransformer extends AccessRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	GetValuesValidator validator = new GetValuesValidator();

	ValidationMessage ret = validator.validate(request);

	if (ret.getResult().equals(ValidationResult.VALIDATION_FAILED)) {

	    return ret;
	}

	return extendedValidation(request);

    }

    public ValidationMessage extendedValidation(WebRequest request) throws GSException {

	WOFRequest getValuesRequest = getValuesRequest(request);

	// SITE CODE EXTRACTION

	String uniqueSiteCode = getValuesRequest.getParameterValue(Parameter.SITE_CODE);

	if (uniqueSiteCode.contains(":")) {
	    uniqueSiteCode = uniqueSiteCode.substring(uniqueSiteCode.indexOf(":") + 1);
	}

	// VARIABLE CODE EXTRACTION

	String uniqueVariableCode = getValuesRequest.getParameterValue(Parameter.VARIABLE);

	if (uniqueVariableCode.contains(":")) {
	    uniqueVariableCode = uniqueVariableCode.substring(uniqueVariableCode.indexOf(":") + 1);
	}

	// CHECK SITE + VARIABLE exist

	GSLoggerFactory.getLogger(getClass()).info("Checking site + variable validity");

	SimpleValueBond siteBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER,
		uniqueSiteCode);

	SimpleValueBond variableBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER,
		uniqueVariableCode);

	String onlineId = WOFQueryUtils.getOnlineId(request.getRequestId(), uniqueSiteCode, uniqueVariableCode);

	if (onlineId == null) {

	    // IN CASE CHECK SITE + VARIABLE failed...

	    // 1. CHECK SITE

	    GSLoggerFactory.getLogger(getClass()).info("Checking site validity");

	    GSResource siteResource = check(request, siteBond);

	    if (siteResource == null) {
		ValidationException exception = new ValidationException();
		exception.setMessage("Site not found: " + uniqueSiteCode);
		exception.setCode(HydroServerProfiler.ERROR_SITE_NOT_FOUND);
		exception.setLocator(uniqueSiteCode);
		ValidationMessage ret = new ValidationMessage();
		ret.addException(exception);
		ret.setResult(ValidationResult.VALIDATION_FAILED);
		return ret;
	    }

	    // 2. CHECK VARIABLE

	    GSLoggerFactory.getLogger(getClass()).info("Checking variable validity");

	    GSResource variableResource = check(request, variableBond);

	    if (variableResource == null) {
		ValidationException exception = new ValidationException();
		exception.setMessage("Variable parameter not found: " + uniqueVariableCode);
		exception.setCode(HydroServerProfiler.ERROR_VARIABLE_NOT_FOUND);
		exception.setLocator(uniqueVariableCode);
		ValidationMessage ret = new ValidationMessage();
		ret.addException(exception);
		ret.setResult(ValidationResult.VALIDATION_FAILED);
		return ret;
	    }

	    // 3. It can be confirmed that it's the SITE + VARIABLE combination that is actually missing!
	    try {

		ValidationException exception = new ValidationException();

		WMLDataDownloader wdd = new WMLDataDownloaderAdapter();
		wdd.setOnlineResource(variableResource,
			variableResource.getHarmonizedMetadata().getCoreMetadata().getOnline().getIdentifier());
		TimeSeriesResponseType tsrt = wdd.getTimeSeriesTemplate();
		WMLDataDownloader wdd2 = new WMLDataDownloaderAdapter();
		wdd2.setOnlineResource(siteResource, siteResource.getHarmonizedMetadata().getCoreMetadata().getOnline().getIdentifier());
		TimeSeriesResponseType tsrt2 = wdd2.getTimeSeriesTemplate();
		tsrt.getTimeSeries().get(0).setSourceInfo(tsrt2.getTimeSeries().get(0).getSourceInfo());

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
		tmpFile.deleteOnExit();
		JAXBWML.getInstance().marshal(response, tmpFile);
		AccessMessage accessMessage = new AccessMessage();
		accessMessage.setWebRequest(request);
		DataObject resource = new DataObject();
		resource.setFile(tmpFile);
		DefaultAccessResultSetMapper mapper = getMapper();

		DataObject mapped = mapper.map(accessMessage, resource);
		String emptyWaterML = new String(Files.readAllBytes(Paths.get(mapped.getFile().toURI())));
		tmpFile.delete();
		mapped.getFile().delete();

		exception.setMessage(emptyWaterML);
		exception.setCode(getVariableNotFoundInSiteErrorCode());
		exception.setLocator(uniqueVariableCode);
		ValidationMessage ret = new ValidationMessage();
		ret.addException(exception);
		ret.setResult(ValidationResult.VALIDATION_FAILED);
		return ret;
	    } catch (Exception e) {
		ValidationMessage ret = new ValidationMessage();
		ret.setError("Unexpected error: " + e.getMessage());
		ret.setResult(ValidationResult.VALIDATION_FAILED);
		return ret;
	    }
	}

	// VALIDATION success

	GSLoggerFactory.getLogger(WOFQueryUtils.class).info("Extended validation success");

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;

    }

    public DefaultAccessResultSetMapper getMapper() {

	return new GetValuesResultSetMapper();

    }

    public String getVariableNotFoundInSiteErrorCode() {
	return HydroServerProfiler.ERROR_GET_VALUES_VARIABLE_NOT_FOUND_IN_SITE;
    }

    public GSResource check(WebRequest request, Bond bond) throws GSException {
	DiscoveryMessage message = new DiscoveryMessage();

	message.setRequestId(request.getRequestId());

	message.setUserBond(bond);

	message.setQueryRegistrationEnabled(false);

	message.setSources(ConfigurationWrapper.getAllSources());

	Page page = new Page(1, 1);
	message.setPage(page);

	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	message.getResourceSelector().setSubset(ResourceSubset.FULL);
	message.getResourceSelector().setIncludeOriginal(false);
	message.setSources(ConfigurationWrapper.getHarvestedSources());
	message.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());

	GSLoggerFactory.getLogger(getClass()).info("Resource discovery STARTED");

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	ResultSet<GSResource> resultSet = executor.retrieve(message);

	return resultSet.getResultsList().isEmpty() ? null : resultSet.getResultsList().get(0);

    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    protected Optional<DataDescriptor> getTargetDescriptor(WebRequest request) throws GSException {

	try {

	    String id = getOnlineId(request);

	    DataDescriptor descriptor = getDefaultDescriptor(request.getRequestId(), id);

	    if (descriptor != null) {
		WOFRequest getValuesRequest = getValuesRequest(request);

		String startDate = getValuesRequest.getParameterValue(Parameter.BEGIN_DATE);

		String endDate = getValuesRequest.getParameterValue(Parameter.END_DATE);

		if (startDate != null && !startDate.equals("") && endDate != null && !endDate.equals("")) {

		    TimeFormatConverter tfc = new TimeFormatConverter();

		    Date start = tfc.convertGetValuesTimeFormatToJavaDate(startDate);

		    Date end = tfc.convertGetValuesTimeFormatToJavaDate(endDate);

		    descriptor.getTemporalDimension().getContinueDimension().setLower(start.getTime());

		    descriptor.getTemporalDimension().getContinueDimension().setUpper(end.getTime());

		}

		descriptor.getTemporalDimension().getContinueDimension().setResolution(null);
		descriptor.getTemporalDimension().getContinueDimension().setResolutionTolerance(null);
		descriptor.getTemporalDimension().getContinueDimension().setSize(null);

		String format = getValuesRequest.getParameterValue(Parameter.FORMAT);
		if (format != null && format.contains("2")) {
		    descriptor.setDataFormat(DataFormat.WATERML_2_0());
		} else if (format != null && format.toLowerCase().contains("netcdf")) {
		    descriptor.setDataFormat(DataFormat.NETCDF());
		} else {
		    descriptor.setDataFormat(DataFormat.WATERML_1_1());
		}

		return Optional.of(descriptor);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't get target descriptor", e);

	}

	return Optional.empty();
    }

    private DataDescriptor getDefaultDescriptor(String requestId, String onlineIdentifier) throws GSException {

	List<GSSource> sources = ConfigurationWrapper.getHarvestedSources();
	StorageInfo databaseURI = ConfigurationWrapper.getDatabaseURI();
	ResultSet<GSResource> resultSet = AccessQueryUtils.findResource(requestId, sources, onlineIdentifier, databaseURI);

	if (resultSet.getResultsList().isEmpty()) {
	    // TODO: error!
	}
	GSResource result = resultSet.getResultsList().get(0);
	ReportsMetadataHandler handler = new ReportsMetadataHandler(result);

	List<DataComplianceReport> reports = handler.getReports();
	Optional<DataComplianceReport> optReport = reports.stream().filter(r -> r.getOnlineId().equals(onlineIdentifier)).findFirst();
	if (!optReport.isPresent()) {

	    // TODO: error

	    return null;
	}

	DataComplianceReport report = optReport.get();

	return report.getFullDataDescriptor();

    }

    @Override
    protected String getOnlineId(WebRequest request) throws GSException {

	try {

	    WOFRequest getValuesRequest = getValuesRequest(request);

	    String uniqueVariableCode = getValuesRequest.getParameterValue(Parameter.VARIABLE);

	    if (uniqueVariableCode.contains(":")) {
		uniqueVariableCode = uniqueVariableCode.substring(uniqueVariableCode.indexOf(":") + 1);
	    }

	    String uniqueSiteCode = getValuesRequest.getParameterValue(Parameter.SITE_CODE);

	    if (uniqueSiteCode.contains(":")) {
		uniqueSiteCode = uniqueSiteCode.substring(uniqueSiteCode.indexOf(":") + 1);
	    }

	    return WOFQueryUtils.getOnlineId(request.getRequestId(), uniqueSiteCode, uniqueVariableCode);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Can't read variable", e);

	}

	return null;
    }

    public WOFRequest getValuesRequest(WebRequest request) {
	return new GetValuesRequest(request);
    }

    @Override
    public String getProfilerType() {

	return HydroServerProfiler.HYDRO_SERVER_INFO.getServiceType();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

}
