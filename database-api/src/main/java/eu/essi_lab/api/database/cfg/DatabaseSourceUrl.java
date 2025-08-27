/**
 * 
 */
package eu.essi_lab.api.database.cfg;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.model.StorageInfo;

/**
 * This particular URL provides all the required info to initialize a database source<br>
 * <br>
 * For <b>MarkLogic</b> it must be like this: "xdbc://user:password@hostname:8000,8004/dbName/configFolder/" where
 * 'configFolder' indicates the db folder where the configuration is stored. The configuration name is not necessary
 * since it is hard-coded.<br>
 * E.g.: "xdbc://user:password@productionhost:8000,8004/PRODUCTION-DB/production/"<br>
 * <br>
 * For <b>OpenSearch</b> it can have three kind of protocols: <i>osm</i>, <i>oss</i>, <i>osl</i> that respectively
 * means
 * <i>OpenSearch Managed</i>, <i>OpenSearch Serverless</i> and <i>OpenSearch Local</i>. The uri must be like
 * this:<br>
 * "osm://awsaccesskey:awssecretkey@hostname/environment/configName" where <code>environment</code> and
 * <code>configName</code>
 * can have different values according to the target environment such as test, production, preproduction, etc...<br>
 * For example, <code>environment</code> could be 'test', 'prod', 'preprod' and <code>configName</code> can be
 * 'testCondig', 'prodConfig' or 'preprodConfig'.<br>
 * E.g.: "osm://awsaccesskey:awssecretkey@productionhost/prod/prodConfig"<br>
 * E.g.: "oss://awsaccesskey:awssecretkey@preproductionhost/preprod/preProdConfig"<br>
 * E.g.: "osl://awsaccesskey:awssecretkey@localhost:9200/test/testConfig"<br>
 * 
 * @author Fabrizio
 */
public class DatabaseSourceUrl {

    /**
     * @param url
     * @return
     */
    public static DatabaseImpl detectImpl(String url) {

	if (url.startsWith("xdbc")) {

	    return DatabaseImpl.MARK_LOGIC;
	}

	return OpenSearchServiceType.//
		protocols().//
		stream().//
		anyMatch(p -> url.startsWith(p)) ? DatabaseImpl.OPENSEARCH : null;
    }

    /**
     * @param uri
     * @return
     */
    public static boolean check(String uri) {

	return uri.startsWith("xdbc") || //
		OpenSearchServiceType.protocols().stream().anyMatch(p -> uri.startsWith(p));
    }

    /**
     * E.g: "xdbc://user:password@hostname:8000,8004/dbName/folder/"
     * E.g: "osm://awsaccesskey:awssecretkey@https:productionhost/prod/prodConfig"
     * E.g: "oss://awsaccesskey:awssecretkey@https:productionhost/prod/prodConfig"
     * E.g: "osl://awsaccesskey:awssecretkey@http:localhost:9200/test/testConfig"
     * E.g: "osl://awsaccesskey:awssecretkey@https:localhost:9200/test/testConfig"
     * 
     * @param url
     * @return
     * @throws URISyntaxException
     */
    public static StorageInfo build(String url) throws URISyntaxException {

	StorageInfo info = new StorageInfo();

	if (url.startsWith("xdbc")) {

	    String xdbc = url.replace("xdbc://", "xdbc_");

	    // uri --> xdbc://hostname:8000,8004
	    String uri = "xdbc://" + xdbc.substring(xdbc.indexOf("@") + 1, xdbc.indexOf("/"));
	    String user = xdbc.substring(xdbc.indexOf("_") + 1, xdbc.indexOf(":"));
	    String password = xdbc.substring(xdbc.indexOf(":") + 1, xdbc.indexOf("@"));

	    xdbc = xdbc.substring(xdbc.indexOf("/") + 1); // dnName/defaultConf/
	    String dbName = xdbc.substring(0, xdbc.indexOf("/"));
	    String folder = xdbc.substring(xdbc.indexOf("/") + 1, xdbc.lastIndexOf("/"));

	    info.setUri(uri);
	    info.setUser(user);
	    info.setPassword(password);
	    info.setIdentifier(folder);
	    info.setName(dbName);

	} else {

	    URI uri = new URI(url);

	    String env = uri.getPath().split("/+")[1];
	    String configName = uri.getPath().split("/+")[2];

	    String userInfo = uri.getAuthority().substring(0, uri.getAuthority().indexOf("@"));
	    String accessKey = userInfo.split(":")[0];
	    String secretKey = userInfo.split(":")[1];

	    OpenSearchServiceType serviceType = OpenSearchServiceType.decode(uri.getScheme());

	    String myURI = url.split("@")[1].split("/")[0].replace("http:", "http://").replace("https:", "https://");

	    info = new StorageInfo(myURI);
	    info.setType(serviceType.getProtocol());

	    info.setIdentifier(env);
	    info.setName(configName);

	    info.setUser(accessKey);
	    info.setPassword(secretKey);
	}

	return info;
    }
}
