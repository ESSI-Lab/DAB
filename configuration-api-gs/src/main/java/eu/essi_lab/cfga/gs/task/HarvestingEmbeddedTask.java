/**
 * 
 */
package eu.essi_lab.cfga.gs.task;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;

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

/**
 * This interface must be implemented by harvesting embedded tasks
 * 
 * @author Fabrizio
 */
public interface HarvestingEmbeddedTask extends CustomTask {

    /**
     * @author Fabrizio
     */
    public enum ExecutionStage {
	/**
	 * 
	 */
	BEFORE_HARVESTING_END,
	/**
	 * 
	 */
	AFTER_HARVESTING_END;
    }

    /**
     * @return
     */
    public ExecutionStage getExecutionStage();

    /**
     * @param request
     */
    public void setListRecordsRequest(ListRecordsRequest request);

    /**
     * @return
     */
    public ListRecordsRequest getListRecordsRequest();
}
