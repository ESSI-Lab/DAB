package eu.essi_lab.access.augmenter;

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

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Optional;

import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author boldrini
 */
public class EmodPaceAugmenter extends ResourceAugmenter<EmodPaceAugmenterSetting> {

    /**
     * 
     */
    private static final String EMOD_PACE_AUGMENTER_CONNECTOR_FACTORY_ERROR = "EMOD_PACE_AUGMENTER_CONNECTOR_FACTORY_ERROR";

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	DataCacheConnector dataCacheConnector = null;
	String sourceId = null;
	String fileId = null;

	try {

	    dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

	    if (dataCacheConnector == null) {
		DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
		dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
		String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
		String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
		String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
		dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
		dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
		dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
		DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
	    }

	    fileId = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getFileIdentifier();
	    if (fileId == null) {
		throw new RuntimeException("Metadata identifier not set");
	    }

	    GSLoggerFactory.getLogger(getClass()).info("[EMOD-PACE] Augmenting {}", fileId);

	    sourceId = resource.getSource().getUniqueIdentifier();

	    StationRecord record = new StationRecord();
	    GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	    if (bbox == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Empty bbox for dataset {}", fileId);
		return Optional.empty();
	    }

	    record.setBbox4326(new BBOX4326(new BigDecimal(bbox.getSouth()), new BigDecimal(bbox.getNorth()),
		    new BigDecimal(bbox.getWest()), new BigDecimal(bbox.getEast())));

	    record.setMetadataIdentifier(fileId);
	    record.setSourceIdentifier(sourceId);
	    record.setDatasetName(resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	    ExtensionHandler handler = resource.getExtensionHandler();
	    Optional<String> themeCategory = handler.getThemeCategory();
	    if (themeCategory.isPresent()) {
		record.setThemeCategory(themeCategory.get());
	    }
	    // String downloadURL = reader.evaluateString(
	    // "/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:distributionInfo[1]/*:MD_Distribution[1]/*:transferOptions[2]/*:MD_DigitalTransferOptions[1]/*:onLine[1]/*:CI_OnlineResource[1]/*:linkage[1]/*[1]");

	    Iterator<Online> onlines = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution()
		    .getDistributionOnlines();
	    while (onlines.hasNext()) {
		Online online = (Online) onlines.next();
		String function = online.getFunctionCode();
		if (function != null && function.equals("download")) {
		    record.setDownloadUrl(online.getLinkage());
		}
	    }

	    String metadataURL = "https://seadatanet.geodab.eu/gs-service/services/essi/view/emod-pace/csw?service=CSW&version=2.0.2&request=GetRecordById&id="
		    + fileId + "&outputschema=http://www.isotc211.org/2005/gmi&elementSetName=full";
	    record.setMetadataUrl(metadataURL);

	    dataCacheConnector.writeStation(record);

	    GSLoggerFactory.getLogger(getClass()).info("[EMOD-PACE] Augmented {}", fileId);

	} catch (Exception e) {

	    throw GSException.createException(getClass(), EMOD_PACE_AUGMENTER_CONNECTOR_FACTORY_ERROR, e);
	}

	return Optional.empty();
    }

    @Override
    public String getType() {

	return "EmodPaceAugmenter";
    }

    @Override
    protected EmodPaceAugmenterSetting initSetting() {

	return new EmodPaceAugmenterSetting();
    }

    @Override
    protected String initName() {

	return "EMOD-PACE augmenter";
    }
}
