/**
 * 
 */
package eu.essi_lab.lib.net.keycloak;

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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class KeycloakUsersManager {

    private String serviceUrl;
    private String adminRealm;
    private String usersRealm;
    private String clientId;
    private String adminUser;
    private String adminPassword;
    private HttpClient httpClient;

    /**
     * 
     */
    public KeycloakUsersManager() {

	httpClient = HttpClient.newHttpClient();

	setAdminRealm("master");
	setClientId("admin-cli");
    }

    /**
     * @return
     */
    public String getServiceUrl() {

	return serviceUrl;
    }

    /**
     * @param serviceUrl
     */
    public void setServiceUrl(String serviceUrl) {

	this.serviceUrl = serviceUrl;
    }

    /**
     * @return
     */
    public String getAdminRealm() {

	return adminRealm;
    }

    /**
     * @param adminRealm
     */
    public void setAdminRealm(String adminRealm) {

	this.adminRealm = adminRealm;
    }

    /**
     * @return
     */
    public String getUsersRealm() {

	return usersRealm;
    }

    /**
     * @param usersRealm
     */
    public void setUsersRealm(String usersRealm) {

	this.usersRealm = usersRealm;
    }

    /**
     * @return
     */
    public String getClientId() {

	return clientId;
    }

    /**
     * @param clientId
     */
    public void setClientId(String clientId) {

	this.clientId = clientId;
    }

    /**
     * @return
     */
    public String getAdminUser() {

	return adminUser;
    }

    /**
     * @param adminUser
     */
    public void setAdminUser(String adminUser) {

	this.adminUser = adminUser;
    }

    /**
     * @return
     */
    public String getAdminPassword() {

	return adminPassword;
    }

    /**
     * @param adminPassword
     */
    public void setAdminPassword(String adminPassword) {

	this.adminPassword = adminPassword;
    }

    /**
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getAccessToken() throws IOException, InterruptedException {

	String tokenUrl = serviceUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

	String form = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&username="
		+ URLEncoder.encode(adminUser, StandardCharsets.UTF_8) + "&password="
		+ URLEncoder.encode(adminPassword, StandardCharsets.UTF_8) + "&grant_type=password";

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(tokenUrl)).header("Content-Type", "application/x-www-form-urlencoded")
		.POST(HttpRequest.BodyPublishers.ofString(form)).build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred: {}", response.body());

	    throw new IOException("Error occurred: " + response.body());
	}

	return response.body().replaceAll(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }

    /**
     * @param accessToken
     * @throws IOException
     * @throws InterruptedException
     */
    public List<KeycloakUser> list(String accessToken) throws IOException, InterruptedException {

	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users";

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken).GET().build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	JSONArray jsonArray = new JSONArray(response.body());

	return jsonArray.toList().//
		stream().//
		map(o -> KeycloakUser.of(new JSONObject((HashMap<?, ?>) o))).//
		collect(Collectors.toList());
    }

    /**
     * @param accessToken
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public int count(String accessToken) throws IOException, InterruptedException {

	return list(accessToken).size();
    }

    /**
     * @param accessToken
     * @param userName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Optional<String> findId(String accessToken, String userName) throws IOException, InterruptedException {

	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users?username=" + URLEncoder.encode(userName, StandardCharsets.UTF_8);

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken).GET().build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {

	    GSLoggerFactory.getLogger(getClass()).error("Error occurred: {}", response.body());

	    throw new RuntimeException("Error occurred: " + response.body());
	}

	String body = response.body();

	String id = body.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

	return id.equals("[]") ? Optional.empty() : Optional.of(id);
    }

    /**
     * @param accessToken
     * @param username
     * @param email
     * @param attributes
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean create(String accessToken, KeycloakUser user) throws IOException, InterruptedException {

	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users";

	HttpRequest request = HttpRequest.newBuilder().//
		uri(URI.create(url)).//
		header("Authorization", "Bearer " + accessToken).//
		header("Content-Type", "application/json").//
		POST(HttpRequest.BodyPublishers.ofString(user.toJSON().toString())).//
		build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	return response.statusCode() == 201;
    }

    /**
     * @param accessToken
     * @param user
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean update(String accessToken, KeycloakUser user) throws IOException, InterruptedException {

	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users/" + user.getIdentifier().get();

	HttpRequest request = HttpRequest.newBuilder().//
		uri(URI.create(url)).//
		header("Authorization", "Bearer " + accessToken).//
		header("Content-Type", "application/json").//
		PUT(HttpRequest.BodyPublishers.ofString(user.toJSON().toString())).//
		build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	return response.statusCode() == 204;
    }

    /**
     * @param accessToken
     * @param userId
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean delete(String accessToken, String userId) throws IOException, InterruptedException {

	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users/" + userId;

	HttpRequest request = HttpRequest.newBuilder().//
		uri(URI.create(url)).//
		header("Authorization", "Bearer " + accessToken).//
		DELETE().//
		build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	return response.statusCode() == 204;
    }
}
