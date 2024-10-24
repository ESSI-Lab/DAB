package eu.essi_lab.accessor.acronet;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.media.jai.Interpolation;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class AcronetClient {

    /**
     * @author Roberto
     */

    private static final String ACRONET_CREDENTIALS_MISSING_ERROR = "ACRONET_CREDENTIALS_MISSING_ERROR";
    private static final String ACRONET_CREDENTIALS_INVALID_ERROR = "ACRONET_CREDENTIALS_INVALID_ERROR";

    private static final String TOKEN_ENDPOINT = "https://testauth.cimafoundation.org/auth/realms/webdrops/protocol/openid-connect/token";

    static final String VARIABLE_PATH = "sensors/classes/";
    static final String SENSORS_PATH = "sensors/list/";
    static final String DATA_PATH = "sensors/data/";
    static final String STATIONGROUP = "ComuneLive%25IChange";

    private String endpoint;
    private String user;
    private String password;

    public AcronetClient() {

    }

    public AcronetClient(String endpoint) {
	this.endpoint = endpoint;
    }

    /**
     * @return the user
     */
    public String getUser() {
	if (user == null)
	    user = ConfigurationWrapper.getCredentialsSetting().getACRONETUser().orElse(null);
	return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
	this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
	if (password == null)
	    password = ConfigurationWrapper.getCredentialsSetting().getACRONETPassword().orElse(null);
	return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
	this.password = password;
    }

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    /**
     * @return
     * @throws IOException
     * @throws GSException
     */
    public String getToken() throws GSException {

	String user = getUser();
	String password = getPassword();

	String token = null;

	if (user == null || password == null) {

	    throw GSException.createException(//
		    getClass(), //
		    "Acronet user and/or password missing", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACRONET_CREDENTIALS_MISSING_ERROR);
	}

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("grant_type", "password");
	params.put("username", user);
	params.put("password", password);
	params.put("client_id", ConfigurationWrapper.getCredentialsSetting().getACRONETClientId().orElse(null));
	params.put("client_secret", ConfigurationWrapper.getCredentialsSetting().getACRONETClientPassword().orElse(null));

	try {
	    Downloader executor = new Downloader();

	    HttpRequest postRequest = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    TOKEN_ENDPOINT, //
		    params);

	    HttpResponse<InputStream> response = executor.downloadResponse(postRequest);
	    int statusCode = response.statusCode();
	    if (statusCode > 400) {
		GSLoggerFactory.getLogger(AcronetClient.class).info("ERROR getting TOKEN: " + statusCode);
		// try again
		throw GSException.createException(//
			getClass(), //
			"Invalid credentials, token retrieval forbidden", //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			ACRONET_CREDENTIALS_INVALID_ERROR);

	    }
	    String result = IOUtils.toString(response.body(), "UTF-8");

	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		token = obj.optString("access_token");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(AcronetClient.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}

	return token;
    }

    /**
     * @return
     * @return
     * @throws IOException
     * @throws GSException
     */
    private Optional<String> downloadString(String request) throws Exception {

	String token = getToken();
	Downloader downloader = new Downloader();

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Authorization", "Bearer " + token);

	return downloader.downloadOptionalString(request, HttpHeaderUtils.build(headers));
    }

    /**
     * @param variable
     * @param stationId
     * @param begin
     * @param end
     * @return JSONArray data
     * @throws Exception
     */
    
    public JSONObject getData(String variable, String stationId, String dateBegin, String dateEnd) throws Exception {
	String response = downloadString(getEndpoint() + DATA_PATH + variable + "/" + stationId + "/?from=" + dateBegin + "&to=" + dateEnd
		+ "&aggr=1H&date_as_string=true").get();
	JSONArray arr = new JSONArray(response);
	
	return arr.optJSONObject(0);
    }

    public List<JSONObject> getData(String variable, Date dateBegin, Date dateEnd) {
	List<JSONObject> ret = new ArrayList<JSONObject>();
	return ret;
    }

    public JSONArray getVariables() throws Exception {
	JSONArray ret = null;
	String response = downloadString(getEndpoint() + VARIABLE_PATH).get();
	ret = new JSONArray(response);
	return ret;
    }

    public List<JSONObject> getSensors(String variable) throws Exception {
	List<JSONObject> ret = new ArrayList<JSONObject>();
	String response = downloadString(getEndpoint() + SENSORS_PATH + variable + "/?stationgroup=" + STATIONGROUP).get();
	JSONArray arr = new JSONArray(response);
	if (arr != null && !arr.isEmpty()) {
	    for (int k = 0; k < arr.length(); k++) {
		ret.add(arr.optJSONObject(k));
	    }
	}
	return ret;
    }

}
