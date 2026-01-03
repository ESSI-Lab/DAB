package eu.essi_lab.profiler.wof.discovery.sites;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Date;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.PropertyType;
import org.cuahsi.waterml._1.SeriesCatalogType;
import org.cuahsi.waterml._1.SeriesCatalogType.Series;
import org.cuahsi.waterml._1.SeriesCatalogType.Series.ValueCount;
import org.cuahsi.waterml._1.SiteInfoResponseType.Site;
import org.cuahsi.waterml._1.SourceType;
import org.cuahsi.waterml._1.TimePeriodType;
import org.cuahsi.waterml._1.VariableInfoType;

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.wof.discovery.variables.GetVariablesResultSetMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author boldrini
 */
public class GetSiteInfoResultSetMapper extends DiscoveryResultSetMapper<Site> {

    private static final String HIS_RES_SET_MAPPER_ERROR = "HIS_RES_SET_MAPPER_ERROR";

    public GetSiteInfoResultSetMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
    }

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema HYDRO_SERVER_SITES_MAPPING_SCHEMA = new MappingSchema();

    @Override
    public MappingSchema getMappingSchema() {

	return HYDRO_SERVER_SITES_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public Site map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    MIMetadata metadata = coreMetadata.getMIMetadata();
	    String varUniqueCode = "";
	    try {
		varUniqueCode = res.getExtensionHandler().getUniqueAttributeIdentifier().get();
	    } catch (Exception e) {
	    }
	    String varName = "";// Battery voltage
	    try {
		varName = metadata.getCoverageDescription().getAttributeTitle();
	    } catch (Exception e) {
	    }

	    ExtensionHandler extensionHandler = res.getExtensionHandler();

	    GetSitesResultSetMapper sitesMapper = new GetSitesResultSetMapper();
	    Site site = sitesMapper.map(message, res);

	    SeriesCatalogType seriesCatalog = new SeriesCatalogType();
	    Series series = new Series();

	    ValueCount valuecount = new ValueCount();
	    Optional<Long> optionalValueCount = extensionHandler.getDataSize();
	    Integer valueCount = null;
	    if (optionalValueCount.isPresent()) {
		valueCount = optionalValueCount.get().intValue();
		valuecount.setValue(valueCount);
	    }
	    series.setValueCount(valuecount);

	    XMLGregorianCalendar beginDate = null;// 2009-09-09T00:00:00Z
	    try {
		TimeIndeterminateValueType indeterminateBegin = metadata.getDataIdentification().getTemporalExtent()
			.getIndeterminateBeginPosition();
		if (indeterminateBegin != null && indeterminateBegin.equals(TimeIndeterminateValueType.NOW)) {
		    beginDate = ISO8601DateTimeUtils.getXMLGregorianCalendar(new Date());
		} else {
		    beginDate = parseDate(metadata.getDataIdentification().getTemporalExtent().getBeginPosition());
		}

	    } catch (Exception e) {
	    }
	    XMLGregorianCalendar endDate = null;// 2017-10-10T00:00:00Z
	    try {
		TimeIndeterminateValueType indeterminateEnd = metadata.getDataIdentification().getTemporalExtent()
			.getIndeterminateEndPosition();
		if (indeterminateEnd != null && indeterminateEnd.equals(TimeIndeterminateValueType.NOW)) {
		    endDate = ISO8601DateTimeUtils.getXMLGregorianCalendar(new Date());
		} else {
		    endDate = parseDate(metadata.getDataIdentification().getTemporalExtent().getEndPosition());
		}

	    } catch (Exception e) {
	    }

	    TimePeriodType timePeriod = new TimePeriodType();
	    timePeriod.setBeginDateTime(beginDate);
	    timePeriod.setBeginDateTimeUTC(beginDate);
	    timePeriod.setEndDateTime(endDate);
	    timePeriod.setEndDateTimeUTC(endDate);
	    series.setVariableTimeInterval(timePeriod);
	    SourceType source = new SourceType();
	    source.setSourceID(1);

	    String sourceOrganizationName = "";
	    String sourceOrganizationDescription = "";
	    ResponsibleParty poc = coreMetadata.getMIMetadata().getDataIdentification().getPointOfContact();
	    if (poc != null) {
		sourceOrganizationName = poc.getOrganisationName();
		sourceOrganizationDescription = poc.getRoleCode();
	    }

	    source.setOrganization(sourceOrganizationName);
	    source.setSourceDescription(sourceOrganizationDescription);
	    String citation = coreMetadata.getMIMetadata().getDataIdentification().getCitationTitle();
	    source.setCitation(citation);
	    series.setSource(source);

	    GetVariablesResultSetMapper variableMapper = new GetVariablesResultSetMapper();
	    VariableInfoType variable = variableMapper.map(message, res);
	    variable.getVariableCode().get(0).setVariableID(getVariableId());

	    series.setVariable(variable);

	    Distribution distribution = metadata.getDistribution();
	    if (distribution != null) {
		Online online = distribution.getDistributionOnline();
		if (online != null) {
		    String onlineId = online.getIdentifier();
		    if (onlineId != null) {
			PropertyType idProperty = new PropertyType();
			idProperty.setName("identifier");
			idProperty.setValue(onlineId);
			series.getSeriesProperty().add(idProperty);
			seriesCatalog.getSeries().add(series);
			site.getSeriesCatalog().add(seriesCatalog);
		    }
		}
	    }

	    return site;

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

    private Integer getVariableId() {
	Integer ret = null;
	synchronized (variableId) {
	    ret = variableId;
	    variableId++;
	}
	return ret;
    }

    private Integer variableId = 1;

    private XMLGregorianCalendar parseDate(String date) {
	if (date == null) {
	    return null;
	}
	Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(date);
	if (!parsed.isPresent()) {
	    return null;
	}
	try {
	    return ISO8601DateTimeUtils.getXMLGregorianCalendar(parsed.get());
	} catch (DatatypeConfigurationException e) {
	    e.printStackTrace();
	}
	return null;
    }
}
