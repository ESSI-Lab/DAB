package eu.essi_lab.downloader.hiscentral;

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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.DatatypeFactory;

import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurves;
import org.cuahsi.waterml._1.ObjectFactory;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.piemonte.HISCentralPiemonteClient;
import eu.essi_lab.accessor.hiscentral.piemonte.HISCentralPiemonteConnector;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public class HISCentralPiemonteScaleDeflussoDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_PIEMONTE_SCALE_DEFLUSSO_DOWNLOAD_ERROR = "HISCENTRAL_PIEMONTE_SCALE_DEFLUSSO_DOWNLOAD_ERROR";

    private HISCentralPiemonteConnector connector;
    private Downloader downloader;

    /**
     *
     */
    public HISCentralPiemonteScaleDeflussoDownloader() {

	connector = new HISCentralPiemonteConnector();
	downloader = new Downloader();
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.RATING_CURVE);
	descriptor.setDataFormat(DataFormat.WATERML_2_0());

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	Exception ex = null;

	try {
	    String linkage = online.getLinkage();
	    HISCentralPiemonteClient client = new HISCentralPiemonteClient(linkage);
	    RatingCurves ratingCurves = client.getRatingCurves("&format=json");
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
		    HISCENTRAL_PIEMONTE_SCALE_DEFLUSSO_DOWNLOAD_ERROR);
	}

	return null;
    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;
    }

    @Override
    public boolean canDownload() {

	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getLinkage().contains(HISCentralPiemonteConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_PIEMONTE_SCLAE_DEFLUSSO_NS_URI));
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }
}
