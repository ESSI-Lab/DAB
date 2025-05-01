/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.HarvestSchedulingRequest;
import eu.essi_lab.gssrv.rest.conf.HarvestSchedulingRequest.RepeatCount;
import eu.essi_lab.gssrv.rest.conf.HarvestSchedulingRequest.RepeatIntervalUnit;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest.SourceType;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class PutSourceRequestTest {

    @Test
    public void getSupportedPametersTest() {

	PutSourceRequest request = new PutSourceRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(8, parameters.size());

	Assert.assertEquals(
		Parameter.of(PutSourceRequest.SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, false),
		parameters.get(0));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_LABEL, ContentType.TEXTUAL, true), parameters.get(1));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_ENDPOINT, ContentType.TEXTUAL, true), parameters.get(2));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SERVICE_TYPE, ContentType.TEXTUAL, SourceType.class, true), parameters.get(3));

	Assert.assertEquals(Parameter.of(PutSourceRequest.HARVEST_SCHEDULING, false, HarvestSchedulingRequest.START_TIME,
		ContentType.ISO8601_DATE_TIME, false), parameters.get(4));
	Assert.assertEquals(Parameter.of(PutSourceRequest.HARVEST_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_COUNT,
		ContentType.TEXTUAL, RepeatCount.class, true), parameters.get(5));
	Assert.assertEquals(Parameter.of(PutSourceRequest.HARVEST_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_INTERVAL,
		ContentType.INTEGER, false), parameters.get(6));
	Assert.assertEquals(Parameter.of(PutSourceRequest.HARVEST_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT,
		ContentType.TEXTUAL, RepeatIntervalUnit.class, false), parameters.get(7));

    }

    @Test
    public void basicTest() {

	PutSourceRequest request = new PutSourceRequest();

	basicTest(request);

	JSONObject object = new JSONObject(request.toString());

	PutSourceRequest request2 = new PutSourceRequest(object);

	basicTest(request2);

	Assert.assertEquals(request, request2);
    }

    @Test
    public void basicTest2() {

	JSONObject object = new JSONObject();

	PutSourceRequest request = new PutSourceRequest(object);

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    /**
     * @param request
     */
    private void basicTest(PutSourceRequest request) {

	Assert.assertEquals("PutSource", request.getName());

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertEquals("sourceId", request.read(PutSourceRequest.SOURCE_ID).get());
	Assert.assertEquals("sourceLabel", request.read(PutSourceRequest.SOURCE_LABEL).get());
	Assert.assertEquals("http://localhost", request.read(PutSourceRequest.SOURCE_ENDPOINT).get());
	Assert.assertEquals(SourceType.WCS_111.getLabel(), request.read(PutSourceRequest.SERVICE_TYPE).get());

	request.validate();
    }

    @Test
    public void unknownParameterTest() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put("xxx", "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest1() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();

	System.out.println(request);
    }

    @Test
    public void validationTest1_2() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId-");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();
    }

    @Test
    public void validationTest1_3() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId-_");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();
    }

    @Test
    public void validationTest1_4() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, UUID.randomUUID().toString());
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();
    }

    @Test
    public void validationTest2() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId!");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest2_2() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId#");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest4() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();
    }

    @Test
    public void validationTest5() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest6() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest7() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest8() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, "xxx", "value");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest9() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.ONCE.getLabel());

	request.validate();
    }

    @Test
    public void validationTest10() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest11() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "10");
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.toString());

	request.validate();
    }

    @Test
    public void validationTest12() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "xxx");
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.toString());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest13() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "10");
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest14() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "10");
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.toString());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.START_TIME, "xx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest15() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "10");
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.toString());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());

	request.validate();
    }

    @Test
    public void validationTest16() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "10");
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.toString());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.START_TIME, ISO8601DateTimeUtils.getISO8601DateTime());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest17() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put("xxx", HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest18() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SERVICE_TYPE, SourceType.WCS_111.getLabel());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "10");
	request.put(PutSourceRequest.HARVEST_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.DAYS.toString());

	request.put(PutSourceRequest.HARVEST_SCHEDULING, "xxx", ISO8601DateTimeUtils.getISO8601DateTime());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }
}
