package eu.essi_lab.api.database.marklogic;

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

import java.io.InputStream;
import java.util.Date;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.wrapper.marklogic.MarkLogicWrapper;

/**
 * @author Fabrizio
 */
public class MarkLogicFolder implements DatabaseFolder {

    /**
     * 
     */
    private static final int CLEARING_STEP = 10000;

    protected String uri;
    protected MarkLogicDatabase mlDataBase;

    public MarkLogicFolder(MarkLogicDatabase mlDB, String uri) {
	this.mlDataBase = mlDB;
	this.uri = uri;
    }

    @Override
    public String getURI() {

	return uri;
    }

    @Override
    public String getCompleteName() {

	String name = new String(uri);
	if (uri.startsWith("/")) {
	    name = name.substring(1, uri.length());
	}

	if (uri.endsWith("/")) {
	    name = name.substring(0, uri.length() - 2);
	}

	return name;
    }

    @Override
    public String getSimpleName() {

	String simpleName = getCompleteName();
	simpleName = simpleName.replace(mlDataBase.getIdentifier() + "_", "");
	simpleName = simpleName.replace(SourceStorageWorker.META_PREFIX, "");
	simpleName = simpleName.replace(SourceStorageWorker.DATA_1_PREFIX, "");
	simpleName = simpleName.replace(SourceStorageWorker.DATA_2_PREFIX, "");

	return simpleName;
    }

    @Override
    public boolean store(String key, Document doc) throws Exception {

	return mlDataBase.getWrapper().store(createResourceUri(uri, key), doc);
    }

    @Override
    public boolean replace(String key, Document doc) throws Exception {

	return mlDataBase.getWrapper().replace(createResourceUri(uri, key), doc);
    }

    @Override
    public boolean storeBinary(String key, InputStream res) throws Exception {

	return mlDataBase.getWrapper().storeBinary(createResourceUri(uri, key), res);
    }

    @Override
    public boolean storeBinary(String key, InputStream res, Date timeStamp) throws Exception {

	return mlDataBase.getWrapper().storeBinary(createResourceUri(uri, key), res, timeStamp);
    }

    @Override
    public boolean replaceBinary(String key, InputStream res) throws Exception {

	return mlDataBase.getWrapper().replaceBinary(createResourceUri(uri, key), res);
    }

    @Override
    public Node get(String key) throws Exception {

	return mlDataBase.getWrapper().get(createResourceUri(uri, key));
    }

    /**
     * See GIP-244 and test eu.essi_lab.api.database.marklogic.test.GIP_244_Test
     * Now non transactional method should be no longer required
     */
    @Override
    public InputStream getBinary(String key) throws Exception {

	return mlDataBase.getWrapper().getBinary(createResourceUri(uri, key));
    }

    @Override
    public Optional<Node> getBinaryProperties(String key) throws Exception {

	return mlDataBase.getWrapper().getBinaryProperties(createResourceUri(uri, key));
    }

    @Override
    public Optional<Date> getBinaryTimestamp(String key) throws Exception {

	Optional<Node> props = getBinaryProperties(key);

	if (props.isPresent()) {

	    XMLNodeReader reader = new XMLNodeReader(props.get());

	    String timeStampString = reader.evaluateString("//*[local-name()='" + MarkLogicWrapper.DOC_TIMESTAMP + "']");

	    if (timeStampString != null && !timeStampString.isEmpty()) {

		long timeStamp = Long.valueOf(timeStampString);

		return Optional.of(new Date(timeStamp));
	    }
	}

	return Optional.empty();
    }

    @Override
    public boolean remove(String key) throws Exception {

	return mlDataBase.getWrapper().remove(createResourceUri(uri, key));
    }

    @Override
    public boolean exists(String key) throws Exception {

	// the getBinary method is more general, the exists method is general
	// since it tests both xml docs and binaries
	return getBinary(key) != null;
    }

    @Override
    public String[] listKeys() throws RequestException {

	String xQuery = "cts:uris('',(),cts:directory-query(\"" + uri + "\"))";

	ResultSequence rs = mlDataBase.execXQuery(xQuery);

	String[] asStrings = rs.asStrings();

	for (int i = 0; i < asStrings.length; i++) {
	    asStrings[i] = asStrings[i].substring(uri.length(), asStrings[i].length());
	}

	return asStrings;
    }

    @Override
    public int size() throws RequestException {

	String xQuery = "xdmp:estimate(cts:search(doc(), cts:directory-query(\"" + uri + "\",'1')))";

	ResultSequence rs = mlDataBase.execXQuery(xQuery);
	return Integer.valueOf(rs.asString());
    }

    @Override
    public void clear() throws RequestException {

	GSLoggerFactory.getLogger(getClass()).debug("Clearing folder STARTED");

	int step = CLEARING_STEP;
	int size = size();
	int counter = 0;

	GSLoggerFactory.getLogger(getClass()).debug("Folder size: {}", StringUtils.format(size));

	while (counter < size) {

	    String xQuery = "for $i in cts:uris(\"" + uri + "\",'document', cts:directory-query('"+uri+"', 'infinity'))[1 to " + step + "] return xdmp:document-delete( $i )";
	    mlDataBase.execXQuery(xQuery);

	    counter += CLEARING_STEP;

	    if (counter < size) {

		double percentage = (((double) counter) / size) * 100;

		String status = "[" + StringUtils.format(counter) + "/" + StringUtils.format(size) + "] - " + StringUtils.format(percentage) + " %";

		GSLoggerFactory.getLogger(getClass()).debug("Status: {}", status);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Clearing folder ENDED");
    }

    private String createResourceUri(String uri, String key) {

	return uri + key;
    }
}
