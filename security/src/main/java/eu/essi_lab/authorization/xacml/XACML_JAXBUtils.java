/**
 * 
 */
package eu.essi_lab.authorization.xacml;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.ow2.authzforce.core.pdp.api.func.FirstOrderBagFunctions.AtLeastOneMemberOf;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderBagFunctions.PrimitiveToBag;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.impl.func.StandardFunction;

import eu.essi_lab.jaxb.common.ObjectFactories;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Match;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

/**
 * @author Fabrizio
 */
public class XACML_JAXBUtils {

    /**
     * @param logicalFunction
     * @param operands
     * @return
     */
    public static ApplyType createLogicalApplyType(StandardFunction logicalFunction, List<ApplyType> operands) {

	if (logicalFunction != StandardFunction.OR && logicalFunction != StandardFunction.AND) {
	    throw new IllegalArgumentException("Not logical function");
	}

	List<JAXBElement<? extends ExpressionType>> list = new ArrayList<>();

	for (ApplyType apply : operands) {

	    list.add(ObjectFactories.XACML().createApply(apply));
	}

	return new ApplyType(null, list, logicalFunction.getId());
    }

    /**
     * At least one element provided by the request designator must match the elements in the <code>values</code> list.
     * This kind of condition can always been used if the request has a bag of a single element<br>
     * <br>
     * <code>values</code>: A,B,C - Request: A,B,C ---> OK<br>
     * <code>values</code>: A,B,C - Request: A,B ---> OK<br>
     * <code>values</code>: A,B,C - Request: A,C ---> OK<br>
     * <code>values</code>: A,B,C - Request: A ---> OK<br>
     * <code>values</code>: A,B,C - Request: C ---> OK <br>
     * <code>values</code>: A,B,C - Request: A,B,D ---> OK<br>
     * <code>values</code>: A,B,C - Request: A,D ---> OK<br>
     * <code>values</code>: A,B,C - Request: D ---> NO<br>
     * 
     * @param values
     * @param attributeDesignatorType
     * @return
     */
    public static ApplyType createAtLeastAnyOfApply(List<String> values, AttributeDesignatorType attributeDesignatorType) {

	String xacmlNs10 = AtLeastOneMemberOf.XACML_NS_1_0;

	List<JAXBElement<? extends ExpressionType>> outerList = new ArrayList<>();
	ApplyType outerApply = null;

	{

	    JAXBElement<AttributeDesignatorType> attributeDesignator = ObjectFactories.XACML()
		    .createAttributeDesignator(attributeDesignatorType);

	    outerList.add(attributeDesignator);

	    String nameSuffixAtLeastOneMemberOf = AtLeastOneMemberOf.NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF;

	    String functionId = xacmlNs10 + "string" + nameSuffixAtLeastOneMemberOf;

	    outerApply = new ApplyType(null, outerList, functionId);
	}

	{
	    List<JAXBElement<? extends ExpressionType>> innerList = new ArrayList<>();

	    for (String value : values) {

		AttributeValueType attributeValueType = createAttribute(value, StandardDatatypes.STRING.getId());

		innerList.add(ObjectFactories.XACML().createAttributeValue(attributeValueType));
	    }

	    String nameSuffixBag = PrimitiveToBag.NAME_SUFFIX_BAG;

	    String functionId = xacmlNs10 + "string" + nameSuffixBag;

	    ApplyType innerApplyType = new ApplyType(null, innerList, functionId);

	    outerList.add(ObjectFactories.XACML().createApply(innerApplyType));
	}

	return outerApply;
    }

    /**
     * @param apply
     * @return
     */
    public static ApplyType createNotApply(ApplyType apply) {

	List<JAXBElement<? extends ExpressionType>> list = new ArrayList<>();

	list.add(ObjectFactories.XACML().createApply(apply));

	return new ApplyType(null, list, StandardFunction.NOT.getId());
    }

