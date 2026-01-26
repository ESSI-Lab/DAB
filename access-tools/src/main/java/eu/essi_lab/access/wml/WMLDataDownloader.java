package eu.essi_lab.access.wml;

import java.io.File;

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.Duration;

import eu.essi_lab.lib.net.utils.whos.*;
import org.cuahsi.waterml._1.ContactInformationType;
import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.QualifierType;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.SourceType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.TimeScale;
import org.cuahsi.waterml._1.VariableInfoType.VariableCode;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.cuahsi.waterml._1.essi.JAXBWML.WML_SiteProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.BNHSPropertyReader;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.OrganizationElementWrapper;
import eu.essi_lab.model.resource.QualifierElementWrapper;
import eu.essi_lab.model.resource.composed.ComposedElement;

/**
 * Helper for downloaders in the hydrology domain. A subclass can useful call getTimeSeriesTemplate method to have a WML
 * 1.1 template prefilled with metadata from the {@link GSResource}.
 * Then it should only call addValue method to add the user requested data values.
 * 
 * @author Boldrini
 */
public abstract class WMLDataDownloader extends DataDownloader {

    public TimeSeriesTemplate getTimeSeriesTemplate(String prefix, String suffix) throws Exception {
	return getTimeSeriesTemplate(getJaxbTimeSeriesTemplate(), prefix, suffix);

    }

    public TimeSeriesTemplate getTimeSeriesTemplate(TimeSeriesResponseType tsrt, String prefix, String suffix) throws Exception {
	File templateFile = File.createTempFile(prefix, suffix);
	TimeSeriesTemplate template = new TimeSeriesTemplate(templateFile, tsrt);
	return template;

    }

    @Deprecated
    public TimeSeriesResponseType getTimeSeriesTemplate() {
	return getJaxbTimeSeriesTemplate();
    }

