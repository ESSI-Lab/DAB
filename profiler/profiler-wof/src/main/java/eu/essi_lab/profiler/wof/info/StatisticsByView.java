package eu.essi_lab.profiler.wof.info;

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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wof.info.GetWaterOneFlowServiceInfoHandler.WOFGroup;

public class StatisticsByView implements StatisticsGenerator {

    private String[] views;

    public StatisticsByView(String... views) {
	this.views = views;
    }

    @Override
    public List<StatisticsMessage> getStatisticMessages(WebRequest webRequest, String viewId, WOFGroup selected, List<GSSource> allSources,
	    LogicalBond andBond) throws GSException {
	List<StatisticsMessage> statisticsMessages = new ArrayList<>();

	for (String view : views) {

	    StatisticsMessage statisticsMessage = new StatisticsMessage();

	    // set the required properties

	    statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	    // statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());
	    statisticsMessage.setWebRequest(webRequest);

	    WebRequestTransformer.setView(//
		    view, //
		    statisticsMessage.getDataBaseURI(), //
		    statisticsMessage);
	    if (statisticsMessage.getView().isPresent()) {
		statisticsMessage.setSources(ConfigurationWrapper.getViewSources(statisticsMessage.getView().get()));
	    } else {
		statisticsMessage.setSources(allSources);
	    }

	    // set the user bond
	    statisticsMessage.setUserBond(andBond);
	    statisticsMessages.add(statisticsMessage);

	}
	return statisticsMessages;
    }

}
