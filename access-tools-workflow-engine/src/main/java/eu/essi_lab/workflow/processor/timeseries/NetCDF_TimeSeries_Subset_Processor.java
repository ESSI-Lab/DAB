package eu.essi_lab.workflow.processor.timeseries;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import ucar.ma2.Array;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;

public class NetCDF_TimeSeries_Subset_Processor extends AbstractTimeSubsetProcessor {

    @Override
    public File subset(File inFile, Date timeBegin, LimitType beginType, Date timeEnd, LimitType endType) throws Exception {

	File outFile = File.createTempFile(getClass().getSimpleName(), ".nc");
	outFile.deleteOnExit();

	NetcdfDataset dataset = NetcdfDataset.openDataset(inFile.getAbsolutePath());

	Attribute typeAttr = dataset.findAttribute("@featureType");
	if (typeAttr == null) {
	    throw getGSException("Not a time series");
	}
	String type = typeAttr.getStringValue();
	if (type == null || !type.equals("timeSeries")) {
	    throw getGSException("Not a time series");
	}

	CoordinateAxis1DTime timeAxis = null;
	List<CoordinateAxis> axes = dataset.getCoordinateAxes();
	Variable timeVariable = null;
	Dimension timeDimension = null;
	CalendarDateRange calendarRange = null;
	for (CoordinateAxis axe : axes) {
	    AxisType axisType = axe.getAxisType();
	    if (axisType != null && axisType.equals(AxisType.Time)) {
		timeVariable = axe.getOriginalVariable();
		timeDimension = timeVariable.getDimension(0);
		timeAxis = CoordinateAxis1DTime.factory(dataset, (VariableDS) timeVariable, null);
		calendarRange = timeAxis.getCalendarDateRange();
	    }
	}

	if (timeVariable != null && calendarRange != null) {
	    Date actualStart = calendarRange.getStart().toDate();
	    Date actualEnd = calendarRange.getEnd().toDate();
	    boolean timeAxisInverted = actualStart.after(actualEnd);
	    if (timeAxisInverted) {
		// switched time axis
		Date tmp = actualStart;
		actualStart = actualEnd;
		actualEnd = tmp;
	    }

	    Integer startIndex = null;
	    Integer endIndex = null;
	    int timeSize;

	    if (beginType.equals(LimitType.ABSOLUTE) && endType.equals(LimitType.ABSOLUTE)
		    && (timeEnd.before(actualStart) || timeBegin.after(actualEnd))) {
		// EMPTY
		timeSize = 0;
		startIndex = 0;
		endIndex = 0;
	    } else {

		Date tmpDate;
		switch (beginType) {
		case ABSOLUTE:
		case CONTAINS:
		    tmpDate = timeAxisInverted ? timeEnd : timeBegin;
		    startIndex = findTimeIndexFromCalendarDate(timeAxis, CalendarDate.of(tmpDate), timeAxisInverted);
		    break;
		case MINIMUM:
		    startIndex = timeAxisInverted ? (int) timeAxis.getSize() - 1 : 0;
		    break;
		case MAXIMUM:
		    startIndex = timeAxisInverted ? 0 : (int) timeAxis.getSize() - 1;
		    break;

		default:
		    break;
		}
		switch (endType) {
		case ABSOLUTE:
		case CONTAINS:
		    tmpDate = timeAxisInverted ? timeBegin : timeEnd;
		    endIndex = findTimeIndexFromCalendarDate(timeAxis, CalendarDate.of(tmpDate), timeAxisInverted);
		    break;
		case MINIMUM:
		    endIndex = timeAxisInverted ? (int) timeAxis.getSize() - 1 : 0;
		    break;
		case MAXIMUM:
		    endIndex = timeAxisInverted ? 0 : (int) timeAxis.getSize() - 1;
		    break;

		default:
		    break;
		}

		timeSize = endIndex - startIndex + 1;
	    }

	    NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, outFile.getAbsolutePath(), null);
	    List<Attribute> globalAttributes = dataset.getGlobalAttributes();
	    for (Attribute globalAttribute : globalAttributes) {
		writer.addGroupAttribute(null, globalAttribute);
	    }
	    List<Dimension> dimensions = dataset.getDimensions();
	    for (Dimension dimension : dimensions) {
		int length = dimension.getLength();
		boolean unlimited = dimension.isUnlimited();
		if (dimension.getShortName().equals(timeDimension.getShortName())) {
		    length = timeSize;
		    if (length == 0) {
			unlimited = true;
		    }
		}
		writer.addDimension(null, dimension.getShortName(), length, dimension.isShared(), unlimited, dimension.isVariableLength());
	    }
	    List<Variable> variables = dataset.getVariables();
	    for (Variable variable : variables) {
		Variable newVar = writer.addVariable(null, variable.getShortName(), variable.getDataType(), variable.getDimensionsString());
		List<Attribute> attributes = variable.getAttributes();
		for (Attribute attribute : attributes) {
		    writer.addVariableAttribute(newVar, attribute);
		}
	    }
	    writer.create();
	    for (Variable variable : variables) {
		Variable newVar = writer.findVariable(variable.getFullName());
		Array values = null;
		boolean skipWrite = false;
		if (timeSize == 0) {
		    List<Dimension> variableDimensions = variable.getDimensions();
		    for (Dimension variableDimension : variableDimensions) {
			if (variableDimension.getShortName().equals(timeDimension.getShortName())) {
			    skipWrite = true;
			}
		    }
		}

		List<Range> ranges = variable.getRanges();
		List<Range> newRanges = new ArrayList<>();
		for (Range range : ranges) {
		    Range newRange = new Range(range);
		    if (range.getName().equals(timeDimension.getShortName())) {
			newRange = new Range(range.getName(), startIndex, endIndex);
		    }
		    newRanges.add(newRange);
		}

		if (!skipWrite) {
		    values = variable.read(newRanges);
		    writer.write(newVar, values);
		}
	    }

	    writer.close();

	    dataset.close();

	    return outFile;

	} else {
	    dataset.close();

	    FileInputStream fis = new FileInputStream(inFile);
	    FileOutputStream fos = new FileOutputStream(outFile);
	    IOUtils.copy(fis, fos);
	    fis.close();
	    fos.close();

	    return outFile;
	}

    }
    public int findTimeIndexFromCalendarDate(CoordinateAxis1DTime timeAxis, CalendarDate d, boolean timeAxisInverted) {
	List<CalendarDate> cdates = timeAxis.getCalendarDates(); // LOOK linear search, switch to binary
	if (timeAxisInverted) {
	    int index = cdates.size() - 1;
	    while (index > 0) {
		if (d.compareTo(cdates.get(index)) < 0)
		    break;
		index--;
	    }
	    return Math.min(cdates.size() - 1, index + 1);
	} else {
	    int index = 0;
	    while (index < cdates.size()) {
		if (d.compareTo(cdates.get(index)) <= 0)
		    break;
		index++;
	    }
	    return Math.min(cdates.size() - 1, Math.max(0, index));
	}
    }

}
