package eu.essi_lab.cfga.gs.setting.credentials.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class CredentialsSettingTest {

    /**
     * 
     */
    @Test
    public void test() {

	CredentialsSetting setting = new CredentialsSetting();

	initTest(setting);
	initTest(new CredentialsSetting(setting.getObject()));
	initTest(new CredentialsSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, CredentialsSetting.class, true));

	setting.setDinaguaPassword("dinaguaPassword");
	setting.setDinaguaToken("dinaguaToken");
	setting.setDinaguaUser("dinaguaUser");
	setting.setINUMETPassword("inumetPassword");
	setting.setINUMETUser("inumetUser");
	setting.setNVEToken("nveToken");
	setting.setSentinelDownloaderToken("sentinelDownloaderToken");
	setting.setSentinelPassword("sentinelPassword");
	setting.setSentinelUser("sentinelUser");
	setting.setWekeoPassword("wekeoPassword");
	setting.setWekeoUser("wekeoUser");
	setting.setSOSTahmoToken("sosTahmoToken");

	test(setting);
	test(new CredentialsSetting(setting.getObject()));
	test(new CredentialsSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, CredentialsSetting.class, true));
    }

    /**
     * @param setting
     */
    private void test(CredentialsSetting setting) {

	Assert.assertEquals("dinaguaPassword", setting.getDinaguaPassword().get());
	Assert.assertEquals("dinaguaToken", setting.getDinaguaToken().get());
	Assert.assertEquals("dinaguaUser", setting.getDinaguaUser().get());
	Assert.assertEquals("inumetPassword", setting.getINUMETPassword().get());
	Assert.assertEquals("inumetUser", setting.getINUMETUser().get());
	Assert.assertEquals("nveToken", setting.getNVEToken().get());
	Assert.assertEquals("sentinelDownloaderToken", setting.getSentinelDownloaderToken().get());
	Assert.assertEquals("sentinelPassword", setting.getSentinelPassword().get());
	Assert.assertEquals("sentinelUser", setting.getSentinelUser().get());
	Assert.assertEquals("wekeoPassword", setting.getWekeoPassword().get());
	Assert.assertEquals("wekeoUser", setting.getWekeUser().get());
	Assert.assertEquals("sosTahmoToken", setting.getSOSTahmoToken().get());
    }

    /**
     * @param setting
     */
    private void initTest(CredentialsSetting setting) {

	Assert.assertFalse(setting.getDinaguaPassword().isPresent());
	Assert.assertFalse(setting.getDinaguaToken().isPresent());
	Assert.assertFalse(setting.getDinaguaUser().isPresent());
	Assert.assertFalse(setting.getINUMETPassword().isPresent());
	Assert.assertFalse(setting.getINUMETUser().isPresent());
	Assert.assertFalse(setting.getNVEToken().isPresent());
	Assert.assertFalse(setting.getSentinelDownloaderToken().isPresent());
	Assert.assertFalse(setting.getSentinelPassword().isPresent());
	Assert.assertFalse(setting.getSentinelUser().isPresent());
	Assert.assertFalse(setting.getWekeoPassword().isPresent());
	Assert.assertFalse(setting.getWekeUser().isPresent());
	Assert.assertFalse(setting.getSOSTahmoToken().isPresent());
    }

}
