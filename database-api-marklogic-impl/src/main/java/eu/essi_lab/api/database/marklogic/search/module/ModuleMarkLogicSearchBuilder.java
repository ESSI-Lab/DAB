/**
 * 
 */
package eu.essi_lab.api.database.marklogic.search.module;

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

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.MarkLogicModuleManager;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSpatialQueryBuilder;
import eu.essi_lab.api.database.marklogic.search.def.DefaultMarkLogicSearchBuilder;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.lib.xml.QualifiedName;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class ModuleMarkLogicSearchBuilder extends DefaultMarkLogicSearchBuilder {

    /**
     * @param message
     * @param markLogicDB
     */
    public ModuleMarkLogicSearchBuilder(DiscoveryMessage message, MarkLogicDatabase markLogicDB) {

	super(message, markLogicDB);
    }

    protected String buildTempExtentQuery(Queryable element, BondOperator operator, String value) {

	return MarkLogicModuleManager.getInstance().getTempExtentQuery(element, operator, value);
    }

    protected String buildTempExtentNowQuery(Queryable element) {

	QualifiedName now = element == MetadataElement.TEMP_EXTENT_BEGIN ? //
		IndexedElements.TEMP_EXTENT_BEGIN_NOW.asQualifiedName() : //
		IndexedElements.TEMP_EXTENT_END_NOW.asQualifiedName();

	return MarkLogicModuleManager.getInstance().getTempExtentNowQuery(now.getLocalPart());
    }

    public MarkLogicSpatialQueryBuilder createSpatialQueryBuilder(MarkLogicSearchBuilder builder) {

	return new ModuleMarkLogicSpatialQueryBuilder(this);
    }

    protected String buildDelectedExcludedQuery() {

	return MarkLogicModuleManager.getInstance().getDeletedExcludedQuery();
    }

    protected String buildWeightQuery(IndexedElement element) {

	int w0 = ranking.computeRangeWeight(element, 1);
	int w1 = ranking.computeRangeWeight(element, 2);
	int w2 = ranking.computeRangeWeight(element, 3);
	int w3 = ranking.computeRangeWeight(element, 4);
	int w4 = ranking.computeRangeWeight(element, 5);
	int w5 = ranking.computeRangeWeight(element, 6);
	int w6 = ranking.computeRangeWeight(element, 7);
	int w7 = ranking.computeRangeWeight(element, 8);
	int w8 = ranking.computeRangeWeight(element, 9);
	int w9 = ranking.computeRangeWeight(element, 10);

	return MarkLogicModuleManager.getInstance().getWeightQuery(element.getElementName(), w0, w1, w2, w3, w4, w5, w6, w7, w8, w9);
    }

    protected String buildGDCWeightQuery() {

	return MarkLogicModuleManager.getInstance().getGDCWeightQuery();
    }

    /**
     * @param name
     * @param value
     * @param property
     * @param bond
     * @return
     */
    protected String buildSourceIdQuery(QualifiedName name, String value, Queryable property) {

	if (!dataFolderCheckEnabled) {

	    return super.buildSourceIdQuery(name, value, property);
	}

	return MarkLogicModuleManager.getInstance().getSourceIdQuery(value, markLogicDB.getSuiteIdentifier());
    }
}
