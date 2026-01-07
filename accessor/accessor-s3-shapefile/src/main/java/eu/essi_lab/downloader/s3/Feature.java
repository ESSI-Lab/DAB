package eu.essi_lab.downloader.s3;

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

import org.geotools.api.data.SimpleFeatureSource;

import eu.essi_lab.accessor.s3.FeatureMetadata;

public class Feature {
    FeatureMetadata featureMetadata = null;

    SimpleFeatureSource featureSource = null;

    public SimpleFeatureSource getFeatureSource() {
	return featureSource;
    }

    public void setFeatureSource(SimpleFeatureSource featureSource) {
	this.featureSource = featureSource;
    }

    public FeatureMetadata getFeatureMetadata() {
	return featureMetadata;
    }

    public void setFeatureMetadata(FeatureMetadata featureMetadata) {
	this.featureMetadata = featureMetadata;
    }

}
