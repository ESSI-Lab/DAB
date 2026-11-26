package eu.essi_lab.cfga.gs.setting.systemsetting.test;

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

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.database.SecondaryUsersDatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SecondaryUsersDatabaseSetting.DownloadTier;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

public class SecondaryUsersDatabaseSettingTest {

    private static final String SECONDARY_SETTING_ID = "secUsersDatabase";
    private static final String DB_SETTING_ID = "secUsersDbSetting";

    @Test
    public void defaults() {

	SecondaryUsersDatabaseSetting setting = new SecondaryUsersDatabaseSetting();

	Assert.assertEquals("dab", setting.getClientId().get());
	Assert.assertEquals(List.of("api", "downloads"), setting.getAllowedPermissions());
	Assert.assertTrue(setting.getUsersRole().isEmpty());
	Assert.assertTrue(setting.getDownloadTiers().isEmpty());
	Assert.assertTrue(setting.getLoginClientId().isEmpty());
	Assert.assertTrue(setting.getScriptsClientId().isEmpty());

	//
	// no credentials and no type
	//
	Setting dbSetting = setting.getSetting(DB_SETTING_ID).get();

	Assert.assertTrue(dbSetting.getOption("dbUser").isEmpty());
	Assert.assertTrue(dbSetting.getOption("dbPassword").isEmpty());
	Assert.assertTrue(dbSetting.getOption("dbType").isEmpty());

	Assert.assertNull(setting.getDatabaseUser());
	Assert.assertNull(setting.getDatabasePassword());
	Assert.assertTrue(setting.getDatabaseType().isEmpty());
    }

    @Test
    public void issuerAndKeysUrl() {

	SecondaryUsersDatabaseSetting setting = new SecondaryUsersDatabaseSetting();

	setting.setDatabaseUri(" https://keycloak.example.org:8085/ ");
	setting.setDatabaseName("users");

	Assert.assertEquals("https://keycloak.example.org:8085", setting.getKeycloakUrl());
	Assert.assertEquals("https://keycloak.example.org:8085/realms/users", setting.getIssuer());
	Assert.assertEquals("https://keycloak.example.org:8085/realms/users/protocol/openid-connect/certs", setting.getJwksUrl());
    }

    @Test
    public void webClients() {

	SecondaryUsersDatabaseSetting setting = new SecondaryUsersDatabaseSetting();

	setting.setLoginClientId(" his-central-portal ");
	setting.setScriptsClientId("dab-cli");

	Assert.assertEquals("his-central-portal", setting.getLoginClientId().get());
	Assert.assertEquals("dab-cli", setting.getScriptsClientId().get());

	setting.setLoginClientId(" ");

	Assert.assertTrue(setting.getLoginClientId().isEmpty());
    }

    @Test
    public void emptyValuesAreIgnored() {

	SecondaryUsersDatabaseSetting setting = new SecondaryUsersDatabaseSetting();

	setting.setUsersRole("   ");
	setting.setClientId(" ");
	setting.setAllowedPermissions(" , ");

	Assert.assertTrue(setting.getUsersRole().isEmpty());
	Assert.assertTrue(setting.getClientId().isEmpty());
	Assert.assertTrue(setting.getAllowedPermissions().isEmpty());

	setting.setAllowedPermissions(" api , downloads,, ");

	Assert.assertEquals(List.of("api", "downloads"), setting.getAllowedPermissions());
    }

    @Test
    public void downloadTiers() {

	SecondaryUsersDatabaseSetting setting = new SecondaryUsersDatabaseSetting();

	setting.setDownloadTiers("standard=100,20\n large = 2000 , 200 \r\n\nnopart=50\nwrong\nnegative=-1,5\nletters=a,b\nnegpart=5,-1");

	Map<String, DownloadTier> tiers = setting.getDownloadTiers();

	Assert.assertEquals(List.of("standard", "large", "nopart"), List.copyOf(tiers.keySet()));

	Assert.assertEquals(new DownloadTier(100, 20), tiers.get("standard"));
	Assert.assertEquals(new DownloadTier(2000, 200), tiers.get("large"));
	Assert.assertEquals(new DownloadTier(50, null), tiers.get("nopart"));
    }

    @Test
    public void downloadTiersOnOneLine() {

	SecondaryUsersDatabaseSetting setting = new SecondaryUsersDatabaseSetting();

	for (String text : List.of(//
		"standard=100,20, large=2000,200", //
		"standard=100,20; large=2000,200", //
		"standard=100,20 large=2000,200", //
		"standard = 100 , 20 ,large = 2000 , 200 ,")) {

	    setting.setDownloadTiers(text);

	    Map<String, DownloadTier> tiers = setting.getDownloadTiers();

	    Assert.assertEquals(text, List.of("standard", "large"), List.copyOf(tiers.keySet()));
	    Assert.assertEquals(text, new DownloadTier(100, 20), tiers.get("standard"));
	    Assert.assertEquals(text, new DownloadTier(2000, 200), tiers.get("large"));
	}

	// part sizes are optional
	setting.setDownloadTiers("small=10, standard=100,20, big=500");

	Map<String, DownloadTier> tiers = setting.getDownloadTiers();

	Assert.assertEquals(new DownloadTier(10, null), tiers.get("small"));
	Assert.assertEquals(new DownloadTier(100, 20), tiers.get("standard"));
	Assert.assertEquals(new DownloadTier(500, null), tiers.get("big"));
    }

