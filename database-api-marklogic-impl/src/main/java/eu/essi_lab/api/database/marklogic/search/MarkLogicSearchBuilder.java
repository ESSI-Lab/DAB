package eu.essi_lab.api.database.marklogic.search;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.indexes.marklogic.MarkLogicIndexTypes;
import eu.essi_lab.indexes.marklogic.MarkLogicScalarType;
import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.messages.RankingStrategy;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.model.QualifiedName;
import eu.essi_lab.model.index.IndexedElementInfo;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceType;

/**
 * @author Fabrizio
 */
public interface MarkLogicSearchBuilder {

    /**
     * @author Fabrizio
     */
    public enum CTSLogicOperator {
	/**
	 * 
	 */
	AND,
	/**
	 * 
	 */
	OR
    }

    /**
     * @author Fabrizio
     */
    public enum QueryableStrategy {
	/**
	 *
	 */
	DEFAULT_STRATEGY,
	/**
	 *
	 */
	NUMERICAL_STRATEGY,
	/**
	 *
	 */
	TEMP_EXTENT_STRATEGY,
	/**
	 *
	 */
	ANY_TEXT_STRATEGY,
	/**
	 *
	 */
	SUBJECT_STRATEGY,
	/**
	 *
	 */
	BOUNDING_BOX_NULL_STRATEGY;

	/**
	 * @param index
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public static QueryableStrategy getStrategy(IndexedMetadataElement index) {

	    IndexedMetadataElement ime = (IndexedMetadataElement) index;
	    Optional<MetadataElement> optional = ime.getMetadataElement();
	    if (optional.isPresent()) {
		MetadataElement element = optional.get();
		switch (element) {
		case TEMP_EXTENT_BEGIN:
		case TEMP_EXTENT_END:
		    return TEMP_EXTENT_STRATEGY;
		case BOUNDING_BOX:
		    return BOUNDING_BOX_NULL_STRATEGY;
		case ANY_TEXT:
		    return ANY_TEXT_STRATEGY;
		case SUBJECT:
		    return SUBJECT_STRATEGY;
		}
	    }

	    IndexedElementInfo indexInfo = index.getInfo(DatabaseImpl.MARK_LOGIC.getName());
	    String type = indexInfo.getIndexType();
	    MarkLogicScalarType scalarType = MarkLogicScalarType.decode(indexInfo.getScalarType());

	    if (type.equals(MarkLogicIndexTypes.RANGE_ELEMENT_INDEX.getType())) {
		switch (scalarType) {

		case DOUBLE:
		case INT:
		    return NUMERICAL_STRATEGY;
		case STRING:
		default:
		    return DEFAULT_STRATEGY;
		}
	    }

	    return null;
	}
    }

    /**
     * @param query
     * @param estimate
     * @return
     */
    public String buildCTSSearchQuery(String query, boolean estimate);

    /**
     * @param query
     * @param estimate
     * @return
     */
    public String buildCTSSearch(String query, boolean estimate);

    /**
     * @param query
     * @param estimate
     * @return
     */
    public String buildNoConstraintsCTSSearchQuery(boolean estimate);

    /**
     * @param estimate
     * @return
     */
    public String buildNoConstraintsCTSSearch(boolean estimate);

    /**
     * @param bond
     * @return
     */
    public String buildQuery(QueryableBond<String> bond);

    /**
     * @param bond
     * @return
     */
    public String buildQuery(SimpleValueBond bond);

    /**
     * @param bond
     * @return
     */
    public String buildQuery(RuntimeInfoElementBond bond);

    /**
     * @param bond
     * @return
     */
    public String buildQuery(SpatialBond bond);

    /**
     * @param bond
     * @return
     */
    public String buildQuery(ResourcePropertyBond bond);

    /**
     * @return
     */
    public String buildTrueQuery();

    /**
     * @param el
     * @param operator
     * @param value
     * @param weight
     * @param quoteValue
     * @return
     */
    public String buildCTSElementRangeQuery(QualifiedName el, String operator, String value, double weight, boolean quoteValue);

    /**
     * @param el
     * @param operator
     * @param value
     * @param quoteValue
     * @return
     */
    public String buildCTSElementRangeQuery(QualifiedName el, String operator, String value, boolean quoteValue);

    /**
     * @param operator
     * @param operands
     * @return
     */
    public String buildCTSLogicQuery(CTSLogicOperator operator, String... operands);

    /**
     * @return
     */
    public RankingStrategy getRankingStrategy();

    /**
     * @return
     */
    public MarkLogicSpatialQueryBuilder createSpatialQueryBuilder(MarkLogicSearchBuilder builder);

    /**
     * Returns the target of the cts:search which includes 'doc()' followed by all the available types of GSResources
     */
    public static String getCTSSearchTarget() {

	return Arrays.asList(ResourceType.values()).//
		stream().map(r -> "gs:" + r.getType()).//
		collect(Collectors.joining(" or ", "doc()[", "]"));
    }

    /**
     * @param name
     * @return
     */
    public static String createFNQName(String name) {

	return "fn:QName('" + NameSpace.GI_SUITE_DATA_MODEL.getURI() + "','" + name + "')";
    }
}
