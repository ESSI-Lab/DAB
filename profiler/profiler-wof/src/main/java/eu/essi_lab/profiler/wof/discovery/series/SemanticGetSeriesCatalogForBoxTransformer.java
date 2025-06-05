/**
 * 
 */
package eu.essi_lab.profiler.wof.discovery.series;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.DiscoverySemanticMessage.ExpansionPolicy;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.GSPredicate;
import eu.essi_lab.model.ontology.OntologyURIs;
import eu.essi_lab.model.ontology.skos.SKOSBroaderPredicate;
import eu.essi_lab.model.ontology.skos.SKOSDefinitionPredicate;
import eu.essi_lab.model.ontology.skos.SKOSLabelPredicate;
import eu.essi_lab.model.ontology.skos.SKOSNarrowerPredicate;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoverySemanticRequestTransformer;
import eu.essi_lab.profiler.wof.WOFRequest;

/**
 * @author Fabrizio
 */
public class SemanticGetSeriesCatalogForBoxTransformer extends DiscoverySemanticRequestTransformer {

    private GetSeriesCatalogForBoxTransformer internalTransformer;

    public SemanticGetSeriesCatalogForBoxTransformer() {

	internalTransformer = new GetSeriesCatalogForBoxTransformer();
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	return internalTransformer.validate(request);
    }

    @Override
    protected List<String> getTermsToExpand(WebRequest request) {
    
        WOFRequest wofRequest = new GetSeriesCatalogForBoxRequest(request);
    
        ArrayList<String> out = new ArrayList<String>();
    
        Optional<SimpleValueBond> keywordBond = wofRequest.getKeywordBond();
        if (keywordBond.isPresent()) {
            out.add(keywordBond.get().getPropertyValue());
        }
    
        return out;
    }

    @Override
    protected List<Queryable> getDiscoveryQueryables(WebRequest request) {

	return Arrays.asList(MetadataElement.ATTRIBUTE_TITLE);
    }

    @Override
    protected ExpansionPolicy getExpansionPolicy(WebRequest request) {

	return ExpansionPolicy.SEARCH_AND_EXPAND;
    }

    @Override
    protected GSKnowledgeScheme getScheme(WebRequest request) {

//	return SKOSKnowledgeScheme.getInstance();
	
	return new GSKnowledgeScheme() {

	    /**
	     * @return
	     */
	    public List<GSPredicate> getExpandPredicates() {
		return Arrays.asList(new SKOSNarrowerPredicate(), new SKOSBroaderPredicate());
	    }

	    /**
	     * @return
	     */
	    public GSPredicate getLabelPredicate() {
		return new SKOSLabelPredicate();
	    }

	    public GSPredicate getAbstractPredicate() {
		return new SKOSDefinitionPredicate();
	    }

	    /**
	     * @return
	     */
	    public String getNamespace() {
		return OntologyURIs.SKOS_NAMESPACE;
	    }
	};
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	return internalTransformer.getUserBond(request);
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	return internalTransformer.getSelector(request);
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return internalTransformer.getPage(request);
    }

    @Override
    public String getProfilerType() {

	return internalTransformer.getProfilerType();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
