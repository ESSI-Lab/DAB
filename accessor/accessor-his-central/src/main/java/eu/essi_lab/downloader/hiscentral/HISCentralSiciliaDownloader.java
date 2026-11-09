package eu.essi_lab.downloader.hiscentral;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeFactory;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.sicilia.HISCentralSiciliaClient;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * Downloads the plausible data of a Protezione Civile Regione Siciliana element, e.g.:
 * https://www.protezionecivilesicilia.it:8443/DatascapeA/v3/data-combo/68615?from=...&to=...
 *
 * @author Roberto
 */
public class HISCentralSiciliaDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_SICILIA_DOWNLOAD_ERROR = "HISCENTRAL_SICILIA_DOWNLOAD_ERROR";

    @Override
    public boolean canDownload() {

	return online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getLinkage().startsWith(HISCentralSiciliaClient.getDataLinkagePrefix(resource.getSource().getEndpoint())) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI);
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(resource.getSource().getEndpoint());
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
	return false;
    }

    @Override
    public boolean canSubset(String dimensionName) {

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	descriptor.setEPSG4326SpatialDimensions(bbox.getNorth(), bbox.getEast());
	descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();

	String endDate = extent.isEndPositionIndeterminate() ? ISO8601DateTimeUtils.getISO8601DateTime() : extent.getEndPosition();

	Optional<Date> begin = ISO8601DateTimeUtils.parseISO8601ToDate(extent.getBeginPosition());
	Optional<Date> end = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);

	if (begin.isPresent() && end.isPresent()) {

	    descriptor.setTemporalDimension(begin.get(), end.get());

	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneDay = TimeUnit.DAYS.toMillis(1);
	    temporalDimension.getContinueDimension().setLowerTolerance(oneDay);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDay);
	}

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	try {

	    Instant end = Instant.now();
	    Instant begin = end.minus(1, java.time.temporal.ChronoUnit.DAYS);

	    DataDimension dimension = targetDescriptor.getTemporalDimension();

	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {

		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = Instant.ofEpochMilli(sizedDimension.getLower().longValue());
		end = Instant.ofEpochMilli(sizedDimension.getUpper().longValue());
	    }

	    String linkage = online.getLinkage();
	    String elementId = linkage.substring(linkage.lastIndexOf('/') + 1).split("\\?")[0];

	    HISCentralSiciliaClient client = new HISCentralSiciliaClient(resource.getSource().getEndpoint());

	    JSONArray data = client.getData(elementId, begin, end);

	    GSLoggerFactory.getLogger(getClass()).info("Element {}: {} values", elementId, data.length());

	    TimeSeriesTemplate template = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    for (int i = 0; i < data.length(); i++) {

		JSONArray pair = data.getJSONArray(i);

		ValueSingleVariable variable = new ValueSingleVariable();
		variable.setValue(pair.optBigDecimal(1, new BigDecimal("-9999")));

		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		calendar.setTimeInMillis(pair.getLong(0));
		variable.setDateTimeUTC(xmlFactory.newXMLGregorianCalendar(calendar));

		addValue(template, variable);
	    }

	    return template.getDataFile();

	} catch (GSException e) {
	    throw e;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_SICILIA_DOWNLOAD_ERROR, //
		    e);
	}
    }
}
