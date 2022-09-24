package eu.essi_lab.iso.datamodel.classes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.geotools.data.shapefile.shp.xml.IdInfo;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import net.opengis.gml.v_3_2_0.AbstractTimePrimitiveType;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import net.opengis.gml.v_3_2_0.TimeInstantPropertyType;
import net.opengis.gml.v_3_2_0.TimeInstantType;
import net.opengis.gml.v_3_2_0.TimeIntervalLengthType;
import net.opengis.gml.v_3_2_0.TimePeriodType;
import net.opengis.gml.v_3_2_0.TimePositionType;
import net.opengis.iso19139.gmd.v_20060504.EXTemporalExtentType;
import net.opengis.iso19139.gts.v_20060504.TMPrimitivePropertyType;

/**
 * EX_TemporalExtent
 * 
 * @author Fabrizio
 */
public class TemporalExtent extends ISOMetadata<EXTemporalExtentType> {

    /**
     * 
     */
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;

    /**
     * @author Fabrizio
     */
    public enum FrameValue {

	P1D(ONE_DAY), //
	P2D(ONE_DAY * 2), //
	P3D(ONE_DAY * 3), //
	P4D(ONE_DAY * 4), //
	P5D(ONE_DAY * 5), //
	P6D(ONE_DAY * 6), //
	P7D(ONE_DAY * 7), //
	P8D(ONE_DAY * 8), //
	P9D(ONE_DAY * 9), //
	P10D(ONE_DAY * 10), //
	P11D(ONE_DAY * 11), //
	P12D(ONE_DAY * 12), //
	P13D(ONE_DAY * 13), //
	P14D(ONE_DAY * 14), //
	P15D(ONE_DAY * 15), //
	P16D(ONE_DAY * 16), //
	P17D(ONE_DAY * 17), //
	P18D(ONE_DAY * 18), //
	P19D(ONE_DAY * 19), //
	P20D(ONE_DAY * 20), //
	P21D(ONE_DAY * 21), //
	P22D(ONE_DAY * 22), //
	P23D(ONE_DAY * 23), //
	P24D(ONE_DAY * 24), //
	P25D(ONE_DAY * 25), //
	P26D(ONE_DAY * 26), //
	P27D(ONE_DAY * 27), //
	P28D(ONE_DAY * 28), //
	P29D(ONE_DAY * 29), //
	P30D(ONE_DAY * 30), //
	P1M(ONE_DAY * 30),//
	P1Y(ONE_DAY * 365)//
	;

	private long millis;

	/**
	 * @param millis
	 */
	private FrameValue(long millis) {

	    this.millis = millis;
	};

	/**
	 * @param value
	 * @return
	 */
	public static boolean isValid(String value) {

	    try {

		return FrameValue.valueOf(value) != null;

	    } catch (IllegalArgumentException ex) {
	    }

	    return false;
	}

	/**
	 * @return
	 */
	public long asMillis() {

	    return millis;
	}
    }

    /**
     * @author Fabrizio
     */
    public enum Frame {

	/**
	 * 
	 */
	BEFORE_NOW("http://essi-lab.eu/time-constants/before-now"),
	/**
	 * 
	 */
	AFTER_NOW("http://essi-lab.eu/time-constants/after-now");

	private String value;

	private Frame(String value) {

	    this.value = value;
	}

	/**
	 * @return
	 */
	public String getValue() {

	    return value;
	}

	/**
	 * @param value
	 * @return
	 */
	public String computeISO8601DateTime(FrameValue value) {

	    return ISO8601DateTimeUtils.getISO8601DateTime(computeDate(value));
	}

	/**
	 * @param value
	 * @return
	 */
	public Date computeDate(FrameValue value) {

	    switch (this) {
	    case BEFORE_NOW:
		return new Date(new Date().getTime() - value.asMillis());
	    case AFTER_NOW:
		return new Date(new Date().getTime() + value.asMillis());
	    }
	    return null;
	}

	/**
	 * @param value
	 * @return
	 */
	public static Frame decode(String value) {

	    if (Objects.isNull(value) || value.isEmpty()) {

		throw new IllegalArgumentException("Invalid value");
	    }

	    switch (value) {
	    case "http://essi-lab.eu/time-constants/before-now":
		return BEFORE_NOW;
	    case "http://essi-lab.eu/time-constants/after-now":
		return AFTER_NOW;
	    }

	    throw new IllegalArgumentException("Frame not found");
	}
    }

