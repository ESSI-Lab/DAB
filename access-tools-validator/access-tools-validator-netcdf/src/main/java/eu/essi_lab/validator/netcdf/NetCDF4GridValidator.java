package eu.essi_lab.validator.netcdf;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.imageio.netcdf.GeoToolsNetCDFReader;
import org.geotools.imageio.netcdf.utilities.NetCDFCRSUtilities;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
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

public class NetCDF4GridValidator extends DataValidatorImpl {

	org.slf4j.Logger logger = GSLoggerFactory.getLogger(NetCDF4GridValidator.class);

	public static final String GRIDS_MISMATCH = "GRIDS mismatch";

	@Override
	public Provider getProvider() {
		return Provider.essiLabProvider();
	}

	public DataType getType() {
		return DataType.GRID;
	}

	@Override
	public DataFormat getFormat() {
		return DataFormat.NETCDF_4();
	}

	/**
	 * Reads data attributes from the data object
	 * 
	 * @param dataObject
	 * @return
	 */

	public DataDescriptor readDataAttributes(DataObject dataObject) {

		DataDescriptor ret = new DataDescriptor();

		CRS crs = null;

		// File tmpFile = null;
		NetcdfDataset dataset = null;
		// GridDataset gridDataset = null;

		try {
			// tmpFile = File.createTempFile("grid-validator", ".nc");
			// tmpFile.deleteOnExit();
			// FileOutputStream fos = new FileOutputStream(tmpFile);
			// IOUtils.copy(dataObject.getStream(), fos);
			// fos.close();

			dataset = NetcdfDataset.openDataset(dataObject.getFile().getAbsolutePath());

			String fti = dataset.getFileTypeId();
			if (fti != null) {
				switch (fti) {
				case "NetCDF-4":
					ret.setDataFormat(DataFormat.NETCDF_4());
					break;
				case "NetCDF-3":
				default:
					ret.setDataFormat(DataFormat.NETCDF_3());
					break;
				}
			}

			List<Variable> mainVariables = NetCDFUtils.getGeographicVariables(dataset);
			if (mainVariables.isEmpty() || // no georeferenced variables
					mainVariables.size() > 1 // multiple georeferenced variables, leading to ambiguity: which variable
			// should be
			// validated?
			// TODO: add in the data descriptor the name parameter, identifying a particular
			// variable / sub dataset
			) {
				throw new IllegalArgumentException(GRIDS_MISMATCH);
			} else {
				ret.setDataType(DataType.GRID);
			}

			CoordinateReferenceSystem decodedCRS = GeoToolsNetCDFReader.extractCRS(dataset, mainVariables.get(0));

			if (decodedCRS != null) {
				// this workaround is because by default GeoTools NetcdfUtils uses OGC CRS 84
				// for NetCDF latitude
				// longitude crs
				if (decodedCRS.equals(NetCDFCRSUtilities.WGS84)) {
					decodedCRS = org.geotools.referencing.CRS.decode("EPSG:4326");
				}

				crs = CRS.fromGeoToolsCRS(decodedCRS);

				ret.setCRS(crs);
			}

			List<CoordinateAxis> axes = dataset.getCoordinateAxes();

			ContinueDimension spatialEast = null;
			ContinueDimension spatialNorth = null;

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
					ret.getOtherDimensions().add(dimension);
				} else {
					switch (type) {
					case GeoX:
					case Lon:
						spatialEast = dimension;
						dimension.setType(DimensionType.ROW);
						datum = crs.getDatum();
						break;
					case GeoY:
					case Lat:
						spatialNorth = dimension;
						dimension.setType(DimensionType.COLUMN);
						datum = crs.getDatum();
						break;
					case GeoZ:
						dimension.setType(DimensionType.VERTICAL);
						datum = Datum.SEA_LEVEL_DATUM_1929();
						break;
					case Time:
						CoordinateAxis1DTime timeAxis = CoordinateAxis1DTime.factory(dataset, axe, null);
						dimension.setType(DimensionType.TIME);
						ret.setTemporalDimension(dimension);
						Double resT = NetCDFUtils.readResolution(axe.read());
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
						ret.getOtherDimensions().add(dimension);
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
			List<DataDimension> spatialDimensions = new ArrayList<>();
			if (crs != null && crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
				spatialDimensions.add(spatialNorth);
				spatialDimensions.add(spatialEast);
			} else {
				spatialDimensions.add(spatialEast);
				spatialDimensions.add(spatialNorth);
			}
			ret.setSpatialDimensions(spatialDimensions);
			if (dataObject.getDataDescriptor() != null) {
				DataDimension temporal = dataObject.getDataDescriptor().getTemporalDimension();
				if (temporal != null)
					ret.setTemporalDimension(temporal);
			}

		} catch (Exception e) {

			throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());

		} finally {
			try {
				// if (gridDataset != null) {
				// gridDataset.close();
				// }
				if (dataset != null) {
					dataset.close();
				}
			} catch (IOException e) {
			}
			// if (tmpFile != null) {
			// boolean del = tmpFile.delete();
			// GSLoggerFactory.getLogger(getClass()).trace("Deletion of file {} with result
			// {}",
			// tmpFile.getAbsolutePath(), del);
			// }
		}

		return ret;
	}

}
