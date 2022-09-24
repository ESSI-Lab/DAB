package eu.essi_lab.validator.geotiff;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class GeoTIFFValidator extends DataValidatorImpl {

    org.slf4j.Logger logger = GSLoggerFactory.getLogger(GeoTIFFValidator.class);

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    public DataType getType() {
	return DataType.GRID;
    }

    @Override
    public DataFormat getFormat() {
	return DataFormat.IMAGE_GEOTIFF();
    }

    @Override
    public ValidationMessage checkSupportForDescriptor(DataDescriptor expected) {
	ValidationMessage ret = super.checkSupportForDescriptor(expected);
	if (ret.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
	    return ret;
	}

	if (expected.getTemporalDimension() != null) {
	    return unsupportedDescriptor("This validator doesn't support time dimension");
	}

	if (!expected.getOtherDimensions().isEmpty()) {
	    return unsupportedDescriptor("This validator doesn't support additional dimensions");
	}

	ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    /**
     * Reads data attributes from the data object
     * 
     * @param dataObject
     * @return
     */
    public DataDescriptor readDataAttributes(DataObject dataObject) {

	DataDescriptor ret = new DataDescriptor();
	ret.setDataType(DataType.GRID);
	ret.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	// READ DATA PHASE

	File tmpFile = null;

	GeoTiffReader reader = null;
	GridCoverage2D coverage = null;

	try {
	    tmpFile = dataObject.getFile(); // File.createTempFile("geotiff-validator", ".tif");
	    // tmpFile.deleteOnExit();
	    // FileOutputStream fos = new FileOutputStream(tmpFile);
	    // IOUtils.copy(dataObject.getStream(), fos);
	    // fos.close();

	    reader = new GeoTiffReader(tmpFile.toURI().toURL(), new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.FALSE));

	    CoordinateReferenceSystem decodedCRS = reader.getCoordinateReferenceSystem();

	    CRS crs = CRS.fromGeoToolsCRS(decodedCRS);
	    ret.setCRS(crs);

	    // GridEnvelope gridRange = reader.getOriginalGridRange();

	    // GridCoverage2D gc = reader.read(null);
	    // if (gc == null) {
	    // throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	    // }

	    GridEnvelope dimensions = reader.getOriginalGridRange();
	    GridCoordinates maxDimensions = dimensions.getHigh();
	    int width = maxDimensions.getCoordinateValue(0) + 1;
	    int height = maxDimensions.getCoordinateValue(1) + 1;
	    int numBands = reader.getGridCoverageCount();

	    coverage = reader.read(null);
	    GridGeometry2D geometry = coverage.getGridGeometry();

	    org.geotools.geometry.Envelope2D lowerCornerPixelEnvelope = geometry.gridToWorld(new GridEnvelope2D(0, 0, 1, 1));

	    double min1 = lowerCornerPixelEnvelope.getCenterX();
	    double min2 = lowerCornerPixelEnvelope.getCenterY();
	    double res1 = lowerCornerPixelEnvelope.getWidth();

	    org.geotools.geometry.Envelope2D upperCornerPixelEnvelope = geometry
		    .gridToWorld(new GridEnvelope2D(width - 1, height - 1, 1, 1));

	    double max1 = upperCornerPixelEnvelope.getCenterX();
	    double max2 = upperCornerPixelEnvelope.getCenterY();
	    double res2 = lowerCornerPixelEnvelope.getHeight();

	    if (min1 > max1) {
		// min and max could be inverted
		Double tmp = min1;
		min1 = max1;
		max1 = tmp;
	    }
	    if (min2 > max2) {
		// min and max could be inverted
		Double tmp = min2;
		min2 = max2;
		max2 = tmp;
	    }

	    long size1 = width;
	    long size2 = height;

	    if (crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		// invert resolutions
		Double tmp = res1;
		res1 = res2;
		res2 = tmp;
		// invert sizes
		Long tmpSize = size1;
		size1 = size2;
		size2 = tmpSize;
		// invert corners
		tmp = min1;
		min1 = min2;
		min2 = tmp;
		tmp = max1;
		max1 = max2;
		max2 = tmp;

	    }

	    List<DataDimension> spatialDimensions = new ArrayList<>();
	    ContinueDimension dimension1 = new ContinueDimension(crs.getFirstAxisName());
	    dimension1.setLower(min1);
	    dimension1.setUpper(max1);
	    dimension1.setResolution(res1);
	    dimension1.setSize(size1);
	    dimension1.setUom(crs.getUOM());
	    dimension1.setDatum(crs.getDatum());
	    spatialDimensions.add(dimension1);

	    ContinueDimension dimension2 = new ContinueDimension(crs.getSecondAxisName());
	    dimension2.setLower(min2);
	    dimension2.setUpper(max2);
	    dimension2.setResolution(res2);
	    dimension2.setSize(size2);
	    dimension2.setUom(crs.getUOM());
	    dimension2.setDatum(crs.getDatum());
	    spatialDimensions.add(dimension2);

	    ret.setSpatialDimensions(spatialDimensions);

	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());

	} finally {
	    if (coverage != null) {
		coverage.dispose(true);
		PlanarImage planarImage = (PlanarImage) coverage.getRenderedImage();
		if (planarImage != null) {
		    ImageUtilities.disposePlanarImageChain(planarImage);
		}
	    }
	    if (reader != null) {
		reader.dispose();
	    }
	}

	return ret;
    }

}
