package eu.essi_lab;

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

import eu.essi_lab.lib.net.publisher.MessagePublisher;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Fabrizio
 */
public class KafkaClient implements MessagePublisher {

    private Properties producerProps;
    private Properties consumerProps;

    /**
     *
     */
    public KafkaClient() {

	producerProps = new Properties();
	consumerProps = new Properties();

	producerProps.put(CommonClientConfigs.CLIENT_ID_CONFIG, "DAB");
	consumerProps.put(CommonClientConfigs.CLIENT_ID_CONFIG, "DAB");
	consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "DAB");

	producerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL);
	consumerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL);

	//
	//
	//

	producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
	producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());

	producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // default
	producerProps.put(CommonClientConfigs.RETRIES_CONFIG, Integer.MAX_VALUE); // default
	producerProps.put(ProducerConfig.ACKS_CONFIG, "all"); // default

	producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 200); // 200 millis
	producerProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 1000); // one minute
	producerProps.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 5 milli; suggested [0 - 50]

	//
	//
	//

	consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
	consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());

	consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    }

    /**
     * @param bootstrapServers
     */
    public KafkaClient(String... bootstrapServers) {

	this(List.of(bootstrapServers));
    }

    /**
     * @param bootstrapServers
     */
    public KafkaClient(List<String> bootstrapServers) {

	this();

	for (String bootstrapServer : bootstrapServers) {

	    addBootstrapServer(bootstrapServer);
	}
    }

    /**
     * @param topic
     * @param key
     * @param value
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void publish(String topic, String key, String value) throws ExecutionException, InterruptedException {

	publish(new ProducerRecord<>(topic, key, value));
    }

    /**
     * @param topic
     * @param value
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void publish(String topic, String value) throws ExecutionException, InterruptedException {

	publish(topic, UUID.randomUUID().toString(), value);
    }

    /**
     * @param records
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void publish(List<ProducerRecord<String, String>> records) throws ExecutionException, InterruptedException {

	try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {

	    for (ProducerRecord<String, String> record : records) {

		final RecordMetadata metadata = producer.send(record).get();

		GSLoggerFactory.getLogger(getClass())
			.debug("Sent -> topic: {}, partition: {}, offset: {}", metadata.topic(), metadata.partition(), metadata.offset());
	    }
	}
    }

    /**
     * @param record
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void publish(ProducerRecord<String, String> record) throws ExecutionException, InterruptedException {

	publish(List.of(record));
    }

    /**
     * @param records
     * @param callback
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<Future<RecordMetadata>> publish(List<ProducerRecord<String, String>> records, Callback callback)
	    throws ExecutionException, InterruptedException {

	final ArrayList<Future<RecordMetadata>> futures = new ArrayList<>();

	try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {

	    for (ProducerRecord<String, String> record : records) {

		futures.add(producer.send(record, callback));
	    }
	}

	return futures;
    }

    /**
     * @param record
     * @param callback
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Future<RecordMetadata> publish(ProducerRecord<String, String> record, Callback callback)
	    throws ExecutionException, InterruptedException {

	return publish(List.of(record), callback).getFirst();
    }

    /**
     * @param topic
     * @param key
     * @param value
     * @param callback
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Future<RecordMetadata> publish(String topic, String key, String value, Callback callback)
	    throws ExecutionException, InterruptedException {

	return publish(new ProducerRecord<>(topic, key, value), callback);
    }

    /**
     * @param topic
     * @param value
     * @param callback
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Future<RecordMetadata> publish(String topic, String value, Callback callback) throws ExecutionException, InterruptedException {

	return publish(topic, UUID.randomUUID().toString(), value, callback);
    }

    /**
     * @return
     */
    public Properties getProducerProps() {

	return producerProps;
    }

    /**
     * @return
     */
    public Properties getConsumerProps() {

	return consumerProps;
    }

    /**
     * @param server <code>host:port</code> (never include 'http' nor 'https')
     */
    public void addBootstrapServer(String server) {

	producerProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, server);
	consumerProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, server);
    }

    /**
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {

	final KafkaClient client = new KafkaClient("localhost:9093");

	String topic = "demo-topic-13";

	List<ProducerRecord<String, String>> records = new ArrayList<>();

	for (int i = 0; i < 10; i++) {

	    String key = "key-" + i;
	    String value = "Messaggio numero " + i;

	    records.add(new ProducerRecord<>(topic, key, value));
	}

	client.publish(records);
    }
}
