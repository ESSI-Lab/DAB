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

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

public class MQTTClientAWS {

    private MqttClientConnection connection;

    public MQTTClientAWS() throws Exception {
	this("whos-test-client", "aycusa6sh8rhm-ats.iot.us-east-1.amazonaws.com", (short) 8883, "root-CA.crt", "whos.cert.pem",
		"whos.private.key");
    }

    public MQTTClientAWS(String clientId, String endpoint, short port, String caCertificate, String clientCertificate,
	    String clientPrivateKey) throws Exception {
	File cert = saveToTemp(clientCertificate);
	File caCert = saveToTemp(caCertificate);
	File key = saveToTemp(clientPrivateKey);
	File.createTempFile("MQTT", ".cert");
	AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(cert.getAbsolutePath(),
		key.getAbsolutePath());
	if (caCertificate != null) {
	    builder.withCertificateAuthorityFromPath(null, caCert.getAbsolutePath());
	}
	MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
	    @Override
	    public void onConnectionInterrupted(int errorCode) {
		if (errorCode != 0) {
		    System.out.println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
		}
	    }

	    @Override
	    public void onConnectionResumed(boolean sessionPresent) {
		System.out.println("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
	    }
	};

	builder.withConnectionEventCallbacks(callbacks).withClientId(clientId).withEndpoint(endpoint).withPort(port).withCleanSession(true)
		.withProtocolOperationTimeoutMs(60000);

	this.connection = builder.build();
	builder.close();
	connect();

    }

    private File saveToTemp(String clientCertificate) throws Exception {
	File tempDir = new File(System.getProperty("java.io.tmpdir"));
	File temp = new File(tempDir, clientCertificate);
	temp.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(temp);
	IOUtils.copy(getClass().getClassLoader().getResourceAsStream(clientCertificate), fos);
	fos.close();
	return temp;
    }

    public void connect() {
	// Connect the MQTT client
	CompletableFuture<Boolean> connected = connection.connect();
	try {
	    boolean sessionPresent = connected.get();
	    System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
	} catch (Exception ex) {
	    throw new RuntimeException("Exception occurred during connect", ex);
	}
    }

    public static void main(String[] args) throws Exception {
	String clientId = "whos-test-client";
	String endpoint = "aycusa6sh8rhm-ats.iot.us-east-1.amazonaws.com";
	short port = 8883;
	String caCertificate = "root-CA.crt";
	String clientCertificate = "whos.cert.pem";
	String clientPrivateKey = "whos.private.key";
	MQTTClientAWS client = new MQTTClientAWS(clientId, endpoint, port, caCertificate, clientCertificate, clientPrivateKey);
	// client.connect();
	String topic = "sdk/test/java";
	client.publish(topic, "{\"test\":\"exe\"}");
	client.publish(topic, "{\"test\":\"exe3\"}");

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
	    topic = "wis2/" + countries[(int) (Math.random() * countries.length)]
		    + "/wra/hydrology-data-core/surface-water-observations/water-quantity-observations/"
		    + parameters[(int) (Math.random() * parameters.length)] + "/stream";
	    topic = "wis2/" + countries[(int) (Math.random() * countries.length)]
		    + "/wra/hydrology-data-core/surface-water-observations/water-quality-observations/"
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

    }

    public void publish(String topic, String msg) {
	try {
	    CompletableFuture<Integer> published = connection
		    .publish(new MqttMessage(topic, msg.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
	    published.get();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

    }

}
