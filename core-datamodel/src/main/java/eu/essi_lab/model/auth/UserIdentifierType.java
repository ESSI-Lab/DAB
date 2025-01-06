package eu.essi_lab.model.auth;

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

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Fabrizio
 */
public enum UserIdentifierType {

    /**
     * 
     */
    OAUTH_EMAIL("oauthEmailIdentifier"),
    /**
     * 
     */
    HOST("hostIdentifier"),
    /**
     * 
     */
    X_FORWARDER_FOR("xForwardedForIdentifier"),
    /**
     * 
     */
    ORIGIN_HEADER("originHeaderIdentifier"),
    /**
     * 
     */
    VIEW_IDENTIFIER("viewIdIdentifier"),
    /**
     * 
     */
    VIEW_CREATOR("viewCreatorIdentifier"),
    /**
     * 
     */
    USER_TOKEN("userTokenIdentifier"),
    /**
     * 
     */
    CLIENT_IDENTIFIER("clientIdentifier");

    private String type;

    /**
     * @param type
     */
    private UserIdentifierType(String type) {

	this.type = type;
    }

    /**
     * @param type
     * @return
     */
    public static Optional<UserIdentifierType> fromType(String type) {

	return Arrays.asList(values()).//
		stream().//
		filter(t -> t.getType().equals(type)).//
		findFirst();
    }

    /**
     * @return the type
     */
    public String getType() {

	return type;
    }
}
