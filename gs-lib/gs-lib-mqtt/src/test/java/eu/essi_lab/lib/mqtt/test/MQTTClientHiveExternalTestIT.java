package eu.essi_lab.lib.mqtt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.mqtt.hive.MQTTPublisherHive;
import eu.essi_lab.lib.mqtt.hive.MQTTSubscriberHive;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class MQTTClientHiveExternalTestIT {

    private MQTTPublisherHive publisher;
    private MQTTSubscriberHive subscriber;

    private static final String MESSAGE_TOPIC = "testTopic";

    @Before
    public void before() throws NumberFormatException, Exception {

	String host = System.getProperty("mqttBrokerHost");
	String port = System.getProperty("mqttBrokerPort");
	String user = System.getProperty("mqttBrokerUser");
	String pwd = System.getProperty("mqttBrokerPwd");

	publisher = new MQTTPublisherHive(host, Integer.valueOf(port), user, pwd);
	subscriber = new MQTTSubscriberHive(host, Integer.valueOf(port), user, pwd);
    }

    @After
    public void after() {

	publisher.disconnect();
	subscriber.disconnect();
    }

    @Test
    public void singlePublisherSingleSubscriberNotRetainMessageReceivedTest() throws NumberFormatException, Exception {

	String messagePayload = "testPayload1";

	List<String> receivedMessages = new ArrayList<>();

	//
	// always clear the topic, even if not retained
	//

	publisher.clear(MESSAGE_TOPIC);

	//
	//
	//

	subscriber.subscribe(MESSAGE_TOPIC, message -> {

	    byte[] payloadAsBytes = message.getPayloadAsBytes();

	    String payload = new String(payloadAsBytes);

	    receivedMessages.add(payload);

	}, (subAck, throwable) -> {

	    //
	    // since not retained, the message must be sent after the registration is ready
	    //

	    try {
		publisher.publish(MESSAGE_TOPIC, messagePayload, false);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	});

	//
	// waits some second to be sure the message has been received
	//

	Thread.sleep(TimeUnit.SECONDS.toMillis(5));

	//
	//
	//

	Assert.assertTrue(receivedMessages.contains(messagePayload));
    }

    @Test
    public void singlePublisherSingleSubscriberNotRetainMessageNotReceivedTest() throws NumberFormatException, Exception {

	String messagePayload = "testPayload2";

	List<String> receivedMessages = new ArrayList<>();

	//
	// always clear the topic, even if not retained
	//

	publisher.clear(MESSAGE_TOPIC);

	//
	//
	//

	subscriber.subscribe(MESSAGE_TOPIC, message -> {

	    byte[] payloadAsBytes = message.getPayloadAsBytes();

	    String payload = new String(payloadAsBytes);

	    receivedMessages.add(payload);
	});

	//
	// the message is published too early, before than the registration is ready
	//

	publisher.publish(MESSAGE_TOPIC, messagePayload, false);

	//
	// waits some second to be sure the message has been received, but the message cannot be received
	//

	Thread.sleep(TimeUnit.SECONDS.toMillis(5));

	//
	//
	//

	Assert.assertFalse(receivedMessages.contains(messagePayload));
    }

    @Test
    public void singlePublisherSingleSubscriberRetainMessageTest() throws NumberFormatException, Exception {

	String messagePayload = "testPayload3";

	List<String> receivedMessages = new ArrayList<>();

	//
	// always clear the topic, even if not retained
	//

	publisher.clear(MESSAGE_TOPIC);

	//
	//
	//

	subscriber.subscribe(MESSAGE_TOPIC, message -> {

	    byte[] payloadAsBytes = message.getPayloadAsBytes();

	    String payload = new String(payloadAsBytes);

	    receivedMessages.add(payload);
	});

	//
	// the message is published too early, before than the registration is ready
	// but since it is retained it will be received 
	//

	publisher.publish(MESSAGE_TOPIC, messagePayload, true);

	//
	// waits some second to be sure the message has been received
	//

	Thread.sleep(TimeUnit.SECONDS.toMillis(5));

	Assert.assertTrue(receivedMessages.contains(messagePayload));
    }

    /**
     * @throws NumberFormatException
     * @throws Exception
     */
    // @Test
    public void simpleTest() throws NumberFormatException, Exception {

	// MqttClient mqttClient = new
	// MqttClient("tcp://b-21d51532-c1e6-478d-964a-13f171f8fd7a-1.mq.us-east-1.amazonaws.com:8883", "test", new
	// MemoryPersistence());
	// MqttClient mqttClient = new MqttClient("tcp://globalbroker.meteo.fr:1883", "testfewf", new
	// MemoryPersistence());
	//
	// MqttConnectOptions connectOptions = new MqttConnectOptions();
	// connectOptions.setUserName("whosnode");
	// connectOptions.setPassword("vGtX2trUKM9i3XUK4SPv49q".toCharArray());

	// Connect to the broker
	// mqttClient.connect(connectOptions);

	// MQTTClient client = new MQTTClient("localhost", 1883);

	String host = System.getProperty("mqttBrokerHost");
	String port = System.getProperty("mqttBrokerPort");
	String user = System.getProperty("mqttBrokerUser");
	String pwd = System.getProperty("mqttBrokerPwd");

	MQTTPublisherHive client = new MQTTPublisherHive(host, Integer.valueOf(port), user, pwd);

	String[] countries = new String[] { "ken", "arg", "bra", "pry", "ury" };
	String[] parameters = new String[] { "level", "temperature", "discharge" };

	for (int i = 0; i < 4343; i++) {

	    JSONObject json = new JSONObject();
	    json.put("id", UUID.randomUUID().toString());
	    json.put("type", "Feature");
	    json.put("version", "v04");
	    JSONObject geometry = new JSONObject();
	    geometry.put("type", "Point");
	    JSONArray coordinates = new JSONArray();
	    coordinates.put("35.28");
	    coordinates.put("3.14");
	    geometry.put("coordinates", coordinates);
	    json.put("geometry", geometry);
	    JSONObject properties = new JSONObject();
	    String topic = "wis2/" + countries[(int) (Math.random() * countries.length)]
		    + "/wra/data/core/hydrology/surface-water-observations/water-quantity-observations/"
		    + parameters[(int) (Math.random() * parameters.length)] + "/stream";
	    topic = "wis2/" + countries[(int) (Math.random() * countries.length)]
		    + "/wra/data/core/hydrology/surface-water-observations/water-quality-observations/"
		    + parameters[(int) (Math.random() * parameters.length)] + "/stream";
	    String id = topic + "/" + UUID.randomUUID().toString();
	    properties.put("data_id", id);
	    properties.put("pubtime", ISO8601DateTimeUtils.getISO8601DateTime());
	    properties.put("wigos_station_identifier", "N/A");
	    json.put("properties", properties);
	    JSONArray links = new JSONArray();
	    JSONObject link = new JSONObject();
	    link.put("rel", "canonical");
	    link.put("type", "application/netcdf");
	    link.put("href", "https://whos.geodab.eu/gs-service/services/essi/view/whos/mqtt?data_id=" + id);
	    links.put(link);
	    json.put("links", links);
	    client.publish(topic, json.toString());
	}

	System.out.println("end");
	client.disconnect();
    }
}
