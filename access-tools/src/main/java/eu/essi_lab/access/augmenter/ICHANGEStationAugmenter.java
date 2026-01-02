package eu.essi_lab.access.augmenter;

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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author boldrini
 */
public class ICHANGEStationAugmenter extends ResourceAugmenter<ICHANGEStationAugmenterSetting> {

    /**
     * 
     */
    private static final String STATION_AUGMENTER_CONNECTOR_FACTORY_ERROR = "STATION_AUGMENTER_CONNECTOR_FACTORY_ERROR";

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

	    GSLoggerFactory.getLogger(getClass()).info("[STATION] Augmenting {}", fileId);

	    sourceId = resource.getSource().getUniqueIdentifier();

	    MIMetadata miMetadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	    StationRecord record = new StationRecord();
	    GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	    if (bbox == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Empty bbox for dataset {}", fileId);
		return Optional.empty();
	    }

	    Double south = bbox.getSouth();
	    Double east = bbox.getEast();
	    Double north = bbox.getNorth();
	    Double west = bbox.getWest();

	    TemporalExtent te = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	    if (te!=null) {
	    	String begin = te.getBeginPosition();
	    	String end = te.getEndPosition();
	    	if (begin!=null && end!=null) {
	    		Optional<Date> optBegin = ISO8601DateTimeUtils.parseISO8601ToDate(begin);
	    		Optional<Date> optEnd = ISO8601DateTimeUtils.parseISO8601ToDate(end);
	    		if (optBegin.isPresent()&&optEnd.isPresent()) {
	    			record.setBegin(optBegin.get());
	    			record.setEnd(optEnd.get());
	    		}	    		
	    	}
	    }
	    MIPlatform platform = miMetadata.getMIPlatform();

	    String stationTitle = platform.getCitation().getTitle();
	    record.setBbox4326(new BBOX4326(new BigDecimal(bbox.getSouth()), new BigDecimal(bbox.getNorth()),
		    new BigDecimal(bbox.getWest()), new BigDecimal(bbox.getEast())));
	    ExtensionHandler handler = resource.getExtensionHandler();

	    record.setSourceIdentifier(sourceId);
	    String id = handler.getUniquePlatformIdentifier().get();
	    record.setPlatformIdentifier(id);
	    record.setDatasetName(stationTitle);

	    record.setMetadataIdentifier(id);
	    
	    
	    

	    // String downloadURL = reader.evaluateString(
	    // "/*/*:harmonizedMetadata[1]/*:coreMetadata[1]/*:isoMetadata[1]/*:MI_Metadata[1]/*:distributionInfo[1]/*:MD_Distribution[1]/*:transferOptions[2]/*:MD_DigitalTransferOptions[1]/*:onLine[1]/*:CI_OnlineResource[1]/*:linkage[1]/*[1]");

	    String metadataURL = null;
	    
	    if(resource.getSource().getEndpoint().contains("trigger-io.difa.unibo.it") || resource.getSource().getEndpoint().contains("aux.ecmwf.int")) {
		record.setThemeCategory("trigger");
		metadataURL = "https://gs-service-production.geodab.eu/gs-service/services/essi/view/trigger/info?monitoringPoint=" + id;
	    } else {
		record.setThemeCategory("i-change");
		metadataURL = "https://gs-service-production.geodab.eu/gs-service/services/essi/view/i-change/info?monitoringPoint=" + id;
	    }
	  
	    record.setMetadataUrl(metadataURL);

	    // retrieve track
	    Iterator<BoundingPolygon> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
		    .getBoundingPolygons();
	    if (iterator != null) {
		if (iterator.hasNext()) {
		    BoundingPolygon polygon = iterator.next();
		    List<List<Double>> multiPoints = polygon.getMultiPoints();
		    if (multiPoints != null) {
			String linestring = "LINESTRING (";
			for (List<Double> point : multiPoints) {
			    if (point.size() >= 2) {
				Double lon = point.get(0);
				Double lat = point.get(1);
				linestring += lon + " " + lat;
			    }
			    if (point.size() == 3) {
				Double alt = point.get(2);
				linestring += " " + alt;
			    }
			    linestring += ",";
			}
			if (linestring.endsWith(",")) {
			    linestring = linestring.substring(0, linestring.length() - 1);
			}
			linestring += ")";
			record.setShape(linestring);
		    }
		}
	    }

	    record.setLastHarvesting(new Date());
	    dataCacheConnector.writeStation(record);

	    GSLoggerFactory.getLogger(getClass()).info("[STATION] Augmented {}", fileId);

	} catch (Exception e) {

	    throw GSException.createException(getClass(), STATION_AUGMENTER_CONNECTOR_FACTORY_ERROR, e);
	}

	return Optional.empty();
    }

    @Override
    public String getType() {

	return "ICHANGEStationAugmenter";
    }

    @Override
    protected ICHANGEStationAugmenterSetting initSetting() {

	return new ICHANGEStationAugmenterSetting();
    }

    @Override
    protected String initName() {

	return "ICHANGE STATION augmenter";
    }
}
