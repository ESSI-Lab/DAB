package eu.essi_lab.lib.mqtt.hive;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

/**
 *
 */
public class MQTTPublisherHive extends AbstractMQTTClientHive {

    /**
     * @param hostName
     * @param port
     * @throws Exception
     */
    public MQTTPublisherHive(String hostName, int port) throws Exception {

	super(hostName, port, UUID.randomUUID().toString(), null, null);
    }

    /**
     * @param hostName
     * @param port
     * @param user
     * @param password
     * @throws Exception
     */
    public MQTTPublisherHive(String hostName, int port, String user, String password) throws Exception {

	super(hostName, port, UUID.randomUUID().toString(), user, password);
    }

    /**
     * @param hostname
     * @param port
     * @param clientId
     * @param user
     * @param password
     * @throws Exception
     */
    public MQTTPublisherHive(String hostName, int port, String clientId, String user, String password) throws Exception {

	super(hostName, port, clientId, user, password);
    }

    /**
     * @return
     */
    protected Mqtt3Client buildClient() {

	return MqttClient.builder()//
		.automaticReconnectWithDefaultConfig()
		.useMqttVersion3()//
		.serverHost(hostName)//
		.serverPort(port) // Port for MQTT, adjust if needed
		.sslWithDefaultConfig() // Use SSL if necessary
		.identifier(clientId)//
		.build()//
		.toBlocking();
    }

    /**
     * @param topic
     * @param message
     * @throws Exception
     */
    public void publish(String topic, String message) throws Exception {

	publish(topic, message, false);
    }

    /**
     * @param topic
     * @param message
     * @param retain
     * @throws Exception
     */
    public void publish(String topic, String message, boolean retain) throws Exception {

	getBlockingClient().//
		publishWith().//
		topic(topic).//
		retain(retain).//
		payload(message.getBytes(StandardCharsets.UTF_8)).//
		qos(MqttQos.AT_MOST_ONCE).//
		send();
    }

    /**
     * @param topic
     * @throws Exception
     */
    public void clear(String topic) throws Exception {

	publish(topic, "", true);
    }

    /**
     * @return
     */
    public Mqtt3BlockingClient getWrappedClient() {

	return getBlockingClient();
    }
}
