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
package de.tum.in.socket.server;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.tum.in.client.IKuraMQTTClient;
import de.tum.in.client.KuraMQTTClient;
import de.tum.in.client.message.KuraPayload;

/**
 * Socket Server Instance
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class SocketServer {

	private static String channelType = "channelType";
	private static IKuraMQTTClient client;
	private static String clientChannel = "clientChannel";

	private static SocketChannel clientSocketChannel;

	/**
	 * List of data to send
	 */
	private static List<RealtimeData> data;

	private static String serverChannel = "serverChannel";

	/**
	 * ServerSocketChannel represents a channel for sockets that listen to
	 * incoming connections.
	 */
	public static void main(final String... args) throws IOException {
		final int port = Integer.valueOf(args[1]);
		final String ipAddress = args[0];

		data = ReadExcel.read();
		client = new KuraMQTTClient.Builder().setHost("m20.cloudmqtt.com").setPort("11143")
				.setUsername("user@email.com").setPassword("iotiwbiot").setClientId("WIFI-SERVER").build();

		// Connect to the Message Broker
		final boolean status = client.connect();
		System.out.println("MQTT Communication Agent->" + client);
		System.out.println("MQTT Communication Agent Status->" + status);
		if (status && Objects.nonNull(data)) {
			final KuraPayload payload = new KuraPayload();
			payload.addMetric("request.id", "26452154872");
			payload.addMetric("requester.client.id", "WIFI-SERVER");
			client.publish("$EDC/tum/B8:27:EB:A6:A9:8A/SOCKET-V1/EXEC/start", payload);
		}

		final ServerSocketChannel channel = ServerSocketChannel.open();

		channel.bind(new InetSocketAddress(ipAddress, port));

		channel.configureBlocking(false);

		final Selector selector = Selector.open();

		final SelectionKey socketServerSelectionKey = channel.register(selector, SelectionKey.OP_ACCEPT);
		final Map<String, String> properties = new HashMap<String, String>();
		properties.put(channelType, serverChannel);
		socketServerSelectionKey.attach(properties);
		for (;;) {

			if (selector.select() == 0) {
				continue;
			}
			final Set<SelectionKey> selectedKeys = selector.selectedKeys();
			final Iterator<SelectionKey> iterator = selectedKeys.iterator();
			while (iterator.hasNext()) {
				final SelectionKey key = iterator.next();
				if (((Map<?, ?>) key.attachment()).get(channelType).equals(serverChannel)) {
					final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
					if (isNull(clientSocketChannel)) {
						clientSocketChannel = serverSocketChannel.accept();
					}

					if (clientSocketChannel != null) {
						clientSocketChannel.configureBlocking(false);
						final SelectionKey clientKey = clientSocketChannel.register(selector, SelectionKey.OP_READ,
								SelectionKey.OP_WRITE);
						final Map<String, String> clientproperties = new HashMap<String, String>();
						clientproperties.put(channelType, clientChannel);
						clientKey.attach(clientproperties);

						while (!Thread.currentThread().isInterrupted()) {
							try {
								final ByteBuffer buf = ByteBuffer.allocate(500);
								final CharBuffer cbuf = buf.asCharBuffer();
								System.out.println(
										"Broadcasting data..." + data.get(new Random().nextInt(300)).toString());
								cbuf.put(data.get(new Random().nextInt(300)).toString());
								cbuf.flip();
								clientSocketChannel.write(buf);
								buf.clear();
								TimeUnit.SECONDS.sleep(3);
							} catch (final InterruptedException e) {
								e.printStackTrace();
							}
						}
					}

				} else {
					final ByteBuffer buffer = ByteBuffer.allocate(20);
					final SocketChannel clientChannel = (SocketChannel) key.channel();
					int bytesRead = 0;
					if (key.isReadable()) {
						if ((bytesRead = clientChannel.read(buffer)) > 0) {
							buffer.flip();
							System.out.println(Charset.defaultCharset().decode(buffer));
							buffer.clear();
						}
						if (bytesRead < 0) {
							clientChannel.close();
						}
					}

				}

				iterator.remove();

			}
		}

	}
}