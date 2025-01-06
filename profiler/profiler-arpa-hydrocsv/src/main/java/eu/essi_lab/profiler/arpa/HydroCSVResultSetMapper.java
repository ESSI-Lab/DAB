package eu.essi_lab.profiler.arpa;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriInfo;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.arpa.HydroCSVTimeSeriesEncoder.CSV_Field;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class HydroCSVResultSetMapper extends DiscoveryResultSetMapper<String> {

    public static final MappingSchema HYDRO_CSV_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {
	return HYDRO_CSV_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    public String map(DiscoveryMessage message, GSResource resource) throws GSException {

	String wpsPath = HydroCSVUtils.getWPSPath();

	String platformName = "";
	String parameterName = "";
	String platformCode = "";
	String parameterCode = "";
	String onlineResourceId = "";
	String downloadURL = "";
	String originalBegin = "";
	String originalEnd = "";

	String temporalBegin = "";
	String temporalEnd = "";
	String latitude = "";
	String longitude = "";
	Optional<String> optionalPlatform = resource.getExtensionHandler().getUniquePlatformIdentifier();
	if (optionalPlatform.isPresent()) {
	    platformCode = optionalPlatform.get();
	}

	Optional<String> optionalAttribute = resource.getExtensionHandler().getUniqueAttributeIdentifier();
	if (optionalAttribute.isPresent()) {
	    parameterCode = optionalAttribute.get();
	}

	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();
	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);
	List<DataComplianceReport> reports = handler.getReports().//
		stream().//
		filter(r -> r.getLastSucceededTest() == DataComplianceTest.EXECUTION).//
		collect(Collectors.toList());

	if (!reports.isEmpty()) {
	    DataComplianceReport report = reports.get(0);
	    onlineResourceId = report.getOnlineId();

	    DataDescriptor descriptor = report.getFullDataDescriptor();

	}

	if (coreMetadata != null) {
	    MIMetadata miMetadata = coreMetadata.getMIMetadata();
	    if (miMetadata != null) {
		MIPlatform platform = miMetadata.getMIPlatform();
		if (platform != null) {
		    platformName = platform.getCitation().getTitle();
		}
		Distribution distribution = miMetadata.getDistribution();
		if (distribution != null) {

		}
		CoverageDescription coverageDescription = miMetadata.getCoverageDescription();
		if (coverageDescription != null) {
		    parameterName = coverageDescription.getAttributeTitle();
		    if (parameterName == null || parameterName.equals("")) {
			parameterName = parameterCode;
		    }
		}
		DataIdentification dataIdentification = miMetadata.getDataIdentification();
		if (dataIdentification != null) {
		    GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
		    if (bbox != null) {
			latitude = "" + bbox.getSouth();
			longitude = "" + bbox.getWest();
		    }
		    TemporalExtent temporal = dataIdentification.getTemporalExtent();
		    if (temporal != null) {
			originalBegin = temporal.getBeginPosition();
			originalEnd = temporal.getEndPosition();
			temporalEnd = temporal.getEndPosition();
			TimeIndeterminateValueType indeterminateEnd = temporal.getIndeterminateEndPosition();
			if (indeterminateEnd != null && indeterminateEnd.equals(TimeIndeterminateValueType.NOW)) {
			    temporalEnd = ISO8601DateTimeUtils.getISO8601DateTime(new Date());
			    originalEnd = temporalEnd;
			}
			Optional<Date> temporalEndDate = ISO8601DateTimeUtils.parseISO8601ToDate(temporalEnd);
			if (temporalEndDate.isPresent()) {
			    long oneMonth = 1000l * 60l * 60l * 24l * 30l;
			    Date date = new Date(temporalEndDate.get().getTime() - oneMonth);
			    temporalBegin = ISO8601DateTimeUtils.getISO8601DateTime(date);
			}
		    }
		}

	    }
	}

	UriInfo uri = message.getWebRequest().getUriInfo();
	downloadURL = uri.getBaseUri().toString() + "/" + wpsPath + "/dataset/" + onlineResourceId
		+ "?service=WPS&request=execute&identifier=gi-axe-transform&storeexecuteresponse=false&" + //
		"DataInputs=outputCRS=EPSG%3A4326;outputFormat=" + DataFormat.WATERML_2_0().getIdentifier() + ";outputTemporalBegin="
		+ temporalBegin + ";outputTemporalEnd=" + temporalEnd;

	String graphService = "http://hiscentral.ddns.net/graph/handler2.ashx?";

	int height = 375;
	int width = 645;

	String hydroPath = HydroCSVUtils.getHydroServerPath();

	String serviceURL = uri.getBaseUri().toString() + "/" + hydroPath;

	try {
	    serviceURL = URLEncoder.encode(serviceURL, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}

	String startDate = temporalBegin;
	String endDate = temporalEnd;

	String graphURL = graphService + "height=" + height + "&width=" + width + "&serviceUrl=" + serviceURL + "&siteCode=" + platformCode
		+ "&variableCode=" + parameterCode + "&startDate=" + startDate + "&endDate=" + endDate;

	HydroCSVTimeSeriesEncoder encoder = new HydroCSVTimeSeriesEncoder();

	encoder.add(CSV_Field.SITE_CODE, platformCode);
	encoder.add(CSV_Field.SITE_NAME, platformName);
	encoder.add(CSV_Field.VARIABLE_CODE, parameterCode);
	encoder.add(CSV_Field.VARIABLE_NAME, parameterName);
	encoder.add(CSV_Field.LATITUDE, latitude);
	encoder.add(CSV_Field.LONGITUDE, longitude);
	encoder.add(CSV_Field.TIMESERIES_CODE, onlineResourceId);
	encoder.add(CSV_Field.WATERML_1_1_DOWNLOAD_URL, downloadURL);
	encoder.add(CSV_Field.GRAPH_URL, graphURL);
	encoder.add(CSV_Field.TIME_BEGIN, originalBegin);
	encoder.add(CSV_Field.TIME_END, originalEnd);

	return encoder.toString();
    }

}
