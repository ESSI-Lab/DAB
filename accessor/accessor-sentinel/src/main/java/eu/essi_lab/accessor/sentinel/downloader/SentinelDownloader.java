package eu.essi_lab.accessor.sentinel.downloader;

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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.locationtech.jts.geom.Envelope;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.sentinel.SatelliteLayers;
import eu.essi_lab.accessor.sentinel.SentinelConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataFormat.FormatType;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class SentinelDownloader extends DataDownloader {

    protected long PREVIEW_SIZE = 300l;

    private static final String SENTINEL_NOT_FOUND = "SENTINEL_NOT_FOUND";
    private static final String SENTINEL_DOWNLOADER_ERROR = "WCS_DOWNLOADER_ERROR";

    protected SentinelConnector connector;
    protected String name;
    protected GSResource source;

    private SimpleDateFormat sentinelDataFormat = null;

    private LinkedHashSet<String> availableCache = new LinkedHashSet<>();

    public static Double TOL = Math.pow(10, -10);

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	String linkage = online.getLinkage();
	this.name = online.getName();
	this.source = resource;
	this.connector = createConnector();
	this.connector.setSourceURL(linkage);
	this.sentinelDataFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	this.sentinelDataFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public SentinelConnector createConnector() {
	return new SentinelConnector();
    }

    public SentinelConnector getConnector() {
	return connector;
    }

    protected void setConnector(SentinelConnector connector) {
	this.connector = connector;
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(getWMSBaseEndpoint() + "request=GetCapabilities");
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.SENTINEL2_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	XMLDocumentReader coverage;
	try {
	    Double east = null;
	    Double west = null;
	    Double south = null;
	    Double north = null;
	    if (resource != null) {
		GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
		if (bbox != null) {
		    east = bbox.getEast();
		    west = bbox.getWest();
		    south = bbox.getSouth();
		    north = bbox.getNorth();
		}

		if (name != null && name.contains("@")) {
		    String privateId = resource.getPrivateId();
		    String publicId = resource.getPublicId();
		    String layer = name.split("@")[0];
		    String identifier = name.split("@")[1];
		}

		// query to DB, maybe it is not necessary
		// Bond bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL,
		// MetadataElement.IDENTIFIER,
		// privateId);
		//
		// DiscoveryMessage message = new DiscoveryMessage();
		// message.setPage(new Page(1));
		// message.setNormalizedBond(bond);
		//
		// StorageUri uri = ConfigurationUtils.getStorageURI();
		//
		//
		// // GSLoggerFactory.getLogger(OAIPMHRequestTransformer.class).debug("Storage uri:
		// // {}", uri);
		//
		// DatabaseReader reader = new DatabaseConsumerFactory().createDataBaseReader(uri);
		//
		// ResultSet<GSResource> resultSet = reader.discover(message);
		// if (!resultSet.getResultsList().isEmpty()) {
		//
		// GSResource resource = resultSet.getResultsList().get(0);
		// Optional<String> ts = resource.getPropertyHandler().getResourceTimeStamp();
		// GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
		//
		// east = bbox.getEast();
		// west = bbox.getWest();
		// south = bbox.getSouth();
		// north = bbox.getNorth();
		//
		// }

		// FORMATS
		DataFormat format = new DataFormat(FormatType.IMAGE_PNG);

		// CRS
		CRS crs = CRS.EPSG_4326();

		// TIME: TODO: check WMS implementation

		TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
		String beginString = null;
		String endString = null;
		beginString = temporalExtent.getBeginPosition();
		endString = temporalExtent.getEndPosition();

		DataDescriptor descriptor = new DataDescriptor();

		descriptor.setDataType(DataType.GRID);

		descriptor.setCRS(crs);

		descriptor.setDataFormat(format);

		if (beginString != null && endString != null) {
		    Date begin = sentinelDataFormat.parse(beginString);
		    Date end = sentinelDataFormat.parse(endString);
		    descriptor.setTemporalDimension(begin, end);
		}

		List<DataDimension> spatialDimensions = crs.getDefaultDimensions();

		DataDimension dimension1 = spatialDimensions.get(0);

		// dimension1.getContinueDimension().setResolution(Math.abs(resolution1));
		// dimension1.getContinueDimension().setSize(size1);
		dimension1.getContinueDimension().setLower(south);
		dimension1.getContinueDimension().setUpper(north);

		dimension1.getContinueDimension().setSize(256l);

		DataDimension dimension2 = spatialDimensions.get(1);

		// dimension2.getContinueDimension().setResolution(Math.abs(resolution2));
		// dimension2.getContinueDimension().setSize(size2);
		dimension2.getContinueDimension().setLower(west);
		dimension2.getContinueDimension().setUpper(east);
		dimension2.getContinueDimension().setSize(256l);

		descriptor.setSpatialDimensions(spatialDimensions);

		ret.add(descriptor);
	    }

	    return ret;

	} catch (Exception e) {
	    e.printStackTrace();
	    throw GSException.createException( //
		    getClass(), //
		    "WCS downloader error", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    SENTINEL_DOWNLOADER_ERROR, //
		    e);
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	InputStream is = null;

	try {

	    if (name != null && name.contains("@")) {

		SatelliteLayers[] vals = SatelliteLayers.values();
		SatelliteLayers l = null;
		for (SatelliteLayers v : vals) {
		    if (name.startsWith(v.toString())) {
			l = v;
			break;
		    }
		}

		if (l == null)
		    throw GSException.createException( //
			    getClass(), //
			    "Unexpected download error: unexpected Layer", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    SENTINEL_DOWNLOADER_ERROR);

		// time
		DataDimension dimension = descriptor.getTemporalDimension();
		String begin = null;
		String end = null;
		if (dimension != null) { // && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    ContinueDimension sizedDimension = dimension.getContinueDimension();
		    Date beginDate = new Date(sizedDimension.getLower().longValue());
		    Date endDate = new Date(sizedDimension.getUpper().longValue());
		    begin = sentinelDataFormat.format(beginDate); // + stamp;
		    end = sentinelDataFormat.format(endDate); // + stamp;
		}
		TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
		String beginString = null;
		String endString = null;
		beginString = temporalExtent.getBeginPosition();
		endString = temporalExtent.getEndPosition();

		// bbox
		List<DataDimension> spatialDimensions = descriptor.getSpatialDimensions();
		DataDimension firstDimension = spatialDimensions.get(0);
		DataDimension secondDimension = spatialDimensions.get(1);
		Number low1 = null;
		Number low2 = null;
		Number upp1 = null;
		Number upp2 = null;
		Envelope descriptorEnvelope = null;
		if (firstDimension != null && secondDimension != null) {
		    low1 = firstDimension.getContinueDimension().getLower();
		    low2 = secondDimension.getContinueDimension().getLower();
		    upp1 = firstDimension.getContinueDimension().getUpper();
		    upp2 = secondDimension.getContinueDimension().getUpper();

		    descriptorEnvelope = new Envelope(low1.doubleValue(), upp1.doubleValue(), low2.doubleValue(), upp2.doubleValue());
		}
		// String bbox = "&BBOX=" + //
		// firstDimension.getContinueDimension().getLower() + "," + //
		// secondDimension.getContinueDimension().getLower() + "," + //
		// firstDimension.getContinueDimension().getUpper() + "," + //
		// secondDimension.getContinueDimension().getUpper();

		GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
		// Double east = null;
		// Double west = null;
		// Double south = null;
		// Double north = null;
		// if (bbox != null) {
		// east = bbox.getEast();
		// west = bbox.getWest();
		// south = bbox.getSouth();
		// north = bbox.getNorth();
		// }

		String privateId = resource.getPrivateId();
		String publicId = resource.getPublicId();
		String layer = name.split("@")[0];
		String identifier = name.split("@")[1];
		double[] covlowercorner = null;
		double[] covuppercorner = null;
		Envelope resourceEnvelop = null;
		if (bbox != null) {
		    covlowercorner = new double[] { bbox.getSouth(), bbox.getWest() };
		    covuppercorner = new double[] { bbox.getNorth(), bbox.getEast() };
		    resourceEnvelop = new Envelope(bbox.getSouth(), bbox.getNorth(), bbox.getWest(), bbox.getEast());
		} else {
		    throw GSException.createException( //
			    getClass(), //
			    "Unexpected Sentinel download error: Bounding Box is null", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    SENTINEL_DOWNLOADER_ERROR);
		}

		if (!isAvailable(identifier, covlowercorner, covuppercorner, beginString, endString, l)) {
		    is = getSuggestionTile("Not yet available");
		    return IOStreamUtils.tempFilefromStream(is, "SENTINEL-DOWNLOADER", ".png");
		} else {

		    if (descriptor.isPreview()) {
			is = getPreview(identifier, covlowercorner, covuppercorner, beginString, endString, l);
			if (is == null) {
			    is = getSuggestionTile("Not yet available");
			    return IOStreamUtils.tempFilefromStream(is, "SENTINEL-DOWNLOADER", ".png");
			}
			return IOStreamUtils.tempFilefromStream(is, "SENTINEL-DOWNLOADER", ".png");
		    }

		    // if not in the selected area
		    // TODO: implement
		    if (descriptorEnvelope != null && resourceEnvelop != null && (!descriptorEnvelope.intersects(resourceEnvelop))) {
			is = getSuggestionTile("");
		    } else {
			double[] lowerCorner = new double[] { low1.doubleValue(), low2.doubleValue() };
			double[] upperCorner = new double[] { upp1.doubleValue(), upp2.doubleValue() };

			if (upperCorner[1] - lowerCorner[1] >= 5) {
			    is = getSuggestionTile("Zoom In");
			    return IOStreamUtils.tempFilefromStream(is, "SENTINEL-DOWNLOADER", ".png");
			} else {
			    // double centerLon = lowerCorner[1] + ((upperCorner[1] - lowerCorner[1]) / 2);
			    // double centerLat = lowerCorner[0] + ((upperCorner[0] - lowerCorner[0]) / 2);
			    // double radius = 0.2;
			    // double[] uc = new double[] { centerLat + radius, centerLon + radius };
			    // double[] lc = new double[] { centerLat - radius, centerLon - radius };

			    String link = getWMSBaseEndpoint() + //
				    "showLogo=false&service=WMS&request=GetMap&layers=" + l.getWMSLayer()
				    + "&styles=&format=image%2Fpng&transparent=true" + //
				    "&version=1.1.1&name=sentinel2&prettyName=Sentinel%202&height=256&width=256&srs=EPSG%3A4326&maxcc=100&"
				    + //
				    "priority=mostRecent&gain=1&evalscript=&COLCOR=&CLOUDCORRECTION=none&time="
				    + beginString.replaceAll("Z", "") + "%2F" + endString.replaceAll("Z", "") + "&bbox=" + lowerCorner[1]
				    + "," + lowerCorner[0] + "," + upperCorner[1] + "," + upperCorner[0];
			    is = getData(link);
			    if (is == null) {

			    } else {
				ClonableInputStream clone = new ClonableInputStream(is);
				if (isEmpty(clone))
				    is = getSuggestionTile("Zoom In");
				else
				    is = clone.clone();
			    }

			    return IOStreamUtils.tempFilefromStream(is, "SENTINEL-DOWNLOADER", ".png");
			}

		    }
		}

	    }

	    return IOStreamUtils.tempFilefromStream(is, "SENTINEL-DOWNLOADER", ".png");

	} catch (GSException e) {
	    throw e;
	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    "Unexpected download error", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    SENTINEL_DOWNLOADER_ERROR);
	} finally {
	    if (is != null)
		try {
		    is.close();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}

    }

    private InputStream getPreview(String identifier, double[] lowerCorner, double[] upperCorner, String start, String end,
	    SatelliteLayers layer) {
	double centerLon = lowerCorner[1] + ((upperCorner[1] - lowerCorner[1]) / 2);
	double centerLat = lowerCorner[0] + ((upperCorner[0] - lowerCorner[0]) / 2);

	double radius = 0.2;
	double[] uc = new double[] { centerLat + radius, centerLon + radius };
	double[] lc = new double[] { centerLat - radius, centerLon - radius };

	String link = getWMSBaseEndpoint() + //
		"showLogo=false&service=WMS&request=GetMap&layers=" + layer.getWMSLayer() + "&styles=&format=image%2Fpng&transparent=true" + //
		"&version=1.1.1&name=sentinel2&prettyName=Sentinel%202&height=256&width=256&srs=EPSG%3A4326&maxcc=100&" + //
		"priority=mostRecent&gain=1&evalscript=&COLCOR=&CLOUDCORRECTION=none&time=" + start.replaceAll("Z", "") + "%2F"
		+ end.replaceAll("Z", "") + "&bbox=" + lc[1] + "," + lc[0] + "," + uc[1] + "," + uc[0];
	InputStream is = getData(link);

	if (is == null) {
	    return null;
	}
	return is;

    }

    private String getWMSBaseEndpoint() {
	String token = ConfigurationWrapper.getCredentialsSetting().getSentinelDownloaderToken().orElse(null);
	return "https://services.sentinel-hub.com/ogc/wms/" + token + "?";
    }

    private boolean isAvailable(String identifier, double[] lowerCorner, double[] upperCorner, String start, String end,
	    SatelliteLayers layer) {

	synchronized (availableCache) {

	    if (availableCache.contains(identifier)) {
		return true;
	    }

	    double centerLon = lowerCorner[1] + ((upperCorner[1] - lowerCorner[1]) / 2);
	    double centerLat = lowerCorner[0] + ((upperCorner[0] - lowerCorner[0]) / 2);

	    double radius = 0.2;
	    double[] uc = new double[] { centerLat + radius, centerLon + radius };
	    double[] lc = new double[] { centerLat - radius, centerLon - radius };

	    String link = getWMSBaseEndpoint() + //
		    "showLogo=false&service=WMS&request=GetMap&layers=" + layer.getWMSLayer()
		    + "&styles=&format=image%2Fpng&transparent=true" + //
		    "&version=1.1.1&name=sentinel2&prettyName=Sentinel%202&height=256&width=256&srs=EPSG%3A4326&maxcc=100&" + //
		    "priority=mostRecent&gain=1&evalscript=&COLCOR=&CLOUDCORRECTION=none&time=" + start.replaceAll("Z", "") + "%2F"
		    + end.replaceAll("Z", "") + "&bbox=" + lc[1] + "," + lc[0] + "," + uc[1] + "," + uc[0];
	    try {
		InputStream is = getData(link);

		if (is == null) {
		    return false;
		}

		ClonableInputStream cis = new ClonableInputStream(is);

		if (isXML(cis.clone())) {

		    return false;
		}

		boolean available = !isEmpty(cis);

		if (available) {
		    if (availableCache.size() > 1000) {
			Iterator<String> it = availableCache.iterator();
			List<String> toRemove = new ArrayList<>();
			for (int i = 0; i < 500; i++) {
			    String id = it.next();
			    toRemove.add(id);
			}
			availableCache.removeAll(toRemove);
		    }
		    availableCache.add(identifier);

		}

		return available;

	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    return false;
	}
    }

    /**
     * If true probably is a ServiceExceptionReport
     * 
     * @param clone
     * @return
     */
    private boolean isXML(InputStream clone) {

	try {

	    new XMLDocumentReader(clone);
	    return true;

	} catch (Exception ex) {

	    return false;
	}
    }

    private InputStream getData(String link) {
	Downloader downloader = getHttpDownloader();
	Optional<InputStream> result = downloader.downloadOptionalStream(link);

	if (result.isPresent())
	    return result.get();

	return null;

    }

    public Downloader getHttpDownloader() {
	return new Downloader();
    }

    private InputStream getSuggestionTile(String todo) throws IOException {

	InputStream is = SentinelDownloader.class.getClassLoader().getResourceAsStream("emptyTile.png");

	final BufferedImage image = ImageIO.read(is);

	Graphics g = image.getGraphics();
	g.setColor(Color.RED);
	g.setFont(g.getFont().deriveFont(30f));

	g.drawString(todo, 10, 100);
	g.dispose();

	ByteArrayOutputStream os = new ByteArrayOutputStream();

	ImageIO.write(image, "png", os);

	is = new ByteArrayInputStream(os.toByteArray());

	return is;
    }

    private boolean isEmpty(ClonableInputStream cis) {

	try {
	    return cis.getLength() <= 300;
	} catch (Throwable e) {

	    e.printStackTrace();
	}

	return true;
	// return false;
    }

    @Override
    protected void reduceDimension(DataDimension dataDimension) {
	if (dataDimension != null) {
	    if (dataDimension instanceof ContinueDimension) {
		ContinueDimension continueDimension = dataDimension.getContinueDimension();
		Number resolution = continueDimension.getResolution();

		if (resolution == null)
		    return;

	    }
	}
    }

}
