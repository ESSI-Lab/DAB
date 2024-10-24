package eu.essi_lab.profiler.gwis;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;

public class GWISResultSetFormatter extends DiscoveryResultSetFormatter<GSResource> {

    /**
     * The encoding name of {@link #GWIS_FORMATTING_ENCODING}
     */
    public static final String GWIS_RECORD_ENCODING_NAME = "gwis-record-encoding";
    /**
     * The encoding version of {@link #GWIS_FORMATTING_ENCODING}
     */
    public static final String GWIS_RECORD_ENCODING_VERSION = "1.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding GWIS_FORMATTING_ENCODING = new FormattingEncoding();

    static {
	GWIS_FORMATTING_ENCODING.setEncoding(GWIS_RECORD_ENCODING_NAME);
	GWIS_FORMATTING_ENCODING.setEncodingVersion(GWIS_RECORD_ENCODING_VERSION);
	GWIS_FORMATTING_ENCODING.setMediaType(MediaType.APPLICATION_XML_TYPE);

    }

    private static final String GWIS_RESULT_SET_FORMATTER_ERROR = "GWIS_RESULT_SET_FORMATTER_ERROR";

    public GWISResultSetFormatter() {

    }

    @Override
    public Response format(DiscoveryMessage message, ResultSet<GSResource> resultSet) throws GSException {

	try {

	    List<GSResource> resources = resultSet.getResultsList();

	    InputStream stream = GWISResultSetFormatter.class.getClassLoader().getResourceAsStream("gwis-code/gwis-template.html");
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    IOUtils.copy(stream, baos);
	    stream.close();
	    baos.close();
	    String response = new String(baos.toByteArray(), StandardCharsets.UTF_8);

	    GSResource resource = resources.get(0);

	    CoreMetadata metadata = resource.getHarmonizedMetadata().getCoreMetadata();

	    CoverageDescription coverageDescription = metadata.getMIMetadata().getCoverageDescription();

	    String title = coverageDescription.getAttributeTitle();

	    String siteCode = resource.getExtensionHandler().getUniquePlatformIdentifier().get();

	    String platformName_1 = "N/A";
	    MIPlatform platform = metadata.getMIMetadata().getMIPlatform();
	    if (platform != null) {
		Citation citation = platform.getCitation();
		if (citation != null) {
		    String pt = citation.getTitle();
		    if (pt != null) {
			platformName_1 = pt;
		    }
		}
	    }

	    String parameterCode = resource.getExtensionHandler().getUniqueAttributeIdentifier().get();

	    String yLabel = coverageDescription.getAttributeTitle();

	    Optional<String> units = resource.getExtensionHandler().getAttributeUnitsAbbreviation();

	    if (!units.isPresent()) {
		units = resource.getExtensionHandler().getAttributeUnits();
	    }

	    String yUnits = units.isPresent() ? units.get() : "N/A";

	    ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);
	    DataComplianceReport report = handler.getReports().get(0);
	    DataDescriptor descriptor = report.getFullDataDescriptor();
	    long upperTime = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	    Date now = new Date();
	    long gap = now.getTime() - upperTime;
	    if (gap < TimeUnit.DAYS.toMillis(14)) {
		upperTime = now.getTime();
	    }
	    long lowerTime = upperTime - (7 * 24 * 60 * 60 * 1000l);
	    String beginTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date(lowerTime));
	    String endTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date(upperTime));
	    
	    title = title.replace("'", "\'");
	    yLabel = yLabel.replace("'", "\'");
	   
	    String platformName_2 = platformName_1.replace("'", "\u2032");
	    platformName_1 = platformName_1.replace("'", "\'");

	    response = response.replace("${TITLE}", title).//
		    replace("${X_LABEL}", "time").//
		    
		    replace("${PLATFORM_NAME_1}", platformName_1).//
		    replace("${PLATFORM_NAME_2}", platformName_2).//

		    replace("${Y_LABEL}", yLabel).//
		    replace("${Y_UNITS}", yUnits).//
		    replace("${BEGIN_TIME}", beginTime).//
		    replace("${END_TIME}", endTime).//
		    replace("${SITE_CODE}", siteCode).//
		    replace("${PARAMETER_CODE}", parameterCode);

	    return buildResponse(response);

	} catch (

	Exception ex) {
	    ex.printStackTrace();
	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GWIS_RESULT_SET_FORMATTER_ERROR);
	}
    }

    @Override
    public FormattingEncoding getEncoding() {

	return GWIS_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private static Response buildResponse(String response) throws Exception {

	ResponseBuilder builder = Response.status(Status.OK);

	builder = builder.entity(response);
	builder = builder.type(MediaType.TEXT_HTML);

	return builder.build();
    }
}
