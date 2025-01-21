package eu.essi_lab.api.database.marklogic;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.Optional;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.messages.bond.View.ViewVisibility;

/**
 * @author Fabrizio
 */
public class ListViewIdsQueryHandler {

    /**
     * @param viewFolder
     * @param request
     * @return
     */
    static String getListViewIdsQuery(DatabaseFolder viewFolder, GetViewIdentifiersRequest request) {

	//
	// !creator !owner !visib
	// !creator !owner visib
	// !creator owner !visib
	// !creator owner visib
	// creator !owner !visib
	// creator !owner visib
	// creator owner !visib
	// creator owner visib
	//

	Optional<String> creator = request.getCreator();
	Optional<String> owner = request.getOwner();
	Optional<ViewVisibility> visibility = request.getVisibility();

	if (!creator.isPresent() && !owner.isPresent() && !visibility.isPresent()) {

	    // it should not happen
	    return null;
	}

	if (!creator.isPresent() && !owner.isPresent() && visibility.isPresent()) {

	    // not supported yet
	    return null;
	}

	if (!creator.isPresent() && owner.isPresent() && !visibility.isPresent()) {

	    return getViewIdsByOwnerQuery(viewFolder, owner.get());
	}

	if (!creator.isPresent() && owner.isPresent() && visibility.isPresent()) {

	    // not supported yet
	    return null;
	}

	if (creator.isPresent() && !owner.isPresent() && !visibility.isPresent()) {

	    return getViewIdsByCreatorQuery(viewFolder, creator.get());
	}

	if (creator.isPresent() && !owner.isPresent() && visibility.isPresent()) {

	    return getViewIdsByCreatorAndVisibilityQuery(viewFolder, creator.get(), visibility.get());
	}

	if (creator.isPresent() && owner.isPresent() && !visibility.isPresent()) {

	    return getViewIdsByOwnerAndCreatorQuery(viewFolder, owner.get(), creator.get());
	}

	if (creator.isPresent() && owner.isPresent() && visibility.isPresent()) {

	    return getViewIdsByOwnerAndCreatorAndVisibilityQuery(viewFolder, owner.get(), creator.get(), visibility.get());
	}

	return null;
    }

    /**
     * @param viewFolder
     * @param creator
     * @return
     */
    private static String getViewIdsByCreatorQuery(DatabaseFolder viewFolder, String creator) {

	String query = "xquery version \"1.0-ml\"; \n";

	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\"; \n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\"; \n";

	query += "let $creator := '" + creator + "' \n";

	query += "for $uris in cts:uris((),(), cts:directory-query(\"/" + viewFolder.getName() + "/\", \"infinity\")   ) \n";

	query += "let $doc :=  fn:document($uris)  \n";

	query += "let $xml := if ($doc/node() instance of binary()) then (xdmp:binary-decode(fn:doc($uris)/node(), \"UTF-8\")) else $doc \n";

	query += "where (fn:contains($xml,fn:concat('<creator>',$creator,'</creator>')) or $doc//creator = $creator) return xdmp:unquote($xml)//id/text() \n";

	return query;
    }

    /**
     * @param viewFolder
     * @param creator
     * @return
     */
    private static String getViewIdsByOwnerQuery(DatabaseFolder viewFolder, String owner) {

	String query = "xquery version \"1.0-ml\"; \n";

	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\"; \n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\"; \n";

	query += "let $owner := '" + owner + "' \n";

	query += "for $uris in cts:uris((),(), cts:directory-query(\"/" + viewFolder.getName() + "/\", \"infinity\")   ) \n";

	query += "let $doc :=  fn:document($uris)  \n";

	query += "let $xml := if ($doc/node() instance of binary()) then (xdmp:binary-decode(fn:doc($uris)/node(), \"UTF-8\")) else $doc \n";

	query += "where (fn:contains($xml,fn:concat('<owner>',$owner,'</owner>')) or $doc//owner = $owner) return xdmp:unquote($xml)//id/text() \n";

	return query;
    }

