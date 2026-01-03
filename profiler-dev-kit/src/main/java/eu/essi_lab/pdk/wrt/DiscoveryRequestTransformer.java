package eu.essi_lab.pdk.wrt;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;

/**
 * Validates and transforms a "discovery query" in the correspondent {@link DiscoveryMessage}. The discovery query is represented by a
 * {@link WebRequest} which encapsulates request parameters and headers.<br> This component is part of the {@link DiscoveryHandler}
 * composition and the calling of {@link #transform(WebRequest)} method is the first step of the workflow
 * <h3>Implementation notes</h3>
 * The main goal of a transformer is to provide the {@link Bond} representation of the {@link WebRequest} parameters.<br>
 * <br>
 * This goal can be achieved in many different ways, depending also by the request method supported by the {@link Profiler} service, GET
 * and/or POST. The {@link WebRequestParameter} API can be used for GET requests. Implementations can define several
 * {@link WebRequestParameter}s as
 * <code>static</code> fields, one for each parameter supported by the {@link Profiler} web service. The
 * {@link Bond} representation of all the defined {@link WebRequestParameter}s provided by the {@link WebRequestParameter#asBond(String)}
 * method, can be used to properly set the {@link DiscoveryMessage#getUserBond(Bond)} property (see {@link WebRequestParameter} java docs
 * for more info).<br>
 * <br>
 * The mapping from {@link WebRequest} parameters to {@link Bond}s depends from the semantic of the parameters supported by {@link Profiler}
 * service. Consider for example an
 * <a href="http://www.opensearch.org/Home">OpenSearch</a> web service and its <a href=
 * "http://www.opensearch.org/Specifications/OpenSearch/1.1/Draft_3#The_.22searchTerms.22_parameter">searchTerms</a> parameter. The semantic
 * of such parameter can be described in natural language: "give me all the metadata records with a textual content matching the supplied
 * search terms". In the GI-suite, the textual content of a {@link GSResource} can be queried by means of the {@link AnyTextBond}, which
 * represents a constraint applied on the entire textual content of a {@link GSResource} (for a complete list of the available {@link Bond}s
 * see {@link BondFactory}).<br>
 * <br>
 * As for all the {@link Pluggable}, in order to be loaded the transformer implementation <b>MUST</b> be registered with the
 * {@link ServiceLoader} API<br>
 * <br>
 *
 * @author Fabrizio
 * @see DiscoveryHandler
 * @see WebRequestParameter
 * @see DiscoveryHandler
 * @see Profiler#handle(WebRequest)
 */
public abstract class DiscoveryRequestTransformer extends WebRequestTransformer<DiscoveryMessage> {

    private static final String EXCLUDED = "EXCLUDED";

    private ProfilerSetting setting;

    /**
     *
     */
    public DiscoveryRequestTransformer() {
    }

    /**
     * @param setting
     */
    public DiscoveryRequestTransformer(ProfilerSetting setting) {

	setSetting(setting);
    }

    /**
     * @return
     */
    public Optional<ProfilerSetting> getSetting() {

	return Optional.ofNullable(setting);
    }

    /**
     * @param setting
     */
    public void setSetting(ProfilerSetting setting) {

	this.setting = setting;
    }

