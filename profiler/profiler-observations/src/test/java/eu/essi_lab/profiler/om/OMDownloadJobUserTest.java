package eu.essi_lab.profiler.om;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.authorization.userfinder.KeycloakBearerTokenResolver;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.profiler.om.scheduling.OMSchedulerSetting;

/**
 * Bulk downloads of users authenticated with a bearer token: the download job runs on behalf of the user saved when the download was
 * requested, since the token is not stored and the user can not be found again from the request URL.
 */
public class OMDownloadJobUserTest {

    @Test
    public void userSurvivesTheJobSetting() {

	OMSchedulerSetting setting = new OMSchedulerSetting();
	setting.setUser(keycloakUser());

	// as the job setting is stored and read back by the scheduler
	OMSchedulerSetting restored = SettingUtils.downCast(new Setting(setting.getObject().toString()), OMSchedulerSetting.class);

	GSUser user = restored.getUser().get();

	Assert.assertEquals("4f1c-user", user.getIdentifier());
	Assert.assertEquals("his_central", user.getRole());
	Assert.assertEquals(KeycloakBearerTokenResolver.AUTH_PROVIDER, user.getAuthProvider());
	Assert.assertTrue(user.isEnabled());

	Assert.assertEquals("user@example.org", user.getStringPropertyValue("email").get());
	Assert.assertTrue(user.hasPermission("api"));
	Assert.assertTrue(user.hasPermission("downloads"));
	Assert.assertArrayEquals(new String[] { "his-central" }, user.getAllowedViews());
	Assert.assertEquals(Integer.valueOf(2000), user.getMaxDownloadSizeMB());
	Assert.assertEquals(Integer.valueOf(200), user.getMaxDownloadPartSizeMB());
    }

    @Test
    public void noUserByDefault() {

	Assert.assertTrue(new OMSchedulerSetting().getUser().isEmpty());

	//
	// jobs stored before the user was saved
	//
	OMSchedulerSetting setting = new OMSchedulerSetting();
	setting.removeOption("user");

	OMSchedulerSetting legacy = SettingUtils.downCast(new Setting(setting.getObject().toString()), OMSchedulerSetting.class);

	Assert.assertTrue(legacy.getUser().isEmpty());
    }

    @Test
    public void onlyBearerTokenUsersAreSaved() {

	Assert.assertTrue(KeycloakBearerTokenResolver.isBearerTokenUser(keycloakUser()));

	GSUser tokenUser = new GSUser();
	tokenUser.setIdentifier("his_central-568a4888");
	tokenUser.setRole("his_central");

	Assert.assertFalse(KeycloakBearerTokenResolver.isBearerTokenUser(tokenUser));
	Assert.assertFalse(KeycloakBearerTokenResolver.isBearerTokenUser(null));
    }

    @Test
    public void downloadToolsKeepTheJobUser() {

	GSUser user = keycloakUser();

	DataDownloaderTool data = new DataDownloaderTool();
	Assert.assertNull(data.getUser());
	data.setUser(user);
	Assert.assertSame(user, data.getUser());

	MetadataDownloaderTool metadata = new MetadataDownloaderTool();
	Assert.assertNull(metadata.getUser());
	metadata.setUser(user);
	Assert.assertSame(user, metadata.getUser());
    }

    private static GSUser keycloakUser() {

	GSUser user = new GSUser();
	user.setIdentifier("4f1c-user");
	user.setEnabled(true);
	user.setRole("his_central");
	user.setAuthProvider(KeycloakBearerTokenResolver.AUTH_PROVIDER);
	user.setPropertyValue("email", "user@example.org");
	user.setPropertyValue("username", "user@example.org");
	user.setPropertyValue("permissions", "api,downloads");
	user.setPropertyValue("allowedViews", "his-central");
	user.setPropertyValue("maxDownloadSizeMB", "2000");
	user.setPropertyValue("maxDownloadPartSizeMB", "200");

	return user;
    }
}
