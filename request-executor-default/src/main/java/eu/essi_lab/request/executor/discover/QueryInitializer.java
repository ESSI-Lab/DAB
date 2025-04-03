package eu.essi_lab.request.executor.discover;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.logicng.formulas.Formula;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.authorization.converter.IRequestAuthorizationConverter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.QueryInitializerMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.EmptyBond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.parser.NotBondParser;
import eu.essi_lab.messages.bond.parser.SourceBondHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.IQueryInitializer;
import eu.essi_lab.views.DefaultViewManager;
import eu.essi_lab.views.IViewManager;

/**
 * The default implementation of query initializer performs the following steps:
 * <ol>
 * <li>Asks the Request Authorization Converter to set the permitted bond, generated starting from the original query
 * bond according to the user source discovery grants.</li>
 * <li>normalizes the permitted bond by converting it to an equivalent bond expressed in normal form (<b>normalized
 * bond</b>):</li>
 * <ol>
 * <li>converts bond in negation normal form with De Morgan's laws</li>
 * <li>converts bond to disjunctive normal form by distributing AND over OR</li>
 * <li>simplify conjunctions e.g. removing the ones that result in empty sets (such as conjunctions containing two
 * sources, i.e. S1 AND S2 or also S1 AND NOT(S1))</li>
 * <li>aggregates conjunctions containing the same source bond (e.g. (S1 AND B1) OR (S1 AND B2) -> S1 AND (B1 OR
 * B2)</li>
 * </ol>
 * </ol>
 *
 * @author boldrini
 * @see IQueryInitializer
 */
public class QueryInitializer implements IQueryInitializer {

    private static final String NORMALIZATION_FAILED_IRREDUCIBLE_INPUT = "Normalization failed because irreducible input";
    private static final String NORMALIZATION_FAILED_UNEXPECTED_SYNTAX = "Normalization failed because unexpected error";
    private static final String UNEXPECTED_LOGICAL_OPERATOR_MESSAGE_PREFIX = "Not expected logical operator: ";
    private IRequestAuthorizationConverter requestAuthorizationConverter = null;

    /**
     * @see IQueryInitializer#initializeQuery(DiscoveryMessage)
     */
    @Override
    public void initializeQuery(QueryInitializerMessage message) throws GSException {

	if (message.getView().isPresent()) {

	    View view = message.getView().get();

	    Optional<Bond> optionalBond = message.getUserBond();

	    Bond userBond = null;

	    if (!optionalBond.isPresent()) {

		userBond = view.getBond();

	    } else {

		userBond = BondFactory.createAndBond(optionalBond.get(), view.getBond());
	    }

	    message.setUserBond(userBond);
	}

	Bond authorizedBond = null;
	// Authorized bond generation
	if (requestAuthorizationConverter != null) {
	    // permission given to a subset of sources, depending on the user
	    authorizedBond = requestAuthorizationConverter.generateAuthorizedBond(message);
	}

	authorizedBond = simplifyAuthorizedBond(message.getUserBond().orElse(null), authorizedBond);

	Bond permittedBond;

	if (message.getUserBond().isPresent()) {
	    if (authorizedBond == null) {
		permittedBond = message.getUserBond().get();
	    } else {
		permittedBond = BondFactory.createAndBond(message.getUserBond().get(), authorizedBond);
	    }
	} else {
	    permittedBond = authorizedBond;
	}

	if (message instanceof DiscoveryMessage) {

	    DiscoveryMessage discoveryMessage = (DiscoveryMessage) message;
	    ResourcePropertyConstraintAdder rpcah = new ResourcePropertyConstraintAdder(discoveryMessage);
	    permittedBond = rpcah.addResourcePropertyConstraints(message.getRequestId(), permittedBond,
		    discoveryMessage.getResultsPriority());
	}

	message.setPermittedBond(permittedBond);

	Bond bond = message.getPermittedBond();
	// GSLoggerFactory.getLogger(getClass()).info("Bond normalization STARTED");

	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.BOND_NORMALIZATION, message.getRequestId(),
		Optional.ofNullable(message.getWebRequest()));

	Bond normalizedBond = normalizeBond(bond);

	pl.logPerformance(GSLoggerFactory.getLogger(getClass()));
	// GSLoggerFactory.getLogger(getClass()).info("Bond normalization ENDED");

