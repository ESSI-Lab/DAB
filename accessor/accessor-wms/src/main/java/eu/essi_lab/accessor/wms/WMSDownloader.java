package eu.essi_lab.accessor.wms;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;

public abstract class WMSDownloader extends DataDownloader {

    public static final String WMS_DOWNLOADER_ERROR = "WMS_DOWNLOADER_ERROR";
    protected int PREVIEW_SIZE = 300;
    protected String name;

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.name = online.getName();
    }

    @Override
    public boolean canConnect() throws GSException {
	try {
	    IWMSCapabilities caps = getConnector().getCapabilities();
	    return caps != null;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return false;

    }

    public URL getImageURL(DataDescriptor descriptor, String baseURL) throws Exception {

	IWMSCapabilities capabilities = getConnector().getCapabilities();
	IWMSLayer layer = capabilities.getLayer(name);

	// CRS EPSG:4326<->OGC:84 change
	if ((descriptor.getCRS().equals(CRS.EPSG_4326()) && //
		layer.getCRSIdentifier(CRS.EPSG_4326()) == null && //
		layer.getCRSIdentifier(CRS.OGC_84()) != null) || //
		(descriptor.getCRS().equals(CRS.OGC_84()) && //
			layer.getCRSIdentifier(CRS.EPSG_4326()) != null && //
			layer.getCRSIdentifier(CRS.OGC_84()) == null)) {
	    List<DataDimension> spatialDimensions = descriptor.getSpatialDimensions();
	    DataDimension first = spatialDimensions.get(0);
	    DataDimension second = spatialDimensions.get(1);
	    spatialDimensions.clear();
	    spatialDimensions.add(second);
	    spatialDimensions.add(first);
	    descriptor.setSpatialDimensions(spatialDimensions);
	    if (descriptor.getCRS().equals(CRS.EPSG_4326())) {
		descriptor.setCRS(CRS.OGC_84());
		spatialDimensions.get(0).setType(DimensionType.COLUMN);
		spatialDimensions.get(1).setType(DimensionType.ROW);
	    } else {
		descriptor.setCRS(CRS.EPSG_4326());
		spatialDimensions.get(0).setType(DimensionType.ROW);
		spatialDimensions.get(1).setType(DimensionType.COLUMN);
	    }
	}

	List<DataDimension> spatialDimensions = descriptor.getSpatialDimensions();

	DataDimension firstDimension = spatialDimensions.get(0);
	DataDimension secondDimension = spatialDimensions.get(1);

	String bbox = getBBOXParameter(firstDimension, secondDimension, descriptor.getCRS());

	// TIME
	DataDimension temporalDimension = descriptor.getTemporalDimension();
	String time = "";
	if (temporalDimension != null) {
	    // OAK Ridge WMS doesn't support TIME Interval but individual value only
	    if (!baseURL.contains("webmap.ornl.gov/ogcbroker/wms")) {
		if (temporalDimension instanceof FiniteDimension) {
		    FiniteDimension finiteDimension = (FiniteDimension) temporalDimension;
		    time = "&TIME=" + //
			    finiteDimension.getPoints().get(0) + "/" + //
			    finiteDimension.getPoints().get(finiteDimension.getPoints().size() - 1);
		} else {
		    Date begin = null;
		    Date end = null;

		    if (temporalDimension != null && temporalDimension.getContinueDimension() != null
			    && temporalDimension.getContinueDimension().getUom() != null
			    && temporalDimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {

			ContinueDimension continueDimension = temporalDimension.getContinueDimension();

			begin = new Date(continueDimension.getLower().longValue());

			end = new Date(continueDimension.getUpper().longValue());

			time = "&TIME=" + //
				ISO8601DateTimeUtils.getISO8601DateTime(layer.getNearestAvailableTime(begin)) + "/" + //
				ISO8601DateTimeUtils.getISO8601DateTime(layer.getNearestAvailableTime(end));
		    }
		}
	    } else {
		if (temporalDimension instanceof FiniteDimension) {
		    FiniteDimension finiteDimension = (FiniteDimension) temporalDimension;
		    String date = finiteDimension.getPoints().get(0);
		    String[] splittedDate = date.split("-");
		    time = "&TIME=" + splittedDate[0] + "-" + splittedDate[1];
		} else {
		    // TODO: continueDimension should not be the case for OAK Ridge
		    GSLoggerFactory.getLogger(getClass()).trace("WARNING! OAK RIDGE CONTINUE DIMENSIONS!!!");
		}
	    }
	}

	List<String> styles = layer.getStyleNames();
	String style = styles.isEmpty() ? "" : styles.get(0);
	Integer width = layer.getFixedWidth();
	Integer height = layer.getFixedHeight();

	if (width == null || height == null && firstDimension != null && secondDimension != null) {

	    Long hSize, wSize;
	    switch (descriptor.getCRS().getAxisOrder()) {
	    case NORTH_EAST:
		hSize = firstDimension.getContinueDimension().getSize();
		wSize = secondDimension.getContinueDimension().getSize();
		break;
	    default:
		wSize = firstDimension.getContinueDimension().getSize();
		hSize = secondDimension.getContinueDimension().getSize();
		break;
	    }
	    if (hSize != null && wSize != null) {
		height = hSize.intValue();
		width = wSize.intValue();
	    }
	}
	if (width == null || height == null) {
	    height = layer.getDefaultHeight();
	    width = layer.getDefaultWidth();
	    while (height > 1000 || width > 1000) {
		height = height / 2;
		width = width / 2;
	    }
	}

	String chosenFormat = descriptor.getDataFormat().getIdentifier();
	String theFormat = "image/png";
	List<String> availableFormats = layer.getFormat();
	for (String availableFormat : availableFormats) {
	    DataFormat f1 = DataFormat.fromIdentifier(chosenFormat);
	    DataFormat f2 = DataFormat.fromIdentifier(availableFormat);
	    if (f1.equals(f2)) {
		theFormat = availableFormat;
	    }
	}

	// REQUEST URL

	String crsIdentifier = layer.getCRSIdentifier(descriptor.getCRS());

	String url = baseURL + "SERVICE=WMS&REQUEST=GetMap&LAYERS=" + urlEncode(name) + getVersionParameter() + "&STYLES="
		+ (style == null ? "" : urlEncode(style)) + getCRSKeyParameter() + crsIdentifier + "&FORMAT=" + urlEncode(theFormat) + time
		+ "&TRANSPARENT=TRUE";

	url += "&WIDTH=" + width + "&HEIGHT=" + height;

	url += bbox;

	GSLoggerFactory.getLogger(getClass()).trace("Default GetMap: {}", url);

	URL ret = null;
	try {

	    ret = new URL(url);
	} catch (MalformedURLException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Can't create URL from {}", url);
	}
	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	try {
	    URL url = getImageURL(descriptor);
	    Downloader downloader = new Downloader();
	    Optional<InputStream> ret = downloader.downloadStream(url.toString());
	    if (ret.isPresent()) {

		return IOStreamUtils.tempFilefromStream(ret.get(), "wms-downloader", ".png");

	    } else {
		throw GSException.createException( //
			getClass(), //
			"Download error", //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			WMS_DOWNLOADER_ERROR);
	    }
	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    "Download error", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WMS_DOWNLOADER_ERROR);
	}

    }

    public URL getImageURL(DataDescriptor descriptor) throws Exception {
	String baseURL = online.getLinkage();

	if (baseURL.endsWith("?") || baseURL.endsWith("&")) {
	    // nothing to do
	} else {
	    if (!baseURL.contains("?")) {
		baseURL = baseURL + "?";
	    } else {
		baseURL = baseURL + "&";
	    }
	}
	return getImageURL(descriptor, baseURL);
    }

    public abstract WMSConnector getConnector();

    protected void reduceDescriptor(DataDescriptor desc) {

	List<DataDimension> spatialDimensions = desc.getSpatialDimensions();
	DataDimension firstDimension = spatialDimensions.get(0);
	DataDimension secondDimension = spatialDimensions.get(1);

	Double secondExtent;
	Double firstExtent;

	secondExtent = secondDimension.getContinueDimension().getUpper().doubleValue()
		- secondDimension.getContinueDimension().getLower().doubleValue();
	firstExtent = firstDimension.getContinueDimension().getUpper().doubleValue()
		- firstDimension.getContinueDimension().getLower().doubleValue();

	Integer firstSize;
	Integer secondSize;
	if (firstExtent > secondExtent) {
	    firstSize = PREVIEW_SIZE;
	    Double ratio = (double) secondExtent / (double) firstExtent;
	    secondSize = (int) (firstSize * ratio);
	} else {
	    secondSize = PREVIEW_SIZE;
	    Double ratio = (double) firstExtent / (double) secondExtent;
	    firstSize = (int) (secondSize * ratio);
	}

	firstDimension.getContinueDimension().setSize(firstSize.longValue());
	secondDimension.getContinueDimension().setSize(secondSize.longValue());

	DataDimension temporalDimension = desc.getTemporalDimension();
	if (temporalDimension != null) {
	    reduceDimensionToPoint(temporalDimension);
	}

	List<DataDimension> otherDimensions = desc.getOtherDimensions();
	for (DataDimension dataDimension : otherDimensions) {
	    reduceDimensionToPoint(dataDimension);
	}

    }

    protected void reduceDimensionToPoint(DataDimension dataDimension) {
	if (dataDimension != null) {
	    if (dataDimension instanceof ContinueDimension) {

		ContinueDimension sizedDimension = dataDimension.getContinueDimension();
		sizedDimension.setUpper(sizedDimension.getLower());

	    } else {
		FiniteDimension discreteDimension = dataDimension.getFiniteDimension();
		discreteDimension.getPoints().subList(0, 0);
	    }
	}

    }

    public abstract String getVersionParameter();

    public abstract String getCRSKeyParameter();

    public abstract String getBBOXParameter(DataDimension firstDimension, DataDimension secondDimension, CRS crs);

    private String urlEncode(String layerName) {
	try {
	    if (layerName == null) {
		return "";
	    }
	    String enc = URLEncoder.encode(layerName, "UTF-8");
	    enc = enc.replace("+", "%20");
	    // OAK Ridge WMS give error with mode=8bit
	    if (online != null && online.getLinkage().contains("webmap.ornl.gov/ogcbroker/wms") && enc.contains("image%2Fpng")) {
		enc = "image%2Fpng";
	    }
	    return enc;
	} catch (Exception e) {
	    return layerName;
	}
    }

}
