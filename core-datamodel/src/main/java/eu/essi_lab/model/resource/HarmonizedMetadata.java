package eu.essi_lab.model.resource;

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
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.jaxb.common.NameSpace;
@XmlRootElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class HarmonizedMetadata {

    @NotNull(message = "coreMetadata field of HarmonizedMetadata cannot be null")
    @Valid
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private CoreMetadata coreMetadata;

    @XmlElementWrapper(name = "augmentedMetadata", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlElement(name = "element", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private List<AugmentedMetadataElement> elements;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ExtendedMetadata extendedMetadata;

    HarmonizedMetadata() {
	this(ResourceType.DATASET);
    }

    HarmonizedMetadata(ResourceType type) {
	this.coreMetadata = new CoreMetadata(type);
	this.elements = new ArrayList<AugmentedMetadataElement>();
	this.extendedMetadata = new ExtendedMetadata();
    }

    /**
     * Returns the list of augmented elements.
     * 
     * @return
     */
    @XmlTransient
    public List<AugmentedMetadataElement> getAugmentedMetadataElements() {
	return elements;
    }

    @XmlTransient
    public CoreMetadata getCoreMetadata() {
	return coreMetadata;
    }

    public void setCoreMetadata(CoreMetadata coreMetadata) {
	this.coreMetadata = coreMetadata;
    }

    @XmlTransient
    public ExtendedMetadata getExtendedMetadata() {
	return extendedMetadata;
    }

}
