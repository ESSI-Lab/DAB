package eu.essi_lab.workflow.processor.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasureTVPType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType.Point;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationPropertyType;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;
import eu.essi_lab.jaxb.wml._2_0.om__2.Result;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.wml._2.JAXBWML2;
import eu.essi_lab.wml._2.ResultWrapper;

public class WML20_Subset_Processor extends AbstractTimeSubsetProcessor {

    public File subset(File inFile, Date timeBegin, LimitType beginType, Date timeEnd, LimitType endType) throws Exception {

	CollectionType collection = JAXBWML2.getInstance().unmarshalCollection(inFile);

	List<OMObservationPropertyType> observationMembers = collection.getObservationMember();

	TreeSet<Date> map = new TreeSet<>();

	for (OMObservationPropertyType observationMember : observationMembers) {

	    OMObservationType observation = observationMember.getOMObservation();

	    Result anyResult = observation.getResult();

	    ResultWrapper wrapper = new ResultWrapper(anyResult);

	    MeasurementTimeseriesType measurementTimeSeries = null;
	    try {
		measurementTimeSeries = wrapper.getMeasurementTimeseriesType();
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    if (measurementTimeSeries != null) {
		List<Point> points = measurementTimeSeries.getPoint();
		for (Point point : points) {

		    MeasureTVPType mtvp = point.getMeasurementTVP();
		    if (mtvp != null) {

			eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType time = mtvp.getTime();
			if (time != null) {
			    List<String> values = time.getValue();
			    if (values != null && !values.isEmpty()) {
				String value = values.get(0);
				Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(value);
				if (optionalDate.isPresent()) {
				    Date date = optionalDate.get();
				    if (date != null) {
					map.add(date);
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	switch (beginType) {
	case MINIMUM:
	    timeBegin = map.first();
	    break;
	case MAXIMUM:
	    timeBegin = map.last();
	    break;
	case ABSOLUTE:
	    break;
	default:
	    break;
	}
	switch (endType) {
	case MINIMUM:
	    timeEnd = map.first();
	    break;
	case MAXIMUM:
	    timeEnd = map.last();
	    break;
	case ABSOLUTE:
	    break;
	default:
	    break;
	}

	for (OMObservationPropertyType observationMember : observationMembers) {

	    OMObservationType observation = observationMember.getOMObservation();

	    Result anyResult = observation.getResult();

	    ResultWrapper wrapper = new ResultWrapper(anyResult);

	    MeasurementTimeseriesType measurementTimeSeries = null;
	    try {
		measurementTimeSeries = wrapper.getMeasurementTimeseriesType();
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    if (measurementTimeSeries != null) {
		List<Point> points = measurementTimeSeries.getPoint();
		List<Point> pointsToRemove = new ArrayList<>();
		for (Point point : points) {

		    MeasureTVPType mtvp = point.getMeasurementTVP();
		    if (mtvp == null) {
			pointsToRemove.add(point);
		    } else {

			eu.essi_lab.jaxb.wml._2_0.gml._3_2_1.TimePositionType time = mtvp.getTime();
			if (time == null) {
			    pointsToRemove.add(point);
			} else {
			    List<String> values = time.getValue();
			    if (values == null || values.isEmpty()) {
				pointsToRemove.add(point);
			    } else {
				String value = values.get(0);
				Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(value);
				if (!optionalDate.isPresent()) {
				    pointsToRemove.add(point);
				} else {
				    Date date = optionalDate.get();
				    if (date == null || date.before(timeBegin) || date.after(timeEnd)) {
					pointsToRemove.add(point);
				    } else {
					// O.K.
				    }
				}
			    }

			}

		    }
		}
		points.removeAll(pointsToRemove);
	    }
	    wrapper.setMeasurementTimeseriesType(measurementTimeSeries);
	    observation.setResult(wrapper);
	}

	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	JAXBWML2.getInstance().marshal(collection, tmpFile);
	return tmpFile;

    }

}
