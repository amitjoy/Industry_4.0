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
package de.tum.in.heartbeat;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * This is used to broadcast MQTT Heartbeat messages
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = false, name = "de.tum.in.mqtt.heartbeat")
@Service(value = { MQTTHeartbeat.class })
public class MQTTHeartbeat extends Cloudlet implements ConfigurableComponent {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "HEARTBEAT-V1";

	/**
	 * Configurable property to set MQTT Heartbeat Period
	 */
	private static final String HEARTBEAT_PERIOD = "de.tum.in.mqtt.heartbeat.period";

	/**
	 * Configurable property to set MQTT Heartbeat Topic Namespace
	 */
	private static final String HEARTBEAT_TOPIC = "de.tum.in.mqtt.heartbeat.topic";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MQTTHeartbeat.class);

	/**
	 * Eclipse Kura Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Future Event Handle for Executor
	 */
	private ScheduledFuture<?> m_handle;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * Scheduled Thread Pool Executor Reference
	 */
	private final ScheduledExecutorService m_worker;

	/* Constructor */
	public MQTTHeartbeat() {
		super(APP_ID);
		this.m_worker = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating MQTT Heartbeat Component...");

		this.m_properties = properties;
		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);

		try {
			this.doBroadcastHeartbeat(this.m_properties);
		} catch (final KuraException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}

		LOGGER.info("Activating MQTT Heartbeat Component... Done.");

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
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating MQTT Heartbeat Component...");
		super.deactivate(context);
		// shutting down the worker and cleaning up the properties
		this.m_worker.shutdown();
		// Releasing the CloudApplicationClient
		LOGGER.info("Releasing CloudApplicationClient for {}...", APP_ID);
		this.getCloudApplicationClient().release();
		LOGGER.debug("Deactivating MQTT Heartbeat Component... Done.");
	}

	/**
	 * Broadcasts the heartbeat message
	 */
	private void doBroadcastHeartbeat(final Map<String, Object> properties) throws KuraException {

		// cancel a current worker handle if one is active
		if (this.m_handle != null) {
			this.m_handle.cancel(true);
		}
		this.m_handle = this.m_worker.scheduleAtFixedRate(() -> {
			LOGGER.info("Sending MQTT Heartbeat...");
			Thread.currentThread().setName(this.getClass().getSimpleName());
			final KuraPayload kuraPayload = new KuraPayload();
			kuraPayload.addMetric("data", "live");
			try {
				this.getCloudApplicationClient().controlPublish((String) properties.get(HEARTBEAT_TOPIC), kuraPayload,
						DFLT_PUB_QOS, DFLT_RETAIN, DFLT_PRIORITY);
			} catch (final KuraException e) {
				LOGGER.error(Throwables.getStackTraceAsString(e));
			}
		} , 0, (int) properties.get(HEARTBEAT_PERIOD), TimeUnit.SECONDS);
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
	 * Used to be called when configurations will get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updating MQTT Heartbeat Component...");

		this.m_properties = properties;
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		try {
			this.doBroadcastHeartbeat(this.m_properties);
		} catch (final Exception e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		LOGGER.info("Updating MQTT Heartbeat Component... Done.");
	}

}