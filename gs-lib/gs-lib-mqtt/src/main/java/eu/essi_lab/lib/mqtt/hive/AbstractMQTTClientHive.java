package eu.essi_lab.lib.mqtt.hive;

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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;

/**
 * @author Fabrizio
 */
public abstract class AbstractMQTTClientHive {

    /**
     * Maximum time to wait for the connection of a blocking client. With automatic reconnect enabled, the HiveMQ client keeps retrying
     * forever when the broker is unreachable, refuses the credentials or fails the TLS handshake, so the connection must be bounded
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 30;

    protected String clientId;
    protected String user;
    protected String password;
    protected String hostName;
    protected int port;
    protected boolean defaultConfig;

    protected Mqtt3Client client;

    /**
     * @param hostName
     * @param port
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port) throws Exception {

	this(hostName, port, UUID.randomUUID().toString(), null, null, true);
    }

    /**
     * @param hostName
     * @param port
     * @param useDefaultConfig
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port, boolean useDefaultConfig) throws Exception {

	this(hostName, port, UUID.randomUUID().toString(), null, null, useDefaultConfig);
    }

    /**
     * @param hostName
     * @param port
     * @param user
     * @param password
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port, String user, String password) throws Exception {

	this(hostName, port, UUID.randomUUID().toString(), user, password, true);
    }

    /**
     * @param hostName
     * @param port
     * @param user
     * @param password
     * @param useDefaultConfig
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port, String user, String password, boolean useDefaultConfig) throws Exception {

	this(hostName, port, UUID.randomUUID().toString(), user, password, useDefaultConfig);
    }

    /**
     * @param hostname
     * @param port
     * @param clientId
     * @param user
     * @param password
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port, String clientId, String user, String password) throws Exception {

	this(hostName, port, clientId, user, password, true);
    }

    /**
     * @param hostName
     * @param port
     * @param clientId
     * @param user
     * @param password
     * @param useDefaultConfig
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port, String clientId, String user, String password, boolean useDefaultConfig)
	    throws Exception {

	this.hostName = hostName;
	this.port = port;
	this.clientId = clientId;
	this.user = user;
	this.password = password;
	this.defaultConfig = useDefaultConfig;
	this.client = buildClient();

	connect();
    }

    /**
     * @throws Exception
     */
    public void connect() throws Exception {

	if (this.client instanceof Mqtt3AsyncClient) {

	    if (user != null && password != null) {

		getAsycnhClient().connectWith()//
			.simpleAuth()//
			.username(user)//
			.password(password.getBytes())//
			.applySimpleAuth()//
			.send();
	    } else {

		getAsycnhClient().connect();
	    }

	} else {

	    Mqtt3AsyncClient asyncView = getBlockingClient().toAsync();

	    CompletableFuture<Mqtt3ConnAck> future;

	    if (user != null && password != null) {

		future = asyncView.connectWith()//
			.simpleAuth()//
			.username(user)//
			.password(password.getBytes())//
			.applySimpleAuth()//
			.send();
	    } else {

		future = asyncView.connect();
	    }

	    try {

		future.get(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

	    } catch (TimeoutException ex) {

		future.cancel(true);

		// stops the automatic reconnect attempts
		asyncView.disconnect();

		throw new Exception("Unable to connect to MQTT broker " + hostName + ":" + port + " within " + DEFAULT_CONNECT_TIMEOUT_SECONDS
			+ " seconds", ex);
	    }
	}
    }

    /**
     * 
     */
    public void disconnect() {

	if (this.client instanceof Mqtt3AsyncClient) {

	    getAsycnhClient().disconnect();

	} else {

	    getBlockingClient().disconnect();
	}
    }

    /**
     * @return the clientId
     */
    public String getClientId() {

	return clientId;
    }

    /**
     * @return
     */
    protected abstract Mqtt3Client buildClient();

    /**
     * @return
     */
    protected Mqtt3AsyncClient getAsycnhClient() {

	return (Mqtt3AsyncClient) client;
    }

    /**
     * @return
     */
    protected Mqtt3BlockingClient getBlockingClient() {

	return (Mqtt3BlockingClient) client;
    }
}
