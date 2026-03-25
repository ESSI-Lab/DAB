//package eu.essi_lab.services.impl;
//
//import org.apache.kafka.clients.consumer.*;
//import org.apache.kafka.common.PartitionInfo;
//import org.apache.kafka.common.TopicPartition;
//
//import org.apache.avro.Schema;
//import org.apache.avro.io.*;
//import org.apache.avro.generic.*;
//
//import com.fasterxml.jackson.databind.*;
//
//import java.net.URI;
//import java.net.http.*;
//import java.nio.ByteBuffer;
//import java.time.Duration;
//import java.util.*;
//
///**
// * @author Fabrizio
// */
//public class KafkaAvroConsumer {
//
//    static Map<Integer, Schema> schemaCache = new HashMap<>();
//    static ObjectMapper mapper = new ObjectMapper();
//
//    // ==============================
//    // TOKEN AUTH
//    // ==============================
//
//    static String getAccessToken() throws Exception {
//
//	HttpClient client = HttpClient.newHttpClient();
//
//	Map<String, Object> payload = new HashMap<>();
//	payload.put("user", DefaultConfig.TOKEN_USER);
//	payload.put("psw", DefaultConfig.TOKEN_PW);
//	payload.put("app_to_use", "TUTTE");
//
//	String body = mapper.writeValueAsString(payload);
//
//	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(DefaultConfig.TOKEN_URL)).header("Content-Type", "application/json")
//		.POST(HttpRequest.BodyPublishers.ofString(body)).build();
//
//	HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//	JsonNode json = mapper.readTree(response.body());
//
//	return json.get("access_token").asText();
//    }
//
//    // ==============================
//    // SCHEMA REGISTRY
//    // ==============================
//
//    static Schema getSchema(int schemaId, String token) throws Exception {
//
//	if (!schemaCache.containsKey(schemaId)) {
//
//	    HttpClient client = HttpClient.newHttpClient();
//
//	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(DefaultConfig.SCHEMA_REGISTRY_URL + "/schemas/ids/" + schemaId))
//		    .header("Authorization", "Bearer " + token).GET().build();
//
//	    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//	    JsonNode json = mapper.readTree(response.body());
//
//	    String schemaStr = json.get("schema").asText();
//
//	    Schema schema = new Schema.Parser().parse(schemaStr);
//
//	    schemaCache.put(schemaId, schema);
//	}
//
//	return schemaCache.get(schemaId);
//    }
//
//    // ==============================
//    // AVRO DESERIALIZATION
//    // ==============================
//
//    static GenericRecord deserialize(byte[] raw, String token) throws Exception {
//
//	ByteBuffer buffer = ByteBuffer.wrap(raw);
//
//	buffer.get(); // magic byte
//	int schemaId = buffer.getInt();
//
//	Schema schema = getSchema(schemaId, token);
//
//	byte[] avroBytes = new byte[buffer.remaining()];
//	buffer.get(avroBytes);
//
//	BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);
//
//	GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
//
//	return reader.read(null, decoder);
//    }
//
//    // ==============================
//    // MAIN
//    // ==============================
//
//    public static void main(String[] args) throws Exception {
//
//	String token = getAccessToken();
//
//	Properties props = new Properties();
//
//	props.put("bootstrap.servers", DefaultConfig.BOOTSTRAP_SERVERS);
//	props.put("group.id", DefaultConfig.GROUP_ID);
//	props.put("enable.auto.commit", "false");
//	props.put("auto.offset.reset", "latest");
//
//	props.put("security.protocol", "SASL_SSL");
//	props.put("sasl.mechanism", "SCRAM-SHA-512");
//
//	props.put("sasl.jaas.config",
//		"org.apache.kafka.common.security.scram.ScramLoginModule required " + "username=\"" + DefaultConfig.USERNAME + "\" "
//			+ "password=\"" + DefaultConfig.PASSWORD + "\";");
//
//	props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
//
//	props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
//
//	KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props);
//
//	List<Map<String, Object>> decodedMessages = new ArrayList<>();
//
//	// ==============================
//	// GET PARTITIONS
//	// ==============================
//
//	List<PartitionInfo> partitions = consumer.partitionsFor(DefaultConfig.TOPIC);
//
//	for (PartitionInfo p : partitions) {
//
//	    TopicPartition tp = new TopicPartition(DefaultConfig.TOPIC, p.partition());
//
//	    consumer.assign(List.of(tp));
//
//	    consumer.seekToEnd(List.of(tp));
//
//	    long high = consumer.position(tp);
//
//	    long start = Math.max(0, high - DefaultConfig.N_MESSAGES);
//
//	    consumer.seek(tp, start);
//
//	    // ==============================
//	    // POLL MESSAGES
//	    // ==============================
//
//	    while (true) {
//
//		ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofSeconds(1));
//
//		if (records.isEmpty())
//		    break;
//
//		for (ConsumerRecord<byte[], byte[]> msg : records) {
//
//		    byte[] raw = msg.value();
//
//		    if (raw == null)
//			continue;
//
//		    // optional text filter
//		    if (DefaultConfig.TEXT_FILTER != null && !DefaultConfig.TEXT_FILTER.isEmpty()) {
//
//			String text = new String(raw);
//
//			System.out.println("***");
//			System.out.println(text);
//			System.out.println("***");
//
//			if (!text.contains(DefaultConfig.TEXT_FILTER))
//			    continue;
//		    }
//
//		    try {
//
//			GenericRecord record = deserialize(raw, token);
//
//			String json = record.toString();
//			Map<String, Object> map = mapper.readValue(json,
//				new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
//				});
//
//			// aspect.value JSON parsing
//			if (map.containsKey("aspect")) {
//
//			    Object aspectObj = map.get("aspect");
//
//			    if (aspectObj instanceof Map) {
//
//				@SuppressWarnings("unchecked")
//				Map<String, Object> aspect = (Map<String, Object>) aspectObj;
//
//				Object value = aspect.get("value");
//
//				if (value instanceof byte[] bytes) {
//
//				    String str = new String(bytes);
//
//				    try {
//					aspect.put("value", mapper.readValue(str, Object.class));
//				    } catch (Exception e) {
//					aspect.put("value", str);
//				    }
//				}
//
//				if (value instanceof String str) {
//
//				    try {
//					aspect.put("value", mapper.readValue(str, Object.class));
//				    } catch (Exception ignored) {
//				    }
//				}
//			    }
//			}
//
//			Map<String, Object> output = new HashMap<>();
//
//			output.put("timestamp", msg.timestamp());
//			output.put("data", map);
//
//			decodedMessages.add(output);
//
//		    } catch (Exception e) {
//
//			System.out.println("Deserialize error: " + e.getMessage());
//		    }
//		}
//	    }
//	}
//
//	consumer.close();
//
//	// ==============================
//	// SORT RESULTS
//	// ==============================
//
//	decodedMessages.sort((a, b) -> Long.compare((Long) b.get("timestamp"), (Long) a.get("timestamp")));
//
//	System.out.println("\nTotal matching messages: " + decodedMessages.size());
//
//	for (int i = 0; i < decodedMessages.size(); i++) {
//
//	    System.out.println("\n----- Message " + (i + 1) + " -----\n");
//
//	    Object data = decodedMessages.get(i).get("data");
//
//	    String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
//
//	    System.out.println(pretty);
//	}
//    }
//}