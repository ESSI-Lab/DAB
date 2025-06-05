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
import java.util.Date;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;
import eu.essi_lab.workflow.processor.DataProcessor;
import eu.essi_lab.workflow.processor.TargetHandler;

public abstract class AbstractTimeSubsetProcessor extends DataProcessor {

    private static final String ABSTRACT_TIME_SUBSET_PROCESSOR_ERROR = "ABSTRACT_TIME_SUBSET_PROCESSOR_ERROR";

    public abstract File subset(File input, Date timeBegin, LimitType beginType, Date timeEnd, LimitType endType) throws Exception;

    @Override
    public DataObject process(GSResource resource,DataObject dataObject, TargetHandler handler) throws Exception {

	DataDimension temporalDimension = handler.getTargetTemporalDimension();
	Long begin = null;
	Long end = null;
	LimitType beginType = LimitType.ABSOLUTE;
	LimitType endType = LimitType.ABSOLUTE;
	if (temporalDimension == null) {
	    begin = 0l;
	    end = 0l;
	} else {
	    if (temporalDimension instanceof ContinueDimension) {
		ContinueDimension continueDimension = temporalDimension.getContinueDimension();
		begin = continueDimension.getLower().longValue();
		beginType = continueDimension.getLowerType();
		end = continueDimension.getUpper().longValue();
		endType = continueDimension.getUpperType();
		Unit uom = continueDimension.getUom();
		if (uom.equals(Unit.SECOND)) {
		    uom = Unit.MILLI_SECOND;
		    begin = begin * 1000;
		    end = end * 1000;
		}
		if (uom.equals(Unit.MILLI_SECOND)) {
		    Datum datum = continueDimension.getDatum();
		    if (datum.equals(Datum.UNIX_EPOCH_TIME())) {
			// o.k.
		    } else {
			throw new RuntimeException("Unsupported time unit datum: " + datum.getIdentifier());
		    }
		} else {
		    throw new RuntimeException("Unsupported time unit type: " + uom.getIdentifier());
		}
	    } else {
		FiniteDimension discreteDimension = temporalDimension.getFiniteDimension();
		throw new RuntimeException("Unsupported dimension type: " + discreteDimension.getClass());
	    }
	}
	DataObject ret = new DataObject();
	Date beginDate = null;
	if (begin != null) {
	    beginDate = new Date(begin);
	}
	Date endDate = null;
	if (end != null) {
	    endDate = new Date(end);
	}
	File output = subset(dataObject.getFile(), beginDate, beginType, endDate, endType);
	ret.setFile(output);
	return ret;
    }

    public GSException getGSException(String message) {
	return GSException.createException(//
		getClass(),//
		message,//
		ErrorInfo.ERRORTYPE_INTERNAL,//
		ErrorInfo.SEVERITY_ERROR,//
		ABSTRACT_TIME_SUBSET_PROCESSOR_ERROR);


    }

}
