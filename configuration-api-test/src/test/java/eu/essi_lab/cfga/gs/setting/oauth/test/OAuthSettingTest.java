package eu.essi_lab.cfga.gs.setting.oauth.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting.OAuthProvider;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class OAuthSettingTest {

    @Test
    public void test() {

	OAuthSetting setting = new OAuthSetting();

	initTest(setting);
	initTest(new OAuthSetting(setting.getObject()));
	initTest(new OAuthSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, OAuthSetting.class, true));

	//
	//
	//

	setting.selectProvider(OAuthProvider.FACEBOOK);

	setting.setClientId("clientId");
	setting.setClientSecret("clientSecret");
	setting.setAdminId("adminId");

	test(setting);
	test(new OAuthSetting(setting.getObject()));
	test(new OAuthSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, OAuthSetting.class, true));
    }

    /**
     * @param setting
     */
    private void test(OAuthSetting setting) {

	OAuthProvider selectedProvider = setting.getSelectedProvider();
	Assert.assertEquals(OAuthProvider.FACEBOOK, selectedProvider);

	Optional<String> adminId = setting.getAdminId();
	Assert.assertEquals("adminId", adminId.get());

	Optional<String> clientId = setting.getClientId();
	Assert.assertEquals("clientId", clientId.get());

	Optional<String> clientSecret = setting.getClientSecret();
	Assert.assertEquals("clientSecret", clientSecret.get());
    }

    /**
     * @param setting
     */
    private void initTest(OAuthSetting setting) {

	OAuthProvider selectedProvider = setting.getSelectedProvider();
	Assert.assertEquals(OAuthProvider.GOOGLE, selectedProvider);

	Optional<String> adminId = setting.getAdminId();
	Assert.assertFalse(adminId.isPresent());

	Optional<String> clientId = setting.getClientId();
	Assert.assertFalse(clientId.isPresent());

	Optional<String> clientSecret = setting.getClientSecret();
	Assert.assertFalse(clientSecret.isPresent());
    }
}
