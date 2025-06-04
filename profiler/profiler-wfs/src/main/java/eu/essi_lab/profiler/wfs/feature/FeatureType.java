package eu.essi_lab.profiler.wfs.feature;

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
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wfs.feature.emodpace.BathymetryThematicDataset;
import eu.essi_lab.profiler.wfs.feature.emodpace.BiologyThematicDataset;
import eu.essi_lab.profiler.wfs.feature.emodpace.ChemistryThematicDataset;
import eu.essi_lab.profiler.wfs.feature.emodpace.MeteorologyThematicDataset;
import eu.essi_lab.profiler.wfs.feature.emodpace.OceanographyThematicDataset;
import eu.essi_lab.profiler.wfs.feature.emodpace.PhysicsThematicDataset;

/**
 * @author boldrini
 */
public abstract class FeatureType {

    public abstract QName getQName();

    public abstract String getTitle();

    public abstract String getAbstract();

    public abstract DiscoveryResultSetFormatter<Node> getResultSetFormatter();

    public abstract DiscoveryRequestTransformer getRequestTransformer();

    /**
     * returns the list of views that support this feature types, or null if all views are supported
     * 
     * @return
     */
    public String[] getSupportedViews() {
	return null;
    }

    public abstract String[] getKeywords();

    public abstract Bond getBond();

    public static List<FeatureType> getFeatureTypes(String view) {
	List<FeatureType> ret = new ArrayList<>();

	if (view != null) {
	    if (view.equals("emod-pace")) {
		ret.add(new BathymetryThematicDataset());
		ret.add(new ChemistryThematicDataset());
		ret.add(new MeteorologyThematicDataset());
		ret.add(new PhysicsThematicDataset());
		ret.add(new BiologyThematicDataset());
		ret.add(new OceanographyThematicDataset());
		// features.add(new DatasetFeatureType());
		return ret;
	    }
	}

	ServiceLoader<FeatureType> loader = ServiceLoader.load(FeatureType.class);
	Iterator<FeatureType> iterator = loader.iterator();
	while (iterator.hasNext()) {
	    FeatureType featureType = (FeatureType) iterator.next();
	    String[] supportedViews = featureType.getSupportedViews();
	    if (view == null || supportedViews == null || supportedViews.length == 0) {
		ret.add(featureType);
	    } else {
		for (String supportedView : supportedViews) {
		    if (view.equals(supportedView)) {
			ret.add(featureType);
			break;
		    }
		}
	    }
	}
	return ret;
    }

    public String getSchema() {

	String elements = "";

	FeatureAttribute[] attributes = getAttributes();

	for (FeatureAttribute attribute : attributes) {

	    elements += "<xsd:element maxOccurs=\"1\" minOccurs=\"0\" name=\"" + attribute.getName() + "\" nillable=\"true\" type=\"xsd:"
		    + attribute.getType() + "\"/>\n";

	}

	String ret = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:"
		+ getQName().getPrefix() + "=\"" + getQName().getNamespaceURI() + "\" elementFormDefault=\"qualified\" targetNamespace=\""
		+ getQName().getNamespaceURI() + "\">\n" + //
		"  <xsd:import namespace=\"http://www.opengis.net/gml\" schemaLocation=\"https://schemas.opengis.net/gml/3.1.1/base/gml.xsd\"/>\n"
		+ //
		"  <xsd:complexType name=\"" + getQName().getLocalPart() + "Type\">\n" + //
		"    <xsd:complexContent>\n" + //
		"      <xsd:extension base=\"gml:AbstractFeatureType\">\n" + //
		"        <xsd:sequence>\n" + //
		"          <xsd:element maxOccurs=\"1\" minOccurs=\"0\" name=\"the_geom\" nillable=\"true\" type=\"gml:MultiGeometryPropertyType\"/>\n"
		+ //
		elements + //
		"        </xsd:sequence>\n" + //
		"      </xsd:extension>\n" + //
		"    </xsd:complexContent>\n" + //
		"  </xsd:complexType>\n" + //
		"  <xsd:element name=\"" + getQName().getLocalPart() + "\" substitutionGroup=\"gml:_Feature\" type=\""
		+ getQName().getPrefix() + ":" + getQName().getLocalPart() + "Type\"/>\n" + //
		"</xsd:schema>\n" + //
		"";
	return ret;
    }

    protected abstract FeatureAttribute[] getAttributes();

}
