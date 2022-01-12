package eu.essi_lab.messages;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.rip.RuntimeInfoProvider;
public abstract class RequestMessage extends GSMessage implements RuntimeInfoProvider {

    /**
     * 
     */
    private static final long serialVersionUID = 3898362167520217358L;
    private static final String WEB_REQUEST = "webRequest";
    private static final String REQUEST_ABSOLUTE_PATH = "request_absolute_path";

    private static final String DATABASE_URL = "databaseURL";
    private static final String USER_JOB_STORAGE_URI = "user_job_storage_uri";
    private static final String USER_JOB_RESULT_ID = "user_job_result_id";
    private static final String SHARED_REPO_INFO = "shared_repository_info";
    private static final String CURRENT_USER = "user";
    private static final String VIEW = "view";
    private static final String SOURCES = "sources";
    private static final String EXCEPTION = "exception";
    private static final String PAGE = "page";
    private static final String SCHEDULED = "scheduled";
    private static final String OUTPUT_SOURCES = "outputSources";
    private static final String REQUEST_TIMEOUT = "requestTimeout";
    private static final String ITERATED_WORKFLOW = "ITERATED_WORKFLOW";

    private String requestId;

    @Override
    public HashMap<String, List<String>> provideInfo() {
	HashMap<String, List<String>> ret = new HashMap<>();
	ret.put(RuntimeInfoElement.MESSAGE_TYPE.getName(), Arrays.asList(getClass().getSimpleName()));
	return ret;
    }

    /**
     * 
     */
    public RequestMessage() {

	setException(new GSException());
	setSources(new ArrayList<GSSource>());
	setOutputSources(false);

	requestId = UUID.randomUUID().toString();
    }

    /**
     * @return
     */
    public String getRequestId() {

	return requestId;
    }

    /**
     * @param id
     */
    public void setRequestId(String id) {

	this.requestId = id;
    }

    // ----------------------------------------
    //
    // Header properties
    //
    //

    /**
     * Gets the request timeout in seconds
     * 
     * @return
     */
    public Integer getRequestTimeout() {

	return getHeader().get(REQUEST_TIMEOUT, Integer.class);
    }

    /**
     * Sets the request timeout in seconds
     * 
     * @param timeout
     */
    public void setRequestTimeout(Integer timeout) {

	getHeader().add(new GSProperty<Integer>(REQUEST_TIMEOUT, timeout));
    }

    public StorageUri getUserJobStorageURI() {

	return getHeader().get(USER_JOB_STORAGE_URI, StorageUri.class);
    }

    public void setUserJobStorageURI(StorageUri url) {

	getHeader().add(new GSProperty<StorageUri>(USER_JOB_STORAGE_URI, url));
    }

    public String getUserJobResultId() {

	return getHeader().get(USER_JOB_RESULT_ID, String.class);
    }

    public void setUserJobResultId(String id) {

	getHeader().add(new GSProperty<String>(USER_JOB_RESULT_ID, id));
    }

    public StorageUri getDataBaseURI() {

	return getHeader().get(DATABASE_URL, StorageUri.class);
    }

    public void setDataBaseURI(StorageUri url) {

	getHeader().add(new GSProperty<StorageUri>(DATABASE_URL, url));
    }

    public SharedRepositoryInfo getSharedRepositoryInfo() {

	return getHeader().get(SHARED_REPO_INFO, SharedRepositoryInfo.class);
    }

    public void setSharedRepositoryInfo(SharedRepositoryInfo info) {

	getHeader().add(new GSProperty<SharedRepositoryInfo>(SHARED_REPO_INFO, info));
    }

    public WebRequest getWebRequest() {

	return getHeader().get(WEB_REQUEST, WebRequest.class);
    }

    public void setWebRequest(WebRequest request) {

	if (request != null) {

	    setHeaderProperty(new GSProperty<WebRequest>(WEB_REQUEST, request));

	    setRequestId(request.getRequestId());

	    if (request.getUriInfo() != null && request.getUriInfo().getAbsolutePath() != null) {

		String requestAbsolutePath = request.getUriInfo().getAbsolutePath().toString();
		setRequestAbsolutePath(requestAbsolutePath);
	    }
	}
    }

    protected void setHeaderProperty(GSProperty<?> property) {
	String name = property.getName();
	Object value = property.getValue();

	getHeader().remove(name);
	if (value != null) {
	    getHeader().add(property);
	}
    }

    protected void setPayloadProperty(GSProperty<?> property) {
	String name = property.getName();
	Object value = property.getValue();

	getPayload().remove(name);
	if (value != null) {
	    getHeader().add(property);
	}
    }

    public Optional<GSUser> getCurrentUser() {

	return Optional.ofNullable(getHeader().get(CURRENT_USER, GSUser.class));
    }

    public void setCurrentUser(GSUser user) {

	getHeader().add(new GSProperty<GSUser>(CURRENT_USER, user));
    }

    public Optional<View> getView() {

	return Optional.ofNullable(getHeader().get(VIEW, View.class));
    }

    public void setView(View view) {

	getHeader().add(new GSProperty<View>(VIEW, view));
    }

    public Boolean getScheduled() {

	return getHeader().get(SCHEDULED, Boolean.class);
    }

    public void setScheduled(Boolean scheduled) {

	getHeader().add(new GSProperty<Boolean>(SCHEDULED, scheduled));
    }

    /**
     * @return
     */
    public boolean isOutputSources() {

	return getHeader().get(OUTPUT_SOURCES, Boolean.class);
    }

    /**
     * If set to <code>true</code> the output content of this discovery query must be generated from
     * {@link #getSources()} instead of from the {@link ResultSet}
     * S
     * 
     * @param set
     */
    public void setOutputSources(boolean set) {

	getHeader().add(new GSProperty<Boolean>(OUTPUT_SOURCES, set));
    }

    /**
     * Retrieves the ordered list of sources related to this discovery message
     * Default value: empty {@link GSSource}s list
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<GSSource> getSources() {

	return getHeader().get(SOURCES, List.class);
    }

    /**
     * Sets the ordered list of sources related to this discovery message
     * 
     * @param sources
     */
    public void setSources(List<GSSource> sources) {

	getHeader().add(new GSProperty<List<GSSource>>(SOURCES, sources));
    }

    /**
     * Default value: a non null {@link GSException}
     * 
     * @return
     */
    public GSException getException() {

	return getHeader().get(EXCEPTION, GSException.class);
    }

    private void setException(GSException exception) {

	getHeader().add(new GSProperty<GSException>(EXCEPTION, exception));
    }

    public void setPage(Page page) {

	getHeader().add(new GSProperty<Page>(PAGE, page));
    }

    public Page getPage() {

	return getHeader().get(PAGE, Page.class);
    }

    public void setRequestAbsolutePath(String requestAbsolutePath) {
	getHeader().add(new GSProperty<String>(REQUEST_ABSOLUTE_PATH, requestAbsolutePath));
    }

    public String getRequestAbsolutePath() {
	return getHeader().get(REQUEST_ABSOLUTE_PATH, String.class);
    }

    /**
     * @return
     */
    public boolean isIteratedWorkflow() {

	Boolean out = getHeader().get(ITERATED_WORKFLOW, Boolean.class);
	return Objects.isNull(out) ? false : out;
    }

    /**
     * 
     */
    public void setIteratedWorkflow() {

	getHeader().add(new GSProperty<Boolean>(ITERATED_WORKFLOW, true));
    }
}
