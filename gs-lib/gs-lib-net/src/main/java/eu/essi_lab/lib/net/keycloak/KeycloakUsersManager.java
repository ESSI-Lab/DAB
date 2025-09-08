/**
 * 
 */
package eu.essi_lab.lib.net.keycloak;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 
     */
    public KeycloakUsersManager() {

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
    public String getAdminAccessToken() throws IOException, InterruptedException {

	String tokenUrl = serviceUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

	String form = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&username="
		+ URLEncoder.encode(adminUser, StandardCharsets.UTF_8) + "&password="
		+ URLEncoder.encode(adminPassword, StandardCharsets.UTF_8) + "&grant_type=password";

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(tokenUrl)).header("Content-Type", "application/x-www-form-urlencoded")
		.POST(HttpRequest.BodyPublishers.ofString(form)).build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {
	    throw new RuntimeException("Errore nel login admin: " + response.body());
	}

	return response.body().replaceAll(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }

    /**
     * @param accessToken
     * @throws IOException
     * @throws InterruptedException
     */
    public List<JSONObject> listUsers(String accessToken) throws IOException, InterruptedException {

	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users";

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken).GET().build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	JSONArray jsonArray = new JSONArray(response.body());

	return jsonArray.toList().stream().map(o -> new JSONObject((HashMap<?, ?>)o)).collect(Collectors.toList());
    }

    /**
     * @param accessToken
     * @param username
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String findUserIdByUsername(String accessToken, String username) throws IOException, InterruptedException {
	
	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken).GET().build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {
	    
	    throw new RuntimeException("Errore nella ricerca utente: " + response.body());
	}

	String body = response.body();

	String userId = body.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
	return userId;
    }

    /**
     * @param accessToken
     * @param username
     * @param email
     * @throws IOException
     * @throws InterruptedException
     */
    public void createUser(String accessToken, String username, String email) throws IOException, InterruptedException {
	
	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users";

	String json = """
		{
		  "username": "%s",
		  "enabled": true,
		  "email": "%s"
		}
		""".formatted(username, email);

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken)
		.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	
	System.out.println(response.statusCode());
    }

    /**
     * @param accessToken
     * @param userId
     * @param newFirstName
     * @param newLastName
     * @throws IOException
     * @throws InterruptedException
     */
    public void updateUser(String accessToken, String userId, String newFirstName, String newLastName)
	    throws IOException, InterruptedException {
	
	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users/" + userId;

	String json = """
		{
		  "firstName": "%s",
		  "lastName": "%s"
		}
		""".formatted(newFirstName, newLastName);

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken)
		.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(json)).build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	
	System.out.println(response.statusCode());
    }

    /**
     * @param accessToken
     * @param userId
     * @throws IOException
     * @throws InterruptedException
     */
    public void deleteUser(String accessToken, String userId) throws IOException, InterruptedException {
	
	String url = serviceUrl + "/admin/realms/" + usersRealm + "/users/" + userId;

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken).DELETE()
		.build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	
	System.out.println(response.statusCode());
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	KeycloakUsersManager manager = new KeycloakUsersManager();
	manager.setServiceUrl("http://localhost:8080");
	manager.setAdminPassword("Sghibbione_77!");
	manager.setAdminUser("fabriadmin");
	manager.setUsersRealm("myrealm");

	String token = manager.getAdminAccessToken();

	List<JSONObject> listUsers = manager.listUsers(token);
	
	System.out.println(listUsers.stream().map(o -> o.toString(3)).collect(Collectors.toList()));
	
	System.out.println(manager.findUserIdByUsername(token, "fabrizio"));

	// manager.createUser(token, "mario.rossi", "mario.rossi@example.com");

	// String userId = manager.findUserIdByUsername(token, "mario.rossi");
	// System.out.println("User ID di mario.rossi: " + userId);
	//
	// manager.updateUser(token, userId, "Mario", "Rossi");
	//
	// manager.deleteUser(token, userId);
    }
}
