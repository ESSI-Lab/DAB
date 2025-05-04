/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.gssrv.rest.conf.requests.HarvestSchedulingRequest;
import eu.essi_lab.gssrv.rest.conf.requests.HarvestSchedulingRequest.RepeatCount;
import eu.essi_lab.gssrv.rest.conf.requests.HarvestSchedulingRequest.RepeatIntervalUnit;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class HarvestSchedulingRequestTest {

    @Test
    public void getSupportedPametersTest() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(5, parameters.size());

	Assert.assertEquals(Parameter.of(HarvestSchedulingRequest.SOURCE_ID, ContentType.TEXTUAL,
		InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, true), parameters.get(0));
	Assert.assertEquals(Parameter.of(HarvestSchedulingRequest.START_TIME, ContentType.ISO8601_DATE_TIME, false), parameters.get(1));

	Assert.assertEquals(Parameter.of(HarvestSchedulingRequest.REPEAT_COUNT, ContentType.TEXTUAL, RepeatCount.class, true),
		parameters.get(2));

	Assert.assertEquals(Parameter.of(HarvestSchedulingRequest.REPEAT_INTERVAL, ContentType.INTEGER, false), parameters.get(3));
	Assert.assertEquals(
		Parameter.of(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, ContentType.TEXTUAL, RepeatIntervalUnit.class, false),
		parameters.get(4));
    }

    @Test
    public void validationTest() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTimeNoUTC());
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	request.validate();

	System.out.println(request);
    }

    @Test
    public void validationTest_1() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest_1_2() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");

	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest2() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest2_1() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, "xxx");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3_1() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, "1980-01-01");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3_2() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, "1980-01-01T");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3_3() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, "1980-01-01T00:00:00");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	request.validate();
    }

    @Test
    public void validationTest3_4() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, "1980-01-01T00:00:00Z");

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3_5() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, "1980-01-01T00:00:00Z");

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.ONCE.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest4() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	request.validate();
    }

    @Test
    public void validationTest4_1() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	request.validate();
    }

    @Test
    public void validationTest4_2() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.MINUTES.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	request.validate();
    }

    @Test
    public void validationTest4_3() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.MONTHS.getLabel());
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	request.validate();
    }

    @Test
    public void validationTest5() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.ONCE.getLabel());

	request.validate();
    }

    @Test
    public void validationTest5_1() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest6() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest7() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest8() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "xxx");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest8_1() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "0.5");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest9() {

	HarvestSchedulingRequest request = new HarvestSchedulingRequest();

	request.put(HarvestSchedulingRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }
}
