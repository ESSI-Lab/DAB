/**
 * 
 */
package eu.essi_lab.accessor.aviso;

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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * @author Fabrizio
 */
public class AVISOMapper extends AbstractResourceMapper {

    public static final String METADATA_SCHEMA = "AVISO_METADATA_SCHEMA";
    public static final String NET_CDF_FILE_PROPERTY = "NET_CDF_FILE";

    private static final String AVISO_MAPPER_NETCDF_FILE_ERROR = "AVISO_MAPPER_NETCDF_FILE_ERROR";

    /**
     * 
     */
    public AVISOMapper() {
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	File file = originalMD.getAdditionalInfo().get(NET_CDF_FILE_PROPERTY, File.class);

	NetcdfDataset dataset = null;
	try {
	    dataset = NetcdfDataset.openDataset(file.toString(), true, null);

	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    AVISO_MAPPER_NETCDF_FILE_ERROR, //
		    e);
	}

	List<Attribute> gas = dataset.getGlobalAttributes();
	List<Variable> variables = dataset.getVariables();

	List<Dimension> dimensions = dataset.getDimensions();

	String isoDate = null;
	String title = null;
	String institution = null;
	String references = null;

	//
	// SEA LEVEL TRENDS VARIABLES
	//
	Double west = null; //
	Double south = null; //
	Double east = null; //
	Double north = null; //
	int lonLength;
	int latLength;

	String sltFillValue = null; // sea level trend value
	String sltUnitOfMeasure = null; // sea level trend units of measure

	//
	// MEAN SEA LEVEL VARIABLES
	//
	String mslFillValue = null; // mean sea level value -> double
	String mslUnitOfMeasure = null; // mean sea level units of measure

	String timeLength = null; // time value
	String timeUnitOfMeasure = null; // time units of measure

	String cycleFillValue = null; // int
	String cycleNumberOfPasses = null; // int (sometimes null)
	String cycleLongName = null;

	String passFillValue = null; // int
	String passLongName = null;

	if (dimensions.size() == 2) {

	    Optional<Dimension> lonDim = dimensions.stream().filter(d -> d.getShortName().equals("longitude")).findFirst();
	    Optional<Dimension> latDim = dimensions.stream().filter(d -> d.getShortName().equals("latitude")).findFirst();

	    // CoordinateAxis findCoordinateAxis = dataset.findCoordinateAxis(AxisType.Lon);
	    // CoordinateAxis1DTime.factory(dataset, variable, null);

	    if (lonDim.isPresent()) {

		lonLength = lonDim.get().getLength();
	    }

	    if (latDim.isPresent()) {

		latLength = latDim.get().getLength();
	    }

	} else {

	    timeLength = String.valueOf(dimensions.get(0).getLength());

	    // CoordinateAxis coordinateAxis = dataset.findCoordinateAxis(AxisType.Time);
	}

	for (Attribute attribute : gas) {

	    //
	    // date stamp / temporal extent
	    //
	    if (attribute.getShortName().equals("history")) {

		String value = attribute.getStringValue();
		isoDate = value.split(" : ")[0].replace(" ", "T");

		if (!ISO8601DateTimeUtils.parseISO8601ToDate(isoDate).isPresent()) {

		    isoDate = null;
		}
	    }

	    //
	    // institution
	    //
	    if (attribute.getShortName().equals("institution")) {

		institution = attribute.getStringValue();
	    }

	    //
	    // references
	    //
	    if (attribute.getShortName().equals("references")) {

		references = attribute.getStringValue();
	    }
	}

