/**
 * 
 */
package eu.essi_lab.api.database.opensearch.query;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.RankingStrategy;

/**
 * 2) aggiungere le basic queries, come quelle di ML per il ranking su document quality, etc
 * 3) mapping corretto dei bond
 * 
 * @author Fabrizio
 */
public class OpenSearchBondHandler implements DiscoveryBondHandler {

    private RankingStrategy ranking;
    private boolean dataFolderCheckEnabled;
    private boolean deletedIncluded;
    private int maxFrequencyMapItems;
    private List<Queryable> tfTargets;
    private Optional<OrderingDirection> orderingDirection;
    private Optional<Queryable> orderingProperty;

    private HashMap<String, String> dataFolderMap;
    private OpenSearchQueryBuilder queryBuilder;

    /**
     * @param message
     * @param map
     */
    public OpenSearchBondHandler(DiscoveryMessage message, HashMap<String, String> map) {

	this.dataFolderMap = map;
	this.ranking = message.getRankingStrategy();
	this.dataFolderCheckEnabled = message.isDataFolderCheckEnabled();
	this.deletedIncluded = message.isDeletedIncluded();
	this.maxFrequencyMapItems = message.getMaxFrequencyMapItems();
	this.tfTargets = message.getTermFrequencyTargets();
	this.orderingDirection = message.getOrderingDirection();
	this.orderingProperty = message.getOrderingProperty();
	this.queryBuilder = new OpenSearchQueryBuilder(map);
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {

	switch (bond.getLogicalOperator()) {
	case AND:
	    queryBuilder.appendBoolMustOpenTag();
	    break;
	case OR:
	    queryBuilder.appendBoolShouldOpenTag();
	    break;
	case NOT:
	    queryBuilder.appendBoolMustNotOpenTag();
	    break;
	}
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {

	switch (bond.getLogicalOperator()) {
	case AND:
	    queryBuilder.appendClosingTag(false);

	    break;

	case OR:

	    queryBuilder.appendClosingTag(true);

	    break;

	case NOT:
	    queryBuilder.appendClosingTag(false);

	    break;
	}

    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {

	Query query = new MatchPhraseQuery.Builder().//
		field(bond.getProperty().getName()).//
		query(bond.getPropertyValue()).//
		build().//
		toQuery();

	switch (bond.getProperty()) {
	case ACCESS_QUALITY:
	    break;
	case COMPLIANCE_LEVEL:
	    break;
	case DOWNLOAD_TIME:
	    break;
	case ESSENTIAL_VARS_QUALITY:
	    break;
	case EXECUTION_TIME:
	    break;
	case HAS_LOWEST_RANKING:
	    break;
	case IS_DELETED:
	    break;
	case IS_DOWNLOADABLE:
	    break;
	case IS_EIFFEL_RECORD:
	    break;
	case IS_EXECUTABLE:
	    break;
	case IS_GEOSS_DATA_CORE:
	    break;
	case IS_GRID:
	    break;
	case IS_ISO_COMPLIANT:
	    break;
	case IS_TIMESERIES:
	    break;
	case IS_TRAJECTORY:
	    break;
	case IS_TRANSFORMABLE:
	    break;
	case IS_VALIDATED:
	    break;
	case IS_VECTOR:
	    break;
	case METADATA_QUALITY:
	    break;
	case OAI_PMH_HEADER_ID:
	    break;
	case ORIGINAL_ID:
	    break;
	case PRIVATE_ID:
	    break;
	case PUBLIC_ID:
	    break;
	case RECOVERY_REMOVAL_TOKEN:
	    break;
	case RESOURCE_TIME_STAMP:
	    break;
	case SOURCE_ID:

	    query = queryBuilder.buildSourceIdQuery(bond);

	    break;

	case SSC_SCORE:
	    break;
	case SUCCEEDED_TEST:
	    break;
	case TEST_TIME_STAMP:
	    break;
	case TYPE:
	    break;
	}

	queryBuilder.append(query);
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	MatchPhraseQuery query = new MatchPhraseQuery.Builder().//
		field(bond.getProperty().getName()).//
		query(bond.getPropertyValue()).//
		build();

	queryBuilder.append(query.toQuery());
    }

    @Override
    public void separator() {

	queryBuilder.appendSeparator();
    }

    @Override
    public void spatialBond(SpatialBond bond) {

    }

    @Override
    public void customBond(QueryableBond<String> bond) {
    }

    @Override
    public void viewBond(ViewBond bond) {
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
    }

    @Override
    public void nonLogicalBond(Bond bond) {
    }

    /**
     * @return
     */
    public Query getQuery() {

	return queryBuilder.build();
    }
}
