package eu.essi_lab.dbupdater;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.augmenter.AugmentersOptionHelper;
import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.configuration.IGSConfigurationReader;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.jobs.GSJobValidationFailReason;
import eu.essi_lab.jobs.GSJobValidationResult;
import eu.essi_lab.jobs.configuration.AbstractGSConfigurableJob;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.MetadataDataDabaseStoragiURI;
import eu.essi_lab.model.SourcesOptionHelper;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
import eu.essi_lab.model.configuration.option.GSConfOptionInteger;
import eu.essi_lab.model.exceptions.GSException;
public class AugmenterJob extends AbstractGSConfigurableJob {

    /**
     *
     */
    private static final long serialVersionUID = -5198684985077108879L;

    public static final String MAX_RECORDS = "MAX_RECORDS";
    public static final String LESS_RECENT_ORDERING = "LESS_RECENT_ORDERING";
    public static final String TIME_BACK_OPTION = "TIME_BACK_OPTION";

    private SourcesOptionHelper sourcesHelper;
    private AugmentersOptionHelper augmentersHelper;

    public AugmenterJob() {

	setLabel("Augmenter JOB");

	sourcesHelper = new SourcesOptionHelper(this);

	augmentersHelper = new AugmentersOptionHelper(this, sourcesHelper);

	augmentersHelper.putAugmenterOption();

	GSConfOptionInteger maxRecordsOption = new GSConfOptionInteger();

	maxRecordsOption.setMandatory(true);
	maxRecordsOption.setKey(MAX_RECORDS);
	maxRecordsOption.setLabel("Maximum number of records to augment (0 = no limitation)");
	maxRecordsOption.setValue(0);

	getSupportedOptions().put(MAX_RECORDS, maxRecordsOption);

	GSConfOptionBoolean mostRecentOption = new GSConfOptionBoolean();

	mostRecentOption.setMandatory(true);
	mostRecentOption.setKey(LESS_RECENT_ORDERING);
	mostRecentOption.setLabel("Elaborates before the less recent (according to the resource time stamp) records");
	mostRecentOption.setValue(true);

	getSupportedOptions().put(LESS_RECENT_ORDERING, mostRecentOption);

	GSConfOptionInteger timeBackOption = new GSConfOptionInteger();

	timeBackOption.setMandatory(true);
	timeBackOption.setKey(TIME_BACK_OPTION);
	timeBackOption.setLabel(
		"Elaborates only records with resource time stamp >= (cur.time - time back) ( in milliseconds ). 0 = no time back)");
	timeBackOption.setValue(0);

	getSupportedOptions().put(TIME_BACK_OPTION, timeBackOption);
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	if (AugmentersOptionHelper.isAugmentersOptionSet(opt)) {

	    augmentersHelper.handleOnOptionSet(opt, ConfigurationUtils.getAllSources());
	    return;
	}

	super.onOptionSet(opt);
    }

    @JsonIgnore
    public Integer getMaxRecords() {

	return (Integer) getSupportedOptions().get(MAX_RECORDS).getValue();
    }

    @JsonIgnore
    public int getTimeBack() {

	return (Integer) getSupportedOptions().get(TIME_BACK_OPTION).getValue();
    }

    @JsonIgnore
    public Boolean isMostRecentOrderingSet() {

	return (Boolean) getSupportedOptions().get(LESS_RECENT_ORDERING).getValue();
    }

    @Override
    public void run(Map<String, Object> jobDataMap, Boolean isRecovering, Optional<GSJobStatus> jobStatus) throws GSException {

	GSLoggerFactory.getLogger(this.getClass()).info("Starting Augmenter Job");

	AugmenterWorker worker = new AugmenterWorker();

	int maxRecords = getMaxRecords();
	boolean orderingSet = isMostRecentOrderingSet();
	int timeBack = getTimeBack();

	worker.setMaxRecords(maxRecords);
	worker.setMostRecentOrdering(orderingSet);
	worker.setTimeBack(timeBack);

	IGSConfigurationReader manager = new GSConfigurationManager(ConfigurationSync.getInstance());

	MetadataDataDabaseStoragiURI storageURI = manager.readInstantiableType(//
		MetadataDataDabaseStoragiURI.class, //
		new Deserializer()).get(0);

	GSLoggerFactory.getLogger(this.getClass()).debug("Using DB @ {}", storageURI.getUri());

	DatabaseConsumerFactory consumerFactory = new DatabaseConsumerFactory();
	DatabaseReader dataBaseReader = consumerFactory.createDataBaseReader(storageURI);
	DatabaseWriter dataBaseWriter = consumerFactory.createDataBaseWriter(storageURI);

	//
	// ----
	//
	worker.setDatabaseReader(dataBaseReader);
	worker.setDatabaseWriter(dataBaseWriter);

	//
	// ----
	//
	List<Augmenter> augmenters = getConfigurableComponents().//
		values().//
		stream().//
		map(c -> ((Augmenter) c)).//
		collect(Collectors.toList());

	worker.setAugmenters(augmenters);

	//
	// ----
	//
	List<String> sourcesiIdsList = sourcesHelper.getSelectedSourcesIdentifiers();
	worker.setSourcesIdsList(sourcesiIdsList);

	//
	// ----
	//
	worker.augment();
    }

    @Override
    public GSJobValidationResult isValid(Map<String, Object> jobDataMap) {

	GSJobValidationResult result = new GSJobValidationResult();

	List<String> sourcesiIdsList = sourcesHelper.getSelectedSourcesIdentifiers();

	if (sourcesiIdsList == null || sourcesiIdsList.isEmpty()) {

	    result.setValid(false);

	    result.setReason(GSJobValidationFailReason.TARGET_SOURCE_NOT_FOUND);

	    return result;
	}

	result.setValid(true);

	return result;
    }

}
