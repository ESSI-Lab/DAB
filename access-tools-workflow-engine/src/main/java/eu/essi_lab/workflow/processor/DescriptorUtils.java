package eu.essi_lab.workflow.processor;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.workflow.processor.CapabilityElement.PresenceType;
public class DescriptorUtils {

    /**
     * Creates a new {@link DataDescriptor} from the supplied <code>dataDescriptor</code>
     * by setting {@link DataType}, {@link CRS} and {@link DataFormat} equals to <code>dataDescriptor</code>, than
     * creates all the required dimensions by comparing the <code>capabilities</code> {@link SubsettingCapability} and
     * {@link ResamplingCapability} with the <code>dataDescriptor</code> dimensions.<br>
     * So for example, if in <code>capabilities</code> a temporal subsetting is
     * required, than the <code>dataDescriptor</code> temporal dimension
     * is cloned and its <i>upper points</i> are reduced, thus <i>simulating</i> a real subsetting use case. The
     * value of the temporal
     * dimension is not relevant, but it is computed in order to be a valid value, according to the initial one.<br>
     * And if a temporal resampling is required, than the current temporal resolution is cloned and reduced,
     * thus <i>simulating</i> a real resampling use case.<br>
     * The cloned, subsetted (and/or resampled) dimension is than set to the returned {@link DataDescriptor}.<br>
     * This same strategy is applied to all the remaining dimensions (other and spatial).<br>
     * 
     * @param dataDescriptor the initial descriptor
     * @param capabilities the required capabilities
     */
    public static DataDescriptor simulateDescriptor(DataDescriptor dataDescriptor, ProcessorCapabilities capabilities) {

	DataDescriptor out = new DataDescriptor();
	out.setCRS(capabilities.getCRSCapability().getFirstValue());
	out.setDataFormat(capabilities.getDataFormatCapability().getFirstValue());
	out.setDataType(capabilities.getDataTypeCapability().getFirstValue());

	SubsettingCapability subsetting = capabilities.getSubsettingCapability();
	ResamplingCapability resampling = capabilities.getResamplingCapability();

	// ---------------------
	//
	// Temporal
	//
	{
	    BooleanCapabilityElement temporalSubsetting = subsetting.getTemporalSubsetting();
	    BooleanCapabilityElement temporalResampling = resampling.getTemporalResampling();

	    DataDimension dimension = getDimension(dataDescriptor.getTemporalDimension(), temporalSubsetting, temporalResampling);
	    out.setTemporalDimension(dimension);
	}

	// ---------------------
	//
	// Other
	//
	{
	    BooleanCapabilityElement otherSubsetting = subsetting.getOtherSubsetting();
	    BooleanCapabilityElement otherResampling = resampling.getOtherResampling();

	    List<DataDimension> dimensions = dataDescriptor.getOtherDimensions();
	    ArrayList<DataDimension> list = new ArrayList<>();

	    for (DataDimension dim : dimensions) {
		DataDimension outDim = getDimension(dim, otherSubsetting, otherResampling);
		list.add(outDim);
	    }
	    out.setOtherDimensions(list);
	}

	// ---------------------
	//
	// Spatial
	//
	{
	    BooleanCapabilityElement spatialSubsetting = subsetting.getSpatialSubsetting();
	    BooleanCapabilityElement spatialResampling = resampling.getSpatialResampling();

	    List<DataDimension> dimensions = dataDescriptor.getSpatialDimensions();
	    ArrayList<DataDimension> list = new ArrayList<>();

	    for (DataDimension dim : dimensions) {
		DataDimension outDim = getDimension(dim, spatialSubsetting, spatialResampling);
		list.add(outDim);
	    }
	    out.setSpatialDimensions(list);
	}

	return out;
    }

