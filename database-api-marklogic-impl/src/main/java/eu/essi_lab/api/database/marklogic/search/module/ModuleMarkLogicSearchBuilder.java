/**
 * 
 */
package eu.essi_lab.api.database.marklogic.search.module;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Properties;

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.MarkLogicModuleQueryBuilder;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSpatialQueryBuilder;
import eu.essi_lab.api.database.marklogic.search.def.DefaultMarkLogicSearchBuilder;
import eu.essi_lab.api.database.marklogic.search.def.DefaultMarkLogicSpatialQueryBuilder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.indexes.IndexedElements;
import eu.essi_lab.lib.xml.QualifiedName;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

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

    @Override
    protected String buildTermFrequencyQuery(Queryable target, int max) {

	if (target.getName().equals(ResourceProperty.SSC_SCORE_EL_NAME) || target.getContentType() != ContentType.TEXTUAL) {

	    return super.buildTermFrequencyQuery(target, max);
	}

	return MarkLogicModuleQueryBuilder.getInstance().getTermFrequencyQuery(target.getName(), max);
    }

    @Override
    protected String buildTempExtentQuery(Queryable element, BondOperator operator, String value) {

	return MarkLogicModuleQueryBuilder.getInstance().getTempExtentQuery(element, operator, value);
    }

    @Override
    protected String buildTempExtentNowQuery(Queryable element) {

	QualifiedName now = element == MetadataElement.TEMP_EXTENT_BEGIN ? //
		IndexedElements.TEMP_EXTENT_BEGIN_NOW.asQualifiedName() : //
		IndexedElements.TEMP_EXTENT_END_NOW.asQualifiedName();

	return MarkLogicModuleQueryBuilder.getInstance().getTempExtentNowQuery(now.getLocalPart());
    }

    @Override
    public MarkLogicSpatialQueryBuilder createSpatialQueryBuilder(MarkLogicSearchBuilder builder) {

	if (isCoveringModeEnabled()) {

	    return new DefaultMarkLogicSpatialQueryBuilder(this);
	}

	return new ModuleMarkLogicSpatialQueryBuilder(this);
    }

    /**
     * @return
     */
    private boolean isCoveringModeEnabled() {

	Optional<Properties> keyValueOption = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();
	if (keyValueOption.isPresent()) {

	    Properties properties = keyValueOption.get();
	    String option = properties.getProperty(KeyValueOptionKeys.COVERING_MODE.getLabel());

	    return option != null && option.equals("enabled");
	}

	return false;
    }

    @Override
    protected String buildDelectedExcludedQuery() {

	return MarkLogicModuleQueryBuilder.getInstance().getDeletedExcludedQuery();
    }

    @Override
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

	return MarkLogicModuleQueryBuilder.getInstance().getWeightQuery(element.getElementName(), w0, w1, w2, w3, w4, w5, w6, w7, w8, w9);
    }

    @Override
    protected String buildGDCWeightQuery() {

	return MarkLogicModuleQueryBuilder.getInstance().getGDCWeightQuery();
    }

    @Override
    public String buildCTSElementRangeQuery(QualifiedName el, String operator, String value, double weight, boolean quoteValue) {

	return MarkLogicModuleQueryBuilder.getInstance().getElementRangeQuery(el, operator, value, weight, quoteValue);
    }

    /**
     * @param name
     * @param value
     * @param property
     * @param bond
     * @return
     */
    @Override
    protected String buildSourceIdQuery(QualifiedName name, String value, Queryable property) {

	return buildSourceIdQuery(name, value, property, BondOperator.EQUAL);
    }

    /**
     * @param name
     * @param value
     * @param property
     * @param bond
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    @Override
    protected String buildSourceIdQuery(QualifiedName name, String value, Queryable property, BondOperator operator) {

	if (!dataFolderCheckEnabled) {

	    return super.buildSourceIdQuery(name, value, property, operator);
	}

	switch (operator) {
	case EQUAL:

	    return MarkLogicModuleQueryBuilder.getInstance().getSourceIdQuery(value, markLogicDB.getIdentifier());

	case TEXT_SEARCH:

	    return MarkLogicModuleQueryBuilder.getInstance().getSourceIdLikeQuery(value, markLogicDB.getIdentifier());
	}

	throw new IllegalArgumentException("Unsupported bond operator: " + operator);
    }

    /**
     * @param operator
     * @param ordered
     * @param operands
     * @return
     */
    @Override
    public String buildCTSLogicQuery(CTSLogicOperator operator, boolean ordered, String... operands) {

	if (ordered) {

	    return super.buildCTSLogicQuery(operator, ordered, operands);
	}

	return MarkLogicModuleQueryBuilder.getInstance().getLogicQuery(operator, buildCTSLogicQueryOperands(operands));
    }

    /**
     * @param el
     * @param val
     * @param weight
     * @return
     */
    @Override
    protected String buildCTSElementWordQuery(QualifiedName el, String val, Integer weight) {

	return MarkLogicModuleQueryBuilder.getInstance().getWordQuery(el.getLocalPart(), val, weight);
    }

    /**
     * @param operator
     * @return
     */
    @Override
    public String getCTSLogicQueryName(LogicalOperator operator) {

	return MarkLogicModuleQueryBuilder.getInstance().getLogicQueryName(operator);
    }

    @Override
    protected String buildCTSNotQuery(String query) {

	return MarkLogicModuleQueryBuilder.getInstance().getNotQuery(query);
    }
}
