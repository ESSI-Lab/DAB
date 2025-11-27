package eu.essi_lab.profiler.os.handler.discover;

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

	if(message.getDataProxyServer().isEmpty()){

	    GSLoggerFactory.getLogger(getClass()).warn("Missing 'dataProxyServer' in 'SystemSetting' -> 'Key-value options'");
	    return;
	}

	if (dist != null) {

	    String publicId = gsResource.getPublicId();

	    dist.getDistributionOnlines().forEachRemaining(online -> {

		String name = online.getName();
		String protocol = online.getProtocol();

		NetProtocolWrapper wrapper = NetProtocolWrapper.of(protocol).orElse(null);

		String linkage = switch (wrapper) {

		    //
		    // ESRI MapServer
		    //

		    case ESRIMapServer_10_0_0, //
			 ESRIMapServer -> //
			    buildOnline(message.getDataProxyServer().get(), publicId, "/esri/MapServer/");

		    //
		    // WCS
		    //

		    case WCS_1_0, //
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

			 WFS_1_0_0,//
			 WFS_1_1_0,//
			 WFS_2_0_0,//

			 //
			 // WMS
			 //

			 WMS_1_1_1,//
			 WMS_1_3_0, //
			 WMS_Q_1_3_0,//

			 WMTS_1_0_0 -> buildOnline(message.getDataProxyServer().get(), publicId, "/ogc");

		    case null, default -> null;
		};

		if (linkage != null) {

		    online.setLinkage(linkage);
		}

	    });
	}
    }

    /**
     * @param proxyEndpoint
     * @param datasetId
     * @param lastPath
     * @return
     */
    private String buildOnline(String proxyEndpoint, String datasetId, String lastPath) {

	return proxyEndpoint + "gil/request/dataset/" + datasetId + lastPath;
    }
}
