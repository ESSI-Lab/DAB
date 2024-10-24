package eu.essi_lab.ommdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Iterator;

import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CMRResourceMapper extends GMDResourceMapper {

    @Override
    public GSResource map(OriginalMetadata originalMD, GSSource source) throws GSException {
	GSResource ret = super.map(originalMD, source);
	MIMetadata metadata = ret.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	if (metadata != null) {
	    Distribution distribution = metadata.getDistribution();
	    if (distribution != null) {
		Iterator<Online> iterator = distribution.getDistributionOnlines();
		while (iterator.hasNext()) {
		    Online online = (Online) iterator.next();
		    String linkage = online.getLinkage();
		    if (linkage != null && linkage.contains("192.168.0.190")) {

			linkage = linkage.replace("192.168.0.190", "geoportal.rcmrd.org");

			online.setLinkage(linkage);
		    }
		}
		Iterator<Online> iterator2 = distribution.getDistributorOnlines();
		while (iterator2.hasNext()) {
		    Online online = (Online) iterator2.next();
		    String linkage = online.getLinkage();
		    if (linkage != null && linkage.contains("192.168.0.190")) {

			linkage = linkage.replace("192.168.0.190", "geoportal.rcmrd.org");

			online.setLinkage(linkage);
		    }
		}
	    }
	}
	return ret;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.CMR_NS_URI;
    }

}
