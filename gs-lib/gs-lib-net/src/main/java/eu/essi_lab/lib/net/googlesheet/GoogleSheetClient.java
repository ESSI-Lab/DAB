package eu.essi_lab.lib.net.googlesheet;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

/**
 * Generic Google Sheet client
 * 
 * @author boldrini
 */
public abstract class GoogleSheetClient {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private String spreadsheetId;
    private String range;
    private Sheets service;

    /**
     * @throws Exception
     */
    public GoogleSheetClient() throws Exception {
	NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

	GoogleCredentials credentials = getCredentials(HTTP_TRANSPORT);

	HttpRequestInitializer init = null;
	if (credentials != null) {

	    init = new HttpCredentialsAdapter(getCredentials(HTTP_TRANSPORT));
	}

	this.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, init).setApplicationName(getApplicationName()).build();
    }

    /**
     * @return the service
     */
    public Sheets getService() {

	return service;
    }

    /**
     * @return
     */
    public String getRange() {
	return range;
    }

    /**
     * @param range
     */
    public void setRange(String range) {
	this.range = range;
    }

    /**
     * @return
     */
    public String getSpreadsheetId() {
	return spreadsheetId;
    }

    /**
     * @param spreadsheetId
     */
    public void setSpreadsheetId(String spreadsheetId) {
	this.spreadsheetId = spreadsheetId;
    }

    /**
     * @return
     */
    private Set<String> googleOAuth2Scopes() {
	Set<String> googleOAuth2Scopes = new HashSet<>();
	googleOAuth2Scopes.add(SheetsScopes.SPREADSHEETS);
	return Collections.unmodifiableSet(googleOAuth2Scopes);
    }

    /**
     * @return
     */
    protected abstract String getApplicationName();

    /**
     * @return
     */
    protected abstract Optional<String> getCredentialsFilePath();

    /**
     * @return
     * @throws IOException
     */
    public ValueRange getData() throws IOException {
	return service.spreadsheets().values().get(spreadsheetId, range).execute();
    }

    /**
     * Creates an authorised Credential object.
     *
     * @param httpTransport The network HTTP Transport.
     * @return An authorised Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private GoogleCredentials getCredentials(NetHttpTransport httpTransport) throws IOException {

	Optional<String> filePath = getCredentialsFilePath();

	if (filePath.isEmpty()) {

	    return null;
	}

	InputStream in = getClass().getClassLoader().getResourceAsStream(filePath.get());

	if (in == null) {
	    throw new FileNotFoundException("Resource not found: " + filePath.get());
	}

	return ServiceAccountCredentials.fromStream(in).createScoped(googleOAuth2Scopes());
    }

}
