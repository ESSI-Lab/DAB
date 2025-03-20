package eu.essi_lab.gssrv.conf.task.turtle;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

public enum FAIREaseProtocol {

    OGC_OPENSEARCH_GEO("http://www.opengis.net/def/serviceType/ogc/opensearch-geo"), //
    OGC_WCS("http://www.opengis.net/def/serviceType/ogc/wcs"), //
    OGC_WFS("http://www.opengis.net/def/serviceType/ogc/wfs"), //
    OGC_WFS_1_0("http://www.opengis.net/def/serviceType/ogc/wfs/1.0"), //
    OGC_WMS("http://www.opengis.net/def/serviceType/ogc/wms"), //
    OGC_WMS_1_1("http://www.opengis.net/def/serviceType/ogc/wms/1.1"), //
    OGC_WMS_1_3("http://www.opengis.net/def/serviceType/ogc/wms/1.3"), //
    OGC_WMTS("http://www.opengis.net/def/serviceType/ogc/wmts"), //
    ARCGIS_REST("https://developers.arcgis.com/rest/"), //
    DIRECT_DOWNLOAD("https://lab.fairease.eu/asset-standards/profiles#direct-download"), //
    FOLDER_PATH("https://lab.fairease.eu/asset-standards/profiles#folder-path"), //
    FTP("https://lab.fairease.eu/asset-standards/profiles#ftp"), //
    HTTP("https://lab.fairease.eu/asset-standards/profiles#http"), //
    HTTPS("https://lab.fairease.eu/asset-standards/profiles#https"), //
    LANDING_PAGE("https://lab.fairease.eu/asset-standards/profiles#landing-page"), //
    METADATA("https://lab.fairease.eu/asset-standards/profiles#metadata"), //
    OPENDAP("https://lab.fairease.eu/asset-standards/profiles#opendap"), //
    STAC("https://lab.fairease.eu/asset-standards/profiles#stac"), //
    GML("https://www.iana.org/assignments/media-types/application/gml+xml"), //
    SPARQL("https://www.iana.org/assignments/media-types/application/sparql-query"), //
    KML("https://www.iana.org/assignments/media-types/application/vnd.google-earth.kml+xml"), //
    SHAPE_FILE("https://www.iana.org/assignments/media-types/application/vnd.shx"), //
    APPLICATION_XML("https://www.iana.org/assignments/media-types/application/xml"), //
    TEXT_XML("https://www.iana.org/assignments/media-types/text/xml");//

    private FAIREaseProtocol(String uri) {
	this.uri = uri;
    }

    private String uri = null;

    public String getUri() {
	return uri;
    }
}
