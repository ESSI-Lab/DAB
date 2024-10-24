/**
 * 
 */
package eu.essi_lab.api.database.elasticsearch;

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

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class ElasticsearchSourceStorage implements SourceStorage {

    @Override
    public boolean supports(StorageInfo dbUri) {

	return false;
    }

    @Override
    public void setDatabase(Database dataBase) {

    }

    @Override
    public Database getDatabase() {

	return null;
    }

    @Override
    public void configure(SourceStorageSetting setting) {

    }

    @Override
    public String getType() {

	return null;
    }

    @Override
    public void harvestingStarted(//
	    GSSource source, //
	    HarvestingStrategy strategy, //
	    boolean recovery, //
	    boolean resume, //
	    Optional<SchedulerJobStatus> status) throws GSException {

    }

    @Override
    public void harvestingEnded(//
	    GSSource source, //
	    Optional<HarvestingProperties> properties, //
	    HarvestingStrategy strategy, //
	    Optional<SchedulerJobStatus> status, //
	    Optional<ListRecordsRequest> request) throws GSException {

    }

    @Override
    public HarvestingProperties retrieveHarvestingProperties(GSSource source) throws GSException {

	return null;
    }

    @Override
    public void storeHarvestingProperties(GSSource source, HarvestingProperties properties) throws GSException {

    }

    @Override
    public List<String> getStorageReport(GSSource source) throws GSException {

	return null;
    }

    @Override
    public Optional<DatabaseFolder> getDataFolder(String sourceIdentifier, boolean writingFolder) throws GSException {

	return Optional.empty();
    }

    @Override
    public void updateErrorsAndWarnReport(GSSource source, String report) throws GSException {

    }

    @Override
    public List<String> retrieveErrorsReport(GSSource source) throws GSException {

	return null;
    }

    @Override
    public List<String> retrieveWarnReport(GSSource source) throws GSException {

	return null;
    }

    @Override
    public SourceStorageSetting getSetting() {

	return null;
    }
}
