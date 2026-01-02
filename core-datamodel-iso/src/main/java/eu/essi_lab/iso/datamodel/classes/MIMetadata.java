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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.todo.MIObjective;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIAcquisitionInformationPropertyType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIAcquisitionInformationType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIImageDescriptionType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIInstrumentPropertyType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIInstrumentType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIPlatformPropertyType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIPlatformType;
import net.opengis.iso19139.gco.v_20060504.RealPropertyType;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDContentInformationType;
import net.opengis.iso19139.gmd.v_20060504.MDContentInformationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDImageDescriptionType;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;

public class MIMetadata extends MDMetadata {

    @XmlElement(name = "MI_Metadata", namespace = CommonNameSpaceContext.GMI_NS_URI)
    protected MIMetadataType type;

    public MIMetadata() {

	this(new MIMetadataType());
    }

    public MIMetadata(MIMetadataType type) {

	this.type = type;
    }

    public MIMetadata(MDMetadataType superType) {

	type = new MIMetadataType();

	superType.copyTo(type);
    }

    public MIMetadata(InputStream stream) throws JAXBException {

	this.type = (MIMetadataType) fromStream(stream);
    }

    public MIMetadata(String string) throws UnsupportedEncodingException, JAXBException {

	this(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    public MIMetadata(Node node) throws JAXBException {

	this.type = (MIMetadataType) fromNode(node);
    }

    public void addMIInstrument(MIInstrument instrument) {

	List<MIAcquisitionInformationPropertyType> information = type.getAcquisitionInformation();
	if (information.isEmpty()) {
	    MIAcquisitionInformationPropertyType propertyType = new MIAcquisitionInformationPropertyType();
	    information.add(propertyType);
	}

	MIAcquisitionInformationPropertyType miAcquisitionInformationPropertyType = information.get(0);
	MIAcquisitionInformationType miAcquisitionInformation = miAcquisitionInformationPropertyType.getMIAcquisitionInformation();

	if (miAcquisitionInformation == null) {

	    miAcquisitionInformation = new MIAcquisitionInformationType();
	    miAcquisitionInformationPropertyType.setMIAcquisitionInformation(miAcquisitionInformation);
	}

	MIInstrumentPropertyType miInstrumentPropertyType = new MIInstrumentPropertyType();
	miInstrumentPropertyType.setMIInstrument(instrument.getElementType());

	miAcquisitionInformation.getInstrument().add(miInstrumentPropertyType);
    }

    /**
     * @XPathDirective( target =
     * "gmi:acquisitionInformation/gmi:MI_AcquisitionInformation", parent =
     * "gmi:platform", position = Position.FIRST)
     */
    public void addMIPlatform(MIPlatform platform) {

	List<MIAcquisitionInformationPropertyType> information = type.getAcquisitionInformation();
	if (information.isEmpty()) {
	    MIAcquisitionInformationPropertyType propertyType = new MIAcquisitionInformationPropertyType();
	    information.add(propertyType);
	}

	MIAcquisitionInformationPropertyType miAcquisitionInformationPropertyType = information.get(0);
	MIAcquisitionInformationType miAcquisitionInformation = miAcquisitionInformationPropertyType.getMIAcquisitionInformation();

	if (miAcquisitionInformation == null) {

	    miAcquisitionInformation = new MIAcquisitionInformationType();
	    miAcquisitionInformationPropertyType.setMIAcquisitionInformation(miAcquisitionInformation);
	}

	MIPlatformPropertyType miPlatformPropertyType = new MIPlatformPropertyType();
	miPlatformPropertyType.setMIPlatform(platform.getElementType());

	miAcquisitionInformation.getPlatform().add(miPlatformPropertyType);
    }

    public MIPlatform getMIPlatform() {

	Iterator<MIPlatform> miPlatforms = getMIPlatforms();
	if (miPlatforms.hasNext()) {
	    return miPlatforms.next();
	}

	return null;
    }

    /**
     * @XPathDirective( target =
     * "gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform")
     */
    public Iterator<MIPlatform> getMIPlatforms() {

	ArrayList<MIPlatform> arrayList = new ArrayList<>();
	List<MIAcquisitionInformationPropertyType> information = type.getAcquisitionInformation();
	try {
	    for (MIAcquisitionInformationPropertyType miAcquisitionInformationPropertyType : information) {
		MIAcquisitionInformationType miAcquisitionInformation = miAcquisitionInformationPropertyType.getMIAcquisitionInformation();
		List<MIPlatformPropertyType> platform = miAcquisitionInformation.getPlatform();
		for (MIPlatformPropertyType miPlatformPropertyType : platform) {
		    MIPlatformType miPlatformType = miPlatformPropertyType.getMIPlatform();
		    if (miPlatformPropertyType != null) {
			MIPlatform miPlatform = new MIPlatform(miPlatformType);
			arrayList.add(miPlatform);
		    }
		}
	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}

	return arrayList.iterator();
    }

    /**
     * @XPathDirective( target =
     * "gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument")
     */
    public Iterator<MIInstrument> getMIInstruments() {

	ArrayList<MIInstrument> arrayList = new ArrayList<>();
	List<MIAcquisitionInformationPropertyType> information = type.getAcquisitionInformation();
	try {
	    for (MIAcquisitionInformationPropertyType miAcquisitionInformationPropertyType : information) {

		MIAcquisitionInformationType miAcquisitionInformation = miAcquisitionInformationPropertyType.getMIAcquisitionInformation();

		List<MIInstrumentPropertyType> instrument = miAcquisitionInformation.getInstrument();
		for (MIInstrumentPropertyType miInstrumentPropertyType : instrument) {
		    MIInstrumentType miInstrumentType = miInstrumentPropertyType.getMIInstrument();
		    MIInstrument miInstrument = new MIInstrument(miInstrumentType);
		    arrayList.add(miInstrument);
		}

	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}

	return arrayList.iterator();
    }

    /**
     * @param percentage
     */
    public void addCloudCoverPercentage(double percentage) {

	List<MDContentInformationPropertyType> contentInfoList = getElementType().getContentInfo();

	MDContentInformationPropertyType propertyType = new MDContentInformationPropertyType();
	contentInfoList.add(propertyType);

	MIImageDescriptionType miImageDescriptionType = new MIImageDescriptionType();

	JAXBElement<? extends AbstractMDContentInformationType> information = ObjectFactories.GMI()
		.createMIImageDescription(miImageDescriptionType);
	propertyType.setAbstractMDContentInformation(information);

	RealPropertyType realPropertyType = new RealPropertyType();
	realPropertyType.setReal(percentage);

	miImageDescriptionType.setCloudCoverPercentage(realPropertyType);
    }

    /**
     * @return
     */
    public List<Double> getCloudCoverPercentageList() {

	ArrayList<Double> out = Lists.newArrayList();

	List<MDContentInformationPropertyType> contentInfoList = getElementType().getContentInfo();

	for (MDContentInformationPropertyType propertyType : contentInfoList) {

	    JAXBElement<? extends AbstractMDContentInformationType> information = propertyType.getAbstractMDContentInformation();

	    if (information != null) {
		AbstractMDContentInformationType value = information.getValue();

		if (value != null && value instanceof MDImageDescriptionType) {

		    MDImageDescriptionType descType = (MDImageDescriptionType) value;
		    RealPropertyType percentage = descType.getCloudCoverPercentage();

		    if (percentage != null) {
			out.add(percentage.getReal());
		    }
		}
	    }
	}

	return out;
    }

    /**
     * @XPathDirective( target =
     * "gmi:acquisitionInformation/gmi:MI_AcquisitionInformation", parent =
     * "gmi:objective", after = "gmi:instrument")
     */
    void addMI_Objective(MIObjective obj) {
    }

    /**
     * @XPathDirective( target =
     * "gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:objective/gmi:MI_Objective")
     */
    Iterator<MIObjective> getMIObjectives() {
	return null;
    }

    @Override
    public MIMetadataType getElementType() {

	return type;
    }

    @Override
    public JAXBElement<? extends MDMetadataType> getElement() throws UnsupportedOperationException {

	JAXBElement<MIMetadataType> element = ObjectFactories.GMI().createMIMetadata(type);
	return element;
    }

}
