package eu.essi_lab.lib.mqtt;

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

import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.helpers.ISO8601DateFormat;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class MQTTClient {

    private String endpoint;
    private int port;
    private MqttClient publisher;

    public MQTTClient(String hostname, int port) throws MqttException {
	this.endpoint = hostname;
	this.port = port;
	String publisherId = UUID.randomUUID().toString();
	this.publisher = new MqttClient("tcp://" + hostname + ":" + port, publisherId);

    }

    public void connect() throws MqttSecurityException, MqttException {
	MqttConnectOptions options = new MqttConnectOptions();
	options.setAutomaticReconnect(true);
	options.setCleanSession(true);
	options.setConnectionTimeout(1);
	GSLoggerFactory.getLogger(getClass()).info("[MQTT] Connecting");
	publisher.connect(options);
    }

    public void publish(String topic, String message) throws MqttPersistenceException, MqttException {
	if (!publisher.isConnected()) {
	    connect();
	}
	MqttMessage msg = new MqttMessage(message.getBytes());
	msg.setQos(0);
	msg.setRetained(true);
	GSLoggerFactory.getLogger(getClass()).info("[MQTT] Publishing");
	publisher.publish(topic, msg);
    }

    public static void main(String[] args) throws Exception {
	MQTTClient client = new MQTTClient("localhost", 1883);

	String[] countries = new String[] {"ken/wra","arg/ina","bra/inmet","pry/dmh-automaticas","pry/dmh-sigedac"};
	String[] parameters = new String[] {"level", "temperature","discharge"};


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
	    String topic = "origin/a/wis2/" + countries[(int) (Math.random() * countries.length)]
		    + "/data/core/hydrology/surface-water-observations/water-quality-observations/"+parameters[(int) (Math.random() * parameters.length)]+"/stream";
	    topic = "origin/a/wis2/" + countries[(int) (Math.random() * countries.length)]
		    + "/data/core/hydrology/surface-water-observations/water-quantity-observations/"+parameters[(int) (Math.random() * parameters.length)]+"/stream";
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
	client.close();
    }

    private void close() throws MqttException {
	if (publisher.isConnected()) {
	    publisher.disconnect();
	}
	publisher.close();

    }
}