    /**
     * Refines the <code>message</code> by setting the {@link Bond}, the distinct queryables, {@link GSSource}s and
     * {@link ResourceSelector}
     *
     * @see DiscoveryMessage#setIncludeCountInRetrieval(boolean)
     * @see DiscoveryMessage#setUserBond(Bond)
     * @see RequestMessage#setSources(List)
     * @see DiscoveryMessage#setResourceSelector(ResourceSelector)
     * @see #getUserBond(WebRequest)
     * @see #getSources(Bond)
     * @see #getSelector(WebRequest)
     */
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {

	//
	// optionally set the ResourceConsumer and the number of threads for the result set mapper
	//

	getSetting().flatMap(ProfilerSetting::getConsumer).ifPresent(message::setResourceConsumer);

	getSetting().flatMap(ProfilerSetting::getResultSetMapperThreadsCount).ifPresent(message::setResultSetMapperThreadsCount);

	//
 	// optionally set the data proxy server endpoint
 	//

	ConfigurationWrapper.getSystemSettings().readKeyValue(SystemSetting.KeyValueOptionKeys.DATA_PROXY_SERVER.getLabel()).
		ifPresent(message::setDataProxyServer);

	//
	//
	//

	message.setIncludeCountInRetrieval(true);

	message.setUserBond(getUserBond(message.getWebRequest()));

	Optional<Queryable> distinctElement = getDistinctElement(message.getWebRequest());
	if (distinctElement.isPresent()) {
	    message.setDistinctValuesElement(distinctElement.get());
	}

	Optional<SortedFields> sortedFields = getSortedFields();
	if (sortedFields.isPresent()) {
	    message.setSortedFields(sortedFields.get());
	}

	Optional<View> optionalView = message.getView();
	Bond finalBond = message.getUserBond().orElse(null);
	if (optionalView.isPresent()) {
	    Optional<View> view = WebRequestTransformer.findView(message.getDataBaseURI(), optionalView.get().getId());
	    if (view.isPresent()) {
		if (finalBond == null) {
		    finalBond = view.get().getBond();
		} else {
		    finalBond = BondFactory.createAndBond(finalBond, view.get().getBond());
		}
	    }
	}

	List<GSSource> sources = getSources(finalBond, optionalView);
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
     * <code>request</code>
     * parameters
     */
    protected abstract Bond getUserBond(WebRequest request) throws GSException;

    /**
     * Returns the element to be used for executing the distinct operation (i.e. removing from all the returned resources the ones having
     * the same values for the given element, except one). By default, this set is empty. Sub transformers can override this method to set
     * specific distinct elements.
     *
     * @return the optional distinct element
     */
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.empty();
    }

    protected Optional<SortedFields> getSortedFields() {
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
     * Returns a list (possible empty ) of target {@link GSSource}s by looking for all the {@link SourceIdentifierBond} of the
     * {@link DiscoveryMessage} bonds
     */
    protected List<GSSource> getSources(Bond bond, Optional<View> optionalView) throws GSException {

	GSLoggerFactory.getLogger(DiscoveryRequestTransformer.class).trace("Getting sources STARTED");

	List<GSSource> allSources = ConfigurationWrapper.getAllSources();

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

		    switch (bond.getOperator()) {
		    case EQUAL:

			allSources.stream().filter(s -> s.getUniqueIdentifier().equals(value)).forEach(s -> sources.add(s));

			break;
		    case TEXT_SEARCH:

			allSources.stream().filter(s -> s.getUniqueIdentifier().contains(value)).forEach(s -> sources.add(s));

			break;
		    default:
			break;
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
			// this will exclude it by the requested sources list
			rb.setPropertyValue(rb.getPropertyValue() + EXCLUDED);
		    }
		}
	    }

	    private boolean isSourceIdentifierBond(Bond bond) {

		return BondFactory.isResourcePropertyBond(bond, ResourceProperty.SOURCE_ID);
	    }

	    @Override
	    public void spatialBond(SpatialBond b) {
	    }

	    @Override
	    public void simpleValueBond(SimpleValueBond bond) {
	    }

	    @Override
	    public void separator() {
	    }

	    @Override
	    public void endLogicalBond(LogicalBond b) {
	    }

	    @Override
	    public void customBond(QueryableBond<String> bond) {
	    }

	    @Override
	    public void viewBond(ViewBond bond) {
	    }

	    @Override
	    public void nonLogicalBond(Bond bond) {
	    }

	    @Override
	    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	    }
	});

	if (!excList.isEmpty()) {
	    throw excList.get(0);
	}

	if (sources.isEmpty()) {
	    if (optionalView.isPresent()) {

		sources.addAll(ConfigurationWrapper.getViewSources(optionalView.get()));

	    } else {

		sources.addAll(ConfigurationWrapper.getAllSources());
	    }
	}

	for (String excludedSource : exclusionList) {
	    Iterator<GSSource> iterator = sources.iterator();
	    while (iterator.hasNext()) {
		GSSource source = iterator.next();
		String id = source.getUniqueIdentifier();
		if (id == null || id.equals(excludedSource)) {
		    iterator.remove();
		}
	    }
	}

	GSLoggerFactory.getLogger(DiscoveryRequestTransformer.class).trace("Getting sources ENDED");

	return sources;

    }
}
