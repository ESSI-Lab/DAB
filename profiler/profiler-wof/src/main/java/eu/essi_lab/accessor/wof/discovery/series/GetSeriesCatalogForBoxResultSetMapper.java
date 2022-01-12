package eu.essi_lab.accessor.wof.discovery.series;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wof.HydroServerProfiler;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
public class GetSeriesCatalogForBoxResultSetMapper extends DiscoveryResultSetMapper<String> {

    private static final String HIS_RES_SET_MAPPER_ERROR = "HIS_RES_SET_MAPPER_ERROR";

    public GetSeriesCatalogForBoxResultSetMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema HIS_CENTRAL_SERIES_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {

	return HIS_CENTRAL_SERIES_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    // MDMetadata metadata = coreMetadata.getMDMetadata();
	    MIMetadata metadata = coreMetadata.getMIMetadata();

	    // PARAMETER IDENTIFIERS
	    List<String> parameterIdentifiers = new ArrayList<>();
	    List<CoverageDescription> coverageDescriptions = Lists.newArrayList(metadata.getCoverageDescriptions());
	    for (CoverageDescription coverageDescription : coverageDescriptions) {
		parameterIdentifiers.add(coverageDescription.getAttributeDescription());
	    }

	    String servCode = ""; // LittleBearRiver
	    try {
		servCode = metadata.getMIPlatform().getMDIdentifierCode().split(":")[0];
	    } catch (Exception e) {
	    }
	    String servURL = ""; // http://data.iutahepscor.org/littlebearriverwof/cuahsi_1_1.asmx
	    try {
		UriInfo uri = message.getWebRequest().getUriInfo();
		servURL = uri.getBaseUri().toString() + "/" + HydroServerProfiler.HYDRO_SERVER_INFO.getServicePath();
	    } catch (Exception e) {
	    }
	    String location = "";// LittleBearRiver:USU-LBR-ParadiseWeather
	    try {
		location = res.getExtensionHandler().getUniquePlatformIdentifier().get();
	    } catch (Exception e) {
	    }
	    String varCode = "";//
	    try {
		varCode = res.getExtensionHandler().getUniqueAttributeIdentifier().get();
	    } catch (Exception e) {
	    }
	    String varName = "";// Battery voltage
	    try {
		varName = metadata.getCoverageDescription().getAttributeTitle();
	    } catch (Exception e) {
	    }
	    if (varName == null || varName.equals("")) {
		varName = varCode;
	    }
	    String beginPosition = "";// 2009-09-09T00:00:00Z
	    Date beginDate = null;
	    try {
		TemporalExtent temporalExtent = metadata.getDataIdentification().getTemporalExtent();
		TimeIndeterminateValueType indeterminatebegin = temporalExtent.getIndeterminateBeginPosition();
		if (indeterminatebegin != null) {
		    switch (indeterminatebegin) {
		    case NOW:
			beginDate = new Date();
			beginPosition = ISO8601DateTimeUtils.getISO8601DateTime(beginDate);
			break;
		    default:
			break;
		    }
		}
		if (beginPosition == null || beginPosition.equals("")) {
		    beginPosition = metadata.getDataIdentification().getTemporalExtent().getBeginPosition();
		    beginDate = ISO8601DateTimeUtils.parseISO8601(beginPosition);
		}
	    } catch (Exception e) {
	    }
	    String endPosition = "";// 2017-10-10T00:00:00Z
	    Date endDate = null;
	    try {
		TemporalExtent temporalExtent = metadata.getDataIdentification().getTemporalExtent();
		TimeIndeterminateValueType indeterminateEnd = temporalExtent.getIndeterminateEndPosition();
		if (indeterminateEnd != null) {
		    switch (indeterminateEnd) {
		    case NOW:
			endDate = new Date();
			endPosition = ISO8601DateTimeUtils.getISO8601DateTime(endDate);
			break;
		    default:
			break;
		    }
		}
		if (endPosition == null || endPosition.equals("")) {
		    endPosition = metadata.getDataIdentification().getTemporalExtent().getEndPosition();
		    endDate = ISO8601DateTimeUtils.parseISO8601(endPosition);
		}
	    } catch (Exception e) {
	    }

	    String valueCount = "";// 2900
	    try {
		Dimension axisDimension = metadata.getGridSpatialRepresentation().getAxisDimension();
		if (axisDimension.getDimensionNameTypeCode().equals("time")) {
		    valueCount = axisDimension.getDimensionSize().toString();
		}
	    } catch (Exception e) {
	    }
	    if (valueCount == null || valueCount.equals("") || valueCount.equals("0")) {
		if (beginDate != null && endDate != null) {
		    long interval = endDate.getTime() - beginDate.getTime();
		    long millisecondsInADay = 1000l * 60l * 60l * 24l;
		    valueCount = "" + (interval / millisecondsInADay); // expected 1 value per day
		} else {
		    valueCount = "10000"; // otherwise HydroDesktop will not show the pointer
		}
	    }
	    String siteName = "";// Little Bear River Paradise Weather Station near Paradise, Utah
	    try {
		siteName = metadata.getMIPlatform().getCitation().getTitle();
	    } catch (Exception e) {
	    }
	    if (siteName == null || siteName.equals("")) {
		siteName = location;
	    }
	    String latitude = "";// 41.5724
	    try {
		latitude = metadata.getDataIdentification().getGeographicBoundingBox().getNorth().toString();
	    } catch (Exception e) {
	    }
	    String longitude = "";// -111.8551
	    try {
		longitude = metadata.getDataIdentification().getGeographicBoundingBox().getEast().toString();
	    } catch (Exception e) {
	    }
	    String dataType = "";// Minimum
	    try {
		ArrayList<Keywords> list = Lists.newArrayList(metadata.getDataIdentification().getKeywords());
		for (Keywords keyword : list) {
		    String thesaurus = keyword.getThesaurusNameCitationTitle();
		    if (thesaurus != null && thesaurus.equals("CUAHSI Data Type CV")) {
			dataType = keyword.getKeywords().next();
		    }
		}
	    } catch (Exception e) {
	    }
	    if (dataType == null || dataType.equals("")) {
		// dataType = "Average";
	    }
	    String valueType = "";// Field Observation
	    try {
		ArrayList<Keywords> list = Lists.newArrayList(metadata.getDataIdentification().getKeywords());
		for (Keywords keyword : list) {
		    String thesaurus = keyword.getThesaurusNameCitationTitle();
		    if (thesaurus != null && thesaurus.equals("CUAHSI Value Type CV")) {
			valueType = keyword.getKeywords().next();
		    }
		}
	    } catch (Exception e) {
	    }
	    if (valueType == null || valueType.equals("")) {
		// valueType = "Field Observation";
	    }
	    String sampleMedium = "";// Not Relevant
	    try {
		ArrayList<Keywords> list = Lists.newArrayList(metadata.getDataIdentification().getKeywords());
		for (Keywords keyword : list) {
		    String type = keyword.getTypeCode();
		    if (type != null && type.equals("stratum")) {
			sampleMedium = keyword.getKeywords().next();
		    }
		}
	    } catch (Exception e) {
	    }
	    if (sampleMedium == null || sampleMedium.equals("")) {
		// sampleMedium = "Surface Water";
	    }
	    String timeUnits = "";// day
	    try {
		Dimension axisDimension = metadata.getGridSpatialRepresentation().getAxisDimension();
		if (axisDimension.getDimensionNameTypeCode().equals("time")) {
		    timeUnits = axisDimension.getResolutionUOM().toString();
		}
	    } catch (Exception e) {
	    }
	    String conceptKeywords = "";// Battery voltage
	    try {
		conceptKeywords = metadata.getCoverageDescription().getAttributeIdentifier();
	    } catch (Exception e) {
	    }
	    String genCategory = "";// Instrumentation
	    try {
		ArrayList<Keywords> list = Lists.newArrayList(metadata.getDataIdentification().getKeywords());
		for (Keywords keyword : list) {
		    String type = keyword.getTypeCode();
		    if (type != null && type.equals("discipline")) {
			genCategory = keyword.getKeywords().next();
		    }
		}
	    } catch (Exception e) {
	    }
	    if (genCategory == null || genCategory.equals("")) {
		// genCategory = "Hydrology";
	    }
	    String timeSupport = "";// 1
	    try {
		Dimension axisDimension = metadata.getGridSpatialRepresentation().getAxisDimension();
		if (axisDimension.getDimensionNameTypeCode().equals("time")) {
		    timeSupport = axisDimension.getResolutionValue().toString();
		}
	    } catch (Exception e) {
	    }

	    SeriesRecordCreator creator = new SeriesRecordCreator();
	    String ret = creator.createSeriesRecord(servCode, servURL, location, varCode, varName, beginPosition, endPosition, valueCount,
		    siteName, latitude, longitude, dataType, valueType, sampleMedium, timeUnits, conceptKeywords, genCategory, timeSupport);

	    return ret;

	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_RES_SET_MAPPER_ERROR);
	}

    }
}
