package eu.essi_lab.gssrv.rest;

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

/**
 * Result of deleting a predefined shape area by prefix.
 */
public final class PredefinedShapeDeleteResult {

    private final Optional<String> errorMessage;
    private final boolean forbidden;

    private PredefinedShapeDeleteResult(Optional<String> errorMessage, boolean forbidden) {

	this.errorMessage = errorMessage;
	this.forbidden = forbidden;
    }

    public static PredefinedShapeDeleteResult ok() {

	return new PredefinedShapeDeleteResult(Optional.empty(), false);
    }

    public static PredefinedShapeDeleteResult failure(String message) {

	return new PredefinedShapeDeleteResult(Optional.of(message), false);
    }

    public static PredefinedShapeDeleteResult forbidden(String message) {

	return new PredefinedShapeDeleteResult(Optional.of(message), true);
    }

    public boolean isSuccess() {

	return errorMessage.isEmpty();
    }

    public boolean isForbidden() {

	return forbidden;
    }

    public Optional<String> getErrorMessage() {

	return errorMessage;
    }
}
