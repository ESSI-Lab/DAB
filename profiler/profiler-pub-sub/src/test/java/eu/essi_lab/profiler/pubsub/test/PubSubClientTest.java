package eu.essi_lab.profiler.pubsub.test;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.json.JSONObject;

import eu.essi_lab.profiler.pubsub.Subscription;
import eu.essi_lab.profiler.pubsub.client.PubSubClient;
import eu.essi_lab.profiler.pubsub.client.PubSubSubscriber;

/**
 * @author Fabrizio
 */
public class PubSubClientTest {

    public static void main(String[] args) throws IOException {

	Subscription subscription = new Subscription();
	subscription.setLabel("Label_" + UUID.randomUUID().toString().substring(0, 4));
	subscription.setClientID(UUID.randomUUID().toString().substring(0, 4));
	subscription.setId(UUID.randomUUID().toString().substring(0, 4));
	subscription.setCreationDate(new Date().getTime());
	subscription.setExpirationDate(subscription.getCreationDate() + 3600000); // 3600000; // 1 hour
	subscription.setInit(true);

	PubSubSubscriber pubSubSubscriber = new PubSubSubscriber() {

	    @Override
	    public void onRetry(String value) {

		System.out.println("Retry: " + value);
	    }

	    @Override
	    public void onReceive(String message) {

		System.out.println(message);
	    }

	    @Override
	    public void onExpiration() {

		System.out.println("Subscription expired");
	    }

	    @Override
	    public void onError() {

		System.out.println("Error occurred");
	    }

	    @Override
	    public void onData(String data) {

		System.out.println(new JSONObject(data.replace("data: ", "").trim()).toString(3));
	    }

	    @Override
	    public void onClose() {

		System.out.println("Subscription canceled");
	    }
	};

	PubSubClient client = new PubSubClient(//
		"http://localhost:8085/gs-service/services/essi/pubsub/subscribe?", //
		subscription, //
		pubSubSubscriber); //

	client.connect();
    }
}
