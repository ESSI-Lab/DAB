package eu.essi_lab.api.database.vol;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class VolatileSourceStorage extends SourceStorage {

    private VolatileDatabase database;
    private StorageInfo dbUri;
    private String startTimeStamp;

    @Override
    public boolean supports(StorageInfo dbUri) {

	this.dbUri = dbUri;

	return dbUri.getName() != null && //
		dbUri.getName().equals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME);
    }

    @Override
    public void setDatabase(Database db) {

	this.database = (VolatileDatabase) db;
    }

    @Override
    public VolatileDatabase getDatabase() {

	return (VolatileDatabase) this.database;
    }

    @Override
    public String getType() {

	return "VolatileSourceStorage";
    }

    @Override
    public void harvestingStarted(//
	    GSSource source, //
	    HarvestingStrategy strategy, //
	    boolean recovery, //
	    boolean resume, //
	    Optional<SchedulerJobStatus> status,
	    Optional<ListRecordsRequest> request) throws GSException {

	startTimeStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();
    }

    @Override
    public void harvestingEnded(//
	    GSSource source, //
	    Optional<HarvestingProperties> optProperties, //
	    HarvestingStrategy strategy, //
	    Optional<SchedulerJobStatus> status) throws GSException {

	HarvestingProperties properties = optProperties.orElse(new HarvestingProperties());

	properties = retrieveHarvestingProperties(source);

	properties.incrementHarvestingCount();

	int size = (int) getDatabase().//
		getResourcesList().//
		stream().//
		filter(r -> r.getSource().equals(source)).//
		count();

	properties.setResourcesCount(size);

	properties.setStartHarvestingTimestamp(startTimeStamp);

	properties.setEndHarvestingTimestamp();

	storeHarvestingProperties(source, properties);
    }

    @Override
    public HarvestingProperties retrieveHarvestingProperties(GSSource source) throws GSException {

	return getDatabase().getHarvesingPropertiesMap().computeIfAbsent(source, s -> new HarvestingProperties());
    }

    @Override
    public void storeHarvestingProperties(GSSource source, HarvestingProperties properties) throws GSException {

	synchronized (getDatabase().getHarvesingPropertiesMap()) {

	    getDatabase().getHarvesingPropertiesMap().put(source, properties);
	}
    }

    @Override
    public List<String> getStorageReport(GSSource source) throws GSException {

	return new ArrayList<String>();
    }

    @Override
    public void updateErrorsAndWarnReport(GSSource source, String report) throws GSException {
    }

    @Override
    public List<String> retrieveErrorsReport(GSSource source) throws GSException {

	return new ArrayList<String>();
    }

    @Override
    public List<String> retrieveWarnReport(GSSource source) throws GSException {

	return new ArrayList<String>();
    }

    @Override
    public Optional<DatabaseFolder> getDataFolder(String sourceIdentifier, boolean writingFolder) throws GSException {

	return Optional.empty();
    }
}
