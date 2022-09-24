package eu.essi_lab.gssrv.health.db;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseProvider;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.internal.Folder;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class HCDatabaseReader implements DatabaseReader, DatabaseProvider {

    private static final String HC_DB_ID = "HC_DB_ID";
    private static final String BAD_HEALTH_CHECK_MESSAGE = "BAD_HEALTH_CHECK_MESSAGE";

    @Override
    public String initialize(StorageUri dbUri, String suiteIdentifier) throws GSException {

	return HC_DB_ID;
    }

    @Override
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {

	checkSources(message);

	GSResource gsResource = new Dataset();

	gsResource.setSource(message.getSources().get(0));

	ResultSet<GSResource> resultSet = new ResultSet<>();

	resultSet.setResultsList(Arrays.asList(gsResource));

	return resultSet;
    }

    @Override
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException {

	return null;
    }

    @Override
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	return null;
    }

    @Override
    public DiscoveryCountResponse count(DiscoveryMessage message) throws GSException {

	checkSources(message);

	DiscoveryCountResponse dcr = new DiscoveryCountResponse();
	dcr.setCount(1);

	return dcr;
    }

    private void checkSources(DiscoveryMessage message) throws GSException {

	if (message.getSources().isEmpty()) {

	    throw GSException.createException(//
		    HCDatabaseReader.class, //
		    "Health Check DB expects a message with at list one harvested or mixed source", //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BAD_HEALTH_CHECK_MESSAGE);
	}
    }

    @Override
    public Optional<GSUser> getUser(String userName) throws GSException {
	return Optional.empty();
    }

    @Override
    public List<GSUser> getUsers() throws GSException {
	return null;
    }

    @Override
    public Optional<View> getView(String viewId) throws GSException {
	return Optional.empty();
    }

    @Override
    public List<String> getViewIdentifiers(int start, int count) throws GSException {
	return null;
    }

    @Override
    public List<String> getViewIdentifiers(int start, int count, String creator) throws GSException {
	return null;
    }

    @Override
    public boolean resourceExists(IdentifierType identifierType, String identifier) throws GSException {
	return false;
    }

    @Override
    public List<GSResource> getResources(IdentifierType identifierType, String identifier) throws GSException {
	return null;
    }

    @Override
    public boolean resourceExists(String originalIdentifier, GSSource source) throws GSException {
	return false;
    }

    @Override
    public GSResource getResource(String originalIdentifier, GSSource source) throws GSException {
	return null;
    }

    @Override
    public SemanticCountResponse count(SemanticMessage message) throws GSException {
	return null;
    }

    @Override
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {
	return null;
    }

    @Override
    public SemanticResponse<GSKnowledgeResourceDescription> execute(SemanticMessage message) throws GSException {
	return null;
    }

    @Override
    public Optional<GSKnowledgeResourceDescription> getKnowlegdeResource(GSKnowledgeScheme scheme, String subjectId) throws GSException {
	return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setDatabase(Database dataBase) {

    }

    @Override
    public boolean supports(StorageUri dbUri) {
	return dbUri instanceof HCDBStorageURI;
    }

    @Override
    public StorageUri getStorageUri() {
	return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Database getDatabase() {
	return new HCDataBase();
    }

    @Override
    public void release() throws GSException {

    }

    @Override
    public Optional<Folder> getFolder(String folderName, boolean createIfNotExist) throws GSException {

	return Optional.empty();
    }
}
