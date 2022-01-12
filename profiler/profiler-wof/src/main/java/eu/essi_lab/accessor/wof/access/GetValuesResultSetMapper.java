package eu.essi_lab.accessor.wof.access;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.AbstractMap.SimpleEntry;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ContactInformationType;
import org.cuahsi.waterml._1.GeogLocationType;
import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.QueryInfoType;
import org.cuahsi.waterml._1.QueryInfoType.Criteria;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo;
import org.cuahsi.waterml._1.SiteInfoType.TimeZoneInfo.DefaultTimeZone;
import org.cuahsi.waterml._1.SourceInfoType;
import org.cuahsi.waterml._1.SourceType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.VariableCode;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.wof.WOFQueryUtils;
import eu.essi_lab.accessor.wof.WOFRequest;
import eu.essi_lab.accessor.wof.WOFRequest.Parameter;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
public class GetValuesResultSetMapper extends DefaultAccessResultSetMapper {

    private static final Double TOL = Math.pow(10, -8);
    private static final Double DEFAULT_NO_DATA_VALUE = -999999.0;

    @Override
    public DataObject map(AccessMessage message, DataObject resource) throws GSException {

	try {

	    XMLDocumentReader reader = getBaseResponse(message.getWebRequest(), resource);

	    String text = "";
	    text = new XMLNodeReader(reader.evaluateNode("//*:timeSeriesResponse[1]")).asString();
	    // to be able to detect both <timeSeriesResponse and <wml:timeSeriesResponse
	    int upperBeginTag = text.indexOf("timeSeriesResponse");
	    String firstPart = text.substring(0, upperBeginTag);
	    int beginTag = firstPart.lastIndexOf("<");
	    text = text.substring(beginTag);
	    // text = text.replace("<", "&lt;").replace(">", "&gt;");
	    resource.getFile().delete();

	    XMLDocumentReader reader2 = new XMLDocumentReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
		    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " + //
		    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" + //
		    "<soap:Body>\n" + //
		    "<GetValuesResponse xmlns=\"http://www.cuahsi.org/his/1.1/ws/\">\n" + //
		    "<GetValuesResult>\n" + //
		    "</GetValuesResult>\n" + //
		    "</GetValuesResponse>\n" + //
		    "</soap:Body>\n" + //
		    "</soap:Envelope>\n");
	    XMLDocumentWriter writer2 = new XMLDocumentWriter(reader2);
	    writer2.setText("//*:GetValuesResult[1]", text);

	    DataObject ret = new DataObject();
	    ret.setFileFromStream(reader2.asStream(), "GetValuesResultSetMapper");
	    ret.setDataDescriptor(resource.getDataDescriptor());
	    if (reader2.asStream() != null)
		reader2.asStream().close();
	    return ret;

	} catch (Exception e) {
	    // throw exception
	}
	return null;

    }

    protected XMLDocumentReader getBaseResponse(WebRequest webRequest, DataObject resource) throws Exception {

	Optional<GSResource> optionalResource = resource.getResource();

	/*
	 * SETTING QUERY INFO
	 */
	WOFRequest getValuesRequest = getValuesRequest(webRequest);

	String variable = getValuesRequest.getParameterValue(Parameter.VARIABLE);
	String startDate = getValuesRequest.getParameterValue(Parameter.BEGIN_DATE);
	String endDate = getValuesRequest.getParameterValue(Parameter.END_DATE);
	String location = getValuesRequest.getParameterValue(Parameter.SITE_CODE);

	boolean hideNoDataValues = (getValuesRequest.getParameterValue(Parameter.HIDE_NO_DATA_VALUES) != null
		&& getValuesRequest.getParameterValue(Parameter.HIDE_NO_DATA_VALUES).equals("true")) ? true : false;

	InputStream stream = new FileInputStream(resource.getFile());
	TimeSeriesResponseType timeSeries = JAXBWML.getInstance().parseTimeSeries(stream);
	stream.close();

	QueryInfoType queryInfo = timeSeries.getQueryInfo();
	if (queryInfo == null) {
	    queryInfo = new QueryInfoType();
	    timeSeries.setQueryInfo(queryInfo);
	}

	GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	calendar.setTime(new Date());
	XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	queryInfo.setCreationTime(xmlCalendar);
	Criteria criteria = new Criteria();
	criteria.setMethodCalled(getMethodName());

	org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter siteParam = new org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter();
	siteParam.setName("site");
	siteParam.setValue(location);
	criteria.getParameter().add(siteParam);

	org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter variableParam = new org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter();
	variableParam.setName("variable");
	variableParam.setValue(variable);
	criteria.getParameter().add(variableParam);

	org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter startParam = new org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter();
	startParam.setName("startDate");
	startParam.setValue(startDate);
	criteria.getParameter().add(startParam);

	org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter endParam = new org.cuahsi.waterml._1.QueryInfoType.Criteria.Parameter();
	endParam.setName("endDate");
	endParam.setValue(endDate);
	criteria.getParameter().add(endParam);

	queryInfo.setCriteria(criteria);

	/*
	 * SETTING NEEDED VALUES FOR HYDRODESKTOP
	 */

	List<TimeSeriesType> series = timeSeries.getTimeSeries();

	if (!series.isEmpty()) {

	    for (TimeSeriesType serie : series) {

		VariableInfoType var = serie.getVariable();
		if (var == null) {
		    var = new VariableInfoType();
		    serie.setVariable(var);
		}
		Double noDataValue = var.getNoDataValue();
		if (noDataValue == null) {
		    noDataValue = DEFAULT_NO_DATA_VALUE;
		    var.setNoDataValue(DEFAULT_NO_DATA_VALUE); // needed by WaterML R library. Otherwise it will crash
							       // during get values call: test from jupyter
		}
		if (var != null) {
		    String network = null;
		    String vocabulary = null;
		    if (!var.getVariableCode().isEmpty()) {
			network = var.getVariableCode().get(0).getNetwork();
			vocabulary = var.getVariableCode().get(0).getVocabulary();
		    }
		    var.getVariableCode().clear();
		    VariableCode variableCode = new VariableCode();
		    variableCode.setNetwork(network);
		    variableCode.setVocabulary(vocabulary);
		    variableCode.setValue(variable);
		    var.getVariableCode().add(variableCode);
		    String variableName = var.getVariableName();

		    if (WOFQueryUtils.isSemanticHarmonizationEnabled(webRequest) && optionalResource.isPresent()) {
			GSResource res = optionalResource.get();
			// SEMANTIC_HARMONIZATION if attribute URI is present, is preferred, to have an harmonized set
			// of attributes
			Optional<String> optionalAttributeURI = res.getExtensionHandler().getAttributeURI();
			if (optionalAttributeURI.isPresent()) {
			    String uri = optionalAttributeURI.get();
			    if (uri != null) {
				HydroOntology ontology = new HydroOntology();
				SKOSConcept concept = ontology.getConcept(uri);
				if (concept != null) {
				    variableName = concept.getPreferredLabel().getKey();
				    List<String> closeMatches = concept.getCloseMatches();
				    if (closeMatches != null && !closeMatches.isEmpty()) {
					try {
					    WMOOntology wmoOntology = new WMOOntology();
					    for (String closeMatch : closeMatches) {
						SKOSConcept skosConcept = wmoOntology.getVariable(closeMatch);
						if (skosConcept != null) {
						    SimpleEntry<String, String> preferredLabel = skosConcept.getPreferredLabel();
						    if (preferredLabel != null) {
							variableName = preferredLabel.getKey();
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

		    }

		    if (variableName != null) {
			// because USGS doesn't correctly escape
			variableName = variableName.replace("&#179;", "³");
		    }
		    var.setVariableName(variableName);

		    UnitsType units = var.getUnit();
		    if (units == null) {
			units = new UnitsType();
			var.setUnit(units);
		    }
		    if (units.getUnitName() == null) {
			units.setUnitName("");
		    }
		    if (units.getUnitDescription() == null) {
			units.setUnitDescription("");
		    }
		    if (units.getUnitAbbreviation() == null) {
			units.setUnitAbbreviation("");
		    }
		    if (units.getUnitType() == null) {
			units.setUnitType("");
		    }
		    if (units.getUnitCode() == null) {
			units.setUnitCode("");
		    }
		    // SEMANTIC_HARMONIZATION if attribute URI is present, is preferred, to have an harmonized set of
		    // attribute units
		    if (WOFQueryUtils.isSemanticHarmonizationEnabled(webRequest) && optionalResource.isPresent()) {
			GSResource res = optionalResource.get();
			Optional<String> optionalAttributeUnitsURI = res.getExtensionHandler().getAttributeUnitsURI();
			if (optionalAttributeUnitsURI.isPresent()) {
			    String uri = optionalAttributeUnitsURI.get();
			    if (uri != null) {

				try {
				    WMOOntology codes = new WMOOntology();
				    WMOUnit unit = codes.getUnit(uri);
				    if (unit != null) {
					units.setUnitName(unit.getPreferredLabel().getKey());
					units.setUnitAbbreviation(unit.getAbbreviation());
				    }
				} catch (Exception e) {
				    e.printStackTrace();
				}
			    }

			}
		    }
		}
		SourceInfoType sourceInfo = serie.getSourceInfo();
		if (sourceInfo == null) {
		    sourceInfo = new SiteInfoType();
		    serie.setSourceInfo(sourceInfo);
		}
		SiteInfoType siteInfo = null;
		if (sourceInfo instanceof SiteInfoType) {
		    siteInfo = (SiteInfoType) sourceInfo;
		    String network = null;
		    if (!siteInfo.getSiteCode().isEmpty()) {
			network = siteInfo.getSiteCode().get(0).getNetwork();
		    }
		    siteInfo.getSiteCode().clear();
		    SiteCode siteCode = new SiteCode();
		    siteCode.setNetwork(network);
		    siteCode.setValue(location);
		    siteInfo.getSiteCode().add(siteCode);
		    String siteName = siteInfo.getSiteName();
		    if (siteName == null || siteName.equals("")) {
			siteInfo.setSiteName(location);
		    }
		    GeoLocation geoLocation = siteInfo.getGeoLocation();
		    if (geoLocation != null) {
			GeogLocationType geogLocationType = geoLocation.getGeogLocation();
			if (geogLocationType instanceof LatLonPointType) {
			    LatLonPointType llp = (LatLonPointType) geogLocationType;

			}
		    }
		    if (optionalResource.isPresent()) {
			WMLDataDownloader.augmentSiteInfo(siteInfo, optionalResource.get());
		    }
		}
		List<TsValuesSingleVariableType> values = serie.getValues();

		if (values.isEmpty()) {
		    TsValuesSingleVariableType value = new TsValuesSingleVariableType();
		    values.add(value);
		}

		boolean setGMTTimeZone = true;
		for (TsValuesSingleVariableType value : values) {
		    List<SourceType> sources = value.getSource();
		    for (SourceType source : sources) {
			List<ContactInformationType> contactInformation = source.getContactInformation();
			for (ContactInformationType contactInfo : contactInformation) {
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
			}
		    }

		    List<ValueSingleVariable> innerValues = value.getValue();
		    // order must be ascendent in time for HydroDesktop
		    XMLGregorianCalendar tmpTime = null;
		    boolean reorderNeeded = false;
		    List<ValueSingleVariable> toRemove = new ArrayList<>();
		    for (ValueSingleVariable innerValue : innerValues) {
			if (hideNoDataValues) {
			    if (noDataValue != null) {
				double d = innerValue.getValue().doubleValue();
				if (Math.abs(d - noDataValue) < TOL) {
				    toRemove.add(innerValue);
				}
			    }
			}
			XMLGregorianCalendar time = innerValue.getDateTime();
			if (time == null) {
			    time = innerValue.getDateTimeUTC();
			    innerValue.setDateTime(time); // date time must be present because of the CUAHSI 1.1 schema!
			    innerValue.setTimeOffset(null);
			} else {
			    setGMTTimeZone = false;
			}
			if (tmpTime == null) {
			    if (time == null) {
				reorderNeeded = true;
				break;
			    }
			    tmpTime = time;
			} else {
			    int compare = tmpTime.compare(time);
			    switch (compare) {
			    case DatatypeConstants.GREATER:
				reorderNeeded = true;
				break;
			    default:
			    case DatatypeConstants.LESSER:
			    case DatatypeConstants.EQUAL:
			    case DatatypeConstants.INDETERMINATE:
				break;
			    }
			}
		    }
		    if (!toRemove.isEmpty()) {
			innerValues.removeAll(toRemove);
		    }
		    if (setGMTTimeZone) {
			TimeZoneInfo timeZoneInfo = new TimeZoneInfo();
			DefaultTimeZone defaultTimeZone = new DefaultTimeZone();
			defaultTimeZone.setZoneOffset("00:00");
			timeZoneInfo.setDefaultTimeZone(defaultTimeZone);
			// no date time found, only date time UTC
			// so it is safe to set time zone UTC
			// this is useful for the WaterML R package
			siteInfo.setTimeZoneInfo(timeZoneInfo);
		    }
		    if (reorderNeeded) {
			innerValues.sort(new Comparator<ValueSingleVariable>() {

			    @Override
			    public int compare(ValueSingleVariable o1, ValueSingleVariable o2) {
				XMLGregorianCalendar t1 = o1.getDateTimeUTC();
				if (t1 == null) {
				    t1 = o1.getDateTime();
				}
				XMLGregorianCalendar t2 = o2.getDateTimeUTC();
				if (t2 == null) {
				    t2 = o2.getDateTime();
				}
				if (t1 != null && t2 != null) {
				    return t1.compare(t2);
				} else {
				    return 0;
				}
			    }
			});

		    }
		}
	    }
	}

	ObjectFactory factory = new ObjectFactory();
	JAXBElement<TimeSeriesResponseType> jaxbElement = factory.createTimeSeriesResponse(timeSeries);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	JAXBWML.getInstance().marshal(jaxbElement, baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	baos.close();
	XMLDocumentReader ret = new XMLDocumentReader(bais);
	return ret;
    }

    public WOFRequest getValuesRequest(WebRequest webRequest) {
	return new GetValuesRequest(webRequest);
    }

    public String getMethodName() {
	return "GetValues";
    }

}
