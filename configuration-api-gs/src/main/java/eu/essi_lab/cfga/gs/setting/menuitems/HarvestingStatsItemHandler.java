package eu.essi_lab.cfga.gs.setting.menuitems;

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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import com.vaadin.flow.component.textfield.TextArea;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.gui.components.tabs.TabContent;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;

/**
 * @author Fabrizio
 */
public class HarvestingStatsItemHandler extends GridMenuItemHandler {

    /**
     * 
     */
    public HarvestingStatsItemHandler() {

    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public HarvestingStatsItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	ConfirmationDialog dialog = new ConfirmationDialog();
	dialog.setTitle("Harvesting statistics");
	dialog.setHeight(400, Unit.PIXELS);
	dialog.setWidth(600, Unit.PIXELS);
	dialog.getConfirmButton().setVisible(false);
	dialog.setCancelText("Close");

	TextArea textArea = new TextArea();

	textArea.setWidth(570, Unit.PIXELS);
	textArea.setHeight(260, Unit.PIXELS);
	textArea.getStyle().set("font-size", "14px");
	textArea.setReadOnly(true);

	dialog.setContent(textArea);

	//
	//
	//

	int harvSourcesCount = ConfigurationWrapper.getHarvestingSettings().size();

	long scheduledSources = ConfigurationWrapper.//
		getHarvestingSettings().//
		stream().//
		filter(s -> s.getScheduling().isEnabled()).//
		count();

	long unscheduledSources = ConfigurationWrapper.//
		getHarvestingSettings().//
		stream().//
		filter(s -> !s.getScheduling().isEnabled()).//
		count();

	StringBuilder builder = new StringBuilder("- Harvested sources: " + harvSourcesCount + "\n\n");

	Optional<String[]> maxMinSizeSources = getMaxMinSizeSources();

	if (maxMinSizeSources.isPresent()) {

	    builder.append("- Biggest source: ").append(maxMinSizeSources.get()[0]).append("\n");
	    builder.append("- Smaller source: ").append(maxMinSizeSources.get()[1]).append("\n\n");
	}

	Optional<String[]> longestShortestHarvesting = getLongestShortestHarvesting();

	if (longestShortestHarvesting.isPresent()) {

	    builder.append("- Longest harvesting: ").append(longestShortestHarvesting.get()[0]).append("\n");
	    builder.append("- Shortest harvesting: ").append(longestShortestHarvesting.get()[1]).append("\n\n");
	}

	builder.append("- Scheduled sources: ").append(scheduledSources).append("\n");
	builder.append("- Non scheduled source: ").append(unscheduledSources).append("\n");

	Scheduler scheduler = SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());
	try {
	    long count = scheduler.getJobStatuslist().stream().filter(j -> j.getPhase() == JobPhase.ERROR).count();
	    builder.append("- Sources with errors: ").append(count).append("\n");

	} catch (SQLException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	}

	//
	//
	//

	textArea.setValue(builder.toString());
	dialog.open();
    }

    /**
     * @return
     */
    public boolean isContextual() {

	return false;
    }

    /**
     * @return
     */
    private Optional<String[]> getMaxMinSizeSources() {

	List<String> list = ConfigurationWrapper.//
		getHarvestingSettings().//
		stream().//
		filter(s -> !SchedulerSupport.getInstance().getSize(s).isEmpty()).//
		sorted((s1, s2) -> parse(s2).compareTo(parse(s1))).//
		map(s -> s.getName() + " with " + SchedulerSupport.getInstance().getSize(s) + " records").//
		collect(Collectors.toList());//

	return toOptionalArray(list);
    }

    /**
     * 
     */
    private Optional<String[]> getLongestShortestHarvesting() {

	List<String> list = ConfigurationWrapper.//
		getHarvestingSettings().//
		stream().//
		filter(s -> !SchedulerSupport.getInstance().getElapsedTime(s).isEmpty()).//
		sorted((s1, s2) -> SchedulerSupport.getInstance().getElapsedTime(s2)
			.compareTo(SchedulerSupport.getInstance().getElapsedTime(s1)))
		.//
		map(s -> s.getName() + " in " + SchedulerSupport.getInstance().getElapsedTime(s) + " (HH:mm:ss)").//
		collect(Collectors.toList());//

	return toOptionalArray(list);
    }

    /**
     * @param list
     * @return
     */
    private Optional<String[]> toOptionalArray(List<String> list) {

	if (list.isEmpty()) {

	    return Optional.empty();
	}

	if (list.size() == 1) {

	    return Optional.of(new String[] { list.getFirst(), list.getFirst() });
	}

	return Optional.of(new String[] { list.getFirst(), list.getLast() });
    }

    /**
     * @param s
     * @return
     */
    private Integer parse(HarvestingSetting s) {

	return Integer.valueOf(SchedulerJobStatus.parse(SchedulerSupport.getInstance().getSize(s)));
    }

    @Override
    public String getItemText() {

	return "Harvesting statistics";
    }
}