    /**
     * @param viewFolder
     * @param owner
     * @param creator
     * @return
     */
    private static String getViewIdsByOwnerAndCreatorQuery(DatabaseFolder viewFolder, String owner, String creator) {

	String query = "xquery version \"1.0-ml\"; \n";

	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\"; \n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\"; \n";

	query += "let $owner := '" + owner + "' \n";

	query += "let $creator := '" + creator + "' \n";

	query += "for $uris in cts:uris((),(), cts:directory-query(\"/" + viewFolder.getName() + "/\", \"infinity\")   ) \n";

	query += "let $doc :=  fn:document($uris)  \n";

	query += "let $xml := if ($doc/node() instance of binary()) then (xdmp:binary-decode(fn:doc($uris)/node(), \"UTF-8\")) else $doc \n";

	query += "where ( (fn:contains($xml,fn:concat('<creator>',$creator,'</creator>')) and fn:contains($xml,fn:concat('<owner>',$owner,'</owner>'))) \n";

	query += "or ($doc//creator = $creator and $doc//owner = $owner)) return xdmp:unquote($xml)//id/text()";

	return query;
    }

    /**
     * @param viewFolder
     * @param owner
     * @param creator
     * @return
     */
    private static String getViewIdsByOwnerAndCreatorAndVisibilityQuery(DatabaseFolder viewFolder, String owner, String creator,
	    ViewVisibility viewVisibility) {

	String query = "xquery version \"1.0-ml\"; \n";

	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\"; \n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\"; \n";

	query += "let $owner := '" + owner + "' \n";

	query += "let $creator := '" + creator + "' \n";

	query += "let $visibility := '" + viewVisibility.name() + "' \n";

	query += "for $uris in cts:uris((),(), cts:directory-query(\"/" + viewFolder.getName() + "/\", \"infinity\")   ) \n";

	query += "let $doc :=  fn:document($uris)  \n";

	query += "let $xml := if ($doc/node() instance of binary()) then (xdmp:binary-decode(fn:doc($uris)/node(), \"UTF-8\")) else $doc \n";

	query += "where ( (fn:contains($xml,fn:concat('<creator>',$creator,'</creator>')) and \n";
	query += "fn:contains($xml,fn:concat('<owner>',$owner,'</owner>')) and \n";
	query += "fn:contains($xml,fn:concat('<visibility>',$visibility,'</visibility>')) ) \n";
	query += "or ($doc//creator = $creator and $doc//owner = $owner and $doc//visibility = $visibility  )) return xdmp:unquote($xml)//id/text() ";

	return query;
    }

    /**
     * @param viewFolder
     * @param creator
     * @param viewVisibility
     * @return
     */
    private static String getViewIdsByCreatorAndVisibilityQuery(DatabaseFolder viewFolder, String creator, ViewVisibility viewVisibility) {

	String query = "xquery version \"1.0-ml\"; \n";

	query += "declare namespace html = \"http://www.w3.org/1999/xhtml\"; \n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\"; \n";

	query += "let $creator := '" + creator + "' \n";

	query += "let $visibility := '" + viewVisibility.name() + "' \n";

	query += "for $uris in cts:uris((),(), cts:directory-query(\"/" + viewFolder.getName() + "/\", \"infinity\")   ) \n";

	query += "let $doc :=  fn:document($uris)  \n";

	query += "let $xml := if ($doc/node() instance of binary()) then (xdmp:binary-decode(fn:doc($uris)/node(), \"UTF-8\")) else $doc \n";

	query += "where ( (fn:contains($xml,fn:concat('<creator>',$creator,'</creator>')) and  \n";
	query += "fn:contains($xml,fn:concat('<visibility>',$visibility,'</visibility>')) ) \n";

	query += "or ($doc//creator = $creator and $doc//visibility = $visibility  )) return xdmp:unquote($xml)//id/text() ";

	return query;
    }

}
