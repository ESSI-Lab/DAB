/**
 * 
 */
package eu.essi_lab.accessor.sentinel;

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

import java.util.Iterator;
import java.util.Optional;

import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.SatelliteScene;

/**
 * @author Fabrizio
 */
public class SentinelThumbnailAugmenter extends ResourceAugmenter<AugmenterSetting> {

    private static final String SENTINEL_THUMBNAIL_AUGMENTER_ERROR = "SENTINEL_THUMBNAIL_AUGMENTER_ERROR";

    /**
     * 
     */
    public SentinelThumbnailAugmenter() {

    }

    /**
     * @param setting
     */
    public SentinelThumbnailAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    protected String initName() {

	return "Sentinel Thumbnail augmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }

    @Override
    public String getType() {

	return "SentinelThumbnailAugmenter";
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	Iterator<BrowseGraphic> overviews = miMetadata.getDataIdentification().getGraphicOverviews();
	if (overviews.hasNext()) {
	    // GSLoggerFactory.getLogger(getClass()).debug("Overview already set: ");
	    // overviews.forEachRemaining(o -> GSLoggerFactory.getLogger(getClass()).debug(o.getFileName()));
	    return Optional.empty();
	}

	try {

	    // GSLoggerFactory.getLogger(getClass()).debug("Augmentation STARTED");

	    Optional<SatelliteScene> optSatelliteScene = resource.getExtensionHandler().getSatelliteScene();

	    Optional<GSResource> out = Optional.empty();

	    if (optSatelliteScene.isPresent()) {

		SatelliteScene satelliteScene = optSatelliteScene.get();

		OriginalMetadata originalMD = resource.getOriginalMetadata();

		XMLDocumentReader reader = new XMLDocumentReader(originalMD.getMetadata());
		String id = reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='identifier']");

		SentinelMapper mapper = new SentinelMapper();
		boolean done = mapper.handleThumbnail(reader, satelliteScene, id, miMetadata);

		if (done) {

		    resource.getExtensionHandler().setSatelliteScene(satelliteScene);

		    out = Optional.of(resource);
		}
	    }

	    // GSLoggerFactory.getLogger(getClass()).debug("Augmentation ENDED");

	    return out;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SENTINEL_THUMBNAIL_AUGMENTER_ERROR, e);
	}
    }

}
