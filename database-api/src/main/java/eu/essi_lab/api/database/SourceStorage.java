package eu.essi_lab.api.database;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This component is notified when an the harvesting of a {@link GSSource}
 * begins and ends in order to prepare the underlying storage
 *
 * @author Fabrizio
 */
public abstract class SourceStorage implements DatabaseProvider, Configurable<SourceStorageSetting> {

    private SourceStorageSetting setting;

    /**
     * 
     */
    public SourceStorage() {
    }

    /**
     * Notifies that the harvesting of <code>source</code> is started
     * 
     * @param source
     * @param strategy
     * @param recovery
     * @param resume
     * @param status
     * @throws GSException
     */
    public void harvestingStarted(//
	    GSSource source, //
	    HarvestingStrategy strategy, //
	    boolean recovery, //
	    boolean resumed, //
	    Optional<SchedulerJobStatus> status, //
	    Optional<ListRecordsRequest> request) throws GSException {

	Database database = getDatabase();
	try {

	    SourceStorageWorker worker = database.getWorker(source.getUniqueIdentifier());
	    worker.harvestingStarted(strategy, this, recovery, resumed, status, request);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during harvesting initialization of source {}", source);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SourceStorageHarvestingStartedError", //
		    e);
	}
    }

    /**
     * Notifies that the harvesting of <code>source</code> is started
     * 
     * @param source
     * @param strategy
     * @param recovery
     * @param resume
     * @throws GSException
     */
    public void harvestingStarted(//
	    GSSource source, //
	    HarvestingStrategy strategy, //
	    boolean recovery, //
	    boolean resume) throws GSException {

	harvestingStarted(source, strategy, recovery, resume, Optional.empty(), Optional.empty());
    }

    /**
     * Notifies that the harvesting of <code>source</code> is ended
     * 
     * @param source
     * @param properties
     * @param strategy
     * @param status
     * @param request
     * @throws GSException
     */
    public void harvestingEnded(//
	    GSSource source, //
	    Optional<HarvestingProperties> properties, //
	    HarvestingStrategy strategy, //
	    Optional<SchedulerJobStatus> status) throws GSException {

	Database database = getDatabase();
	try {

	    SourceStorageWorker worker = database.getWorker(source.getUniqueIdentifier());
	    worker.harvestingEnded(properties, strategy, status);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during harvesting finalization of source {}", source);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SourceStorageHarvestingEndedError", //
		    e);
	}
    }

    /**
     * Notifies that the harvesting of <code>source</code> is ended
     *
     * @param source
     * @param properties
     * @param strategy
     * @throws GSException
     */
    public void harvestingEnded(//
	    GSSource source, //
	    Optional<HarvestingProperties> properties, //
	    HarvestingStrategy strategy) throws GSException {

	harvestingEnded(source, properties, strategy, Optional.empty());
    }

    /**
     * Notifies that the harvesting of <code>source</code> is ended
     *
     * @param source
     * @param properties
     * @param strategy
     * @throws GSException
     */
    public void harvestingEnded(GSSource source, HarvestingStrategy strategy) throws GSException {

	harvestingEnded(source, Optional.empty(), strategy, Optional.empty());
    }

    /**
     * Retrieve a properties file which provide information about the harvesting of
     * the supplied <code>source</code>
     *
     * @param source the harvested source
     * @return a {@link HarvestingProperties} with information about the harvesting
     *         of the supplied <code>source</code>
     */
    public HarvestingProperties retrieveHarvestingProperties(GSSource source) throws GSException {

	Database database = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = database.getWorker(sid);
	    return worker.getHarvestingProperties();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SourceStorageRetrieveHarvestingPropertiesError", //
		    e);
	}
    }

    /**
     * @param source
     * @param properties
     * @throws GSException
     */
    public void storeHarvestingProperties(GSSource source, HarvestingProperties properties) throws GSException {

	Database database = getDatabase();
	try {

	    SourceStorageWorker worker = database.getWorker(source.getUniqueIdentifier());
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
		    "SourceStorageStoreHarvestingPropertiesError", //
		    e);
	}
    }

    /**
     * @return
     */
    public List<String> getStorageReport(GSSource source) throws GSException {

	Database database = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = database.getWorker(sid);
	    return worker.getStorageReport();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SourceStorageGetStorageReportError", //
		    e);
	}
    }

    /**
     * Returns the data folder for the source with the given
     * <code>sourceIdentifier</code>.
     * 
     * @param sourceIdentifier it <code>true</code>, returns the
     * @return
     * @throws GSException
     */
    public Optional<DatabaseFolder> getDataFolder(String sourceIdentifier, boolean writingFolder) throws GSException {

	DatabaseFolder folder = null;

	SourceStorageWorker worker = getDatabase().getWorker(sourceIdentifier);

	try {
	    if (writingFolder) {

		folder = getDatabase().findWritingFolder(worker);

	    } else {

		if (worker.existsData1Folder()) {

		    folder = worker.getData1Folder();

		} else if (worker.existsData2Folder()) {

		    folder = worker.getData2Folder();
		}
	    }

	} catch (GSException e) {

	    throw e;
	}

	return Optional.ofNullable(folder);
    }

    /**
     * @param source
     * @param report
     * @throws GSException
     */
    public void updateErrorsAndWarnReport(GSSource source, String report) throws GSException {

	Database database = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = database.getWorker(sid);

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
		    "SourceStorageUpdateErrorsAndWarnReportError", //
		    e);
	}
    }

    /**
     * @param source
     * @return
     * @throws GSException
     */
    public List<String> retrieveErrorsReport(GSSource source) throws GSException {

	Database database = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = database.getWorker(sid);
	    return worker.retrieveErrorsReport();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SourceStorageRetrieveErrorsReportError", //
		    e);
	}
    }

    /**
     * @param source
     * @return
     * @throws GSException
     */
    public List<String> retrieveWarnReport(GSSource source) throws GSException {

	Database database = getDatabase();
	String sid = source.getUniqueIdentifier();

	try {

	    SourceStorageWorker worker = database.getWorker(sid);
	    return worker.retrieveWarnReport();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "SourceStorageRetrieveWarnReportError", //
		    e);
	}

    }

    @Override
    public void configure(SourceStorageSetting setting) {

	this.setting = setting;
    }

    @Override
    public SourceStorageSetting getSetting() {

	return setting;
    }
}
