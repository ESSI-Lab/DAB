package eu.essi_lab.identifierdecorator;

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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.model.GSSource;

/**
 * This exception is thrown when the current harvested resource has the same original identifier of an existing resource
 * provided by a different source
 * 
 * @author Fabrizio
 */
public class ConflictingResourceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8766297598766056445L;
    private List<GSSource> incomingSources;
    private List<GSSource> existingSources;
    private List<String> originalIds;

    /**
     * 
     */
    public ConflictingResourceException() {

	originalIds = new ArrayList<>();
	existingSources = new ArrayList<>();
	incomingSources = new ArrayList<>();
    }

    /**
     * @return
     */
    public List<GSSource> getIncomingSources() {
	return incomingSources;
    }

    /**
     * @return
     */
    public List<GSSource> getExistingSources() {
	return existingSources;
    }

    /**
     * @return
     */
    public List<String> getOriginalIds() {
	return originalIds;
    }

    /**
     * @param originalId
     * @param incomingSource
     * @param existingSource
     */
    public void add(String originalId, GSSource incomingSource, GSSource existingSource) {

	originalIds.add(originalId);
	incomingSources.add(incomingSource);
	existingSources.add(existingSource);
    }
}
