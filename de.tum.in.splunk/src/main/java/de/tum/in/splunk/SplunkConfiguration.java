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
package de.tum.in.splunk;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bundle is used to configure information bus end-points
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.splunk")
@Service(value = { SplunkConfiguration.class })
public class SplunkConfiguration implements ConfigurableComponent {

	/**
	 * Configurable property to set Bluetooth Real-time Topic Namespace
	 */
	private static final String BLUETOOTH_REALTIME_TOPIC = "bluetooth.realtime.topic";

	/**
	 * Configurable Property to set Activity Events Topic Namespace
	 */
	private static final String EVENT_LOG_TOPIC = "event.log.topic";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SplunkConfiguration.class);

	/**
	 * Configurable Property to set OPC-UA Topic Namespace
	 */
	private static final String OPCUA_REALTIME_TOPIC = "opcua.realtime.topic";

	/**
	 * Configurable Property to set WiFi Topic Namespace
	 */
	private static final String WIFI_REALTIME_TOPIC = "wifi.realtime.topic";

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/* Constructor */
	public SplunkConfiguration() {
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating Splunk Configuration Component...");

		this.m_properties = properties;
		this.setConfiguration();

		LOGGER.info("Activating Splunk Configuration Component... Done.");

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
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating Splunk Configuration Component...");

		LOGGER.debug("Deactivating Splunk Configuration Component... Done.");
	}

	/**
	 * Sets or updates the configuration parameters in {@link SystemService}
	 */
	private void setConfiguration() {
		this.m_systemService.getProperties().put(BLUETOOTH_REALTIME_TOPIC,
				this.m_properties.get(BLUETOOTH_REALTIME_TOPIC));

		this.m_systemService.getProperties().put(OPCUA_REALTIME_TOPIC, this.m_properties.get(OPCUA_REALTIME_TOPIC));

		this.m_systemService.getProperties().put(WIFI_REALTIME_TOPIC, this.m_properties.get(WIFI_REALTIME_TOPIC));

		this.m_systemService.getProperties().put(EVENT_LOG_TOPIC, this.m_properties.get(EVENT_LOG_TOPIC));
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
	 * Used to be called when configurations will get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updating Splunk Configuration Component...");

		this.m_properties = properties;
		this.setConfiguration();
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updating Splunk Configuration Component... Done.");
	}

}