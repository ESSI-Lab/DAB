package eu.essi_lab.lib.kafka.client.test;

import eu.essi_lab.lib.kafka.client.KafkaClient;
import eu.essi_lab.lib.kafka.client.KafkaClient.SaslMechanism;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

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
    public void initTest() {

	String host = System.getProperty("kafka.host");

	int port = Integer.parseInt(System.getProperty("kafka.port"));

	String server = host + ":" + port;

	initTest(new KafkaClient(host, port));
	initTest(new KafkaClient(server));
	initTest(new KafkaClient(List.of(server)));
    }

    /**
     * @param client
     */
    private void initTest(KafkaClient client) {

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
