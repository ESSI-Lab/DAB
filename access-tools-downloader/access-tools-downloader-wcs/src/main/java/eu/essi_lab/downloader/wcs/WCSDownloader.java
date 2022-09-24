package eu.essi_lab.downloader.wcs;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.util.FileItemHeadersImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataFormat.FormatType;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.EPSGCRS;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;

public abstract class WCSDownloader extends DataDownloader {

    protected long PREVIEW_SIZE = 300l;

    private static final String WCS_COVERAGE_NOT_FOUND = "WCS_COVERAGE_NOT_FOUND";
    private static final String WCS_DOWNLOADER_ERROR = "WCS_DOWNLOADER_ERROR";
    @SuppressWarnings("rawtypes")
    protected WCSConnector connector;
    protected String name;
    private static Object DOWNLOAD_LOCK = new Object(); // only one thread at a time can download

    public static Double TOL = Math.pow(10, -10);

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	String linkage = online.getLinkage();
	this.name = online.getName();
	this.connector = createConnector();
	this.connector.setSourceURL(linkage);
    }

    @SuppressWarnings("rawtypes")
    public abstract WCSConnector createConnector();

    @SuppressWarnings("rawtypes")
    public WCSConnector getConnector() {
	return connector;
    }

    @Override
    public boolean canConnect() throws GSException {
	String endpoint = online.getLinkage();
	String version = getVersionParameter();
	try {
	    XMLDocumentReader caps = getConnector().getCapabilities(endpoint, version);
	    return caps != null;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return false;

    }

    @SuppressWarnings("rawtypes")
    protected void setConnector(WCSConnector connector) {
	this.connector = connector;
    }

    @Override
    public abstract boolean canDownload();

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	XMLDocumentReader coverage;
	try {
	    coverage = getConnector().getCoverageDescription(name);
	    if (coverage == null) {
		throw GSException.createException( //
			getClass(), //
			"Server not ready", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_FATAL, //
			"REMOTE_SERVER_ERROR");
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException( //
		    getClass(), //
		    "Server not ready: " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "REMOTE_SERVER_ERROR");
	}

	try {

	    String coverageName = getCoverageName(coverage);

	    if (coverageName == null || !coverageName.equals(name)) {
		throw GSException.createException( //
			getClass(), //
			"Coverage not found: " + name, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			WCS_COVERAGE_NOT_FOUND);
	    }

	    // FORMATS
	    Set<DataFormat> formats = getFormats(coverage);

	    // CRS
	    Set<CRS> crses = getResponseCRSes(coverage);

	    List<Date> times = getTimes(coverage);

	    for (DataFormat format : formats) {

		for (CRS crs : crses) {

		    // GRID ORIGIN
		    SimpleEntry<Double, Double> gridOrigin12 = getGridOrigin(coverage, crs);

		    // RESOLUTION
		    // gets resolution from the coverage, as +-res1, +-res2
		    List<Double> resolutions = getResolutions(coverage, crs);

		    // SIZE
		    List<Long> sizes = getSizes(coverage, crs);

		    if (gridOrigin12 != null && //
			    resolutions != null && !resolutions.isEmpty() && //
			    (sizes == null || sizes.isEmpty())) {
			// missing size information.. this is often the case for WCS 1.1.1
			// in these cases the bounding box should be perfectly aligned
			// to the grid, however a check is made before accepting such a descriptor.

			Double resolution1 = Math.abs(resolutions.get(0));
			Double lower1 = getLowerCornerFirstDimension(coverage, crs);
			Double upper1 = getUpperCornerFirstDimension(coverage, crs);

			Long size1 = calculateMissingSize(lower1, upper1, resolution1);

			Double resolution2 = Math.abs(resolutions.get(1));
			Double lower2 = getLowerCornerSecondDimension(coverage, crs);
			Double upper2 = getUpperCornerSecondDimension(coverage, crs);

			Long size2 = calculateMissingSize(lower2, upper2, resolution2);

			if (size1 != null && size2 != null) {
			    sizes = new ArrayList<Long>();
			    sizes.add(size1);
			    sizes.add(size2);
			}
		    }

		    if (gridOrigin12 != null && //
			    resolutions != null && !resolutions.isEmpty() && //
			    sizes != null && !sizes.isEmpty()) {
			// this is the only way to obtain correct results. Bounding box is not usually useful as
			// it is often larger with respect to the actual grid. It is used only when no size information
			// is available (e.g. WCS 1.1.1)

			Double origin1 = gridOrigin12.getKey();
			Double origin2 = gridOrigin12.getValue();

			Double resolution1 = resolutions.get(0);
			Double resolution2 = resolutions.get(1);

			Long size1 = sizes.get(0);
			Long size2 = sizes.get(1);

			SimpleEntry<Double, Double> gridCorners1 = getGridCorners(origin1, resolution1, size1);
			Double lower1 = gridCorners1.getKey();
			Double upper1 = gridCorners1.getValue();

			SimpleEntry<Double, Double> gridCorners2 = getGridCorners(origin2, resolution2, size2);
			Double lower2 = gridCorners2.getKey();
			Double upper2 = gridCorners2.getValue();

			DataDescriptor descriptor = new DataDescriptor();

			descriptor.setDataType(DataType.GRID);

			descriptor.setCRS(crs);

			descriptor.setDataFormat(format);

			List<DataDimension> spatialDimensions = crs.getDefaultDimensions();

			DataDimension dimension1 = spatialDimensions.get(0);

			dimension1.getContinueDimension().setResolution(Math.abs(resolution1));
			dimension1.getContinueDimension().setResolutionTolerance(Math.abs(resolution1 / 50.));
			dimension1.getContinueDimension().setSize(size1);
			dimension1.getContinueDimension().setLower(lower1);
			dimension1.getContinueDimension().setUpper(upper1);
			dimension1.getContinueDimension().setLowerTolerance(Math.abs(resolution1));
			dimension1.getContinueDimension().setUpperTolerance(Math.abs(resolution1));

			DataDimension dimension2 = spatialDimensions.get(1);

			dimension2.getContinueDimension().setResolution(Math.abs(resolution2));
			dimension2.getContinueDimension().setResolutionTolerance(Math.abs(resolution2 / 50.));
			dimension2.getContinueDimension().setSize(size2);
			dimension2.getContinueDimension().setLower(lower2);
			dimension2.getContinueDimension().setUpper(upper2);
			dimension2.getContinueDimension().setLowerTolerance(Math.abs(resolution2));
			dimension2.getContinueDimension().setUpperTolerance(Math.abs(resolution2));

			descriptor.setSpatialDimensions(spatialDimensions);

			if (times != null && !times.isEmpty()) {
			    descriptor.setTemporalDimension(times.get(0), times.get(times.size() - 1));
			    List<Number> points = new ArrayList<>();
			    for (Date date : times) {
				points.add(date.getTime());
			    }
			    descriptor.getTemporalDimension().getContinueDimension().setPoints(points);
			}

			ret.add(descriptor);

		    }
		}
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
		    WCS_DOWNLOADER_ERROR, //
		    e);
	}
    }

    protected abstract List<Date> getTimes(XMLDocumentReader coverage);

    private Long calculateMissingSize(Double lower, Double upper, Double resolution) {
	Long size = null;
	double extent = upper - lower;
	double reminder = extent % resolution;
	if (reminder < TOL || Math.abs(reminder - resolution) < TOL) {
	    // we are authorized to set the size
	    size = Math.round(extent / resolution);
	    if (getVersionParameter().equals("1.1.1")) {
		size = size + 1;
	    }
	}
	return size;
    }

    private SimpleEntry<Double, Double> getGridCorners(Double origin, Double resolution, Long size) {
	Double gridEnd = origin + resolution * size - resolution;

	SimpleEntry<Double, Double> ret;
	if (origin < gridEnd) {
	    ret = new SimpleEntry<Double, Double>(origin, gridEnd);
	} else {
	    ret = new SimpleEntry<Double, Double>(gridEnd, origin);
	}
	return ret;
    }

    protected abstract SimpleEntry<Double, Double> getGridOrigin(XMLDocumentReader coverage, CRS crs);

    // /**
    // * Note: min and maximum are not center of grid, but limits of the envelope
    // *
    // * @param min
    // * @param max
    // * @param resolution
    // * @param size
    // * @return
    // */
    // private Long adjustSize(Double min, Double max, Double resolution) {
    // double extent = max - min;
    // long points = (long) (extent / resolution);
    // return points;
    // }
    //
    // /**
    // * Note: min and maximum are not center of grid, but limits of the envelope
    // *
    // * @param min
    // * @param max
    // * @param resolution
    // * @return
    // */
    // private Double adjustResolution(Double min, Double max, Double resolution) {
    // if (resolution == null) {
    // return null;
    // }
    // if (min == null || max == null) {
    // return null;
    // }
    // double extent = max - min;
    // double reminder = extent % resolution;
    // if (reminder < TOL) {
    // return resolution;
    // } else {
    // // it's not an exact resolution! -- adjusting...
    // double points = Math.round(extent / resolution);
    // return extent / points;
    // }
    // }

    protected abstract List<Long> getSizes(XMLDocumentReader coverage, CRS crs);

    protected abstract Double getUpperCornerSecondDimension(XMLDocumentReader coverage, CRS crs);

    protected abstract Double getUpperCornerFirstDimension(XMLDocumentReader coverage, CRS crs);

    protected abstract Double getLowerCornerSecondDimension(XMLDocumentReader coverage, CRS crs);

    protected abstract Double getLowerCornerFirstDimension(XMLDocumentReader coverage, CRS crs);

    protected abstract List<Double> getResolutions(XMLDocumentReader coverage, CRS crs);

    protected abstract String getCoverageName(XMLDocumentReader coverage);

    protected abstract Set<DataFormat> getFormats(XMLDocumentReader coverage);

    /**
     * This method is used to tag as GeoTIFF the only Tiff format present (most likely it will be a GeoTIFF. The tagging
     * "as GeoTIFF" is not done on every tiff formats as there are cases where both TIFF and GeoTIFF formats are
     * returned!
     * 
     * @param formats
     */
    protected void possiblyTagGeoTiffFormat(HashSet<DataFormat> formats) {
	DataFormat possibleGeoTiff = null;
	boolean geoTiffFound = false;
	for (DataFormat format : formats) {
	    if (format.equals(DataFormat.IMAGE_GEOTIFF())) {
		geoTiffFound = true;
	    } else if (format.equals(DataFormat.IMAGE_TIFF())) {
		possibleGeoTiff = format;
	    }
	}
	if (!geoTiffFound && possibleGeoTiff != null) {
	    possibleGeoTiff.setType(FormatType.IMAGE_GEOTIFF);
	}
    }

    protected Set<CRS> getRequestCRSes(XMLDocumentReader coverage) {
	return getResponseCRSes(coverage);
    }

    protected abstract Set<CRS> getResponseCRSes(XMLDocumentReader coverage);

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    // synchronized (DOWNLOAD_LOCK) {// because many servers give error in case multiple download requests are
	    // executed at the same time

	    File file;
	    URL url = getDownloadURL(descriptor);
	    Downloader downloader = getHttpDownloader();
	    Optional<SimpleEntry<Header[], InputStream>> ret = downloader.downloadHeadersAndBody(url.toString());
	    if (ret.isPresent()) {
		// possibly unpack the multipart
		SimpleEntry<Header[], InputStream> headersAndBody = ret.get();
		file = unpackFromMultipart(headersAndBody.getKey(), headersAndBody.getValue());
		if (file == null) {
		    throw GSException.createException( //
			    getClass(), //
			    "Unpack error", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    WCS_DOWNLOADER_ERROR);
		}
		return file;

	    } else {
		throw GSException.createException( //
			getClass(), //
			"Download error", //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			WCS_DOWNLOADER_ERROR);
	    }

	    // }
	} catch (GSException e) {
	    throw e;
	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    "Unexpected download error", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WCS_DOWNLOADER_ERROR);
	}

    }

    /**
     * This can be used by subclasses to extract the useful stream from the total wcs response (as in case of multipart
     * responses)
     * 
     * @param ret
     * @return
     */
    protected File unpackFromMultipart(Header[] headers, InputStream body) {

	File tmpFile = null;
	try {
	    tmpFile = File.createTempFile("wcs-downloader", ".bin");
	    tmpFile.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(body, fos);
	    body.close();
	    fos.close();
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}

	String multipartBoundary = null;
	for (Header header : headers) {
	    if (header.getName().equals("Content-Type")) {
		String parameter = header.getValue();
		if (parameter.contains("multipart")) {
		    ParameterParser parser = new ParameterParser();
		    Map<String, String> result = parser.parse(parameter, ';');
		    multipartBoundary = result.get("boundary");
		}
	    }
	}

	if (multipartBoundary == null) {
	    return tmpFile;
	} else {

	    byte[] boundaryBytes = multipartBoundary.getBytes(StandardCharsets.UTF_8);

	    try {
		FileInputStream fis = new FileInputStream(tmpFile);

		MultipartStream multipartStream = new MultipartStream(fis, boundaryBytes, 4096, null);

		boolean nextPart = multipartStream.skipPreamble();

		while (nextPart) {
		    FileItemHeaders parsedHeaders = getParsedHeaders(multipartStream.readHeaders());
		    String contentType = parsedHeaders.getHeader("Content-Type");
		    if (contentType.toLowerCase().contains("xml")) {
			// it's the server response, skip this part
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			multipartStream.readBodyData(bos);
			bos.close();
			nextPart = multipartStream.readBoundary();
		    } else {
			File dataFile = File.createTempFile("wcs-downloader-multi-part", ".bin");
			FileOutputStream fos = new FileOutputStream(dataFile);
			multipartStream.readBodyData(fos);
			tmpFile.delete();
			fos.close();
			return dataFile;
		    }
		}
		fis.close();
	    } catch (Exception e) {
	    }

	    try {
		// some errors happened.. fallback with a fast implementation:
		FileInputStream fis = new FileInputStream(tmpFile);
		MultipartStreamSimple multipartStream = new MultipartStreamSimple(fis, multipartBoundary);
		String chosenContent = null;
		for (String contentType : multipartStream.getParts().keySet()) {

		    if (contentType != null && !contentType.toLowerCase().contains("xml")) {
			chosenContent = contentType;
		    }
		}
		File chosenFile = null;
		if (chosenContent == null) {
		    chosenFile = multipartStream.getParts().values().iterator().next();
		} else {
		    chosenFile = multipartStream.getParts().get(chosenContent);
		}
		for (File file : multipartStream.getParts().values()) {
		    if (!file.equals(chosenFile)) {
			file.delete();
		    }
		}
		tmpFile.delete();
		return chosenFile;

	    } catch (Exception e) {
	    }

	    return null;

	}

    }

    /**
     * <p>
     * Parses the <code>header-part</code> and returns as key/value
     * pairs.
     * <p>
     * If there are multiple headers of the same names, the name
     * will map to a comma-separated list containing the values.
     *
     * @param headerPart The <code>header-part</code> of the current
     *        <code>encapsulation</code>.
     * @return A <code>Map</code> containing the parsed HTTP request headers.
     */
    protected FileItemHeaders getParsedHeaders(String headerPart) {
	final int len = headerPart.length();
	FileItemHeadersImpl headers = new FileItemHeadersImpl();
	int start = 0;
	for (;;) {
	    int end = parseEndOfLine(headerPart, start);
	    if (start == end) {
		break;
	    }
	    StringBuilder header = new StringBuilder(headerPart.substring(start, end));
	    start = end + 2;
	    while (start < len) {
		int nonWs = start;
		while (nonWs < len) {
		    char c = headerPart.charAt(nonWs);
		    if (c != ' ' && c != '\t') {
			break;
		    }
		    ++nonWs;
		}
		if (nonWs == start) {
		    break;
		}
		// Continuation line found
		end = parseEndOfLine(headerPart, nonWs);
		header.append(" ").append(headerPart.substring(nonWs, end));
		start = end + 2;
	    }
	    parseHeaderLine(headers, header.toString());
	}
	return headers;
    }

    /**
     * Skips bytes until the end of the current line.
     * 
     * @param headerPart The headers, which are being parsed.
     * @param end Index of the last byte, which has yet been
     *        processed.
     * @return Index of the \r\n sequence, which indicates
     *         end of line.
     */
    private int parseEndOfLine(String headerPart, int end) {
	int index = end;
	for (;;) {
	    int offset = headerPart.indexOf('\r', index);
	    if (offset == -1 || offset + 1 >= headerPart.length()) {
		throw new IllegalStateException("Expected headers to be terminated by an empty line.");
	    }
	    if (headerPart.charAt(offset + 1) == '\n') {
		return offset;
	    }
	    index = offset + 1;
	}
    }

    /**
     * Reads the next header line.
     * 
     * @param headers String with all headers.
     * @param header Map where to store the current header.
     */
    private void parseHeaderLine(FileItemHeadersImpl headers, String header) {
	final int colonOffset = header.indexOf(':');
	if (colonOffset == -1) {
	    // This header line is malformed, skip it.
	    return;
	}
	String headerName = header.substring(0, colonOffset).trim();
	String headerValue = header.substring(header.indexOf(':') + 1).trim();
	headers.addHeader(headerName, headerValue);
    }

    public Downloader getHttpDownloader() {
	return new Downloader();
    }

    protected void reduceDescriptor(DataDescriptor desc) {

	List<DataDimension> spatialDimensions = desc.getSpatialDimensions();
	DataDimension dimension1 = spatialDimensions.get(0);
	DataDimension dimension2 = spatialDimensions.get(1);

	double upper1 = dimension1.getContinueDimension().getUpper().doubleValue();
	double lower1 = dimension1.getContinueDimension().getLower().doubleValue();
	Number res1 = dimension1.getContinueDimension().getResolution();
	Long size1 = dimension1.getContinueDimension().getSize();

	Double extent1 = upper1 - lower1;
	if (res1 != null) {
	    double res = res1.doubleValue() / 2.0;
	    lower1 = lower1 - res;
	    upper1 = upper1 + res;
	    extent1 = upper1 - lower1;
	}

	double lower2 = dimension2.getContinueDimension().getLower().doubleValue();
	double upper2 = dimension2.getContinueDimension().getUpper().doubleValue();
	Number res2 = dimension2.getContinueDimension().getResolution();
	Long size2 = dimension2.getContinueDimension().getSize();

	Double extent2 = upper2 - lower2;
	if (res2 != null) {
	    double res = res2.doubleValue() / 2.0;
	    lower2 = lower2 - res;
	    upper2 = upper2 + res;
	    extent2 = upper2 - lower2;
	}

	if (extent1 > extent2) {
	    size1 = PREVIEW_SIZE;
	    Double ratio = (double) extent2 / (double) extent1;
	    size2 = (long) (size1 * ratio);
	} else {
	    size2 = PREVIEW_SIZE;
	    Double ratio = (double) extent1 / (double) extent2;
	    size1 = (long) (size2 * ratio);
	}

	dimension1.getContinueDimension().setSize(size1);
	double resolution1 = extent1 / size1;
	dimension1.getContinueDimension().setResolution(resolution1);
	dimension1.getContinueDimension().setResolutionTolerance(resolution1 / 50.0);
	dimension1.getContinueDimension().setLowerTolerance(resolution1);
	dimension1.getContinueDimension().setUpperTolerance(resolution1);
	if (res1 != null) {
	    double res = resolution1 / 2.0;
	    lower1 = lower1 + res;
	    upper1 = upper1 - res;
	    dimension1.getContinueDimension().setLower(lower1);
	    dimension1.getContinueDimension().setUpper(upper1);
	}

	dimension2.getContinueDimension().setSize(size2);
	double resolution2 = extent2 / size2;
	dimension2.getContinueDimension().setResolution(resolution2);
	dimension2.getContinueDimension().setResolutionTolerance(resolution2 / 50.0);
	dimension2.getContinueDimension().setLowerTolerance(resolution2);
	dimension2.getContinueDimension().setUpperTolerance(resolution2);
	if (res2 != null) {
	    double res = resolution2 / 2.0;
	    lower2 = lower2 + res;
	    upper2 = upper2 - res;
	    dimension2.getContinueDimension().setLower(lower2);
	    dimension2.getContinueDimension().setUpper(upper2);
	}

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
		List<Number> points = new ArrayList<Number>();
		points.add(sizedDimension.getLower());
		sizedDimension.setPoints(points);

	    } else {
		FiniteDimension discreteDimension = dataDimension.getFiniteDimension();
		discreteDimension.getPoints().subList(0, 0);
	    }
	}

    }

    public URL getDownloadURL(DataDescriptor descriptor) throws Exception {

	String baseURL = getConnector().getSourceURL();

	if (baseURL.endsWith("?") || baseURL.endsWith("&")) {
	    // nothing to do
	} else {
	    if (!baseURL.contains("?")) {
		baseURL = baseURL + "?";
	    } else {
		baseURL = baseURL + "&";
	    }
	}
	return getDownloadURL(descriptor, baseURL);
    }

    public URL getDownloadURL(DataDescriptor descriptor, String baseURL) throws Exception {

	XMLDocumentReader coverage = getConnector().getCoverageDescription(name);

	List<DataDimension> spatialDimensions = descriptor.getSpatialDimensions();

	// CRS
	Set<CRS> responseCrses = getResponseCRSes(coverage);
	CRS outputCRS = null;
	for (CRS responseCRS : responseCrses) {
	    if (responseCRS.equals(descriptor.getCRS())) {
		outputCRS = responseCRS;
	    }
	}
	if (outputCRS == null) {
	    throw GSException.createException( //
		    getClass(), //
		    "No CRS specified for output", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WCS_DOWNLOADER_ERROR);
	}

	Set<CRS> requestCrses = getRequestCRSes(coverage);
	CRS inputCRS = null;
	for (CRS requestCRS : requestCrses) {
	    inputCRS = requestCRS;
	    if (requestCRS.equals(outputCRS)) {
		break;
	    }
	}

	SimpleEntry<Long, Long> userSpatialSizes = getSpatialSizes(spatialDimensions);

	SimpleEntry<Double, Double> userSpatialResolutions = getSpatialResolution(spatialDimensions);
	if (!inputCRS.equals(outputCRS)) {
	    userSpatialResolutions = null;
	}

	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> userLowerAndUpperCorners = getLowerAndUpperCorners(
		spatialDimensions);
	boolean invertedLatLon = false;
	if (inputCRS.equals(EPSGCRS.OGC_84()) && outputCRS.equals(EPSGCRS.EPSG_4326())) {
	    invertedLatLon = true;
	}

	if (!inputCRS.equals(outputCRS) && !invertedLatLon) {
	    double l1 = getLowerCornerFirstDimension(coverage, inputCRS);
	    double l2 = getLowerCornerSecondDimension(coverage, inputCRS);
	    SimpleEntry<Double, Double> lowerCorner = new SimpleEntry<Double, Double>(l1, l2);
	    double u1 = getUpperCornerFirstDimension(coverage, inputCRS);
	    double u2 = getUpperCornerSecondDimension(coverage, inputCRS);
	    SimpleEntry<Double, Double> upperCorner = new SimpleEntry<Double, Double>(u1, u2);
	    userLowerAndUpperCorners = new SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>>(lowerCorner, upperCorner);
	}

	if (userLowerAndUpperCorners != null && userSpatialResolutions == null && userSpatialSizes != null) {
	    SimpleEntry<Double, Double> lowerCorner = userLowerAndUpperCorners.getKey();
	    SimpleEntry<Double, Double> upperCorner = userLowerAndUpperCorners.getValue();
	    Double extent1 = upperCorner.getKey() - lowerCorner.getKey();
	    Double extent2 = upperCorner.getValue() - lowerCorner.getValue();
	    Double res1 = extent1 / (userSpatialSizes.getKey() - 1.0);
	    Double res2 = extent2 / (userSpatialSizes.getValue() - 1.0);
	    userSpatialResolutions = new SimpleEntry<Double, Double>(res1, res2);
	}

	if (userSpatialSizes == null && userSpatialResolutions == null) {
	    // SIZE: resorting to defaults
	    List<Long> defaultSpatialSizes = getSizes(coverage, outputCRS);
	    Long size1 = defaultSpatialSizes.get(0);
	    Long size2 = defaultSpatialSizes.get(1);
	    userSpatialSizes = new SimpleEntry<Long, Long>(size1, size2);
	}

	String resolutionParameter = getResolutionParameter(coverage, userSpatialResolutions, userSpatialSizes, outputCRS);

	// half resolution is added/subtracted from user grid points because WCS 1.0.0 requires the exterior envelope
	// instead of the center grid points limits (as in our data model and in CF)
	userLowerAndUpperCorners = fixUserLowerAndUpperCorners(userLowerAndUpperCorners, userSpatialResolutions);

	String bbox = getSpatialSubsetParameter(coverage, userLowerAndUpperCorners, outputCRS);

	// TIME
	List<Date> times = getTimes(coverage);

	DataDimension timeDimension = descriptor.getTemporalDimension();
	if (timeDimension != null) {
	    List<Number> points = timeDimension.getContinueDimension().getPoints();
	    List<Date> timesToMaintain = new ArrayList<>();
	    for (Number point : points) {
		for (Date time : times) {
		    if (time.getTime() == point.longValue()) {
			timesToMaintain.add(time);
		    }
		}
	    }
	    // if (timesToMaintain.isEmpty()) {
	    // timesToMaintain.add(times.get(0));
	    // }
	    times.retainAll(timesToMaintain);
	}

	String time = getTimesParameter(times);

	// FORMAT
	Set<DataFormat> availableFormats = getFormats(coverage);
	String theFormat = null;
	for (DataFormat availableFormat : availableFormats) {
	    if (availableFormat.equals(descriptor.getDataFormat())) {
		theFormat = availableFormat.getIdentifier();
	    }
	}
	if (theFormat == null) {
	    throw GSException.createException( //
		    getClass(), //
		    "No format specified", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WCS_DOWNLOADER_ERROR);
	}

	// REQUEST URL
	String url = baseURL + //
		"SERVICE=WCS" + //
		"&VERSION=" + getVersionParameter() + //
		"&REQUEST=GetCoverage" + //
		"&" + getCoverageParameter() + "=" + urlEncode(name) + //
		getCRSParameter(coverage, inputCRS, outputCRS) + //
		bbox + //
		time + //
		resolutionParameter + //
		"&FORMAT=" + urlEncode(theFormat);

	GSLoggerFactory.getLogger(getClass()).trace("GetCoverage: {}", url);

	URL ret = null;
	try {
	    ret = new URL(url);
	} catch (MalformedURLException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Can't create URL from {}", url);
	}
	return ret;
    }

    protected SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> fixUserLowerAndUpperCorners(
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> userLowerAndUpperCorners,
	    SimpleEntry<Double, Double> userSpatialResolutions) {
	return userLowerAndUpperCorners;
    }

    public abstract String getTimesParameter(List<Date> times);

    public SimpleEntry<Double, Double> getSpatialResolution(List<DataDimension> spatialDimensions) {
	Double res1 = null;
	Double res2 = null;
	DataDimension firstDimension = spatialDimensions.get(0);
	DataDimension secondDimension = spatialDimensions.get(1);
	if (firstDimension != null && secondDimension != null) {
	    Number resFirst = firstDimension.getContinueDimension().getResolution();
	    Number resSecond = secondDimension.getContinueDimension().getResolution();
	    if (resFirst != null && resSecond != null) {
		res1 = resFirst.doubleValue();
		res2 = resSecond.doubleValue();
		SimpleEntry<Double, Double> ret = new SimpleEntry<>(res1, res2);
		return ret;
	    }
	}
	return null;
    }

    public SimpleEntry<Long, Long> getSpatialSizes(List<DataDimension> spatialDimensions) {
	Long size1 = null;
	Long size2 = null;
	DataDimension firstDimension = spatialDimensions.get(0);
	DataDimension secondDimension = spatialDimensions.get(1);
	if (firstDimension != null && secondDimension != null) {
	    size1 = firstDimension.getContinueDimension().getSize();
	    size2 = secondDimension.getContinueDimension().getSize();
	    if (size1 != null && size2 != null) {
		SimpleEntry<Long, Long> ret = new SimpleEntry<>(size1, size2);
		return ret;
	    }
	}
	return null;
    }

    public SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> getLowerAndUpperCorners(
	    List<DataDimension> spatialDimensions) {
	DataDimension firstDimension = spatialDimensions.get(0);
	DataDimension secondDimension = spatialDimensions.get(1);
	if (firstDimension != null && secondDimension != null) {
	    Number min1 = firstDimension.getContinueDimension().getLower();
	    Number max1 = firstDimension.getContinueDimension().getUpper();
	    Number min2 = secondDimension.getContinueDimension().getLower();
	    Number max2 = secondDimension.getContinueDimension().getUpper();
	    if (min1 != null && min2 != null && max1 != null && max2 != null) {
		SimpleEntry<Double, Double> lowerCorner = new SimpleEntry<>(min1.doubleValue(), min2.doubleValue());
		SimpleEntry<Double, Double> upperCorner = new SimpleEntry<>(max1.doubleValue(), max2.doubleValue());
		return new SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>>(lowerCorner, upperCorner);
	    }
	}
	return null;
    }

    public abstract String getResolutionParameter(XMLDocumentReader coverage, SimpleEntry<Double, Double> userSpatialResolutions,
	    SimpleEntry<Long, Long> userSpatialSizes, CRS crs);

    public abstract String getCRSParameter(XMLDocumentReader coverage, CRS inputCRS, CRS outputCRS);

    public abstract String getCoverageParameter();

    /**
     * @param coverage the coverage description document
     * @param userLowerAndUpperCorners the user requested lower and upper corners (already converted from center grid
     *        points to enclosing envelope)
     * @param crs the crs chosen by the user
     * @return
     */
    public abstract String getSpatialSubsetParameter(XMLDocumentReader coverage,
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> userLowerAndUpperCorners, CRS crs);

    public abstract String getVersionParameter();

    protected String urlEncode(String parameter) {
	try {
	    if (parameter == null) {
		return "";
	    }
	    String enc = URLEncoder.encode(parameter, "UTF-8");
	    enc = enc.replace("+", "%20");
	    return enc;
	} catch (Exception e) {
	    return parameter;
	}
    }

}
