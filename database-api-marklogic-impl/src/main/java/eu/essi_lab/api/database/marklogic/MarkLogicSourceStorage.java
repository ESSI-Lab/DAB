package eu.essi_lab.api.database.marklogic;

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

import java.util.List;
import java.util.Optional;

import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class MarkLogicSourceStorage extends MarkLogicReader implements SourceStorage {

    /**
     *
     */
    private static final String MARK_LOGIC_HARVESTING_STARTED_ERROR_ID = "MARK_LOGIC_HARVESTING_STARTED_ERROR";
    private static final String MARK_LOGIC_HARVESTING_ENDED_ERROR_ID = "MARK_LOGIC_HARVESTING_ENDED_ERROR";
    private static final String MARK_LOGIC_HARVESTING_PROPERTIES_READ_ERR_ID = "MARK_LOGIC_HARVESTING_PROPERTIES_READ_ERROR";
    private static final String MARK_LOGIC_HARVESTING_PROPERTIES_STORE_ERROR_ID = "MARK_LOGIC_HARVESTING_PROPERTIES_STORE_ERROR";
    private static final String MARK_LOGIC_GET_REPORT_ERR_ID = "MARK_LOGIC_GET_REPORT_ERROR";
    private static final String MARK_LOGIC_UPDATE_ERRORS_REPORT_ERR_ID = "MARK_LOGIC_UPDATE_ERRORS_REPORT_ERROR";
    private static final String MARK_LOGIC_GET_ERRORS_REPORT_ERR_ID = "MARK_LOGIC_GET_ERRORS_REPORT_ERROR";
    private SourceStorageSetting setting;

    public MarkLogicSourceStorage() {

	this.setting = ConfigurationWrapper.getSourceStorageSettings();
    }

    @Override
    public void harvestingStarted(//
	    GSSource source, //
	    HarvestingStrategy strategy, //
	    boolean recovery, //
	    boolean resumed, //
	    Optional<SchedulerJobStatus> status) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());
	    worker.harvestingStarted(strategy, recovery, resumed, status);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during harvesting initialization of source {}", source);

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
    public void harvestingEnded(//
	    GSSource source, //
	    Optional<HarvestingProperties> properties, //
	    HarvestingStrategy strategy, //
	    Optional<SchedulerJobStatus> status, //
	    Optional<ListRecordsRequest> request) throws GSException {

	MarkLogicDatabase markLogicDB = getDatabase();
	try {

	    SourceStorageWorker worker = markLogicDB.getWorker(source.getUniqueIdentifier());
	    worker.harvestingEnded(this, properties, strategy, status, request);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during harvesting finalization of source {}", source);

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
    public Optional<DatabaseFolder> getDataFolder(String sourceIdentifier, boolean writingFolder) throws GSException {

	DatabaseFolder folder = null;

	MarkLogicWriter writer = (MarkLogicWriter) DatabaseProviderFactory.getDatabaseWriter(getDatabase().getStorageInfo());

	SourceStorageWorker worker = getDatabase().getWorker(sourceIdentifier);

	try {
	    if (writingFolder) {

		folder = writer.findWritingFolder(worker);

	    } else {

		if (worker.existsData1Folder()) {

		    folder = worker.getData1Folder();
		}

		if (worker.existsData2Folder()) {

		    folder = worker.getData2Folder();
		}
	    }

	} catch (RequestException e) {

	    throw GSException.createException(getClass(), "MARK_LOGIC_GET_SOURCE_FOLDER_ERROR", e);

	} catch (GSException e) {

	    throw e;
	}

	return Optional.ofNullable(folder);
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

    @Override
    public SourceStorageSetting getSetting() {

	return setting;
    }

    @Override
    public void configure(SourceStorageSetting setting) {

	this.setting = setting;
    }

    @Override
    public String getType() {

	return "MarkLogicSourceStorage";
    }
}
