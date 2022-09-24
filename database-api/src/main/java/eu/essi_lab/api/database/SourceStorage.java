package eu.essi_lab.api.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.exceptions.GSException;

/**
 * This component is notified when an the harvesting of a {@link GSSource} begins and ends in order to prepare the
 * underlying storage. The
 * behavior of this component is completely implementation dependent
 *
 * @author Fabrizio
 */
public interface SourceStorage extends DatabaseConsumer, Configurable<SourceStorageSetting> {

    /**
     * Notifies that the harvesting of <code>source</code> is started
     *
     * @param source the harvested source
     * @param strategy the strategy used to harvest the source
     * @param status
     */
    void harvestingStarted(GSSource source, HarvestingStrategy strategy, boolean recovery, Optional<SchedulerJobStatus> status) throws GSException;

    /**
     * Notifies that the harvesting of <code>source</code> is started
     *
     * @param source the harvested source
     * @param strategy the strategy used to harvest the source
     * @param status
     */
    void harvestingEnded(GSSource source, HarvestingStrategy strategy,  Optional<SchedulerJobStatus> status) throws GSException;

    /**
     * Notifies that the harvesting of <code>source</code> is started
     *
     * @param source the harvested source
     * @param strategy the strategy used to harvest the source
     */
    void harvestingStarted(GSSource source, HarvestingStrategy strategy, boolean recovery) throws GSException;

    /**
     * Notifies that the harvesting of <code>source</code> is started
     *
     * @param source the harvested source
     * @param strategy the strategy used to harvest the source
     */
    void harvestingEnded(GSSource source, HarvestingStrategy strategy) throws GSException;

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
