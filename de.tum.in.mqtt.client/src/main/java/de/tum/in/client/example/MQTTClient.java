/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tum.in.client.example;

import de.tum.in.client.IKuraMQTTClient;
import de.tum.in.client.KuraMQTTClient;
import de.tum.in.client.adapter.MessageListener;
import de.tum.in.client.message.KuraPayload;

public final class MQTTClient {

	private static IKuraMQTTClient client;
	private static String clientId;
	private static boolean status;

	public static void main(final String... args) {

		if ("PUB".equals(args[0])) {
			clientId = "PUB_CLIENT";
		} else {
			clientId = "SUB_CLIENT";
		}

		// Create the connection object
		client = new KuraMQTTClient.Builder().setHost("m20.cloudmqtt.com").setPort("11143").setClientId(clientId)
				.setUsername("user@email.com").setPassword("iotiwbiot").build();

		// Connect to the Message Broker
		status = client.connect();

		// Retrieve the operation
		final String operation = args[0];

		// Retrieve the topic
		final String topic = args[1];

		switch (operation) {
		case "PUB":
			// Retrive the request id
			final String requestId = args[2];

			if ((topic != null) && (requestId != null)) {
				publish(topic, requestId);
			} else {
				throw new IllegalArgumentException(
						"Make sure you have given input for topic name and request id as well");
			}
			break;

		case "SUB":
			if (topic != null) {
				subscribe(topic);
			}
			break;

		default:
			throw new IllegalArgumentException("Operation Not Supported");
		}

		// Finally disconnect
		client.disconnect();
	}

	private static void publish(final String topic, final String requestId) {
		final KuraPayload payload = new KuraPayload();
		payload.addMetric("request.id", requestId);
		payload.addMetric("requester.client.id", clientId);
		System.out.println("$EDC" + topic);

		if (status) {
			client.publish("$EDC" + topic, payload);
			System.out.println("--------------------------------------------------------------------");
			System.out.println("Request Published");
			System.out.println("Request ID : " + requestId);
			System.out.println("Request Client ID : " + clientId);
			System.out.println("--------------------------------------------------------------------");
		}
	}

	private static void subscribe(final String topic) {
		System.out.println("$EDC" + topic);
		// Subscribe to the topic first
		if (status) {
			client.subscribe("$EDC" + topic, new MessageListener() {

				@Override
				public void processMessage(final KuraPayload payload) {
					System.out.println(payload.metrics());
				}
			});

			System.out.println("Subscribed to channels " + client.getSubscribedChannels());

			System.out.println("Waiting for new messages");

			while (!Thread.currentThread().isInterrupted()) {
			}
		}

	}
}
