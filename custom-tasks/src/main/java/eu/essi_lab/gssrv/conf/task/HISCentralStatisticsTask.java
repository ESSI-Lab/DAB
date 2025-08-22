package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CollectionType;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

public class HISCentralStatisticsTask extends AbstractCustomTask {

    public HISCentralStatisticsTask() {
	// TODO Auto-generated constructor stub
    }

    @Override
    public String getName() {
	return "HIS-Central statistics task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("HIS-Central statistics STARTED");

	View view = DiscoveryRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), "his-central").get();
	List<GSSource> sources = ConfigurationWrapper.getViewSources(view);

	for (GSSource source : sources) {
	    GSLoggerFactory.getLogger(getClass()).info("Source {}", source.getLabel());
	    ResourcePropertyBond sBond = BondFactory.createSourceIdentifierBond(source.getUniqueIdentifier());
	    DiscoveryCountResponse count = count(source, sBond);
	    GSLoggerFactory.getLogger(getClass()).info("Total: {}", count.getCount());
	    ResourcePropertyBond cBond = BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.COLLECTION_TYPE,
		    CollectionType.STATION.toString());
//
	    LogicalBond aBond = BondFactory.createAndBond(sBond, cBond);
	    count = count(source, aBond);
	    GSLoggerFactory.getLogger(getClass()).info("Collections: {}", count.getCount());
	}

	GSLoggerFactory.getLogger(getClass()).info("HIS-Central statistics ENDED");
    }

    private DiscoveryCountResponse count(GSSource source, Bond bond) throws Exception {
	DatabaseFinder dbFinder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());
	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(UUID.randomUUID().toString());
	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	message.getResourceSelector().setSubset(ResourceSubset.CORE);
	ArrayList<GSSource> ss = new ArrayList<GSSource>();
	ss.add(source);
	message.setSources(ss);
//	View view = DiscoveryRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), "his-central").get();
//
//	message.setView(view);
	message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

	message.setNormalizedBond(bond);
	message.setPermittedBond(bond);
	message.setUserBond(bond);

	return dbFinder.count(message);
    }

}
