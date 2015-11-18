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
package de.tum.in.socket.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.isNull;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.data.DataService;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import de.tum.in.activity.log.ActivityLogService;
import de.tum.in.activity.log.IActivityLogService;
import de.tum.in.events.Events;

/**
 * This bundle is responsible for communicating with the Socket Server
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.socket.client")
@Service(value = { SocketClient.class })
public class SocketClient extends Cloudlet implements ConfigurableComponent {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "SOCKET-V1";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SocketClient.class);

	/**
	 * Configurable property to set Socket Server Connection Port Number
	 */
	private static final String SOCKET_CONNECT_PORT = "socket.connect.port";

	/**
	 * Configurable property to set socket server IP address
	 */
	private static final String SOCKET_IP = "socket.server.ip";

	/**
	 * Property for WiFi Real-time Topic Namespace
	 */
	private static final String WIFI_REALTIME_TOPIC = "wifi.realtime.topic";

	/**
	 * Socket Connection Channel
	 */
	private SocketChannel channel;

	/**
	 * Activity Log Service Dependency
	 */
	@Reference(bind = "bindActivityLogService", unbind = "unbindActivityLogService")
	private volatile IActivityLogService m_activityLogService;

	/**
	 * Eclipse Kura Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Eclipse Kura Configuration Service Dependency
	 */
	@Reference(bind = "bindConfigurationService", unbind = "unbindConfigurationService")
	private volatile ConfigurationService m_configurationService;

	/**
	 * Eclipse Kura Data Service Dependency
	 */
	@Reference(bind = "bindDataService", unbind = "unbindDataService")
	private volatile DataService m_dataService;

	/**
	 * OSGi Event Admin Service Dependency
	 */
	@Reference(bind = "bindEventAdmin", unbind = "unbindEventAdmin")
	private volatile EventAdmin m_eventAdmin;

	/**
	 * Future Event Handle for Executor
	 */
	private Future<?> m_handle;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * Placeholder for socket IP Address
	 */
	private String m_socketIPAddress;

	/**
	 * Placeholder for socket Port
	 */
	private int m_socketPort;

	/**
	 * Eclipse Kura System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/**
	 * Data Receiver Worker Thread
	 */
	private final ExecutorService m_worker;

	/* Constructor */
	public SocketClient() {
		super(APP_ID);
		this.m_worker = Executors.newSingleThreadExecutor();
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating Socket Client Component...");

		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);
		this.m_properties = properties;

		LOGGER.info("Activating Socket Client Component... Done.");

	}

	/**
	 * Callback to be used while {@link ActivityLogService} is registering
	 */
	public synchronized void bindActivityLogService(final IActivityLogService activityLogService) {
		if (this.m_activityLogService == null) {
			this.m_activityLogService = activityLogService;
		}
	}

	/**
	 * Callback to be used while {@link CloudService} is registering
	 */
	public synchronized void bindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == null) {
			super.setCloudService(this.m_cloudService = cloudService);
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is registering
	 */
	public synchronized void bindConfigurationService(final ConfigurationService configurationService) {
		if (this.m_configurationService == null) {
			this.m_configurationService = configurationService;
		}
	}

	/**
	 * Callback to be used while {@link DataService} is registering
	 */
	public synchronized void bindDataService(final DataService dataService) {
		if (this.m_dataService == null) {
			this.m_dataService = dataService;
		}
	}

	/**
	 * Callback to be used while {@link EventAdmin} is registering
	 */
	public synchronized void bindEventAdmin(final EventAdmin eventAdmin) {
		if (this.m_eventAdmin == null) {
			this.m_eventAdmin = eventAdmin;
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is registering
	 */
	public synchronized void bindSystemService(final SystemService systemService) {
		if (this.m_systemService == null) {
			this.m_systemService = systemService;
		}
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating Socket Client Component...");
		LOGGER.debug("Deactivating Socket Component... Done.");
	}

	/**
	 * Publishes asynchronous events for caching
	 */
	private void doBroadcastEventsForCaching(final String message) {
		LOGGER.debug("Publishing Event for caching wifi data...");

		final Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("data", message);
		properties.put("timestamp", LocalDateTime.now());
		final Event event = new Event(Events.DATA_CACHE, properties);

		this.m_eventAdmin.postEvent(event);

		LOGGER.debug("Publishing Event for caching wifi data...Done");
	}

	/** {@inheritDoc}} */
	@Override
	protected void doExec(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		if ("start".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Socket Communication Started...");

			this.extractConfiguration();

			// cancel a current worker handle if one is active
			if (this.m_handle != null) {
				this.m_handle.cancel(true);
			}
			final Runnable task = () -> {
				try {
					this.doProcess(respPayload);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			};
			this.m_handle = this.m_worker.submit(task);

			LOGGER.info("Socket Communication Done");
		}

		if ("stop".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Socket Communication Stopped...");
			this.m_worker.shutdownNow();
			LOGGER.info("Socket Communication Done");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}
	}

	/** {@inheritDoc}} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Socket Client Configuration Retrieving...");
		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			final ComponentConfiguration configuration = this.m_configurationService.getComponentConfiguration(APP_ID);

			final IterableMap map = new HashedMap(configuration.getConfigurationProperties());
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}
			this.m_activityLogService.saveLog("Socket Client Configuration Retrieved");

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("Socket Client Configuration Retrieved");
	}

	/**
	 * Process the receiving of data from server
	 */
	private void doProcess(final KuraResponsePayload respPayload) throws KuraException {
		this.m_activityLogService.saveLog("Socket Communication Started");

		String message = null;
		try {
			if (isNull(this.channel)) {
				this.channel = SocketChannel.open();
			}

			// we open this channel in non blocking mode
			if (!isNull(this.channel) && !this.channel.isConnectionPending()) {
				this.channel.configureBlocking(false);
				this.channel.connect(new InetSocketAddress(this.m_socketIPAddress, this.m_socketPort));
			}

			while (!this.channel.finishConnect()) {
				// No need to log. Still connecting to server.
			}
			while (!Thread.currentThread().isInterrupted()) {
				final ByteBuffer bufferA = ByteBuffer.allocate(500);
				message = "";
				while ((this.channel.read(bufferA)) > 0) {
					bufferA.flip();
					message += Charset.defaultCharset().decode(bufferA);
				}
				if (message.length() > 0) {
					LOGGER.info("Message Received: " + message);
				}
				if (!Strings.isNullOrEmpty(message)) {
					this.doPublish(respPayload, message);
				}
			}
		} catch (final Exception e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
	}

	/**
	 * Publishes the data to cloud
	 */
	private void doPublish(final KuraResponsePayload respPayload, final String message) throws KuraException {
		final KuraPayload payload = new KuraPayload();
		payload.addMetric("result", checkNotNull(message));

		this.doBroadcastEventsForCaching(message);

		// Publish for Mobile Clients
		LOGGER.debug("Publishing WiFi Data.....to Mobile Clients");
		this.getCloudApplicationClient().controlPublish("data", payload, DFLT_PUB_QOS, DFLT_RETAIN, DFLT_PRIORITY);

		// Publish for Splunk DWH
		LOGGER.debug("Publishing WiFi Data.....to Splunk");
		this.m_systemService.getProperties().getProperty(WIFI_REALTIME_TOPIC);
		this.m_dataService.publish("$EDC/tum/splunk/data/dump", message.getBytes(), DFLT_PUB_QOS, DFLT_RETAIN,
				DFLT_PRIORITY);
		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
	}

	/** {@inheritDoc}} */
	@Override
	protected void doPut(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Socket Client Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			this.m_configurationService.updateConfiguration(APP_ID, reqPayload.metrics());

			this.m_activityLogService.saveLog("Socket Client Configuration Updated");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("Socket Client Configuration Updated");
	}

	/**
	 * Extracts configuration for populating placeholders with respective values
	 */
	private void extractConfiguration() {
		LOGGER.debug("Extracting Socket Client Configuration....");
		this.m_socketIPAddress = (String) this.m_properties.get(SOCKET_IP);
		this.m_socketPort = (int) this.m_properties.get(SOCKET_CONNECT_PORT);
		LOGGER.debug("Extracting Socket Client Configuration....Done");
	}

	/**
	 * Callback to be used while {@link ActivityLogService} is deregistering
	 */
	public synchronized void unbindActivityLogService(final IActivityLogService activityLogService) {
		if (this.m_activityLogService == activityLogService) {
			this.m_activityLogService = null;
		}
	}

	/**
	 * Callback to be used while {@link CloudService} is deregistering
	 */
	public synchronized void unbindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == cloudService) {
			super.setCloudService(this.m_cloudService = null);
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is deregistering
	 */
	public synchronized void unbindConfigurationService(final ConfigurationService configurationService) {
		if (this.m_configurationService == configurationService) {
			this.m_configurationService = null;
		}
	}

	/**
	 * Callback to be used while {@link DataService} is deregistering
	 */
	public synchronized void unbindDataService(final DataService dataService) {
		if (this.m_dataService == dataService) {
			this.m_dataService = null;
		}
	}

	/**
	 * Callback to be used while {@link EventAdmin} is deregistering
	 */
	public synchronized void unbindEventAdmin(final EventAdmin eventAdmin) {
		if (this.m_cloudService == eventAdmin) {
			this.m_eventAdmin = null;
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is deregistering
	 */
	public synchronized void unbindSystemService(final SystemService systemService) {
		if (this.m_systemService == systemService) {
			this.m_systemService = null;
		}
	}

	/**
	 * Used to be called when configurations get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updating Socket Client Component...");

		this.m_properties = properties;
		this.extractConfiguration();

		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updating Socket Client Component... Done.");
	}

}