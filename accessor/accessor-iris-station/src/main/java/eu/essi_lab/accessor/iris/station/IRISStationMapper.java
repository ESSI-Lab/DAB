/**
 * 
 */
package eu.essi_lab.accessor.iris.station;

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

import java.util.Date;

import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceType;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class IRISStationMapper extends OriginalIdentifierMapper {

    /**
     * 
     */
    public static final String IRIS_STATION_SCHEMA = "http://www.fdsn.org/text/station";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	IRISStationWrapper wrapper = IRISStationWrapper.asWrapper(resource.getOriginalMetadata());

	return wrapper.getId();
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	IRISStationWrapper wrapper = IRISStationWrapper.asWrapper(originalMD);
	String chanCode = wrapper.getChanCode();
	Double elev = wrapper.getElev();
	String endTime = wrapper.getEndTime();
	String id = wrapper.getId();
	String instrument = wrapper.getInstrument();
	Double lat = wrapper.getLat();
	Double lon = wrapper.getLon();
	String netCode = wrapper.getNetCode();
	String netDesc = wrapper.getNetDesc();
	String parentId = wrapper.getParentId();
	String staCode = wrapper.getStaCode();
	String startTime = wrapper.getStartTime();
	String staSiteName = wrapper.getStaSiteName();
	ResourceType type = wrapper.getType();

	GSResource out = null;

	if (type == ResourceType.DATASET) {
	    out = new Dataset();
	} else {
	    out = new DatasetCollection();
	}

	out.setSource(source);

	MIMetadata mi_Metadata = out.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	mi_Metadata.setParentIdentifier(parentId != null ? parentId : source.getUniqueIdentifier());

	setCitation(mi_Metadata, "Channel [" + netCode + "_" + staCode + "_" + chanCode + "]",
		"Channel " + chanCode + " operating on Station site " + staSiteName + " on Network " + netDesc);

	if (startTime != null && endTime != null) {
	    mi_Metadata.getDataIdentification().addTemporalExtent("_" + id, startTime, endTime);
	}

	if (lat != null && lon != null) {
	    mi_Metadata.getDataIdentification().addGeographicBoundingBox(lat, lon, lat, lon);
	}

	if (elev != null) {
	    mi_Metadata.getDataIdentification().addVerticalExtent(elev, elev);
	}

	setOnline(mi_Metadata, netCode, staCode, chanCode);

	if (instrument != null) {

	    MIInstrument mi_Instrument = new MIInstrument();
	    mi_Instrument.setDescription(instrument);
	    mi_Metadata.addMIInstrument(mi_Instrument);
	}

	return out;
    }

    private void setOnline(MIMetadata md, String netCode, String staCode, String chanCode) {

	String start = md.getDataIdentification().getTemporalExtent().getBeginPosition();
	String end = md.getDataIdentification().getTemporalExtent().getEndPosition();

	final double ONE_WEEK_MILLIS = 6.048e+8;

	if (end != null && end.equals("now")) {
	    end = ISO8601DateTimeUtils.getISO8601DateTime();
	}

	if (end != null) {

	    try {
		long endTime = ISO8601DateTimeUtils.parseISO8601(end).getTime();
		start = ISO8601DateTimeUtils.getISO8601DateTime(new Date((long) (endTime - ONE_WEEK_MILLIS)));
	    } catch (IllegalArgumentException e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }

	} else if (start != null && end == null) {

	    try {
		long startTime = ISO8601DateTimeUtils.parseISO8601(start).getTime();
		end = ISO8601DateTimeUtils.getISO8601DateTime(new Date((long) (startTime - ONE_WEEK_MILLIS)));
	    } catch (IllegalArgumentException e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	} else {
	    return;
	}

	String endpoint = getDataselectServiceEndpoint() + "net=" + netCode + "&sta=" + staCode + "&loc=--&cha=" + chanCode + "&start="
		+ start + "&end=" + end;

	md.getDistribution().clearDistributionOnlines();

	// no authentication online

	Online noAuthOnline = createOnline(endpoint, "Dataselect URI", "Last week of time series data in miniSEED format", true);
	md.getDistribution().addDistributionOnline(noAuthOnline);

	// authentication online

	endpoint = getDataselectAuthServiceEndpoint() + "net=" + netCode + "&sta=" + staCode + "&loc=--&cha=" + chanCode + "&start=" + start
		+ "&end=" + end;

	Online authOnline = createOnline(endpoint, "Last week of time series data in miniSEED format (Digest authentication required)",
		"Dataselect URI with Digest authentication to the MiniSEED data", false);
	md.getDistribution().addDistributionOnline(authOnline);

	// distribution format
	Format format = new Format();
	format.setName("MiniSEED");

	// adds the format
	md.getDistribution().addFormat(format);
    }

    private void setCitation(MIMetadata mi_Metadata, String title, String abstract_) {

	mi_Metadata.getDataIdentification().setCitationTitle(title);
	mi_Metadata.getDataIdentification().addCitationDate(ISO8601DateTimeUtils.getISO8601DateTime(), "creation");
	mi_Metadata.getDataIdentification().setAbstract(abstract_);
    }

    private String getDataselectServiceEndpoint() {

	return "http://service.iris.edu/fdsnws/dataselect/1/query?";
    }

    private String getDataselectAuthServiceEndpoint() {

	return "http://service.iris.edu/fdsnws/dataselect/1/queryauth?";
    }

    private Online createOnline(String endpoint, String name, String description, boolean direct) {

	Online online = new Online();
	online.setLinkage(endpoint);
	online.setProtocol("HTTP_GET");
	online.setName(name);
	online.setDescription(description);
	online.setFunctionCode("download");

	return online;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return IRIS_STATION_SCHEMA;
    }

}
