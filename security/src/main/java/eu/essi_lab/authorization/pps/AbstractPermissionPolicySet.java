/**
 * 
 */
package eu.essi_lab.authorization.pps;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.combining.StandardCombiningAlgorithm;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import eu.essi_lab.authorization.PolicySetWrapper;
import eu.essi_lab.authorization.builder.PPSBuilder;
import eu.essi_lab.authorization.builder.PPSPolicyBuilder;
import eu.essi_lab.authorization.builder.PPSRuleBuilder;
import eu.essi_lab.authorization.xacml.XACML_JAXBUtils;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Policy;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public abstract class AbstractPermissionPolicySet implements PolicySetWrapper {

    private String role;
    private PolicySet policySet;
    private String ruleCombiningAlgorithm;
    private HashMap<String, PPSRuleBuilder> buildersMap;

    /**
     * 
     */
    public static final int DEFAULT_OFFSET_LIMIT = 200;

    /**
     * 
     */
    public static final int DEFAULT_MAX_RECORDS_LIMIT = 50;

    /**
     * 
     */
    public static final int ANONYMOUS_MAX_RECORDS_LIMIT = 10;

    /**
     * 
     */
    public static final String VIEW_ID_MISSING_VALUE = "viewMissing";

    /**
     * 
     */
    public static final String VIEW_CREATOR_MISSING_VALUE = "viewCreatorMissing";

    /**
     * @param role
     */
    public AbstractPermissionPolicySet(String role) {

	this(role, StandardCombiningAlgorithm.XACML_3_0_RULE_COMBINING_PERMIT_OVERRIDES.getId());
    }

    /**
     * @param role
     * @param ruleCombiningAlgorithm
     */
    public AbstractPermissionPolicySet(String role, String ruleCombiningAlgorithm) {

	this.role = role;
	this.ruleCombiningAlgorithm = ruleCombiningAlgorithm;
	this.buildersMap = new HashMap<String, PPSRuleBuilder>();
    }

    /**
     * @return
     */
    public String getRole() {

	return role;
    }

    @Override
    public PolicySet getPolicySet() {

	if (policySet == null) {

	    editPPSPolicy();

	    PPSBuilder ppsBuilder = new PPSBuilder(role);

	    PPSPolicyBuilder ppsPolicyBuilder = new PPSPolicyBuilder(//
		    "PPS:" + role + ":role:policy", //
		    ruleCombiningAlgorithm);

	    buildersMap.entrySet().forEach(b -> ppsPolicyBuilder.addRule(b.getValue().build()));

	    Policy policy = ppsPolicyBuilder.build();

	    ppsBuilder.addPolicy(policy);

	    policySet = ppsBuilder.build();
	}

	return policySet;
    }

    /**
     * 
     */
    protected abstract void editPPSPolicy();

    /**
     * @param action
     * @param ruleId
     */
    private void setAction(Action action, String ruleId) {

	AnyOf accessAnyOf = XACML_JAXBUtils.createAnyOfAllOfMatch(//
		StandardFunction.STRING_EQUAL.getId(), //
		action.getId(), //
		StandardDatatypes.STRING.getId(),

		null, //
		XacmlAttributeCategory.XACML_3_0_ACTION.value(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		true);

	addRule(ruleId, accessAnyOf);
    }

    /**
     * 
     */
    protected void setAccessAction(String ruleId) {

	setAction(Action.ACCESS, ruleId);
    }

    /**
     * 
     */
    protected void setDiscoveryAction(String ruleId) {

	setAction(Action.DISCOVERY, ruleId);
    }

    /**
     * 
     */
    protected void setReadViewAction(String ruleId) {

	setAction(Action.READ_VIEW, ruleId);
    }

    /**
     * 
     */
    protected void setCreateViewAction(String ruleId) {

	setAction(Action.CREATE_VIEW, ruleId);
    }

    /**
     * 
     */
    protected void setUpdateViewAction(String ruleId) {

	setAction(Action.UPDATE_VIEW, ruleId);
    }

    /**
     * 
     */
    protected void setDeleteViewAction(String ruleId) {

	setAction(Action.DELETE_VIEW, ruleId);
    }

    /**
     * 
     */
    protected void setOtherAction(String ruleId) {

	setAction(Action.OTHER, ruleId);
    }

    /**
     * @param maxDownload
     */
    protected void setDownloadLimit(String ruleId, int maxDownload) {

	AnyOf downloadAnyOf = XACML_JAXBUtils.createAnyOfAllOfMatch(

		Function.XACML_NS_1_0 + "integer-greater-than-or-equal", //
		String.valueOf(maxDownload), //
		StandardDatatypes.INTEGER.getId(), //
		Issuer.DOWNLOAD.getId(), //
		XacmlAttributeCategory.XACML_3_0_RESOURCE.value(), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value(), //
		StandardDatatypes.INTEGER.getId(), //
		true);

	addRule(ruleId, downloadAnyOf);
    }

    /**
     * 
     */
    protected void setUnapplicableRule(String ruleId) {

	setOffsetLimit(ruleId, Integer.MIN_VALUE);
	setMaxRecordsLimit(ruleId, Integer.MIN_VALUE);
    }

    /**
     * @param offset
     */
    protected void setOffsetLimit(String ruleId, int offset) {

	AnyOf offsetAnyOf = XACML_JAXBUtils.createAnyOfAllOfMatch(

		Function.XACML_NS_1_0 + "integer-greater-than-or-equal", //
		String.valueOf(offset), //
		StandardDatatypes.INTEGER.getId(), //
		Issuer.OFFSET.getId(), //

		XacmlAttributeCategory.XACML_3_0_RESOURCE.value(), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value(), //
		StandardDatatypes.INTEGER.getId(), //
		true);

	addRule(ruleId, offsetAnyOf);
    }

    /**
     * @param maxRecords
     */
    protected void setMaxRecordsLimit(String ruleId, int maxRecords) {

	AnyOf maxRecordsAnyOf = XACML_JAXBUtils.createAnyOfAllOfMatch(

		Function.XACML_NS_1_0 + "integer-greater-than-or-equal", //
		String.valueOf(maxRecords), //
		StandardDatatypes.INTEGER.getId(), //
		Issuer.MAX_RECORDS.getId(), //

		XacmlAttributeCategory.XACML_3_0_RESOURCE.value(), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value(), //
		StandardDatatypes.INTEGER.getId(), //
		true);

	addRule(ruleId, maxRecordsAnyOf);
    }

    /**
     * @param identifiers
     * @return
     */
    protected ApplyType createAllowedClientIdentifiersApply(String... identifiers) {

	//
	// creates the and condition
	//
	AttributeDesignatorType pathType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.CLIENT_IDENTIFIER.getId(), //
		false);//

	return XACML_JAXBUtils.createAtLeastAnyOfApply(Arrays.asList(identifiers), pathType);

    }

    /**
     * @param paths
     * @return
     */
    protected ApplyType createPathApply(String... paths) {

	//
	// creates condition
	//
	AttributeDesignatorType pathType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.PATH.getId(), //
		true);//

	return XACML_JAXBUtils.createAtLeastAnyOfApply(Arrays.asList(paths), pathType);
    }

    /**
     * @param paths
     * @return
     */
    protected ApplyType createViewVisibilityApply(ViewVisibility viewVisibility) {

	//
	// creates condition
	//
	AttributeDesignatorType type = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.VIEW_VISIBILITY.getId(), //
		true);//

	return XACML_JAXBUtils.createStringEqualApply(viewVisibility.name(), type);
    }

    /**
     * @param viewIds
     * @return
     */
    protected ApplyType createViewOwnerApply(String viewOwner) {

	AttributeDesignatorType type = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.VIEW_OWNER.getId(), //
		true);//

	return XACML_JAXBUtils.createStringEqualApply(viewOwner, type);
    }

    /**
     * @param viewIds
     * @return
     */
    protected ApplyType createViewIdentifiersApply(String... viewIds) {

	AttributeDesignatorType viewType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.VIEW_ID.getId(), //
		true);//

	return XACML_JAXBUtils.createAtLeastAnyOfApply(Arrays.asList(viewIds), viewType);
    }

    /**
     * @return
     */
    protected ApplyType createNoViewIdentifiersApply() {

	AttributeDesignatorType viewType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.VIEW_ID.getId(), //
		true);//

	return XACML_JAXBUtils.createAtLeastAnyOfApply(Arrays.asList(VIEW_ID_MISSING_VALUE), viewType);
    }

    /**
     * @return
     */
    protected ApplyType createNoCreatorIdentifiersApply() {

	AttributeDesignatorType viewType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.VIEW_CREATOR.getId(), //
		true);//

	return XACML_JAXBUtils.createAtLeastAnyOfApply(Arrays.asList(VIEW_ID_MISSING_VALUE), viewType);
    }

    /**
     * @param origin
     * @return
     */
    protected ApplyType createOriginHeaderApply(String... origin) {

	AttributeDesignatorType viewCreatorType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.ORIGIN.getId(), //
		true);//

	return XACML_JAXBUtils.createAtLeastAnyOfApply(Arrays.asList(origin), viewCreatorType);
    }

    /**
     * @param viewCreator
     * @return
     */
    protected ApplyType createViewCreatorApply(String viewCreator) {

	AttributeDesignatorType viewCreatorType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.VIEW_CREATOR.getId(), //
		true);//

	return XACML_JAXBUtils.createStringEqualApply(viewCreator, viewCreatorType);
    }

    /**
     * @param allowedIPs
     * @return
     */
    protected ApplyType createAllowedIPApply(String... allowedIPs) {

	AttributeDesignatorType type = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT.value(), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.ALLOWED_IP.getId(), //
		true);//

	return XACML_JAXBUtils.createAtLeastAnyOfApply(Arrays.asList(allowedIPs), type);
    }

    /**
     * @param sources
     * @return
     */
    protected ApplyType createSourcesApply(String... sources) {

	AttributeDesignatorType sourceType = new AttributeDesignatorType(//
		XacmlAttributeCategory.XACML_3_0_RESOURCE.value(), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value(), //
		StandardDatatypes.STRING.getId(), //
		Issuer.SOURCE.getId(), //
		true);//

	return XACML_JAXBUtils.createAnyOfAllApply(Arrays.asList(sources), sourceType, StandardFunction.STRING_EQUAL);
    }

    /**
     * @param applyTypes
     */
    protected void setAndCondition(String ruleId, ApplyType... applyTypes) {

	ApplyType toApply = null;

	if (applyTypes.length == 1) {

	    toApply = applyTypes[0];

	} else {

	    toApply = XACML_JAXBUtils.createLogicalApplyType(StandardFunction.AND, Arrays.asList(applyTypes));
	}

	Condition condition = new Condition(ObjectFactories.XACML().createApply(toApply));

	getBuilder(ruleId).setCondition(condition);
    }

    /**
     * @param applyTypes
     */
    protected void setOrCondition(String ruleId, ApplyType... applyTypes) {

	ApplyType toApply = null;

	if (applyTypes.length == 1) {

	    toApply = applyTypes[0];

	} else {

	    toApply = XACML_JAXBUtils.createLogicalApplyType(StandardFunction.OR, Arrays.asList(applyTypes));
	}

	Condition condition = new Condition(ObjectFactories.XACML().createApply(toApply));

	getBuilder(ruleId).setCondition(condition);
    }

    /**
     * @param applyTypes
     */
    protected ApplyType createORApply(ApplyType... applyTypes) {

	ApplyType toApply = null;

	if (applyTypes.length == 1) {

	    toApply = applyTypes[0];

	} else {

	    toApply = XACML_JAXBUtils.createLogicalApplyType(StandardFunction.OR, Arrays.asList(applyTypes));
	}

	return toApply;
    }

    /**
     * @param applyTypes
     */
    protected ApplyType createANDApply(ApplyType... applyTypes) {

	ApplyType toApply = null;

	if (applyTypes.length == 1) {

	    toApply = applyTypes[0];

	} else {

	    toApply = XACML_JAXBUtils.createLogicalApplyType(StandardFunction.AND, Arrays.asList(applyTypes));
	}

	return toApply;
    }

    /**
     * @return
     */
    protected ApplyType createDiscoveryPathApply() {

	return createPathApply(//
		"opensearch", //
		"bnhs", //
		"thredds", //
		"csw", //
		"cswisogeo", //
		"oaipmh", //
		"hiscentral.asmx", //
		"hiscentralitaly.asmx", //
		"arpa-rest", //
		"cuahsi_1_1.asmx", //
		"rest", //
		"hydrocsv", //
		"sos", //
		"wfs", //
		"wms-extent", //
		"oapi", //
		"gwis", //
		"ArcGIS", //
		"ArcGISProxy", //
		"gwps", //
		"timeseries-api", //"timeseries-api", //
		"om-api", //
		"terms-api", //
		"semantic", "rest-views");
    }

    /**
     * @return
     */
    protected ApplyType createAccessPathApply() {

	return createPathApply(//
		"cuahsi_1_1.asmx", //
		"rest", //
		"hydrocsv", //
		"thredds", //
		"wms", //
		"wms-extent", //
		"oapi", //
		"wfs", //
		"sos", //
		"gwps", //
		"ArcGIS", //
		"ArcGISProxy", //
		"timeseries-api", //
		"om-api", //
		"terms-api", //
		"gwis");
    }

    /**
     * @return
     */
    protected String createRandomId() {

	return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * @param ruleId
     * @return
     */
    private PPSRuleBuilder getBuilder(String ruleId) {

	PPSRuleBuilder ppsRuleBuilder = buildersMap.get(ruleId);
	if (ppsRuleBuilder == null) {
	    ppsRuleBuilder = new PPSRuleBuilder(ruleId);
	    buildersMap.put(ruleId, ppsRuleBuilder);
	}

	return ppsRuleBuilder;
    }

    /**
     * @param ruleId
     * @param discoveryAnyOf
     */
    private void addRule(String ruleId, AnyOf discoveryAnyOf) {

	PPSRuleBuilder ppsRuleBuilder = buildersMap.get(ruleId);
	if (ppsRuleBuilder == null) {
	    ppsRuleBuilder = new PPSRuleBuilder(ruleId);
	    buildersMap.put(ruleId, ppsRuleBuilder);
	}

	getBuilder(ruleId).addTargetAnyOf(discoveryAnyOf);
    }
}
