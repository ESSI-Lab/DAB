/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.HarvestSourceRequest;
import eu.essi_lab.gssrv.rest.conf.HarvestSourceRequest.RepeatIntervalUnit;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class HarvestSourceRequestTest {

    @Test
    public void getSupportedPametersTest() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(4, parameters.size());

	Assert.assertEquals(
		Parameter.of(HarvestSourceRequest.SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true),
		parameters.get(0));
	Assert.assertEquals(Parameter.of(HarvestSourceRequest.START_TIME, ContentType.ISO8601_DATE_TIME, false), parameters.get(1));
	Assert.assertEquals(Parameter.of(HarvestSourceRequest.REPEAT_INTERVAL, ContentType.INTEGER, false), parameters.get(2));
	Assert.assertEquals(Parameter.of(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, ContentType.TEXTUAL, RepeatIntervalUnit.class, false),
		parameters.get(3));
    }

    @Test
    public void validationTest() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	request.validate();
    }

    @Test
    public void validationTest2() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, "xxx");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3_1() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, "1980-01-01");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3_2() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, "1980-01-01T");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3_3() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, "1980-01-01T00:00:00");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	request.validate();
    }

    @Test
    public void validationTest3_4() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, "1980-01-01T00:00:00Z");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	request.validate();
    }

    @Test
    public void validationTest4() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	request.validate();
    }

    @Test
    public void validationTest4_1() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.getLabel());

	request.validate();
    }

    @Test
    public void validationTest4_2() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.MINUTES.getLabel());

	request.validate();
    }

    @Test
    public void validationTest4_3() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.MONTHS.getLabel());

	request.validate();
    }

    @Test
    public void validationTest5() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");

	request.validate();
    }

    @Test
    public void validationTest6() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");

	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest7() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");

	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest8() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "xxx");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest8_1() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "0.5");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.WEEKS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest9() {

	HarvestSourceRequest request = new HarvestSourceRequest();

	request.put(HarvestSourceRequest.SOURCE_ID, "sourceId");
	request.put(HarvestSourceRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());
	request.put(HarvestSourceRequest.REPEAT_INTERVAL, "1");
	request.put(HarvestSourceRequest.REPEAT_INTERVAL_UNIT, "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }
}
