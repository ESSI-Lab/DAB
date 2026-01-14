package eu.essi_lab.iso.datamodel.classes;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.RealPropertyType;
import net.opengis.iso19139.gmd.v_20060504.*;

/**
 * MD_Distribution
 *
 * @author Fabrizio
 */
public class Distribution extends ISOMetadata<MDDistributionType> {

    public Distribution(MDDistributionType type) {

	super(type);
    }

    public Distribution() {

	super(new MDDistributionType());
    }

    public Distribution(InputStream stream) throws JAXBException {

	super(stream);
    }

    /**
     * @return
     */
    public List<ResponsibleParty> getDistributorParties() {

	final ArrayList<ResponsibleParty> out = new ArrayList<>();

	try {
	    type.getDistributor().forEach(prop -> {

		final CIResponsiblePartyType ciResponsibleParty = prop.getMDDistributor().getDistributorContact().getCIResponsibleParty();

		out.add(new ResponsibleParty(ciResponsibleParty));
	    });

	} catch (Exception e) {
	}

	return out;
    }

    // --------------------------------------------------------
    //
    // Format
    //

    /**
     * @return
     * @XPathDirective(target = "gmd:distributionFormat/*")
     */
    public Iterator<Format> getFormats() {

	ArrayList<Format> out = new ArrayList<Format>();

	List<MDFormatPropertyType> formats = type.getDistributionFormat();
	for (MDFormatPropertyType f : formats) {

	    MDFormatType mdFormat = f.getMDFormat();
	    Format format = new Format(mdFormat);
	    out.add(format);
	}

	return out.iterator();
    }

    public Format getFormat() {

	Iterator<Format> formatList = getFormats();
	if (formatList.hasNext()) {
	    return formatList.next();
	}

	return null;
    }

    /**
     * @param format
     * @XPathDirective(target = ".", parent = "gmd:distributionFormat", before = "gmd:transferOptions", position = Position.LAST)
     */
    public void addFormat(Format format) {

	MDFormatPropertyType propertyType = new MDFormatPropertyType();
	propertyType.setMDFormat(format.getElementType());

	type.getDistributionFormat().add(propertyType);
    }

    /**
     * @XPathDirective(clear = "gmd:distributionFormat")
     */
    public void clearFormats() {

	type.getDistributionFormat().clear();
    }

    // --------------------------------------------------------
    //
    // Online
    //

    /**
     * @return
     * @XPathDirective(target = ".//gmd:MD_DigitalTransferOptions/gmd:onLine/*")
     */
    public Iterator<Online> getDistributionOnlines() {

	ArrayList<Online> out = new ArrayList<Online>();

	// transferOptions
	List<MDDigitalTransferOptionsPropertyType> transferOptions = type.getTransferOptions();
	for (MDDigitalTransferOptionsPropertyType t : transferOptions) {

	    // MD_DigitalTransferOptions
	    MDDigitalTransferOptionsType mdDigitalTransferOptions = t.getMDDigitalTransferOptions();
	    if (mdDigitalTransferOptions != null) {
		List<CIOnlineResourcePropertyType> onLine = mdDigitalTransferOptions.getOnLine();

		// onLine
		for (CIOnlineResourcePropertyType ciOnlineResourcePropertyType : onLine) {

		    // onLine
		    CIOnlineResourceType ciOnlineResource = ciOnlineResourcePropertyType.getCIOnlineResource();
		    Online online = new Online(ciOnlineResource);
		    out.add(online);
		}
	    }
	}

	return out.iterator();
    }

    public Online getDistributionOnline() {
	Iterator<Online> iterator = getDistributionOnlines();
	if (iterator.hasNext()) {
	    return iterator.next();
	}
	return null;
    }

    public Online getDistributionOnline(String identifier) {
	List<Online> onlines = Lists.newArrayList(getDistributionOnlines());
	for (Online online : onlines) {
	    if (online.getIdentifier() != null && online.getIdentifier().equals(identifier)) {
		return online;
	    }
	}
	return null;
    }

    /**
     * @param onLine
     * @XPathDirective(target = "gmd:transferOptions/gmd:MD_DigitalTransferOptions", parent = "gmd:onLine", position = Position.LAST)
     */
    public void addDistributionOnline(Online onLine) {

	addDistributionOnline(onLine, null);

    }

    /**
     * @param onLine
     * @param transferSize
     * @XPathDirective(target = "gmd:transferOptions/gmd:MD_DigitalTransferOptions", parent = "gmd:onLine", position = Position.LAST)
     */
    public void addDistributionOnline(Online onLine, Double transferSize) {

	CIOnlineResourceType elementType = onLine.getElementType();
	CIOnlineResourcePropertyType ciOnlineResourcePropertyType = new CIOnlineResourcePropertyType();
	ciOnlineResourcePropertyType.setCIOnlineResource(elementType);

	List<MDDigitalTransferOptionsPropertyType> transferOptions = type.getTransferOptions();
	addOnline(transferOptions, ciOnlineResourcePropertyType, transferSize);

    }

    /**
     * @XPathDirective(clear = "gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine")
     */
    public void clearDistributionOnlines() {

	List<MDDigitalTransferOptionsPropertyType> transferOptions = type.getTransferOptions();
	transferOptions.clear();
    }

    // --------------------------------------------------------
    //
    // Distributor onlines
    //

