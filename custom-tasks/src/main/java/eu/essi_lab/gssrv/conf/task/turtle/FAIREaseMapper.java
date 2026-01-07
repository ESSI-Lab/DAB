package eu.essi_lab.gssrv.conf.task.turtle;

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

public class FAIREaseMapper {

    public static FAIREaseMapping map(String protocol, String linkage, String sourceId) {
	FAIREaseMapping ret = new FAIREaseMapping();
	String mappedProtocol = mapProtocol(protocol, linkage, sourceId);
	ret.setProtocol(mappedProtocol);
	ret.setInAccessService(isInAccessService(mappedProtocol));
	FAIREaseProtocol mapped = FAIREaseProtocol.decode(mappedProtocol);
	if (mapped != null) {
	    switch (mapped) {
	    case APPLICATION_XML:
		ret.setProtocol(FAIREaseProtocol.METADATA.getUri());
		ret.setMediaType(FAIREaseProtocol.APPLICATION_XML.getUri());
		break;
	    case GML:
		ret.setProtocol(FAIREaseProtocol.DIRECT_DOWNLOAD.getUri());
		ret.setMediaType(FAIREaseProtocol.GML.getUri());
		break;
	    case KML:
		ret.setProtocol(FAIREaseProtocol.DIRECT_DOWNLOAD.getUri());
		ret.setMediaType(FAIREaseProtocol.KML.getUri());
		break;
	    case SHAPE_FILE:
		ret.setProtocol(FAIREaseProtocol.DIRECT_DOWNLOAD.getUri());
		ret.setMediaType(FAIREaseProtocol.SHAPE_FILE.getUri());
		break;

	    default:
		break;
	    }
	}
	return ret;
    }

