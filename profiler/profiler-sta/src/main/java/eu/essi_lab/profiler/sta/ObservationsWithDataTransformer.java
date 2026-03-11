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

package eu.essi_lab.profiler.sta;

import eu.essi_lab.messages.Page;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.messages.web.WebRequest;

/**
 * Transformer for Observations when fetching real data.
 * Limits datastreams per block to avoid too many access requests.
 */
public class ObservationsWithDataTransformer extends ObservationsTransformer {

    private static final int DATASTREAMS_PER_BLOCK = 5;

    @Override
    protected Page getPage(WebRequest request) throws GSException {
	STARequest staRequest = new STARequest(request);
	int top = staRequest.getTop() != null ? staRequest.getTop() : DATASTREAMS_PER_BLOCK;
	top = Math.min(top, 20);
	int skip = staRequest.getSkip() != null ? staRequest.getSkip() : 0;
	if (staRequest.getEntityId().isPresent() && !staRequest.getNavigationProperty().isPresent()) {
	    return new Page(1, 1);
	}
	return new Page(skip + 1, top);
    }
}
