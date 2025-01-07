package eu.essi_lab.gssrv.conf.task;

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
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.views.DefaultViewManager;

/**
 * @author Fabrizio
 */
public class GEOSSAccessedSourceTask extends AbstractCustomTask {

    private int count;

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "- List of GEOSS sources having at least one access augmenter -\n");

	//
	//
	//

	DatabaseReader reader = DatabaseProviderFactory.getReader(ConfigurationWrapper.getDatabaseURI());

	DefaultViewManager viewManager = new DefaultViewManager();
	viewManager.setDatabaseReader(reader);

	List<String> sourceIds = ((LogicalBond) viewManager.getView("geoss").//
		get().//
		getBond()).//
			getOperands().//
			stream().//
			map(o -> ((ResourcePropertyBond) o).getPropertyValue()).//
			collect(Collectors.toList());

	//
	//
	//

	count = 1;

	List<String> labels = ConfigurationWrapper.getHarvestingSettings().//
		stream().//

		filter(s -> sourceIds.contains(s.getSelectedAccessorSetting().getGSSourceSetting().getSourceIdentifier())).//

		filter(s -> s.getSelectedAugmenterSettings().stream().anyMatch(as -> as.getName().toLowerCase().contains("access"))).//

		peek(s -> s.getSelectedAugmenterSettings()
			.forEach(as -> System.out.println(" Source: " + s.getSelectedAccessorSetting().getGSSourceSetting().getSourceLabel()
				+ " - Augmenter: " + as.getName())))
		.//

		map(s -> s.getSelectedAccessorSetting().getGSSourceSetting().getSourceLabel()).//

		sorted().//

		map(s -> "" + (count++) + " - " + s).//

		collect(Collectors.toList());

	for (String label : labels) {

	    log(status, label);
	}

	log(status, "\n- List of GEOSS sources having no access augmenter -\n");

	count = 1;

	labels = ConfigurationWrapper.getHarvestingSettings().//
		stream().//

		filter(s -> sourceIds.contains(s.getSelectedAccessorSetting().getGSSourceSetting().getSourceIdentifier())).//

		filter(s -> s.getSelectedAugmenterSettings().isEmpty()).//

		map(s -> s.getSelectedAccessorSetting().getGSSourceSetting().getSourceLabel()).//

		sorted().//

		map(s -> "" + (count++) + " - " + s).//

		collect(Collectors.toList());

	for (String label : labels) {

	    log(status, label);
	}

    }

    @Override
    public String getName() {

	return "GEOSS accessed sources viewer";
    }
}
