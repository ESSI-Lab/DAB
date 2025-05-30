/**
 * 
 */
package eu.essi_lab.identifierdecorator;

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

import eu.essi_lab.model.exceptions.SkipProcessStepException;
import eu.essi_lab.model.resource.GSResource;

/**
 * This exception is thrown when the current harvested resource has the same original identifier of an existing resource
 * provided by a the same source
 * 
 * @author Fabrizio
 */
public class DuplicatedResourceException extends SkipProcessStepException {

    /**
     * 
     */
    private static final long serialVersionUID = 4825228843179600842L;
    private String originalId;
    private GSResource incomingResource;
    private int duplicationCase;
    private GSResource existingResource;

    /**
     * 
     */
    public DuplicatedResourceException() {
    }

    /**
     * @param existingResource
     * @param originalId
     */
    public DuplicatedResourceException(//
	    GSResource incomingResource, //
	    GSResource existingResource, //
	    String originalId, //
	    int duplicationCase) {

	this.incomingResource = incomingResource;
	this.existingResource = existingResource;
	this.originalId = originalId;
	this.duplicationCase = duplicationCase;
    }

    /**
     * @return
     */
    public int getDuplicationCase() {

	return duplicationCase;
    }

    /**
     * @return
     */
    public GSResource getExistingResource() {

	return existingResource;
    }

    /**
     * @return
     */
    public GSResource getIncomingResource() {

	return incomingResource;
    }

    /**
     * @return
     */
    public String getOriginalId() {

	return originalId;
    }
}