    /**
     * @author Fabrizio
     */
    public enum TimeIntervalUnit {
	YEAR, //
	MONTH, //
	DAY, //
	HOUR, //
	MINUTE, //
	SECOND; //

	public String toString() {

	    return super.toString().toLowerCase();
	}
    }

    /**
     * @XPathDirective(target = "@id")
     */
    public void setId(String id) {
	type.setId(id);
    }

    /**
     * @XPathDirective(target = "@id")
     */
    public String getId() {
	return type.getId();
    }

    public TemporalExtent() {

	this(new EXTemporalExtentType());
    }

    public TemporalExtent(InputStream stream) throws JAXBException {

	super(stream);
    }

    public TemporalExtent(EXTemporalExtentType type) {

	super(type);
    }

    // ------------------------
    //
    // Begin and end position
    //
    /**
     * @XPathDirective(target = "gmd:extent/gml:TimePeriod/gml:beginPosition")
     */
    public void setBeginPosition(String value) {

	setPosition(value, null, false, true);
    }

    /**
     * @XPathDirective(target = "gmd:extent/gml:TimePeriod/gml:endPosition")
     */
    public void setEndPosition(String value) {

	setPosition(value, null, false, false);
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:beginPosition")
     */
    public String getBeginPosition() {

	Optional<String> position = getOptionalBeginPosition();

	if (position.isPresent()) {

	    if (FrameValue.isValid(position.get())) {

		FrameValue frameValue = FrameValue.valueOf(position.get());

		if (Objects.nonNull(frameValue)) {

		    if (isBeforeNowBeginPosition()) {

			return Frame.BEFORE_NOW.computeISO8601DateTime(frameValue);

		    } else {

			return Frame.AFTER_NOW.computeISO8601DateTime(frameValue);
		    }
		}
	    }

	    return position.get();
	}

	if (isBeginPositionIndeterminate() && getIndeterminateBeginPosition() == TimeIndeterminateValueType.NOW) {

	    return ISO8601DateTimeUtils.getISO8601DateTime();
	}

	return null;
    }

    /**
     * @return
     */
    public Optional<FrameValue> getBeforeNowBeginPosition() {

	if (isBeforeNowBeginPosition()) {

	    return Optional.of(FrameValue.valueOf(getOptionalBeginPosition().get()));
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public Optional<FrameValue> getAfterNowBeginPosition() {

	if (isAfterNowBeginPosition()) {

	    return Optional.of(FrameValue.valueOf(getOptionalBeginPosition().get()));
	}

	return Optional.empty();
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:endPosition")
     */
    public String getEndPosition() {

	try {

	    if (isEndPositionIndeterminate() && getIndeterminateEndPosition() == TimeIndeterminateValueType.NOW) {

		return ISO8601DateTimeUtils.getISO8601DateTime();
	    }

	    return getTimePeriodType().getEndPosition().getValue().get(0);
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    // ------------------------
    //
    // Time period identifier
    //
    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/@gml:id")
     */
    public void setTimePeriodId(String value) {

	setTimePeriodValue(value, "id"); // id
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/@*:id")
     */
    public String getTimePeriodId() {

	try {
	    return getTimePeriodType().getId();
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    // ---------------------------
    //
    // Duration
    //
    public void setDuration(String value) throws IllegalArgumentException {

	setTimePeriodValue(value, "duration");// duration
    }

    public javax.xml.datatype.Duration getDuration() {
	try {
	    return getTimePeriodType().getDuration();
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    // ---------------------------
    //
    // Time interval
    //
    /**
     * @XPathDirective(target = "gmd:extent/gml:TimePeriod/gml:timeInterval")
     */
    public void setTimeInterval(double value) {

	setTimePeriodValue(String.valueOf(value), "interval");// interval
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:timeInterval")
     */
    public Double getTimeInterval() {

	try {
	    return Double.valueOf(getTimePeriodType().getTimeInterval().getValue().toString());
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:extent/gml:TimePeriod/gml:timeInterval/@unit")
     */
    public void setTimeIntervalUnit(TimeIntervalUnit unit) {

	setTimePeriodValue(unit.toString(), "unit");// interval
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:timeInterval/@unit")
     */
    public TimeIntervalUnit getTimeIntervalUnit() {

	try {
	    return TimeIntervalUnit.valueOf(getTimePeriodType().getTimeInterval().getUnit().toUpperCase());
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    // ------------------------
    //
    // Indeterminate
    //
    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:beginPosition/@indeterminatePosition")
     */
    public void setIndeterminateBeginPosition(TimeIndeterminateValueType value) {

	setPosition(null, value, false, true);
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:endPosition/@indeterminatePosition")
     */
    public void setIndeterminateEndPosition(TimeIndeterminateValueType value) {

	setPosition(null, value, false, false);
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:beginPosition/@indeterminatePosition")
     */
    public TimeIndeterminateValueType getIndeterminateBeginPosition() {

	try {
	    return getTimePeriodType().getBeginPosition().getIndeterminatePosition();

	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/*:endPosition/@indeterminatePosition")
     */
    public TimeIndeterminateValueType getIndeterminateEndPosition() {

	try {
	    return getTimePeriodType().getEndPosition().getIndeterminatePosition();
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "boolean(gmd:extent/*:TimePeriod/*:beginPosition/@indeterminatePosition)")
     */
    public Boolean isBeginPositionIndeterminate() {

	return getIndeterminateBeginPosition() != null;
    }

    /**
     * @param value
     */
    public void setBeforeNowBeginPosition(FrameValue value) {

	setBeginPosition(String.valueOf(value));

	getTimePeriodType().getBeginPosition().setFrame(Frame.BEFORE_NOW.getValue());

    }

    /**
     * @param value
     */
    public void setAfterNowBeginPosition(FrameValue value) {

	setBeginPosition(String.valueOf(value));

	getTimePeriodType().getBeginPosition().setFrame(Frame.AFTER_NOW.getValue());

    }

    /**
     * @return
     */
    public boolean isBeforeNowBeginPosition() {

	try {
	    if (getTimePeriodType().getBeginPosition().isSetFrame()) {

		String frame = getTimePeriodType().getBeginPosition().getFrame();

		return Frame.decode(frame) == Frame.BEFORE_NOW;
	    }
	} catch (Exception ex) {
	}

	return false;
    }

    /**
     * @return
     */
    public boolean isAfterNowBeginPosition() {

	try {
	    if (getTimePeriodType().getBeginPosition().isSetFrame()) {

		String frame = getTimePeriodType().getBeginPosition().getFrame();

		return Frame.decode(frame) == Frame.AFTER_NOW;
	    }
	} catch (Exception ex) {
	}

	return false;
    }

    /**
     * @XPathDirective(target = "boolean(gmd:extent/*:TimePeriod/*:endPosition/@indeterminatePosition)")
     */
    public Boolean isEndPositionIndeterminate() {

	return getIndeterminateEndPosition() != null;
    }

    // ---------------------------
    //
    // Begin and end time instant
    //
    /**
     * @XPathDirective(target = "gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition")
     */
    public void setTimeInstantBegin(String value) {

	setPosition(value, null, true, true);
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition")
     */
    public String getTimeInstantBegin() {

	try {
	    return getTimePeriodType().getBegin().getTimeInstant().getTimePosition().getValue().get(0);
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:extent/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition")
     */
    public void setTimeInstantEnd(String value) {

	setPosition(value, null, true, false);
    }

    /**
     * @XPathDirective(target = "gmd:extent/*:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition")
     */
    public String getTimeInstantEnd() {

	try {
	    return getTimePeriodType().getEnd().getTimeInstant().getTimePosition().getValue().get(0);
	} catch (NullPointerException | ClassCastException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    /**
     * @return
     */
    private Optional<String> getOptionalBeginPosition() {

	try {

	    return Optional.of(getTimePeriodType().getBeginPosition().getValue().get(0));

	} catch (Exception ex) {
	}

	return Optional.empty();
    }

    private javax.xml.datatype.Duration createDuration(String duration) throws IllegalArgumentException {
	try {
	    DatatypeFactory factory = DatatypeFactory.newInstance();
	    if (duration != null && !duration.equals("")) {
		return factory.newDuration(duration);
	    } else {
		throw new IllegalArgumentException("duration cannot be null or empty string");
	    }
	} catch (DatatypeConfigurationException e) {
	}
	return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setTimePeriodValue(String value, String target) {

	TMPrimitivePropertyType extent = type.getExtent();
	if (extent == null) {
	    extent = new TMPrimitivePropertyType();
	    type.setExtent(extent);
	}

	JAXBElement<AbstractTimePrimitiveType> abstractTimePrimitive = extent.getAbstractTimePrimitive();
	if (abstractTimePrimitive == null) {

	    TimePeriodType timePeriodType = new TimePeriodType();
	    setValue(timePeriodType, value, target);

	    JAXBElement<TimePeriodType> createTimePeriod = ObjectFactories.GML().createTimePeriod(timePeriodType);
	    extent.setAbstractTimePrimitive((JAXBElement) createTimePeriod);

	} else {
	    TimePeriodType type = (TimePeriodType) abstractTimePrimitive.getValue();
	    setValue(type, value, target);
	}
    }

    private void setValue(TimePeriodType type, String value, String target) {

	switch (target) {
	case "duration":
	    type.setDuration(createDuration(value));
	    break;
	case "id":
	    type.setId(value);
	    break;
	case "interval":
	    TimeIntervalLengthType timeInterval = type.getTimeInterval();
	    if (timeInterval == null) {
		timeInterval = new TimeIntervalLengthType();
		type.setTimeInterval(timeInterval);
	    }

	    timeInterval.setValue(new BigDecimal(value));
	    break;
	case "unit":
	    timeInterval = type.getTimeInterval();
	    if (timeInterval == null) {
		timeInterval = new TimeIntervalLengthType();
		type.setTimeInterval(timeInterval);
	    }
	    timeInterval.setUnit(value);
	    break;
	}
    }

    private TimePeriodType getTimePeriodType() throws NullPointerException, ClassCastException, IndexOutOfBoundsException {

	TMPrimitivePropertyType extent = type.getExtent();
	JAXBElement<AbstractTimePrimitiveType> abstractTimePrimitive = extent.getAbstractTimePrimitive();
	TimePeriodType value = (TimePeriodType) abstractTimePrimitive.getValue();
	return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setPosition(String value, TimeIndeterminateValueType indType, boolean timeInstant, boolean begin) {

	// position
	TimePositionType timePositionType = new TimePositionType();
	if (indType != null) {
	    timePositionType.setIndeterminatePosition(indType);
	}
	if (value != null) {
	    timePositionType.setValue(Lists.newArrayList(value));
	}

	// instant
	TimeInstantType timeInstantType = new TimeInstantType();
	timeInstantType.setTimePosition(timePositionType);

	TimeInstantPropertyType timeInstantPropertyType = new TimeInstantPropertyType();
	timeInstantPropertyType.setTimeInstant(timeInstantType);

	TMPrimitivePropertyType extent = type.getExtent();
	if (extent == null) {
	    extent = new TMPrimitivePropertyType();
	    type.setExtent(extent);
	}

	JAXBElement<AbstractTimePrimitiveType> abstractTimePrimitive = extent.getAbstractTimePrimitive();
	if (abstractTimePrimitive == null) {

	    TimePeriodType timePeriodType = new TimePeriodType();

	    if (begin) {
		if (timeInstant) {
		    timePeriodType.setBegin(timeInstantPropertyType);
		} else {
		    timePeriodType.setBeginPosition(timePositionType);
		}
	    } else {
		if (timeInstant) {
		    timePeriodType.setEnd(timeInstantPropertyType);

		} else {
		    timePeriodType.setEndPosition(timePositionType);
		}
	    }

	    JAXBElement<TimePeriodType> createTimePeriod = ObjectFactories.GML().createTimePeriod(timePeriodType);
	    extent.setAbstractTimePrimitive((JAXBElement) createTimePeriod);

	} else {
	    TimePeriodType type = (TimePeriodType) abstractTimePrimitive.getValue();
	    if (begin) {

		if (timeInstant) {
		    type.setBegin(timeInstantPropertyType);
		} else {
		    type.setBeginPosition(timePositionType);
		}

	    } else {

		if (timeInstant) {
		    type.setEnd(timeInstantPropertyType);

		} else {
		    type.setEndPosition(timePositionType);
		}
	    }
	}
    }

    public JAXBElement<EXTemporalExtentType> getElement() {

	JAXBElement<EXTemporalExtentType> element = ObjectFactories.GMD().createEXTemporalExtent(type);
	return element;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, JAXBException {

	// TemporalExtent temporalExtent = new TemporalExtent();
	//
	// temporalExtent.setBeginPosition("XXXXX");
	//
	// temporalExtent.setBeforeNowBeginPosition(10000);
	//
	// System.out.println(temporalExtent.isBeforeNowBeginPosition());
	//
	// MDMetadata mdMetadata = new MDMetadata();
	//
	// mdMetadata.addDataIdentification(new DataIdentification());
	//
	// mdMetadata.getDataIdentification().addTemporalExtent(temporalExtent);
	//
	// System.out.println(mdMetadata.asString(true));

	java.time.Duration d = java.time.Duration.parse("P30D");
	System.out.println("Duration in seconds: " + d.get(java.time.temporal.ChronoUnit.SECONDS));
    }
}