	message.setNormalizedBond(normalizedBond);

    }

    /**
     * Simplify the permitted bond, eliminating repeated sources that may have been introduced by permitted bond
     * generation. <br/>
     * E.g. User
     * bond: (T1 AND S1) Authorized bond: (S1 OR S2) -> T1 AND S1 <br/>
     * It will work only with authorizedBonds of the form:
     * <ol>
     * <li>S1</li>
     * <li>S1 OR S2 ... OR SN</li>
     * </ol>
     *
     * @param message
     */
    public Bond simplifyAuthorizedBond(Bond userBond, Bond authorizedBond) {

	List<Bond> authorizedSourceBonds;
	if (authorizedBond == null) {
	    return null;
	}
	if (authorizedBond instanceof LogicalBond) {
	    LogicalBond orBond = (LogicalBond) authorizedBond;
	    if (!orBond.getLogicalOperator().equals(LogicalOperator.OR)) {
		return authorizedBond;
	    } else {
		authorizedSourceBonds = orBond.getOperands();
	    }
	} else if (authorizedBond instanceof ResourcePropertyBond) {
	    ResourcePropertyBond rpb = (ResourcePropertyBond) authorizedBond;
	    if (rpb.getProperty().equals(ResourceProperty.SOURCE_ID)) {
		authorizedSourceBonds = new ArrayList<>();
		authorizedSourceBonds.add(rpb);
	    } else {
		return authorizedBond;
	    }
	} else {
	    return authorizedBond;
	}

	// here we search for a block of user required sources, such as:
	// 0) S1
	// 1) S1 OR ... OR SN
	// 2) S1 AND (other constraints)
	// 3) (S1 OR ... OR SN) AND (other constraints)
	// 4) bond involving NOT operator -> will not be optimized at this time
	// 5) bond without source bond -> will not be optimized
	// 6) other configurations will not be optimized

	NotBondParser nbp = new NotBondParser();
	Set<Bond> notBonds = nbp.parseBond(userBond);
	SourceBondHandler sbh = new SourceBondHandler(userBond);

	// this set will contain the source identifiers used in the user query.
	// if the optimization is not feasible, then it will be an empty set.
	Set<String> userSourceIdentifiers = new HashSet<>();

	// to eliminate case 4) and case 5)
	if (notBonds.isEmpty() && !sbh.getSourceIdentifiers().isEmpty()) {

	    List<Bond> operands = new ArrayList<Bond>();

	    if (userBond instanceof LogicalBond) {
		LogicalBond logicalBond = (LogicalBond) userBond;

		// case 1)
		if (!getSourceIdentifiersFromOR(logicalBond).isEmpty()) {
		    userSourceIdentifiers = getSourceIdentifiersFromOR(logicalBond);
		} else
		// case 2) 3)
		if (logicalBond.getLogicalOperator().equals(LogicalOperator.AND)) {
		    operands = logicalBond.getOperands();
		    for (Bond bond : operands) {
			if (!getSourceIdentifiersFromOR(bond).isEmpty()) {
			    userSourceIdentifiers = getSourceIdentifiersFromOR(bond);
			} else {
			    sbh = new SourceBondHandler(bond);
			    if (!sbh.getSourceIdentifiers().isEmpty()) {
				userSourceIdentifiers.clear();
				break;
			    }
			}
		    }
		}
	    } else if (userBond instanceof ResourcePropertyBond) {
		if (!getSourceIdentifiersFromOR(userBond).isEmpty()) {
		    // case 0)
		    userSourceIdentifiers = getSourceIdentifiersFromOR(userBond);
		}
	    }
	}

	// it's possible to optimize only if the OR sources is found in user query
	boolean optimize = !userSourceIdentifiers.isEmpty();

	LogicalBond ret = BondFactory.createOrBond();

	Set<Bond> optimizableSourcesSet = new HashSet<>();

	for (Bond bond : authorizedSourceBonds) {

	    if (bond instanceof ResourcePropertyBond) {
		ResourcePropertyBond rpb = (ResourcePropertyBond) bond;
		if (!rpb.getProperty().equals(ResourceProperty.SOURCE_ID)) {
		    // not optimizable bond
		    ret.getOperands().add(bond);
		    continue;
		}
	    } else {
		// not optimizable bond
		ret.getOperands().add(bond);
		continue;
	    }

	    ResourcePropertyBond rpb = (ResourcePropertyBond) bond;
	    String sourceId = rpb.getPropertyValue();
	    userSourceIdentifiers.remove(sourceId);
	    // possibly optimizable bond
	    optimizableSourcesSet.add(rpb);

	}

	if (optimize && userSourceIdentifiers.isEmpty()) {
	    // optimization can be done
	    optimizableSourcesSet.clear();
	}

	ret.getOperands().addAll(optimizableSourcesSet);

	if (ret.getOperands().isEmpty()) {
	    return null;
	}

	if (ret.getOperands().size() == 1) {
	    return ret.getFirstOperand();
	}

	return ret;

    }

    /**
     * Normalizes the given bond, according to the steps described in {@link QueryInitializer}
     *
     * @param bond the bond to normalize
     * @return a normalized bond
     * @throws GSException in case the normalization failed, for whatever reason
     */
    public Bond normalizeBond(Bond bond) throws GSException {
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - preliminary simplification");
	Bond simpleAndsForm = getNestedConjunctionsSimplifiedForm(bond);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - sources simplification");
	Bond repeatedBondSimplification = getRepeatedBondSimplifiedForm(simpleAndsForm);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - repeated bond simplification");
	Bond simplifiedForm = getSourcesSimplifiedForm(repeatedBondSimplification);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - negational form");
	Bond negationNormalForm = getNegationNormalForm(simplifiedForm);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - aggregating simple bonds");
	Bond aggregatedSimpleBonds = getAggregateSimpleBonds(negationNormalForm);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - disjunctive form");
	// using the naive algorithm, as the library based algorithm seems to require more time!
	Bond disjunctiveNormalForm = getDisjunctiveNormalFormNaive(aggregatedSimpleBonds);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - simplified conjunctions");
	Bond cleanDisjunctiveNormalForm = simplifyConjunctions(disjunctiveNormalForm);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - aggregated conjunctions");
	Bond aggregatedConjunctions = aggregateConjunctions(cleanDisjunctiveNormalForm);

	Bond likeSourcesSimplification = getLikeSourcesSimplifiedForm(aggregatedConjunctions);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - cleanup");
	// this seems not to be needed... already per source aggregated at this point!
	// Bond ret = getPerSourceAggregation(aggregatedConjunctions);
	// GSLoggerFactory.getLogger(getClass()).trace("Normalization - done");
	return likeSourcesSimplification;

    }

    /**
     * aggregate per source e.g. : 1) S1 AND A AND B -> S1 AND (A AND B)
     *
     * @param bond
     * @return
     */
    public Bond getPerSourceAggregation(Bond bond) {
	if (bond == null) {
	    return null;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    LogicalOperator operator = lb.getLogicalOperator();
	    if (operator.equals(LogicalOperator.AND)) {
		return getPerSourceAggregationFromConjunction(lb);
	    } else if (operator.equals(LogicalOperator.OR)) {
		List<Bond> newOperands = new ArrayList<>();
		for (Bond operand : lb.getOperands()) {
		    if (operand instanceof LogicalBond) {
			LogicalBond cb = (LogicalBond) operand;
			LogicalOperator childOperator = cb.getLogicalOperator();
			if (childOperator.equals(LogicalOperator.AND)) {
			    Bond newOperand = getPerSourceAggregationFromConjunction(cb);
			    newOperands.add(newOperand);
			}
		    }

		}
		lb.getOperands().clear();
		lb.getOperands().addAll(newOperands);
		return BondFactory.createOrBond(newOperands);
	    } else {
		return bond;
	    }
	} else {
	    return bond;
	}
    }

    private Bond getPerSourceAggregationFromConjunction(LogicalBond bond) {
	List<Bond> operands = bond.getOperands();
	Bond sourceBond = null;
	List<Bond> rest = new ArrayList<>();
	for (Bond operand : operands) {
	    if (isSourceIdentifierBond(operand)) {
		sourceBond = operand;
	    } else {
		rest.add(operand);
	    }
	}
	if (sourceBond == null) {
	    return bond;
	} else {
	    rest.add(0, sourceBond);
	    return BondFactory.createAndBond(rest);
	}
    }

    /**
     * In case of same bond repeated inside a logical operator, just delete all of them except one. E.g. A AND A AND A
     * AND B -> A AND B
     *
     * @param bond
     * @return
     */
    private Bond getRepeatedBondSimplifiedForm(Bond bond) {

	if (bond == null) {
	    return null;
	}

	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    HashSet<Bond> bonds = new HashSet<Bond>(lb.getOperands());
	    lb.getOperands().clear();
	    for (Bond uniqueBond : bonds) {
		Bond simplifiedBond = getRepeatedBondSimplifiedForm(uniqueBond);
		lb.getOperands().add(simplifiedBond);
	    }
	    if (!lb.getLogicalOperator().equals(LogicalOperator.NOT) && lb.getOperands().size() == 1) {
		return lb.getOperands().get(0);
	    }
	}

	return bond;
    }

    /**
     * In case of conjunctions of sources, simplifications are possible! e.g. (S like 1) AND (S1) -> S1
     *
     * @param bond
     * @return
     */
    private Bond getLikeSourcesSimplifiedForm(Bond bond) {
	if (bond == null) {
	    return null;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    LogicalOperator operator = lb.getLogicalOperator();
	    if (operator.equals(LogicalOperator.AND)) {
		List<Bond> operands = lb.getOperands();
		List<ResourcePropertyBond> sourceBonds = new ArrayList<>();
		List<ResourcePropertyBond> likeBonds = new ArrayList<>();
		List<Bond> others = new ArrayList<>();
		for (Bond operand : operands) {
		    if (isSourceIdentifierBond(operand)) {
			sourceBonds.add((ResourcePropertyBond) operand);
		    } else if (isSourceIdentifierLikeBond(operand)) {
			likeBonds.add((ResourcePropertyBond) operand);
		    } else {
			others.add(operand);
		    }
		}
		if (sourceBonds.size() > 1) {
		    return null;
		}
		if (sourceBonds.size() == 1 && likeBonds.size() > 0) {
		    ResourcePropertyBond sourceBond = sourceBonds.get(0);
		    String id = sourceBond.getPropertyValue();
		    boolean ok = true;
		    for (ResourcePropertyBond likeBond : likeBonds) {
			String value = likeBond.getPropertyValue();
			if (!id.contains(value)) {
			    ok = false;
			}
		    }
		    if (ok) {
			return sourceBond;
		    } else {
			return null;
		    }
		}
	    } else if (operator.equals(LogicalOperator.OR)) {
		List<Bond> cleaned = new ArrayList<Bond>();
		for (Bond operand : lb.getOperands()) {
		    Bond c = getLikeSourcesSimplifiedForm(operand);
		    if (c != null) {
			cleaned.add(c);
		    }
		}
		switch (cleaned.size()) {
		case 0:
		    return null;
		case 1:
		    return cleaned.get(0);
		default:
		    lb.getOperands().clear();
		    lb.getOperands().addAll(cleaned);
		    break;
		}
	    }
	}
	return bond;
    }

    /**
     * In case of conjunctions of disjuncted sources, simplifications are possible! e.g. (S1 OR S2) AND (S1 OR S3) -> S1
     *
     * @param bond
     * @return
     */
    private Bond getSourcesSimplifiedForm(Bond bond) {
	if (bond == null) {
	    return null;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    LogicalOperator operator = lb.getLogicalOperator();
	    if (operator.equals(LogicalOperator.AND)) {
		List<Bond> operands = lb.getOperands();
		List<Bond> simplifiableSourceBonds = new ArrayList<>();
		List<Bond> others = new ArrayList<>();
		for (Bond operand : operands) {
		    if (isSimplifiableSourceBond(operand)) {
			simplifiableSourceBonds.add(operand);
		    } else {
			others.add(operand);
		    }
		}
		if (simplifiableSourceBonds.size() > 1) {
		    Bond simplifiedBond = simplifiableSourceBonds.get(0);
		    for (int i = 1; i < simplifiableSourceBonds.size(); i++) {
			Bond toSimplify = simplifiableSourceBonds.get(i);
			simplifiedBond = simplifySourceBonds(simplifiedBond, toSimplify);
		    }
		    if (simplifiedBond.equals(new EmptyBond())) {
			return new EmptyBond();
		    }
		    others.add(simplifiedBond);
		    switch (others.size()) {
		    case 1:
			return others.get(0);
		    default:
			return BondFactory.createAndBond(others);
		    }
		}

	    }
	}
	return bond;
    }

    private Bond simplifySourceBonds(Bond sources1, Bond sources2) {
	HashMap<String, List<Bond>> operands1 = getSourceOperands(sources1);
	HashMap<String, List<Bond>> operands2 = getSourceOperands(sources2);
	Set<String> identifiers1 = new HashSet<>(operands1.keySet());
	Set<String> identifiers2 = new HashSet<>(operands2.keySet());
	identifiers1.retainAll(identifiers2);
	List<Bond> result = new ArrayList<>();
	for (String identifier : identifiers1) {
	    ResourcePropertyBond bond = BondFactory.createSourceIdentifierBond(identifier);
	    List<Bond> bonds1 = operands1.get(identifier);
	    List<Bond> bonds2 = operands2.get(identifier);
	    List<Bond> andBonds = new ArrayList<>();
	    if (bonds1.isEmpty() && bonds2.isEmpty()) {
		result.add(bond);
	    } else if (bonds1.isEmpty() && !bonds2.isEmpty()) {
		andBonds.add(bond);
		andBonds.addAll(bonds2);
		Bond andBond = BondFactory.createAndBond(andBonds);
		result.add(andBond);
	    } else if (!bonds1.isEmpty() && bonds2.isEmpty()) {
		andBonds.add(bond);
		andBonds.addAll(bonds1);
		Bond andBond = BondFactory.createAndBond(andBonds);
		result.add(andBond);
	    } else if (bonds1.size() == bonds2.size()) {
		for (int i = 0; i < bonds1.size(); i++) {
		    Bond bond1 = bonds1.get(i);
		    Bond bond2 = bonds2.get(i);
		    if (!bond1.equals(bond2)) {
			throw new IllegalArgumentException("Not simplifiable");
		    }
		}
		andBonds.add(bond);
		andBonds.addAll(bonds1);
		Bond andBond = BondFactory.createAndBond(andBonds);
		result.add(andBond);
	    } else {
		throw new IllegalArgumentException("Not simplifiable");
	    }
	}
	switch (result.size()) {
	case 0:
	    return new EmptyBond();
	case 1:
	    return result.get(0);
	default:
	    return BondFactory.createOrBond(result);
	}
    }

    /**
     * Maps from simplifiable bonds to a map, having as key the source identifier and as value the bond to be put in and
     *
     * @param sources
     * @return
     */
    private HashMap<String, List<Bond>> getSourceOperands(Bond sources) {
	HashMap<String, List<Bond>> ret = new HashMap<>();
	if (isSourceIdentifierBond(sources)) {
	    // CASE 1
	    ResourcePropertyBond rpb = (ResourcePropertyBond) sources;
	    ret.put(rpb.getPropertyValue(), new ArrayList<>());
	} else if (sources instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) sources;
	    LogicalOperator operator = lb.getLogicalOperator();
	    if (operator.equals(LogicalOperator.OR)) {
		// possible CASE 3-4
		List<Bond> operands = lb.getOperands();
		String sourceId = null;
		for (Bond operand : operands) {
		    if (isSourceIdentifierBond(operand)) {
			// CASE 3
			ResourcePropertyBond rpb = (ResourcePropertyBond) operand;
			ret.put(rpb.getPropertyValue(), new ArrayList<>());
		    } else {
			// CASE 4
			if (operand instanceof LogicalBond) {
			    LogicalBond cb = (LogicalBond) operand;
			    LogicalOperator childOperator = cb.getLogicalOperator();
			    if (childOperator.equals(LogicalOperator.AND)) {
				List<Bond> childOperands = cb.getOperands();
				List<Bond> bonds = new ArrayList<>();
				for (Bond childOperand : childOperands) {
				    if (isSourceIdentifierBond(childOperand)) {
					ResourcePropertyBond rpb = (ResourcePropertyBond) childOperand;
					sourceId = rpb.getPropertyValue();
				    } else {
					bonds.add(childOperand);
				    }
				}
				ret.put(sourceId, bonds);
			    }
			}
		    }
		}
	    } else if (operator.equals(LogicalOperator.AND)) {
		// CASE 2
		List<Bond> childOperands = lb.getOperands();
		ResourcePropertyBond rpb = null;
		List<Bond> bonds = new ArrayList<>();
		for (Bond childOperand : childOperands) {
		    if (isSourceIdentifierBond(childOperand)) {
			rpb = (ResourcePropertyBond) childOperand;
		    } else {
			bonds.add(childOperand);
		    }
		}
		ret.put(rpb.getPropertyValue(), bonds);
	    }
	}
	return ret;
    }

    /**
     * Returns true if in the forms: 1) S1 2) S1 AND B AND C 3) S1 OR S2 OR SN 4) S1 OR (S2 AND B AND C) OR SN
     *
     * @param bond
     * @return
     */
    private boolean isSimplifiableSourceBond(Bond bond) {
	if (bond == null) {
	    return false;
	}
	// CASE 1
	if (isSourceIdentifierBond(bond)) {
	    return true;
	} else if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    LogicalOperator operator = lb.getLogicalOperator();
	    if (operator.equals(LogicalOperator.OR)) {
		// possible CASE 3-4
		List<Bond> operands = lb.getOperands();
		for (Bond operand : operands) {
		    if (!isSourceIdentifierBond(operand)) {

			// may still be S AND OTHER -> still simplifiable
			if (operand instanceof LogicalBond) {
			    LogicalBond cb = (LogicalBond) operand;
			    LogicalOperator childOperator = cb.getLogicalOperator();
			    if (childOperator.equals(LogicalOperator.AND)) {
				List<Bond> childOperands = cb.getOperands();
				boolean sourceFound = false;
				for (Bond childOperand : childOperands) {
				    if (isSourceIdentifierBond(childOperand)) {
					sourceFound = true;
				    }
				}
				if (!sourceFound) {
				    return false;
				}
			    } else {
				return false;
			    }
			} else {
			    return false;
			}
		    }
		}
		// all the operands have been checked
		return true;
	    } else if (operator.equals(LogicalOperator.AND)) {
		// possible CASE 2
		List<Bond> childOperands = lb.getOperands();
		boolean sourceFound = false;
		for (Bond childOperand : childOperands) {
		    if (isSourceIdentifierBond(childOperand)) {
			sourceFound = true;
		    }
		}
		if (sourceFound) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * In case of a conjunction moves up the operands of child conjunctions e.g. A AND (B AND C) -> A AND B AND C
     *
     * @param bond
     * @return
     */
    private Bond getNestedConjunctionsSimplifiedForm(Bond bond) {
	if (bond == null) {
	    return null;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    LogicalOperator operator = lb.getLogicalOperator();
	    if (operator.equals(LogicalOperator.AND)) {
		List<Bond> fatherOperands = lb.getOperands();
		List<Bond> newFatherOperands = new ArrayList<>();
		for (int i = 0; i < fatherOperands.size(); i++) {
		    Bond child = getNestedConjunctionsSimplifiedForm(fatherOperands.get(i));
		    if (child instanceof LogicalBond) {
			LogicalBond cb = (LogicalBond) child;
			LogicalOperator childOperator = cb.getLogicalOperator();
			if (childOperator.equals(LogicalOperator.AND)) {
			    List<Bond> operands = cb.getOperands();
			    for (Bond op : operands) {
				newFatherOperands.add(op);
			    }
			} else {
			    newFatherOperands.add(cb);
			}
		    } else {
			newFatherOperands.add(child);
		    }
		}
		bond = BondFactory.createLogicalBond(LogicalOperator.AND, newFatherOperands);
	    } else {
		List<Bond> operands = lb.getOperands();
		List<Bond> newOperands = new ArrayList<Bond>();
		for (Bond operand : operands) {
		    Bond simplifiedBond = getNestedConjunctionsSimplifiedForm(operand);
		    newOperands.add(simplifiedBond);
		}
		operands.clear();
		operands.addAll(newOperands);

	    }
	}
	return bond;
    }

    protected IViewManager createViewManager(StorageInfo databaseURI) throws GSException {
	DatabaseReader reader = DatabaseProviderFactory.getReader(databaseURI);
	DatabaseWriter writer = DatabaseProviderFactory.getWriter(databaseURI);
	DefaultViewManager ret = new DefaultViewManager();
	ret.setDatabaseReader(reader);
	ret.setDatabaseWriter(writer);
	return ret;
    }

    /**
     * The default generation assumes that the user can access every sources<br>
     * <br>
     * The returned authorized bond will be one of the following form:
     * <ol>
     * <li>null</li>
     * <li>S1</li>
     * <li>S1 OR S2 OR ... SN</li>
     * </ol>
     *
     * @param message
     * @return
     * @throws GSException
     */
    private Bond generateDefaultAuthorizedBond(QueryInitializerMessage message) throws GSException {

	// ----------------------------------------------------------------------
	//
	// creates the sources bond possibly in AND with the resource type bond
	//
	Set<Bond> sourcesSet = new HashSet<>();

	for (GSSource gsSource : message.getSources()) {

	    sourcesSet.add(BondFactory.createSourceIdentifierBond(gsSource.getUniqueIdentifier()));

	}

	Bond sourcesBond = null;
	if (sourcesSet.size() == 1) {
	    sourcesBond = sourcesSet.iterator().next();
	} else if (sourcesSet.size() > 1) {
	    sourcesBond = BondFactory.createOrBond(sourcesSet);
	}

	return sourcesBond;

    }

    /**
     * Returns the list of source identifiers s1, s2, ... , sn only if the given bond is a bond of the form:
     * <ol>
     * <li>S1 OR S2 ... OR SN</li>
     * <li>S1</li>
     * </ol>
     *
     * @param logicalBond
     * @return
     */
    private Set<String> getSourceIdentifiersFromOR(Bond bond) {
	Set<String> sourceIdentifiers = new HashSet<>();

	if (bond instanceof LogicalBond) {
	    LogicalBond logicalBond = (LogicalBond) bond;
	    if (logicalBond.getLogicalOperator().equals(LogicalOperator.OR)) {
		List<Bond> operands = logicalBond.getOperands();
		for (Bond operand : operands) {
		    if (operand instanceof ResourcePropertyBond) {
			ResourcePropertyBond rpb = (ResourcePropertyBond) operand;

			if (rpb.getProperty() == ResourceProperty.SOURCE_ID) {

			    sourceIdentifiers.add(rpb.getPropertyValue());
			} else {
			    return new HashSet<>();
			}
		    } else {
			return new HashSet<>();
		    }
		}
		return sourceIdentifiers;
	    } else {
		return new HashSet<>();
	    }
	} else if (bond instanceof ResourcePropertyBond) {
	    ResourcePropertyBond rpb = (ResourcePropertyBond) bond;

	    if (rpb.getProperty() == ResourceProperty.SOURCE_ID) {
		sourceIdentifiers.add(rpb.getPropertyValue());
		return sourceIdentifiers;
	    } else {
		return new HashSet<>();
	    }
	} else {

	    return new HashSet<>();
	}

    }

    private Bond createOrBond(HashSet<Bond> otherBonds) {
	if (otherBonds.size() == 1) {
	    return otherBonds.iterator().next();
	} else {
	    return BondFactory.createOrBond(otherBonds.toArray(new Bond[] {}));
	}
    }

    private Bond createAndBond(HashSet<Bond> otherBonds) {
	if (otherBonds.size() == 1) {
	    return otherBonds.iterator().next();
	} else {
	    return BondFactory.createAndBond(otherBonds.toArray(new Bond[] {}));
	}
    }

    /**
     * Computes the negation normal form of the given bond with De Morgan's laws and resolving double negations. A
     * formula is in negation
     * normal form if the negation operator NOT is only applied to variables and the only other allowed Boolean
     * operators are AND , and OR.
     *
     * @param bond
     * @return
     * @throws GSException
     */
    private Bond getNegationNormalForm(Bond bond) throws GSException {
	// if bond is null, nothing has to be done
	if (bond == null) {
	    return null;
	}

	if (bond instanceof LogicalBond) {

	    LogicalBond logicalBond = (LogicalBond) bond;
	    switch (logicalBond.getLogicalOperator()) {
	    case NOT:
		Bond operand = logicalBond.getOperands().iterator().next();
		if (operand instanceof LogicalBond) {
		    LogicalBond childLogicalBond = (LogicalBond) operand;
		    LogicalOperator childOperator = childLogicalBond.getLogicalOperator();

		    switch (childOperator) {
		    case NOT:
			// NOT(NOT(BOND)) -> BOND
			Bond innerBond = childLogicalBond.getFirstOperand();
			// recursive call
			return getNegationNormalForm(innerBond);
		    case AND:
		    case OR:
			// NOT(P1 AND P2 AND ... PN) -> NOT(P1) OR NOT(P2) OR ... NOT(PN)
			// NOT(P1 OR P2 OR ... PN) -> NOT(P1) AND NOT(P2) AND ... NOT(PN)
			LogicalOperator newOperator = null;
			switch (childOperator) {
			case AND:
			    newOperator = LogicalOperator.OR;
			    break;
			case OR:
			    newOperator = LogicalOperator.AND;
			    break;
			default:
			    // not possible
			    break;
			}
			Set<Bond> newOperands = new HashSet<>();
			for (Bond childOperand : childLogicalBond.getOperands()) {
			    Bond newOperand = BondFactory.createNotBond(childOperand);
			    // recursive calls
			    Bond negationChild = getNegationNormalForm(newOperand);
			    newOperands.addAll(getHomogeneousOperands(newOperator, negationChild));
			}
			Bond ret = BondFactory.createLogicalBond(newOperator, newOperands);
			return ret;
		    default:
			throwUnexpectedSyntaxException(UNEXPECTED_LOGICAL_OPERATOR_MESSAGE_PREFIX + childOperator);
		    }
		}
		break;
	    case AND:
	    case OR:
		List<Bond> newOperands = new ArrayList<Bond>();
		for (Bond childOperand : logicalBond.getOperands()) {
		    // recursive calls
		    Bond negationChild = getNegationNormalForm(childOperand);
		    newOperands.addAll(getHomogeneousOperands(logicalBond.getLogicalOperator(), negationChild));
		}
		Bond ret = BondFactory.createLogicalBond(logicalBond.getLogicalOperator(), newOperands);
		return ret;

	    default:
		throwUnexpectedSyntaxException(UNEXPECTED_LOGICAL_OPERATOR_MESSAGE_PREFIX + logicalBond.getLogicalOperator());

	    }

	}
	return bond;

    }

    /**
     * Converts bond to disjunctive normal form by distributing AND over OR A disjunctive normal form (DNF) is a
     * normalization of a logical
     * formula which is a disjunction of conjunctive clauses.
     *
     * @param bond
     * @return
     * @throws GSException
     */
    public Bond getDisjunctiveNormalForm(Bond bond) throws GSException {
	if (bond == null) {
	    return null;
	}
	BondFormula bondFormula = new BondFormula(bond);
	Formula formula = bondFormula.getFormula();
	Formula dnf = formula.bdd().dnf();
	Bond ret = bondFormula.getBond(dnf);
	return ret;
    }

    /**
     * Aggregates simple bonds (e.g. not involving source bonds) when mixed with source bonds. E.g. A AND S1 AND S2 AND
     * B -> (A AND B) AND S1 AND S2
     * 
     * @param bond
     * @return
     * @throws GSException
     */
    public Bond getAggregateSimpleBonds(Bond bond) throws GSException {
	if (bond == null) {
	    return null;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    List<Bond> operands = lb.getOperands();
	    List<Bond> simpleBonds = new ArrayList<Bond>();
	    List<Bond> sourceRelatedBonds = new ArrayList<Bond>();
	    for (Bond child : operands) {
		if (isSourceRelatedBond(child)) {
		    Bond aggregatedChild = getAggregateSimpleBonds(child);
		    sourceRelatedBonds.add(aggregatedChild);
		} else {
		    simpleBonds.add(child);
		}
	    }
	    lb.getOperands().clear();
	    lb.getOperands().addAll(sourceRelatedBonds);
	    if (sourceRelatedBonds.isEmpty()) {
		lb.getOperands().addAll(simpleBonds);
	    } else if (!simpleBonds.isEmpty()) {
		if (simpleBonds.size() == 1) {
		    lb.getOperands().add(simpleBonds.get(0));
		} else {
		    // grouping
		    LogicalBond groupedBond = BondFactory.createLogicalBond(lb.getLogicalOperator(), simpleBonds);
		    lb.getOperands().add(groupedBond);
		}
	    }
	}
	return bond;
    }

    /**
     * Converts bond to disjunctive normal form by distributing AND over OR A disjunctive normal form (DNF) is a
     * normalization of a logical formula which is a disjunction of conjunctive clauses. Attention: bonds that are not
     * source related aren't distributed, because the aim is to obtain the normalization with respect to sources.
     *
     * @param bond
     * @return
     * @throws GSException
     */
    public Bond getDisjunctiveNormalFormNaive(Bond bond) throws GSException {
	// if bond is null, nothing has to be done
	if (bond == null) {
	    return null;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond logicalBond = (LogicalBond) bond;
	    LogicalOperator operator = logicalBond.getLogicalOperator();
	    List<Bond> operands = logicalBond.getOperands();
	    List<Bond> newOperands = new ArrayList<Bond>();
	    switch (operator) {
	    case AND:
		LogicalBond orBond = null;
		Set<Bond> restOperands = new HashSet<>();

		for (Bond child : operands) {
		    if (child instanceof LogicalBond) {
			LogicalBond childLogicalBond = (LogicalBond) child;
			LogicalOperator childOperator = childLogicalBond.getLogicalOperator();
			if (childOperator.equals(LogicalOperator.OR) && orBond == null && isSourceRelatedBond(childLogicalBond)) {
			    orBond = childLogicalBond;
			} else {
			    restOperands.add(child);
			}
		    } else {
			restOperands.add(child);
		    }
		}
		if (orBond == null) {
		    for (Bond child : operands) {
			// recursive calls
			Bond dnfChild = getDisjunctiveNormalFormNaive(child);
			Set<Bond> homogeneousOperands = getHomogeneousOperands(LogicalOperator.AND, dnfChild);
			newOperands.addAll(homogeneousOperands);
		    }
		    return BondFactory.createAndBond(newOperands);
		} else {

		    List<Bond> orOperands = orBond.getOperands();

		    for (Bond orOperand : orOperands) {
			HashSet<Bond> andOperands = new HashSet<>();
			andOperands.addAll(getHomogeneousOperands(LogicalOperator.AND, orOperand));
			for (Bond restOperand : restOperands) {
			    andOperands.addAll(getHomogeneousOperands(LogicalOperator.AND, restOperand));
			}
			Bond andGroup = null;
			if (andOperands.size() == 1) {
			    andGroup = andOperands.iterator().next();
			} else {
			    andGroup = BondFactory.createAndBond(andOperands);
			}
			// recursive call
			Bond newBond = getDisjunctiveNormalFormNaive(andGroup);
			newOperands.addAll(getHomogeneousOperands(LogicalOperator.OR, newBond));

		    }

		    return BondFactory.createOrBond(newOperands);
		}

	    case OR:
		for (Bond child : operands) {
		    // recursive calls
		    Bond dnfChild = getDisjunctiveNormalFormNaive(child);
		    newOperands.addAll(getHomogeneousOperands(LogicalOperator.OR, dnfChild));
		}
		return BondFactory.createLogicalBond(operator, newOperands);
	    case NOT:
		return bond;
	    default:
		throwUnexpectedSyntaxException(UNEXPECTED_LOGICAL_OPERATOR_MESSAGE_PREFIX + operator);
	    }
	}
	return bond;

    }

    private Set<Bond> getHomogeneousOperands(LogicalOperator operator, Bond dnfChild) {
	Set<Bond> newOperands = new HashSet<>();
	if (dnfChild instanceof LogicalBond) {
	    LogicalBond dnfChildLogicalBond = (LogicalBond) dnfChild;
	    if (dnfChildLogicalBond.getLogicalOperator().equals(operator)) {
		// the child operator is homogeneous with the parent operator, hence only the children are added
		newOperands.addAll(dnfChildLogicalBond.getOperands());
	    } else {
		// otherwise is added en toto
		newOperands.add(dnfChild);
	    }
	} else {
	    newOperands.add(dnfChild);
	}
	return newOperands;
    }

    private Bond simplifyConjunctions(Bond bond) throws GSException {
	// if bond is null, nothing has to be done
	if (bond == null) {
	    return null;
	}

	if (bond instanceof LogicalBond) {
	    LogicalBond logicalBond = (LogicalBond) bond;
	    List<Bond> operands = logicalBond.getOperands();
	    List<Bond> newOperands = new ArrayList<Bond>();
	    switch (logicalBond.getLogicalOperator()) {
	    case OR: // we are in the outer or
		for (Bond child : operands) {
		    Bond newOperand = simplifyConjunctions(child);
		    if (newOperand != null) {
			newOperands.add(newOperand);
		    }
		}
		if (newOperands.isEmpty()) {
		    return null;
		}
		if (newOperands.size() == 1) {
		    return newOperands.iterator().next();
		}
		return BondFactory.createOrBond(newOperands);

	    case AND:
		// we are in a composed leaf, let's simplify it
		Set<ResourcePropertyBond> positiveSourceBonds = new HashSet<>();
		Set<ResourcePropertyBond> negativeSourceBonds = new HashSet<>();

		for (Bond child : operands) {
		    if (isSourceIdentifierBond(child)) {
			ResourcePropertyBond sourceBond = (ResourcePropertyBond) child;
			positiveSourceBonds.add(sourceBond);
		    } else if (child instanceof LogicalBond) {
			LogicalBond logicalChild = (LogicalBond) child;
			if (logicalChild.getLogicalOperator().equals(LogicalOperator.NOT)
				&& isSourceIdentifierBond(logicalChild.getFirstOperand())) {
			    negativeSourceBonds.add((ResourcePropertyBond) logicalChild.getFirstOperand());
			} else {
			    newOperands.add(child);
			}
		    } else {
			newOperands.add(child);
		    }
		}
		// check for S1 AND S2 -> false
		if (positiveSourceBonds.size() > 1) {
		    return null;
		}
		// check for S1 AND NOT(S1) -> false
		for (ResourcePropertyBond positiveBond : positiveSourceBonds) {
		    for (ResourcePropertyBond negativeBond : negativeSourceBonds) {
			if (positiveBond.equals(negativeBond)) {
			    return null;
			}
		    }
		}
		// check for S1 AND NOT(S2) -> S1
		if (!negativeSourceBonds.isEmpty() && !positiveSourceBonds.isEmpty()) {
		    negativeSourceBonds.clear();
		}
		for (ResourcePropertyBond negativeBond : negativeSourceBonds) {
		    newOperands.add(BondFactory.createNotBond(negativeBond));
		}
		for (ResourcePropertyBond positiveBond : positiveSourceBonds) {
		    newOperands.add(positiveBond);
		}

		if (newOperands.isEmpty()) {
		    return null;
		}
		if (newOperands.size() == 1) {
		    return newOperands.iterator().next();
		}
		return BondFactory.createAndBond(newOperands);

	    case NOT:
		// nothing to do, we are in the leaf
		return bond;
	    default:
		break;
	    }

	}

	return bond;
    }

    /**
     * aggregates conjunctions containing the same source bond (e.g. (S1 AND B1) OR (S1 AND B2) -> S1 AND (B1 OR B2)
     *
     * @param bond the input bond
     * @return an aggregated bond
     * @throws GSException
     */
    private Bond aggregateConjunctions(Bond bond) throws GSException {
	// if bond is null, nothing has to be done
	if (bond == null) {
	    return null;
	}

	if (bond instanceof LogicalBond) {
	    LogicalBond logicalBond = (LogicalBond) bond;
	    List<Bond> operands = logicalBond.getOperands();
	    List<Bond> newOperands = new ArrayList<Bond>();
	    HashMap<ResourcePropertyBond, HashSet<Bond>> finalBonds = new HashMap<>();
	    switch (logicalBond.getLogicalOperator()) {
	    case OR: // we are in the outer or

		SourceBondHandler sbp = new SourceBondHandler(logicalBond);

		if (sbp.getSourceIdentifiers().isEmpty()) {
		    // nothing to aggregate
		    return bond;
		}

		if (sbp.getSourceIdentifiers().size() == 1) {
		    // nothing to aggregate
		    return bond;
		}

		for (Bond child : operands) {
		    ResourcePropertyBond sourceBond = null;
		    HashSet<Bond> otherBondsInOr = new HashSet<>();
		    if (child instanceof LogicalBond) {
			LogicalBond logicalChild = (LogicalBond) child;
			if (logicalChild.getLogicalOperator().equals(LogicalOperator.AND)) {
			    HashSet<Bond> otherBondsInAnd = new HashSet<>();
			    for (Bond grandChildBond : logicalChild.getOperands()) {
				if (isSourceIdentifierBond(grandChildBond)) {
				    sourceBond = (ResourcePropertyBond) grandChildBond;
				} else {
				    if (grandChildBond instanceof LogicalBond) {
					LogicalBond grandLogicalChildBond = (LogicalBond) grandChildBond;
					if (grandLogicalChildBond.getLogicalOperator().equals(LogicalOperator.NOT)
						&& isSourceIdentifierBond(grandLogicalChildBond.getFirstOperand())) {

					    throwNegatedSourceException();

					}
				    }
				    otherBondsInAnd.add(grandChildBond);
				}
			    }
			    Bond andBond = createAndBond(otherBondsInAnd);
			    otherBondsInOr.add(andBond);
			} else if (logicalChild.getLogicalOperator().equals(LogicalOperator.NOT)) {
			    if (isSourceIdentifierBond(logicalChild.getFirstOperand())) {
				throwNegatedSourceException();
			    } else {
				otherBondsInOr.add(child);
			    }
			} else {
			    // OR

			    throw GSException.createException(//
				    getClass(), //
				    "Normalization failed because a nested OR was found in the disjunctive normal form", //
				    "Query normalization failed unexpectedly", ErrorInfo.ERRORTYPE_INTERNAL, //
				    ErrorInfo.SEVERITY_ERROR, //
				    NORMALIZATION_FAILED_IRREDUCIBLE_INPUT);

			}
		    } else if (isSourceIdentifierBond(child)) {
			sourceBond = (ResourcePropertyBond) child;
			otherBondsInOr.add(sourceBond);

		    } else {

			otherBondsInOr.add(child);
		    }

		    HashSet<Bond> storedBond = finalBonds.get(sourceBond);
		    if (storedBond != null) {
			otherBondsInOr.addAll(storedBond);
		    }
		    finalBonds.put(sourceBond, otherBondsInOr);
		}
		HashSet<Bond> restBondsInOr = finalBonds.get(null);
		Bond restBonds = null;
		if (restBondsInOr != null) {
		    if (restBondsInOr.size() == 1) {
			restBonds = restBondsInOr.iterator().next();
		    }
		    if (restBondsInOr.size() > 1) {
			restBonds = BondFactory.createOrBond(restBondsInOr);
		    }
		}

		for (Map.Entry<ResourcePropertyBond, HashSet<Bond>> entry : finalBonds.entrySet()) {

		    ResourcePropertyBond sourceBond = entry.getKey();

		    if (sourceBond != null) {
			HashSet<Bond> otherBondsInOr = finalBonds.get(sourceBond);
			ResourcePropertyBond innerSourceBondBond = null;
			for (Bond otherBond : otherBondsInOr) {
			    if (isSourceIdentifierBond(otherBond)) {
				innerSourceBondBond = (ResourcePropertyBond) otherBond;
			    }
			}
			if (innerSourceBondBond == null) {
			    if (restBonds != null) {
				otherBondsInOr.addAll(getHomogeneousOperands(LogicalOperator.OR, restBonds));
			    }
			    Bond orBond = createOrBond(otherBondsInOr);
			    Set<Bond> homogeneousOperands = getHomogeneousOperands(LogicalOperator.AND, orBond);
			    homogeneousOperands.add(sourceBond);
			    newOperands.add(BondFactory.createAndBond(homogeneousOperands));
			} else {
			    // the inner source bond wins to all other bonds
			    newOperands.add(innerSourceBondBond);
			}
		    }
		}
		if (newOperands.isEmpty()) {
		    return null;
		}
		if (newOperands.size() == 1) {
		    return newOperands.iterator().next();
		}
		return BondFactory.createOrBond(newOperands);
	    case NOT:

		if (isSourceIdentifierBond(operands.iterator().next())) {
		    throwNegatedSourceException();

		}
		// nothing to do, we are in the leaf
		return bond;

	    case AND:
	    default:
		// we are in a leaf
		for (Bond operand : operands) {
		    if (operand instanceof LogicalBond) {
			LogicalBond childLogicalBond = (LogicalBond) operand;
			if (childLogicalBond.getLogicalOperator().equals(LogicalOperator.NOT)
				&& isSourceIdentifierBond(childLogicalBond.getFirstOperand())) {

			    throwNegatedSourceException();

			}
		    }
		}
		return bond;

	    }
	} else {

	    return bond;
	}
    }

    /**
     * @throws GSException
     */
    private void throwNegatedSourceException() throws GSException {

	throw GSException.createException(//
		getClass(), //
		"Normalization failed because of negated source bond", //
		"Query normalization failed, you could try to write a simpler query", //
		ErrorInfo.ERRORTYPE_CLIENT, //
		ErrorInfo.SEVERITY_ERROR, //
		NORMALIZATION_FAILED_IRREDUCIBLE_INPUT);

    }

    /**
     * @param message
     * @throws GSException
     */
    private void throwUnexpectedSyntaxException(String message) throws GSException {

	throw GSException.createException(//
		getClass(), //
		message, //
		"Query normalization failed, because of an internal error", //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		NORMALIZATION_FAILED_UNEXPECTED_SYNTAX);
    }

    private boolean isSourceRelatedBond(Bond bond) {
	if (bond == null) {
	    return false;
	}
	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    List<Bond> operands = lb.getOperands();
	    for (Bond child : operands) {
		if (isSourceRelatedBond(child)) {
		    return true;
		}
	    }
	    return false;
	} else {
	    return isSourceIdentifierBond(bond);
	}
    }

    private boolean isSourceIdentifierBond(Bond bond) {

	if (bond == null) {
	    return false;
	}
	boolean bool = BondFactory.isResourcePropertyBond(bond, ResourceProperty.SOURCE_ID);
	if (bool) {
	    return (((ResourcePropertyBond) bond).getOperator().equals(BondOperator.EQUAL));
	}
	return bool;
    }

    private boolean isSourceIdentifierLikeBond(Bond bond) {

	if (bond == null) {
	    return false;
	}
	boolean bool = BondFactory.isResourcePropertyBond(bond, ResourceProperty.SOURCE_ID);
	if (bool) {
	    return (((ResourcePropertyBond) bond).getOperator().equals(BondOperator.TEXT_SEARCH));
	}
	return bool;
    }

    @Override
    public void setRequestAuthorizationConverter(IRequestAuthorizationConverter requestAuthorizationConverter) {
	this.requestAuthorizationConverter = requestAuthorizationConverter;

    }

}