	for (Variable variable : variables) {

	    String varName = variable.getShortName();

	    //
	    // SEA LEVEL THRENDS or MEAN SEA LEVEL
	    //

	    if (varName.equals("sea_level_trends") || varName.equals("msl")) {

		Attribute longNameAttribute = variable.findAttribute("long_name");
		if (Objects.nonNull(longNameAttribute)) {
		    title = longNameAttribute.getStringValue();
		}

		Attribute units = variable.findAttribute("units");
		if (Objects.nonNull(units)) {

		    if (varName.equals("msl")) {

			mslUnitOfMeasure = units.getValue(0).toString();

		    } else {

			sltUnitOfMeasure = units.getValue(0).toString();
		    }
		}

		Attribute fillValue = variable.findAttribute("_FillValue");
		if (Objects.nonNull(fillValue)) {
		    if (varName.equals("msl")) {

			mslFillValue = fillValue.getValue(0).toString();

		    } else {

			sltFillValue = fillValue.getValue(0).toString();
		    }
		}
	    }

	    //
	    // SEA LEVEL THRENDS VARIABLES
	    //
	    try {
		if (varName.equals("longitude")) {

		    double[] lonArray = (double[]) dataset.findCoordinateAxis(AxisType.Lon).read().copyTo1DJavaArray();

		    double minLon = Arrays.stream(lonArray).map(v -> new Double(v)).min().getAsDouble();
		    double maxLon = Arrays.stream(lonArray).map(v -> new Double(v)).max().getAsDouble();

		    west = minLon <= 180 ? minLon : -(minLon - 180);
		    east = maxLon <= 180 ? maxLon : -(maxLon - 180);

		} else if (varName.equals("latitude")) {

		    double[] latArray = (double[]) dataset.findCoordinateAxis(AxisType.Lat).read().copyTo1DJavaArray();

		    double minLat = Arrays.stream(latArray).map(v -> new Double(v)).min().getAsDouble();
		    double maxLat = Arrays.stream(latArray).map(v -> new Double(v)).max().getAsDouble();

		    south = minLat <= 90 ? minLat : -(minLat - 90);
		    north = maxLat <= 90 ? maxLat : -(maxLat - 90);
		}
	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }

	    //
	    // MEAN SEA LEVEL VARIABLES
	    //

	    if (varName.equals("pass")) {

		Attribute fillValue = variable.findAttribute("_FillValue");
		if (Objects.nonNull(fillValue)) {
		    passFillValue = fillValue.getValue(0).toString();
		}

		Attribute longName = variable.findAttribute("long_name");
		if (Objects.nonNull(longName)) {
		    passLongName = longName.getValue(0).toString();
		}

	    } else if (varName.equals("time")) {

		// CoordinateAxis1DTime.factory(dataset, variable, null);

		Attribute units = variable.findAttribute("units");
		if (Objects.nonNull(units)) {
		    timeUnitOfMeasure = units.getStringValue();
		}

	    } else if (varName.equals("cycle")) {

		Attribute fillValue = variable.findAttribute("_FillValue");
		if (Objects.nonNull(fillValue)) {
		    cycleFillValue = fillValue.getValue(0).toString();
		}

		Attribute longName = variable.findAttribute("long_name");
		if (Objects.nonNull(longName)) {
		    cycleLongName = longName.getValue(0).toString();
		}

		Attribute numPasses = variable.findAttribute("number_of_passes");
		if (Objects.nonNull(numPasses)) {
		    cycleNumberOfPasses = numPasses.getValue(0).toString();
		}
	    }
	}

	try {
	    dataset.release();
	    dataset.close();
	    file.delete();

	} catch (IOException e) {
	    file.deleteOnExit();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	Dataset gsResource = new Dataset();
	CoreMetadata coreMetadata = gsResource.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();
	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	//
	// date stamp / temporal extent
	//
	if (Objects.nonNull(isoDate)) {

	    miMetadata.setDateStampAsDate(isoDate.substring(0, isoDate.indexOf("T")));
	    coreMetadata.addTemporalExtent(isoDate, isoDate);
	}

	//
	// title / abstract
	//
	if (Objects.nonNull(title)) {

	    coreMetadata.setTitle(title);

	    if (Objects.nonNull(sltUnitOfMeasure)) {

		coreMetadata.setAbstract(title + " expressed in " + sltUnitOfMeasure);
	    } else if (Objects.nonNull(mslUnitOfMeasure)) {

		coreMetadata.setAbstract(title + " expressed in " + mslUnitOfMeasure);
	    }
	}

	//
	// institution and references as keywords
	//
	if (Objects.nonNull(institution)) {

	    dataIdentification.addKeyword(institution);
	}

	if (Objects.nonNull(references)) {

	    dataIdentification.addKeyword(references);
	}

	//
	// bbox
	//
	if (Objects.nonNull(west) && Objects.nonNull(south) && Objects.nonNull(east) && Objects.nonNull(north)) {

	    coreMetadata.addBoundingBox(//
		    Double.valueOf(north), //
		    Double.valueOf(west), //
		    Double.valueOf(south), //
		    Double.valueOf(east));
	}

	//
	// distribution info
	//

	coreMetadata.addDistributionOnlineResource(//
		file.getName(), //
		source.getEndpoint().replace("http", "ftp") + file.getName(), //
		NetProtocols.FTP.getCommonURN(), //
		"download");

	return gsResource;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	return null;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return METADATA_SCHEMA;
    }

    public static void main(String[] args) {

	double[] d = new double[] { 4, 5, 6 };

	Arrays.stream(d).boxed().collect(Collectors.toList());

    }

}
