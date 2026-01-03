package eu.essi_lab.accessor.dws.client;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.essi_lab.model.resource.InterpolationType;

public class DWSStation {

    private String stationCode;
    private String stationName;
    private Integer catchmentAreaKm2;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Date beginDate;
    private Date endDate;

    public String getStationCode() {
	return stationCode;
    }

    public void setStationCode(String stationCode) {
	this.stationCode = stationCode;
    }

    public String getStationName() {
	return stationName;
    }

    public void setStationName(String stationName) {
	this.stationName = stationName;
    }

    public Integer getCatchmentAreaKm2() {
	return catchmentAreaKm2;
    }

    public void setCatchmentAreaKm2(Integer catchmentAreaKm2) {
	this.catchmentAreaKm2 = catchmentAreaKm2;
    }

    public BigDecimal getLatitude() {
	return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
	this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
	return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
	this.longitude = longitude;
    }

    public Date getBeginDate() {
	return beginDate;
    }

    public void setBeginDate(Date beginDate) {
	this.beginDate = beginDate;
    }

    public Date getEndDate() {
	return endDate;
    }

    public void setEndDate(Date endDate) {
	this.endDate = endDate;
    }

    // volume - m^3 - million cubic metres interp=total period=1M
    // flow rate m^3/s - cubic metres/sec interp=average period=1D
    // water level, stream - m - meter - interp=continuos
    // discharge, stream - m^3/s - cubic metres/sec - interp=continous
    public enum DWSVariable {
	VOLUME("Volume", "volume", "m³", InterpolationType.TOTAL, "Montlhy"), //
	FLOW_RATE("Flow Rate", "flow_rate", "m³/s", InterpolationType.AVERAGE, "Daily"), //
	WATER_LEVEL("Water Level, stream", "water_level", "m", InterpolationType.CONTINUOUS, "Point"), //
	DISCHARGE("Discharge, stream", "discharge", "m³/s", InterpolationType.CONTINUOUS, "Point");

	

	DWSVariable(String label, String paramId, String units, InterpolationType interpolation, String dataType) {
	    this.label = label;
	    this.paramId = paramId;
	    this.units = units;
	    this.interpolation = interpolation;
	    this.dataType = dataType;
	}

	public String getLabel() {
	    return label;
	}

	public void setLabel(String label) {
	    this.label = label;
	}

	public String getParamId() {
	    return paramId;
	}

	public void setParamId(String paramId) {
	    this.paramId = paramId;
	}

	public String getUnits() {
	    return units;
	}

	public void setUnits(String units) {
	    this.units = units;
	}

	public InterpolationType getInterpolation() {
	    return interpolation;
	}

	public void setInterpolation(InterpolationType interpolation) {
	    this.interpolation = interpolation;
	}

	private String label;
	private String paramId;
	private String units;
	private InterpolationType interpolation;
	private String dataType;

	public String getDataType() {
	    return dataType;
	}

	public void setDataType(String dataType) {
	    this.dataType = dataType;
	}

	public static DWSVariable decode(String parameterCode) {
	    for (DWSVariable var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;
	}

    }

    public List<Variable> getVariables() {
	// TODO: now we put here hard-coded variables

	List<Variable> ret = new ArrayList<>();

	DWSVariable[] variables = DWSVariable.values();
	for (int i = 0; i < variables.length; i++) {
	    String abbreviation = variables[i].getParamId();
	    String label = variables[i].getLabel();
	    String unit = variables[i].getUnits();
	    InterpolationType interpolation = variables[i].getInterpolation();
	    ret.add(new Variable(variables[i].name() ,abbreviation, label, unit, interpolation));
	}

	return ret;

    }

}
