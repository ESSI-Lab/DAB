package eu.essi_lab.validator.png;

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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.DataValidatorImpl;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class PNGValidator extends DataValidatorImpl {

    org.slf4j.Logger logger = GSLoggerFactory.getLogger(PNGValidator.class);

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    public DataType getType() {
	return DataType.GRID;
    }

    @Override
    public DataFormat getFormat() {
	return DataFormat.IMAGE_PNG();
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
	ret.setDataFormat(DataFormat.IMAGE_PNG());

	// READ DATA PHASE

	File tmpFile = null;

	try {
	    tmpFile = dataObject.getFile(); // File.createTempFile("geotiff-validator", ".tif");
	    // tmpFile.deleteOnExit();
	    // FileOutputStream fos = new FileOutputStream(tmpFile);
	    // IOUtils.copy(dataObject.getStream(), fos);
	    // fos.close();

	    FileInputStream stream = new FileInputStream(tmpFile);
	    ImageInputStream iis = ImageIO.createImageInputStream(stream);
	    Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
	    DataFormat format = null;
	    while (readers.hasNext()) {
		ImageReader reader = (ImageReader) readers.next();
		String formatName = reader.getFormatName();
		format = DataFormat.fromIdentifier(formatName);
		break;
	    }

	    stream.close();
	    iis.close();

	    if (format == null || !format.equals(DataFormat.IMAGE_PNG())) {
		
		throw new IllegalArgumentException("Unable to read file format or format not PNG");
	    }

	    BufferedImage bimg = ImageIO.read(tmpFile);
	    int width = bimg.getWidth();
	    int height = bimg.getHeight();

	    DataDescriptor inputDescriptor = dataObject.getDataDescriptor();
	    CRS crs = inputDescriptor.getCRS();

	    if (crs != null) {
		
		ret.setCRS(crs);
		
		DataDimension dim1 = inputDescriptor.getFirstSpatialDimension();
		Long size1 = dim1.getContinueDimension().getSize();
		DataDimension dim2 = inputDescriptor.getSecondSpatialDimension();
		Long size2 = dim2.getContinueDimension().getSize();
		
		if(dim1 != null && dim2 != null) {
		    List<DataDimension> spatialDimensionsList = new ArrayList<DataDimension>();
		    spatialDimensionsList.add(dim1);
		    spatialDimensionsList.add(dim2);
		    ret.setSpatialDimensions(spatialDimensionsList);
		}
		

		Long inputWidth = null;
		Long inputHeight = null;
		switch (crs.getAxisOrder()) {
		case NORTH_EAST:
		    inputHeight = size1;
		    inputWidth = size2;
		    break;
		case EAST_NORTH:
		default:
		    inputHeight = size2;
		    inputWidth = size1;
		    break;
		}
		if (inputWidth == width && inputHeight == height) {
		    return inputDescriptor;
		}
	    }

	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());

	} finally {
	    // if (tmpFile != null) {
	    // boolean del = tmpFile.delete();
	    // GSLoggerFactory.getLogger(getClass()).trace("Deletion of file {} with result {}",
	    // tmpFile.getAbsolutePath(), del);
	    // }
	}

	return ret;
    }

}
