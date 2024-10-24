package eu.essi_lab.request.executor.discover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.request.executor.schedule.UserScheduledWorkerJobExecutor;

public class ScheduleUserJobExecutor1Test {

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Before
    public void before() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();
	
	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Test
    public void testInputs1() {
	try {
	    UserScheduledWorkerJobExecutor executor = new UserScheduledWorkerJobExecutor<>();
	    DiscoveryMessage message = new DiscoveryMessage();
	    StorageInfo uri = new StorageInfo("http://localhost");
	    uri.setName("dataBaseName");
	    message.setUserJobStorageURI(uri);
	    executor.retrieve(message);
	    fail("Should have thrown exception");
	} catch (GSException e) {
	    assertEquals(UserScheduledWorkerJobExecutor.EXCEPTION_MISSING_CLASSES, e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testInputs2() {
	try {
	    UserScheduledWorkerJobExecutor executor = new UserScheduledWorkerJobExecutor<>();
	    DiscoveryMessage message = new DiscoveryMessage();
	    StorageInfo uri = new StorageInfo("http://localhost");
	    uri.setName("dataBaseName");
	    message.setUserJobStorageURI(uri);
	    executor.setWorkerHandler("", "class2", "class3");
	    executor.retrieve(message);
	    fail("Should have thrown exception");
	} catch (GSException e) {
	    assertEquals(UserScheduledWorkerJobExecutor.EXCEPTION_MISSING_CLASSES, e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testInputs3() {
	try {
	    UserScheduledWorkerJobExecutor executor = new UserScheduledWorkerJobExecutor<>();
	    DiscoveryMessage message = new DiscoveryMessage();
	    StorageInfo uri = new StorageInfo("http://localhost");
	    uri.setName("dataBaseName");
	    message.setUserJobStorageURI(uri);
	    executor.setWorkerHandler("class1", "", "class3");
	    executor.retrieve(message);
	    fail("Should have thrown exception");
	} catch (GSException e) {
	    assertEquals(UserScheduledWorkerJobExecutor.EXCEPTION_MISSING_CLASSES, e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testInputs4() {
	try {
	    UserScheduledWorkerJobExecutor executor = new UserScheduledWorkerJobExecutor<>();
	    DiscoveryMessage message = new DiscoveryMessage();
	    StorageInfo uri = new StorageInfo("http://localhost");
	    uri.setName("dataBaseName");
	    message.setUserJobStorageURI(uri);
	    executor.setWorkerHandler("class1", "class2", "");
	    executor.retrieve(message);
	    fail("Should have thrown exception");
	} catch (GSException e) {
	    assertEquals(UserScheduledWorkerJobExecutor.EXCEPTION_MISSING_CLASSES, e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testInputs5() {
	try {
	    UserScheduledWorkerJobExecutor executor = new UserScheduledWorkerJobExecutor<>();
	    DiscoveryMessage message = new DiscoveryMessage();
	    executor.setWorkerHandler("class1", "class2", "class3");
	    executor.retrieve(message);
	    fail("Should have thrown exception");
	} catch (GSException e) {
	    assertEquals(UserScheduledWorkerJobExecutor.EXCEPTION_MISSING_STORAGE_URI_COMPLEX, e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testInputs6() {
	try {
	    UserScheduledWorkerJobExecutor executor = new UserScheduledWorkerJobExecutor<>();
	    DiscoveryMessage message = new DiscoveryMessage();
	    StorageInfo uri = new StorageInfo();
	    uri.setName("dataBaseName");
	    message.setUserJobStorageURI(uri);
	    executor.setWorkerHandler("class1", "class2", "class3");
	    executor.retrieve(message);
	    fail("Should have thrown exception");
	} catch (GSException e) {
	    assertEquals(UserScheduledWorkerJobExecutor.EXCEPTION_MISSING_STORAGE_URI, e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testInputs7() {
	try {
	    UserScheduledWorkerJobExecutor executor = new UserScheduledWorkerJobExecutor<>();
	    DiscoveryMessage message = new DiscoveryMessage();
	    StorageInfo uri = new StorageInfo("http://localhost");
	    message.setUserJobStorageURI(uri);
	    executor.setWorkerHandler("class1", "class2", "class3");
	    executor.retrieve(message);
	    fail("Should have thrown exception");
	} catch (GSException e) {
	    assertEquals(UserScheduledWorkerJobExecutor.EXCEPTION_MISSING_STORAGE_NAME, e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testInputs10() {
	try {
	    UserScheduledWorkerJobExecutor noInitExecutor = new UserScheduledWorkerJobExecutor();
	    DiscoveryMessage message = new DiscoveryMessage();
	    StorageInfo uri = new StorageInfo("http://localhost");
	    uri.setName("dataBaseName");
	    message.setUserJobStorageURI(uri);
	    noInitExecutor.setWorkerHandler("class1", "class2", "class3");
	    noInitExecutor.retrieve(message);
	    // fail("Should have thrown exception");
	} catch (GSException e) {
	    fail("Should not have thrown exception");
	}
    }
}
