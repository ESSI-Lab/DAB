/**
 * 
 */
package eu.essi_lab.authorization.authzforce.ext;

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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.ow2.authzforce.xmlns.pdp.ext.AbstractPolicyProvider;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdListPolicyProvider")
public class IdListPolicyProvider extends AbstractPolicyProvider {

    @XmlElement(name = "identifiers", required = true)
    private List<String> identifiers;

    @XmlTransient
    private PolicySet root;

    public IdListPolicyProvider(PolicySet root) {

	this.identifiers = new ArrayList<>();
	this.root = root;
    }

    /**
     * @return
     */
    @XmlTransient
    public List<String> getIdentifiers() {

	return identifiers;
    }

    /**
    * 
    */
    public PolicySet getRootPolicySet() {

	return this.root;
    }
}
