package eu.essi_lab.cfga.gs.setting.sessioncoordinator.test;

import eu.essi_lab.cfga.gs.setting.sessioncoordinator.*;
import org.junit.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class SessionCoordinatorSettingTest {

    @Test
    public void initTest() {

	SessionCoordinatorSetting setting = new SessionCoordinatorSetting();

	SessionCoordinatorSetting.ServiceCoordinatorMode serviceCoordinatorMode = setting.getServiceCoordinatorMode();

	Assert.assertEquals(SessionCoordinatorSetting.ServiceCoordinatorMode.LOCAL, serviceCoordinatorMode);

	int heartbeat = setting.getHeartbeat();

	Assert.assertEquals(SessionCoordinatorSetting.DEFAULT_HEARTBEAT_SECONDS, heartbeat);

	int distributedMessageChannelSize = setting.getDistributedMessageChannelSize();

	Assert.assertEquals(SessionCoordinatorSetting.DEFAULT_CHANNEL_SIZE, distributedMessageChannelSize);

	int localMessageChannelSize = setting.getLocalMessageChannelSize();

	Assert.assertEquals(SessionCoordinatorSetting.DEFAULT_CHANNEL_SIZE, localMessageChannelSize);

	int maxServices = setting.getMaxServices();

	Assert.assertEquals(SessionCoordinatorSetting.DEFAULT_MAX_SERVICES, maxServices);

	int ttl = setting.getTTL();

	Assert.assertEquals(SessionCoordinatorSetting.DEFAULT_TTL_SECONDS, ttl);

	String redisEndpointPort = setting.getRedisEndpoint(true);

	Assert.assertEquals("localhost:" + SessionCoordinatorSetting.DEFAULT_REDIS_PORT, redisEndpointPort);

	String redisEndpointNoPort = setting.getRedisEndpoint(false);

	Assert.assertEquals("localhost", redisEndpointNoPort);

	Optional<String> redisPassword = setting.getRedisPassword();

	Assert.assertTrue(redisPassword.isEmpty());

	Optional<String> redisUsername = setting.getRedisUsername();

	Assert.assertTrue(redisUsername.isEmpty());
    }

    @Test
    public void setTest() {

	SessionCoordinatorSetting setting = new SessionCoordinatorSetting();

	setting.setRedisEndpoint("endpoint","3030");
	setting.setRedisPassword("password");
	setting.setRedisUsername("username");

	String redisEndpointPort = setting.getRedisEndpoint(true);

	Assert.assertEquals("endpoint:3030", redisEndpointPort);

	String redisEndpointNoPort = setting.getRedisEndpoint(false);

	Assert.assertEquals("endpoint", redisEndpointNoPort);

	Optional<String> redisPassword = setting.getRedisPassword();

	Assert.assertEquals("password", redisPassword.get());

	Optional<String> redisUsername = setting.getRedisUsername();

	Assert.assertEquals("username", redisUsername.get());
    }

    @Test
    public void editableSettingTest() {


    }

}
