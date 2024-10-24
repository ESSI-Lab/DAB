package eu.essi_lab.messages;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.web.WebRequest;

public class WebRequestTest {

    private WebRequest request;

    @Before
    public void init() {
	request = new WebRequest();
	this.request = Mockito.spy(request);

    }

    @Test
    public void viewTest1() throws Exception {
	Mockito.doReturn("opensearch/noview/example").when(request).getRequestPath();
	Assert.assertFalse(request.extractViewId().isPresent());
    }

    @Test
    public void viewTest2() throws Exception {
	String uuid = UUID.randomUUID().toString();
	Mockito.doReturn("view/" + uuid + "/opensearch").when(request).getRequestPath();
	assertEquals(uuid, request.extractViewId().get());
    }

    @Test
    public void viewTest3() throws Exception {
	String uuid = UUID.randomUUID().toString();
	Mockito.doReturn("view/" + uuid + "/opensearch/operation/").when(request).getRequestPath();
	assertEquals(uuid, request.extractViewId().get());
    }

    @Test
    public void viewTest4() throws Exception {
	String uuid1 = UUID.randomUUID().toString();
	String uuid2 = UUID.randomUUID().toString();
	Mockito.doReturn("view/" + uuid1 + "/opensearch/view/" + uuid2).when(request).getRequestPath();
	assertEquals(uuid1, request.extractViewId().get());
    }

}
