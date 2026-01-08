package eu.essi_lab.lib.kafka.client.test;

import eu.essi_lab.lib.kafka.client.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.security.auth.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class KafkaClientExternalTestIT   {

    private KafkaClient client;

    @Before
    public void before() {

	String host = System.getProperty("kafka.host");

	int port = Integer.parseInt(System.getProperty("kafka.port"));

	SecurityProtocol securityProtocol = SecurityProtocol.forName(System.getProperty("kafka.security.protocol"));

	KafkaClient.SaslMechanism saslMechanism = KafkaClient.SaslMechanism.of(System.getProperty("kafka.sasl.mechanism")).get();

	String kafkaUser = System.getProperty("kafka.user");

	String kafkaPwd = System.getProperty("kafka.pwd");

	int reqTimeout = Integer.parseInt(System.getProperty("kafka.request.timeout"));

	client = new KafkaClient(host, port);

	client.setRequestTimeoutMls(reqTimeout);

	client.setSecurity(securityProtocol, saslMechanism, kafkaUser, kafkaPwd);
    }

    @Test
    public void blockingProducerTest1() throws ExecutionException, InterruptedException {

	String topic = System.getProperty("kafka.topic");

	List<ProducerRecord<String, String>> records = new ArrayList<>();

	int size = 10;

	for (int i = 0; i < size; i++) {

	    records.add(new ProducerRecord<>(topic, "Message #" + i + ", " + topic));
	}

	List<RecordMetadata> metadata = client.publish(records);

	Assert.assertEquals(size, metadata.size());

	metadata.forEach(m -> Assert.assertEquals(topic, m.topic()));
    }
}
