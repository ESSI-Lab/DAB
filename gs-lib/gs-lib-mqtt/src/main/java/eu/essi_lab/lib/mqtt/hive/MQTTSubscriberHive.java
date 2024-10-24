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

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;

/**
 *
 */
public class MQTTSubscriberHive extends AbstractMQTTClientHive {

    /**
     * @param hostName
     * @param port
     * @throws Exception
     */
    public MQTTSubscriberHive(String hostName, int port) throws Exception {
	
	super(hostName, port, UUID.randomUUID().toString(), null, null);
    }

    /**
     * @param hostName
     * @param port
     * @param user
     * @param password
     * @throws Exception
     */
    public MQTTSubscriberHive(String hostName, int port, String user, String password) throws Exception {
	
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
    public MQTTSubscriberHive(String hostName, int port, String clientId, String user, String password) throws Exception {

	super(hostName, port, clientId, user, password);
    }

    /**
     * @return
     */
    @Override
    protected Mqtt3Client buildClient() {

	return MqttClient.builder()//
		.automaticReconnectWithDefaultConfig()
		.useMqttVersion3()//
		.serverHost(hostName)//
		.serverPort(port) // Port for MQTT, adjust if needed
		.sslWithDefaultConfig() // Use SSL if necessary
		.identifier(clientId)//
		.buildAsync();

    }

    /**
     * @param topic
     * @param callback
     * @param whenComplete
     * @throws Exception
     */
    public void subscribe(String topic, //
	    Consumer<Mqtt3Publish> callback, //
	    BiConsumer<Mqtt3SubAck, Throwable> whenComplete) throws Exception {

	getAsycnhClient().//
		subscribeWith().//
		topicFilter(topic).//
		callback(callback).//
		send().//
		whenComplete(whenComplete);
    }

    /**
     * @param topic
     * @param message
     * @param callback
     * @throws Exception
     */
    public void subscribe(String topic, //
	    Consumer<Mqtt3Publish> callback) throws Exception {

	getAsycnhClient().//
		subscribeWith().//
		topicFilter(topic).//
		callback(callback).//
		send();
    }

    /**
     * @param topic
     */
    public void unsubscribe(String topic) {

	getAsycnhClient().//
		unsubscribeWith().//
		topicFilter(topic).//
		send();
    }

    /**
     * @return
     */
    public Mqtt3AsyncClient getWrappedClient() {

	return getAsycnhClient();
    }

    /**
     * @return the clientId
     */
    public String getClientId() {

	return clientId;
    }
}
