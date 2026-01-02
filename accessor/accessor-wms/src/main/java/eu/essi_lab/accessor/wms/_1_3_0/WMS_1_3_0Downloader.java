package eu.essi_lab.accessor.wms._1_3_0;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.wms.WMSDownloader;
import eu.essi_lab.jaxb.wms._1_3_0.WMSCapabilities;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;
import net.opengis.gml.v_3_2_0.EnvelopeType;

public class WMS_1_3_0Downloader extends WMSDownloader {

    private static final String WMS_DOWNLOADER_ERROR = "WMS_DOWNLOADER_ERROR";
    private WMS_1_3_0Connector connector;

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	String linkage = online.getLinkage();
	this.connector = new WMS_1_3_0Connector();
	this.connector.setSourceURL(linkage);
    }

    public WMS_1_3_0Connector getConnector() {
	return connector;
    }

    protected void setConnector(WMS_1_3_0Connector connector) {
	this.connector = connector;
    }

    @Override
    public boolean canDownload() {

	return NetProtocolWrapper.check(online.getProtocol(),NetProtocolWrapper.WMS_1_3_0);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	WMSCapabilities fullCapabilities = getConnector().retrieveCapabilities();
	if (fullCapabilities == null) {
	    throw GSException.createException( //
		    getClass(), //
		    "Server not ready", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "REMOTE_SERVER_ERROR");
	}
	WMS_1_3_0Capabilities capabilities = new WMS_1_3_0Capabilities(fullCapabilities);
	WMS_1_3_0Layer layer = new WMS_1_3_0Layer(capabilities, name);

	if (layer.getLayer() == null) {
	    throw GSException.createException( //
		    getClass(), //
		    "Layer not found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WMS_DOWNLOADER_ERROR);
	}

	try {

	    // TIME
	    Optional<Date> beginPosition = layer.getBeginPosition();
	    Optional<Date> endPosition = layer.getEndPosition();

	    DataDimension temporalDimension = null;

	    if (beginPosition.isPresent() && endPosition.isPresent()) {

		ContinueDimension continueTemporalDimension = new ContinueDimension(DataDescriptor.TIME_DIMENSION_NAME);

		continueTemporalDimension.setType(DimensionType.TIME);
		continueTemporalDimension.setLower(beginPosition.get().getTime());
		continueTemporalDimension.setUpper(endPosition.get().getTime());
		continueTemporalDimension.setUom(Unit.MILLI_SECOND);
		continueTemporalDimension.setDatum(Datum.UNIX_EPOCH_TIME());

		temporalDimension = continueTemporalDimension;

		List<Date> availableTimes = layer.getAvailableTimes();
		if (availableTimes != null && !availableTimes.isEmpty()) {
		    List<String> gridPoints = new ArrayList<>();
		    for (int i = 0; i < availableTimes.size(); i++) {
			Date time = availableTimes.get(i);
			gridPoints.add(ISO8601DateTimeUtils.getISO8601DateTime(time));
		    }
		    FiniteDimension finiteTemporalDimension = new FiniteDimension(DataDescriptor.TIME_DIMENSION_NAME);

		    finiteTemporalDimension.setType(DimensionType.TIME);
		    finiteTemporalDimension.setPoints(gridPoints);

		    finiteTemporalDimension.setPoints(gridPoints);

		    temporalDimension = finiteTemporalDimension;
		}

	    }

	    // AREA

	    List<String> formats = new ArrayList<>();
	    if (layer.getFormat() != null) {
		formats.addAll(layer.getFormat());
	    }
	    if (formats.isEmpty()) {
		formats = new ArrayList<>();
		// no format defined... trying with image png...
		formats.add("image/png");
	    }

	    List<String> crses = layer.getCRS();
	    if (crses == null || crses.isEmpty()) {
		crses = new ArrayList<>();
		crses.add("EPSG:4326");
	    }

	    HashSet<CRS> crsList = new HashSet<>();
	    for (String crs : crses) {
		CRS gridCRS = CRS.fromIdentifier(crs);
		crsList.add(gridCRS);
		if (gridCRS.equals(CRS.EPSG_4326())) {
		    // also lonlat is added.. it will be sufficient to invert appropriately the BBOX axis parameters
		    crsList.add(CRS.OGC_84());
		}
		if (gridCRS.equals(CRS.OGC_84())) {
		    // also latlon is added.. it will be sufficient to invert appropriately the BBOX axis parameters
		    crsList.add(CRS.EPSG_4326());
		}
	    }

	    for (String format : formats) {
		for (CRS crs : crsList) {

		    DataDescriptor descriptor = new DataDescriptor();

		    descriptor.setDataType(DataType.GRID);

		    descriptor.setDataFormat(DataFormat.fromIdentifier(format));

		    descriptor.setCRS(crs);

		    descriptor.setTemporalDimension(temporalDimension);

		    EnvelopeType envelope = layer.getEnvelope(crs.getIdentifier());
		    if (envelope == null) {
			// Envelope envelope4326 = layer.getEnvelope(CRS.EPSG4326);
			// if (envelope4326 != null) {
			// try {
			// CRSConverter converter = new CRSConverter(CRS.EPSG4326, crs);
			// envelope = converter.convert(envelope4326);
			// } catch (Exception e) {
			// // unsupported conversion
			// }
			// }
		    }
		    if (envelope != null) {

			List<DataDimension> spatialDimensions = new ArrayList<>();
			ContinueDimension firstDimension = new ContinueDimension("first");
			firstDimension.setLower(envelope.getLowerCorner().getValue().get(0));
			firstDimension.setUpper(envelope.getUpperCorner().getValue().get(0));
			spatialDimensions.add(firstDimension);
			ContinueDimension secondDimension = new ContinueDimension("second");
			secondDimension.setLower(envelope.getLowerCorner().getValue().get(1));
			secondDimension.setUpper(envelope.getUpperCorner().getValue().get(1));
			spatialDimensions.add(secondDimension);
			firstDimension.setName(crs.getFirstAxisName());
			if (firstDimension.getName() == null || firstDimension.getName().equals("")) {
			    firstDimension.setName("dim1");
			}
			secondDimension.setName(crs.getSecondAxisName());
			if (secondDimension.getName() == null || secondDimension.getName().equals("")) {
			    secondDimension.setName("dim2");
			}
			firstDimension.setUom(crs.getUOM());
			secondDimension.setUom(crs.getUOM());
			Integer defaultHeight = layer.getDefaultHeight();
			Integer defaultWidth = layer.getDefaultWidth();
			switch (crs.getAxisOrder()) {
			case NORTH_EAST:
			    firstDimension.setType(DimensionType.ROW);
			    secondDimension.setType(DimensionType.COLUMN);
			    if (defaultHeight != null) {
				firstDimension.setSize(defaultHeight.longValue());
			    }
			    if (defaultWidth != null) {
				secondDimension.setSize(defaultWidth.longValue());
			    }
			    break;
			case EAST_NORTH:
			default:
			    firstDimension.setType(DimensionType.COLUMN);
			    secondDimension.setType(DimensionType.ROW);
			    if (defaultWidth != null) {
				firstDimension.setSize(defaultWidth.longValue());
			    }
			    if (defaultHeight != null) {
				secondDimension.setSize(defaultHeight.longValue());
			    }
			    break;
			}

			descriptor.setSpatialDimensions(spatialDimensions);
			ret.add(descriptor);
		    }
		}
	    }

	    return ret;
	} catch (Exception e) {
	    e.printStackTrace();
	    throw GSException.createException( //
		    getClass(), //
		    "WMS accessor error found", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    WMS_DOWNLOADER_ERROR, //
		    e);
	}
    }

    public String getCRSKeyParameter() {
	return "&CRS=";
    }

    public String getVersionParameter() {
	return "&VERSION=1.3.0";
    }

    public String getBBOXParameter(DataDimension firstDimension, DataDimension secondDimension, CRS crs) {

	// 1.3.0 KVP order always minx, miny, maxx, maxy (see 06-042 7.3.2)
	String bbox = "&BBOX=" + //
		firstDimension.getContinueDimension().getLower() + "," + //
		secondDimension.getContinueDimension().getLower() + "," + //
		firstDimension.getContinueDimension().getUpper() + "," + //
		secondDimension.getContinueDimension().getUpper();
	return bbox;

    }

}
