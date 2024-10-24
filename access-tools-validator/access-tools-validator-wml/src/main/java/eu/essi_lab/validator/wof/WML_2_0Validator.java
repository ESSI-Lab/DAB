package eu.essi_lab.validator.wof;

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

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasureTVPType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.jaxb.wml._2_0.TVPDefaultMetadataPropertyType;
import eu.essi_lab.jaxb.wml._2_0.TVPMetadataType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractFeatureType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractGeometryType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.AbstractTimeObjectType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.DirectPositionType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.FeaturePropertyType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.MeasureType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.PointType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimeInstantType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePeriodType;
import eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.jaxb.wml._2_0.om__2.TimeObjectPropertyType;
import eu.essi_lab.jaxb.wml._2_0.sams._2_0.SFSpatialSamplingFeatureType;
import eu.essi_lab.jaxb.wml._2_0.sams._2_0.ShapeType;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;

public class WML_2_0Validator extends DataValidatorImpl {

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    @Override
    public DataFormat getFormat() {
	return DataFormat.WATERML_2_0();
    }

    @Override
    public DataType getType() {
	return DataType.TIME_SERIES;
    }

    @Override
    public ValidationMessage checkSupportForDescriptor(DataDescriptor descriptor) {
	ValidationMessage ret = super.checkSupportForDescriptor(descriptor);
	if (ret.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
	    return ret;
	}

	if (!descriptor.getDataType().equals(DataType.TIME_SERIES)) {
	    return unsupportedDescriptor("Not a time series descriptor - unable to validate");
	}

	ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    @Override
    public DataDescriptor readDataAttributes(DataObject dataObject) {

	DataDescriptor ret = new DataDescriptor();

	CollectionType collection = null;
	try {
	    FileInputStream stream = new FileInputStream(dataObject.getFile());
	    collection = JAXBWML2.getInstance().unmarshalCollection(stream);
	    stream.close();
	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}

	ret.setDataFormat(DataFormat.WATERML_2_0());
	ret.setDataType(DataType.TIME_SERIES);

	Long timeBegin = null;
	Long timeEnd = null;
	Long timeResolution = null;
	boolean invalidResolution = false;
	Long previousTime = null;

	HashSet<Long> times = new HashSet<>();

	List<OMObservationPropertyType> observationMembers = collection.getObservationMember();
	for (OMObservationPropertyType observationMember : observationMembers) {
	    OMObservationType observation = observationMember.getOMObservation();
	    TimeObjectPropertyType phenomenonTime = observation.getPhenomenonTime();
	    if (phenomenonTime != null) {
		JAXBElement<? extends AbstractTimeObjectType> abstractTimeObject = phenomenonTime.getAbstractTimeObject();
		if (abstractTimeObject != null) {
		    String timeName = abstractTimeObject.getName().getLocalPart();
		    if (timeName.equals("AbstractTimeObject")) {
			throw new IllegalArgumentException("Abstract time element present instead of realization");
		    }
		}
	    }
	    FeaturePropertyType foi = observation.getFeatureOfInterest();
	    JAXBElement<? extends AbstractFeatureType> abstractFeature = foi.getAbstractFeature();
	    if (abstractFeature != null) {
		AbstractFeatureType feature = abstractFeature.getValue();
		if (feature instanceof SFSpatialSamplingFeatureType) {
		    SFSpatialSamplingFeatureType sfssf = (SFSpatialSamplingFeatureType) feature;
		    ShapeType shape = sfssf.getShape();
		    JAXBElement<? extends AbstractGeometryType> jaxbAbstractGeometry = shape.getAbstractGeometry();
		    String geometryName = jaxbAbstractGeometry.getName().getLocalPart();
		    if (geometryName.equals("AbstractGeometry")) {
			throw new IllegalArgumentException("Abstract geometry element present instead of realization");
		    }
		    AbstractGeometryType abstractGeometry = jaxbAbstractGeometry.getValue();
		    if (abstractGeometry instanceof PointType) {
			PointType point = (PointType) abstractGeometry;
			String srs = point.getSrsName();
			DirectPositionType pos = point.getPos();
			if (srs == null) {
			    srs = pos.getSrsName();
			}
			List<Double> values = pos.getValue();
			if (values.size() == 2) {
			    CRS crs;

			    if (srs != null) {
				crs = CRS.fromIdentifier(srs);
			    } else {
				crs = CRS.EPSG_4326();
			    }
			    ret.setCRS(crs);

			    DataDimension spatial1 = new ContinueDimension(crs.getFirstAxisName());
			    spatial1.getContinueDimension().setLower(values.get(0));
			    spatial1.getContinueDimension().setUpper(values.get(0));
			    spatial1.getContinueDimension().setSize(1l);

			    DataDimension spatial2 = new ContinueDimension(crs.getSecondAxisName());
			    spatial2.getContinueDimension().setLower(values.get(1));
			    spatial2.getContinueDimension().setUpper(values.get(1));
			    spatial2.getContinueDimension().setSize(1l);

			    ret.setSpatialDimensions(Arrays.asList(spatial1, spatial2));

			}
		    }
		} else {
		    throw new IllegalArgumentException("Unexpected abstract feature");
		}
	    } else {
		// by default if no feature is found, is set a CRS of 4326 without spatial axis
		ret.setCRS(CRS.EPSG_4326());
	    }

	    Result anyResult = observation.getResult();

	    ResultWrapper wrapper = new ResultWrapper(anyResult);

	    MeasurementTimeseriesType measurements = null;
	    try {
		measurements = wrapper.getMeasurementTimeseriesType();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    MeasureType measure = null;
	    if (measurements == null) {
		measure = wrapper.getMeasureType();
	    }
	    if (measurements != null) {

		List<TVPDefaultMetadataPropertyType> metadatas = measurements.getDefaultPointMetadata();
		if (metadatas != null) {
		    for (TVPDefaultMetadataPropertyType metadata : metadatas) {
			JAXBElement<? extends TVPMetadataType> dtm = metadata.getDefaultTVPMetadata();
			if (dtm != null) {
			    String metadataName = dtm.getName().getLocalPart();
			    if (metadataName.equals("TVPMeasurementMetadata")) {
				throw new IllegalArgumentException("Abstract TVPMeasurementMetadata element present");
			    }
			}
		    }
		}

		List<Point> points = measurements.getPoint();
		for (Point point : points) {

		    MeasureTVPType tvp = point.getMeasurementTVP();
		    TimePositionType time = tvp.getTime();
		    if (time != null) {

			List<String> values = time.getValue();
			if (values != null && !values.isEmpty()) {
			    String value = values.get(0);
			    Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(value);
			    if (optionalDate.isPresent()) {
				Date date = optionalDate.get();
				Long milliseconds = date.getTime();
				if (milliseconds != null) {
				    times.add(milliseconds);
				    if (previousTime != null) {
					long tmpResolution = Math.abs(milliseconds - previousTime);
					if (timeResolution == null) {
					    timeResolution = tmpResolution;
					} else {
					    if (timeResolution != tmpResolution) {
						invalidResolution = true;
					    }
					}
				    }
				    previousTime = milliseconds;
				    if (timeBegin == null || timeBegin > milliseconds) {
					timeBegin = milliseconds;
				    }
				    if (timeEnd == null || timeEnd < milliseconds) {
					timeEnd = milliseconds;
				    }
				}
			    }
			}
		    }
		}
	    } else if (measure != null) {
		// String uom = measure.getUom();
		// double value = measure.getValue();
		// in this case a single time instant must be present

		TimeObjectPropertyType time = observation.getPhenomenonTime();
		if (time != null) {
		    JAXBElement<? extends AbstractTimeObjectType> abstractTime = time.getAbstractTimeObject();
		    if (abstractTime != null) {
			AbstractTimeObjectType timeValue = abstractTime.getValue();
			if (timeValue instanceof TimeInstantType) {
			    TimeInstantType instant = (TimeInstantType) timeValue;
			    String timeString = instant.getTimePosition().getValue().get(0);
			    Optional<Date> optionalMilliseconds = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
			    if (optionalMilliseconds.isPresent()) {
				long milliseconds = optionalMilliseconds.get().getTime();
				times.add(milliseconds);
				if (timeBegin == null || timeBegin > milliseconds) {
				    timeBegin = milliseconds;
				}
				if (timeEnd == null || timeEnd < milliseconds) {
				    timeEnd = milliseconds;
				}
			    }
			} else if (timeValue instanceof TimePeriodType) {
			    TimePeriodType tpt = (TimePeriodType) timeValue;
			    TimePositionType end = tpt.getEndPosition();
			    String timeString = end.getValue().get(0);
			    Optional<Date> optionalMilliseconds = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
			    if (optionalMilliseconds.isPresent()) {
				long milliseconds = optionalMilliseconds.get().getTime();
				times.add(milliseconds);
				if (timeBegin == null || timeBegin > milliseconds) {
				    timeBegin = milliseconds;
				}
				if (timeEnd == null || timeEnd < milliseconds) {
				    timeEnd = milliseconds;
				}
			    }

			}
		    }
		}

	    } else {
		throw new IllegalArgumentException("Expected MeasurementTimeseriesType or MeasureType");
	    }

	}

	if (timeBegin != null && timeEnd != null) {
	    ret.setTemporalDimension(new Date(timeBegin), new Date(timeEnd));
	    if (!invalidResolution) {
		ret.getTemporalDimension().getContinueDimension().setResolution(timeResolution);
	    }
	    ret.getTemporalDimension().getContinueDimension().setSize((long) times.size());
	}

	return ret;

    }

}
