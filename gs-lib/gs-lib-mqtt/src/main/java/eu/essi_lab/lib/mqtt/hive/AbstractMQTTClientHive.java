package eu.essi_lab.lib.mqtt.hive;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.UUID;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

/**
 * @author Fabrizio
 */
public abstract class AbstractMQTTClientHive {

    protected String clientId;
    protected String user;
    protected String password;
    protected String hostName;
    protected int port;

    protected Mqtt3Client client;

    /**
     * @param hostName
     * @param port
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port) throws Exception {
	this(hostName, port, UUID.randomUUID().toString(), null, null);
    }

    /**
     * @param hostName
     * @param port
     * @param user
     * @param password
     * @throws Exception
     */
    public AbstractMQTTClientHive(String hostName, int port, String user, String password) throws Exception {
	this(hostName, port, UUID.randomUUID().toString(), user, password);
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

	this.hostName = hostName;
	this.port = port;
	this.clientId = clientId;
	this.user = user;
	this.password = password;

	this.client = buildClient();

	connect();
    }

    /**
     * @throws Exception
     */
    public void connect() throws Exception {

	if (this.client instanceof Mqtt3AsyncClient) {

	    getAsycnhClient().connectWith()//
		    .simpleAuth()//
		    .username(user)//
		    .password(password.getBytes())//
		    .applySimpleAuth()//
		    .send();

	} else {

	    getBlockingClient().connectWith()//
		    .simpleAuth()//
		    .username(user)//
		    .password(password.getBytes())//
		    .applySimpleAuth()//
		    .send();
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
