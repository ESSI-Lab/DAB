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

import java.io.InputStream;

/**
 * Outcome type for predefined shape uploads (delegates to {@link PredefinedShapeManagementService}).
 */
public class PredefinedShapeUploadService {

    /**
     * Outcome of a predefined shape upload.
     */
    public static final class UploadOutcome {

	private final String entryPrefix;
	private final String errorMessage;
	private final boolean forbidden;

	private UploadOutcome(String entryPrefix, String errorMessage, boolean forbidden) {

	    this.entryPrefix = entryPrefix;
	    this.errorMessage = errorMessage;
	    this.forbidden = forbidden;
	}

	public static UploadOutcome success(String entryPrefix) {

	    return new UploadOutcome(entryPrefix, null, false);
	}

	public static UploadOutcome failure(String errorMessage) {

	    return new UploadOutcome(null, errorMessage, false);
	}

	public static UploadOutcome forbidden(String errorMessage) {

	    return new UploadOutcome(null, errorMessage, true);
	}

	public boolean isSuccess() {

	    return errorMessage == null;
	}

	public String getEntryPrefix() {

	    return entryPrefix;
	}

	public String getErrorMessage() {

	    return errorMessage;
	}

	public boolean isForbidden() {

	    return forbidden;
	}
    }

    private final PredefinedShapeManagementService managementService = new PredefinedShapeManagementService();

    /**
     * @param originalFileName
     * @param explicitShapeId optional user-provided shape identifier; if blank, derived from {@code originalFileName}
     * @param zipStream
     * @return upload outcome
     */
    public UploadOutcome upload(String originalFileName, String explicitShapeId, InputStream zipStream, String group, String owner,
	    boolean actorIsAdmin) {

	return managementService.upload(originalFileName, explicitShapeId, zipStream, group, owner, actorIsAdmin);
    }
}
