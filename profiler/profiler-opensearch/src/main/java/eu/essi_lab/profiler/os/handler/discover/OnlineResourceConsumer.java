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

	if (dist != null) {

	    String publicId = gsResource.getPublicId();

	    //	    gsResource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().get

	    dist.getDistributionOnlines().forEachRemaining(online -> {

		String name = online.getName();
		String protocol = online.getProtocol();

		NetProtocolWrapper wrapper = NetProtocolWrapper.of(protocol).orElse(null);

		String linkage = switch (wrapper) {

		    //
		    // ESRI MapServer
		    //

		    case ESRIMapServer_10_0_0, ESRIMapServer ->
			    "https://sim-dev.mase.gov.it/core/api/gil/request/dataset/" + publicId + "/esri/MapServer/" + name;

		    //
		    // WCS
		    //

		    case  WCS_1_0, WCS_EDO, WCS_1_0_0, WCS_1_0_0_TDS -> "";

		    case WCS_1_1, WCS_1_1_1 -> "";

		    case WCS_1_1_2 -> "";

		    case WCS_2_0 -> "";
		    case WCS_2_0_1 -> "";

		    //
		    // WFS
		    //

		    case WFS_1_0_0 -> buildOnline(message.getDataProxyServer().get(), publicId, wrapper);

		    case WFS_1_1_0 -> buildOnline(message.getDataProxyServer().get(),publicId, wrapper);

		    case WFS_2_0_0 -> buildOnline(message.getDataProxyServer().get(),publicId, wrapper);



		    //
		    // WMS
		    //

		    case WMS_1_1_1 ->  buildOnline(message.getDataProxyServer().get(),publicId, wrapper);
		    case WMS_1_3_0, WMS_Q_1_3_0 ->  buildOnline(message.getDataProxyServer().get(),publicId, wrapper);

		    case WMTS_1_0_0 ->  buildOnline(message.getDataProxyServer().get(),publicId, wrapper);

		    case null, default -> null;
		};

		if(linkage != null){

		    online.setLinkage(linkage);
		}

	    });
	}
    }

    /**
     * @param service
     * @param proxyEndpoint
     * @param datasetId
     * @param wrapper
     * @return
     */
    private String buildOnline(String proxyEndpoint, String datasetId, NetProtocolWrapper wrapper){

	return proxyEndpoint + "gil/request/dataset/" + datasetId
		+ "/ogc?service="+wrapper.get().getSrvType()+"&VERSION="+wrapper.get().getSrvVersion()+"&REQUEST=GetCapabilities";
    }
}
