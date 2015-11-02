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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.bluetooth.ServiceRecord;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.io.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.tum.in.activity.log.ActivityLogService;
import de.tum.in.activity.log.IActivityLogService;

/**
 * Used to consume all the service records provided by all the paired Bluetooth
 * Enabled Milling Machines
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = false, name = "de.tum.in.bluetooth.milling.machine")
@Service(value = { BluetoothMillingMachine.class })
public class BluetoothMillingMachine extends Cloudlet {

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.bluetooth.milling.machine";

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "MILLING-V1";

	/**
	 * Property for Bluetooth Real-time Topic Namespace
	 */
	private static final String BLUETOOTH_REALTIME_TOPIC = "bluetooth.realtime.topic";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothMillingMachine.class);

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
	 * Connection Service Dependency
	 */
	@Reference(bind = "bindConnectorService", unbind = "unbindConnectorService")
	private volatile ConnectorService m_connectorService;

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
	 * Bluetooth Service Record Dependency for paired bluetooth devices
	 */
	@Reference(bind = "bindServiceRecord", unbind = "unbindServiceRecord", policy = ReferencePolicy.DYNAMIC, target = "(service.name=Bluetooth-Milling-Machine-Simulation)", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private volatile ServiceRecord m_serviceRecord;

	/**
	 * Holds List of Service Record for all the paired devices
	 */
	private final List<ServiceRecord> m_serviceRecords = Lists.newCopyOnWriteArrayList();

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
		LOGGER.info("Activating Bluetooth Milling Machine Component...");

		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);

		LOGGER.info("Activating Bluetooth Milling Machine Component... Done.");
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
	 * Callback to be used while {@link ConnectorService} is registering
	 */
	public synchronized void bindConnectorService(final ConnectorService connectorService) {
		if (this.m_connectorService == null) {
			this.m_connectorService = connectorService;
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
	 * Callback to be used while {@link ServiceRecord} is registering
	 */
	public synchronized void bindServiceRecord(final ServiceRecord serviceRecord) {
		if (!this.m_serviceRecords.contains(serviceRecord)) {
			this.m_serviceRecords.add(serviceRecord);
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
		LOGGER.debug("Deactivating Bluetooth Milling Machine Component...");

		super.deactivate(context);
		this.m_serviceRecords.clear();
		this.m_worker.shutdown();

		LOGGER.debug("Deactivating Bluetooth Milling Machine Component... Done.");
	}

	/** {@inheritDoc} */
	@Override
	protected void doExec(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		if ("start".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Milling Machine Communication Started...");
			this.m_serviceRecords.stream().forEach(serviceRecord -> this.doPublish(serviceRecord));
			this.m_activityLogService.saveLog("Bluetooth Milling Machine Communication Started");
			LOGGER.info("Bluetooth Milling Machine Communication Done");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		if ("stop".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Milling Machine Communication Stopped...");
			this.m_worker.shutdownNow();
			LOGGER.info("Bluetooth Milling Machine Communication Done");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Bluetooth Milling Machine Component GET handler");

		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Milling Machine Configuration Retrieval Started...");

			final ComponentConfiguration configuration = this.m_configurationService
					.getComponentConfiguration(APP_CONF_ID);

			final IterableMap map = (IterableMap) configuration.getConfigurationProperties();
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}
			this.m_activityLogService.saveLog("Bluetooth Milling Machine Configuration Retrieved");

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

			LOGGER.info("Bluetooth Milling Machine Configuration Retrieval Finished");
		}
	}

	/**
	 * Used to publish real-time data retrieved from all the milling machines
	 * and cache it
	 */
	private void doPublish(final ServiceRecord serviceRecord) {

		// cancel a current worker handle if one if active
		if (this.m_handle != null) {
			this.m_handle.cancel(true);
		}

		LOGGER.debug("Publishing Real-time Data Started.....");

		final String remoteDeviceAddress = serviceRecord.getHostDevice().getBluetoothAddress();

		LOGGER.info("Active Bluetooth Data Transfer for " + remoteDeviceAddress);

		final String bluetoothRealtimeTopic = this.m_systemService.getProperties()
				.getProperty(BLUETOOTH_REALTIME_TOPIC);

		final BluetoothConnector bluetoothConnector = new BluetoothConnector.Builder()
				.setConnectorService(this.m_connectorService).setTopic(bluetoothRealtimeTopic)
				.setServiceRecord(serviceRecord).setEventAdmin(this.m_eventAdmin)
				.setCloudClient(this.getCloudApplicationClient()).build();

		this.m_handle = this.m_worker.submit(bluetoothConnector);
	}

	/** {@inheritDoc} */
	@Override
	protected void doPut(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {

		LOGGER.info("Bluetooth Milling Machine Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			this.m_configurationService.updateConfiguration(APP_CONF_ID, reqPayload.metrics());
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		this.m_activityLogService.saveLog("Bluetooth Milling Machine Configuration Updated");

		LOGGER.info("Bluetooth Milling Machine Configuration Updated");
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
	 * Callback to be used while {@link ConnectorService} is deregistering
	 */
	public synchronized void unbindConnectorService(final ConnectorService connectorService) {
		if (this.m_connectorService != null) {
			this.m_connectorService = null;
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
	 * Callback to be used while {@link ServiceRecord} is deregistering
	 */
	public synchronized void unbindServiceRecord(final ServiceRecord serviceRecord) {
		if ((this.m_serviceRecords.size() > 0) && this.m_serviceRecords.contains(serviceRecord)) {
			this.m_serviceRecords.remove(serviceRecord);
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
		LOGGER.info("Updating Bluetooth Milling Machine Component...");
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));
		LOGGER.info("Updating Bluetooth Milling Machine Component... Done.");
	}
}