package eu.essi_lab.accessor.sos._1_0_0;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.geotools.api.geometry.BoundingBox;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.xml.sax.SAXException;

import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author Fabrizio
 */
public class SOSSensorML {

    private XMLDocumentReader reader;

    /**
     * @param reader
     */
    public SOSSensorML(XMLDocumentReader reader) {

	this.reader = reader;
    }

    /**
     * @param document
     * @throws IOException
     * @throws SAXException
     */
    public SOSSensorML(String document) throws SAXException, IOException {

	this.reader = new XMLDocumentReader(document);
    }

    /**
     * @param document
     * @throws IOException
     * @throws SAXException
     */
    public SOSSensorML(InputStream document) throws SAXException, IOException {

	this.reader = new XMLDocumentReader(document);
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getName() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:System/*:name")));
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getOriginatingOrganization() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:System/*:OriginatingOrganization")));
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public List<String> getAcquiferKeywords() {

	// <gml:Aquifer>
	// <gml:AquiferName>test aquifer name</gml:AquiferName>
	// <gml:AquiferMaterial>test aquifer material</gml:AquiferMaterial>
	// <gml:AquiferType>Sand and gravel</gml:AquiferType>
	// <gml:AquiferThickness>300</gml:AquiferThickness>
	// <gml:Confinement>Unconfined</gml:Confinement>
	// </gml:Aquifer>

	ArrayList<String> out = new ArrayList<String>();

	Optional.ofNullable(normalize(evaluateString(reader, "//*:Aquifer/*:AquiferName"))).filter(v -> v != null)
		.ifPresent(v -> out.add(v));
	Optional.ofNullable(normalize(evaluateString(reader, "//*:Aquifer/*:AquiferMaterial"))).filter(v -> v != null)
		.ifPresent(v -> out.add(v));
	Optional.ofNullable(normalize(evaluateString(reader, "//*:Aquifer/*:AquiferType"))).filter(v -> v != null)
		.ifPresent(v -> out.add(v));
	Optional.ofNullable(normalize(evaluateString(reader, "//*:Aquifer/*:AquiferThickness"))).filter(v -> v != null)
		.ifPresent(v -> out.add(v));
	Optional.ofNullable(normalize(evaluateString(reader, "//*:Aquifer/*:OriginatingOrganization"))).filter(v -> v != null)
		.ifPresent(v -> out.add(v));
	Optional.ofNullable(normalize(evaluateString(reader, "//*:Aquifer/*:Confinement"))).filter(v -> v != null)
		.ifPresent(v -> out.add(v));

	return out;
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getPhotoLink() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:System/*:photo")));
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getLicenseSummary() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:System/*:License/*:summary")));
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getLicenseName() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:System/*:License/*:name")));
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getLicenseDescription() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:System/*:License/*:description")));
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getRestriction() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:System/*:Restriction/*:description")));

    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<String> getUniqueIdentifier() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:identifier/*:Term/*:value")));
    }

    /**
     * @return
     */
    public Optional<String> getSystemType() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:classifier[@name='System Type']/*:Term/*:value")));
    }

    /**
     * @return
     */
    public Optional<String> getSensorType() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:classifier[@name='Sensor Type']/*:Term/*:value")));
    }

    /**
     * @return
     */
    public Optional<Double> getElevation() {

	String elevation = normalize(evaluateString(reader, "//*:elevation/*:value"));
	if (elevation != null) {

	    return Optional.ofNullable(Double.valueOf(elevation));
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> getElevationUOM() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:elevation/*:unit")));
    }

    /**
     * @return
     */
    public Optional<String> getCountry() {

	return Optional.ofNullable(normalize(evaluateString(reader, "//*:location//*:country")));
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public Optional<TemporalExtent> getTemporalExtent() {

	String begin = normalize(evaluateString(reader, "//*:interval//*:begin"));
	String end = normalize(evaluateString(reader, "//*:interval//*:end"));

	TemporalExtent temporalExtent = null;

	if (begin != null) {

	    temporalExtent = new TemporalExtent();
	    temporalExtent.setBeginPosition(begin);
	}

	if (end != null) {

	    if (temporalExtent == null) {
		temporalExtent = new TemporalExtent();
	    }

	    temporalExtent.setEndPosition(end);
	}

	return Optional.ofNullable(temporalExtent);
    }

    /**
     * @return
     * @throws XPathExpressionException
     */
    public List<SOSObservedProperty> getObservervedProperties() {

	ArrayList<SOSObservedProperty> list = new ArrayList<SOSObservedProperty>();

	try {
	    List<String> names = reader.evaluateTextContent("//*:DataRecord/*:field[not(@name='Time')]/@name");

	    for (String name : names) {

		String def = normalize(evaluateString(reader, "//*:DataRecord/*:field[@name='" + name + "']/*:Quantity/@definition"));
		String uom = normalize(evaluateString(reader, "//*:DataRecord/*:field[@name='" + name + "']/*:Quantity/*:uom/@code"));

		list.add(new SOSObservedProperty(name, def, uom));
	    }
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return list;
    }

    /**
     * @return
     */
    public Optional<BoundingBox> getLocation() {

	String lat = normalize(evaluateString(reader, "//*:coordinates/*:latitude"));
	String lon = normalize(evaluateString(reader, "//*:coordinates/*:longitude"));

	ReferencedEnvelope out = null;

	if (lat != null && lon != null) {

	    out = new ReferencedEnvelope(//
		    Double.valueOf(lon), //
		    Double.valueOf(lon), //
		    Double.valueOf(lat), //
		    Double.valueOf(lat), //
		    DefaultGeographicCRS.WGS84);
	}

	return Optional.ofNullable(out);
    }

    /**
     * @param value
     * @return
     */
    private String normalize(String value) {

	if (value != null) {

	    value = value.trim().strip();

	    if (value.isBlank() || value.isEmpty()) {

		value = null;
	    }
	}

	return value;
    }

    /**
     * @param reader
     * @param xPath
     * @return
     */
    private String evaluateString(XMLDocumentReader reader, String xPath) {

	try {
	    return reader.evaluateString(xPath);

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

}
