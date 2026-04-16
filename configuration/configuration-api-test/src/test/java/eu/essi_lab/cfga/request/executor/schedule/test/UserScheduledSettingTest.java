/**
 * 
 */
package eu.essi_lab.cfga.request.executor.schedule.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.request.executor.schedule.UserScheduledSetting;

/**
 * @author Fabrizio
 */
public class UserScheduledSettingTest {

    @Test
    public void test() throws ClassNotFoundException {

	UserScheduledSetting setting = new UserScheduledSetting();

	test1(setting);
	test1(new UserScheduledSetting(setting.getObject()));
	test1(new UserScheduledSetting(setting.getObject().toString()));

	StorageInfo storageUri = new StorageInfo();
	storageUri.setIdentifier("configFolder");
	storageUri.setPassword("password");
	storageUri.setName("dataBaseName");
	storageUri.setUri("uri");
	storageUri.setUser("user");

	AccessMessage accessMessage = new AccessMessage();
	accessMessage.setDataBaseURI(storageUri);
	
	GSUser user = new GSUser();
	user.setIdentifier("userId");
	user.setRole("userRole");	
	accessMessage.setCurrentUser(user);
	accessMessage.setOnlineId("onlineId");

	setting.setRequestMessage(accessMessage);

	setting.setHandler("handler");
	setting.setMapper("mapper");
	setting.setFormatter("formatter");

	test2(setting, storageUri);
	test2(new UserScheduledSetting(setting.getObject()), storageUri);
	test2(new UserScheduledSetting(setting.getObject().toString()), storageUri);

	test3(setting, accessMessage);
	test3(new UserScheduledSetting(setting.getObject()), accessMessage);
	test3(new UserScheduledSetting(setting.getObject().toString()), accessMessage);

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setWebRequest(WebRequest.createGET("http://localhost"));
	discoveryMessage.setUserBond(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "3"));
	discoveryMessage.setScheduled(true);
	discoveryMessage.setUserJobStorageURI(storageUri);

	setting.setRequestMessage(discoveryMessage);

	test4(setting, discoveryMessage);
	test4(new UserScheduledSetting(setting.getObject()), discoveryMessage);
	test4(new UserScheduledSetting(setting.getObject().toString()), discoveryMessage);
    }

    /**
     * @param setting
     * @throws ClassNotFoundException
     */
    private void test1(UserScheduledSetting setting) throws ClassNotFoundException {

	String configurableType = setting.getConfigurableType();
	Assert.assertEquals("UserSchedulerWorker", configurableType);

	SchedulingGroup schedulingGroup = setting.getGroup();
	Assert.assertEquals(schedulingGroup, SchedulingGroup.ASYNCH_ACCESS);

	Assert.assertNotNull(setting.getIdentifier());

	Assert.assertNull(setting.getFormatter());
	Assert.assertNull(setting.getHandler());
	Assert.assertNull(setting.getMapper());
	Assert.assertNull(setting.getRequestMessage());
    }

    /**
     * @param setting
     */
    private void test2(UserScheduledSetting setting, StorageInfo storageUri) {

	Assert.assertEquals("formatter", setting.getFormatter());
	Assert.assertEquals("handler", setting.getHandler());
	Assert.assertEquals("mapper", setting.getMapper());
    }

    /**
     * @param setting
     * @param accessMessage
     */
    private void test3(UserScheduledSetting setting, AccessMessage accessMessage) {

	Assert.assertEquals(((AccessMessage) setting.getRequestMessage()).getDataBaseURI(), accessMessage.getDataBaseURI());

	Assert.assertEquals(((AccessMessage) setting.getRequestMessage()).getCurrentUser(), accessMessage.getCurrentUser());

	Assert.assertEquals(((AccessMessage) setting.getRequestMessage()).getOnlineId(), accessMessage.getOnlineId());
    }

    /**
     * @param setting
     * @param discoveryMessage
     */
    private void test4(UserScheduledSetting setting, DiscoveryMessage discoveryMessage) {

	Assert.assertEquals(setting.getRequestMessage().getWebRequest().getServletRequest().getRequestURI(),
		discoveryMessage.getWebRequest().getServletRequest().getRequestURI());

	Assert.assertEquals(((DiscoveryMessage) setting.getRequestMessage()).getUserBond().get(), discoveryMessage.getUserBond().get());

	Assert.assertEquals(((DiscoveryMessage) setting.getRequestMessage()).getScheduled(), discoveryMessage.getScheduled());

	Assert.assertEquals(((DiscoveryMessage) setting.getRequestMessage()).getUserJobStorageURI(),
		discoveryMessage.getUserJobStorageURI());

    }
}
