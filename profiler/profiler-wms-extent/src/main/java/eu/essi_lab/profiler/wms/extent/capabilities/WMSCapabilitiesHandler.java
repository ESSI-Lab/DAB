/**
 * 
 */
package eu.essi_lab.profiler.wms.extent.capabilities;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import eu.essi_lab.access.datacache.BBOX3857;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StationsStatistics;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.jaxb.wms._1_3_0.BoundingBox;
import eu.essi_lab.jaxb.wms._1_3_0.Capability;
import eu.essi_lab.jaxb.wms._1_3_0.ContactAddress;
import eu.essi_lab.jaxb.wms._1_3_0.ContactInformation;
import eu.essi_lab.jaxb.wms._1_3_0.ContactPersonPrimary;
import eu.essi_lab.jaxb.wms._1_3_0.DCPType;
import eu.essi_lab.jaxb.wms._1_3_0.EXGeographicBoundingBox;
import eu.essi_lab.jaxb.wms._1_3_0.Get;
import eu.essi_lab.jaxb.wms._1_3_0.HTTP;
import eu.essi_lab.jaxb.wms._1_3_0.Keyword;
import eu.essi_lab.jaxb.wms._1_3_0.KeywordList;
import eu.essi_lab.jaxb.wms._1_3_0.Layer;
import eu.essi_lab.jaxb.wms._1_3_0.LegendURL;
import eu.essi_lab.jaxb.wms._1_3_0.OnlineResource;
import eu.essi_lab.jaxb.wms._1_3_0.OperationType;
import eu.essi_lab.jaxb.wms._1_3_0.Request;
import eu.essi_lab.jaxb.wms._1_3_0.Service;
import eu.essi_lab.jaxb.wms._1_3_0.Style;
import eu.essi_lab.jaxb.wms._1_3_0.WMSCapabilities;
import eu.essi_lab.jaxb.wms.extension.JAXBWMS;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wms.extent.WMSLayer;

/**
 * @author boldrini
 */
public class WMSCapabilitiesHandler extends DefaultRequestHandler {

    private static final String WMS_CAPABILITIES_HANDLER_ERROR = "WMS_CAPABILITIES_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new WMSGetCapabilitiesRequest(request);
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	DataCacheConnector dataCacheConnector = null;

