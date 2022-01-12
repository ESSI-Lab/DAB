package eu.essi_lab.pdk.wrt;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
public abstract class DiscoveryRequestTransformer extends WebRequestTransformer<DiscoveryMessage> {

    private static final String UNIDENTIFIED_SOURCE_ERROR = "NON_EXISTENT_SOURCE_ERROR";
    private static final String EXCLUDED = "EXCLUDED";
    private transient Logger logger = GSLoggerFactory.getLogger(DiscoveryRequestTransformer.class);

    /**
     * Refines the <code>message</code> by setting the {@link Bond}, the distinct queryables, {@link GSSource}s and
     * {@link ResourceSelector}
     *
     * @see DiscoveryMessage#setUserBond(Bond)
     * @see RequestMessage#setSources(List)
     * @see DiscoveryMessage#setResourceSelector(ResourceSelector)
     * @see #getUserBond(WebRequest)
     * @see #getSources(Bond)
     * @see #getSelector(WebRequest)
     */
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {

	message.setUserBond(getUserBond(message.getWebRequest()));

	Optional<Queryable> distinctElement = getDistinctElement(message.getWebRequest());
	if (distinctElement.isPresent()) {
	    message.setDistinctValuesElement(distinctElement.get());
	}

	Optional<Queryable> orderingProperty = getOrderingProperty();
	if (orderingProperty.isPresent()) {
	    message.setOrderingProperty(orderingProperty.get());
	}

	Optional<OrderingDirection> orderingDirection = getOrderingDirection();
	if (orderingDirection.isPresent()) {
	    message.setOrderingDirection(orderingDirection.get());
	}

	List<GSSource> sources = getSources(message.getUserBond().orElse(null));
	message.setSources(sources);

	ResourceSelector selector = getSelector(message.getWebRequest());
	message.setResourceSelector(selector);

	return message;
    }

    /**
     * Creates an instance of {@link DiscoveryMessage}
     */
    protected DiscoveryMessage createMessage() {

	return new DiscoveryMessage();
    }

    /**
     * Returns the {@link Bond} which represents the supplied <code>request</code> parameters
     *
     * @param request the {@link WebRequest} triggered by the {@link Profiler} supported client
     * @return a {@link Bond} (possible a {@link LogicalBond} or <code>null</code>) which represents the supplied
     *         <code>request</code>
     *         parameters
     */
    protected abstract Bond getUserBond(WebRequest request) throws GSException;

    /**
     * Returns the element to be used for executing the distinct operation (i.e. removing from all the
     * returned resources the ones having the same values for the given element, except one).
     * By default this set is empty. Sub transformers can override this method to set specific distinct elements.
     * 
     * @return the optional distinct element
     */
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.empty();
    }

    /**
     * Returns the queryable property to order results returned by the discovery operation.
     * 
     * @return the optional distinct queryable
     */
    protected Optional<Queryable> getOrderingProperty() {
	return Optional.empty();
    }

    /**
     * Returns the ordering direction to order results returned by the discovery operation.
     * 
     * @return the optional ordering direction
     */
    protected Optional<OrderingDirection> getOrderingDirection() {
	return Optional.empty();
    }

    /**
     * Returns the {@link ResourceSelector} to set to the {@link DiscoveryMessage}
     *
     * @param request the {@link WebRequest} triggered by the {@link Profiler} supported client
     * @return a non <code>null</code> instance of {@link ResourceSelector}
     */
    protected abstract ResourceSelector getSelector(WebRequest request);

    /**
     * Returns a list (possible empty ) of target {@link GSSource}s by looking for all the {@link SourceIdentifierBond}
     * of the {@link
     * DiscoveryMessage} bonds
     */
    protected List<GSSource> getSources(Bond bond) throws GSException {

	logger.trace("Getting sources STARTED");

	DiscoveryBondParser bondParser = new DiscoveryBondParser(bond);
	final List<GSSource> sources = new ArrayList<>();
	final List<GSException> excList = new ArrayList<>();
	final List<String> exclusionList = new ArrayList<>();
	bondParser.parse(new DiscoveryBondHandler() {
	    @Override
	    public void resourcePropertyBond(ResourcePropertyBond bond) {

		if (bond.getProperty() == ResourceProperty.SOURCE_ID) {
		    String value = bond.getPropertyValue();
		    if (value.contains(EXCLUDED)) {
			// it is the operand of a NOT logical bond
			exclusionList.add(value.replace(EXCLUDED, ""));
			return;
		    }

		    if (value.equals("ROOT")) {
			return;
		    }

		    GSSource source = null;
		    try {
			source = ConfigurationUtils.getSource(value);
		    } catch (GSException ex) {
			excList.add(ex);
			return;
		    }
		    if (source != null) {
			sources.add(source);
		    } else {
			// the request validation MUST check the sources ids,
			// this exception should NEVER be thrown
			GSException exception = GSException.createException(//
				getClass(), //
				"Unidentified source: " + value, null, //
				ErrorInfo.ERRORTYPE_CLIENT, //
				ErrorInfo.SEVERITY_ERROR, //
				UNIDENTIFIED_SOURCE_ERROR);//
			excList.add(exception);
		    }
		}
	    }

	    @Override
	    public void startLogicalBond(LogicalBond b) {

		LogicalOperator logicalOperator = b.getLogicalOperator();
		if (logicalOperator == LogicalOperator.NOT) {

		    Bond firstOperand = b.getFirstOperand();
		    if (isSourceIdentifierBond(firstOperand)) {
			ResourcePropertyBond rb = (ResourcePropertyBond) firstOperand;
			// this will excludes it by the requested sources list
			rb.setPropertyValue(rb.getPropertyValue() + EXCLUDED);
		    }
		}
	    }

	    private boolean isSourceIdentifierBond(Bond bond) {

		return BondFactory.isResourcePropertyBond(bond, ResourceProperty.SOURCE_ID);
	    }

	    @Override
	    public void spatialBond(SpatialBond b) {
		// nothing to do here
	    }

	    @Override
	    public void simpleValueBond(SimpleValueBond bond) {
		// nothing to do here
	    }

	    @Override
	    public void separator() {
		// nothing to do here
	    }

	    @Override
	    public void endLogicalBond(LogicalBond b) {
		// nothing to do here
	    }

	    @Override
	    public void customBond(QueryableBond<String> bond) {
		// nothing to do here
	    }

	    @Override
	    public void viewBond(ViewBond bond) {
		// nothing to do here

	    }

	    @Override
	    public void nonLogicalBond(Bond bond) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
		// TODO Auto-generated method stub
		
	    }
	});

	if (!excList.isEmpty()) {
	    throw excList.get(0);
	}

	if (sources.isEmpty()) {
	    sources.addAll(ConfigurationUtils.getAllSources());
	}

	for (String excludedSource : exclusionList) {
	    Iterator<GSSource> iterator = sources.iterator();
	    while (iterator.hasNext()) {
		GSSource source = iterator.next();
		if (source.getUniqueIdentifier().equals(excludedSource)) {
		    iterator.remove();
		}
	    }
	}

	logger.trace("Getting sources ENDED");

	return sources;

    }
}
