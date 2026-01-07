package org.cuahsi.waterml._1.essi;

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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.NamespaceContext;

import org.cuahsi.waterml._1.MethodType;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.PropertyType;
import org.cuahsi.waterml._1.SiteInfoResponseType;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.VariableInfoType;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.essi_lab.lib.xml.NameSpace;
 
public class JAXBWML {
    private ObjectFactory factory;
    private JAXBContext context;

    public JAXBContext getContext() {
	return context;
    }

    private static BiMap<String, String> namespacePrefixMapper = null;
    static {
	namespacePrefixMapper = HashBiMap.create();

	namespacePrefixMapper.put("http://www.cuahsi.org/waterML/1.1/", "");
	namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
	namespacePrefixMapper.put("http://www.opengis.net/gml", "gml");
	namespacePrefixMapper.put("http://www.w3.org/1999/xlink", "xlink");
	namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema", "xsd");
	namespacePrefixMapper.put("http://www.cuahsi.org/waterML/", "wtr");

    }

    public ObjectFactory getFactory() {
	return factory;
    }

    private static JAXBWML instance = null;

    public static JAXBWML getInstance() {
	if (instance == null) {
	    instance = new JAXBWML();
	}
	return instance;
    }

    public void release() {
	instance = null;
	factory = null;
    }

    private JAXBWML() {
	try {
	    this.factory = new ObjectFactory();

	    this.context = JAXBContext.newInstance(ObjectFactory.class);

	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public NamespaceContext getNamespaceContext() {
	return new NamespaceContext() {

	    @Override
	    public Iterator getPrefixes(String namespaceURI) {
		return namespacePrefixMapper.values().iterator();
	    }

	    @Override
	    public String getPrefix(String namespaceURI) {
		return namespacePrefixMapper.get(namespaceURI);
	    }

	    @Override
	    public String getNamespaceURI(String prefix) {
		return namespacePrefixMapper.inverse().get(prefix);
	    }
	};
    }

    public Marshaller getMarshaller() throws JAXBException {
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, getNamespacePrefixMapper());
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	return marshaller;
    }

    public NamespacePrefixMapper getNamespacePrefixMapper() {
	return getNamespacePrefixMapper(new String[] {});
    }

    public NamespacePrefixMapper getNamespacePrefixMapper(String[] contextualNamespaces) {
	return new NamespacePrefixMapper() {

	    @Override
	    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		if (namespaceUri == null || namespaceUri.isEmpty()) {
		    return suggestion;
		}
		return namespacePrefixMapper.get(namespaceUri);
	    }

	    @Override
	    public String[] getContextualNamespaceDecls() {
		if (contextualNamespaces == null || contextualNamespaces.length == 0) {
		    return super.getContextualNamespaceDecls();
		}
		return contextualNamespaces;
	    }

	};
    }

