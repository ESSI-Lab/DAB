package eu.essi_lab.pdk.wrt;

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

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.views.DefaultViewManager;

/**
 * Validates and transforms a {@link WebRequest} in the correspondent {@link RequestMessage}
 *
 * @param <M> the type of the {@link RequestMessage} to provide as result of the transformation
 * @author Fabrizio
 */
public abstract class WebRequestTransformer<M extends RequestMessage> implements WebRequestValidator, Pluggable {

    protected static final String DB_STORAGE_URI_NOT_FOUND = "DB_STORAGE_URI_NOT_FOUND";
    private static final String VIEW_NOT_FOUND = "VIEW_NOT_FOUND";
    private static final String FINDING_VIEW_ERROR = "FINDING_VIEW_ERROR";

    /**
     * Transforms the supplied <code>request</code> in the correspondent {@link RequestMessage}. This partial
     * implementation initialise the
     * {@link RequestMessage} created with {@link #createMessage()} by setting the following common properties and
     * invokes the {@link
     * #refineMessage(RequestMessage)} method
     * <br>
     * <b>Common properties set</b>
     * <ul>
     * <li>the {@link WebRequest} (see {@link RequestMessage#setWebRequest(WebRequest)})</li>
     * <li>the view identifier (see {@link RequestMessage#getViewId()}) read from the {@link WebRequest})</li>
     * <li>the {@link GSUser} (see {@link RequestMessage#getCurrentUser()}) created from the {@link WebRequest})</li>
     * <li>the {@link StorageInfo} (see {@link RequestMessage#setDataBaseURI(StorageInfo)})</li>
     * <li>the {@link Page} (see {@link RequestMessage#setPage(Page)})</li>
     * </ul>
     */
    public M transform(WebRequest request) throws GSException {

	M message = createMessage();

	message.setProfilerName(request.getProfilerName());

	message.setRequestId(request.getRequestId());

	message.setWebRequest(request);

	message.setPage(getPage(request));

	message.setCurrentUser(request.getCurrentUser());

	DownloadSetting downloadSetting = ConfigurationWrapper.getDownloadSetting();
	message.setUserJobStorageURI(downloadSetting.getStorageUri());

	StorageInfo storageUri = ConfigurationWrapper.getStorageInfo();

	message.setDataBaseURI(storageUri);

	handleView(request, storageUri, message);

	return refineMessage(message);
    }

    /**
     * Extracts the view identifier from the <code>request</code> and, if present, tries to retrieve the related view
     * and sets it to the <code>message</code>.<br>
     * This method is called at last in the {@link #transform(WebRequest)} method, just before
     * {@link #refineMessage(RequestMessage)}
     * 
     * @param request
     * @param storageUri
     * @param message
     * @throws GSException
     */
    protected void handleView(WebRequest request, StorageInfo storageUri, RequestMessage message) throws GSException {

	Optional<String> viewId = request.extractViewId();

	if (viewId.isPresent()) {

	    setView(viewId.get(), storageUri, message);
	}
    }

    /**
     * A string which identifies the type of profiler suitable for this transformer
     *
     * @return a non <code>null</code> string which identifies the type of profiler suitable for this transformer
     * @see ProfilerSetting#getServiceType()
     */
    public abstract String getProfilerType();

    /**
     * Refines <code>message</code> created with {@link #createMessage()} and initialized with
     * {@link #transform(WebRequest)}
     */
    protected abstract M refineMessage(M message) throws GSException;

    /**
     * Creates a concrete implementation of {@link RequestMessage}
     */
    protected abstract M createMessage();

    /**
     * Returns the {@link Page} used to delimit the portion of the {@link ResultSet} to handle
     *
     * @param request the {@link WebRequest} triggered by the {@link Profiler} supported client
     * @return a non <code>null</code> {@link Page} used to delimit the portion of the {@link ResultSet} to handle
     */
    protected abstract Page getPage(WebRequest request) throws GSException;

    /**
     * Sets the view if present
     * 
     * @param request
     * @param storageUri
     * @param message
     * @throws GSException
     */
    public static void setView(String viewId, StorageInfo storageUri, RequestMessage message) throws GSException {

	GSLoggerFactory.getLogger(WebRequestTransformer.class).debug("Finding view {} STARTED", viewId);

	Optional<View> view = findView(storageUri, viewId);

	if (view.isPresent()) {

	    message.setView(view.get());

	    GSLoggerFactory.getLogger(WebRequestTransformer.class).debug("Finding view {} ENDED", viewId);

	} else {

	    throw GSException.createException(//
		    WebRequestTransformer.class, //
		    "View " + viewId + " not found", //
		    "Provided view identifier is not valid", //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    VIEW_NOT_FOUND);
	}
    }

    /**
     * @param databaseURI
     * @param viewIdentifier
     * @return
     * @throws GSException
     */
    public static Optional<View> findView(StorageInfo databaseURI, String viewIdentifier) throws GSException {

	try {

	    DatabaseReader reader = DatabaseProviderFactory.getReader(databaseURI);

	    DefaultViewManager manager = new DefaultViewManager();
	    manager.setDatabaseReader(reader);

	    return manager.getResolvedView(viewIdentifier);

	} catch (GSException ex) {

	    GSLoggerFactory.getLogger(WebRequestTransformer.class).error(ex.getMessage(), ex);

	    throw GSException.createException(//
		    WebRequestTransformer.class, //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    FINDING_VIEW_ERROR);
	}
    }
}
