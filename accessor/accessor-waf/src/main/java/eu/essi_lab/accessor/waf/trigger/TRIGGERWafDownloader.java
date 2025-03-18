package eu.essi_lab.accessor.waf.trigger;

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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.geotools.imageio.netcdf.GeoToolsNetCDFReader;
import org.geotools.imageio.netcdf.utilities.NetCDFCRSUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;

public class TRIGGERWafDownloader extends DataDownloader {

    @Override
    public boolean canDownload() {
	return online.getProtocol().equals(TRIGGERWafConnector.S3_TRIGGER_PROTOCOL);
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    File file = TRIGGERWafConnector.getLocalCopy(online.getLinkage(), online.getName());
	    List<DataDescriptor> ret = new ArrayList<>();

	    NetcdfDataset ncDataset = NetcdfDataset.openDataset(file.getAbsolutePath());

	    List<Variable> mainVariables = NetCDFUtils.getGeographicVariables(ncDataset);
	    Variable mainVariable = mainVariables.get(0);
	    CoordinateReferenceSystem decodedCRS = GeoToolsNetCDFReader.extractCRS(ncDataset, mainVariable);
	    CRS crs = CRS.EPSG_4326();
	    if (decodedCRS != null) {
		// this workaround is because by default GeoTools NetcdfUtils uses OGC CRS 84
		// for NetCDF latitude
		// longitude crs
		if (decodedCRS.equals(NetCDFCRSUtilities.WGS84)) {
		    decodedCRS = org.geotools.referencing.CRS.decode("EPSG:4326");
		}

		crs = CRS.fromGeoToolsCRS(decodedCRS);

	    }

	    List<CoordinateAxis> axes = ncDataset.getCoordinateAxes();

	    ContinueDimension spatialEast = null;
	    ContinueDimension spatialNorth = null;
	    ContinueDimension time = null;

	    for (CoordinateAxis axe : axes) {
		ContinueDimension dimension = new ContinueDimension(NetCDFUtils.getAxisName(axe));
		double min = axe.getMinValue();
		double max = axe.getMaxValue();
		long size = axe.getSize();
		Double resolution = NetCDFUtils.readResolution(axe.read());
		Unit uom = Unit.fromIdentifier(NetCDFUtils.getAxisUnit(axe));
		Datum datum = null;

		AxisType type = axe.getAxisType();
		if (type == null) {
		} else {
		    switch (type) {
		    case GeoX:
		    case Lon:
			spatialEast = dimension;
			dimension.setType(DimensionType.ROW);
			break;
		    case GeoY:
		    case Lat:
			spatialNorth = dimension;
			dimension.setType(DimensionType.COLUMN);
			break;
		    case Time:
			CoordinateAxis1DTime timeAxis = CoordinateAxis1DTime.factory(ncDataset, axe, null);
			time = dimension;
			dimension.setType(DimensionType.TIME);
			Double resT = NetCDFUtils.readMinimumResolution(axe.read());
			// it means it has a regular time grid (although time units may not be
			// milliseconds), so we are
			// authorized to divide
			if (resT != null && size != 0) {
			    long milliSeconds = timeAxis.getCalendarDateRange().getDurationInSecs() * 1000;
			    resolution = (double) milliSeconds / (double) (size - 1);
			    min = timeAxis.getCalendarDateRange().getStart().getMillis();
			    max = timeAxis.getCalendarDateRange().getEnd().getMillis();
			    uom = Unit.MILLI_SECOND;
			    datum = Datum.UNIX_EPOCH_TIME();
			}
			break;
		    default:
			break;
		    }
		}
		dimension.setLower(min);
		dimension.setUpper(max);
		dimension.setSize(size);
		dimension.setResolution(resolution);
		dimension.setDatum(datum);
		dimension.setUom(uom);
	    }

	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.GRID);

	    descriptor.setCRS(crs);

	    descriptor.setDataFormat(DataFormat.NETCDF());

	    List<DataDimension> spatialDimensions = crs.getDefaultDimensions();

	    DataDimension dimension1 = spatialDimensions.get(0);

	    dimension1.getContinueDimension().setResolution(spatialNorth.getResolution().doubleValue());
	    dimension1.getContinueDimension().setSize(spatialNorth.getSize());
	    dimension1.getContinueDimension().setLower(spatialNorth.getLower());
	    dimension1.getContinueDimension().setUpper(spatialNorth.getUpper());

	    DataDimension dimension2 = spatialDimensions.get(1);

	    dimension2.getContinueDimension().setResolution(spatialEast.getResolution().doubleValue());
	    dimension2.getContinueDimension().setSize(spatialEast.getSize());
	    dimension2.getContinueDimension().setLower(spatialEast.getLower());
	    dimension2.getContinueDimension().setUpper(spatialEast.getUpper());

	    descriptor.setSpatialDimensions(spatialDimensions);

	    descriptor.setTemporalDimension(time);

	    ret.add(descriptor);
	    return ret;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	File file;
	try {
	    file = TRIGGERWafConnector.getLocalCopy(online.getLinkage(), online.getName());
	    return file;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return null;
    }

    @Override
    public boolean canConnect() throws GSException {
	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(online.getLinkage());
	if (stream.isPresent()) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return false;
    }

}
