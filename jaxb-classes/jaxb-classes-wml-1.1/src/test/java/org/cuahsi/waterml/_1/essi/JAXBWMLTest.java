package org.cuahsi.waterml._1.essi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.SeriesCatalogType;
import org.cuahsi.waterml._1.SeriesCatalogType.Series;
import org.cuahsi.waterml._1.SiteInfoResponseType;
import org.cuahsi.waterml._1.SiteInfoResponseType.Site;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.VariableInfoType;
import org.junit.Test;

/**
 * Tests that the two main types of WML documents are correctly parsed
 * 
 * @author boldrini
 */
public class JAXBWMLTest {

    @Test
    public void testTimeSeries() throws Exception {
	InputStream timeSeriesStream = JAXBWMLTest.class.getClassLoader().getResourceAsStream("timeSeriesResponse.xml");
	TimeSeriesResponseType tsrt = JAXBWML.getInstance().parseTimeSeries(timeSeriesStream);
	int value = tsrt.getTimeSeries().get(0).getValues().get(0).getValue().get(0).getValue().intValue();
	assertEquals(13, value);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	JAXBWML.getInstance().marshal(tsrt, baos);
	tsrt = JAXBWML.getInstance().parseTimeSeries(new ByteArrayInputStream(baos.toByteArray()));
	value = tsrt.getTimeSeries().get(0).getValues().get(0).getValue().get(0).getValue().intValue();
	assertEquals(13, value);
    }

    @Test
    public void testSites() throws Exception {
	InputStream sitesStream = JAXBWMLTest.class.getClassLoader().getResourceAsStream("sitesResponse.xml");
	SiteInfoResponseType sirt = JAXBWML.getInstance().parseSitesResponse(sitesStream);
	String siteName = sirt.getSite().get(0).getSiteInfo().getSiteName();
	assertEquals("Ancient_Forest", siteName);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	JAXBWML.getInstance().marshal(sirt, baos);
	sirt = JAXBWML.getInstance().parseSitesResponse(new ByteArrayInputStream(baos.toByteArray()));
	siteName = sirt.getSite().get(0).getSiteInfo().getSiteName();
	assertEquals("Ancient_Forest", siteName);
    }

    @Test
    public void testNamespaces() throws Exception {
	ObjectFactory factory = new ObjectFactory();
	TimeSeriesResponseType tsrt = new TimeSeriesResponseType();
	TimeSeriesType ts = new TimeSeriesType();
	tsrt.getTimeSeries().add(ts);
	JAXBElement<TimeSeriesResponseType> jaxb = factory.createTimeSeriesResponse(tsrt);
	JAXBWML.getInstance().marshal(jaxb, System.out);
    }

    @Test
    public void testSeriesRemoval() throws Exception {
	TimeSeriesResponseType timeSeries = JAXBWML.getInstance()
		.parseTimeSeries(JAXBWMLTest.class.getClassLoader().getResourceAsStream("multiple-series.xml"));
	assertEquals(6, timeSeries.getTimeSeries().size());
	JAXBWML.getInstance().filterSeriesByMethodId(timeSeries, 39886);
	assertEquals(1, timeSeries.getTimeSeries().size());
	JAXBWML.getInstance().filterSeriesByMethodId(timeSeries, 39886);
	assertEquals(1, timeSeries.getTimeSeries().size());
	JAXBWML.getInstance().filterSeriesByMethodId(timeSeries, 339886);
	assertEquals(0, timeSeries.getTimeSeries().size());
    }

    @Test
    public void testNullEmpty() throws Exception {
	SiteInfoResponseType sirt = new SiteInfoResponseType();
	Site site = new Site();
	SiteInfoType siteInfo = new SiteInfoType();
	SeriesCatalogType catalog = new SeriesCatalogType();
	Series series = new Series();
	VariableInfoType variable = new VariableInfoType();
	variable.setVariableName("testVariable");
	series.setVariable(variable);
	catalog.getSeries().add(series);
	site.getSeriesCatalog().add(catalog);
	site.setSiteInfo(siteInfo);
	sirt.getSite().add(site);
	JAXBElement<SiteInfoResponseType> jaxbElement = new JAXBElement<SiteInfoResponseType>(new QName("siteResponse"),
		SiteInfoResponseType.class, sirt);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	JAXBWML.getInstance().getMarshaller().marshal(jaxbElement, baos);
	JAXBWML.getInstance().getMarshaller().marshal(jaxbElement, System.out);
	baos.close();
	String str = new String(baos.toByteArray());
	assertTrue(str.contains("testVariable"));
	assertTrue(str.contains("noDataValue"));

    }

}
