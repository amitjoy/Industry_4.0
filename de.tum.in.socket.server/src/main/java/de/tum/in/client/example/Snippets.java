/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
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

/**
 * Example Codes for MQTT Communication
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@SuppressWarnings({ "unused" })
public final class Snippets {

	private static String BLUETOOTH_V1_CONF_PUBLISH = "$EDC/tum/B8:27:EB:A6:A9:8A/BLUETOOTH-V1/GET/configurations";
	private static String BLUETOOTH_V1_CONF_SUBSCRIBE = "$EDC/tum/ANKUR/BLUETOOTH-V1/REPLY/4234216342143261";
	private static String BLUETOOTH_V1_ON_PUBLISH = "$EDC/tum/B8:27:EB:A6:A9:8A/BLUETOOTH-V1/EXEC/start";
	private static String BLUETOOTH_V1_ON_SUBSCRIBE = "$EDC/tum/ANKUR/BLUETOOTH-V1/REPLY/4234216342143261";
	private static IKuraMQTTClient client;
	private static String clientId = "AMIT";
	private static String HEARTBEAT = "$EDC/tum/B8:27:EB:A6:A9:8A/HEARTBEAT-V1/mqtt/heartbeat";
	private static String MILLING_DATA = "$EDC/tum/B8:27:EB:A6:A9:8A/MILLING-V1/data";
	private static String MILLING_V1_ON_PUBLISH = "$EDC/tum/B8:27:EB:A6:A9:8A/MILLING-V1/EXEC/start";
	private static String SOCKET_DATA = "$EDC/tum/B8:27:EB:A6:A9:8A/SOCKET-V1/data";
	private static String SOCKET_V1_ON_SUBSCRIBE = "$EDC/tum/AMIT/SOCKET-V1/REPLY/4234216342143261";
	private static String SOCKET_V1_START = "$EDC/tum/B8:27:EB:A6:A9:8A/SOCKET-V1/EXEC/start";
	private static String SOCKET_V1_STOP = "$EDC/tum/B8:27:EB:A6:A9:8A/SOCKET-V1/EXEC/stop";
	private static boolean status;

	public static void main(final String... args) {
		// Create the connection object
		client = new KuraMQTTClient.Builder().setHost("m20.cloudmqtt.com").setPort("11143")
				.setUsername("user@email.com").setPassword("iotiwbiot").setClientId(clientId).build();

		// Connect to the Message Broker
		status = client.connect();

		// Subscription
		if (status) {
			client.subscribe(MILLING_DATA, new MessageListener() {

				@Override
				public void processMessage(final KuraPayload payload) {
					System.out.println(payload.metrics());
				}
			});

			System.out.println("Subscribed to channels " + client.getSubscribedChannels());

			System.out.println("Waiting for new messages");

		}

		// Payload Generation for Publishing
		final KuraPayload payload = new KuraPayload();
		payload.addMetric("request.id", "4234216342143261");
		payload.addMetric("requester.client.id", clientId);
		System.out.println(status);

		// Publishing
		if (status) {
			client.publish(MILLING_V1_ON_PUBLISH, payload);

			System.out.println("--------------------------------------------------------------------");
			System.out.println("Request Published");
			System.out.println("Request ID : " + "4234216342143261");
			System.out.println("Request Client ID : " + clientId);
			System.out.println("--------------------------------------------------------------------");
		}

		while (!Thread.currentThread().isInterrupted()) {
		}
	}

}