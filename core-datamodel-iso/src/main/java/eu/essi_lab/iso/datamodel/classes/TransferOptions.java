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

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.RealPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIOnlineResourcePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDigitalTransferOptionsType;

/**
 * Technical means and media by which a resource is obtained from the distributor
 * 
 * @author boldrini
 */
public class TransferOptions extends ISOMetadata<MDDigitalTransferOptionsType> {

    public TransferOptions(InputStream stream) throws JAXBException {

	super(stream);
    }

    public TransferOptions(MDDigitalTransferOptionsType type) {

	super(type);
    }

    public TransferOptions() {

	super(new MDDigitalTransferOptionsType());
    }

    @Override
    public JAXBElement<MDDigitalTransferOptionsType> getElement() {

	JAXBElement<MDDigitalTransferOptionsType> element = ObjectFactories.GMD().createMDDigitalTransferOptions(type);
	return element;
    }

    /**
     * @param transferSize estimated size of a unit in the specified transfer format, expressed in megabytes.
     *        The transfer size is > 0.0
     */
    public void setTransferSize(Double transferSize) {
	RealPropertyType value = new RealPropertyType();
	value.setReal(transferSize);
	type.setTransferSize(value);
    }

    /**
     * Returns the transferSize: estimated size of a unit in the specified transfer format, expressed in megabytes.
     * The transfer size is > 0.0
     */
    public Double getTransferSize() {
	if (type.isSetTransferSize()) {
	    return type.getTransferSize().getReal();
	}
	return null;
    }

    /**
     * @param onLine information about online sources from which the resource can be obtained
     */
    public void addOnline(Online onLine) {

	CIOnlineResourcePropertyType onlineProperty = new CIOnlineResourcePropertyType();
	onlineProperty.setCIOnlineResource(onLine.getElementType());
	type.getOnLine().add(onlineProperty);

    }

    /**
     * @XPathDirective(clear = "gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine")
     */
    public void clearOnlines() {

	type.unsetOnLine();
    }

    /**
     * @return information about online sources from which the resource can be obtained
     */
    public Iterator<Online> getOnlines() {
	ArrayList<Online> ret = new ArrayList<>();
	if (type != null) {
	    List<CIOnlineResourcePropertyType> onlines = type.getOnLine();
	    for (CIOnlineResourcePropertyType online : onlines) {
		ret.add(new Online(online.getCIOnlineResource()));
	    }
	}
	return ret.iterator();

    }

}
