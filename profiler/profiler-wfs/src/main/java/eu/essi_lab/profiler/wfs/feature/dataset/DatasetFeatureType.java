package eu.essi_lab.profiler.wfs.feature.dataset;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wfs.feature.FeatureAttribute;
import eu.essi_lab.profiler.wfs.feature.FeatureType;
import eu.essi_lab.profiler.wfs.feature.station.GetFeatureRequestTransformer;
import eu.essi_lab.profiler.wfs.feature.station.GetFeatureResultSetFormatter;

public class DatasetFeatureType extends FeatureType {

    @Override
    public QName getQName() {
	return new QName("http://essi-lab.eu", "dataset", "essi");
    }

    @Override
    public String getTitle() {
	return "Datasets";
    }

    @Override
    public String getAbstract() {
	return "Geo locations of datasets";
    }

    @Override
    public String[] getKeywords() {
	return new String[] { "dataset", "bbox", "point" };
    }

    @Override
    public Bond getBond() {

	return null;
    }

    @Override
    public DiscoveryResultSetFormatter<Node> getResultSetFormatter() {
	return new GetFeatureResultSetFormatter();
    }

    @Override
    public DiscoveryRequestTransformer getRequestTransformer() {
	return new GetFeatureRequestTransformer();
    }
    
    @Override
    protected FeatureAttribute[] getAttributes() {
	return new FeatureAttribute[] { //
		new FeatureAttribute("name", "string"), //
		new FeatureAttribute("id", "string"), //
	};
    }

}
