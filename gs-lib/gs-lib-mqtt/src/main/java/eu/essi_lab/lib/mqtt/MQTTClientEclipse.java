package eu.essi_lab.lib.mqtt;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class MQTTClientEclipse {

    private String endpoint;
    private int port;
    private MqttClient publisher;
    private String user = null;
    private String password = null;

    public MQTTClientEclipse() throws MqttException {
	this("b-21d51532-c1e6-478d-964a-13f171f8fd7a-1.mq.us-east-1.amazonaws.com", 8883, UUID.randomUUID().toString(), "whos",
		"AreaDellaRicercaDiFirenze123!");
    }

    public MQTTClientEclipse(String hostname, int port, String clientId) throws MqttException {
	this(hostname, port, clientId, null, null);
    }

    public MQTTClientEclipse(String hostname, int port, String clientId, String user, String password) throws MqttException {
	this.endpoint = hostname;
	this.port = port;
	this.publisher = new MqttClient("tcp://" + hostname + ":" + port, clientId, new MemoryPersistence());

	this.user = user;
	this.password = password;

    }

    public void connect() throws Exception {
	MqttConnectOptions options = new MqttConnectOptions();
	options.setAutomaticReconnect(true);
	options.setCleanSession(true);
	options.setConnectionTimeout(1);
	if (user != null && password != null) {
	    options = new MqttConnectOptions();
	    // Set the username and password
	    options.setUserName(user);
	    options.setPassword(password.toCharArray()); // Password should be a character array
	}
	GSLoggerFactory.getLogger(getClass()).info("[MQTT] Connecting");
	publisher.connect(options);
	GSLoggerFactory.getLogger(getClass()).info("[MQTT] Connected");
    }

    private static KeyStore loadCertificate(String id, String certificateFile) throws Exception {
	KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	keyStore.load(null, null);

	try (FileInputStream fis = new FileInputStream(certificateFile)) {
	    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);
	    keyStore.setCertificateEntry(id, certificate);
	}

	return keyStore;
    }

    private static KeyStore loadCertificate(String id, String certificateFile, String privateKeyPassword) throws Exception {
	KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	keyStore.load(null, null);
	FileInputStream certStream = new FileInputStream(certificateFile);
	X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certStream);
	// PrivateKey privateKey = loadPrivateKey(privateKeyPassword);
	keyStore.setKeyEntry(id, null, privateKeyPassword.toCharArray(), new java.security.cert.Certificate[] { certificate });
	return keyStore;
    }

    public void publish(String topic, String message) throws Exception {
	if (!publisher.isConnected()) {
	    connect();
	}
	MqttMessage msg = new MqttMessage(message.getBytes());
	msg.setQos(0);
	msg.setRetained(true);
	GSLoggerFactory.getLogger(getClass()).info("[MQTT] Publishing");
	publisher.publish(topic, msg);
    }
 

    private void close() throws MqttException {
	if (publisher.isConnected()) {
	    publisher.disconnect();
	}
	publisher.close();

    }
}
