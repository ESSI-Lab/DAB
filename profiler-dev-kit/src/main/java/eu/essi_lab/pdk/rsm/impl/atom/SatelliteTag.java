/**
 * 
 */
package eu.essi_lab.pdk.rsm.impl.atom;

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

import java.util.Objects;

import org.jdom2.Element;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.atom.CustomEntry;

/**
 * @author Fabrizio
 */
public abstract class SatelliteTag {

    /**
     * 
     */
    protected Element acquisition;

    /**
     * 
     */
    public SatelliteTag() {

	acquisition = CustomEntry.createElement("acquisition", NameSpace.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param platformid
     */
    public void setPlatformId(String platformid) {

	if (Objects.nonNull(platformid)) {

	    CustomEntry.addContentTo(acquisition, "platformid", platformid);
	}
    }

    /**
     * @param platform
     */
    public void setPlatformDesc(String platform) {

	if (Objects.nonNull(platform)) {

	    CustomEntry.addContentTo(acquisition, "platform", platform);
	}
    }

    /**
     * @param instrument
     */
    public void setInstrument(String instrument) {

	if (Objects.nonNull(instrument)) {

	    CustomEntry.addContentTo(acquisition, "instrument", instrument);
	}
    }

    /**
     * @param productType
     */
    public void setProductType(String productType) {

	if (Objects.nonNull(productType)) {

	    CustomEntry.addContentTo(acquisition, "productType", productType);
	}
    }

    /**
     * @param cloud_cover_percentage
     */
    public void setCloudCoverPercentage(String cloud_cover_percentage) {

	if (Objects.nonNull(cloud_cover_percentage)) {

	    CustomEntry.addContentTo(acquisition, "cloud_cover_percentage", cloud_cover_percentage);
	}
    }

    /**
     * @return
     */
    public Element getAcquisitionElement() {

	return acquisition;
    }

    /**
     * @return
     */
    public boolean isEmpty() {

	return getAcquisitionElement().getContentSize() == 0;
    }

}
