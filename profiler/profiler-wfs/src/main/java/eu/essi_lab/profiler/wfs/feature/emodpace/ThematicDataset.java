package eu.essi_lab.profiler.wfs.feature.emodpace;

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

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wfs.feature.FeatureAttribute;
import eu.essi_lab.profiler.wfs.feature.FeatureType;

public abstract class ThematicDataset extends FeatureType {

    @Override
    public QName getQName() {
	return new QName("http://essi-lab.eu", normalize(getTheme()), "essi");
    }

    private String normalize(String name) {
	if (name.contains(" ")) {
	    return name.replace(" ", "-");
	}
	return name;
    }

    protected abstract String getTheme();

    @Override
    public String getTitle() {
	return getTheme() ;
    }

    @Override
    public String getAbstract() {
	return "Geo locations of datasets belonging to the theme: " + getTheme();
    }

    @Override
    public String[] getKeywords() {
	return new String[] { "dataset", "area", "point", getTheme() };
    }

    @Override
    public Bond getBond() {

	return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.THEME_CATEGORY, getTheme());
    }

    @Override
    public DiscoveryResultSetFormatter<Node> getResultSetFormatter() {
	return new ThematicDatasetResultSetFormatter(this);
    }

    @Override
    public DiscoveryRequestTransformer getRequestTransformer() {
	return new ThematicDatasetRequestTransformer(this);
    }

    @Override
    protected FeatureAttribute[] getAttributes() {
	return new FeatureAttribute[] { //
		new FeatureAttribute("title", "string"), //
		new FeatureAttribute("abstract", "string"), //
		new FeatureAttribute("theme", "string"), //
		new FeatureAttribute("organization", "string"), //
		new FeatureAttribute("platform", "string"), //
		new FeatureAttribute("parameter", "string"), //
		new FeatureAttribute("downloadURL", "string"), new FeatureAttribute("metadataURL", "string")//
	};
    }

}
