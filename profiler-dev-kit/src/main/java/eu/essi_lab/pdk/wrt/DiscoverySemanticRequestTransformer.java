/**
 * 
 */
package eu.essi_lab.pdk.wrt;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.DiscoverySemanticMessage;
import eu.essi_lab.messages.DiscoverySemanticMessage.ExpansionPolicy;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;

/**
 * @author Fabrizio
 */
public abstract class DiscoverySemanticRequestTransformer extends DiscoveryRequestTransformer {

    protected DiscoverySemanticMessage refineMessage(DiscoveryMessage message) throws GSException {

	super.refineMessage(message);

	DiscoverySemanticMessage dsm = (DiscoverySemanticMessage) message;

	dsm.setDiscoveryQueryables(getDiscoveryQueryables(message.getWebRequest()));
	dsm.setExpansionPolicy(getExpansionPolicy(message.getWebRequest()));
	dsm.setScheme(getScheme(message.getWebRequest()));
	dsm.setTermsToExpand(getTermsToExpand(message.getWebRequest()));
	
	return dsm;
    }

    /**
     * @return
     */
    protected abstract List<Queryable> getDiscoveryQueryables(WebRequest request);

    /**
     * @return
     */
    protected abstract ExpansionPolicy getExpansionPolicy(WebRequest request);

    /**
     * @return
     */
    protected abstract GSKnowledgeScheme getScheme(WebRequest request);

    /**
     * @return
     */
    protected abstract List<String> getTermsToExpand(WebRequest request);

    /**
     * Creates an instance of {@link DiscoverySemanticMessage}
     */
    protected DiscoverySemanticMessage createMessage() {

	return new DiscoverySemanticMessage();
    }
}
