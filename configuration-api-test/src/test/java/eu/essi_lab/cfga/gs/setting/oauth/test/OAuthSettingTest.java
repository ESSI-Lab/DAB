package eu.essi_lab.cfga.gs.setting.oauth.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.oauth.GoogleProviderSetting;
import eu.essi_lab.cfga.gs.setting.oauth.KeycloakProviderSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthProviderSetting;
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

	setting.selectProvider(OAuthProvider.KEYCLOAK);

	setting.getSelectedProviderSetting().setTokenURL("token");
	setting.getSelectedProviderSetting().setUserInfoURL("user");
	setting.getSelectedProviderSetting().setLoginURL("login");

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
	Assert.assertEquals(OAuthProvider.KEYCLOAK, selectedProvider);

	OAuthProviderSetting selectedProviderSetting = setting.getSelectedProviderSetting();
	Assert.assertEquals(selectedProviderSetting.getSettingClass(), KeycloakProviderSetting.class);

	Assert.assertEquals("login", selectedProviderSetting.getLoginURL().get());
	Assert.assertEquals("token", selectedProviderSetting.getTokenURL().get());
	Assert.assertEquals("user", selectedProviderSetting.getUserInfoURL().get());

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

	OAuthProviderSetting selectedProviderSetting = setting.getSelectedProviderSetting();
	Assert.assertEquals(selectedProviderSetting.getSettingClass(), GoogleProviderSetting.class);

	Assert.assertTrue(selectedProviderSetting.getLoginURL().isPresent());
	Assert.assertTrue(selectedProviderSetting.getTokenURL().isPresent());
	Assert.assertTrue(selectedProviderSetting.getUserInfoURL().isPresent());

	Optional<String> adminId = setting.getAdminId();
	Assert.assertFalse(adminId.isPresent());

	Optional<String> clientId = setting.getClientId();
	Assert.assertFalse(clientId.isPresent());

	Optional<String> clientSecret = setting.getClientSecret();
	Assert.assertFalse(clientSecret.isPresent());
    }
}