    /**
     * @return
     * @XPathDirective(target = ".//gmd:distributorTransferOptions//gmd:MD_DigitalTransferOptions/gmd:onLine/*")
     */
    public Iterator<Online> getDistributorOnlines() {

	ArrayList<Online> out = new ArrayList<Online>();
	List<MDDistributorPropertyType> distributor = type.getDistributor();
	for (MDDistributorPropertyType mdDistributorPropertyType : distributor) {

	    MDDistributorType mdDistributor = mdDistributorPropertyType.getMDDistributor();
	    if (mdDistributor != null) {
		List<MDDigitalTransferOptionsPropertyType> distributorTransferOptions = mdDistributor.getDistributorTransferOptions();
		for (MDDigitalTransferOptionsPropertyType t : distributorTransferOptions) {
		    MDDigitalTransferOptionsType mdDigitalTransferOptions = t.getMDDigitalTransferOptions();
		    if (mdDigitalTransferOptions != null) {
			List<CIOnlineResourcePropertyType> onLine = mdDigitalTransferOptions.getOnLine();

			for (CIOnlineResourcePropertyType ciOnlineResourcePropertyType : onLine) {

			    CIOnlineResourceType ciOnlineResource = ciOnlineResourcePropertyType.getCIOnlineResource();
			    Online online = new Online(ciOnlineResource);
			    out.add(online);
			}
		    }
		}
	    }
	}

	return out.iterator();
    }

    public void addDistributorOnline(Online onLine) {
	addDistributorOnline(onLine, null);
    }

    public void addDistributorOnline(Online onLine, Double transferSize) {

	CIOnlineResourceType elementType = onLine.getElementType();
	CIOnlineResourcePropertyType ciOnlineResourcePropertyType = new CIOnlineResourcePropertyType();
	ciOnlineResourcePropertyType.setCIOnlineResource(elementType);

	List<MDDistributorPropertyType> distributor = type.getDistributor();
	MDDistributorType mdDistributor = null;
	if (!distributor.isEmpty()) {
	    mdDistributor = distributor.get(0).getMDDistributor();
	} else {
	    mdDistributor = new MDDistributorType();
	    MDDistributorPropertyType mdDistributorPropertyType = new MDDistributorPropertyType();
	    mdDistributorPropertyType.setMDDistributor(mdDistributor);
	    distributor.add(mdDistributorPropertyType);
	}

	List<MDDigitalTransferOptionsPropertyType> transferOptions = mdDistributor.getDistributorTransferOptions();

	addOnline(transferOptions, ciOnlineResourcePropertyType, transferSize);

    }

    public void clearDistributorOnlines() {

	List<MDDistributorPropertyType> distributor = type.getDistributor();
	distributor.clear();
    }

    /**
     * Adds a distributor contact (responsible party)
     * @param contact
     */
    public void addDistributorContact(ResponsibleParty contact) {
	List<MDDistributorPropertyType> distributor = type.getDistributor();
	MDDistributorType mdDistributor = null;
	if (!distributor.isEmpty()) {
	    mdDistributor = distributor.get(0).getMDDistributor();
	} else {
	    mdDistributor = new MDDistributorType();
	    MDDistributorPropertyType mdDistributorPropertyType = new MDDistributorPropertyType();
	    mdDistributorPropertyType.setMDDistributor(mdDistributor);
	    distributor.add(mdDistributorPropertyType);
	}

	CIResponsiblePartyPropertyType contactProperty = new CIResponsiblePartyPropertyType();
	contactProperty.setCIResponsibleParty(contact.getElementType());
	mdDistributor.setDistributorContact(contactProperty);
    }

    private void addOnline(List<MDDigitalTransferOptionsPropertyType> transferOptions,
	    CIOnlineResourcePropertyType ciOnlineResourcePropertyType, Double transferSize) {

	MDDigitalTransferOptionsPropertyType ditalTransferOptionsPropertyType = new MDDigitalTransferOptionsPropertyType();
	MDDigitalTransferOptionsType optionsType = new MDDigitalTransferOptionsType();
	if (transferSize != null) {
	    RealPropertyType realPropertyType = new RealPropertyType();
	    realPropertyType.setReal(transferSize);
	    optionsType.setTransferSize(realPropertyType);
	}
	optionsType.getOnLine().add(ciOnlineResourcePropertyType);
	ditalTransferOptionsPropertyType.setMDDigitalTransferOptions(optionsType);
	transferOptions.add(ditalTransferOptionsPropertyType);
    }

    @Override
    public JAXBElement<MDDistributionType> getElement() {

	JAXBElement<MDDistributionType> element = ObjectFactories.GMD().createMDDistribution(type);
	return element;
    }

    /**
     * Gets an iterator for the distribution transfer options
     *
     * @return
     */
    public Iterator<TransferOptions> getDistributionTransferOptions() {

	ArrayList<TransferOptions> out = new ArrayList<TransferOptions>();
	List<MDDigitalTransferOptionsPropertyType> options = type.getTransferOptions();
	for (MDDigitalTransferOptionsPropertyType option : options) {
	    out.add(new TransferOptions(option.getMDDigitalTransferOptions()));
	}

	return out.iterator();
    }

    /**
     * Gets an iterator for the transfer options from all the distributors
     *
     * @return
     */
    public Iterator<TransferOptions> getDistributorTransferOptions() {

	ArrayList<TransferOptions> out = new ArrayList<TransferOptions>();
	List<MDDistributorPropertyType> distributors = type.getDistributor();
	for (MDDistributorPropertyType distributor : distributors) {
	    List<MDDigitalTransferOptionsPropertyType> options = distributor.getMDDistributor().getDistributorTransferOptions();
	    for (MDDigitalTransferOptionsPropertyType option : options) {
		out.add(new TransferOptions(option.getMDDigitalTransferOptions()));
	    }
	}

	return out.iterator();
    }

}
