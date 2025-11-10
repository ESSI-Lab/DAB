package eu.essi_lab;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author Fabrizio
 */
public class KafkaClient {

    public static void main(String[] args) {

	Properties props = new Properties();
	props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
	props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
 	props.put(CommonClientConfigs.RETRIES_CONFIG, 3);
	props.put("linger.ms", 1);

	// Crea il produttore
	try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

	    String topic = "demo-topic-4";

	    for (int i = 0; i < 10; i++) {

		String key = "key-" + i;
		String value = "Messaggio numero " + i;

		ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);


		try {
		    RecordMetadata metadata = producer.send(record).get();
		    System.out.printf(
			    "Inviato a topic=%s partizione=%d offset=%d%n",
			    metadata.topic(), metadata.partition(), metadata.offset()
		    );
		} catch (ExecutionException | InterruptedException e) {


		    e.printStackTrace();
		}
	    }
	}
    }
}
