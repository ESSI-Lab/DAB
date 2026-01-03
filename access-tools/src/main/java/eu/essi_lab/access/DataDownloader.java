package eu.essi_lab.access;

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
import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;

/**
 * {@link DataDownloader} works with a specified online resource in order to:
 * <ul>
 * <li>provide available remote service data descriptors</li>
 * <li>perform data download of the selected one</li>
 * </ul>
 *
 * 
 * @author boldrini
 */
public abstract class DataDownloader implements Pluggable {

    private static final String ONLINE_RESOURCE_NOT_FOUND = "ONLINE_RESOURCE_NOT_FOUND";

    protected GSResource resource;
    protected Online online;

    public DataDownloader() {
	// no argument-constructor needed by service loader
    }

    /**
     * Sets the online resource that this downloader should work with
     * 
     * @param resource {@link GSResource} containing the online resource that this downloader should work with.
     * @param onlineId the online resource identifier
     * @throws GSException
     */
    public void setOnlineResource(GSResource resource, String onlineId) throws GSException {

	this.resource = resource;
	this.online = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnline(onlineId);

	if (online == null) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to find online resource with id: " + onlineId, //
		    "Data download failed", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ONLINE_RESOURCE_NOT_FOUND);
	}
    }

    /**
     * @return
     */
    public Online getOnline() {

	return online;
    }

    /**
     * Returns true if the present connector can download from the previously set online resource
     * 
     * @return
     */
    public abstract boolean canDownload();

    /**
     * Returns a list of descriptors describing the remote data offering
     * 
     * @return
     * @throws GSException
     */
    public abstract List<DataDescriptor> getRemoteDescriptors() throws GSException;

    /**
     * Returns a the same list of descriptors of {@link #getRemoteDescriptors()} but with the spatial/temporal
     * dimensions
     * reduced
     * 
     * @return
     * @throws GSException
     */
    public List<DataDescriptor> getPreviewRemoteDescriptors() throws GSException {

	List<DataDescriptor> descriptors = getRemoteDescriptors();

	for (DataDescriptor dataDescriptor : descriptors) {
	    reduceDescriptor(dataDescriptor);
	}

	return descriptors;
    }

    /**
     * Returns a the same list of descriptors of {@link #getRemoteDescriptors()} but with the spatial/temporal
     * dimensions
     * reduced
     * 
     * @return
     * @throws GSException
     */
    public List<DataDescriptor> getPreviewRemoteDescriptors(List<DataDescriptor> descriptors) throws GSException {

	ArrayList<DataDescriptor> ret = new ArrayList<>();

	for (DataDescriptor descriptor : descriptors) {
	    DataDescriptor cloneDescriptor = descriptor.clone();
	    reduceDescriptor(cloneDescriptor);
	    ret.add(cloneDescriptor);
	}

	return ret;
    }

    /**
     * Downloads remote data corresponding to the given descriptor (originating from a full descriptor considering
     * additional parameters such as subset).
     * 
     * @param descriptor
     * @return
     * @throws GSException if the data cannot be downloaded
     */
    public abstract File download(DataDescriptor descriptor) throws GSException;

    /**
     * Returns true if this downloader is able to subset along this dimension
     * 
     * @param dimensionName This string may be use for further computation in overriding classes
     * @return
     */
    public boolean canSubset(String dimensionName) {
	return false;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    protected void reduceDescriptor(DataDescriptor desc) {

	List<DataDimension> spatialDimensions = desc.getSpatialDimensions();
	for (DataDimension dataDimension : spatialDimensions) {
	    if (canSubset(dataDimension.getName())) {
		reduceDimension(dataDimension);
	    }
	}

	DataDimension temporalDimension = desc.getTemporalDimension();
	if (temporalDimension != null && canSubset(temporalDimension.getName())) {
	    reduceDimension(temporalDimension);
	}

	List<DataDimension> otherDimensions = desc.getOtherDimensions();
	for (DataDimension dataDimension : otherDimensions) {
	    if (canSubset(dataDimension.getName())) {
		reduceDimension(dataDimension);
	    }
	}

    }

    protected void reduceDimension(DataDimension dataDimension) {
	if (dataDimension != null) {
	    if (dataDimension instanceof ContinueDimension) {
		ContinueDimension continueDimension = dataDimension.getContinueDimension();
		Number resolution = continueDimension.getResolution();
		Number size = continueDimension.getSize();
		Number upper = continueDimension.getUpper();
		Number lower = continueDimension.getLower();
		boolean isLowerTolerance = continueDimension.isLowerApproximate();
		boolean isUpperTolerance = continueDimension.isUpperApproximate();
		boolean isResolutionTolerance = continueDimension.isResolutionApproximate();

		if (resolution != null && !isLowerTolerance && !isUpperTolerance && !isResolutionTolerance) {

		    if (resolution instanceof Long && lower instanceof Long && upper instanceof Long) {
			long newSize = 5;
			Number newUpper = (Long) lower + (Long) resolution * newSize;
			// if there aren't 5 points, let's proceed with 2 points
			if (isGreater(newUpper, upper)) {
			    newSize = 2;
			    newUpper = (long) (//
			    lower.doubleValue() + resolution.doubleValue() * newSize);
			}
			if (!isGreater(newUpper, upper)) {
			    continueDimension.setSize(newSize + 1);
			    continueDimension.setUpper(newUpper);
			}
		    } else {
			long newSize = 5l;
			long newUpper = (long) (lower.doubleValue() + resolution.doubleValue() * newSize);
			// if there aren't 5 points, let's proceed with 2 points
			if (newUpper > upper.doubleValue()) {
			    newSize = 2l;
			    newUpper = (long) (lower.doubleValue() + resolution.doubleValue() * newSize);
			}
			if (newUpper < upper.doubleValue()) {
			    continueDimension.setSize(newSize + 1);
			    continueDimension.setUpper(newUpper);
			}
		    }
		    return;
		}

		Long expectedSize = size != null ? size.longValue() : null;
		if (resolution != null) {
		    double extent = upper.doubleValue() - lower.doubleValue();
		    expectedSize = (long) ((extent / resolution.doubleValue()) + 1);
		}

		//
		// if no resolution is provided the dimension is arbitrarily spaced!
		// if there is a tolerance on lower or upper, no specific values can be given as well.
		// unable to appropriately reduce. e.g. most important the upper limit is not known,
		// so by reducing e.g. to 1/10 it could happen that no data is selected on the upper limit
		// only two options remain:
		// 1) to put lower limit equals to upper limit
		// 2) to have it untouched
		//
		// for performance reasons 1) has been selected as the preferred option. In other terms
		// a reduced dimension is a dimension with a single point if no resolution information is available.
		// Otherwise is a dimension reduced to the first 5 elements.
		// Note, if a tolerance is present on upper and lower limits picking one value will not work, so only in
		// this case an arbitrary reduction will be made
		//
		//

		if (isLowerTolerance && isUpperTolerance) {

		    Long factor = 20l;

		    if (expectedSize != null) {
			// a factor giving approximatively 20 values will be chosen
			Double extent = upper.doubleValue() - lower.doubleValue();
			double expectedResolution = extent / (expectedSize - 1);
			double reducedExtent = expectedResolution * 20;
			factor = (long) (extent / reducedExtent);
			if (factor < 1) {
			    factor = 1l;
			}
		    }

		    if (lower instanceof Long && upper instanceof Long) {
			Long lowerLong = (Long) lower;
			Long upperLong = (Long) upper;
			Long extent = upperLong - lowerLong;
			long reducedExtent = extent / factor;
			continueDimension.setLower(lowerLong);
			continueDimension.setUpper(lowerLong + reducedExtent);
			continueDimension.setSize(null);
		    } else {
			Double extent = upper.doubleValue() - lower.doubleValue();
			Double reducedExtent = extent / factor;
			continueDimension.setLower(lower.doubleValue());
			continueDimension.setUpper(lower.doubleValue() + reducedExtent);
			continueDimension.setSize(null);
		    }

		} else if (isLowerTolerance) {
		    continueDimension.setLower(upper);
		    continueDimension.setSize(1l);
		} else if (isUpperTolerance) {
		    continueDimension.setUpper(lower);
		    continueDimension.setSize(1l);
		} else {
		    continueDimension.setSize(1l);
		    continueDimension.setUpper(lower);
		}
		if (continueDimension.getSize() != null && continueDimension.getSize() == 1) {
		    continueDimension.setResolution(null);
		}
	    } else {
		FiniteDimension discreteDimension = dataDimension.getFiniteDimension();
		if (discreteDimension.getPoints().size() > 1) {
		    List<String> sublist = discreteDimension.getPoints().subList(0, 1);
		    discreteDimension.setPoints(sublist);
		} else {
		    List<String> sublist = discreteDimension.getPoints().subList(0, 0);
		    discreteDimension.setPoints(sublist);
		}
	    }
	}

    }

    private boolean isGreater(Number n1, Number n2) {
	if (n1 instanceof Long && n2 instanceof Long) {
	    return n1.longValue() > n2.longValue();
	} else {
	    return n1.doubleValue() > n2.doubleValue();
	}
    }

    /**
     * Checks if the remote access service is reachable (e.g. online)
     * 
     * @return
     */
    public abstract boolean canConnect() throws GSException;
}
