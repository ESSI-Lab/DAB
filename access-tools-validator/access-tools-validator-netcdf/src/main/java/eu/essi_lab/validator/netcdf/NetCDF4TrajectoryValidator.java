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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.geotools.feature.FeatureCollection;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import ucar.ma2.IndexIterator;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft.DsgFeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureCollectionIterator;
import ucar.nc2.ft.point.PointDatasetImpl;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft.point.standard.StandardTrajectoryCollectionImpl;

public class NetCDF4TrajectoryValidator extends DataValidatorImpl {

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    @Override
    public DataFormat getFormat() {
	return DataFormat.NETCDF_4();
    }

    @Override
    public DataType getType() {
	return DataType.TRAJECTORY;
    }

    @Override
    public ValidationMessage checkSupportForDescriptor(DataDescriptor expected) {
	ValidationMessage ret = super.checkSupportForDescriptor(expected);
	if (ret.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
	    return ret;
	}
	if (expected.getCRS() != null && !expected.getCRS().equals(CRS.EPSG_4326())) {
	    return unsupportedDescriptor("Only EPSG:4326 CRS is supported by this validator");
	}

	ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    @Override
    public DataDescriptor readDataAttributes(DataObject dataObject) {

	DataDescriptor ret = new DataDescriptor();

	File tmpFile = dataObject.getFile();
	// File tmpFile2 = null;
	FeatureDataset dataset = null;
	try {
	    // tmpFile = File.createTempFile("time-series-validator", ".nc");
	    // tmpFile.deleteOnExit();
	    // tmpFile2 = File.createTempFile("time-series-validator-2", ".nc");
	    // tmpFile2.deleteOnExit();
	    // FileOutputStream fos = new FileOutputStream(tmpFile);
	    // IOUtils.copy(dataObject.getStream(), fos);
	    // fos.close();
	    // FileOutputStream fos2 = new FileOutputStream(tmpFile2);
	    // FileInputStream fis = new FileInputStream(tmpFile);
	    // IOUtils.copy(fis, fos2);
	    // fos2.close();
	    // fis.close();

	    dataset = FeatureDatasetFactoryManager.open(FeatureType.TRAJECTORY, tmpFile.getAbsolutePath(), null, null);

	    String fti = dataset.getNetcdfFile().getFileTypeId();
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

	    PointDatasetImpl fdp = (PointDatasetImpl) dataset;

	    List<DsgFeatureCollection> collections = fdp.getPointFeatureCollectionList();

	    if (collections.get(0) instanceof StandardTrajectoryCollectionImpl) {
		StandardTrajectoryCollectionImpl collection = (StandardTrajectoryCollectionImpl) collections.get(0);
		ret.setDataType(DataType.TRAJECTORY);

		PointFeatureCollectionIterator iterator = collection.getPointFeatureCollectionIterator();
		
		while(iterator.hasNext()) {
		    PointFeatureCollection pfc = iterator.next();
		    Double verticalBegin = null;
		    Double verticalEnd = null;
		    Double s = null;
		    Double w = null;
		    Double n = null;
		    Double e = null;
		    Long timeBegin = null;
		    Long timeEnd = null;

		    while (pfc.hasNext()) {
			PointFeature pf = pfc.next();
			StationPointFeature spf = (StationPointFeature) pf;
			double alt = spf.getLocation().getAltitude();
			if (verticalBegin == null || alt < verticalBegin) {
			    verticalBegin = alt;
			}
			if (verticalEnd == null || alt > verticalEnd) {
			    verticalEnd = alt;
			}
			double lon = spf.getLocation().getLongitude();
			double lat = spf.getLocation().getLatitude();

			if (n == null || lat > n) {
			    n = lat;
			}
			if (e == null || lon > e) {
			    e = lon;
			}
			if (s == null || lat < s) {
			    s = lat;
			}
			if (w == null || lon < w) {
			    w = lon;
			}
			long time = spf.getNominalTimeAsCalendarDate().getMillis();
			if (timeBegin == null || time < timeBegin) {
			    timeBegin = time;
			}
			if (timeEnd == null || time > timeEnd) {
			    timeEnd = time;
			}
		    }

		    if (s != null && n != null && w != null && e != null) {
			ret.setCRS(CRS.EPSG_4326());
			ret.setEPSG4326SpatialDimensions(n, e, s, w);
			if (Math.abs(n - s) < TOL) {
			    ret.getFirstSpatialDimension().getContinueDimension().setSize(1l);
			}
			if (Math.abs(e - w) < TOL) {
			    ret.getSecondSpatialDimension().getContinueDimension().setSize(1l);
			}
		    }

		    if (verticalBegin != null && verticalEnd != null) {
			ret.setVerticalDimension(verticalBegin, verticalEnd);
			if (Math.abs(verticalEnd - verticalBegin) < TOL) {
			    ret.getOtherDimension(DimensionType.VERTICAL).getContinueDimension().setSize(1l);
			}
		    }
		    if (timeBegin != null && timeEnd != null) {
			ret.setTemporalDimension(new Date(timeBegin), new Date(timeEnd));
			augmentTemporalDimension(tmpFile, ret.getTemporalDimension());
		    }
		}
		
		
	    }

	   

	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	} finally {
	    if (dataset != null) {
		try {
		    dataset.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	    // if (tmpFile != null) {
	    // boolean del = tmpFile.delete();
	    // GSLoggerFactory.getLogger(getClass()).trace("Deletion of file {} with result {}",
	    // tmpFile.getAbsolutePath(), del);
	    // }
	    // if (tmpFile2 != null) {
	    // boolean del2 = tmpFile2.delete();
	    // GSLoggerFactory.getLogger(getClass()).trace("Deletion of file {} with result {}",
	    // tmpFile2.getAbsolutePath(), del2);
	    // }
	}

	return ret;

    }

    private void augmentTemporalDimension(File tmpFile2, DataDimension temporalDimension) {
	NetcdfDataset dataset = null;
	try {
	    dataset = NetcdfDataset.openDataset(tmpFile2.getAbsolutePath());

	    List<CoordinateAxis> axes = dataset.getCoordinateAxes();
	    for (CoordinateAxis axe : axes) {
		AxisType type = axe.getAxisType();
		if (type != null) {
		    switch (type) {
		    case Time:
			long size = axe.getSize();
			temporalDimension.getContinueDimension().setSize(size);
			Double resolution = NetCDFUtils.readResolution(axe.read());
			if (resolution != null && Math.abs(resolution) > TOL) {
			    temporalDimension.getContinueDimension().setResolution(resolution);
			} else {
			    Double extent = NetCDFUtils.readExtent(axe.read());
			    if (size > 1) {
				resolution = extent / (size - 1);

				IndexIterator index = axe.read().getIndexIterator();
				Double maxResolution = null;
				Double tmp = null;
				while (index.hasNext()) {
				    double d = index.getDoubleNext();
				    if (tmp == null) {
					tmp = d;
				    } else {
					Double tmpResolution = d - tmp;
					if (maxResolution == null) {
					    maxResolution = tmpResolution;
					}
					if (tmpResolution > maxResolution) {
					    maxResolution = tmpResolution;
					}
					tmp = d;
				    }

				}

				temporalDimension.getContinueDimension().setResolution(resolution);
				temporalDimension.getContinueDimension().setResolutionTolerance(maxResolution);
			    }

			}

			break;
		    default:
			break;
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (dataset != null) {
		try {
		    dataset.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

}
