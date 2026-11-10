package eu.essi_lab.downloader.hiscentral;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.lombardia.opendata.LombardiaWFSClient;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ratings.RatingCurves;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

/**
 * Downloads the rating curves of a Lombardia river level sensor as WaterML 2.0 part 2, reading the piece-wise
 * formulas from the ARPA Lombardia WFS
 *
 * @author boldrini
 */
public class HISCentralLombardiaOpenDataScaleDeflussoDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_LOMBARDIA_SCALE_DEFLUSSO_DOWNLOAD_ERROR = "HISCENTRAL_LOMBARDIA_SCALE_DEFLUSSO_DOWNLOAD_ERROR";

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.RATING_CURVE);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setDataFormat(DataFormat.WATERML_2_0());

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	try {
	    RatingCurves ratingCurves = LombardiaWFSClient.toRatingCurves(LombardiaWFSClient.getFeatures(online.getLinkage()));

	    if (ratingCurves.getCurves().isEmpty()) {
		throw new IllegalStateException("No rating curve found at " + online.getLinkage());
	    }

	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	    Files.write(tmpFile.toPath(), ratingCurves.toWaterML2().getBytes());
	    return tmpFile;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_LOMBARDIA_SCALE_DEFLUSSO_DOWNLOAD_ERROR);
	}
    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;
    }

    @Override
    public boolean canDownload() {

	return online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_LOMBARDIA_OPEN_DATA_SCALE_DEFLUSSO_NS_URI);
    }

    @Override
    public boolean canConnect() throws GSException {

	return online.getLinkage() != null;
    }
}
