/**
 *
 */
package eu.essi_lab.cfga.rest.test;

import eu.essi_lab.cfga.gs.task.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.cfga.rest.source.HarvestSchedulingRequest.*;
import eu.essi_lab.cfga.rest.task.*;
import eu.essi_lab.lib.utils.*;
import org.json.*;
import org.junit.*;

import java.util.*;

/**
 * @author boldrini
 */
public class CustomTaskRequestsTest {

    @Test
    public void namesTest() {

	Assert.assertEquals("PutCustomTask", ConfigRequest.computeName(PutCustomTaskRequest.class));
	Assert.assertEquals("EditCustomTask", ConfigRequest.computeName(EditCustomTaskRequest.class));
	Assert.assertEquals("RemoveCustomTask", ConfigRequest.computeName(RemoveCustomTaskRequest.class));
	Assert.assertEquals("ListCustomTasks", ConfigRequest.computeName(ListCustomTasksRequest.class));
	Assert.assertEquals("CustomTaskScheduling", ConfigRequest.computeName(CustomTaskSchedulingRequest.class));
	Assert.assertEquals("CustomTaskUnscheduling", ConfigRequest.computeName(CustomTaskUnschedulingRequest.class));
    }

    @Test
    public void putValidationTest() {

	PutCustomTaskRequest request = new PutCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_NAME, DefaultCustomTask.getTaskName());
	request.put(PutCustomTaskRequest.TASK_DESCRIPTION, "desc");
	request.put(PutCustomTaskRequest.TASK_OPTIONS, "a=b\nc=d");
	request.put(PutCustomTaskRequest.EMAIL_RECIPIENTS, "a@b.it, c@d.it");

	request.validate();

	// with id

	request.put(PutCustomTaskRequest.TASK_ID, "task-1");

