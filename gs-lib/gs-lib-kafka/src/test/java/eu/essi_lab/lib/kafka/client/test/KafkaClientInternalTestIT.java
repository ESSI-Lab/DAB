package eu.essi_lab.lib.kafka.client.test;

import eu.essi_lab.lib.kafka.client.KafkaClient;
import eu.essi_lab.lib.kafka.client.KafkaClient.SaslMechanism;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Fabrizio
 */
public class KafkaClientInternalTestIT {

    private KafkaClient client;

    @Before
    public void before() {

	String host = System.getProperty("kafka.host");

	int port = Integer.parseInt(System.getProperty("kafka.port"));

	client = new KafkaClient(host, port);
    }

    @Test
    public void saslMechanismEnumTest() {

	Assert.assertEquals(SaslMechanism.PLAIN, SaslMechanism.of("PLAIN").get());
	Assert.assertEquals(SaslMechanism.GSSAPI, SaslMechanism.of("GSSAPI").get());
	Assert.assertEquals(SaslMechanism.SCRAM_SHA_512, SaslMechanism.of("SCRAM-SHA-512").get());
	Assert.assertEquals(SaslMechanism.OAUTHBEARER, SaslMechanism.of("OAUTHBEARER").get());

	Assert.assertFalse(SaslMechanism.of("XXX").isPresent());

	Assert.assertEquals("PLAIN", SaslMechanism.PLAIN.value());
	Assert.assertEquals("GSSAPI", SaslMechanism.GSSAPI.value());
	Assert.assertEquals("SCRAM-SHA-512", SaslMechanism.SCRAM_SHA_512.value());
	Assert.assertEquals("OAUTHBEARER", SaslMechanism.OAUTHBEARER.value());

	String plainLoginModule = SaslMechanism.plainLoginModule("admin", "pwd");

	Assert.assertEquals("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"pwd\"",
		plainLoginModule);

	String scramLoginModule = SaslMechanism.scramLoginModule("admin", "pwd");

	Assert.assertEquals("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"admin\" password=\"pwd\"",
		scramLoginModule);
    }

    @Test
    public void blockingProducerTest1() throws ExecutionException, InterruptedException {

	String topic = "test-topic-1";

	List<ProducerRecord<String, String>> records = new ArrayList<>();

	int size = 10;

	for (int i = 0; i < size; i++) {

	    records.add(new ProducerRecord<>(topic, "Message #" + i + ", " + topic));
	}

	List<RecordMetadata> metadata = client.publish(records);

	Assert.assertEquals(size, metadata.size());

	metadata.forEach(m -> Assert.assertEquals(topic, m.topic()));
    }

    @Test
    public void blockingProducerTest2() throws ExecutionException, InterruptedException {

	String topic = "test-topic-2";

	List<ProducerRecord<String, String>> records = new ArrayList<>();

	List<RecordMetadata> metadata = client.publish(topic, null, "Message of " + topic);

	Assert.assertEquals(1, metadata.size());

	Assert.assertEquals(topic, metadata.getFirst().topic());
    }

    @Test
    public void blockingProducerTest3() throws ExecutionException, InterruptedException {

	String topic = "test-topic-3";

	List<ProducerRecord<String, String>> records = new ArrayList<>();

	List<RecordMetadata> metadata = client.publish(new ProducerRecord<>(topic, "Message of " + topic));

	Assert.assertEquals(1, metadata.size());

	Assert.assertEquals(topic, metadata.getFirst().topic());
    }

    @Test
    public void asynchProducerTest1() throws ExecutionException, InterruptedException {

	String topic = "test-topic-4";

	ProducerRecord<String, String> record = new ProducerRecord<>(topic, "Message of " + topic);

	Future<RecordMetadata> future = client.publish(record, (metadata, exception) -> {

	    Assert.assertNull(exception);

	    Assert.assertEquals(topic, metadata.topic());
	});

	future.get();
    }

    @Test
    public void asynchProducerTest2() throws ExecutionException, InterruptedException {

	String topic = "test-topic-5";

	Future<RecordMetadata> future = client.publish(topic, UUID.randomUUID().toString(), "Message of " + topic,
		(metadata, exception) -> {

		    Assert.assertNull(exception);

		    Assert.assertEquals(topic, metadata.topic());
		});

	future.get();
    }

    @Test
    public void asynchProducerTest3() throws ExecutionException, InterruptedException {

	String topic = "test-topic-6";

	Future<RecordMetadata> future = client.publish(topic, "Message of " + topic, (metadata, exception) -> {

	    Assert.assertNull(exception);

	    Assert.assertEquals(topic, metadata.topic());
	});

	future.get();
    }

