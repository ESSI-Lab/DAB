package eu.essi_lab.downloader.opensearch;

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

import org.locationtech.jts.geom.Geometry;

import eu.essi_lab.accessor.s3.FeatureMetadata;

/**
 * Cached OpenSearch shape feature (geometry + harvest metadata).
 */
public class OpenSearchFeature {

    private FeatureMetadata featureMetadata;
    private Geometry geometry;

    public FeatureMetadata getFeatureMetadata() {
	return featureMetadata;
    }

    public void setFeatureMetadata(FeatureMetadata featureMetadata) {
	this.featureMetadata = featureMetadata;
    }

    public Geometry getGeometry() {
	return geometry;
    }

    public void setGeometry(Geometry geometry) {
	this.geometry = geometry;
    }
}
