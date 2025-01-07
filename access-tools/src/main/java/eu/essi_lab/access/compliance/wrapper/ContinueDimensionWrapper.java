package eu.essi_lab.access.compliance.wrapper;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;

/**
 * @author Fabrizio
 */
public class ContinueDimensionWrapper extends AbstractDimensionWrapper {

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String lower;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String upper;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String uom;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String datum;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Long size;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String resolution;

    /**
     * 
     */
    public ContinueDimensionWrapper() {

	super(null);
    }

    /**
     * @param sizedDimension
     */
    public ContinueDimensionWrapper(ContinueDimension sizedDimension) {

	super(sizedDimension);

	setDatum(sizedDimension.getDatum() != null ? sizedDimension.getDatum().getIdentifier() : null);
	setLower(sizedDimension.getLower());
	setResolution(sizedDimension.getResolution());
	setSize(sizedDimension.getSize());
	setUom(sizedDimension.getUom() != null ? sizedDimension.getUom().getIdentifier() : null);
	setUpper(sizedDimension.getUpper());
    }

    /**
     * @param dim
     * @return
     * @throws ParseException
     */
    public static ContinueDimension wrap(ContinueDimensionWrapper dim) throws ParseException {

	String datum = dim.getDatum();
	String name = dim.getName();
	String resolution = dim.getResolution();
	Long size = dim.getSize();
	String type = dim.getType();
	String uom = dim.getUom();
	String upper = dim.getUpper();
	String lower = dim.getLower();

	ContinueDimension sizedDimension = new ContinueDimension(name);
	if (datum != null) {
	    sizedDimension.setDatum(Datum.fromIdentifier(datum));
	}
	if (resolution != null) {
	    sizedDimension.setResolution(NumberFormat.getInstance(Locale.US).parse(resolution));
	}

	if (type != null) {
	    sizedDimension.setType(DimensionType.fromIdentifier(type));
	}
	if (uom != null) {
	    sizedDimension.setUom(Unit.fromIdentifier(uom));
	}
	sizedDimension.setUpper(NumberFormat.getInstance(Locale.US).parse(upper));
	sizedDimension.setLower(NumberFormat.getInstance(Locale.US).parse(lower));
	sizedDimension.setSize(size);

	return sizedDimension;
    }

    @XmlTransient
    public String getDatum() {

	return datum;
    }

    public void setDatum(String datum) {

	this.datum = datum;
    }

    @XmlTransient
    public String getLower() {

	return lower;
    }

    public void setLower(Number lower) {

	if (lower != null) {

	    this.lower = lower.toString();
	}
    }

    @XmlTransient
    public String getUpper() {

	return upper;
    }

    public void setUpper(Number upper) {

	if (upper != null) {

	    this.upper = upper.toString();
	}
    }

    @XmlTransient
    public String getUom() {

	return uom;
    }

    public void setUom(String uom) {

	this.uom = uom;
    }

    @XmlTransient
    public String getResolution() {

	return resolution;
    }

    public void setResolution(Number resolution) {

	if (resolution != null) {

	    this.resolution = resolution.toString();
	}
    }

    public void setSize(Long size) {

	this.size = size;
    }

    @XmlTransient
    public Long getSize() {

	return size;
    }
}
