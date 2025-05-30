package eu.essi_lab.lib.sensorthings._1_1.client.request;

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

import java.util.Optional;

/**
 * @author Fabrizio
 */
public class AddressableEntity implements Composable {

    private String identifier;
    private boolean quoteIdentifier;
    private EntityRef entityRef;

    /**
     * @param entityRef
     */
    AddressableEntity(EntityRef entityRef) {

	this.entityRef = entityRef;
    }

    /**
     * @param entityRef
     * @param identifier
     * @param quoteIdentifier
     */
    AddressableEntity(EntityRef entityRef, String identifier, boolean quoteIdentifier) {

	this(entityRef);
	this.identifier = identifier;
	this.quoteIdentifier = quoteIdentifier;
    }

    /**
     * @return
     */
    public boolean isQuoteIdentifierSet() {

	return quoteIdentifier;
    }

    /**
     * @return the identifier
     */
    public Optional<String> getIdentifier() {

	return Optional.ofNullable(identifier);
    }

    /**
     * @return
     */
    public EntityRef getEntityRef() {

	return entityRef;
    }

    @Override
    public String compose() {

	String id = "";
	if (identifier != null && quoteIdentifier) {
	    id = "'" + identifier + "'";
	} else if (identifier != null && !quoteIdentifier) {
	    id = identifier;
	}

	if (!id.isEmpty()) {
	    id = "(" + id + ")";
	}

	return entityRef.getName() + id;
    }
}
