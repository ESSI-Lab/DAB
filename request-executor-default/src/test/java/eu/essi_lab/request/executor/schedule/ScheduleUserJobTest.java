//package eu.essi_lab.request.executor.schedule;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.fail;
//
//import java.util.HashMap;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//
//import eu.essi_lab.jobs.listener.GSJobListener;
//import eu.essi_lab.messages.DiscoveryMessage;
//import eu.essi_lab.messages.web.WebRequest;
//import eu.essi_lab.model.StorageUri;
//import eu.essi_lab.model.exceptions.ErrorInfo;
//import eu.essi_lab.model.exceptions.GSException;
//
//public class ScheduleUserJobTest {
//
//    @Rule
//    public ExpectedException exceptions = ExpectedException.none();
//    private UserSchedulerWorker job;
//    private HashMap<String, Object> map;
//
//    @Before
//    public void init() {
//	this.job = new UserSchedulerWorker(null);
//	this.map = new HashMap<String, Object>();
//    }
//
//    @Test
//    public void test1() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    message.setWebRequest(WebRequest.create("http://localhost"));
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	} catch (GSException e) {
//	    throw e;
//	}
//    }
//
//    @Test
//    public void test2() throws GSException {
//	try {
//	    // map.put(ScheduleUserJob.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_CLASS, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test3() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    // map.put(ScheduleUserJob.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_CLASS, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test4() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    // map.put(ScheduleUserJob.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_CLASS, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test5() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, "fake");
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_INSTANTIATION_HANDLER, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test6() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, "fake");
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_INSTANTIATION_MAPPER, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test7() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, "fake");
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_INSTANTIATION_FORMATTER, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test8() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    // map.put(ScheduleUserJob.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_MESSAGE, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test9() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    // map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_EXECUTION_ID_HINT, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test10() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    url.setStorageName("storageName");
//	    // message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_RESULT_STORAGE_URI_COMPLEX, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test11() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri();
//	    url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_RESULT_STORAGE_URI, info.getErrorId());
//	}
//    }
//
//    @Test
//    public void test12() throws GSException {
//	try {
//	    map.put(UserSchedulerWorker.HANDLER, FakeProfilerHandler.class.getName());
//	    map.put(UserSchedulerWorker.FORMATTER, FakeMessageResponseFormatter.class.getName());
//	    map.put(UserSchedulerWorker.MAPPER, FakeMessageResponseMapper.class.getName());
//	    map.put(GSJobListener.GS_JOB_STATUS_HINT_EXECUTION_ID, "id");
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    StorageUri url = new StorageUri("http://localhost");
//	    // url.setStorageName("storageName");
//	    message.setUserJobStorageURI(url);
//	    map.put(UserSchedulerWorker.MESSAGE, message);
//	    job.run(map, false, null);
//	    fail("Expected exception");
//	} catch (GSException e) {
//	    ErrorInfo info = e.getErrorInfoList().get(0);
//	    assertEquals(UserSchedulerWorker.EXCEPTION_MISSING_RESULT_STORAGE_URI_STORAGE_NAME, info.getErrorId());
//	}
//    }
//
//}
