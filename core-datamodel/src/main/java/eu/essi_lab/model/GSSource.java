package eu.essi_lab.model;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
public class GSSource implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8735607845534707851L;

    @NotNull(message = "uniqueIdentifier field of GSSource cannot be null")
    @XmlAttribute(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String uniqueIdentifier;

    @NotNull(message = "strategy field of GSSource cannot be null")
    @XmlAttribute(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private BrokeringStrategy strategy;

    @NotNull(message = "resultsPriority field of GSSource cannot be null")
    @XmlAttribute(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ResultsPriority resultsPriority = ResultsPriority.UNSET;

    @NotNull(message = "endpoint field of GSSource cannot be null")
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String endpoint;

    @NotNull(message = "label field of GSSource cannot be null")
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String label;

    @NotNull(message = "deployment field of GSSource cannot be null")
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<String> deployment;

    @XmlAttribute(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private SortOrder orderingDirection = SortOrder.ASCENDING;

    @XmlAttribute(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String orderingProperty;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String version;

    /**
     * 
     */
    public GSSource(String sourceId) {
	setResultsPriority(ResultsPriority.UNSET);
	setSortOrder(SortOrder.ASCENDING);
	if (sourceId != null) {
	    setUniqueIdentifier(sourceId);
	}
	this.deployment = new ArrayList<String>();
    }

    /**
     * 
     */
    public GSSource() {
	this(null);
    }

    @XmlTransient
    public SortOrder getSortOrder() {
	return orderingDirection;
    }

    public void setSortOrder(SortOrder order) {
	this.orderingDirection = order;
    }

    @XmlTransient
    public String getSortProperty() {
	return orderingProperty;
    }

    /**
     * @param property
     */
    public void setSortProperty(String property) {
	this.orderingProperty = property;
    }

    @XmlTransient
    public List<String> getDeployment() {
	return deployment;
    }

    /**
     * @param deployment
     */
    public void addDeployment(String deployment) {
	this.deployment.add(deployment);
    }

    @XmlTransient
    public String getUniqueIdentifier() {
	return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
	this.uniqueIdentifier = uniqueIdentifier;
    }

    @XmlTransient
    public BrokeringStrategy getBrokeringStrategy() {
	return strategy;
    }

    public void setBrokeringStrategy(BrokeringStrategy strategy) {
	this.strategy = strategy;
    }

    @XmlTransient
    public ResultsPriority getResultsPriority() {
	return resultsPriority;
    }

    public void setResultsPriority(ResultsPriority priority) {
	this.resultsPriority = priority;
    }

    @XmlTransient
    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    @XmlTransient
    public String getVersion() {
	return version;
    }

    public void setVersion(String version) {
	this.version = version;
    }

    @XmlTransient
    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    @Override

    public String toString() {
	return this.getLabel() + " [" + this.getUniqueIdentifier() + "] [" + this.getEndpoint() + "]";
    }

    @Override
    public boolean equals(Object o) {

	if (o instanceof GSSource) {

	    GSSource s = (GSSource) o;
	    return s.getBrokeringStrategy() == this.getBrokeringStrategy() && //
		    (s.getEndpoint() == null && this.getEndpoint() == null || s.getEndpoint().equals(this.getEndpoint()) && //
			    s.getSortOrder() == this.getSortOrder() && s.getSortProperty() == this.getSortProperty()
			    && //
			    s.getResultsPriority() == this.getResultsPriority() && //
			    (s.getUniqueIdentifier() == null && this.getUniqueIdentifier() == null
				    || s.getUniqueIdentifier().equals(this.getUniqueIdentifier()))
			    && //
			    (s.getVersion() == null && this.getVersion() == null || s.getVersion().equals(this.getVersion())) &&

			    (s.getDeployment() == null && this.getDeployment() == null || s.getDeployment().equals(this.getDeployment())) //

		    );//
	}

	return false;
    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

}
