/**
 * 
 */
package eu.essi_lab.accessor.sentinel;

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

import java.util.Iterator;
import java.util.Optional;

import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.SatelliteScene;

/**
 * @author Fabrizio
 */
public class SentinelOnlineResourceAugmenter extends ResourceAugmenter<AugmenterSetting> {

    /**
     * 
     */
    private static final String SENTINEL_ONLINE_RESOURCE_AUGMENTER_ERROR = "SENTINEL_ONLINE_RESOURCE_AUGMENTER_ERROR";

    /**
     * 
     */
    public SentinelOnlineResourceAugmenter() {

    }

    /**
     * @param setting
     */
    public SentinelOnlineResourceAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    protected String initName() {

	return "Sentinel Online Resource augmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }

    @Override
    public String getType() {

	return "SentinelOnlineResourceAugmenter";
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	Distribution distribution = miMetadata.getDistribution();
	// Iterator<Online> onlines = distribution.getDistributionOnlines();
	boolean isAlreadySet = false;
	Iterator<Online> onlines = distribution.getDistributionOnlines();
	while (onlines.hasNext()) {
	    Online o = onlines.next();
	    if (o.getProtocol().equals(CommonNameSpaceContext.SENTINEL2_URI)) {
		isAlreadySet = true;
		break;
	    }
	}

	if (isAlreadySet) {
	    GSLoggerFactory.getLogger(getClass()).debug("Online Resource already set: ");
	    onlines.forEachRemaining(o -> GSLoggerFactory.getLogger(getClass()).debug(o.getName()));
	    return Optional.empty();
	}

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Augmentation STARTED");

	    // OriginalMetadata originalMD = resource.getOriginalMetadata();
	    //
	    // XMLDocumentReader reader = new XMLDocumentReader(originalMD.getMetadata());
	    //
	    // String id = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='identifier']");

	    SatelliteScene satelliteScene = resource.getExtensionHandler().getSatelliteScene().get();

	    SentinelMapper mapper = new SentinelMapper();

	    mapper.addComplexLink(miMetadata, miMetadata.getFileIdentifier(), satelliteScene);

	    Optional<GSResource> out = Optional.empty();

	    // if (done) {

	    resource.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);

	    out = Optional.of(resource);

	    GSLoggerFactory.getLogger(getClass()).debug("Augmentation ENDED");

	    return out;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SENTINEL_ONLINE_RESOURCE_AUGMENTER_ERROR, e);
	}
    }

}