    /**
     * @param value
     * @param attributeDesignatorType
     * @return
     */
    public static ApplyType createStringEqualApply(String value, AttributeDesignatorType attributeDesignatorType) {

	String xacmlNs10 = AtLeastOneMemberOf.XACML_NS_1_0;

	//
	// in the inner apply there is the AttributeDesignator and the function is
	// urn:oasis:names:tc:xacml:1.0:function:string-one-and-only
	//
	JAXBElement<AttributeDesignatorType> attributeDesignator = ObjectFactories.XACML()
		.createAttributeDesignator(attributeDesignatorType);

	// creates the inner list
	List<JAXBElement<? extends ExpressionType>> innerList = new ArrayList<>();

	innerList.add(attributeDesignator);

	String functionId = xacmlNs10 + "string-one-and-only";

	ApplyType innerApply = new ApplyType(null, innerList, functionId);

	// creates the outer list
	List<JAXBElement<? extends ExpressionType>> outerList = new ArrayList<>();

	//
	// creates the attribute value which goes in the outer apply
	//
	AttributeValueType attributeValueType = createAttribute(value, StandardDatatypes.STRING.getId());

	//
	// adds the inner apply and the attribute in the outer list
	//
	outerList.add(ObjectFactories.XACML().createApply(innerApply));
	outerList.add(ObjectFactories.XACML().createAttributeValue(attributeValueType));

	//
	// creates the outer apply with the function urn:oasis:names:tc:xacml:1.0:function:string-equal
	//
	return new ApplyType(null, outerList, StandardFunction.STRING_EQUAL.getId());
    }

    /**
     * All the elements provided by the request designator must match the elements in the <code>values</code> list. This
     * condition should be used if the request provides a bag of more than one element, or if the elements
     * must be compared with the "contains" function instead of with the "equal" function, otherwise it is equivalent to
     * {@link #createAtLeastAnyOfApply(List, AttributeDesignatorType)} <br>
     * <br>
     * <code>values</code>: A,B,C - Request: A,B,C ---> OK<br>
     * <code>values</code>: A,B,C - Request: A,B ---> OK<br>
     * <code>values</code>: A,B,C - Request: A,C ---> OK<br>
     * <code>values</code>: A,B,C - Request: A ---> OK<br>
     * <code>values</code>: A,B,C - Request: C ---> OK <br>
     * <code>values</code>: A,B,C - Request: A,B,D ---> NO<br>
     * <code>values</code>: A,B,C - Request: A,D ---> NO<br>
     * <code>values</code>: A,B,C - Request: D ---> NO<br>
     * 
     * @param values
     * @param attributeDesignatorType
     * @param function
     * @return
     */
    public static ApplyType createAnyOfAllApply(//
	    List<String> values, //
	    AttributeDesignatorType attributeDesignatorType, //
	    StandardFunction function) {

	String xacmlNs10 = AtLeastOneMemberOf.XACML_NS_1_0;

	//
	// elements in the outer list must be put exactly in this order to avoid issues
	//

	List<JAXBElement<? extends ExpressionType>> outerList = new ArrayList<>();

	{
	    FunctionType functionType = new FunctionType(function.getId());

	    outerList.add(ObjectFactories.XACML().createFunction(functionType));
	}

	ApplyType outerApply = null;

	{

	    List<JAXBElement<? extends ExpressionType>> innerList = new ArrayList<>();

	    for (String value : values) {

		AttributeValueType attributeValueType = createAttribute(value, StandardDatatypes.STRING.getId());

		innerList.add(ObjectFactories.XACML().createAttributeValue(attributeValueType));
	    }

	    String nameSuffixBag = PrimitiveToBag.NAME_SUFFIX_BAG;

	    String functionId = xacmlNs10 + "string" + nameSuffixBag;

	    ApplyType innerApplyType = new ApplyType(null, innerList, functionId);

	    outerList.add(ObjectFactories.XACML().createApply(innerApplyType));
	}

	{

	    JAXBElement<AttributeDesignatorType> attributeDesignator = ObjectFactories.XACML()
		    .createAttributeDesignator(attributeDesignatorType);

	    outerList.add(attributeDesignator);

	    String functionId = xacmlNs10 + "any-of-all";

	    outerApply = new ApplyType(null, outerList, functionId);
	}

	return outerApply;
    }

