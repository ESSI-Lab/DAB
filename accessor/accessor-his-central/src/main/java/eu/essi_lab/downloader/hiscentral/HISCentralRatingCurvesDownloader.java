package eu.essi_lab.downloader.hiscentral;

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

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.deflusso.HISCentralRatingCurvesMapper;
import eu.essi_lab.accessor.hiscentral.deflusso.RatingCurvesClient;
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
 * Downloads a station's rating curve (scala di deflusso) as WaterML 2.0, reading it from the shared SharePoint
 * folder through {@link RatingCurvesClient}.
 *
 * @author boldrini
 */
public class HISCentralRatingCurvesDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_RATING_CURVES_DOWNLOAD_ERROR = "HISCENTRAL_RATING_CURVES_DOWNLOAD_ERROR";

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

	Exception ex = null;

	try {
	    String endpoint = online.getLinkage();
	    String stationId = online.getName();
	    String sourceId = HISCentralRatingCurvesMapper.readSourceId(resource.getOriginalMetadata());

	    RatingCurvesClient client = new RatingCurvesClient(endpoint);
	    RatingCurves ratingCurves = client.getRatingCurves(sourceId, stationId);

	    if (ratingCurves == null || ratingCurves.getCurves().isEmpty()) {
		throw new IllegalStateException(
			"No rating curve found for source=" + sourceId + " station=" + stationId);
	    }

	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	    Files.write(tmpFile.toPath(), ratingCurves.toWaterML2().getBytes());
	    return tmpFile;

	} catch (Exception e) {

	    ex = e;
	}

	if (ex != null) {

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_RATING_CURVES_DOWNLOAD_ERROR);
	}

	return null;
    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;
    }

    @Override
    public boolean canDownload() {

	boolean ret = (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI) && //
		online.getLinkage() != null);

	if (ret) {
	    GSLoggerFactory.getLogger(getClass()).info("Rating curves downloader found");
	}

	return ret;
    }

    @Override
    public boolean canConnect() throws GSException {

	return online.getLinkage() != null;
    }
}