    /**
     * Creates a new {@link ProcessorCapabilities} setting {@link DataType}, {@link CRS} and {@link DataFormat} equals
     * to <code>descriptor</code>, than set {@link SubsettingCapability#NO_SUBSETTING()} and
     * {@link ResamplingCapability#NO_RESAMPLING()}
     * 
     * @param descriptor the current {@link DataObject} description
     */
    public static ProcessorCapabilities fromInputDescriptor(DataDescriptor descriptor) {

	return ProcessorCapabilities.create(//
		CapabilityElement.anyFromDataType(descriptor.getDataType()), //
		CapabilityElement.anyFromCRS(descriptor.getCRS()), //
		CapabilityElement.anyFromDataFormat(descriptor.getDataFormat()), //
		SubsettingCapability.NO_SUBSETTING(), //
		ResamplingCapability.NO_RESAMPLING());
    }

    /**
     * Creates a new {@link ProcessorCapabilities} setting the {@link CapabilityElement} of
     * {@link DataType}, {@link CRS} and {@link DataFormat} from the related values of
     * <code>targetDescriptor</code> and {@link PresenceType#ANY}, than compares the dimensions of
     * <code>descriptor</code> and <code>targetDescriptor</code> in order to properly set {@link SubsettingCapability}
     * and {@link ResamplingCapability}
     * 
     * @param descriptor the current {@link DataObject} description
     * @param targetDescriptor the target {@link DataObject} description
     */
    public static ProcessorCapabilities fromTargetDescriptor(DataDescriptor descriptor, DataDescriptor targetDescriptor) {

	ProcessorCapabilities out = new ProcessorCapabilities();
	out.setDataTypeCapability(CapabilityElement.anyFromDataType(targetDescriptor.getDataType()));
	out.setCRSCapability(CapabilityElement.anyFromCRS(targetDescriptor.getCRS()));
	out.setDataFormatCapability(CapabilityElement.anyFromDataFormat(targetDescriptor.getDataFormat()));

	DataDimension initTemporalDimension = descriptor.getTemporalDimension();
	List<DataDimension> initSpatialDimensions = descriptor.getSpatialDimensions();
	List<DataDimension> initOtherDimensions = descriptor.getOtherDimensions();

	DataDimension targetTemporalDimension = targetDescriptor.getTemporalDimension();
	List<DataDimension> targetSpatialDimensions = targetDescriptor.getSpatialDimensions();
	List<DataDimension> targetOtherDimensions = targetDescriptor.getOtherDimensions();

	// other (subsetting, resampling)
	SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement> otherElement = compareDimensions(//
		initOtherDimensions, //
		targetOtherDimensions);//

	// temporal (subsetting, resampling)
	SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement> temporalElement = compareDimension(//
		initTemporalDimension, //
		targetTemporalDimension);//

	// spatial (subsetting, resampling)
	SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement> spatialElement = compareDimensions(//
		initSpatialDimensions, //
		targetSpatialDimensions);//

	SubsettingCapability subsettingCapability = new SubsettingCapability(//
		spatialElement.getKey(), //
		temporalElement.getKey(), //
		otherElement.getKey());//

	ResamplingCapability resamplingCapability = new ResamplingCapability(//
		spatialElement.getValue(), //
		temporalElement.getValue(), //
		otherElement.getValue());//

	out.setSubsettingCapability(subsettingCapability);
	out.setResamplingCapability(resamplingCapability);

	return out;
    }

    private static SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement> compareDimensions(//
	    List<DataDimension> initDimensions, //
	    List<DataDimension> targetDimensions) { //

	ArrayList<SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement>> arrayList = new ArrayList<>();

	// List<DataDimension> sortedInitDimensions = initDimensions.stream().//
	// sorted((v1, v2) -> v1.getName().compareTo(v2.getName())).//
	// collect(Collectors.toList());
	//
	// List<DataDimension> sortedTargetDimensions = targetDimensions.stream().//
	// sorted((v1, v2) -> v1.getName().compareTo(v2.getName())).//
	// collect(Collectors.toList());

	for (int i = 0; i < initDimensions.size(); i++) {

	    SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement> element;

	    if (targetDimensions.isEmpty()) {
		element = //
			new SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement>(//
				new BooleanCapabilityElement(PresenceType.ANY), new BooleanCapabilityElement(PresenceType.ANY));//
	    } else {

		DataDimension dimension = initDimensions.get(i);
		DataDimension dimension2 = targetDimensions.get(i);

		element = compareDimension(dimension, dimension2);
	    }
	    arrayList.add(element);
	}

	boolean trueSubMatch = arrayList.stream().//
		anyMatch(e -> e.getKey().getValues().size() == 1 && e.getKey().getFirstValue() == true);

	boolean trueResMatch = arrayList.stream().//
		anyMatch(e -> e.getValue().getValues().size() == 1 && e.getValue().getFirstValue() == true);

	BooleanCapabilityElement outSubsetting = new BooleanCapabilityElement(PresenceType.ANY);
	if (trueSubMatch) {
	    outSubsetting = new BooleanCapabilityElement(true);
	}

	BooleanCapabilityElement outResampling = new BooleanCapabilityElement(PresenceType.ANY);
	if (trueResMatch) {
	    outResampling = new BooleanCapabilityElement(true);
	}

	return new SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement>(outSubsetting, outResampling);
    }

