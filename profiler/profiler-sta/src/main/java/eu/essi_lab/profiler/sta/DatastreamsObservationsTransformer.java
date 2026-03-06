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
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.sta.STARequest.EntitySet;

/**
 * Transformer for Datastreams(id)/Observations.
 * Returns a single datastream metadata (for temporal extent) via discovery.
 */
public class DatastreamsObservationsTransformer extends DatastreamsTransformer {

    @Override
    protected Page getPage(WebRequest request) throws GSException {
	STARequest staRequest = new STARequest(request);
	if (staRequest.getEntitySet().orElse(null) == EntitySet.Datastreams
		&& staRequest.getEntityId().isPresent()
		&& "Observations".equals(staRequest.getNavigationProperty().orElse(null))) {
	    return new Page(1, 1);
	}
	return super.getPage(request);
    }
}
