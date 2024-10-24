package eu.essi_lab.iso.datamodel.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Optional;

import javax.xml.datatype.Duration;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.MetadataTest;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.TimeIntervalUnit;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import net.opengis.iso19139.gmd.v_20060504.EXTemporalExtentType;

public class TemporalExtentTest extends MetadataTest<TemporalExtent, EXTemporalExtentType> {

    public TemporalExtentTest() {
	super(TemporalExtent.class, EXTemporalExtentType.class);
    }

    @Test
    public void test() {

	try {

	    TemporalExtent temporalExtent = new TemporalExtent();

	    Assert.assertNull(temporalExtent.getBeginPosition());
	    Assert.assertNull(temporalExtent.getEndPosition());

	    Assert.assertNull(temporalExtent.getIndeterminateBeginPosition());
	    Assert.assertNull(temporalExtent.getIndeterminateEndPosition());

	    Assert.assertNull(temporalExtent.getTimeInstantBegin());
	    Assert.assertNull(temporalExtent.getTimeInstantEnd());

	    Assert.assertNull(temporalExtent.getTimeInterval());
	    Assert.assertNull(temporalExtent.getTimeIntervalUnit());

	    Assert.assertNull(temporalExtent.getTimePeriodId());
	    Assert.assertNull(temporalExtent.getDuration());

	    Assert.assertFalse(temporalExtent.isBeginPositionIndeterminate());
	    Assert.assertFalse(temporalExtent.isEndPositionIndeterminate());

	    test(temporalExtent);
	    test(temporalExtent.getElementType());

	    Assert.assertFalse(temporalExtent.isBeforeNowBeginPosition());
	    Assert.assertFalse(temporalExtent.isAfterNowBeginPosition());

	    Assert.assertFalse(temporalExtent.getBeforeNowBeginPosition().isPresent());
	    Assert.assertFalse(temporalExtent.getAfterNowBeginPosition().isPresent());

	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    temporalExtent.toStream(outputStream);

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

	    TemporalExtent temp2 = new TemporalExtent(inputStream);
	    test(temp2);
	    test(temp2.getElementType());

	    test2(new TemporalExtent());
	    test3(new TemporalExtent());
	    test4(new TemporalExtent());
	    test5(new TemporalExtent());
	    test6(new TemporalExtent());
	    test7(new TemporalExtent());
	    test8(new TemporalExtent());
	    test9(new TemporalExtent());
	    test10(new TemporalExtent());
	    test11(new TemporalExtent());
	    test12(new TemporalExtent());

	} catch (Exception e) {

	    e.printStackTrace();

	    fail("Exception thrown");
	}
    }

    /**
     * @param temporalExtent
     */
    private void test11(TemporalExtent temporalExtent) {

	temporalExtent.setAfterNowBeginPosition(FrameValue.P10D);

	Assert.assertTrue(temporalExtent.isAfterNowBeginPosition());
	Assert.assertFalse(temporalExtent.isBeforeNowBeginPosition());

	Assert.assertFalse(temporalExtent.getBeforeNowBeginPosition().isPresent());
	Assert.assertTrue(temporalExtent.getAfterNowBeginPosition().isPresent());

	FrameValue frameValue = temporalExtent.getAfterNowBeginPosition().get();

	Assert.assertEquals(FrameValue.P10D, frameValue);

	String beginPosition = temporalExtent.getBeginPosition();
	Optional<Date> iso8601ToDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition);

