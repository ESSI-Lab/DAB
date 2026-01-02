package eu.essi_lab.bufr.datamodel;

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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.InterpolationType;

@XmlRootElement
public class BUFRElement {

    public BUFRElement() {

    }

    public BUFRElement(String name, String code, String units, String value) {
	super();
	this.name = name;
	this.code = code;
	this.units = units;
	this.value = value;
    }

    @XmlElement
    private String name;

    @XmlElement
    private String code;

    @XmlElement
    private InterpolationType interpolationType = null;

    @XmlElement
    private BUFRElement timeSupport;

    @XmlElement
    private List<BUFRElement> auxiliaryElements = new ArrayList<>();

    @XmlElement
    private String units;
    @XmlElement
    private String value;

    @XmlTransient
    public List<BUFRElement> getAuxiliaryElements() {
	return auxiliaryElements;
    }

    @XmlTransient
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    @XmlTransient
    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    @XmlTransient
    public String getUnits() {
	return units;
    }

    public void setUnits(String units) {
	this.units = units;
    }

    @XmlTransient
    public String getValue() {
	return value;
    }

    public void setValue(String value) {
	this.value = value;
    }

    @XmlTransient
    public InterpolationType getInterpolationType() {
	return interpolationType;
    }

    public void setInterpolationType(InterpolationType interpolationType) {
	this.interpolationType = interpolationType;
    }

    @XmlTransient
    public BUFRElement getTimeSupport() {
	return timeSupport;
    }

    public void setTimeSupport(BUFRElement timeSupport) {
	this.timeSupport = timeSupport;
    }

    public void print() {
	GSLoggerFactory.getLogger(getClass()).info(toString());

    }

    @Override
    public String toString() {
	String info = name;
	if (units != null) {
	    info += " (units: " + units + ")";
	}
	if (timeSupport != null) {
	    info += " (time support: " + timeSupport.toString() + ")";
	}
	if (!getAuxiliaryElements().isEmpty()) {
	    for (BUFRElement bufrElement : auxiliaryElements) {
		info += " (aux element: " + bufrElement.toString() + ")";
	    }
	}
	info += ": " + value;
	return info;
    }

    public boolean isTimePeriorOrDisplacement() {
	if (name.contains("Time_period_or_displacement")) {
	    return true;
	} else {
	    return false;
	}
    }

    public boolean isTimeSignificance() {
	return name.contains("Time_significance");
    }

    private String[] headerLabels = new String[] { "WMO_block_number", "WMO_station_number", "Type_of_station", "Year", "Month", "Day",
	    "Hour", "Minute", "Latitude", "Longitude", "BUFR:", "WMO_Header:", "_CoordSysBuilder" };

    private String[] auxiliaryLabels = new String[] { "Height_of_station_ground_above_mean_sea_level",
	    "Height_of_sensor_above_local_ground_ord_deck_of_marine_platform", "Vertical_significance_", "Type_of_instrumentation" };

    public boolean isHeaderVariable() {
	for (String noVariableLabel : headerLabels) {
	    if (name.contains(noVariableLabel)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isAuxiliaryVariable() {
	for (String noVariableLabel : auxiliaryLabels) {
	    if (name.contains(noVariableLabel)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isVariable() {

	if (isTimePeriorOrDisplacement()) {
	    return false;
	}

	if (isTimeSignificance()) {
	    return false;
	}

	for (String noVariableLabel : headerLabels) {
	    if (name.contains(noVariableLabel)) {
		return false;
	    }
	}
	for (String noVariableLabel : auxiliaryLabels) {
	    if (name.contains(noVariableLabel)) {
		return false;
	    }
	}

	return true;
    }

}
