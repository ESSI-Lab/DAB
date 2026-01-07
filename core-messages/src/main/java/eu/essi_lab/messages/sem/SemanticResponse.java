package eu.essi_lab.messages.sem;

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

import java.util.Optional;

import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.count.SemanticCountResponse;

/**
 * @author Fabrizio
 */
public class SemanticResponse<T> extends MessageResponse<T, SemanticCountResponse> {

    private T parentObject;

    /**
     * 
     */
    public SemanticResponse() {

	super();
    }

    /**
     * Creates a new {@link MessageResponse} which is a clone of the supplied <code>resultSet</code>
     * but without {@link #getResultsList()}
     * 
     * @param response a non <code>null</code> {@link MessageResponse} to clone
     */
    public SemanticResponse(SemanticResponse<?> response) {
	setException(response.getException());
	setCountResponse(response.getCountResponse());
    }

    /**
     * @return the parentObect
     */
    public Optional<T> getParentObject() {

	return Optional.ofNullable(parentObject);
    }

    /**
     * @param parentObect the parentObect to set
     */
    public void setParentObject(T parentObect) {

	this.parentObject = parentObect;
    }

    @Override
    public String getName() {

	return "SEMANTIC_RESPONSE";
    }
}
