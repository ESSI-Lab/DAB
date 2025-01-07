//package eu.essi_lab.accessor.worldcereal.downloader;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
//
//import java.io.File;
//import java.io.InputStream;
//import java.math.BigDecimal;
//import java.net.URISyntaxException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.GregorianCalendar;
//import java.util.List;
//import java.util.Locale;
//import java.util.Optional;
//import java.util.TimeZone;
//
//import javax.xml.bind.JAXBElement;
//import javax.xml.datatype.DatatypeFactory;
//import javax.xml.datatype.XMLGregorianCalendar;
//
//import org.cuahsi.waterml._1.ObjectFactory;
//import org.cuahsi.waterml._1.TimeSeriesResponseType;
//import org.cuahsi.waterml._1.ValueSingleVariable;
//import org.cuahsi.waterml._1.essi.JAXBWML;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import eu.essi_lab.access.DataDownloader;
//import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealCollectionMapper;
//import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealConnector;
//import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
//import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
//import eu.essi_lab.lib.net.downloader.Downloader;
//import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.lib.utils.IOStreamUtils;
//import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
//import eu.essi_lab.model.exceptions.ErrorInfo;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.model.resource.GSResource;
//import eu.essi_lab.model.resource.data.CRS;
//import eu.essi_lab.model.resource.data.DataDescriptor;
//import eu.essi_lab.model.resource.data.DataFormat;
//import eu.essi_lab.model.resource.data.DataType;
//import eu.essi_lab.model.resource.data.Unit;
//import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
//import eu.essi_lab.model.resource.data.dimension.DataDimension;
//
//public class WorldCerealDownloader extends DataDownloader {
//
//    private static final String WORLDCEREAL_DOWNLOAD_ERROR = null;
//
//    
//    private Downloader downloader;
//    private WorldCerealConnector connector;
//    
//    public WorldCerealDownloader() {
//	downloader = new Downloader();
//	connector = new WorldCerealConnector();
//    }
//
//    @Override
//    public boolean canDownload() {
//	return (online.getFunctionCode() != null && //
//		online.getFunctionCode().equals("download") && //
//		online.getLinkage() != null && //
//		online.getLinkage().contains(WorldCerealConnector.BASE_URL) && //
//		online.getProtocol() != null && //
//		online.getProtocol().equals(WorldCerealCollectionMapper.SCHEMA_URI));
//    }
//
//    @Override
//    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
//	List<DataDescriptor> ret = new ArrayList<>();
//
//	DataDescriptor descriptor = new DataDescriptor();
//	descriptor.setDataType(DataType.TIME_SERIES);
//	descriptor.setDataFormat(DataFormat.WATERML_1_1());
//	descriptor.setCRS(CRS.EPSG_4326());
//
//	//
//	// spatial extent
//	//
//	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
//
//	Double lat = bbox.getNorth();
//	Double lon = bbox.getEast();
//
//	descriptor.setEPSG4326SpatialDimensions(lat, lon);
//	descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
//	descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
//	descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
//	descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
//	descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
//	descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
//
//	//
//	// temp extent
//	//
//	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
//
//	String startDate = extent.getBeginPosition();
//	String endDate = extent.getEndPosition();
//
//	if (extent.isEndPositionIndeterminate()) {
//	    endDate = ISO8601DateTimeUtils.getISO8601DateTime();
//	}
//
//	Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
//	Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);
//
//	if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
//
//	    Date begin = optionalBegin.get();
//	    Date end = optionalEnd.get();
//
//	    descriptor.setTemporalDimension(begin, end);
//
//	    DataDimension temporalDimension = descriptor.getTemporalDimension();
//	    Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
//
//	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
//	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
//	}
//
//	ret.add(descriptor);
//
//	return ret;
//    }
//
//    @Override
//    public File download(DataDescriptor descriptor) throws GSException {
//	Exception ex = null;
//
//	try {
//
//	    Date begin = null;
//	    Date end = null;
//
//	    ObjectFactory factory = new ObjectFactory();
//
//	    String startString = null;
//	    String endString = null;
//
//	    DataDimension dimension = descriptor.getTemporalDimension();
//
//	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
//
//		ContinueDimension sizedDimension = dimension.getContinueDimension();
//
//		begin = new Date(sizedDimension.getLower().longValue());
//		end = new Date(sizedDimension.getUpper().longValue());
//
//		startString = ISO8601DateTimeUtils.getISO8601DateTime(begin);
//		endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
//	    }
//
//	    if (startString == null || endString == null) {
//
//		startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
//		endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
//	    }
//	    String linkage = online.getLinkage();
////	    + "&start=" + startString + "&end=" + endString;
////	    linkage = linkage.replaceAll("Z", "");
//
//	    Optional<InputStream> ret = downloader.downloadOptionalStream(linkage.toString());
//
//	    if (ret.isPresent()) {
//
//		return IOStreamUtils.tempFilefromStream(ret.get(), "worldcereal-downloader", ".json");
//
//	    }
//
//	} catch (Exception e) {
//
//	    ex = e;
//	}
//
//	throw GSException.createException(//
//		getClass(), //
//		ex.getMessage(), //
//		null, //
//		ErrorInfo.ERRORTYPE_INTERNAL, //
//		ErrorInfo.SEVERITY_ERROR, //
//		WORLDCEREAL_DOWNLOAD_ERROR);
//    }
//
//    @Override
//    public boolean canConnect() throws GSException {
//	try {
//	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
//	} catch (URISyntaxException e) {
//
//	    GSLoggerFactory.getLogger(getClass()).error(e);
//	}
//
//	return false;
//    }
//    
//    @Override
//    public void setOnlineResource(GSResource resource, String onlineId) throws GSException {
//        super.setOnlineResource(resource, onlineId);
//        this.connector.setSourceURL(resource.getSource().getEndpoint());
//    }
//
//    public static void main(String[] args) {
//	// TODO Auto-generated method stub
//
//    }
//
//}