    public static String mapProtocol(String protocol, String linkage, String sourceId) {

	if (sourceId == null) {
	    return protocol;
	}
	if (protocol == null) {
	    return null;
	}
	protocol = protocol.trim();
	switch (sourceId) {
	case "marineID": // CMEMS
	    switch (protocol) {
	    case "OGC:WMTS":
		return FAIREaseProtocol.OGC_WMTS.getUri();
	    case "WWW:STAC":
		return FAIREaseProtocol.STAC.getUri();
	    case "NETWORK:LINK":
		// TODO
		break;
	    default:
		break;
	    }
	    break;
	case "easydata": // EasyData
	    switch (protocol) {
	    case "DOI":
		return FAIREaseProtocol.LANDING_PAGE.getUri();
	    case "DOI jeu de données lié":
		return FAIREaseProtocol.LANDING_PAGE.getUri();

	    case "WWW:LINK-1.0-http--link":
		return FAIREaseProtocol.HTTP.getUri();

	    case "Code source en accès ouvert":
		return FAIREaseProtocol.LANDING_PAGE.getUri(); // CHECK

	    case "Publication en accès ouvert":
		return FAIREaseProtocol.LANDING_PAGE.getUri(); // CHECK

	    case "Service de téléchargement":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri(); // check

	    case "Service Web cartographique":
		return FAIREaseProtocol.OGC_WMS.getUri(); // check

	    case "Site Web projet":
		return FAIREaseProtocol.LANDING_PAGE.getUri(); // check
	    default:
		break;
	    }
	    break;
	case "UUID-456602db-4275-4410-8b68-436fd23ace69": // EEA SDI
	    switch (protocol) {

	    case "DOI":
		return FAIREaseProtocol.LANDING_PAGE.getUri();

	    case "EEA:FILEPATH":
		if (linkage.endsWith("/")) {
		    return FAIREaseProtocol.FOLDER_PATH.getUri();
		} else {
		    return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri();
		}

	    case "EEA:FOLDERPATH":
		return FAIREaseProtocol.FOLDER_PATH.getUri();

	    case "ESRI:REST":
		return FAIREaseProtocol.ARCGIS_REST.getUri();

	    case "OGC:OpenSearch":
		return FAIREaseProtocol.OGC_OPENSEARCH_GEO.getUri();

	    case "OGC:WCS":
		return FAIREaseProtocol.OGC_WCS.getUri();

	    case "OGC:WMS":
		return FAIREaseProtocol.OGC_WMS.getUri();

	    case "OGC:WMTS":
		return FAIREaseProtocol.OGC_WMTS.getUri();

	    case "WWW:DOWNLOAD":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri();

	    case "WWW:DOWNLOAD-1.0-http--download":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri();

	    case "WWW:DOWNLOAD-1.0-ftp--download":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri(); // comments

	    case "WWW:LINK":
		return FAIREaseProtocol.HTTP.getUri();

	    case "WWW:LINK-1.0-http--link":
		return FAIREaseProtocol.HTTP.getUri();

	    case "WWW:URL":
		return FAIREaseProtocol.FOLDER_PATH.getUri(); // report back

	    case "EEA:DBPG":
		break; // not usable

	    case "WWW:DOWNLOAD:Microsoft Excel (.xls, .xlsx)":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri(); // error link, 1 record

	    case "null":
		break;

	    default:
		break;
	    }
	    break;
	case "emodnet-network": // EMODNET

	    switch (protocol) {

	    case "DOI":
		return FAIREaseProtocol.LANDING_PAGE.getUri();

	    case "OGC:WCS":
		return FAIREaseProtocol.OGC_WCS.getUri();

	    case "OGC:WFS":
		return FAIREaseProtocol.OGC_WFS.getUri();

	    case "OGC Web Feature Service":
		return FAIREaseProtocol.OGC_WFS.getUri();

	    case "OGC:WFS-1.0.0-http-get-capabilities":
		return FAIREaseProtocol.OGC_WFS_1_0.getUri();

	    case "OGC:WMS":
		return FAIREaseProtocol.OGC_WMS.getUri();

	    case "OGC Web Map Service":
		return FAIREaseProtocol.OGC_WMS.getUri();

	    case "OGC:WMS-1.1.1-http-get-map":
		return FAIREaseProtocol.OGC_WMS_1_1.getUri();

	    case "OGC:WMS-1.1.1-http-get-capabilities":
		return FAIREaseProtocol.OGC_WMS_1_1.getUri();

	    case "OGC:WMS-1.3.0-http-get-map":
		return FAIREaseProtocol.OGC_WMS_1_3.getUri();

	    case "OGC:WMS-1.3.0-http-get-capabilities":
		return FAIREaseProtocol.OGC_WMS_1_3.getUri();

	    case "OGC:WMTS":
		return FAIREaseProtocol.OGC_WMTS.getUri();

	    case "WWW:DOWNLOAD":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri();

	    case "WWW:DOWNLOAD-1.0-http--download":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri();

	    case "WWW:DOWNLOAD-1.0-link--download":
		return FAIREaseProtocol.DIRECT_DOWNLOAD.getUri();

	    case "WWW:LINK":
		return FAIREaseProtocol.HTTP.getUri();

	    case "WWW:LINK-1.0-http--related":
		return FAIREaseProtocol.HTTP.getUri();

	    case "WWW:LINK-1.0-http--link":
		return FAIREaseProtocol.HTTP.getUri();

	    case "WWW:LINK-1.0-http--metadata-URL":
		return FAIREaseProtocol.LANDING_PAGE.getUri();

	    case "WWW:LINK-1.0-http--publication-URL":
		return FAIREaseProtocol.LANDING_PAGE.getUri();

	    case "application/xml":
		return FAIREaseProtocol.APPLICATION_XML.getUri();// check TODO

	    case "application/gml+xml":
		return FAIREaseProtocol.GML.getUri();// TODO

	    case "OGC:GML":
		return FAIREaseProtocol.GML.getUri(); // TODO

	    case "application/vnd.google-earth.kml+xml":
		return FAIREaseProtocol.KML.getUri(); // check

	    case "application/vnd.shp":
		return FAIREaseProtocol.SHAPE_FILE.getUri(); // check

	    case "application/vnd.ogc.wms_xml":
		return FAIREaseProtocol.OGC_WMS.getUri(); // check

	    case "application/vnd.ogc.wfs_xml":
		return FAIREaseProtocol.OGC_WFS.getUri(); // check

	    case "text/xml":
		return FAIREaseProtocol.TEXT_XML.getUri(); // check

	    case "image/png":
		break; // TODO
	    // return FAIREaseProtocol.

	    case "null":
		break;
	    // return FAIREaseProtocol.

	    default:
		break;
	    }

	    break;
	case "icos-socat": // ICOS SOCAT
	    switch (protocol) {

	    default:
		break;
	    }
	    break;
	case "jrcdatacatalogdbid": // JRC
	    switch (protocol) {
	    case "HTTP":
		return FAIREaseProtocol.HTTP.getUri();

	    default:
		break;
	    }
	    break;
	case "usnodcdbid": // US NODC
	    switch (protocol) {

	    case "ftp":
		return FAIREaseProtocol.FTP.getUri();

	    case "FTP":
		return FAIREaseProtocol.FTP.getUri();

	    case "http":
		return FAIREaseProtocol.HTTP.getUri();

	    case "HTTPS":
		return FAIREaseProtocol.HTTPS.getUri();

	    case "https":
		return FAIREaseProtocol.HTTPS.getUri();

	    case "OGC:WCS":
		return FAIREaseProtocol.OGC_WCS.getUri();

	    case "OGC:WMS":
		return FAIREaseProtocol.OGC_WMS.getUri();

	    case "OPeNDAP:OPeNDAP":
		return FAIREaseProtocol.OPENDAP.getUri();

	    default:
		break;
	    }
	    break;
	case "FROMREGISTRY--regprefseparator--registrytestid1--regprefseparator--dad0859e-206c-480b-a080-d4eea70ee22d": // VITO
	    switch (protocol) {
	    case "HTTP":
		return FAIREaseProtocol.HTTP.getUri();

	    case "HTTPS":
		return FAIREaseProtocol.HTTPS.getUri();

	    // case "Empty list":
	    // return FAIREaseProtocol.
	    //
	    // case "Beacon endpoint?":
	    // return FAIREaseProtocol.
	    //
	    // case "Sparql endpoint":
	    // return FAIREaseProtocol.
	    //
	    // case "openeo, examind, stac, ..":
	    // return FAIREaseProtocol.

	    default:
		break;
	    }
	    break;
	case "wekeo": // WEKEO
	    switch (protocol) {

	    default:
		break;
	    }
	    break;
	default:
	    break;
	}
	return protocol;

    }

    public static boolean isInAccessService(String protocol) {
	if (protocol == null) {
	    return false;
	}
	FAIREaseProtocol fep = FAIREaseProtocol.decode(protocol);
	if (fep!=null) {
	switch (fep) {
	case ARCGIS_REST:
	case OGC_OPENSEARCH_GEO:
	case OGC_WCS:
	case OGC_WFS:
	case OGC_WFS_1_0:
	case OGC_WMS:
	case OGC_WMS_1_1:
	case OGC_WMS_1_3:
	case OGC_WMTS:
	case OPENDAP:
	case SPARQL:
	case STAC:
	    return true;
	default:
	    return false;
	}
	}else {
	    return false;
	}

    }

}