    public Unmarshaller getUnmarshaller() throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	unmarshaller.setEventHandler(new ValidationEventHandler() {
	    @Override
	    public boolean handleEvent(ValidationEvent event) {
		System.out.println(event.getMessage());
		return true;
	    }
	});
	return unmarshaller;
    }

    public SiteInfoResponseType parseSitesResponse(InputStream stream) throws Exception {
	Object sites = getUnmarshaller().unmarshal(stream);
	if (sites instanceof JAXBElement<?>) {
	    sites = ((JAXBElement<?>) sites).getValue();
	}
	if (sites instanceof SiteInfoResponseType) {
	    SiteInfoResponseType sirt = (SiteInfoResponseType) sites;
	    return sirt;
	}
	return null;
    }

    private TimeSeriesResponseType castTimeSeries(Object series) {
	if (series instanceof JAXBElement<?>) {
	    series = ((JAXBElement<?>) series).getValue();
	}
	if (series instanceof TimeSeriesResponseType) {
	    TimeSeriesResponseType tsrt = (TimeSeriesResponseType) series;
	    return tsrt;
	}
	return null;
    }

    public TimeSeriesResponseType parseTimeSeries(File stream) throws Exception {
	Object series = getUnmarshaller().unmarshal(stream);
	return castTimeSeries(series);
    }

    public TimeSeriesResponseType parseTimeSeries(InputStream stream) throws Exception {
	Object series = getUnmarshaller().unmarshal(stream);
	return castTimeSeries(series);
    }

    private JAXBElement<?> getJAXBElement(Object jaxbElement) {
	if (jaxbElement instanceof JAXBElement<?>) {
	    return (JAXBElement<?>) jaxbElement;

	}
	if (jaxbElement instanceof TimeSeriesResponseType) {
	    TimeSeriesResponseType tsrt = (TimeSeriesResponseType) jaxbElement;
	    return factory.createTimeSeriesResponse(tsrt);
	}
	if (jaxbElement instanceof SiteInfoResponseType) {
	    SiteInfoResponseType sirt = (SiteInfoResponseType) jaxbElement;
	    return factory.createSitesResponse(sirt);

	}
	return null;
    }

    public void filterSeriesByMethodId(TimeSeriesResponseType seriesResponse, Integer methodId) {
	List<TimeSeriesType> series = seriesResponse.getTimeSeries();
	List<TimeSeriesType> seriesToRemove = new ArrayList<>();

	for (TimeSeriesType serie : series) {
	    List<TsValuesSingleVariableType> values = serie.getValues();
	    if (values != null) {
		if (values.isEmpty()) {
		    seriesToRemove.add(serie);
		} else {
		    TsValuesSingleVariableType value = values.get(0);
		    List<MethodType> methods = value.getMethod();
		    if (methods == null || methods.isEmpty()) {
			seriesToRemove.add(serie);
		    } else {
			boolean found = false;
			for (MethodType method : methods) {
			    Integer id = method.getMethodID();
			    if (id != null && id.equals(methodId)) {
				found = true;
				break;
			    }
			}
			if (!found) {
			    seriesToRemove.add(serie);
			}
		    }
		}
	    }
	}
	series.removeAll(seriesToRemove);

    }

    public void marshal(Object jaxbElement, javax.xml.transform.Result result) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, result);
    }

    public void marshal(Object jaxbElement, java.io.OutputStream os) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, os);
    }

    public void marshal(Object jaxbElement, File output) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, output);
    }

    public void marshal(Object jaxbElement, java.io.Writer writer) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, writer);
    }

    public void marshal(Object jaxbElement, org.xml.sax.ContentHandler handler) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, handler);
    }

    public void marshal(Object jaxbElement, org.w3c.dom.Node node) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, node);
    }

    public void marshal(Object jaxbElement, javax.xml.stream.XMLStreamWriter writer) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, writer);
    }

    public void marshal(Object jaxbElement, javax.xml.stream.XMLEventWriter writer) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, writer);
    }

    public enum WML_SiteProperty {
	COUNTRY("Country"), COUNTRY_ISO3("CountryISO3");

	private String code;

	public String getCode() {
	    return code;
	}

	WML_SiteProperty(String code) {
	    this.code = code;
	}

    }

    public String getProperty(SiteInfoType siteInfo, WML_SiteProperty property) {
	return getProperty(siteInfo, property.getCode());
    }

    public String getProperty(SiteInfoType siteInfo, String propertyCode) {
	if (siteInfo == null) {
	    return null;
	}
	List<PropertyType> properties = siteInfo.getSiteProperty();
	for (PropertyType property : properties) {
	    if (property.getName() != null && property.getName().equals(propertyCode)) {
		return property.getValue();
	    }
	}
	return null;
    }

    public void setPropertyIfNotPresent(SiteInfoType siteInfo, WML_SiteProperty property, String propertyValue) {
	setPropertyIfNotPresent(siteInfo, property.getCode(), propertyValue);
    }

    public void setPropertyIfNotPresent(SiteInfoType siteInfo, String propertyCode, String propertyValue) {
	String value = getProperty(siteInfo, propertyCode);
	if (value != null && !value.isEmpty()) {
	    return;
	}
	addProperty(siteInfo, propertyCode, propertyValue);
    }

    public void setProperty(SiteInfoType siteInfo, WML_SiteProperty property, String propertyValue) {
	setProperty(siteInfo, property.getCode(), propertyValue);
    }

    public void setProperty(SiteInfoType siteInfo, String propertyCode, String propertyValue) {
	clearProperty(siteInfo, propertyCode);
	addProperty(siteInfo, propertyCode, propertyValue);
    }

    public void clearProperty(SiteInfoType siteInfo, String propertyCode) {
	List<PropertyType> toRemove = new ArrayList<>();
	for (PropertyType propertyType : siteInfo.getSiteProperty()) {
	    if (propertyType.getName().equals(propertyCode)) {
		toRemove.add(propertyType);
	    }
	}
	siteInfo.getSiteProperty().removeAll(toRemove);
    }

    public void addProperty(SiteInfoType siteInfo, String propertyCode, String propertyValue) {
	PropertyType newProperty = new PropertyType();
	newProperty.setName(propertyCode);
	newProperty.setValue(propertyValue);
	siteInfo.getSiteProperty().add(newProperty);
    }

    public String getVariableURI(VariableInfoType variableInfo) {
	List<PropertyType> properties = variableInfo.getVariableProperty();
	for (PropertyType property : properties) {
	    if (property.getName().equals(VARIABLE_URI)) {
		return property.getValue();
	    }
	}
	return null;
    }

    public void addVariableURI(VariableInfoType variableInfo, String uri) {
	variableInfo.getVariableProperty().add(getVariableURIProperty(uri));
    }

    protected PropertyType getVariableURIProperty(String uri) {
	PropertyType ret = new PropertyType();
	ret.setName(VARIABLE_URI);
	ret.setValue(uri);
	ret.setUri(uri);
	return ret;
    }

    private static final String VARIABLE_URI = "variableURI";

}