    private static SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement> compareDimension(//
	    DataDimension dim1, //
	    DataDimension dim2) {//

	BooleanCapabilityElement subTempElement = null;
	BooleanCapabilityElement resTempElement = null;

	if (dim1 == null && dim2 == null) {

	    subTempElement = new BooleanCapabilityElement(PresenceType.ANY);
	    resTempElement = new BooleanCapabilityElement(PresenceType.ANY);
	} else {

	    if (dim1 == null) {

		// is this correct? check
		subTempElement = new BooleanCapabilityElement(PresenceType.ANY);
		resTempElement = new BooleanCapabilityElement(PresenceType.ANY);

	    } else if (dim1 instanceof ContinueDimension) {

		ContinueDimension sizedInput = (ContinueDimension) dim1;
		ContinueDimension sizedOutput = (ContinueDimension) dim2;

		boolean upperChanged = !DataDimension.equals(sizedInput.getUpper(), sizedOutput.getUpper());
		boolean lowerChanged = !DataDimension.equals(sizedInput.getLower(), sizedOutput.getLower());

		if (upperChanged || lowerChanged) {

		    subTempElement = new BooleanCapabilityElement(true);

		} else {

		    subTempElement = new BooleanCapabilityElement(PresenceType.ANY);
		}

		if (sizedOutput.getResolution() != null && //

			(sizedInput.getResolution() == null || //
				!DataDimension.equals(sizedInput.getResolution(), sizedOutput.getResolution()))) {

		    resTempElement = new BooleanCapabilityElement(true);

		} else {

		    resTempElement = new BooleanCapabilityElement(PresenceType.ANY);

		}
	    } else if (dim1 instanceof FiniteDimension) {

		FiniteDimension sizedInput = dim1.getFiniteDimension();
		FiniteDimension sizedOutput = dim2.getFiniteDimension();

		boolean equals = Objects.equals(sizedInput.getPoints(), sizedOutput.getPoints());
		if (equals) {

		    subTempElement = new BooleanCapabilityElement(PresenceType.ANY);
		} else {
		    subTempElement = new BooleanCapabilityElement(true);
		}

		resTempElement = new BooleanCapabilityElement(PresenceType.ANY);
	    }
	}

	SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement> entry = //
		new SimpleEntry<BooleanCapabilityElement, BooleanCapabilityElement>(//
			subTempElement, resTempElement);//

	return entry;
    }

