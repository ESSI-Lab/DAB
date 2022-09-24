package eu.essi_lab.api.database.vol;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class VolatileSourceStorage implements SourceStorage {

    private VolatileDatabase database;
    private SourceStorageSetting setting;
    private StorageUri dbUri;
    private String startTimeStamp;

    @SuppressWarnings("rawtypes")
    @Override
    public void setDatabase(Database db) {

	this.database = (VolatileDatabase) db;
    }

    @Override
    public boolean supports(StorageUri dbUri) {

	this.dbUri = dbUri;

	return dbUri.getStorageName() != null && //
		dbUri.getStorageName().equals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME);
    }

    @Override
    public StorageUri getStorageUri() {

	return this.dbUri;
    }

    @Override
    public VolatileDatabase getDatabase() {

	return (VolatileDatabase) this.database;
    }

    @Override
    public void configure(SourceStorageSetting setting) {

	this.setting = setting;
    }

    @Override
    public String getType() {

	return "VolatileSourceStorage";
    }

    @Override
    public void harvestingStarted(GSSource source, HarvestingStrategy strategy, boolean recovery) throws GSException {

	harvestingStarted(source, strategy, recovery, null);
    }

    @Override
    public void harvestingEnded(GSSource source, HarvestingStrategy strategy) throws GSException {

	harvestingEnded(source, strategy, null);
    }

    @Override
    public void harvestingStarted(GSSource source, HarvestingStrategy strategy, boolean recovery, Optional<SchedulerJobStatus> status)
	    throws GSException {

	startTimeStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();
    }

    @Override
    public void harvestingEnded(GSSource source, HarvestingStrategy strategy, Optional<SchedulerJobStatus> status) throws GSException {

	HarvestingProperties properties = new HarvestingProperties();

	properties = retrieveHarvestingProperties(source);

	int harvCount = properties.getHarvestingCount();

	properties.setHarvestingCount(harvCount == -1 ? 1 : harvCount++);

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
    public SourceStorageSetting getSetting() {

	return this.setting;
    }
}