    @Test
    public void asynchProducerTest4() throws ExecutionException, InterruptedException {

	String topic = "test-topic-7";

	ProducerRecord<String, String> record1 = new ProducerRecord<>(topic, "Message #1 of " + topic);
	ProducerRecord<String, String> record2 = new ProducerRecord<>(topic, "Message #1 of " + topic);
	ProducerRecord<String, String> record3 = new ProducerRecord<>(topic, "Message #1 of " + topic);

	List<Future<RecordMetadata>> futures = client.publish(List.of(record1, record2, record3), (metadata, exception) -> {

	    Assert.assertNull(exception);

	    Assert.assertEquals(topic, metadata.topic());
	});

	for (Future<RecordMetadata> recordMetadataFuture : futures) {
	    recordMetadataFuture.get();
	}

	Assert.assertEquals(3, futures.size());
    }

    @Test
    public void propsTest() {

	String host = System.getProperty("kafka.host");

	int port = Integer.parseInt(System.getProperty("kafka.port"));

	String server = host + ":" + port;

	propsTest(new KafkaClient(host, port));
	propsTest(new KafkaClient(server));
	propsTest(new KafkaClient(List.of(server)));
    }

    /**
     * @param client
     */
    private void propsTest(KafkaClient client) {

	Properties producerProps = client.getProducerProps();
	Assert.assertFalse(producerProps.isEmpty());

	Properties consumerProps = client.getConsumerProps();
	Assert.assertFalse(consumerProps.isEmpty());

	//
	//
	//

	String prodServers = producerProps.getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG);
	String consServers = consumerProps.getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG);

	String server = System.getProperty("kafka.host") + ":" + System.getProperty("kafka.port");

	Assert.assertEquals(server, prodServers);
	Assert.assertEquals(server, consServers);

	//
	//
	//

	client.addBootstrapServer("server1:9090");
	client.addBootstrapServer("server2", 9090);

	prodServers = producerProps.getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG);
	consServers = consumerProps.getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG);

	Assert.assertEquals(3, prodServers.split(",").length);
	Assert.assertEquals(3, consServers.split(",").length);

	Assert.assertEquals("server2:9090", prodServers.split(",")[0]);
	Assert.assertEquals("server1:9090", prodServers.split(",")[1]);
	Assert.assertEquals(server, prodServers.split(",")[2]);

	//
	//
	//

	client.setSecurity(SecurityProtocol.SASL_SSL, SaslMechanism.PLAIN, "admin", "pwd");

	Assert.assertEquals(SaslMechanism.plainLoginModule("admin", "pwd"), producerProps.getProperty(SaslConfigs.SASL_JAAS_CONFIG));
	Assert.assertEquals(SaslMechanism.plainLoginModule("admin", "pwd"), consumerProps.getProperty(SaslConfigs.SASL_JAAS_CONFIG));

	Assert.assertEquals(SecurityProtocol.SASL_SSL.name(), producerProps.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
	Assert.assertEquals(SaslMechanism.PLAIN.value(), producerProps.getProperty(SaslConfigs.SASL_MECHANISM));

	Assert.assertEquals(SecurityProtocol.SASL_SSL.name(), consumerProps.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
	Assert.assertEquals(SaslMechanism.PLAIN.value(), consumerProps.getProperty(SaslConfigs.SASL_MECHANISM));

	//
	//
	//

	client.setSecurity(SecurityProtocol.SASL_SSL, SaslMechanism.SCRAM_SHA_512, "admin", "pwd");

	Assert.assertEquals(SaslMechanism.scramLoginModule("admin", "pwd"), producerProps.getProperty(SaslConfigs.SASL_JAAS_CONFIG));
	Assert.assertEquals(SaslMechanism.scramLoginModule("admin", "pwd"), consumerProps.getProperty(SaslConfigs.SASL_JAAS_CONFIG));

	Assert.assertEquals(SecurityProtocol.SASL_SSL.name(), producerProps.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
	Assert.assertEquals(SaslMechanism.SCRAM_SHA_512.value(), producerProps.getProperty(SaslConfigs.SASL_MECHANISM));

	Assert.assertEquals(SecurityProtocol.SASL_SSL.name(), consumerProps.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
	Assert.assertEquals(SaslMechanism.SCRAM_SHA_512.value(), consumerProps.getProperty(SaslConfigs.SASL_MECHANISM));
    }
}
