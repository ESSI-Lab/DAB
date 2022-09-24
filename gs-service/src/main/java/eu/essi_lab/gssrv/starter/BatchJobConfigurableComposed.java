package eu.essi_lab.gssrv.starter;

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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;

import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSMainConfigurable;
import eu.essi_lab.model.configuration.Subcomponent;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class BatchJobConfigurableComposed extends AbstractGSconfigurableComposed implements IGSMainConfigurable {

    /**
     *
     */
    private static final long serialVersionUID = -6013495032874325548L;

    private static final String BATCH_JOB_KEY = "BATCH_JOB_KEY";
    private static final String BATCH_JOBS_KEY = "BATCH_JOBS_KEY";
    private static final String BATCH_JOB_CREATION_ERROR = "BATCH_JOB_CREATION_ERROR";

    private Map<String, GSConfOption<?>> options = new HashMap<>();

    public BatchJobConfigurableComposed() {

	setLabel("Batch Jobs");
	setKey(BATCH_JOBS_KEY);

	addBatchJobOption(BATCH_JOB_KEY);
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {

	return options;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

	GSConfOptionSubcomponent option = (GSConfOptionSubcomponent) opt;

	Subcomponent subcomponent = option.getValue();
	String augmenterClass = subcomponent.getValue();

	try {
	    IGSConfigurableJob job = (IGSConfigurableJob) Class.forName(augmenterClass).newInstance();

	    getConfigurableComponents().put(UUID.randomUUID().toString().substring(0, 6), job);

	} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't instantiate {}", augmenterClass, e);

	    GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BATCH_JOB_CREATION_ERROR, //
		    e);
	}

    }

    private void addBatchJobOption(String key) {

	ServiceLoader<IGSConfigurableJob> loader = ServiceLoader.load(IGSConfigurableJob.class);

	GSConfOptionSubcomponent option = new GSConfOptionSubcomponent();
	option.setKey(key);
	option.setLabel("Select batch job");

	for (IGSConfigurableJob job : loader) {

	    Subcomponent subcomponent = new Subcomponent(//
		    job.getLabel(), //
		    job.getClass().getCanonicalName());

	    option.getAllowedValues().add(subcomponent);
	}

	getSupportedOptions().put(option.getKey(), option);
    }

    @Override
    public void onFlush() throws GSException {
	// nothing to do here
    }

}
