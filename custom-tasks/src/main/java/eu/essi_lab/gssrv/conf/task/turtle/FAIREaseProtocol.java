package eu.essi_lab.gssrv.conf.task.turtle;

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
    DIRECT_DOWNLOAD("https://w3id.org/fair-ease/ns/endpoint-types#direct-download"), //
    FOLDER_PATH("https://w3id.org/fair-ease/ns/endpoint-types#folder-path"), //
    FTP("https://w3id.org/fair-ease/ns/endpoint-types#ftp"), //
    HTTP("https://w3id.org/fair-ease/ns/endpoint-types#http"), //
    HTTPS("https://w3id.org/fair-ease/ns/endpoint-types#https"), //
    LANDING_PAGE("https://w3id.org/fair-ease/ns/endpoint-types#landing-page"), //
    METADATA("https://w3id.org/fair-ease/ns/endpoint-types#metadata"), //
    OPENDAP("https://w3id.org/fair-ease/ns/endpoint-types#opendap"), //
    STAC("https://w3id.org/fair-ease/ns/endpoint-types#stac"), //
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

    public static FAIREaseProtocol decode(String protocol) {
	if (protocol ==null) {
	    return null;
	}
	for (FAIREaseProtocol ret : FAIREaseProtocol.values()) {
	    if (ret.getUri().equals(protocol)) {
		return ret;
	    }
	}
	return null;
    }
}
