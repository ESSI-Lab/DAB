package eu.essi_lab.api.database.marklogic;

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

import java.util.List;

import eu.essi_lab.api.database.HarvestingStrategy;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class MarkLogicSourceStorage extends MarkLogicReader implements SourceStorage {
    private static final long serialVersionUID = 631794288222442782L;
    private static final String MARK_LOGIC_HARVESTING_STARTED_ERROR_ID = "MARK_LOGIC_HARVESTING_STARTED_ERROR";
    private static final String MARK_LOGIC_HARVESTING_ENDED_ERROR_ID = "MARK_LOGIC_HARVESTING_ENDED_ERROR";
    private static final String MARK_LOGIC_HARVESTING_PROPERTIES_READ_ERR_ID = "MARK_LOGIC_HARVESTING_PROPERTIES_READ_ERROR";
    private static final String MARK_LOGIC_HARVESTING_PROPERTIES_STORE_ERROR_ID = "MARK_LOGIC_HARVESTING_PROPERTIES_STORE_ERROR";
    private static final String MARK_LOGIC_GET_REPORT_ERR_ID = "MARK_LOGIC_GET_REPORT_ERROR";
    private static final String MARK_LOGIC_UPDATE_ERRORS_REPORT_ERR_ID = "MARK_LOGIC_UPDATE_ERRORS_REPORT_ERROR";
    private static final String MARK_LOGIC_GET_ERRORS_REPORT_ERR_ID = "MARK_LOGIC_GET_ERRORS_REPORT_ERROR";

    public MarkLogicSourceStorage() {
	// Nothing to init
    }

    @Override
    public void harvestingStarted(GSSource source, HarvestingStrategy strategy, boolean recovery) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());
	    worker.harvestingStarted(strategy, recovery);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't start harvesting  of {} {}", source.getLabel(), source.getUniqueIdentifier(),
		    e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_HARVESTING_STARTED_ERROR_ID, //
		    e);
	}
    }

    @Override
    public void storeHarvestingProperties(GSSource source, HarvestingProperties properties) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());
	    worker.storeHarvestingProperties(properties);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't store harvesting properties of {} {}", source.getLabel(),
		    source.getUniqueIdentifier(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_HARVESTING_PROPERTIES_STORE_ERROR_ID, //
		    e);
	}
    }

    @Override
    public void harvestingEnded(GSSource source, HarvestingStrategy strategy) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());
	    worker.harvestingEnded(this, strategy);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_HARVESTING_ENDED_ERROR_ID, //
		    e);
	}
    }

    @Override
    public HarvestingProperties retrieveHarvestingProperties(GSSource source) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(sid);
	    return worker.getSourceHarvestingProperties();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_HARVESTING_PROPERTIES_READ_ERR_ID, //
		    e);
	}
    }

    @Override
    public List<String> getStorageReport(GSSource source) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(sid);
	    return worker.getStorageReport();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_REPORT_ERR_ID, //
		    e);
	}
    }

    @Override
    public void updateErrorsAndWarnReport(GSSource source, String report) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(sid);

	    worker.updateErrorsAndWarnHarvestingReport(report, true);
	    worker.updateErrorsAndWarnHarvestingReport(report, false);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_UPDATE_ERRORS_REPORT_ERR_ID, //
		    e);
	}
    }

    @Override
    public List<String> retrieveErrorsReport(GSSource source) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(sid);
	    return worker.retrieveErrorsReport();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_ERRORS_REPORT_ERR_ID, //
		    e);
	}
    }

    @Override
    public List<String> retrieveWarnReport(GSSource source) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(sid);
	    return worker.retrieveWarnReport();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_ERRORS_REPORT_ERR_ID, //
		    e);
	}

    }

}
