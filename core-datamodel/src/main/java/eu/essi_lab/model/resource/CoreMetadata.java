package eu.essi_lab.model.resource;

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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import eu.essi_lab.lib.xml.NameSpace;

/**
 * Core set of harmonized metadata, based on the "ISO-19115 Core metadata for geographic datasets" and on "Dublin Core"
 *
 * @author Fabrizio
 */
public class CoreMetadata {

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private MIMetadata isoMetadata;

    CoreMetadata() {

	this(ResourceType.DATASET);
	// isoMetadata = new MIMetadata();
	//
	// DataIdentification identification = new DataIdentification();
	// isoMetadata.addDataIdentification(identification);
	//
	// Distribution distribution = new Distribution();
	// isoMetadata.setDistribution(distribution);
    }

    public CoreMetadata(ResourceType resourceType) {

	isoMetadata = new MIMetadata();
	if (resourceType.equals(ResourceType.SERVICE)) {
	    ServiceIdentification serviceId = new ServiceIdentification();
	    DataIdentification dataid = isoMetadata.convertServiceIdentificationToDataIdentification(serviceId);
	    // isoMetadata.addDataIdentification(dataid);
	    isoMetadata.addServiceIdentification(serviceId);
	} else {
	    DataIdentification identification = new DataIdentification();
	    isoMetadata.addDataIdentification(identification);
	}
	Distribution distribution = new Distribution();
	isoMetadata.setDistribution(distribution);
    }

    @XmlTransient
    public String getIdentifier() {

	return isoMetadata.getFileIdentifier();
    }

    public void setIdentifier(String id) {

	isoMetadata.setFileIdentifier(id);
    }

    @XmlTransient
    public String getTitle() {

	return isoMetadata.getDataIdentification().getCitationTitle();

    }

    public void setTitle(String title) {

	getDataIdentification().setCitationTitle(title);
    }

    @XmlTransient
    public String getAbstract() {

	return isoMetadata.getDataIdentification().getAbstract();
    }

    public void setAbstract(String abstract_) {

	getDataIdentification().setAbstract(abstract_);
    }

    public void addDistributionOnlineResource(String name, String linkage, String protocol, String functionCode) {

	Online online = new Online();
	online.setName(name);
	online.setLinkage(linkage);
	online.setProtocol(protocol);
	online.setFunctionCode(functionCode);

	getDistribution().addDistributionOnline(online);
    }

    public Online getOnline() {

	Distribution distribution = isoMetadata.getDistribution();
	if (distribution != null) {
	    Iterator<Online> iterator = distribution.getDistributionOnlines();
	    if (iterator.hasNext()) {
		return iterator.next();
	    }
	}
	return null;
    }

    public void addDistributionFormat(String format) {

	Format format_ = new Format();
	format_.setName(format);

	getDistribution().addFormat(format_);
    }

    public Format getFormat() {

	Distribution distribution = isoMetadata.getDistribution();
	if (distribution == null) {
	    return null;
	}

	return isoMetadata.getDistribution().getFormat();
    }

    /**
     * use big decimals method
     *
     * @param north
     * @param west
     * @param south
     * @param east
     */
    @Deprecated
    public void addBoundingBox(double north, double west, double south, double east) {
	addBoundingBox(new BigDecimal(north), new BigDecimal(west), new BigDecimal(south), new BigDecimal(east));
    }

    /**
     * @param north
     * @param west
     * @param south
     * @param east
     */
    public void addBoundingBox(BigDecimal north, BigDecimal west, BigDecimal south, BigDecimal east) {

	getDataIdentification().addGeographicBoundingBox(north, west, south, east);
    }

    public GeographicBoundingBox getBoundingBox() {

	DataIdentification dataIdentification = isoMetadata.getDataIdentification();
	if (dataIdentification == null) {
	    return null;
	}

	return dataIdentification.getGeographicBoundingBox();
    }

    public void addTemporalExtent(String beginPosition, String endPosition) {

	getDataIdentification().addTemporalExtent(beginPosition, endPosition);
    }

    public TemporalExtent getTemporalExtent() {

	DataIdentification dataIdentification = isoMetadata.getDataIdentification();
	if (dataIdentification == null) {
	    return null;
	}

	return isoMetadata.getDataIdentification().getTemporalExtent();
    }

    public void setMIMetadata(MIMetadata metadata) {

	this.isoMetadata = metadata;
    }

    @XmlTransient
    public MIMetadata getMIMetadata() {

	return isoMetadata;
    }

    private Distribution getDistribution() {

	Distribution distribution = isoMetadata.getDistribution();
	if (distribution == null) {
	    distribution = new Distribution();
	    isoMetadata.setDistribution(distribution);
	}
	return distribution;
    }

    /**
     * @return
     */
    public DataIdentification getDataIdentification() {

	DataIdentification dataIdentification = isoMetadata.getDataIdentification();

	if (dataIdentification == null) {

	    dataIdentification = new DataIdentification();
	    isoMetadata.addDataIdentification(dataIdentification);
	}

	return dataIdentification;
    }

    /**
     * Removes the acquisition information from a copy of the {@link #getReadOnlyMDMetadata()} and returns it as {@link MDMetadata} in the
     * GMD namespace
     *
     * @return a copy of {@link #getReadOnlyMDMetadata()} without the acquisition information and in the GMD namespace
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @XmlTransient
    public MDMetadata getReadOnlyMDMetadata() throws JAXBException, UnsupportedEncodingException {

	InputStream stream = isoMetadata.asStream();
	MIMetadataType elementType = new MIMetadata(stream).getElementType();
	elementType.unsetAcquisitionInformation();

	return new MDMetadata(elementType);
    }

}