    public TimeSeriesResponseType getJaxbTimeSeriesTemplate() {
	TimeSeriesResponseType ret = new TimeSeriesResponseType();
	TimeSeriesType timeSeries = new TimeSeriesType();
	VariableInfoType variableInfo = new VariableInfoType();
	ExtensionHandler extensions = resource.getExtensionHandler();

	String variableName = "";
	try {
	    variableName = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeTitle();
	} catch (Exception e) {
	}
	// if attribute URI is present, is preferred, to have an harmonized set of attributes
	Optional<String> optionalAttributeURI = resource.getExtensionHandler().getObservedPropertyURI();
	if (optionalAttributeURI.isPresent()) {
	    String uri = optionalAttributeURI.get();
	    if (uri != null) {

		JAXBWML.getInstance().addVariableURI(variableInfo, uri);

		HydroOntology ontology;

		if (uri.startsWith(HISCentralOntology.HIS_CENTRAL_BASE_URI)) {
		    ontology = new HISCentralOntology();
		}else{
		    ontology =new WHOSOntology();
		}
		SKOSConcept concept = ontology.getConcept(uri);
		if (concept != null) {
		    variableName = concept.getPreferredLabel("en");
		    HashSet<String> closeMatches = concept.getCloseMatches();
		    if (closeMatches != null && !closeMatches.isEmpty()) {
			try {
			    WMOOntology wmoOntology = new WMOOntology();
			    for (String closeMatch : closeMatches) {
				SKOSConcept variable = wmoOntology.getVariable(closeMatch);
				if (variable != null) {
				    String preferredLabel = variable.getPreferredLabel("en");
				    if (preferredLabel != null) {
//					variableName = preferredLabel.getKey();
				    }
				}
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}

		    }
		}
	    }
	}
	variableInfo.setVariableName(variableName);

	try {
	    switch (extensions.getTimeInterpolation().get()) {
	    case AVERAGE:
	    case AVERAGE_PREC:
	    case AVERAGE_SUCC:
		variableInfo.setDataType("Average");
		break;
	    case CATEGORICAL:
		variableInfo.setDataType("Categorical");
		break;
	    case CONST:
	    case CONST_PREC:
	    case CONST_SUCC:
		variableInfo.setDataType("Constant over interval");
		break;
	    case CONTINUOUS:
		variableInfo.setDataType("Continuous");
		break;
	    case DISCONTINUOUS:
		variableInfo.setDataType("Sporadic");
		break;
	    case INCREMENTAL:
		variableInfo.setDataType("Incremental");
		break;
	    case INSTANT_TOTAL:
		variableInfo.setDataType("Continuous");
		break;
	    case MAX:
	    case MAX_PREC:
	    case MAX_SUCC:
		variableInfo.setDataType("Maximum");
		break;
	    case MIN:
	    case MIN_PREC:
	    case MIN_SUCC:
		variableInfo.setDataType("Minimum");
		break;
	    case STATISTICAL:
		variableInfo.setDataType("");
		break;
	    case TOTAL:
	    case TOTAL_PREC:
	    case TOTAL_SUCC:
		variableInfo.setDataType("Cumulative");
		break;
	    default:
	    }
	} catch (Exception e) {
	}

	VariableCode variableCode = new VariableCode();

	try {
	    variableCode.setValue(
		    resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription().getAttributeIdentifier());
	} catch (Exception e) {
	}

	try {
	    variableCode.setVocabulary(resource.getSource().getUniqueIdentifier());
	} catch (Exception e) {
	}

	variableInfo.getVariableCode().add(variableCode);

	VariableCode variableCode2 = new VariableCode();
	variableCode2.setVocabulary("WHOS");
	variableCode2.setValue(resource.getExtensionHandler().getUniqueAttributeIdentifier().get());
	variableInfo.getVariableCode().add(variableCode2);

	try {
	    Double noDataValue = Double.parseDouble(extensions.getAttributeMissingValue().get());
	    variableInfo.setNoDataValue(noDataValue);
	} catch (Exception e) {
	}

	Optional<String> aggregationPeriod = resource.getExtensionHandler().getTimeAggregationDuration8601();
	Optional<String> resolutionPeriod = resource.getExtensionHandler().getTimeResolutionDuration8601();

	TimeScale timeScale = new TimeScale();
	org.cuahsi.waterml._1.ObjectFactory factory = new ObjectFactory();
	variableInfo.setTimeScale(factory.createVariableInfoTypeTimeScale(timeScale));
	UnitsType timeUnit = new UnitsType();
	timeUnit.setUnitType("Time");
	timeScale.setUnit(timeUnit);
	if (aggregationPeriod.isPresent() || resolutionPeriod.isPresent()) {

	    String aggregationTimeUnits = null;
	    String resolutionTimeUnits = null;
	    if (aggregationPeriod.isPresent()) {
		try {
		    Duration duration = ISO8601DateTimeUtils.getDuration(aggregationPeriod.get());
		    SimpleEntry<BigDecimal, String> unitsValue = ISO8601DateTimeUtils.getUnitsValueFromDuration(duration);
		    aggregationTimeUnits = unitsValue.getValue();
		    if (unitsValue.getKey() == null) {
			GSLoggerFactory.getLogger(getClass()).error("Null units for: {} {}", duration, aggregationTimeUnits);
		    } else {
			timeScale.setTimeSupport(unitsValue.getKey().floatValue());
		    }
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }
	    if (resolutionPeriod.isPresent()) {
		try {
		    Duration duration = ISO8601DateTimeUtils.getDuration(resolutionPeriod.get());
		    SimpleEntry<BigDecimal, String> unitsValue = ISO8601DateTimeUtils.getUnitsValueFromDuration(duration);
		    resolutionTimeUnits = unitsValue.getValue();
		    timeScale.setTimeSpacing(unitsValue.getKey().floatValue());
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }
	    if (aggregationTimeUnits != null && resolutionTimeUnits != null && !aggregationTimeUnits.equals(resolutionTimeUnits)) {
		GSLoggerFactory.getLogger(getClass()).error("Different time units in WOF profiler!");
	    }

	    String timeUnits = aggregationTimeUnits;
	    if (timeUnits == null) {
		timeUnits = resolutionTimeUnits;
	    }

	    if (timeUnits != null) {
		timeUnit.setUnitName(timeUnits);
	    }
	    String timeUnitsAbbreviation = ISO8601DateTimeUtils.getTimeUnitsAbbreviation(timeUnits);
	    if (timeUnitsAbbreviation != null) {
		timeUnit.setUnitAbbreviation(timeUnitsAbbreviation);
	    }

	}

	TsValuesSingleVariableType value = new TsValuesSingleVariableType();

	augmentValueInfo(value, resource);

	timeSeries.getValues().add(value);

	// variableInfo.g

	timeSeries.setVariable(variableInfo);
	List<ComposedElement> qualifiers = resource.getExtensionHandler().getComposedElements("qualifier");
	
	for (ComposedElement qualifier : qualifiers) {
	    QualifierElementWrapper wrapper = new QualifierElementWrapper(qualifier);
	    String flag = wrapper.getCode();
	    String description = wrapper.getDescription();
	    QualifierType qt = new QualifierType();
	    qt.setQualifierCode(flag);
	    qt.setQualifierDescription(description);
	    value.getQualifier().add(qt);
	}

	ret.getTimeSeries().add(timeSeries);

	SiteInfoType siteInfo = new SiteInfoType();
	augmentSiteInfo(siteInfo, resource);
	timeSeries.setSourceInfo(siteInfo);

	UnitsType units = new UnitsType();
	String unitsName = null;
	String unitsAbbreviation = null;
	if (extensions.getAttributeUnits().isPresent()) {
	    unitsName = extensions.getAttributeUnits().get();
	}
	if (extensions.getAttributeUnitsAbbreviation().isPresent()) {
	    unitsAbbreviation = extensions.getAttributeUnitsAbbreviation().get();
	}
	// if attribute URI is present, is preferred, to have an harmonized set of attribute units
	Optional<String> optionalAttributeUnitsURI = resource.getExtensionHandler().getAttributeUnitsURI();
	if (optionalAttributeUnitsURI.isPresent()) {
	    String uri = optionalAttributeUnitsURI.get();
	    if (uri != null) {

		try {
		    WMOOntology codes = new WMOOntology();
		    WMOUnit unit = codes.getUnit(uri);
		    if (unit != null) {
			unitsName = unit.getPreferredLabel("");
			unitsAbbreviation = unit.getAbbreviation();
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	if (unitsName != null) {
	    units.setUnitName(unitsName);
	    units.setUnitDescription(unitsName);
	}
	if (unitsAbbreviation != null) {
	    units.setUnitCode(unitsAbbreviation);
	    units.setUnitAbbreviation(unitsAbbreviation);
	}

	variableInfo.setUnit(units);

	return ret;
    }

    private void getTimeValueFromDuration(Duration duration) {
	// TODO Auto-generated method stub

    }

    public static void augmentValueInfo(TsValuesSingleVariableType value, GSResource resource) {
	ResponsibleParty poc = null;
	try {
	    poc = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification().getPointOfContact();
	} catch (Exception e) {
	}
	try {
	    if (poc == null) {
		poc = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
			.getCitationResponsibleParties().get(0);
	    }
	} catch (Exception e) {
	}

	if (poc != null) {
	    SourceType source = new SourceType();
	    source.setSourceID(1);

	    ContactInformationType contactInfo = new ContactInformationType();
	    try {
		source.setOrganization(poc.getOrganisationName());
		contactInfo.setContactName(poc.getIndividualName());
		contactInfo.setTypeOfContact(poc.getRoleCode());
		Contact c = poc.getContact();
		if (c != null) {
		    Address address = c.getAddress();
		    if (address != null) {
			contactInfo.getEmail().add(address.getElectronicMailAddress());
			Iterator<String> itPhone = c.getPhoneVoices();
			while (itPhone.hasNext()) {
			    String phone = (String) itPhone.next();
			    contactInfo.getPhone().add(phone);
			}

			String deliveryPoint = Optional.ofNullable(address.getDeliveryPoint()).map(v -> v + " ").orElse("");
			String city = Optional.ofNullable(address.getCity()).map(v -> v + " ").orElse("");
			String postalCode = Optional.ofNullable(address.getPostalCode()).map(v -> v + " ").orElse("");
			String country = Optional.ofNullable(address.getCountry()).map(v -> v + " ").orElse("");

			contactInfo.getAddress().add(deliveryPoint + city + postalCode + country);

		    }
		    Online co = c.getOnline();
		    if (co != null) {
			source.getSourceLink().add(co.getLinkage());
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    if (contactInfo.getContactName() == null) {
		contactInfo.setContactName("");
	    }
	    if (contactInfo.getTypeOfContact() == null) {
		contactInfo.setTypeOfContact("");
	    }
	    if (contactInfo.getEmail().isEmpty()) {
		contactInfo.getEmail().add("not available");
	    }
	    if (contactInfo.getPhone().isEmpty()) {
		contactInfo.getPhone().add("not available");
	    }
	    if (contactInfo.getAddress().isEmpty()) {
		contactInfo.getAddress().add("not available");
	    }
	    source.getContactInformation().add(contactInfo);

	    value.getSource().add(source);
	}

    }

    public void addValue(TimeSeriesTemplate template, ValueSingleVariable value) throws JAXBException {
	template.addValue(value);

    }

    @Deprecated
    public void addValue(TimeSeriesResponseType tsrt, ValueSingleVariable v) {
	List<SourceType> sources = tsrt.getTimeSeries().get(0).getValues().get(0).getSource();
	if (!sources.isEmpty()) {
	    v.setSourceID(new BigInteger("" + sources.get(0).getSourceID()));
	}
	tsrt.getTimeSeries().get(0).getValues().get(0).getValue().add(v);

    }

    public static void augmentSiteInfo(SiteInfoType siteInfo, GSResource resource) {
	if (resource == null) {
	    return;
	}
	SiteCode siteCode = new SiteCode();
	try {
	    if (resource.getExtensionHandler().getUniquePlatformIdentifier().isPresent()) {
		siteCode.setValue(resource.getExtensionHandler().getUniquePlatformIdentifier().get());
	    } else {
		siteCode.setValue(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getMDIdentifierCode());
	    }
	} catch (Exception e) {
	}
	try {
	    siteCode.setNetwork(resource.getSource().getUniqueIdentifier());
	} catch (Exception e) {
	}
	siteCode.setSiteID(1);
	siteInfo.getSiteCode().clear();
	siteInfo.getSiteCode().add(siteCode);
	try {
	    siteInfo.setSiteName(
		    resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle());
	} catch (Exception e) {
	}
	try {
	    siteInfo.setAltname(resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getDescription());
	} catch (Exception e) {
	}
	GeoLocation geog = new GeoLocation();
	siteInfo.setGeoLocation(geog);
	try {
	    LatLonPointType latLonType = new LatLonPointType();
	    latLonType.setSrs("EPSG:4326");
	    latLonType.setLatitude(resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox().getNorth());
	    latLonType.setLongitude(resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox().getEast());
	    geog.setGeogLocation(latLonType);
	} catch (Exception e) {
	}
	try {
	    siteInfo.setElevationM(
		    resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getVerticalExtent().getMaximumValue());
	    siteInfo.setVerticalDatum(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getVerticalExtent()
		    .getVerticalCRS().getId());
	} catch (Exception e) {
	}
	// Sets BNHS properties needed for the WHOS-Arctic
	List<SimpleEntry<BNHSProperty, String>> properties = BNHSPropertyReader.readProperties(resource);
	for (SimpleEntry<BNHSProperty, String> property : properties) {
	    BNHSProperty key = property.getKey();
	    if (key.isInWML()) {
		JAXBWML.getInstance().setPropertyIfNotPresent(siteInfo, key.getLabel(), property.getValue());
	    }
	}
	// Sets the country property
	Optional<String> country = resource.getExtensionHandler().getCountryISO3();
	if (country.isPresent()) {
	    Country decoded = Country.decode(country.get());
	    if (decoded != null) {
		String shortName = decoded.getShortName();
		JAXBWML.getInstance().setPropertyIfNotPresent(siteInfo, WML_SiteProperty.COUNTRY, shortName);
	    }
	}
    }
}
