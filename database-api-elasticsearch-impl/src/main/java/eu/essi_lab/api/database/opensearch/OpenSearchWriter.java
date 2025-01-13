/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OpenSearchWriter extends DatabaseWriter {

    @Override
    public void setDatabase(Database dataBase) {
	// TODO Auto-generated method stub

    }

    @Override
    public Database getDatabase() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean supports(StorageInfo dbUri) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void remove(GSResource resource) throws GSException {
	// TODO Auto-generated method stub

    }

    @Override
    public void storeRDF(Node rdf) throws GSException {
	// TODO Auto-generated method stub

    }

    @Override
    public void remove(String propertyName, String propertyValue) throws GSException {
	// TODO Auto-generated method stub

    }

}
