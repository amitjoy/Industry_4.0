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
package de.tum.in.client;

import java.util.Set;

import com.google.common.annotations.Beta;

import de.tum.in.client.adapter.MessageListener;
import de.tum.in.client.message.KuraPayload;

/**
 * The client interface used to operate on MQTT Calls
 *
 * @author AMIT KUMAR MONDAL
 */
@Beta
public interface IKuraMQTTClient {

	/**
	 * Connection related exception
	 *
	 */
	class ConnectionException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ConnectionException(final String message) {
			super(message);
		}
	}

	/**
	 * Protocol
	 */
	public static final String PROTOCOL = "tcp";

	/**
	 * Connect to Message Broker
	 */
	public boolean connect();

	/**
	 * Disconnect the client from the broker
	 */
	public void disconnect();

	/**
	 * Returns the connected client id
	 */
	public String getClientId();

	/**
	 * Returns the connected host
	 */
	public String getHost();

	/**
	 * Returns the channels the client is currently subscribed to.
	 *
	 * @return set of channels the client is currently subscribed to
	 */
	public Set<String> getSubscribedChannels();

	/**
	 * Checks whether the client is connected to Message Broker
	 */
	public boolean isConnected();

	/**
	 * Publish a message to a channel
	 *
	 * @param channel
	 *            the channel we are publishing to
	 * @param message
	 *            the message we are publishing
	 */
	public void publish(final String channel, KuraPayload payload);

	/**
	 * Subscribes to a channel and registers a callback that is fired every time
	 * a new message is published on the channel.
	 *
	 * @param channel
	 *            the channel we are subscribing to
	 * @param callback
	 *            the callback to be fired whenever a message is received on
	 *            this channel
	 */
	public void subscribe(final String channel, final MessageListener callback);

	/**
	 * Unsubscribes from a channel.
	 *
	 * @param channel
	 *            the channel we are unsubscribing to
	 */
	public void unsubscribe(String channel);

}
