/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

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

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class ViewsMapping extends IndexMapping {

    /**
     * 
     */
    private static final String VIEWS_INDEX = Database.VIEWS_FOLDER + "-index";

    //
    // views-index properties
    //
    public static final String VIEW = "view";
    public static final String VIEW_ID = "viewID";
    public static final String VIEW_LABEL = "viewLabel";
    public static final String VIEW_OWNER = "viewOwner";
    public static final String VIEW_CREATOR = "viewCreator";
    public static final String VIEW_VISIBILITY = "viewVisibility";
    public static final String SOURCE_DEPLOYMENT = "sourceDeployment";

    private static ViewsMapping instance;

    /**
    * 
    */
    protected ViewsMapping() {

	super(VIEWS_INDEX);

	addProperty(VIEW_ID, FieldType.Text.jsonValue());
	addProperty(VIEW_LABEL, FieldType.Text.jsonValue());
	addProperty(VIEW_OWNER, FieldType.Text.jsonValue());
	addProperty(VIEW_CREATOR, FieldType.Text.jsonValue());
	addProperty(VIEW_VISIBILITY, FieldType.Text.jsonValue());
	addProperty(SOURCE_DEPLOYMENT, FieldType.Text.jsonValue());

	addProperty(toKeywordField(VIEW_ID), FieldType.Keyword.jsonValue());
	addProperty(toKeywordField(VIEW_LABEL), FieldType.Keyword.jsonValue());
	addProperty(toKeywordField(VIEW_OWNER), FieldType.Keyword.jsonValue());
	addProperty(toKeywordField(VIEW_CREATOR), FieldType.Keyword.jsonValue());
	addProperty(toKeywordField(VIEW_VISIBILITY), FieldType.Keyword.jsonValue());
	addProperty(toKeywordField(SOURCE_DEPLOYMENT), FieldType.Keyword.jsonValue());
    }

    /**
     * @return
     */
    public static final ViewsMapping get() {

	if (instance == null) {

	    instance = new ViewsMapping();
	}

	return instance;
    }
}