	Assert.assertTrue(iso8601ToDate.isPresent());
    }

    /**
     * @param temporalExtent
     */
    private void test12(TemporalExtent temporalExtent) {

	temporalExtent.setBeforeNowBeginPosition(FrameValue.P10D);

	Assert.assertFalse(temporalExtent.isAfterNowBeginPosition());
	Assert.assertTrue(temporalExtent.isBeforeNowBeginPosition());

	Assert.assertTrue(temporalExtent.getBeforeNowBeginPosition().isPresent());
	Assert.assertFalse(temporalExtent.getAfterNowBeginPosition().isPresent());

	FrameValue frameValue = temporalExtent.getBeforeNowBeginPosition().get();

	Assert.assertEquals(FrameValue.P10D, frameValue);

	String beginPosition = temporalExtent.getBeginPosition();
	Optional<Date> iso8601ToDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition);

	Assert.assertTrue(iso8601ToDate.isPresent());

    }

    private void test(EXTemporalExtentType elementType) {

	test(new TemporalExtent(elementType));
    }

    private void test3(TemporalExtent temporalExtent) {

	temporalExtent.setBeginPosition("BEGIN");
	Assert.assertEquals(temporalExtent.getBeginPosition(), "BEGIN");
    }

    private void test4(TemporalExtent temporalExtent) {

	temporalExtent.setTimeInstantBegin("BEGIN");
	Assert.assertEquals(temporalExtent.getTimeInstantBegin(), "BEGIN");
    }

    private void test5(TemporalExtent temporalExtent) {

	temporalExtent.setEndPosition("END");
	Assert.assertEquals(temporalExtent.getEndPosition(), "END");
    }

    private void test6(TemporalExtent temporalExtent) {

	temporalExtent.setTimeInstantEnd("END");
	Assert.assertEquals(temporalExtent.getTimeInstantEnd(), "END");
    }

    private void test7(TemporalExtent temporalExtent) {

	temporalExtent.setDuration("P3D");
	Assert.assertEquals(temporalExtent.getDuration().getDays(), 3);
    }

    private void test8(TemporalExtent temporalExtent) {

	temporalExtent.setTimeInterval(1);
	Assert.assertEquals(temporalExtent.getTimeInterval(), new Double(1));
    }

    private void test9(TemporalExtent temporalExtent) {

	temporalExtent.setTimeIntervalUnit(TimeIntervalUnit.DAY);
	Assert.assertEquals(temporalExtent.getTimeIntervalUnit(), TimeIntervalUnit.DAY);
    }

    private void test10(TemporalExtent temporalExtent) {

	temporalExtent.setTimePeriodId("ID");
	Assert.assertEquals(temporalExtent.getTimePeriodId(), "ID");
    }

    private void test2(TemporalExtent temporalExtent) {

	temporalExtent.setIndeterminateBeginPosition(TimeIndeterminateValueType.NOW);
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.BEFORE);

	Assert.assertEquals(temporalExtent.getIndeterminateBeginPosition(), TimeIndeterminateValueType.NOW);
	Assert.assertEquals(temporalExtent.getIndeterminateEndPosition(), TimeIndeterminateValueType.BEFORE);

	temporalExtent.setIndeterminateBeginPosition(TimeIndeterminateValueType.NOW);
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);

	String beginPosition = temporalExtent.getBeginPosition();
	String endPosition = temporalExtent.getEndPosition();

	Assert.assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition).isPresent());
	Assert.assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(endPosition).isPresent());

    }

    private void test(TemporalExtent temporalExtent) {

	Assert.assertEquals(temporalExtent.isBeginPositionIndeterminate(), false);
	Assert.assertEquals(temporalExtent.isEndPositionIndeterminate(), false);

	temporalExtent.setBeginPosition("BEGIN");
	temporalExtent.setEndPosition("END");

	Assert.assertEquals(temporalExtent.getBeginPosition(), "BEGIN");
	Assert.assertEquals(temporalExtent.getEndPosition(), "END");

	temporalExtent.setIndeterminateBeginPosition(TimeIndeterminateValueType.NOW);
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.UNKNOWN);

	Assert.assertEquals(temporalExtent.isBeginPositionIndeterminate(), true);
	Assert.assertEquals(temporalExtent.isEndPositionIndeterminate(), true);

	Assert.assertNotNull(temporalExtent.getBeginPosition());
	Assert.assertNull(temporalExtent.getEndPosition());

	Assert.assertEquals(temporalExtent.getIndeterminateBeginPosition(), TimeIndeterminateValueType.NOW);
	Assert.assertEquals(temporalExtent.getIndeterminateEndPosition(), TimeIndeterminateValueType.UNKNOWN);

	temporalExtent.setTimeInstantBegin("BEGIN");
	temporalExtent.setTimeInstantEnd("END");

	Assert.assertEquals(temporalExtent.getTimeInstantBegin(), "BEGIN");
	Assert.assertEquals(temporalExtent.getTimeInstantEnd(), "END");

	temporalExtent.setTimeInterval(2);
	Assert.assertEquals(temporalExtent.getTimeInterval(), new Double(2));

	temporalExtent.setTimePeriodId("TIMEPERIOD_ID");
	Assert.assertEquals(temporalExtent.getTimePeriodId(), "TIMEPERIOD_ID");

	try {
	    temporalExtent.setDuration("xxx");
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	}

	try {
	    temporalExtent.setDuration("");
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	}

	try {
	    temporalExtent.setDuration(null);
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	}

	temporalExtent.setDuration("P3D");
	Duration duration = temporalExtent.getDuration();
	Assert.assertEquals(duration.getDays(), 3);

	temporalExtent.setTimeIntervalUnit(TimeIntervalUnit.DAY);
	Assert.assertEquals(temporalExtent.getTimeIntervalUnit(), TimeIntervalUnit.DAY);

	temporalExtent.setBeginPosition("BEGIN");
	temporalExtent.setEndPosition("END");
    }

    @Override
    public void setProperties(TemporalExtent metadata) {
	metadata.setBeginPosition("2017-01-01T00:00:00Z");
	metadata.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	metadata.setId("my-id");
    }

    @Override
    public void checkProperties(TemporalExtent metadata) {
	Assert.assertEquals("2017-01-01T00:00:00Z", metadata.getBeginPosition());
	Assert.assertEquals(TimeIndeterminateValueType.NOW, metadata.getIndeterminateEndPosition());
	Assert.assertEquals("my-id", metadata.getId());

    }

    @Override
    public void clearProperties(TemporalExtent metadata) {
	metadata.setBeginPosition(null);
	metadata.setEndPosition(null);
	metadata.setIndeterminateBeginPosition(null);
	metadata.setIndeterminateEndPosition(null);
	metadata.setId(null);

    }

    @Override
    public void checkNullProperties(TemporalExtent metadata) {
	Assert.assertEquals(null, metadata.getBeginPosition());
	Assert.assertEquals(null, metadata.getIndeterminateEndPosition());
	Assert.assertEquals(null, metadata.getId());
    }

}
