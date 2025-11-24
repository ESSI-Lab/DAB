package eu.essi_lab.messages.test;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.DiscoveryMessage.EiffelAPIDiscoveryOption;
import eu.essi_lab.messages.GSMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;

public class GSMessageTest {

    @Test
    public void testMessageHeader() {

	GSMessage gsMessage = new GSMessage();

	String paramName = "p";

	gsMessage.getHeader().add(new GSProperty<>(paramName, "value1"));

	String param = gsMessage.getHeader().get(paramName, String.class);
	Assert.assertEquals(param, "value1");

	boolean add = gsMessage.getHeader().add(new GSProperty<>(paramName, "value2"));

	// a property with the given name already exists, so the add operation fails
	Assert.assertEquals(add, false);
	// and the value is still the same
	Assert.assertEquals(param, "value1");

	gsMessage.getHeader().remove(paramName);
	param = gsMessage.getHeader().get(paramName, String.class);

	Assert.assertNull(param);

	Integer integer = gsMessage.getHeader().get(paramName, Integer.class);
	Assert.assertNull(integer);

	Page page = gsMessage.getHeader().get(paramName, Page.class);
	Assert.assertNull(page);

	add = gsMessage.getHeader().add(new GSProperty<>(paramName, page));
	// a property with the given name already exists, but with different types
	Assert.assertEquals(add, true);
    }

    @Test
    public void testMessagePayload() {

	GSMessage gsMessage = new GSMessage();

	String paramName = "p";

	gsMessage.getPayload().add(new GSProperty<>(paramName, "value1"));

	String param = gsMessage.getPayload().get(paramName, String.class);
	Assert.assertEquals(param, "value1");

	boolean add = gsMessage.getPayload().add(new GSProperty<>(paramName, "value2"));

	// a property with the given name already exists, so the add operation fails
	Assert.assertEquals(add, false);
	// and the value is still the same
	Assert.assertEquals(param, "value1");

	gsMessage.getPayload().remove(paramName);
	param = gsMessage.getPayload().get(paramName, String.class);

	Assert.assertNull(param);

	Integer integer = gsMessage.getPayload().get(paramName, Integer.class);
	Assert.assertNull(integer);

	Page page = gsMessage.getPayload().get(paramName, Page.class);
	Assert.assertNull(page);

	add = gsMessage.getPayload().add(new GSProperty<>(paramName, page));
	// a property with the given name already exists, but with different types
	Assert.assertEquals(add, true);
    }

    @Test
    public void DiscoveryMessageTest() {

	DiscoveryMessage message = new DiscoveryMessage();

	Assert.assertTrue(message.isDataFolderCheckEnabled());
	Assert.assertFalse(message.getView().isPresent());
	Assert.assertNull(message.getDataBaseURI());
	Assert.assertNull(message.getNormalizedBond());
	Assert.assertNull(message.getPage());
	Assert.assertFalse(message.getCurrentUser().isPresent());
	Assert.assertNull(message.getNormalizedBond());
	Assert.assertFalse(message.getUserBond().isPresent());
	Assert.assertNull(message.getPermittedBond());
	Assert.assertNull(message.getWebRequest());
	Assert.assertFalse(message.getIteratedWorkflow().isPresent());
	Assert.assertFalse(message.getResultSetMapperThreadsCount().isPresent());

	Assert.assertFalse(message.getEiffelAPIDiscoveryOption().isPresent());

	Assert.assertEquals(0, message.getSources().size());
	Assert.assertFalse(message.isDeletedIncluded());
	Assert.assertNotNull(message.getException());
	Assert.assertEquals(DiscoveryMessage.DEFAULT_MAX_TERM_FREQUENCY_MAP_ITEMS, message.getMaxFrequencyMapItems());
	Assert.assertNotNull(message.getRankingStrategy());

	// view id
	String viewId = UUID.randomUUID().toString();
	message.setView(new View(viewId));

	Assert.assertEquals(message.getView().get().getId(), viewId);

	// page
	Page page = new Page();
	message.setPage(page);

	Assert.assertEquals(page, message.getPage());

	// user
	GSUser gsUser = new GSUser();
	gsUser.setIdentifier("useremail");
	message.setCurrentUser(gsUser);

	Assert.assertEquals(gsUser, message.getCurrentUser().get());

	// include deleted
	message.setIncludeDeleted(true);
	Assert.assertTrue(message.isDeletedIncluded());

	message.setIncludeDeleted(false);
	Assert.assertFalse(message.isDeletedIncluded());

	// max term frequency items
	message.setMaxFrequencyMapItems(1);
	Assert.assertEquals(1, message.getMaxFrequencyMapItems());

	// web request
	WebRequest request = WebRequest.createGET("http://request?pippo");

	message.setWebRequest(request);
	Assert.assertEquals("pippo", message.getWebRequest().getQueryString());

	// storage uri
	StorageInfo storageUri = new StorageInfo("url");

	message.setDataBaseURI(storageUri);
	Assert.assertEquals("url", message.getDataBaseURI().getUri());

	// deleted
	message.setIncludeDeleted(true);
	Assert.assertTrue(message.isDeletedIncluded());

	// max terms
	message.setMaxFrequencyMapItems(15);
	Assert.assertEquals(15, message.getMaxFrequencyMapItems());

	// exc
	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setCaller(GSMessageTest.class);
	message.getException().getErrorInfoList().add(errorInfo);
	Assert.assertEquals(GSMessageTest.class, message.getException().getErrorInfoList().getFirst().getCaller());

	message.disableDataFolderCheck();
	Assert.assertFalse(message.isDataFolderCheckEnabled());

	// iteration mode
	message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	Assert.assertEquals(IterationMode.FULL_RESPONSE, message.getIteratedWorkflow().get());

	// eiffel discovery option
	message.enableEiffelAPIDiscoveryOption(EiffelAPIDiscoveryOption.FILTER_AND_SORT);
	Assert.assertEquals(EiffelAPIDiscoveryOption.FILTER_AND_SORT, message.getEiffelAPIDiscoveryOption().get());

	// result set mapper threads
	message.setResultSetMapperThreadsCount(10);
	Assert.assertEquals(Integer.valueOf(10), message.getResultSetMapperThreadsCount().get());

    }

}
