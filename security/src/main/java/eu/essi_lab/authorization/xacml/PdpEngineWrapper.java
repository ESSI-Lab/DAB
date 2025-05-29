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

import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_ACTION;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_RESOURCE;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.CloseablePdpEngine;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.value.AnyUriValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.MediumInteger;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import eu.essi_lab.authorization.PolicySetWrapper.Issuer;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class PdpEngineWrapper {

    private CloseablePdpEngine pdp;
    private DecisionRequestBuilder<?> requestBuilder;

    /**
     * @param builder
     * @throws Exception
     */
    public PdpEngineWrapper(CloseablePdpEngine pdp) throws Exception {

	this.pdp = pdp;

	this.requestBuilder = pdp.newRequestBuilder(-1, -1);
    }

    /**
     * 
     */
    public void setUserRole(String role) {

	AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());

	AttributeBag<?> roleAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.ANYURI, //
		new AnyUriValue("roles;" + role));

	requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, roleAttributeValues);
    }

    /**
     * @param action
     */
    public void setAction(String action) {

	AttributeFqn actionIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_ACTION.value(), //
		Optional.empty(), //
		XacmlAttributeId.XACML_1_0_ACTION_ID.value());

	AttributeBag<?> actionIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue(action));

	requestBuilder.putNamedAttributeIfAbsent(actionIdAttributeId, actionIdAttributeValues);
    }

    /**
     * 
     */
    public void setSources(String... sources) {

	AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_RESOURCE.value(), //
		Optional.of(Issuer.SOURCE.getId()), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());

	List<StringValue> collect = Arrays.asList(sources).//
		stream().//
		map(StringValue::new).//
		collect(Collectors.toList());

	AttributeBag<StringValue> sourceAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		collect);

	requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, sourceAttributeValues);
    }

    /**
     * @param count
     */
    public void setDownloadCount(int count) {

	AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_RESOURCE.value(), //
		Optional.of(Issuer.DOWNLOAD.getId()), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());

	AttributeBag<?> resourceIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.INTEGER, //
		new IntegerValue(new MediumInteger(count)));

	requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);
    }

    /**
     * @param offset
     */
    public void setOffset(int offset) {

	AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_RESOURCE.value(), //
		Optional.of(Issuer.OFFSET.getId()), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());

	AttributeBag<?> resourceIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.INTEGER, //
		new IntegerValue(new MediumInteger(offset)));

	requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);
    }

    /**
     * @param maxRecords
     */
    public void setMaxRecords(int maxRecords) {

	AttributeFqn resourceIdAttributeId = AttributeFqns.newInstance(//
		XACML_3_0_RESOURCE.value(), //
		Optional.of(Issuer.MAX_RECORDS.getId()), //
		XacmlAttributeId.XACML_1_0_RESOURCE_ID.value());

	AttributeBag<?> resourceIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.INTEGER, //
		new IntegerValue(new MediumInteger(maxRecords)));

	requestBuilder.putNamedAttributeIfAbsent(resourceIdAttributeId, resourceIdAttributeValues);
    }

    /**
     * @param path
     */
    public void setPath(String path) {

	setAccessSubject(Issuer.PATH.getId(), path);
    }

    /**
     * @param ip
     */
    public void setClientId(String clientIdentifier) {

	setAccessSubject(Issuer.CLIENT_IDENTIFIER.getId(), clientIdentifier);
    }

    /**
     * @param ipList
     */
    public void setIPs(String... ipList) {

	AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.of(Issuer.ALLOWED_IP.getId()), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	List<StringValue> collect = Arrays.asList(ipList).//
		stream().//
		map(StringValue::new).//
		collect(Collectors.toList());

	AttributeBag<StringValue> ipAttributeValues = Bags.newAttributeBag(//
		StandardDatatypes.STRING, //
		collect);

	requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, ipAttributeValues);

	// setAccessSubject(Issuer.ALLOWED_IP.getId(), ip);
    }

    /**
     * @param view
     */
    public void setViewIdentifier(String view) {

	setAccessSubject(Issuer.VIEW_ID.getId(), view);
    }

    /**
     * @param viewVisibility
     */
    public void setViewVisibility(ViewVisibility viewVisibility) {

	setAccessSubject(Issuer.VIEW_VISIBILITY.getId(), viewVisibility.name());
    }

    /**
     * @param viewOwner
     */
    public void setViewOwner(String viewOwner) {

	setAccessSubject(Issuer.VIEW_OWNER.getId(), viewOwner);
    }

    /**
     * @param viewCreator
     */
    public void setViewCreator(String viewCreator) {

	setAccessSubject(Issuer.VIEW_CREATOR.getId(), viewCreator);
    }

    /**
     * @param origin
     */
    public void setOriginHeader(String origin) {

	setAccessSubject(Issuer.ORIGIN.getId(), origin);
    }

    /**
     * @param issuer
     * @param value
     */
    private void setAccessSubject(String issuer, String value) {

	AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(//
		XACML_1_0_ACCESS_SUBJECT.value(), //
		Optional.of(issuer), //
		XacmlAttributeId.XACML_1_0_SUBJECT_ID.value());

	AttributeBag<StringValue> subjectIdAttributeValues = Bags.singletonAttributeBag(//
		StandardDatatypes.STRING, //
		new StringValue(value));

	requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);
    }

    // private void add(String... users) {
    //
    // AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(//
    // XACML_1_0_ACCESS_SUBJECT.value(), //
    // Optional.empty(), //
    // XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
    //
    // List<AnyUriValue> collect = Arrays.asList(users).//
    // stream().//
    // map(AnyUriValue::new).//
    // collect(Collectors.toList());
    //
    // AttributeBag<AnyUriValue> roleAttributeValues = Bags.newAttributeBag(//
    // StandardDatatypes.ANYURI, //
    // collect);
    //
    // requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, roleAttributeValues);
    // }

    /**
     * @param expectedType
     */
    public DecisionType evaluate() {

	DecisionRequest request = requestBuilder.build(false);

	// System.out.println("---------------------------------------\n");
	// System.out.println(request);
	// System.out.println("---------------------------------------\n");

	DecisionResult result = pdp.evaluate(request);

	return result.getDecision();
    }

    /**
     * @return
     */
    public CloseablePdpEngine getPdpEngine() {
	return pdp;
    }

    /**
     * @return
     */
    public DecisionRequestBuilder<?> getRequestBuilder() {
	return requestBuilder;
    }

    /**
     * 
     */
    public void reset() {

	requestBuilder.reset();
    }

    public void close() throws IOException {

	pdp.close();
    }
}
