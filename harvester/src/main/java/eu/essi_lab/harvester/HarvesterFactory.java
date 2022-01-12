package eu.essi_lab.harvester;

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

import eu.essi_lab.adk.GSAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.harvester.component.HarvesterPlan;
import eu.essi_lab.harvester.job.HarvesterJob;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class HarvesterFactory {

    private static final String HARVESTER_JOB_ID_PREFIX = "harvesterJob_";
    private static final String HARVESTER_KEY = "harvester";
    private static final String ERR_ID_NULL_IOAIPMHACCESSOR = "ERR_ID_NULL_IOAIPMHACCESSOR";

    private static final HarvesterFactory INSTANCE = new HarvesterFactory();

    private HarvesterFactory() {
    }

    /**
     * @return
     */
    public static HarvesterFactory getInstance() {

	return INSTANCE;
    }

    public IGSConfigurableComposed getHarvesterConfigurable(IHarvestedAccessor hacc) throws GSException {

	if (hacc == null) {

	    throw GSException.createException(//
		    HarvesterFactory.class, //
		    "Missing IHarvestedAccessor instance", //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ERR_ID_NULL_IOAIPMHACCESSOR);
	}

	return createConfigurable(hacc);
    }

    public IGSConfigurableComposed getHarvesterConfigurable(GSAccessor mixed) {

	String toremove = mixed.getHarvestedAccessor().getKey();

	Harvester harvester = initHarvester(mixed.getHarvestedAccessor());

	HarvesterJob job = createHarvesterJob(((GSSource) mixed.getInstantiableType()).getUniqueIdentifier());

	harvester.addJob(job);

	mixed.getConfigurableComponents().remove(toremove);

	mixed.getConfigurableComponents().put(harvester.getKey(), harvester);

	return mixed;
    }

    private Harvester initHarvester(IHarvestedAccessor accessor) {

	Harvester configurable = new Harvester();

	configurable.setKey(HARVESTER_KEY + ":" + accessor.getKey());

	configurable.setAccessor(accessor);

	configurable.setPlan(new HarvesterPlan());

	return configurable;
    }

    private Harvester createConfigurable(IHarvestedAccessor accessor) {

	Harvester configurable = initHarvester(accessor);

	HarvesterJob job = createHarvesterJob(accessor.getSource().getUniqueIdentifier());

	configurable.addJob(job);

	return configurable;
    }

    private HarvesterJob createHarvesterJob(String sid) {

	HarvesterJob job = new HarvesterJob();

	job.setId(HARVESTER_JOB_ID_PREFIX + sid);

	return job;
    }
}
