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
package de.tum.in.python.bluetooth.milling.machine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
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
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import de.tum.in.activity.log.ActivityLogService;
import de.tum.in.activity.log.IActivityLogService;

/**
 * This bundle is used to trigger python program to communicate with the
 * bluetooth milling machine. The primary focus is to control all the milling
 * machines remotely.
 *
 * The scenario we have here that as soon as the client initiates a connection
 * with the server, the bluetooth server starts broadcasting data. The client
 * needs to read the data in real-time and publish it for the business clients
 * to use the data for further usage.
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = false, name = "de.tum.in.python.bluetooth.milling-machine")
@Service(value = { BluetoothMillingMachine.class })
public class BluetoothMillingMachine extends Cloudlet implements ConfigurableComponent {

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.python.bluetooth.milling.machine";

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "PY-MILLING-V1";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothMillingMachine.class);

	/**
	 * Configurable property to set list of bluetooth enabled milling machines
	 * to be discovered
	 */
	private static final String MILLING_MACHINES = "bluetooh.milling.machines";

	/**
	 * Activity Log Service Dependency
	 */
	@Reference(bind = "bindActivityLogService", unbind = "unbindActivityLogService")
	private volatile IActivityLogService m_activityLogService;

	/**
	 * Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Configuration Service Dependency
	 */
	@Reference(bind = "bindConfigurationService", unbind = "unbindConfigurationService")
	private volatile ConfigurationService m_configurationService;

	/**
	 * Future Event Handle for Executor
	 */
	private Future<?> m_handle;

	/**
	 * Holds List of Milling Machines to communicate
	 */
	private final List<String> m_millingMachines = Lists.newCopyOnWriteArrayList();

	/**
	 * Configurable Properties set using Metatype Configuration Management
	 */
	private Map<String, Object> m_properties;

	/**
	 * Eclipse Kura System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/**
	 * Bluetooth Connector Worker Thread
	 */
	private final ExecutorService m_worker;

	/* Constructor */
	public BluetoothMillingMachine() {
		super(APP_ID);
		this.m_worker = Executors.newSingleThreadExecutor();
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating Python Bluetooth Milling Machine Component...");

		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);
		this.m_properties = properties;

		LOGGER.info("Activating Python Bluetooth Milling Machine Component... Done.");
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
		LOGGER.debug("Deactivating Python Bluetooth Milling Machine Component...");

		super.deactivate(context);
		this.m_millingMachines.clear();
		this.m_worker.shutdown();

		LOGGER.debug("Deactivating Python Bluetooth Milling Machine Component... Done.");
	}

	/**
	 *
	 */
	private void doCommunicate(final String macAddress) {
		// cancel a current worker handle if one is active
		if (this.m_handle != null) {
			this.m_handle.cancel(true);
		}
		LOGGER.debug("Initiating Python Bluetooth Milling Machine Communication....");
		this.m_handle = this.m_worker.submit(() -> CommandUtil.initCommunication(macAddress));
	}

	/** {@inheritDoc} */
	@Override
	protected void doExec(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		if ("start".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Python Milling Machine Communication Started...");
			this.extractConfiguration();
			this.m_millingMachines.stream().forEach(macAddress -> this.doCommunicate(macAddress));
			this.m_activityLogService.saveLog("Bluetooth Python Milling Machine Communication Started");
			LOGGER.info("Bluetooth Python Milling Machine Communication Done");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		if ("stop".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Python Milling Machine Communication Stopped...");
			this.m_worker.shutdownNow();
			LOGGER.info("Bluetooth Python Milling Machine Communication Done");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Bluetooth Python Milling Machine Component GET handler");

		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Python Milling Machine Configuration Retrieval Started...");

			final ComponentConfiguration configuration = this.m_configurationService
					.getComponentConfiguration(APP_CONF_ID);

			final IterableMap map = (IterableMap) configuration.getConfigurationProperties();
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}
			this.m_activityLogService.saveLog("Bluetooth Python Milling Machine Configuration Retrieved");

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

			LOGGER.info("Bluetooth Python Milling Machine Configuration Retrieval Finished");
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doPut(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {

		LOGGER.info("Bluetooth Python Milling Machine Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			this.m_configurationService.updateConfiguration(APP_CONF_ID, reqPayload.metrics());
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		this.m_activityLogService.saveLog("Bluetooth Python Milling Machine Configuration Updated");

		LOGGER.info("Bluetooth Python Milling Machine Configuration Updated");
	}

	/**
	 * Extracts provided configuration
	 */
	private void extractConfiguration() {
		final String millingMachines = (String) this.m_properties.get(MILLING_MACHINES);
		final String DEVICE_SPLITTER = "#";
		Iterators.addAll(this.m_millingMachines, Splitter.on(DEVICE_SPLITTER).split(millingMachines).iterator());
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
		LOGGER.info("Updating Python Bluetooth Milling Machine Component...");
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));
		LOGGER.info("Updating Python Bluetooth Milling Machine Component... Done.");
	}
}