    @Test
    public void valuesSurviveSerialization() {

	SystemSetting systemSetting = new SystemSetting();

	SecondaryUsersDatabaseSetting secondary = systemSetting.getSetting(SECONDARY_SETTING_ID, SecondaryUsersDatabaseSetting.class).get();

	secondary.setDatabaseUri("https://keycloak.example.org");
	secondary.setDatabaseName("users");
	secondary.setUsersRole(" his_central ");
	secondary.setAllowedPermissions("api");
	secondary.setDownloadTiers("standard=100,20");

	// disabled by default
	Assert.assertTrue(systemSetting.getSecondaryUsersDatabaseSetting().isEmpty());

	secondary.setEnabled(true);

	check(systemSetting);
	check(new SystemSetting(systemSetting.getObject()));
	check(new SystemSetting(systemSetting.getObject().toString()));
	check(SettingUtils.downCast(systemSetting, SystemSetting.class, true));
    }

    @Test
    public void legacyConfigurationIsFixed() {

	SystemSetting systemSetting = new SystemSetting();

	SecondaryUsersDatabaseSetting secondary = systemSetting.getSetting(SECONDARY_SETTING_ID, SecondaryUsersDatabaseSetting.class).get();
	secondary.setEnabled(true);
	secondary.setDatabaseUri("https://keycloak.example.org");
	secondary.setDatabaseName("users");

	//
	// simulates a configuration stored before this refactoring: admin credentials and type, no new options
	//
	Setting dbSetting = secondary.getSetting(DB_SETTING_ID).get();

	dbSetting.removeOption(SecondaryUsersDatabaseSetting.USERS_ROLE_OPTION_KEY);
	dbSetting.removeOption(SecondaryUsersDatabaseSetting.CLIENT_ID_OPTION_KEY);
	dbSetting.removeOption(SecondaryUsersDatabaseSetting.ALLOWED_PERMISSIONS_OPTION_KEY);
	dbSetting.removeOption(SecondaryUsersDatabaseSetting.DOWNLOAD_TIERS_OPTION_KEY);

	dbSetting.addOption(stringOption("dbUser", "admin"));
	dbSetting.addOption(stringOption("dbPassword", "secret"));
	dbSetting.addOption(stringOption("dbType", "Keycloak"));

	SystemSetting legacy = new SystemSetting(systemSetting.getObject().toString());

	Assert.assertEquals("secret", legacy.getSecondaryUsersDatabaseSetting().get().getSetting(DB_SETTING_ID).get()
		.getOption("dbPassword", String.class).get().getValue());

	//
	// the same fix applied by the DABStarter when the similarity check fails
	//
	SystemSetting fixed = SettingUtils.downCast(SelectionUtils.resetAndSelect(legacy, false), SystemSetting.class);

	SecondaryUsersDatabaseSetting fixedSecondary = fixed.getSecondaryUsersDatabaseSetting().get();

	Assert.assertEquals("https://keycloak.example.org", fixedSecondary.getKeycloakUrl());
	Assert.assertEquals("users", fixedSecondary.getRealm());

	// credentials are gone
	Setting fixedDbSetting = fixedSecondary.getSetting(DB_SETTING_ID).get();
	Assert.assertTrue(fixedDbSetting.getOption("dbUser").isEmpty());
	Assert.assertTrue(fixedDbSetting.getOption("dbPassword").isEmpty());
	Assert.assertTrue(fixedDbSetting.getOption("dbType").isEmpty());

	// new options with their defaults
	Assert.assertEquals("dab", fixedSecondary.getClientId().get());
	Assert.assertEquals(List.of("api", "downloads"), fixedSecondary.getAllowedPermissions());
	Assert.assertTrue(fixedSecondary.getUsersRole().isEmpty());

	fixedSecondary.setUsersRole("his_central");

	Assert.assertEquals("his_central", fixed.getSecondaryUsersDatabaseSetting().get().getUsersRole().get());
    }

    private static Option<String> stringOption(String key, String value) {

	Option<String> option = new Option<>(String.class);
	option.setKey(key);
	option.setLabel(key);
	option.setValue(value);

	return option;
    }

    private void check(SystemSetting systemSetting) {

	SecondaryUsersDatabaseSetting setting = systemSetting.getSecondaryUsersDatabaseSetting().get();

	Assert.assertEquals("his_central", setting.getUsersRole().get());
	Assert.assertEquals("https://keycloak.example.org/realms/users", setting.getIssuer());
	Assert.assertEquals(List.of("api"), setting.getAllowedPermissions());
	Assert.assertEquals(new DownloadTier(100, 20), setting.getDownloadTiers().get("standard"));
    }
}
