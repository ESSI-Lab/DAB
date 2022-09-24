///**
// * 
// */
//package eu.essi_lab.authorization.kuzzle;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.Semaphore;
//
//import org.json.JSONObject;
//
//import eu.essi_lab.authorization.CloseableUserBaseClient;
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.model.auth.GSUser;
//import io.kuzzle.sdk.core.Collection;
//import io.kuzzle.sdk.core.Document;
//import io.kuzzle.sdk.core.Kuzzle;
//import io.kuzzle.sdk.listeners.ResponseListener;
//import io.kuzzle.sdk.responses.SecurityDocumentList;
//import io.kuzzle.sdk.security.AbstractSecurityDocument;
//import io.kuzzle.sdk.security.User;
//import io.kuzzle.sdk.state.States;
//
///**
// * @author Fabrizio
// */
//public class KuzzleClient implements CloseableUserBaseClient {
//
//    private String endpoint;
//    private String userName;
//    private String password;
//    private Kuzzle kuzzle;
//
//    /**
//     * @return
//     */
//    public Kuzzle getKuzzle() {
//	return kuzzle;
//    }
//
//    /**
//     * @return
//     */
//    public String getEndpoint() {
//	return endpoint;
//    }
//
//    /**
//     * @param endpoint
//     */
//    public void setEndpoint(String endpoint) {
//	this.endpoint = endpoint;
//    }
//
//    /**
//     * @return
//     */
//    public String getUserName() {
//	return userName;
//    }
//
//    /**
//     * @param userName
//     */
//    public void setUserName(String userName) {
//	this.userName = userName;
//    }
//
//    /**
//     * @return
//     */
//    public String getPassword() {
//	return password;
//    }
//
//    /**
//     * @param password
//     */
//    public void setPassword(String password) {
//	this.password = password;
//    }
//
//    @Override
//    public Optional<GSUser> getUser(String userName) throws Exception {
//
//	final List<String> roles = new ArrayList<String>();
//
//	final List<String> errorMessage = new ArrayList<String>();
//
//	Semaphore semaphore = new Semaphore(0);
//
//	getUserRoles(userName, new ResponseListener<List<String>>() {
//
//	    @Override
//	    public void onSuccess(List<String> response) {
//
//		roles.addAll(response);
//
//		semaphore.release();
//	    }
//
//	    @Override
//	    public void onError(JSONObject error) {
//
//		errorMessage.add(error.toString());
//
//		semaphore.release();
//	    }
//	});
//
//	semaphore.acquire();
//
//	if (!errorMessage.isEmpty()) {
//
//	    throw new Exception(errorMessage.get(0));
//	}
//
//	if (roles.isEmpty()) {
//
//	    return Optional.empty();
//	}
//
//	GSUser user = new GSUser();
//	user.setIdentifier(userName);
//	user.setRole(roles.get(0));
//
//	return Optional.of(user);
//    }
//
//    @Override
//    public List<GSUser> getUsers() throws Exception {
//
//	return null;
//    }
//
//    @Override
//    public void close() throws IOException {
//
//	exit();
//    }
//
//    /**
//     * @param userName
//     * @param listener
//     * @throws Exception
//     */
//    public void getUserRoles(String userName, ResponseListener<List<String>> listener) throws Exception {
//
//	searchUsers(new ResponseListener<SecurityDocumentList>() {
//
//	    @Override
//	    public void onSuccess(SecurityDocumentList response) {
//
//		List<AbstractSecurityDocument> documents = response.getDocuments();
//
//		int failedCount = 0;
//
//		for (AbstractSecurityDocument doc : documents) {
//
//		    User user = (User) doc;
//
//		    String kuid = user.getId();
//
//		    if (kuid.equals(userName)) {
//
//			kuzzle.security.getCredentials("local", kuid, new ResponseListener<JSONObject>() {
//
//			    @Override
//			    public void onSuccess(JSONObject response) {
//
//				String[] profileIds = user.getProfileIds();
//
//				listener.onSuccess(Arrays.asList(profileIds));
//			    }
//
//			    @Override
//			    public void onError(JSONObject error) {
//
//				GSLoggerFactory.getLogger(getClass()).error(error.toString(3));
//
//				listener.onError(error);
//			    }
//			});
//
//		    } else {
//
//			failedCount++;
//
//			if (failedCount == documents.size()) {
//
//			    GSLoggerFactory.getLogger(getClass()).error("User not found");
//
//			    listener.onError(new JSONObject("{'warn':'User not found'}"));
//			}
//		    }
//		}
//	    }
//
//	    @Override
//	    public void onError(JSONObject error) {
//
//		GSLoggerFactory.getLogger(getClass()).error(error.toString(3));
//
//		listener.onError(error);
//	    }
//	});
//    }
//
//    /**
//     * @param listener
//     * @throws Exception
//     */
//    public void getUserRights(final ResponseListener<JSONObject[]> listener) throws Exception {
//
//	kuzzle = new Kuzzle(endpoint);
//
//	kuzzle.connect();
//
//	while (kuzzle.getState() != States.CONNECTED) {
//
//	    Thread.sleep(1000);
//	}
//
//	GSLoggerFactory.getLogger(getClass()).debug("Connected to Kuzzle service");
//
//	JSONObject user = new JSONObject("{\"username\":\"" + userName + "\",\"password\":\"" + password + "\"}");
//
//	kuzzle.login("local", user, new ResponseListener<JSONObject>() {
//
//	    @Override
//	    public void onSuccess(JSONObject response) {
//
//		GSLoggerFactory.getLogger(getClass()).debug("Login succeeded");
//
//		kuzzle.getMyRights(listener);
//	    }
//
//	    @Override
//	    public void onError(JSONObject error) {
//
//		GSLoggerFactory.getLogger(getClass()).error("Login failed: ");
//		GSLoggerFactory.getLogger(getClass()).error(error.toString(3));
//
//		exit();
//	    }
//	});
//    }
//
//    /**
//     * @param listener
//     * @throws Exception
//     */
//    public void getUserIdentifiers(final List<String> identifiers) throws Exception {
//
//	kuzzle = new Kuzzle(endpoint);
//
//	kuzzle.connect();
//
//	while (kuzzle.getState() != States.CONNECTED) {
//
//	    Thread.sleep(1000);
//	}
//
//	GSLoggerFactory.getLogger(getClass()).debug("Connected to Kuzzle service");
//
//	JSONObject user = new JSONObject("{\"username\":\"" + userName + "\",\"password\":\"" + password + "\"}");
//
//	kuzzle.login("local", user, new ResponseListener<JSONObject>() {
//
//	    @Override
//	    public void onSuccess(JSONObject response) {
//
//		GSLoggerFactory.getLogger(getClass()).debug("Login succeeded");
//
//		System.out.println(response.toString(3));
//
//		// kuzzle.getMyRights(listener);
//	    }
//
//	    @Override
//	    public void onError(JSONObject error) {
//
//		GSLoggerFactory.getLogger(getClass()).error("Login failed: ");
//		GSLoggerFactory.getLogger(getClass()).error(error.toString(3));
//
//		exit();
//	    }
//	});
//    }
//
//    /**
//     * @param listener
//     * @throws Exception
//     */
//    public void searchUsers(final ResponseListener<SecurityDocumentList> listener) throws Exception {
//
//	kuzzle = new Kuzzle(endpoint);
//
//	kuzzle.connect();
//
//	while (kuzzle.getState() != States.CONNECTED) {
//
//	    Thread.sleep(1000);
//	}
//
//	GSLoggerFactory.getLogger(getClass()).debug("Connected to Kuzzle service");
//
//	JSONObject user = new JSONObject("{\"username\":\"" + userName + "\",\"password\":\"" + password + "\"}");
//
//	kuzzle.login("local", user, new ResponseListener<JSONObject>() {
//
//	    @Override
//	    public void onSuccess(JSONObject response) {
//
//		GSLoggerFactory.getLogger(getClass()).debug("Login succeeded");
//
//		kuzzle.security.searchUsers(new JSONObject(), listener);
//	    }
//
//	    @Override
//	    public void onError(JSONObject error) {
//
//		GSLoggerFactory.getLogger(getClass()).error("Login failed: ");
//		GSLoggerFactory.getLogger(getClass()).error(error.toString(3));
//
//		exit();
//	    }
//	});
//    }
//
//    /**
//     * @param docId
//     * @param listener
//     * @throws Exception
//     */
//    public void getDocument(String docId, ResponseListener<Document> listener) throws Exception {
//
//	kuzzle = new Kuzzle(endpoint);
//
//	kuzzle.connect();
//
//	while (kuzzle.getState() != States.CONNECTED) {
//
//	    Thread.sleep(1000);
//	}
//
//	GSLoggerFactory.getLogger(getClass()).debug("Connected to Kuzzle service");
//
//	JSONObject user = new JSONObject("{\"username\":\"" + userName + "\",\"password\":\"" + password + "\"}");
//
//	kuzzle.login("local", user, new ResponseListener<JSONObject>() {
//
//	    @Override
//	    public void onSuccess(JSONObject response) {
//
//		GSLoggerFactory.getLogger(getClass()).debug("Login succeeded");
//
//		Collection collection = new Collection(kuzzle, "testcollection1", "testindex");
//
//		collection.fetchDocument(docId, listener);
//	    }
//
//	    @Override
//	    public void onError(JSONObject error) {
//
//		GSLoggerFactory.getLogger(getClass()).error("Login failed: ");
//		GSLoggerFactory.getLogger(getClass()).error(error.toString(3));
//
//		exit();
//	    }
//	});
//    }
//
//    /**
//     * 
//     */
//    public void exit() {
//
//	kuzzle.logout(new ResponseListener<Void>() {
//
//	    @Override
//	    public void onSuccess(Void response) {
//
//		GSLoggerFactory.getLogger(getClass()).debug("Logout succeeded");
//
//		kuzzle.disconnect();
//	    }
//
//	    @Override
//	    public void onError(JSONObject error) {
//
//		GSLoggerFactory.getLogger(getClass()).error("Logout error:");
//		GSLoggerFactory.getLogger(getClass()).error(error.toString(3));
//
//		kuzzle.disconnect();
//	    }
//	});
//    }
//
//    public static void main(String[] args) throws Exception {
//
//	KuzzleClient kuzzleClient = new KuzzleClient();
//	kuzzleClient.setEndpoint("ec2-54-242-210-108.compute-1.amazonaws.com");
//	kuzzleClient.setUserName("sghibbione");
//	kuzzleClient.setPassword("sghibbione");
//
//	GSUser user = kuzzleClient.getUser("fabrizio.papeschi@cnr.it").get();
//
//	System.out.println("ROLE: " + user.getRole());
//
//	kuzzleClient.close();
//
//	// Sys tem.exit(0);
//
//	// kuzzleHandler.searchUsers(new ResponseListener<SecurityDocumentList>() {
//	//
//	// @Override
//	// public void onSuccess(SecurityDocumentList response) {
//	//
//	// List<AbstractSecurityDocument> documents = response.getDocuments();
//	// for (AbstractSecurityDocument doc : documents) {
//	//
//	// User user = (User) doc;
//	// JSONObject content = user.getContent();
//	//
//	// String id = user.getId();
//	// // String[] profileIds = user.getProfileIds();
//	//
//	// System.out.println("User " + id + " found");
//	//
//	// System.out.println(content.toString(3));
//	// }
//	// }
//	//
//	// @Override
//	// public void onError(JSONObject error) {
//	//
//	// System.out.println(error);
//	// }
//	// });
//
//	// kuzzleHandler.getUserRights(new ResponseListener<JSONObject[]>() {
//	//
//	// @Override
//	// public void onSuccess(JSONObject[] response) {
//	//
//	// GSLoggerFactory.getLogger(getClass()).debug("GetMyRights succeeded");
//	//
//	// for (JSONObject jsonObject : response) {
//	// System.out.println(jsonObject);
//	// }
//	// }
//	//
//	// @Override
//	// public void onError(JSONObject error) {
//	//
//	// System.out.println("GetMyRights error");
//	// System.out.println(error);
//	// }
//	// });
//
//	// kuzzleHandler.getDocument("doc1", new ResponseListener<Document>() {
//	//
//	// @Override
//	// public void onSuccess(Document response) {
//	//
//	// GSLoggerFactory.getLogger(getClass()).debug("Get doc1 succeeded");
//	//
//	// System.out.println(response.getContent().toString(3));
//	//
//	// kuzzleHandler.exit();
//	// }
//	//
//	// @Override
//	// public void onError(JSONObject error) {
//	//
//	// System.out.println(error.toString(3));
//	//
//	// kuzzleHandler.exit();
//	// }
//	// });
//
//    }
//}
