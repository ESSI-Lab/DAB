package eu.essi_lab.access;

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

import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CFProjection;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public abstract class DataValidatorImpl implements DataValidator {

    private static final String CRS_MISMATCH = "CRS mismatch";

    public static final String DATA_TYPE_MISMATCH = "Data Type mismatch";

    public static final String FORMAT_MISMATCH = "Format mismatch";

    public static final String GRIDS_MISMATCH = "GRIDS mismatch";

    private static final String CRS_UNSUPPORTED = "CRS_UNSUPPORTED";

    private static final String SPATIAL_DIMENSION_MISMATCH = "Spatial dimensions mismatch";

    private static final String TEMPORAL_DIMENSION_MISMATCH = "Temporal dimension mismatch";

    private static final String OTHER_DIMENSION_MISMATCH = "Other dimension mismatch";

    public static Double TOL = Math.pow(10, -8);

    org.slf4j.Logger logger = GSLoggerFactory.getLogger(DataValidatorImpl.class);

    @Override
    public ValidationMessage validate(DataObject dataObject) throws GSException {

	DataDescriptor descriptor = dataObject.getDataDescriptor();

	ValidationMessage ret = checkSupportForDescriptor(descriptor);

	if (ret.getResult().equals(ValidationResult.VALIDATION_FAILED)) {
	    return ret;
	}

	// the following will be filled in the READ DATA PHASE

	DataDescriptor actual = null;
	try {
	    actual = readDataAttributes(dataObject);
	} catch (IllegalArgumentException e) {
	    ret = new ValidationMessage();
	    ret.setErrorCode(e.getMessage());
	    ret.setError("Not a valid " + getFormat() + " " + getType() + " type");
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	    return ret;
	}

	return check(descriptor, actual);
    }
    public ValidationMessage checkSupportForDescriptor(DataDescriptor descriptor) {
	if (descriptor == null) {
	    return invalidDescriptor("Null descriptor");
	}
	if (descriptor.getDataFormat() == null) {
	    return invalidDescriptor("Null format descriptor");
	}
	if (descriptor.getDataType() == null) {
	    return unsupportedDescriptor("Null data type descriptor");
	}

	DataFormat formatToValidate = descriptor.getDataFormat();
	DataFormat validableFormat = getFormat();

	if (!formatToValidate.equals(validableFormat) && !validableFormat.isSubTypeOf(formatToValidate)) {

	    return unsupportedDescriptor("Unable to validate " + formatToValidate
		    + " using this validator. Supported format by this validator: " + validableFormat);
	}
	if (!descriptor.getDataType().equals(getType())) {
	    return unsupportedDescriptor(
		    "Not a " + getType() + " descriptor (was " + descriptor.getDataType() + ") - unable to validate using this validator");
	}

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;

    }

    public abstract DataDescriptor readDataAttributes(DataObject dataObject);

    protected ValidationMessage check(DataDescriptor expected, DataDescriptor actual) {

	// Finally, the read variables are checked, expected ones against actual ones.

	// FORMAT
	if (expected.getDataFormat() != null) {

	    if (actual.getDataFormat() == null) {
		return getValidationMismatch(FORMAT_MISMATCH);
	    }

	    if (!expected.getDataFormat().equals(actual.getDataFormat())//
		    && !actual.getDataFormat().isSubTypeOf(expected.getDataFormat())) {
		return getValidationMismatch(FORMAT_MISMATCH);
	    }

	}

	boolean switchDimensions = false;

	// CRS
	if (expected.getCRS() != null && expected.getSpatialDimensions() != null && !expected.getSpatialDimensions().isEmpty()) {

	    if (actual.getCRS() == null) {
		return getValidationMismatch(CRS_UNSUPPORTED);
	    }

	    if (!expected.getCRS().equals(actual.getCRS())) {

		logger.warn("Not equal CRS: checking if at least same projection");
		//
		CFProjection p1 = expected.getCRS().getProjection();
		CFProjection p2 = actual.getCRS().getProjection();
		if (p1 == null || p2 == null || !p1.equals(p2)) {
		    return getValidationMismatch(CRS_MISMATCH);
		}

		if (expected.getCRS().equals(CRS.EPSG_4326()) && actual.getCRS().equals(CRS.OGC_84()) || //
			expected.getCRS().equals(CRS.OGC_84()) && actual.getCRS().equals(CRS.EPSG_4326())) {
		    switchDimensions = true;
		}

	    }

	}

	// DATA TYPE

	if (expected.getDataType() != null) {

	    if (actual.getDataType() == null) {
		return getValidationMismatch(DATA_TYPE_MISMATCH);
	    }

	    if (!expected.getDataType().equals(actual.getDataType())) {
		return getValidationMismatch(DATA_TYPE_MISMATCH);
	    }

	}

	// SPATIAL DIMENSIONS
	DataDimension expectedSpatialDimension1 = null;
	expectedSpatialDimension1 = expected.getFirstSpatialDimension();
	if (switchDimensions) {
	    expectedSpatialDimension1 = expected.getSecondSpatialDimension();
	}
	if (expectedSpatialDimension1 != null) {
	    if (expectedSpatialDimension1.getContinueDimension() != null) {
		DataDimension dimension = actual.getFirstSpatialDimension();
		if (dimension == null || dimension.getContinueDimension() == null) {
		    Long expectedSize = expectedSpatialDimension1.getContinueDimension().getSize();
		    if (expectedSize != null && expectedSize == 0) {
			// o.k.
		    } else {
			return getValidationMismatch(SPATIAL_DIMENSION_MISMATCH);
		    }
		} else if (!checkDimensions(expectedSpatialDimension1.getContinueDimension(), dimension.getContinueDimension())) {
		    return getValidationMismatch(SPATIAL_DIMENSION_MISMATCH);
		}
	    } else {
		return unsupportedDescriptor("Only continue dimension checks are supported at this time");
	    }
	}
	DataDimension expectedSpatialDimension2 = null;
	expectedSpatialDimension2 = expected.getSecondSpatialDimension();
	if (switchDimensions) {
	    expectedSpatialDimension2 = expected.getFirstSpatialDimension();
	}
	if (expectedSpatialDimension2 != null) {
	    if (expectedSpatialDimension2.getContinueDimension() != null) {
		DataDimension dimension = actual.getSecondSpatialDimension();
		if (dimension == null || dimension.getContinueDimension() == null) {
		    Long expectedSize = expectedSpatialDimension2.getContinueDimension().getSize();
		    if (expectedSize != null && expectedSize == 0) {
			// o.k.
		    } else {
			return getValidationMismatch(SPATIAL_DIMENSION_MISMATCH);
		    }
		} else if (!checkDimensions(expectedSpatialDimension2.getContinueDimension(), dimension.getContinueDimension())) {
		    return getValidationMismatch(SPATIAL_DIMENSION_MISMATCH);
		}
	    } else {
		return unsupportedDescriptor("Only continue dimension checks are supported at this time");
	    }
	}
	// TEMPORAL DIMENSION
	DataDimension expectedTimeDimension = null;
	expectedTimeDimension = expected.getTemporalDimension();
	if (expectedTimeDimension != null) {
	    if (expectedTimeDimension.getContinueDimension() != null) {
		DataDimension dimension = actual.getTemporalDimension();
		if (dimension == null || dimension.getContinueDimension() == null) {
		    Long expectedSize = expectedTimeDimension.getContinueDimension().getSize();
		    if (expectedSize != null && expectedSize == 0) {
			// o.k.
		    } else {
			return getValidationMismatch(TEMPORAL_DIMENSION_MISMATCH);
		    }
		} else if (!checkDimensions(expectedTimeDimension.getContinueDimension(), dimension.getContinueDimension())) {
		    return getValidationMismatch(TEMPORAL_DIMENSION_MISMATCH);
		}

	    } else {
		return unsupportedDescriptor("Only continue dimension checks are supported at this time");
	    }
	}
	// OTHER DIMENSIONS
	List<String> expectedOtherDimensionNames = expected.getOtherDimensionNames();
	expectedOtherDimensionNames.removeAll(actual.getOtherDimensionNames());
	if (!expectedOtherDimensionNames.isEmpty()) {
	    // required other dimension not found
	    return getValidationMismatch(OTHER_DIMENSION_MISMATCH);
	}

	for (DataDimension expectedDimension : expected.getOtherDimensions()) {
	    if (expectedDimension.getContinueDimension() != null) {
		DataDimension dimension = actual.getOtherDimension(expectedDimension.getName());
		if (dimension == null) {
		    // if not found by name, we try to find by type
		    dimension = actual.getOtherDimension(expectedDimension.getType());
		}
		if (dimension == null || dimension.getContinueDimension() == null) {
		    Long expectedSize = expectedDimension.getContinueDimension().getSize();
		    if (expectedSize != null && expectedSize == 0) {
			// o.k.
		    } else {
			return getValidationMismatch(OTHER_DIMENSION_MISMATCH);
		    }
		} else if (!checkDimensions(expectedDimension.getContinueDimension(), dimension.getContinueDimension())) {
		    return getValidationMismatch(OTHER_DIMENSION_MISMATCH);
		}
	    } else {
		return unsupportedDescriptor("Only continue dimension checks are supported at this time");
	    }
	}

	// at least
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;

    }

    private boolean checkDimensions(ContinueDimension expected, ContinueDimension actual) {

	Number expectedMinNumber = expected.getLower();
	Number expectedMinTolerance = expected.getLowerTolerance();
	Double expectedMin = expectedMinNumber == null ? null : expectedMinNumber.doubleValue();
	Number expectedMaxNumber = expected.getUpper();
	Double expectedMax = expectedMaxNumber == null ? null : expectedMaxNumber.doubleValue();
	Number expectedMaxTolerance = expected.getUpperTolerance();
	Double expectedRes = expected.getResolution() == null ? null : expected.getResolution().doubleValue();
	Double expectedResTolerance = expected.getResolutionTolerance() == null ? 0 : expected.getResolutionTolerance().doubleValue();
	Long expectedSize = expected.getSize();

	// the required descriptor is inconsistent!
	Double limitTolerance = 0.;
	if (expectedMinTolerance != null) {
	    limitTolerance = expectedMinTolerance.doubleValue();
	}
	if (expectedMaxTolerance != null) {
	    limitTolerance = Math.max(expectedMaxTolerance.doubleValue(), limitTolerance);
	}
	// the axis consitency with respect to the resolution can be checked only if precise upper and lower limits
	// are given
	if (limitTolerance < TOL) {
	    checkAxisConsistency(expectedMinNumber, expectedMaxNumber, expectedSize, expectedRes, expectedResTolerance);
	}

	Number actualMinNumber = actual.getLower();
	Double actualMin = actualMinNumber == null ? null : actualMinNumber.doubleValue();
	Number actualMaxNumber = actual.getUpper();
	Double actualMax = actualMaxNumber == null ? null : actualMaxNumber.doubleValue();
	Double actualRes = actual.getResolution() == null ? null : actual.getResolution().doubleValue();
	Long actualSize = actual.getSize();
	Number actualResTolerance = actual.getResolutionTolerance() == null ? null : actual.getResolutionTolerance();

	// the descriptor read by the data validator is inconsistent!
	checkAxisConsistency(actualMinNumber, actualMaxNumber, actualSize, actualRes, actualResTolerance);

	Unit expectedUOM = expected.getUom();
	Unit actualUOM = actual.getUom();

	expectedMin = normalize(expectedUOM, expectedMin);
	expectedMax = normalize(expectedUOM, expectedMax);
	expectedRes = normalize(expectedUOM, expectedRes);

	actualMin = normalize(actualUOM, actualMin);
	actualMax = normalize(actualUOM, actualMax);
	actualRes = normalize(actualUOM, actualRes);

	if (expectedMax != null && expectedMin != null) {

	    double extent = Math.abs(expectedMax - expectedMin);

	    double defaultTolerance = extent / 10.;

	    double toleranceLowNegative;
	    double toleranceLowPositive;
	    switch (expected.getLowerType()) {
	    case ABSOLUTE:
	    default:
		toleranceLowNegative = expectedMinTolerance != null ? expectedMinTolerance.doubleValue() : defaultTolerance;
		toleranceLowPositive = expectedMinTolerance != null ? expectedMinTolerance.doubleValue() : defaultTolerance;
		break;
	    case CONTAINS:
		toleranceLowNegative = 0;
		toleranceLowPositive = extent;
		break;

	    }

	    double toleranceUpNegative;
	    double toleranceUpPositive;
	    switch (expected.getUpperType()) {
	    case ABSOLUTE:
	    default:
		toleranceUpNegative = expectedMaxTolerance != null ? expectedMaxTolerance.doubleValue() : defaultTolerance;
		toleranceUpPositive = expectedMaxTolerance != null ? expectedMaxTolerance.doubleValue() : defaultTolerance;
		break;
	    case CONTAINS:
		toleranceUpNegative = extent;
		toleranceUpPositive = 0;
		break;
	    }

	    // expected lower and upper bounds for the actual minimum grid point
	    double expectedMinLower = expectedMin - toleranceLowNegative - TOL;
	    double expectedMinUpper = expectedMin + toleranceLowPositive + TOL;
	    // expected lower and upper bounds for the actual maximum grid point
	    double expectedMaxLower = expectedMax - toleranceUpNegative - TOL;
	    double expectedMaxUpper = expectedMax + toleranceUpPositive + TOL;

	    if (actualMin == null || actualMax == null) {
		return false;
	    }
	    if (actualMin < expectedMinLower) {
		return false;
	    }
	    if (actualMin > expectedMinUpper) {
		return false;
	    }
	    if (actualMax < expectedMaxLower) {
		return false;
	    }
	    if (actualMax > expectedMaxUpper) {
		return false;
	    }
	}
	if (expectedRes != null) {
	    if (actualRes == null) {
		return false;
	    }
	    double lowerRes = expectedRes - expectedResTolerance;
	    double upperRes = expectedRes + expectedResTolerance;
	    if (lowerRes - TOL > actualRes.doubleValue()) {
		return false;
	    }
	    if (upperRes + TOL < actualRes.doubleValue()) {
		return false;
	    }
	}
	if (expectedSize != null) {
	    if (actualSize == null) {
		return false;
	    }
	    if (expectedSize - actualSize != 0) {
		return false;
	    }
	}
	return true;
    }

    private Double normalize(Unit uom, Double d) {
	if (d == null) {
	    return null;
	}
	if (uom == null) {
	    return d;
	}
	if (uom.equals(Unit.SECOND)) {
	    return d * 1000;
	}
	return d;
    }

    public void checkAxisConsistency(Number min, Number max, Long size, Number res, Number resTolerance) {
	if (min != null && max != null) {
	    if (res != null) {
		// check that resolution is valid according to the given extent
		double minDouble = min.doubleValue();
		double maxDouble = max.doubleValue();
		double resDouble = res.doubleValue();
		double extent = maxDouble - minDouble;

		double reminder = Math.abs(extent % resDouble);
		reminder = Math.abs(Math.min(reminder, resDouble - reminder));
		if (resTolerance != null && Math.abs(resTolerance.doubleValue()) > TOL) {
		    if (reminder > resTolerance.doubleValue()) {
			throw new IllegalArgumentException(
				"Inconsistent descriptor. The resolution is not consistent with the given min max grid points, also considering resolution tolerance: (max-min)%res != 0");
		    }
		} else if (reminder > (resDouble / (1E7))) {
		    throw new IllegalArgumentException(
			    "Inconsistent descriptor. The resolution is not consistent with the given min max grid points: (max-min)%res != 0");
		}
		if (size != null && (resTolerance == null || Math.abs(resTolerance.doubleValue()) < TOL)) {
		    // check that size is valid as well, if a precise resolution is given
		    long points = Math.round(extent / resDouble) + 1;
		    if (points != size) {
			throw new IllegalArgumentException(
				"Inconsistent descriptor. The size is not consistent with the specified resolution");
		    }
		}
	    }
	}
    }

    public ValidationMessage invalidDescriptor(String message) {
	ValidationMessage ret = new ValidationMessage();
	ret.setErrorCode(DataValidatorErrorCode.INVALID_DESCRIPTOR.toString());
	ret.setError(message);
	ret.setResult(ValidationResult.VALIDATION_FAILED);
	return ret;
    }

    public ValidationMessage unsupportedDescriptor(String message) {
	ValidationMessage ret = new ValidationMessage();
	ret.setErrorCode(DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR.toString());
	ret.setError(message);
	ret.setResult(ValidationResult.VALIDATION_FAILED);
	return ret;
    }

    public ValidationMessage getValidationMismatch(String message) {
	ValidationMessage ret = new ValidationMessage();
	ret.setErrorCode(DataValidatorErrorCode.DESCRIPTOR_MISMATCH.toString());
	ret.setError(message);
	ret.setResult(ValidationResult.VALIDATION_FAILED);
	return ret;
    }

    public GSException getGSException(String message) {
	GSException ret = new GSException();
	ErrorInfo info = new ErrorInfo();
	info.setErrorDescription(message);
	ret.addInfo(info);
	return ret;

    }

}
