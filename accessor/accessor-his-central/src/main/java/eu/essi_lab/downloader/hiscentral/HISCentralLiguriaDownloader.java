package eu.essi_lab.downloader.hiscentral;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.liguria.HISCentralLiguriaConnector;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.JSONArrayStreamParser;
import eu.essi_lab.lib.utils.JSONArrayStreamParserListener;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public class HISCentralLiguriaDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_LIGURIA_DOWNLOAD_ERROR = "HISCENTRAL_LIGURIA_DOWNLOAD_ERROR";

    /** Max lines per chunk for external sort (bounded memory). */
    private static final int EXTERNAL_SORT_CHUNK_SIZE = 50_000;

    private HISCentralLiguriaConnector connector;
    private Downloader downloader;

    /**
     * 
     */
    public HISCentralLiguriaDownloader() {

	connector = new HISCentralLiguriaConnector();
	downloader = new Downloader();
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	//
	// spatial extent
	//
	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	Double lat = bbox.getNorth();
	Double lon = bbox.getEast();

	descriptor.setEPSG4326SpatialDimensions(lat, lon);
	descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

	//
	// temp extent
	//
	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();

	String startDate = extent.getBeginPosition();
	String endDate = extent.getEndPosition();

	if (extent.isEndPositionIndeterminate()) {
	    endDate = ISO8601DateTimeUtils.getISO8601DateTime();
	}

	Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
	Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);

	if (optionalBegin.isPresent() && optionalEnd.isPresent()) {

	    Date begin = optionalBegin.get();
	    Date end = optionalEnd.get();

	    descriptor.setTemporalDimension(begin, end);

	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;

	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	}

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	Exception ex = null;

	try {

	    Date begin = null;
	    Date end = null;

	    ObjectFactory factory = new ObjectFactory();

	    String startString = null;
	    String endString = null;

	    DataDimension dimension = targetDescriptor.getTemporalDimension();

	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {

		ContinueDimension sizedDimension = dimension.getContinueDimension();

		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());

		startString = ISO8601DateTimeUtils.getISO8601DateTime(begin);
		endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
	    }

	    if (startString == null || endString == null) {

		startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
		endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
	    }

	    startString = transform(startString);
	    endString = transform(endString);
	    String linkage = online.getLinkage() + "&dtrf_beg=" + startString + "&dtrf_end=" + endString;
	    // linkage = linkage.replaceAll("Z", "");

	    String var = online.getName().split("_")[2];
	    String code = online.getName().split("_")[1];

	    TimeSeriesTemplate template = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    File tempFile = null;
	    try {

		tempFile = File.createTempFile(getClass().getSimpleName(), ".json");
		tempFile.deleteOnExit();
		try (InputStream is = HISCentralLiguriaConnector.getData(online.getLinkage(), code, startString, endString);
			OutputStream fileOut = new FileOutputStream(tempFile)) {
		    is.transferTo(fileOut);
		}
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).error("Failed to download or write HTTP response: " + e.getMessage());

	    }
	    // if (streamResp != null) {
	File sortedFile = null;
	try {
	    sortedFile = File.createTempFile(getClass().getSimpleName() + "_sorted", ".json");
	    sortedFile.deleteOnExit();
	    sortJsonArrayByDtrf(tempFile, sortedFile);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Failed to sort observations by time: " + e.getMessage());
	    throw GSException.createException(getClass(), e.getMessage(), null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, HISCENTRAL_LIGURIA_DOWNLOAD_ERROR);
	}
	File fileToParse = sortedFile != null ? sortedFile : tempFile;
	try (InputStream cachedStream = new FileInputStream(fileToParse)) {

		JSONArrayStreamParser parser = new JSONArrayStreamParser();

		parser.parse(cachedStream, new JSONArrayStreamParserListener() {
		    @Override
		    public void notifyJSONObject(JSONObject object) {

			ValueSingleVariable variable = new ValueSingleVariable();

			// TODO: get variable of interest -- see HISCentralLiguriaConnector class
			String valueString = object.optString(var);

			if (valueString != null && !valueString.isEmpty()) {

			    //
			    // value
			    //
			    // temperature and wind values need to be divided by 10
			    if (var.toLowerCase().contains("temp") || var.toLowerCase().contains("wspd")) {
				double d = Double.valueOf(valueString) / 10;
				valueString = String.valueOf(d);
			    }
			    // creek level values need to be divided by 100
			    if (var.toLowerCase().contains("crlvm")) {
				double d = Double.valueOf(valueString) / 100;
				valueString = String.valueOf(d);
			    }

			    BigDecimal dataValue = new BigDecimal(valueString);
			    variable.setValue(dataValue);

			    //
			    // date
			    //

			    String date = object.optString("DTRF");
			    Optional<Date> optionalDate = ISO8601DateTimeUtils.parseNotStandard2ToDate(date);
			    // DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
			    // iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			    if (optionalDate.isPresent()) {
				Date parsed = optionalDate.get();// iso8601OutputFormat.parse(date);

				GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
				gregCal.setTime(parsed);

				XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
				variable.setDateTimeUTC(xmlGregCal);

				try {
				    addValue(template, variable);
				} catch (JAXBException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}
			    }
			}

		    }

		    @Override
		    public void finished() {
			GSLoggerFactory.getLogger(getClass()).info("Completed download for station code: " + code + ".");

			// finished = true;
		    }

		    @Override
		    public void notifyJSONArray(JSONArray object) {
			// TODO Auto-generated method stub
			
		    }
		});

	    }

	    // Now it is safe to delete the temp files
	    if (tempFile != null && tempFile.exists()) {
		tempFile.delete();
	    }
	    if (sortedFile != null && sortedFile.exists()) {
		sortedFile.delete();
	    }

	    return template.getDataFile();

	} catch (

	Exception e) {

	    ex = e;
	}

	throw GSException.createException(//

		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_LIGURIA_DOWNLOAD_ERROR);

    }

    /**
     * Produces a second file with the same JSON array structure but observations
     * sorted by DTRF (oldest to newest). Uses external sort to avoid loading the
     * whole file in memory.
     *
     * @param inputJsonArray  file containing a JSON array of objects with DTRF
     * @param outputSortedJsonArray output file (same structure, sorted by DTRF)
     */
    private void sortJsonArrayByDtrf(File inputJsonArray, File outputSortedJsonArray) throws Exception {
	File linesFile = File.createTempFile(getClass().getSimpleName() + "_lines", ".txt");
	linesFile.deleteOnExit();
	List<File> runFiles = new ArrayList<>();
	try {
	    // 1) Stream input: one object at a time, write "DTRF\tJSON" per line
	    try (InputStream is = new FileInputStream(inputJsonArray);
		    BufferedWriter lineWriter = new BufferedWriter(
			    new FileWriter(linesFile, StandardCharsets.UTF_8))) {
		JSONArrayStreamParser parser = new JSONArrayStreamParser();
		parser.parse(is, new JSONArrayStreamParserListener() {
		    @Override
		    public void notifyJSONObject(JSONObject object) {
			try {
			    String dtrf = object.optString("DTRF", "");
			    lineWriter.write(dtrf);
			    lineWriter.write('\t');
			    lineWriter.write(object.toString());
			    lineWriter.newLine();
			} catch (IOException e) {
			    throw new RuntimeException(e);
			}
		    }

		    @Override
		    public void finished() {}

		    @Override
		    public void notifyJSONArray(JSONArray object) {}
		});
	    }

	    // 2) External sort: read in chunks, sort by DTRF, write runs
	    List<String> chunk = new ArrayList<>(EXTERNAL_SORT_CHUNK_SIZE);
	    try (BufferedReader reader = new BufferedReader(
		    new FileReader(linesFile, StandardCharsets.UTF_8))) {
		String line;
		while ((line = reader.readLine()) != null) {
		    chunk.add(line);
		    if (chunk.size() >= EXTERNAL_SORT_CHUNK_SIZE) {
			runFiles.add(sortChunkAndWriteRun(chunk));
			chunk.clear();
		    }
		}
		if (!chunk.isEmpty()) {
		    runFiles.add(sortChunkAndWriteRun(chunk));
		}
	    }

	    // 3) Build sorted JSON array: merge runs then write output, or write "[]" if empty
	    if (runFiles.isEmpty()) {
		try (BufferedWriter out = new BufferedWriter(
			new FileWriter(outputSortedJsonArray, StandardCharsets.UTF_8))) {
		    out.write("[]");
		}
		return;
	    }

	    File mergedFile = File.createTempFile(getClass().getSimpleName() + "_merged", ".txt");
	    mergedFile.deleteOnExit();
	    try {
		mergeRunFiles(runFiles, mergedFile);

		// 4) Write final JSON array from merged lines (strip DTRF prefix)
		try (BufferedReader merged = new BufferedReader(
			new FileReader(mergedFile, StandardCharsets.UTF_8));
			BufferedWriter out = new BufferedWriter(
				new FileWriter(outputSortedJsonArray, StandardCharsets.UTF_8))) {
		    out.write('[');
		    String line;
		    boolean first = true;
		    while ((line = merged.readLine()) != null) {
			int tab = line.indexOf('\t');
			String json = tab >= 0 ? line.substring(tab + 1) : line;
			if (!first) {
			    out.write(',');
			}
			out.newLine();
			out.write(json);
			first = false;
		    }
		    if (first) {
			out.newLine();
		    }
		    out.write(']');
		}
	    } finally {
		mergedFile.delete();
	    }
	} finally {
	    linesFile.delete();
	    for (File run : runFiles) {
		run.delete();
	    }
	}
    }

    private File sortChunkAndWriteRun(List<String> chunk) throws IOException {
	chunk.sort(Comparator.comparing(line -> {
	    int t = line.indexOf('\t');
	    return t >= 0 ? line.substring(0, t) : line;
	}));
	File run = File.createTempFile(getClass().getSimpleName() + "_run", ".txt");
	run.deleteOnExit();
	try (BufferedWriter w = new BufferedWriter(new FileWriter(run, StandardCharsets.UTF_8))) {
	    for (String line : chunk) {
		w.write(line);
		w.newLine();
	    }
	}
	return run;
    }

    private void mergeRunFiles(List<File> runFiles, File mergedFile) throws IOException {
	class RunReader {
	    final BufferedReader reader;
	    String dtrf;
	    String line;

	    RunReader(File f) throws IOException {
		this.reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
		this.line = reader.readLine();
		this.dtrf = line != null ? (line.indexOf('\t') >= 0 ? line.substring(0, line.indexOf('\t')) : line) : null;
	    }

	    boolean hasNext() {
		return line != null;
	    }

	    void advance() throws IOException {
		line = reader.readLine();
		dtrf = line != null ? (line.indexOf('\t') >= 0 ? line.substring(0, line.indexOf('\t')) : line) : null;
	    }
	}
	List<RunReader> readers = new ArrayList<>();
	try {
	    for (File f : runFiles) {
		readers.add(new RunReader(f));
	    }
	    try (BufferedWriter out = new BufferedWriter(new FileWriter(mergedFile, StandardCharsets.UTF_8))) {
		while (!readers.isEmpty()) {
		    int minIdx = 0;
		    String minDtrf = readers.get(0).dtrf;
		    for (int i = 1; i < readers.size(); i++) {
			String d = readers.get(i).dtrf;
			if (d != null && (minDtrf == null || d.compareTo(minDtrf) < 0)) {
			    minDtrf = d;
			    minIdx = i;
			}
		    }
		    RunReader r = readers.get(minIdx);
		    out.write(r.line);
		    out.newLine();
		    r.advance();
		    if (!r.hasNext()) {
			r.reader.close();
			readers.remove(minIdx);
		    }
		}
	    }
	} finally {
	    for (RunReader r : readers) {
		try {
		    r.reader.close();
		} catch (IOException ignored) {
		}
	    }
	}
    }

    private String transform(String startString) {
	String ret = null;
	String s = startString.replace("-", "").replace("T", "");
	String[] splittedTime = s.split(":");
	if (splittedTime.length > 1) {
	    ret = splittedTime[0] + splittedTime[1];
	}
	return ret;
    }

    @Override
    public boolean canSubset(String dimensionName) {

	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public boolean canDownload() {

	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getLinkage().contains(HISCentralLiguriaConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI));
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }
}
