package eu.essi_lab.profiler.wis.observations;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.CollectionItems;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;

public class WISObservationsFilter implements WebRequestFilter {

    @Override
    public boolean accept(WebRequest request) throws GSException {
	WISRequest wr = new WISRequest(request);
	TopRequest topRequest = wr.getTopRequest();
	CollectionItems collectionItem = wr.getCollectionItem();	
	return topRequest!=null && topRequest.equals(TopRequest.COLLECTIONS) && collectionItem.equals(CollectionItems.OBSERVATIONS);
    }

}
