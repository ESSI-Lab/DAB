package eu.essi_lab.accessor.rihmi;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasureTVPType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.ReferenceType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
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
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;

public class RIHMIAralDownloader extends DataDownloader {

    private static final String RIHMI_ARAL_DOWNLOAD_ERROR = "RIHMI_ARAL_DOWNLOAD_ERROR";

    private RIHMIClient client = new RIHMIClient();

    private String stationId;
    
    private boolean isDischarge;

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.stationId = RIHMIConnector.extractStationId(online.getLinkage());
	if(online.getLinkage().contains(client.getAralDischargeEndpoint())){
	    isDischarge = true;
	}

    }

    @Override
    public boolean canConnect() throws GSException {
	try {
	    Date start = ISO8601DateTimeUtils
		    .parseISO8601ToDate(resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getBeginPosition()).get();
	    Date end = ISO8601DateTimeUtils
		    .parseISO8601ToDate(resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getEndPosition()).get();
	    
	    InputStream data = client.getAralWaterML(stationId, start, end, isDischarge);
	    XMLDocumentReader reader = new XMLDocumentReader(data);
	    Number count = reader.evaluateNumber("count(//*:value)");
	    System.out.println(count.longValue() + " values retrieved");
	    return (count.longValue() > 20);
	} catch (Exception e) {
	    // TODO: handle exception
	}
	return false;
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.ARAL_BASIN_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {

	return true;

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	try {

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_2_0());
	    descriptor.setCRS(CRS.EPSG_4326());
	    String name = online.getName();

	    RIHMIIdentifierMangler mangler = new RIHMIIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterId = mangler.getParameterIdentifier();
		String stationId = mangler.getPlatformIdentifier();

		Date start = ISO8601DateTimeUtils
			.parseISO8601ToDate(resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getBeginPosition())
			.get();
		Date end = ISO8601DateTimeUtils
			.parseISO8601ToDate(resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getEndPosition()).get();
		descriptor.setTemporalDimension(start, end);
		DataDimension temporalDimension = descriptor.getTemporalDimension();
		temporalDimension.getContinueDimension().setUpperTolerance(1000 * 60 * 60 * 24 * 3);
		ret.add(descriptor);
		return ret;

	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RIHMI_ARAL_DOWNLOAD_ERROR, //
		    e);

	}
	return ret;

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();

	    RIHMIIdentifierMangler mangler = new RIHMIIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterName = mangler.getParameterIdentifier();
		String stationId = mangler.getPlatformIdentifier();

		Date begin = null;
		Date end = null;
		DataDimension dimension = descriptor.getTemporalDimension();
		if (dimension != null && dimension instanceof ContinueDimension) {
		    Unit uom = ((ContinueDimension) dimension).getUom();

		    if (uom != null) {
			if (uom.equals(Unit.SECOND) || uom.equals(Unit.MILLI_SECOND)) {
			    long factor = 1;
			    if (uom.equals(Unit.SECOND)) {
				factor = 1000;
			    }
			    ContinueDimension continueDimension = (ContinueDimension) dimension;
			    begin = new Date(continueDimension.getLower().longValue() * factor);
			    end = new Date(continueDimension.getUpper().longValue() * factor);
			}

		    }
		}

		if (begin != null && end != null) {
		    InputStream wml = client.getAralWaterML(stationId, begin, end, isDischarge);
		    XMLDocumentReader reader = new XMLDocumentReader(wml);
		    String posValue = reader.evaluateString("//*:pos[1]");
		    wml.close();
		    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
		    writer.setText("//*:pos[1]", posValue.replace(",", ""));

		    writer.addAttributesNS("//*:value[not(normalize-space()) and not(@*:nil)]", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
			    "xsi:nil", "true");

		    // File tmp = File.createTempFile(getClass().getSimpleName(), ".wml");
		    // tmp.deleteOnExit();
		    // FileOutputStream fos = new FileOutputStream(tmp);
		    // IOUtils.copy(wml, fos);
		    // fos.close();
		    // wml.close();
		    //

		    CollectionType collection;
		    try {
			ByteArrayInputStream str = reader.asStream();
			collection = JAXBWML2.getInstance().unmarshalCollection(str);
			str.close();
		    } catch (Exception e) {

			GSLoggerFactory.getLogger(getClass()).error(e);

			throw GSException.createException(//
				getClass(), //
				ErrorInfo.ERRORTYPE_INTERNAL, //
				ErrorInfo.SEVERITY_ERROR, //
				RIHMI_ARAL_DOWNLOAD_ERROR, //
				e//
			);
		    }

		    List<OMObservationPropertyType> observationMembers = collection.getObservationMember();

		    for (OMObservationPropertyType observationMember : observationMembers) {

			OMObservationType observation = observationMember.getOMObservation();

			ReferenceType observedProperty = observation.getObservedProperty();
			if (observedProperty == null) {
			    observedProperty = new ReferenceType();
			    observedProperty.setHref(parameterName);
			    observedProperty.setTitle(parameterName);
			    observation.setObservedProperty(observedProperty);
			}

			Result anyResult = observation.getResult();

			ResultWrapper wrapper = new ResultWrapper(anyResult);

			MeasurementTimeseriesType measurementTimeSeries = null;
			try {
			    measurementTimeSeries = wrapper.getMeasurementTimeseriesType();
			} catch (Exception e) {
			    e.printStackTrace();
			}
			eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.MeasureType measure = null;
			if (measurementTimeSeries == null) {
			    measure = wrapper.getMeasureType();
			}
			if (measurementTimeSeries != null) {

			    List<Point> points = measurementTimeSeries.getPoint();
			    // List<Point> goodPoints = new ArrayList<MeasurementTimeseriesType.Point>();
			    // for (Point point : points) {
			    //
			    // MeasureTVPType mtvp = point.getMeasurementTVP();
			    // if (mtvp != null) {
			    // eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType time = mtvp.getTime();
			    // if (time != null) {
			    // List<String> values = time.getValue();
			    // if (values != null && !values.isEmpty()) {
			    // String value = values.get(0);
			    // Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(value);
			    // if (optionalDate.isPresent()) {
			    // Date date = optionalDate.get();
			    // if (date != null) {
			    // JAXBElement<MeasureType> jaxb = mtvp.getValue();
			    // if (jaxb != null) {
			    // MeasureType myMeasure = jaxb.getValue();
			    // if (myMeasure != null) {
			    // goodPoints.add(point);
			    // }
			    // }
			    // }
			    // }
			    // }
			    // }
			    // }
			    // }
			    points.sort(new Comparator<Point>() {

				@Override
				public int compare(Point p1, Point p2) {
				    MeasureTVPType mtvp1 = p1.getMeasurementTVP();
				    MeasureTVPType mtvp2 = p2.getMeasurementTVP();
				    eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType time1 = mtvp1.getTime();
				    eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType time2 = mtvp2.getTime();
				    List<String> values1 = time1.getValue();
				    List<String> values2 = time2.getValue();
				    String value1 = values1.get(0);
				    String value2 = values2.get(0);
				    Date date1 = ISO8601DateTimeUtils.parseISO8601ToDate(value1).get();
				    Date date2 = ISO8601DateTimeUtils.parseISO8601ToDate(value2).get();
				    return date2.compareTo(date1);
				}
			    });
			    // points.clear();
			    // points.addAll(goodPoints);
			    ResultWrapper resultWrapper = new ResultWrapper();
			    resultWrapper.setMeasurementTimeseriesType(measurementTimeSeries);
			    observation.setResult(resultWrapper);

			}
		    }

		    File tmp = File.createTempFile(getClass().getSimpleName(), ".wml");
		    tmp.deleteOnExit();
		    FileOutputStream fos = new FileOutputStream(tmp);

		    JAXBWML2.getInstance().marshal(collection, fos);

		    return tmp;

		}

	    }

	    return null;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading ");

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RIHMI_ARAL_DOWNLOAD_ERROR, //
		    e);
	}

    }

}
