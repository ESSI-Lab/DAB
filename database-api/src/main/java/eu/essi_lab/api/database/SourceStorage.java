package eu.essi_lab.api.database;

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

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This component is notified when an the harvesting of a {@link GSSource} begins and ends in order to prepare the
 * underlying storage. The behaviour of this component is completely implementation dependent
 *
 * @author Fabrizio
 */
public interface SourceStorage extends DatabaseProvider, Configurable<SourceStorageSetting> {

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
    void harvestingStarted(//
	    GSSource source, //
	    HarvestingStrategy strategy, //
	    boolean recovery, //
	    boolean resume, //
	    Optional<SchedulerJobStatus> status) throws GSException;

    /**
     * Notifies that the harvesting of <code>source</code> is started
     * 
     * @param source
     * @param strategy
     * @param recovery
     * @param resume
     * @throws GSException
     */
    default void harvestingStarted(//
	    GSSource source, //
	    HarvestingStrategy strategy, //
	    boolean recovery, //
	    boolean resume) throws GSException {

	harvestingStarted(source, strategy, recovery, resume, Optional.empty());
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
    void harvestingEnded(//
	    GSSource source, //
	    Optional<HarvestingProperties> properties, //
	    HarvestingStrategy strategy, //
	    Optional<SchedulerJobStatus> status, //
	    Optional<ListRecordsRequest> request) throws GSException;

    /**
     * Notifies that the harvesting of <code>source</code> is ended
     *
     * @param source
     * @param properties
     * @param strategy
     * @throws GSException
     */
    default void harvestingEnded(//
	    GSSource source, //
	    Optional<HarvestingProperties> properties, //
	    HarvestingStrategy strategy) throws GSException {

	harvestingEnded(source, properties, strategy, Optional.empty(), Optional.empty());
    }

    /**
     * Notifies that the harvesting of <code>source</code> is ended
     *
     * @param source
     * @param properties
     * @param strategy
     * @throws GSException
     */
    default void harvestingEnded(GSSource source, HarvestingStrategy strategy) throws GSException {

	harvestingEnded(source, Optional.empty(), strategy, Optional.empty(), Optional.empty());
    }

    /**
     * Retrieve a properties file which provide information about the harvesting of the supplied <code>source</code>
     *
     * @param source the harvested source
     * @return a {@link HarvestingProperties} with information about the harvesting of the supplied
     *         <code>source</code>
     */
    HarvestingProperties retrieveHarvestingProperties(GSSource source) throws GSException;

    /**
     * @param source
     * @param properties
     * @throws GSException
     */
    void storeHarvestingProperties(GSSource source, HarvestingProperties properties) throws GSException;

    /**
     * @return
     */
    List<String> getStorageReport(GSSource source) throws GSException;

    /**
     * @param sourceIdentifier
     * @return
     * @throws GSException
     */
    Optional<DatabaseFolder> getDataFolder(String sourceIdentifier, boolean writingFolder) throws GSException;

    /**
     * @param source
     * @param report
     * @throws GSException
     */
    void updateErrorsAndWarnReport(GSSource source, String report) throws GSException;

    /**
     * @param source
     * @return
     * @throws GSException
     */
    public List<String> retrieveErrorsReport(GSSource source) throws GSException;

    /**
     * @param source
     * @return
     * @throws GSException
     */
    public List<String> retrieveWarnReport(GSSource source) throws GSException;

    @Override
    public SourceStorageSetting getSetting();
}