    /**
     * @param matchId
     * @param attrValueContent
     * @param attrValuedataType
     * @param attrDesignatorCategory
     * @param attrDesignatorId
     * @param attrDesignatorDataType
     * @param mustBePresent
     * @return
     */
    public static AnyOf createAnyOfAllOfMatch(//
	    String matchId, //
	    String attrValueContent, //
	    String attrValuedataType, //
	    String attrDesignatorCategory, //
	    String attrDesignatorId, //
	    String attrDesignatorDataType, //
	    boolean mustBePresent) {

	return createAnyOfAllOfMatch(//
		matchId, //
		attrValueContent, //
		attrValuedataType, //
		null, //
		attrDesignatorCategory, //
		attrDesignatorId, //
		attrDesignatorDataType, //
		mustBePresent);
    }

    /**
     * @param value
     * @param dataType
     * @return
     */
    public static AttributeValueType createAttribute(String value, String dataType) {

	ArrayList<Serializable> content = new ArrayList<>();
	content.add(value);

	AttributeValueType attributeValueType = new AttributeValueType(//
		content, //
		dataType, //
		null);

	return attributeValueType;
    }

    /**
     * @param matchId
     * @param attrValueContent
     * @param attrValuedataType
     * @param attrDesignatorCategory
     * @param attrDesignatorId
     * @param attrDesignatorDataType
     * @param mustBePresent
     * @return
     */
    public static AnyOf createAnyOfAllOfMatch(//
	    String matchId, //
	    String attrValueContent, //
	    String attrValuedataType, //

	    String attrDesignatorIssuer, //
	    String attrDesignatorCategory, //
	    String attrDesignatorId, //
	    String attrDesignatorDataType, //
	    boolean mustBePresent) {

	ArrayList<AllOf> allOfList = new ArrayList<AllOf>();
	ArrayList<Match> matchList = new ArrayList<Match>();

	AttributeValueType attributeValueType = createAttribute(attrValueContent, attrValuedataType);

	AttributeDesignatorType attributeDesignatorType = new AttributeDesignatorType(//
		attrDesignatorCategory, //
		attrDesignatorId, //
		attrDesignatorDataType, //
		attrDesignatorIssuer, //
		mustBePresent);//

	Match match = new Match(//
		attributeValueType, //
		null, //
		attributeDesignatorType, //
		matchId);

	matchList.add(match);

	allOfList.add(new AllOf(matchList));

	return new AnyOf(allOfList);
    }

    /**
     * @param target
     * @param policySetId
     * @param policySetIdReference
     * @param algorithm
     * @return
     */
    public static PolicySet createPolicySet(Target target, String policySetId, String policySetIdReference, String algorithm) {

	ArrayList<Serializable> refList = new ArrayList<>();

	IdReferenceType idReferenceType = new IdReferenceType(policySetIdReference, null, null, null);

	JAXBElement<IdReferenceType> ref = ObjectFactories.XACML().createPolicySetIdReference(idReferenceType);

	refList.add(ref);

	return createPolicySet(target, policySetId, refList, algorithm);
    }

    /**
     * @param target
     * @param policySetId
     * @param refList
     * @param algorithm
     * @return
     */
    public static PolicySet createPolicySet(Target target, String policySetId, List<Serializable> refList, String algorithm) {

	return new PolicySet(//
		null, //
		null, //
		null, //
		target, //
		refList, //
		null, //
		null, //
		policySetId, //
		"1.0", //
		algorithm, //
		null);
    }

    /**
     * @param policySetId
     * @param refList
     * @param algorithm
     * @return
     */
    public static PolicySet createPolicySet(String policySetId, List<Serializable> refList, String algorithm) {

	return createPolicySet(ObjectFactories.XACML().createTarget(), policySetId, refList, algorithm);
    }

    /**
     * @param policySetId
     * @param policySetIdReference
     * @param algorithm
     * @return
     */
    public static PolicySet createPolicySet(String policySetId, String policySetIdReference, String algorithm) {

	ArrayList<Serializable> refList = new ArrayList<>();

	IdReferenceType idReferenceType = new IdReferenceType(policySetIdReference, null, null, null);

	JAXBElement<IdReferenceType> ref = ObjectFactories.XACML().createPolicySetIdReference(idReferenceType);

	refList.add(ref);

	return createPolicySet(ObjectFactories.XACML().createTarget(), policySetId, refList, algorithm);

    }
}