	try {

	    dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

	    if (dataCacheConnector == null) {
		DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
		dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
		String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
		String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
		String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
		dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
		dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
		dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
		DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
	    }

	    WMSCapabilities capabilities = new WMSCapabilities();

	    // SERVICE IDENTIFICATION

	    Service service = new Service();
	    capabilities.setService(service);

	    service.setName("WMS");
	    service.setTitle("DAB WMS");
	    service.setAbstract("GI-suite brokering service Web Map Service");
	    KeywordList klist = new KeywordList();
	    Keyword k1 = new Keyword();
	    k1.setValue("WMS");
	    klist.getKeywords().add(k1);
	    Keyword k2 = new Keyword();
	    k2.setValue("DAB");
	    klist.getKeywords().add(k2);
	    service.setKeywordList(klist);
	    OnlineResource online = new OnlineResource();
	    online.setHref("http://essi-lab.eu/");
	    online.setTitle("ESSI-Lab homepage");
	    service.setOnlineResource(online);
	    ContactInformation contactInfo = new ContactInformation();
	    contactInfo.setContactVoiceTelephone("+390555226591");
	    ContactPersonPrimary contactPerson = new ContactPersonPrimary();
	    contactPerson.setContactOrganization("CNR-IIA");
	    contactPerson.setContactPerson("Paolo Mazzetti");
	    contactInfo.setContactPersonPrimary(contactPerson);
	    contactInfo.setContactPosition("Head of the Division of Florence of CNR-IIA");
	    contactInfo.setContactElectronicMailAddress("info@essi-lab.eu");
	    ContactAddress address = new ContactAddress();
	    address.setAddressType("postal");
	    address.setAddress("Area di Ricerca di Firenze, Via Madonna del Piano, 10");
	    address.setCity("Sesto Fiorentino");
	    address.setCountry("Italy");
	    address.setPostCode("50019");
	    address.setStateOrProvince("Firenze");
	    contactInfo.setContactAddress(address);
	    service.setContactInformation(contactInfo);

	    service.setFees("none");
	    service.setAccessConstraints("none");
	    service.setLayerLimit(new BigInteger("16"));
	    service.setMaxWidth(new BigInteger("2048"));
	    service.setMaxHeight(new BigInteger("2048"));
	    Capability cap = new Capability();

	    Request request = new Request();

	    OperationType capOperation = new OperationType();
	    DCPType capDCPT = new DCPType();
	    HTTP capHttp = new HTTP();
	    String url = "";
	    List<WMSLayer> wmsLayers = null;
	    try {
		UriInfo uri = webRequest.getUriInfo();
		String viewPart = "";
		Optional<String> view = webRequest.extractViewId();
		wmsLayers = WMSLayer.decode(view);
		if (view.isPresent()) {
		    viewPart = "view/" + view.get() + "/";
		}
		url = uri.getRequestUri().toString();
		url = url.substring(0, url.indexOf("?") + 1);
	    } catch (Exception e) {
	    }

	    /// GET CAPABILITIES
	    Get capGet = new Get();
	    OnlineResource capOnlineGet = new OnlineResource();

	    capOnlineGet.setHref(url);

	    capGet.setOnlineResource(capOnlineGet);

	    capHttp.setGet(capGet);
	    capDCPT.setHTTP(capHttp);
	    capOperation.getDCPTypes().add(capDCPT);
	    capOperation.getFormats().add("text/xml");
	    request.setGetCapabilities(capOperation);

	    /// GET MAP
	    OperationType getMapOperation = new OperationType();
	    getMapOperation.getFormats().add("image/png");
	    getMapOperation.getFormats().add("image/gif");
	    getMapOperation.getFormats().add("image/jpeg");
	    DCPType mapDCPT = new DCPType();
	    HTTP mapHttp = new HTTP();
	    Get mapGet = new Get();
	    OnlineResource mapOnlineGet = new OnlineResource();

	    mapOnlineGet.setHref(url);
	    mapGet.setOnlineResource(mapOnlineGet);
	    mapHttp.setGet(mapGet);
	    mapDCPT.setHTTP(mapHttp);
	    getMapOperation.getDCPTypes().add(mapDCPT);
	    request.setGetMap(getMapOperation);

	    /// GET FEATURE INFO
	    OperationType getFeatureInfoOperation = new OperationType();
	    getFeatureInfoOperation.getFormats().add("application/json");
	    getFeatureInfoOperation.getFormats().add("text/html");
	    DCPType mapDCPTgfi = new DCPType();
	    HTTP mapHttpgfi = new HTTP();
	    Get mapGetgfi = new Get();
	    OnlineResource gfiOnlineGet = new OnlineResource();

	    gfiOnlineGet.setHref(url);
	    mapGetgfi.setOnlineResource(gfiOnlineGet);
	    mapHttpgfi.setGet(mapGetgfi);
	    mapDCPTgfi.setHTTP(mapHttpgfi);
	    getFeatureInfoOperation.getDCPTypes().add(mapDCPTgfi);
	    request.setGetFeatureInfo(getFeatureInfoOperation);

	    cap.setRequest(request);

	    capabilities.setCapability(cap);

	    eu.essi_lab.jaxb.wms._1_3_0.Exception exceptions = new eu.essi_lab.jaxb.wms._1_3_0.Exception();
	    exceptions.getFormats().add("XML");
	    exceptions.getFormats().add("BLANK");
	    cap.setException(exceptions);

	    Layer rootLayer = new Layer();

	    rootLayer.setTitle("DAB");
	    cap.setLayer(rootLayer);
	    rootLayer.getCRS().add("CRS:84");
	    rootLayer.getCRS().add("EPSG:3857");

	    for (WMSLayer wmsLayer : wmsLayers) {

		Double south = null;
		Double east = null;
		Double north = null;
		Double west = null;
		Double minx = null;
		Double miny = null;
		Double maxx = null;
		Double maxy = null;

		StationsStatistics statistics = dataCacheConnector.getStationStatisticsWithProperties(null, true,
			new SimpleEntry<String, String>(wmsLayer.getProperty(), wmsLayer.getValue()));
		BBOX4326 bbox4326 = statistics.getBbox4326();
		BBOX3857 bbox3857 = statistics.getBbox3857();

		south = bbox4326.getSouth().doubleValue();
		west = bbox4326.getWest().doubleValue();
		east = bbox4326.getEast().doubleValue();
		north = bbox4326.getNorth().doubleValue();
		miny = bbox3857.getMiny().doubleValue();
		minx = bbox3857.getMinx().doubleValue();
		maxx = bbox3857.getMaxx().doubleValue();
		maxy = bbox3857.getMaxy().doubleValue();

		double width = north.doubleValue() - south.doubleValue();
		double height = east.doubleValue() - west.doubleValue();

		double widthX = maxx - minx;
		double heightY = maxy - miny;

		maxx = maxx + widthX / 10;
		minx = minx - widthX / 10;
		maxy = maxy + heightY / 10;
		miny = miny - heightY / 10;

		north = north + height / 10;
		south = south - height / 10;
		west = west - width / 10;
		east = east + width / 10;

		if (north > 90) {
		    north = 90.;
		}
		if (south < -90) {
		    south = -90.;
		}
		if (east > 180) {
		    east = 180.;
		}
		if (west < -180) {
		    west = -180.;
		}

		EXGeographicBoundingBox bbox = new EXGeographicBoundingBox();
		bbox.setNorthBoundLatitude(north.doubleValue());
		bbox.setSouthBoundLatitude(south.doubleValue());
		bbox.setWestBoundLongitude(west.doubleValue());
		bbox.setEastBoundLongitude(east.doubleValue());

		Layer layer = new Layer();
		layer.setName(wmsLayer.getLayerName());
		layer.setTitle(wmsLayer.getLayerName());
		layer.setAbstract(wmsLayer.getLayerName());
		layer.setQueryable(true);
		layer.setEXGeographicBoundingBox(bbox);
		BoundingBox wbbox4326 = new BoundingBox();
		wbbox4326.setCRS("CRS:84");
		wbbox4326.setMinx(west.doubleValue());
		wbbox4326.setMaxx(east.doubleValue());
		wbbox4326.setMiny(south.doubleValue());
		wbbox4326.setMaxy(north.doubleValue());
		layer.getBoundingBoxes().add(wbbox4326);
		BoundingBox wbbox3857 = new BoundingBox();
		wbbox3857.setCRS("EPSG:3857");
		wbbox3857.setMinx(minx);
		wbbox3857.setMaxx(maxx);
		wbbox3857.setMiny(miny);
		wbbox3857.setMaxy(maxy);
		layer.getBoundingBoxes().add(wbbox3857);
		Style style = new Style();
		style.setName("default");
		style.setTitle("default");
		style.setAbstract("default");
		LegendURL legend = new LegendURL();
		legend.setWidth(new BigInteger("150"));
		legend.setHeight(new BigInteger("20"));
		legend.setFormat("image/png");
		OnlineResource legendOnline = new OnlineResource();
		legendOnline.setHref(url + "?service=WMS&request=GetLegendGraphic&LAYER=" + wmsLayer.getLayerName());
		legend.setOnlineResource(legendOnline);
		style.getLegendURLs().add(legend);
		layer.getStyles().add(style);
		rootLayer.getLayers().add(layer);

	    }

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    JAXBWMS.getInstance().getMarshaller().marshal(capabilities, baos);

	    ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());

	    XMLDocumentReader reader = new XMLDocumentReader(bis);
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    writer.setText("//*:Layer[@queryable='true']/@queryable", "1");

	    // InputStream stream = WMSCapabilitiesHandler.class.getClassLoader().getResourceAsStream("cap.xml");
	    // IOUtils.copy(stream, baos);
	    // stream.close();

	    String ret = reader.asString();
	    baos.close();

	    return ret;

	} catch (

	Exception e) {
	    e.printStackTrace();

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WMS_CAPABILITIES_HANDLER_ERROR, //
		    e);
	}

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_XML_TYPE;
    }
}