    /**
     * Given an input and subsetting and resampling indications, transform the input according and returns the new
     * dimension
     * 
     * @param dim
     * @param sub
     * @param res
     * @return
     */
    private static DataDimension getDimension(DataDimension dim, BooleanCapabilityElement sub, BooleanCapabilityElement res) {

	if (dim == null) {
	    return null;
	}

	DataDimension dataDimension = dim.clone();

	if (dim instanceof ContinueDimension) {

	    ContinueDimension cd = (ContinueDimension) dim;
	    ContinueDimension clone = cd.clone();
	    Number resolution = clone.getResolution();
	    Number lower = clone.getLower();
	    Number upper = clone.getUpper();

	    if (sub.getPresence() == PresenceType.ANY && sub.getFirstValue() == true) {

		// this must be refined
		clone.setSize(null);

		// commented, because we may know size, but not the resolution
		// (the dataset might be no regularly spaced)
		// if (resolution == null) {
		// Long size = cd.getSize();
		// if (size != null) {
		// resolution = getResolution(upper, lower, size);
		// }
		// }

		Number newUpperValue = null;
		if (resolution != null) {
		    newUpperValue = getNewUpper(lower, resolution, 10);
		    if (isGreater(newUpperValue, upper)) {
			newUpperValue = getNewUpper(lower, resolution, 2);
			if (isGreater(newUpperValue, upper)) {
			    newUpperValue = null;
			}
		    }
		}

		if (newUpperValue == null) {
		    // if no information about the resolution is available!
		    newUpperValue = getMedianPoint(lower, upper, clone.getSize());
		    // throw new RuntimeException("No resolution information available, not possible to continue");
		    clone.setUpperType(LimitType.CONTAINS);
		}
		clone.setUpper(newUpperValue);

		upper = newUpperValue;

	    }

	    if (res.getPresence() == PresenceType.ANY && res.getFirstValue() == true) {

		// this must be refined
		clone.setSize(null);

		if (resolution != null) {
		    if (resolution instanceof Long) {
			resolution = resolution.longValue() / 2;
		    } else {
			resolution = resolution.doubleValue() / 2.0;
		    }
		} else {
		    Long originalSize = cd.getSize();
		    double extent = cd.getUpper().doubleValue() - cd.getLower().doubleValue();
		    double approximateResolution = extent / (originalSize - 1);
		    resolution = approximateResolution / 2.0;
		}
		clone.setResolution(resolution);
	    }

	    if (resolution != null && !clone.isLowerApproximate() && !clone.isUpperApproximate()) {
		Long size = Math.round((upper.doubleValue() - lower.doubleValue()) / resolution.doubleValue() + 1);
		clone.setSize(size);
	    }

	    dataDimension = clone;

	} else if (dim instanceof FiniteDimension) {

	    FiniteDimension dd = (FiniteDimension) dim;
	    FiniteDimension clone = dd.clone();

	    if (sub.getPresence() == PresenceType.ANY && sub.getFirstValue() == true) {

		if (!clone.getPoints().isEmpty()) {

		    clone.getPoints().remove(0);
		}
	    }

	    dataDimension = clone;
	}

	return dataDimension;
    }

    private static Number getMedianPoint(Number l, Number u, Number size) {

	if (size != null) {
	    double extent = u.doubleValue() - l.doubleValue();
	    double expectedResolution = extent / (size.longValue() - 1);
	    double newExtent = expectedResolution * 20;
	    if (newExtent > extent) {
		newExtent = extent;
	    }
	    return l.doubleValue() + newExtent;
	}

	if (l instanceof Long && u instanceof Long) {
	    Long l1 = (Long) l;
	    Long l2 = (Long) u;
	    return l1 + (l2 - l1) / 2;
	}

	return l.doubleValue() + (u.doubleValue() - l.doubleValue()) / 2.0;
    }

    private static boolean isGreater(Number n1, Number n2) {
	if (n1 instanceof Long && n2 instanceof Long) {
	    Long l1 = (Long) n1;
	    Long l2 = (Long) n2;
	    return l1 > l2;
	}
	return n1.doubleValue() > n2.doubleValue();
    }

    private static Number getNewUpper(Number lower, Number resolution, int points) {
	if (lower instanceof Long && resolution instanceof Long) {
	    Long lowerLong = (Long) lower;
	    Long resolutionLong = (Long) resolution;
	    return lowerLong + resolutionLong * points;
	}
	return lower.doubleValue() + resolution.doubleValue() * points;

    }

    private static Number getResolution(Number upper, Number lower, Long size) {

	if (Math.abs(upper.doubleValue() - lower.doubleValue()) < 0.000000000000001) {
	    return null;
	}

	if (upper instanceof Long && lower instanceof Long) {
	    Long upLong = (Long) upper;
	    Long lowLong = (Long) lower;
	    return (upLong - lowLong) / (size - 1.0);
	}

	return (upper.doubleValue() - lower.doubleValue()) / (size - 1.0);

    }

}
