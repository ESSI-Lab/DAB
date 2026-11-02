package eu.essi_lab.cfga.scheduler.test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.scheduler.impl.MySQLConnectionManager;
import eu.essi_lab.cfga.scheduler.impl.PersistentJobStoreScheduler;

public class PersistentJobStoreSchedulerJobStatusTest {

    /**
     * A corrupted JOB_STATUS row must be skipped, not break the whole list
     *
     * @throws Exception
     */
    @Test
    public void malformedStatusRowIsSkippedTest() throws Exception {

	ResultSet rs = mock(ResultSet.class);

	when(rs.next()).thenReturn(true, true, true, true, true, false);

	when(rs.getString("JOB_NAME")).thenReturn("job1", "job2", "job3", "job4", "job5");

	when(rs.getString("JOB_STATUS")).thenReturn(//
		"{\"settingId\":\"id1\"}", //
		"{\"settingId\":\"id2\",\"message\":\"unterminated", //
		null, //
		"", //
		"{\"settingId\":\"id5\"}");

	MySQLConnectionManager manager = mock(MySQLConnectionManager.class);
	when(manager.execQuery(anyString())).thenReturn(rs);

	PersistentJobStoreScheduler scheduler = new PersistentJobStoreScheduler(null, null, manager);

	List<SchedulerJobStatus> list = scheduler.getJobStatuslist();

	Assert.assertEquals(2, list.size());
	Assert.assertEquals("id1", list.get(0).getSettingId());
	Assert.assertEquals("id5", list.get(1).getSettingId());

	verify(manager).close(rs);
    }

    /**
     * The status must be bound as a parameter, so quotes and JSON escape sequences reach the DB unchanged
     *
     * @throws Exception
     */
    @Test
    public void setJobStatusIsParameterizedTest() throws Exception {

	JSONObject object = new JSONObject();
	object.put("settingId", "id1");
	object.put("message", "it's a \"quoted\" message\nwith a C:\\path");

	SchedulerJobStatus status = new SchedulerJobStatus(object);

	MySQLConnectionManager manager = mock(MySQLConnectionManager.class);

	PersistentJobStoreScheduler scheduler = new PersistentJobStoreScheduler(null, null, manager);

	scheduler.setJobStatus(status);

	verify(manager).execUpdate(//
		eq("UPDATE GS_QRTZ_JOB_DETAILS SET JOB_STATUS = ? WHERE JOB_NAME = ?"), //
		eq(status.toString()), //
		eq(SchedulerUtils.createJobKeyName("id1")));

	// the stored value can be parsed back as it was
	Assert.assertEquals("it's a \"quoted\" message\nwith a C:\\path",
		new SchedulerJobStatus(new JSONObject(status.toString())).getObject().getString("message"));
    }
}
