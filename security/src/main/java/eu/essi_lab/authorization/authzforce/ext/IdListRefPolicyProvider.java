/**
 * 
 */
package eu.essi_lab.authorization.authzforce.ext;

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

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.combining.CombiningAlgRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.policy.BaseStaticRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.CloseableRefPolicyProvider;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersionPatterns;
import org.ow2.authzforce.core.pdp.api.policy.StaticTopLevelPolicyElementEvaluator;
import org.ow2.authzforce.core.pdp.impl.policy.PolicyEvaluators;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import eu.essi_lab.authorization.PdpEngineBuilder;
import eu.essi_lab.authorization.PolicySetWrapper;
import eu.essi_lab.authorization.psloader.PolicySetLoader;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public class IdListRefPolicyProvider extends BaseStaticRefPolicyProvider {

    private final ExpressionFactory expressionFactory;
    private final CombiningAlgRegistry combiningAlgRegistry;
    private static PolicySetLoader loader;
    
    /**
     * 
     * @param loader
     */
    public static void setPolicySetLoader(PolicySetLoader loader) {
	
	IdListRefPolicyProvider.loader = loader;	
    }

    private IdListRefPolicyProvider(

	    final ExpressionFactory expressionFactory, //
	    final XmlnsFilteringParserFactory xacmlParserFactory, //
	    final CombiningAlgRegistry combiningAlgRegistry) {

	super(UNLIMITED_POLICY_REF_DEPTH);

	this.expressionFactory = expressionFactory;
	this.combiningAlgRegistry = combiningAlgRegistry;
    }

    public static class Factory extends CloseableRefPolicyProvider.Factory<IdListPolicyProvider> {

	private static final IllegalArgumentException ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
		"Undefined CombiningAlgorithm registry");
	private static final IllegalArgumentException ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
		"Undefined Expression factory");
	private static final IllegalArgumentException ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
		"Undefined XACML parser factory");
	private static final IllegalArgumentException NULL_CONF_ARGUMENT_EXCEPTION = new IllegalArgumentException(
		"PolicyProvider configuration undefined");

	private static PolicySet rootPolicySet;

	@Override
	public Class<IdListPolicyProvider> getJaxbClass() {

	    return IdListPolicyProvider.class;
	}

	@Override
	public CloseableRefPolicyProvider getInstance(

		final IdListPolicyProvider idListProvider, //
		final XmlnsFilteringParserFactory xmlParserFactory, //
		final int maxPolicySetRefDepth, //
		final ExpressionFactory expressionFactory, //
		final CombiningAlgRegistry combiningAlgRegistry, //
		final EnvironmentProperties environmentProperties) throws IllegalArgumentException {

	    if (idListProvider == null) {
		throw NULL_CONF_ARGUMENT_EXCEPTION;
	    }

	    rootPolicySet = idListProvider.getRootPolicySet();

	    if (xmlParserFactory == null) {
		throw ILLEGAL_XACML_PARSER_FACTORY_ARGUMENT_EXCEPTION;
	    }

	    if (expressionFactory == null) {
		throw ILLEGAL_EXPRESSION_FACTORY_ARGUMENT_EXCEPTION;
	    }

	    if (combiningAlgRegistry == null) {
		throw ILLEGAL_COMBINING_ALG_REGISTRY_ARGUMENT_EXCEPTION;
	    }

	    return new IdListRefPolicyProvider(expressionFactory, xmlParserFactory, combiningAlgRegistry);
	}
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public StaticTopLevelPolicyElementEvaluator getPolicy(//
	    final String policyId, //
	    final Optional<PolicyVersionPatterns> policyPolicyVersionPatterns) throws IndeterminateEvaluationException {

	Optional<Policy> policy = PolicySetWrapper.getPolicy(//
		loader.loadPermissionPolicySets(), //
		policyId);

	if (policy.isPresent()) {

	    try {

		return PolicyEvaluators.getInstance(//
			policy.get(), //
			null, //
			new HashMap<String, String>(), //
			expressionFactory, //
			combiningAlgRegistry);

	    } catch (final IllegalArgumentException e) {

		throw e;
	    }
	}

	throw new IllegalArgumentException("Policy " + policyId + " not found");
    }

    @Override
    public StaticTopLevelPolicyElementEvaluator getPolicySet(//
	    final String policyId, //
	    final Optional<PolicyVersionPatterns> policyPolicyVersionPatterns, //
	    final Deque<String> policySetRefChain)

	    throws IndeterminateEvaluationException {

	Optional<PolicySet> policySet = Optional.empty();

	if (policyId.equals(PdpEngineBuilder.ROOT_POLICY_SET_ID)) {

	    policySet = Optional.of(Factory.rootPolicySet);

	} else {

	    policySet = PolicySetWrapper.getPolicySet(//
		    loader.loadPermissionPolicySets(), //
		    policyId);

	    if (!policySet.isPresent()) {

		policySet = PolicySetWrapper.getPolicySet(//
			loader.loadRolePolicySets(), //
			policyId);
	    }
	}

	try {
	    return PolicyEvaluators.getInstanceStatic(//
		    policySet.get(), //
		    null, //
		    new HashMap<String, String>(), //
		    expressionFactory, //
		    combiningAlgRegistry, //
		    this, //
		    policySetRefChain);

	} catch (final IllegalArgumentException e) {

	    throw new IndeterminateEvaluationException(//
		    e.getMessage(), //
		    XacmlStatusCode.PROCESSING_ERROR.value(), //
		    e);
	}
    }

}
