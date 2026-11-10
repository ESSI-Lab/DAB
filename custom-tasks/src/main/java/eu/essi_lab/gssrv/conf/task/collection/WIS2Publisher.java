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
 * is shared and opened at the first publication; in case of error it is closed, and reopened at the next publication
 *
 * @author boldrini
 */
public class WIS2Publisher {

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

	if (client == null && !connect()) {
	    return false;
	}

	try {

	    client.publish(topic, message.getJSONObject().toString(3), false);
	    return true;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(WIS2Publisher.class).error("Unable to publish to WIS2 MQTT broker topic {}: {}", topic, e.getMessage(),
		    e);
	    disconnect();
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
		GSLoggerFactory.getLogger(WIS2Publisher.class).error(e);
	    }
	    client = null;
	}
    }
}
