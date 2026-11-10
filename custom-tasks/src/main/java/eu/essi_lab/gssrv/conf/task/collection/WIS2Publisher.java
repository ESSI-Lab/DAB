package eu.essi_lab.gssrv.conf.task.collection;


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

import java.util.Optional;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.MQTTBrokerSetting;
import eu.essi_lab.lib.mqtt.hive.MQTTPublisherHive;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Publishes the WIS2 notification messages to the WIS2 MQTT broker configured in the system settings. The connection
 * is shared and opened at the first publication; when it has been dropped or a publication fails, it is closed and
 * reopened, and the publication is retried once
 *
 * @author boldrini
 */
public class WIS2Publisher {

    /**
     * Publication attempts of a message: a failed attempt closes the connection, and the next one opens a new one
     */
    private static final int MAX_ATTEMPTS = 2;

    private static MQTTPublisherHive client;

    private WIS2Publisher() {
    }

    /**
     * Publishes the given notification message, not retained: a retained message would be redelivered (e.g. on broker
     * reconnections) after the notified data has changed, and its integrity would no longer match the content served at
     * the link
     * 
     * @param topic
     * @param message
     * @return true if the message has been published
     */
    public static synchronized boolean publish(String topic, WISNotificationMessage message) {

	String payload = message.getJSONObject().toString(3);

	for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

	    // the shared connection may have been dropped by the broker since the last publication (e.g. idle timeout or
	    // broker restart): in this case it is replaced by a new one, instead of waiting for the automatic reconnection
	    if (client != null && !isConnected()) {

		GSLoggerFactory.getLogger(WIS2Publisher.class).info("WIS2 MQTT broker connection lost, reconnecting");
		disconnect();
	    }

	    if (client == null && !connect()) {
		return false;
	    }

	    try {

		client.publish(topic, payload, false);
		return true;

	    } catch (Exception e) {

		disconnect();

		if (attempt < MAX_ATTEMPTS) {

		    GSLoggerFactory.getLogger(WIS2Publisher.class).warn("Unable to publish to WIS2 MQTT broker topic {}: {}, retrying with a new connection",
			    topic, e.getMessage());
		} else {

		    GSLoggerFactory.getLogger(WIS2Publisher.class).error("Unable to publish to WIS2 MQTT broker topic {}: {}", topic,
			    e.getMessage(), e);
		}
	    }
	}

	return false;
    }

    /**
     * @return true if the shared client is currently connected to the broker
     */
    private static boolean isConnected() {

	try {

	    return client.getWrappedClient().getState().isConnected();

	} catch (Exception e) {

	    return false;
	}
    }

    /**
     * @return true if connected
     */
    private static boolean connect() {

	Optional<MQTTBrokerSetting> broker = ConfigurationWrapper.getSystemSettings().getWis2MqttBroker();

	if (broker.isEmpty()) {

	    GSLoggerFactory.getLogger(WIS2Publisher.class).info("WIS2 MQTT broker not configured");
	    return false;
	}

	MQTTBrokerSetting mqtt = broker.get();
	String endpoint = mqtt.getHost().get() + ":" + mqtt.getPort().get();

	try {

	    GSLoggerFactory.getLogger(WIS2Publisher.class).info("Connecting to WIS2 MQTT broker {} as user {}", endpoint, mqtt.getUser().get());

	    client = new MQTTPublisherHive(mqtt.getHost().get(), mqtt.getPort().get(), mqtt.getUser().get(), mqtt.getPassword().get());

	    GSLoggerFactory.getLogger(WIS2Publisher.class).info("Connected to WIS2 MQTT broker {}", endpoint);
	    return true;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(WIS2Publisher.class).error("Unable to connect to WIS2 MQTT broker {}: {}", endpoint, e.getMessage(), e);
	    client = null;
	    return false;
	}
    }

    /**
     * 
     */
    private static void disconnect() {

	if (client != null) {
	    try {
		client.disconnect();
	    } catch (Exception e) {
		// expected when the connection has already been dropped (the client is not connected)
		GSLoggerFactory.getLogger(WIS2Publisher.class).debug("Disconnection from WIS2 MQTT broker: {}", e.getMessage());
	    }
	    client = null;
	}
    }
}