	request.validate();
    }

    @Test
    public void putMissingTaskNameTest() {

	PutCustomTaskRequest request = new PutCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_DESCRIPTION, "desc");

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void putUnknownTaskNameTest() {

	PutCustomTaskRequest request = new PutCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_NAME, "xxx");

	IllegalArgumentException ex = Assert.assertThrows(IllegalArgumentException.class, request::validate);

	Assert.assertTrue(ex.getMessage().contains(DefaultCustomTask.getTaskName()));
    }

    @Test
    public void putInvalidIdTest() {

	PutCustomTaskRequest request = new PutCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_NAME, DefaultCustomTask.getTaskName());
	request.put(PutCustomTaskRequest.TASK_ID, "invalid id!");

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void putWithSchedulingTest() {

	PutCustomTaskRequest request = new PutCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_ID, "task-1");
	request.put(PutCustomTaskRequest.TASK_NAME, DefaultCustomTask.getTaskName());

	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.START_TIME,
		ISO8601DateTimeUtils.getISO8601DateTime("Europe/Berlin"));
	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "2");
	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.HOURS.getLabel());

	request.validate();

	CustomTaskSchedulingRequest schedulingRequest = request.getSchedulingRequest();

	schedulingRequest.validate();

	Assert.assertEquals("task-1", schedulingRequest.readString(PutCustomTaskRequest.TASK_ID).get());
	Assert.assertEquals("2", schedulingRequest.readString(HarvestSchedulingRequest.REPEAT_INTERVAL).get());
    }

    @Test
    public void putWithInconsistentSchedulingTest() {

	PutCustomTaskRequest request = new PutCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_NAME, DefaultCustomTask.getTaskName());

	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void putWithZeroIntervalTest() {

	PutCustomTaskRequest request = new PutCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_NAME, DefaultCustomTask.getTaskName());

	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.INDEFINITELY.getLabel());
	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL, "0");
	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, RepeatIntervalUnit.HOURS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void editValidationTest() {

	EditCustomTaskRequest request = new EditCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_ID, "task-1");

	// at least one parameter to edit is required
	Assert.assertThrows(IllegalArgumentException.class, request::validate);

	request.put(PutCustomTaskRequest.TASK_OPTIONS, "");

	request.validate();

	request.put(PutCustomTaskRequest.TASK_NAME, "xxx");

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void editMissingIdTest() {

	EditCustomTaskRequest request = new EditCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_DESCRIPTION, "desc");

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void editSchedulingNotSupportedTest() {

	EditCustomTaskRequest request = new EditCustomTaskRequest();

	request.put(PutCustomTaskRequest.TASK_ID, "task-1");
	request.put(PutCustomTaskRequest.TASK_DESCRIPTION, "desc");
	request.put(PutCustomTaskRequest.TASK_SCHEDULING, HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.ONCE.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void schedulingValidationTest() {

	CustomTaskSchedulingRequest request = new CustomTaskSchedulingRequest();

	request.put(PutCustomTaskRequest.TASK_ID, "task-1");
	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.ONCE.getLabel());

	request.validate();

	request.put(HarvestSchedulingRequest.REPEAT_INTERVAL, "1");

	// interval not allowed with repeat count 'Once'
	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void schedulingMissingIdTest() {

	CustomTaskSchedulingRequest request = new CustomTaskSchedulingRequest();

	request.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.ONCE.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    public void removeAndUnschedulingValidationTest() {

	RemoveCustomTaskRequest remove = new RemoveCustomTaskRequest();

	Assert.assertThrows(IllegalArgumentException.class, remove::validate);

	remove.put(PutCustomTaskRequest.TASK_ID, "task-1");

	remove.validate();

	CustomTaskUnschedulingRequest unscheduling = new CustomTaskUnschedulingRequest();

	Assert.assertThrows(IllegalArgumentException.class, unscheduling::validate);

	unscheduling.put(PutCustomTaskRequest.TASK_ID, "task-1");

	unscheduling.validate();
    }

    @Test
    public void listValidationTest() {

	ListCustomTasksRequest request = new ListCustomTasksRequest();

	request.validate();

	request.put(PutCustomTaskRequest.TASK_ID, "task-1, task-2");

	request.validate();

	Assert.assertEquals(List.of("task-1", "task-2"), request.readStrings(PutCustomTaskRequest.TASK_ID));
    }

    @Test
    public void buildAndEditSettingTest() {

	PutCustomTaskRequest put = new PutCustomTaskRequest();

	put.put(PutCustomTaskRequest.TASK_ID, "task-1");
	put.put(PutCustomTaskRequest.TASK_NAME, DefaultCustomTask.getTaskName());
	put.put(PutCustomTaskRequest.TASK_DESCRIPTION, "desc");
	put.put(PutCustomTaskRequest.TASK_OPTIONS, "a=b");
	put.put(PutCustomTaskRequest.EMAIL_RECIPIENTS, "a@b.it, c@d.it");

	CustomTaskSetting setting = CustomTaskSettingUtils.build(put);

	Assert.assertEquals("task-1", setting.getIdentifier());
	Assert.assertEquals(DefaultCustomTask.getTaskName(), setting.getSelectedTaskName());
	Assert.assertEquals("desc", setting.getTaskDescription());
	Assert.assertEquals("a=b", setting.getTaskOptions().get());
	Assert.assertEquals(List.of("a@b.it", "c@d.it"), setting.getEmailRecipients());
	Assert.assertFalse(setting.getScheduling().isEnabled());

	//
	// editing description and clearing options and recipients
	//

	EditCustomTaskRequest edit = new EditCustomTaskRequest();

	edit.put(PutCustomTaskRequest.TASK_ID, "task-1");
	edit.put(PutCustomTaskRequest.TASK_DESCRIPTION, "new desc");
	edit.put(PutCustomTaskRequest.TASK_OPTIONS, "");
	edit.put(PutCustomTaskRequest.EMAIL_RECIPIENTS, "");

	edit.validate();

	CustomTaskSetting edited = CustomTaskSettingUtils.build(edit, setting);

	Assert.assertEquals("task-1", edited.getIdentifier());
	Assert.assertEquals(DefaultCustomTask.getTaskName(), edited.getSelectedTaskName());
	Assert.assertEquals("new desc", edited.getTaskDescription());
	Assert.assertTrue(edited.getTaskOptions().isEmpty());
	Assert.assertTrue(edited.getEmailRecipients().isEmpty());

	// the original setting is not modified
	Assert.assertEquals("desc", setting.getTaskDescription());
    }

    @Test
    public void fromJSONTest() {

	JSONObject object = new JSONObject();
	object.put("request", "PutCustomTask");

	JSONObject parameters = new JSONObject();
	object.put("parameters", parameters);

	parameters.put(PutCustomTaskRequest.TASK_NAME, DefaultCustomTask.getTaskName());

	JSONObject scheduling = new JSONObject();
	scheduling.put(HarvestSchedulingRequest.REPEAT_COUNT, RepeatCount.ONCE.getLabel());

	parameters.put(PutCustomTaskRequest.TASK_SCHEDULING, scheduling);

	PutCustomTaskRequest request = new PutCustomTaskRequest(object);

	request.validate();

	Assert.assertTrue(request.readCompositeParameters().contains(PutCustomTaskRequest.TASK_SCHEDULING));
    }
}
