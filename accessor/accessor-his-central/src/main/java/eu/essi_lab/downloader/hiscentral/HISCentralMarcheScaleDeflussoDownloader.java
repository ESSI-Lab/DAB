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

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.marche.HISCentralMarcheConnector;
import eu.essi_lab.accessor.hiscentral.marche.HISCentralMarcheConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurvePoint;
import eu.essi_lab.model.ratings.RatingCurves;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import reactor.util.repeat.RepeatSpec;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Fabrizio
 */
public class HISCentralMarcheScaleDeflussoDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_MARCHE_SCALE_DEFLUSSO_DOWNLOAD_ERROR = "HISCENTRAL_MARCHE_SCALE_DEFLUSSO_DOWNLOAD_ERROR";

    private HISCentralMarcheConnector connector;
    private Downloader downloader;

    /**
     *
     */
    public HISCentralMarcheScaleDeflussoDownloader() {

	connector = new HISCentralMarcheConnector();
	downloader = new Downloader();
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.RATING_CURVE);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setDataFormat(DataFormat.WATERML_2_0());

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	Exception ex = null;

	try {
	    String linkage = online.getLinkage();
	    Downloader d = new Downloader();
	    Optional<String> response = d.downloadOptionalString(linkage);
	    RatingCurves ratingCurves = null;
	    if(response.isPresent()) {
		ratingCurves = getRatingCurves(response.get());
	    }

	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	    Files.write(tmpFile.toPath(), ratingCurves.toWaterML2().getBytes());
	    return tmpFile;

	} catch (Exception e) {

	    ex = e;
	}

	if (ex != null) {

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_MARCHE_SCALE_DEFLUSSO_DOWNLOAD_ERROR);
	}

	return null;
    }

    private RatingCurves getRatingCurves(String s) throws GSException {
	Map<String, RatingCurve> curvesByPeriod = new LinkedHashMap<>();

	try {


	JSONObject root = new JSONObject(s);
	JSONArray results = root.getJSONArray("ratingCurves");
	for (int i = 0; i < results.length(); i++) {
	    JSONObject obj = results.getJSONObject(i);

	    String key = obj.optString("validPeriod");

	    String[] splittedTime = key.split("/");



	    LocalDate beginDate = convertDate(splittedTime[0]);
	    LocalDate endDate = convertDate(splittedTime[1]);
	    JSONArray values = obj.optJSONArray("points");

		for(int k=0; k < values.length(); k++){
		    JSONObject point = values.getJSONObject(k);
		    BigDecimal level = point.optBigDecimal("stage", null);
		    BigDecimal discharge = point.optBigDecimal("discharge", null);
		    RatingCurve curve = curvesByPeriod.get(key);
		    if (curve == null) {
			curve = new RatingCurve(beginDate, endDate);
			curvesByPeriod.put(key, curve);
		    }

		    curve.addPoint(new RatingCurvePoint(level, discharge));

		}

	}

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error while parsing paginated rating curves", e);

	    throw GSException.createException(getClass(), "Unable to parse paginated rating curves", null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, HISCENTRAL_MARCHE_SCALE_DEFLUSSO_DOWNLOAD_ERROR);

	}
	return new RatingCurves(curvesByPeriod.values());
    }

    private LocalDate convertDate(String input) {
	// formatter matching your pattern
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");

	// parse to OffsetDateTime
	OffsetDateTime odt = OffsetDateTime.parse(input, formatter);

	// extract LocalDate
	LocalDate date = odt.toLocalDate();

	return date;
    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;
    }

    @Override
    public boolean canDownload() {

	boolean ret = (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getLinkage().contains(HISCentralMarcheConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_MARCHE_SCALE_DEFLUSSO_NS_URI));

	if (ret){
	    GSLoggerFactory.getLogger(getClass()).info("Deflusso downloader found");
	}

	return ret;
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
