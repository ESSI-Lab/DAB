package eu.essi_lab.pdk;

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

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResourceConsumer;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class OnlineResourceConsumer implements ResourceConsumer {

    @Override
    public void consume(GSResource gsResource, DiscoveryMessage message) {

	final Distribution dist = gsResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	if (message.getDataProxyServer().isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Missing 'dataProxyServer' in 'SystemSetting' -> 'Key-value options'");
	    return;
	}

	if (dist != null) {

	    String publicId = gsResource.getPublicId();

	    dist.getDistributionOnlines().forEachRemaining(online -> {

		String name = online.getName();
		String protocol = online.getProtocol();

		//
		// first try: guessing from protocol encoded by us from WCS, WMS, WFS, WMTS mappers
		//

		String linkage = fromProtocol(message, publicId, protocol);

		if (linkage == null) {

		    //
		    // second try: guessing from protocol encoded with OpenGIS IRI (e.g.: http://www.opengis.net/def/serviceType/ogc/wms)
		    //

		    String protocolGmxAnchor = online.getProtocolGmxAnchorHref();

		    linkage = fromProtocol(message, publicId, protocolGmxAnchor);

		    if (linkage == null) {

			//
			// last chances: guessing from plain text (e.g: WMS, OGC Web Map Service (WMS), etc..)
			//

			linkage = fromPlainText(message, publicId, protocol);

			if (linkage == null) {

			    linkage = fromPlainText(message, publicId, protocolGmxAnchor);
			}
		    }
		}

		if (linkage != null) {

		    online.setLinkage(linkage);
		}
	    });
	}
    }

    /**
     * @param protocol
     * @param message
     * @param publicId
     * @return
     */
    private String fromPlainText(DiscoveryMessage message, String publicId, String protocol) {

	if (protocol == null) {

	    return null;
	}

	if (protocol.toLowerCase().contains("wms")) {

	    return fromProtocol(message, publicId, NetProtocolWrapper.WMS.getCommonURN());

	} else if (protocol.toLowerCase().contains("wfs")) {

	    return fromProtocol(message, publicId, NetProtocolWrapper.WFS.getCommonURN());

	} else if (protocol.toLowerCase().contains("wcs")) {

	    return fromProtocol(message, publicId, NetProtocolWrapper.WCS.getCommonURN());

	} else if (protocol.toLowerCase().contains("wmts")) {

	    return fromProtocol(message, publicId, NetProtocolWrapper.WMTS.getCommonURN());
	}

	return null;
    }

    /**
     * @param message
     * @param publicId
     * @param protocol
     * @return
     */
    private String fromProtocol(DiscoveryMessage message, String publicId, String protocol) {

	NetProtocolWrapper wrapper = NetProtocolWrapper.of(protocol).orElse(null);

	return switch (wrapper) {

	    //
	    // ESRI MapServer
	    //

	    case ESRIMapServer_10_0_0, //
		 ESRIMapServer -> //
		    fromProtocol(message.getDataProxyServer().get(), publicId, "/esri/MapServer/");

	    //
	    // WCS
	    //

	    case WCS,//
		 WCS_1_0, //
		 WCS_EDO, //
		 WCS_1_0_0, //
		 WCS_1_0_0_TDS,//
		 WCS_1_1, WCS_1_1_1,//
		 WCS_1_1_2,//
		 WCS_2_0,//
		 WCS_2_0_1,//

		 //
		 // WFS
		 //

		 WFS,//
		 WFS_1_0_0,//
		 WFS_1_1_0,//
		 WFS_2_0_0,//

		 //
		 // WMS
		 //

		 WMS,//
		 WMS_1_1_1,//
		 WMS_1_3_0, //
		 WMS_Q_1_3_0,//

		 //
		 // WMTS
		 //

		 WMTS,//
		 WMTS_1_0_0 -> fromProtocol(message.getDataProxyServer().get(), publicId, "/ogc");

	    case null, default -> null;
	};
    }

    /**
     * @param proxyEndpoint
     * @param datasetId
     * @param lastPath
     * @return
     */
    private String fromProtocol(String proxyEndpoint, String datasetId, String lastPath) {

	return proxyEndpoint + "gil/request/dataset/" + datasetId + lastPath;
    }
}
