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
package de.tum.in.bluetooth.milling.machine;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.stream.Collectors;

import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.io.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import de.tum.in.events.Events;

/**
 * Used to establish connection between the paired bluetooth device and Gateway
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class BluetoothConnector implements Runnable {

	/**
	 * Builder class to set Service Records and Connector Service
	 */
	public static class Builder {

		/**
		 * builder to build the object
		 */
		public BluetoothConnector build() {
			return new BluetoothConnector();
		}

		/**
		 * Setter to set bluetooth Service record for paired device
		 */
		public Builder setCloudClient(final CloudClient client) {
			s_cloudClient = client;
			return this;
		}

		/**
		 * Setter to set OSGi Connector Service
		 */
		public Builder setConnectorService(final ConnectorService connectorService) {
			s_connectorService = connectorService;
			return this;
		}

		/**
		 * Setter to set Event Admin Service
		 */
		public Builder setEventAdmin(final EventAdmin eventAdmin) {
			s_eventAdmin = eventAdmin;
			return this;
		}

		/**
		 * Setter to set bluetooth Service record for paired device
		 */
		public Builder setServiceRecord(final ServiceRecord record) {
			s_serviceRecord = record;
			return this;
		}

		/**
		 * Setter to set Bluetooth Real-time topic
		 */
		public Builder setTopic(final String topic) {
			s_topic = topic;
			return this;
		}

	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothConnector.class);

	/**
	 * Paired Bluetooth Device Serial Port Profile Service
	 */
	private static CloudClient s_cloudClient;

	/**
	 * OSGi Connector Service
	 */
	@SuppressWarnings("unused")
	private static ConnectorService s_connectorService;

	/**
	 * Event Admin Service
	 */
	private static EventAdmin s_eventAdmin;

	/**
	 * Paired Bluetooth Device Serial Port Profile Service
	 */
	private static ServiceRecord s_serviceRecord;

	/**
	 * Bluetooth Real-time Topic
	 */
	private static String s_topic;

	/**
	 * Buffer Reader for the paired bluetooth device
	 */
	private BufferedReader m_bufferedReader;

	/**
	 * Input Connection Stream for the paired bluetooth device
	 */
	private InputStream m_inputStream;

	/**
	 * The response from the input stream
	 */
	private String m_response;

	/**
	 * Stream connection between paired bluetooth device and RPi
	 */
	private StreamConnection m_streamConnection;

	/** Constructor */
	private BluetoothConnector() {
	}

	/**
	 * Publishes asynchronous events for caching
	 */
	private void doBroadcastEventForCaching(final String data) {
		LOGGER.debug("Publishing Event for caching bluetooth data...");

		final Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("data", data);
		properties.put("timestamp", LocalDateTime.now());
		final Event event = new Event(Events.DATA_CACHE, properties);

		s_eventAdmin.postEvent(event);

		LOGGER.debug("Publishing Event for caching bluetooth data...Done");

	}

	/**
	 * Publish the data to message broker
	 */
	private void doPublish(final String data) throws KuraException {
		this.doBroadcastEventForCaching(this.m_response);

		LOGGER.debug("Publishing Bluetooth Data.....");

		final KuraPayload payload = new KuraPayload();
		payload.addMetric("result", data);

		// publishing for mobile client
		LOGGER.debug("Publishing Bluetooth Data.....to Mobile Clients");
		s_cloudClient.controlPublish("data", payload, 0, false, 5);

		// publishing for Splunk
		LOGGER.debug("Publishing Bluetooth Data.....to Splunk");
		s_cloudClient.controlPublish("splunk", s_topic, data.getBytes(), 0, false, 5);

		LOGGER.debug("Publishing Bluetooth Data.....Done");
	}

	/**
	 * Reading data from the stream
	 */
	private void doRead() {
		try {
			this.m_inputStream = checkNotNull(this.m_streamConnection).openInputStream();
			LOGGER.debug("Input Stream (Bluetooth): " + this.m_inputStream);
			this.readDataFromInputStream();
		} catch (final Exception e) {
			LOGGER.warn(Throwables.getStackTraceAsString(e));
		} finally {
			try {
				checkNotNull(this.m_inputStream).close();
				checkNotNull(this.m_bufferedReader).close();
			} catch (final Exception e) {
				LOGGER.warn("Error closing input stream");
			}
		}
	}

	/**
	 * Getter to retrieve the established input connection
	 */
	public InputStream getInputStream() {
		return this.m_inputStream;
	}

	/**
	 * Reads data from the {@link InputStream}
	 */
	private void readDataFromInputStream() throws IOException {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				this.m_bufferedReader = new BufferedReader(new InputStreamReader(checkNotNull(this.m_inputStream)));
				LOGGER.info("Buffered Reader: " + this.m_bufferedReader);
				LOGGER.info("Buffered Reader Lines List: "
						+ checkNotNull(this.m_bufferedReader).lines().collect(Collectors.toList()));

				this.m_response = this.m_bufferedReader.readLine();
				checkNotNull(this.m_response);
				this.doPublish(this.m_response);
				LOGGER.debug("Data from stream: " + this.m_response);
			}
		} catch (final Exception e) {
			LOGGER.error("Bluetooth Error Occurred " + Throwables.getStackTraceAsString(e));
		}
	}

	/**
	 * Used to establish connection between the paired bluetooth device and
	 * Gateway
	 */
	@Override
	public void run() {
		LOGGER.info("Bluetooth Connection initiating for ... " + s_serviceRecord.getHostDevice().getBluetoothAddress());

		final String connectionURL = s_serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
		try {
			LOGGER.info("Connecting to..." + s_serviceRecord.getHostDevice().getBluetoothAddress()
					+ " with connection url " + connectionURL);
			this.m_streamConnection = (StreamConnection) Connector.open(connectionURL);
			LOGGER.info("Successfully Connected to " + s_serviceRecord.getHostDevice().getBluetoothAddress()
					+ " with stream " + this.m_streamConnection);
		} catch (final IOException e) {
			LOGGER.warn("Not able to connect to the remote device. " + Throwables.getStackTraceAsString(e));
		}
		LOGGER.info("Connection Established with " + s_serviceRecord.getHostDevice().getBluetoothAddress());
		try {
			LOGGER.info("Getting IO Streams for " + s_serviceRecord.getHostDevice().getBluetoothAddress());
			this.doRead();
			LOGGER.debug("Streams Returned-> InputStream: " + this.m_inputStream);
		} catch (final Exception e) {
			LOGGER.warn("Unable to retrieve stream connection for remote device" + Throwables.getStackTraceAsString(e));
		}
	}

}
