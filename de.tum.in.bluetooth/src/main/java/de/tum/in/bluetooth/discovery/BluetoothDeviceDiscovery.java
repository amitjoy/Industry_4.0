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
package de.tum.in.bluetooth.discovery;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
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
import org.eclipse.kura.watchdog.CriticalComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intel.bluetooth.RemoteDeviceHelper;

import de.tum.in.activity.log.ActivityLogService;
import de.tum.in.activity.log.IActivityLogService;
import de.tum.in.bluetooth.BluetoothController;
import de.tum.in.bluetooth.constant.UUIDs;
import de.tum.in.bluetooth.devices.Device;
import de.tum.in.bluetooth.devices.DeviceList;

/**
 * Component Discovering bluetooth device periodically and publishing a
 * {@link RemoteDevice} service per found device. Notice that bluetooth is an
 * active discovery protocol which means that device departures and arrivals are
 * detected periodically. So interacting with a bluetooth device can throw
 * {@link IOException} at any time. If bluetooth is not available, the component
 * just stops. Inquiries can not be run concurrently.
 *
 * @author AMIT KUMAR MONDAL
 */
@Component(policy = ConfigurationPolicy.REQUIRE, name = "de.tum.in.bluetooth")
@Service(value = { BluetoothController.class })
public class BluetoothDeviceDiscovery extends Cloudlet
		implements BluetoothController, ConfigurableComponent, CriticalComponent {

	/**
	 * Bluetooth discovery mode (inquiry).
	 */
	public enum DiscoveryMode {
		// Global inquiry
		GIAC,
		// Local Inquiry
		LIAC
	}

	/**
	 * Used to search for new set of reachable bluetooth devices
	 *
	 * @see BluetoothDeviceDiscovery#discovered(Set)
	 */
	private class ServiceCheckAgent implements Runnable, DiscoveryListener {

		private final int m_action;

		private final RemoteDevice m_device;

		private final Logger m_logger = LoggerFactory.getLogger(ServiceCheckAgent.class);

		private boolean m_searchInProgress = false;

		public ServiceCheckAgent(final RemoteDevice remoteDevice, final int action) {
			if ((action != SERVICECHECK_REGISTER_IF_HERE) && (action != SERVICECHECK_UNREGISTER_IF_NOT_HERE)) {
				throw new IllegalArgumentException();
			}
			this.m_device = remoteDevice;
			this.m_action = action;
		}

		/*
		 *
		 * ********* DiscoveryListener **********
		 */
		@Override
		public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
			// Not used here.
		}

		void doSearch(final LocalDevice local) {
			synchronized (this) {
				this.m_searchInProgress = true;
				try {

					if (Env.isTestEnvironmentEnabled()) {
						this.m_logger.warn("=== TEST ENVIRONMENT ENABLED ===");
					} else {
						final UUID[] searchUuidSet = { UUIDs.PUBLIC_BROWSE_GROUP.uuid() };
						local.getDiscoveryAgent().searchServices(null, searchUuidSet, this.m_device, this);
					}
					this.wait();
				} catch (final InterruptedException e) {
					if (this.m_searchInProgress) {
						// we're stopping, aborting discovery.
						this.m_searchInProgress = false;
						this.m_logger.warn("Interrupting bluetooth service discovery - interruption");
					} else {
						// Search done !
					}
				} catch (final BluetoothStateException e) {
					// well ... bad choice. Bluetooth driver not ready
					// Just abort.
					this.m_logger.error("Cannot search for bluetooth services", e);
					BluetoothDeviceDiscovery.this.unregister(this.m_device);
					return;
				}
				// Do nothing
			}
		}

		private LocalDevice initialize() {
			LocalDevice local = null;
			try {
				local = LocalDevice.getLocalDevice();
			} catch (final BluetoothStateException e) {
				this.m_logger.error("Bluetooth Adapter not started.");
			}
			return local;
		}

		/** {@inheritDoc} */
		@Override
		public void inquiryCompleted(final int discType) {
			// Not used here.
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {
				final LocalDevice local = this.initialize();
				if (!LocalDevice.isPowerOn() || (local == null)) {
					this.m_logger.error("Bluetooth adapter not ready");
					BluetoothDeviceDiscovery.this.unregister(this.m_device);
					return;
				}
				this.doSearch(local);
			} catch (final Throwable e) {
				this.m_logger.error("Unexpected exception during service inquiry", e);
				BluetoothDeviceDiscovery.this.unregister(this.m_device);
			}
		}

		/** {@inheritDoc} */
		@Override
		public void servicesDiscovered(final int transID, final ServiceRecord[] servRecord) {
			synchronized (this) {
				if (!this.m_searchInProgress) {
					// We were stopped.
					this.notifyAll();
					return;
				}
			}
			// Do nothing
		}

		/** {@inheritDoc} */
		@Override
		public void serviceSearchCompleted(final int transID, final int respCode) {
			if (respCode != SERVICE_SEARCH_COMPLETED) {
				if (this.m_action == SERVICECHECK_UNREGISTER_IF_NOT_HERE) {
					this.m_logger.info(
							"Device " + this.m_device.getBluetoothAddress() + " have disappeared : Unregister it.");
					BluetoothDeviceDiscovery.this.unregister(this.m_device);
				} else if (this.m_action == SERVICECHECK_REGISTER_IF_HERE) {
					this.m_logger.info("Device " + this.m_device.getBluetoothAddress() + " is not here");
				}
			} else {
				if (this.m_action == SERVICECHECK_REGISTER_IF_HERE) {
					this.m_logger.info("Device " + this.m_device.getBluetoothAddress() + " is here : Register it.");
					BluetoothDeviceDiscovery.this.register(this.m_device);
				} else if (this.m_action == SERVICECHECK_UNREGISTER_IF_NOT_HERE) {
					this.m_logger.info("Device " + this.m_device.getBluetoothAddress() + " is still here.");
				}
			}

			synchronized (this) {
				this.m_searchInProgress = false;
				this.notifyAll();
			}
		}
	}

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.bluetooth";

	/**
	 * Defines Application ID for Pi's bluetooth application
	 */
	private static final String APP_ID = "BLUETOOTH-V1";

	/**
	 * Configurable property to set list of bluetooth enabled devices to be
	 * discovered
	 */
	private static final String DEVICES = "bluetooh.discovery.devices";

	/**
	 * Configurable property to get all the configurations for the remote
	 * bluetooth devices
	 */
	private static final String DEVICES_LIST = "bluetooh.devices";

	/**
	 * Configurable property to set search filter for list of bluetooth enabled
	 * devices to be discovered
	 */
	private static final String DEVICES_LIST_FILTER = "bluetooh.devices.filter";

	/**
	 * Configurable property specifying the discovery mode among GIAC and LIAC.
	 */
	private static final String DISCOVERY_MODE = "bluetooth.discovery.mode";

	/**
	 * Configuration property enabling the support of unnamed devices. Unnamed
	 * devices do not communicate their name.
	 */
	private static final String IGNORE_UNNAMED_DEVICES = "bluetooth.ignore.unnamed.devices";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothDeviceDiscovery.class);

	/**
	 * This configuration property enables the online check when a device is
	 * found. It turns around the Windows 7 behavior, where the device discovery
	 * returns all paired devices even if they are not reachable anymore.
	 * However it introduces a performance cost ( a service discovery for each
	 * cached device on every discovery search). It should be used in
	 * combination with <tt>bluetooth.discovery.unpairOnDeparture</tt>.
	 */
	private static final String ONLINE_CHECK_ON_DISCOVERY = "bluetooth.discovery.onlinecheck";

	/**
	 * Configurable Property specifying the time between two inquiries. This
	 * time is specified in <b>second</b>, and should be carefully chosen. Too
	 * many inquiries flood the network and block correct discovery. A too big
	 * period, makes the device dynamism hard to track.
	 */
	private static final String PERIOD = "bluetooth.discovery.period";

	private static final int SERVICECHECK_REGISTER_IF_HERE = 1;

	private static final int SERVICECHECK_UNREGISTER_IF_NOT_HERE = 0;

	/**
	 * All the supported bluetooth stacks in Service Gateway
	 */
	private static final List<String> SUPPORTED_STACKS = Arrays.asList("winsock", "widcomm", "mac", "bluez"); // "bluez-dbus"

	/**
	 * Watchdog Critical Timeout Component
	 */
	private static final int TIMEOUT_COMPONENT = 10;

	/**
	 * Configuration property enabling the unpairing of matching devices (filter
	 * given in the fleet description) when they are not reachable anymore.
	 */
	private static final String UNPAIR_LOST_DEVICES = "bluetooth.discovery.unpairOnDeparture";

	/**
	 * Checks whether the given list contains the given device. The check is
	 * based on the bluetooth address.
	 *
	 * @param list
	 *            a non-null list of remote device
	 * @param device
	 *            the device to check
	 * @return {@code true} if the device is in the list, {@çode false}
	 *         otherwise.
	 */

	public static boolean contains(final Set<RemoteDevice> list, final RemoteDevice device) {
		for (final RemoteDevice d : list) {
			if (d.getBluetoothAddress().equals(device.getBluetoothAddress())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Activity Log Service Dependency
	 */
	@Reference(bind = "bindActivityLogService", unbind = "unbindActivityLogService")
	private volatile IActivityLogService m_activityLogService;

	/**
	 * Device Discovery Agent to handle bluetooth enabled device detection
	 */
	private DeviceDiscoveryAgent m_agent;

	/**
	 * Kura Cloud Service Injection
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Eclipse Kura Configuration Service Dependency
	 */
	@Reference(bind = "bindConfigurationService", unbind = "unbindConfigurationService")
	private volatile ConfigurationService m_configurationService;

	/**
	 * Bundle Context.
	 */
	private BundleContext m_context;

	/**
	 * Map storing the currently exposed bluetooth device.
	 */
	private final Map<RemoteDevice, ServiceRegistration<?>> m_devices = Maps.newHashMap();

	/**
	 * Placeholder for M_DEVICES_LIST
	 */
	private String m_devicesList;

	/**
	 * Placeholder for M_DEVICES_LIST_FILTER
	 */
	private String m_devicesListFilter;

	/**
	 * Placeholder for M_DISCOVERY_MODE
	 */
	private DiscoveryMode m_discoveryMode;

	/**
	 * The fleet device filter (regex configured in the configuration).
	 */
	private Pattern m_filter;

	/**
	 * Set of devices loaded from the <tt>devices</tt> configuration property.
	 * This contains the authentication information for the device.
	 */
	private DeviceList m_fleet;

	/**
	 * Placeholder for M_IGNORE_UNNAMED_DEVICES
	 */
	private boolean m_ignoreUnnamedDevices;

	/**
	 * Map storing the MAC address to name association (ex.
	 * AXERWSD3452U=MY_BLUETOOTH_NAME). It avoids ignoring unnamed devices, as
	 * once we get a name, it is stored in this list. This map can be persisted
	 * if the device name file is set.
	 */
	private Properties m_names;

	/**
	 * Placeholder for M_ONLINE_CHECK_ON_DISCOVERY
	 */
	private boolean m_onlineCheckOnDiscovery;

	/**
	 * Placeholder for M_PERIOD
	 */
	private int m_period;

	/**
	 * Configurable Properties set using Metatype Configuration Management
	 */
	private Map<String, Object> m_properties;

	/**
	 * Placeholder for M_UNPAIR_LOST_DEVICES
	 */
	private boolean m_unpairLostDevices;

	/**
	 * Creates a {@link BluetoothDeviceDiscovery}.
	 */
	public BluetoothDeviceDiscovery() {
		super(APP_ID);
	}

	/**
	 * Creates a {@link BluetoothDeviceDiscovery}.
	 *
	 * @param context
	 *            the bundle context
	 */
	public BluetoothDeviceDiscovery(final BundleContext context) {
		super(APP_ID);
		this.m_context = checkNotNull(context, "Bluetooth Bundle Context must not be null");
	}

	/**
	 * Callback while this component is getting registered
	 *
	 * @param properties
	 *            the service configuration properties
	 */
	@Activate
	protected synchronized void activate(final ComponentContext context, final Map<String, Object> properties) {
		LOGGER.info("Activating Bluetooth Component....");
		super.setCloudService(this.m_cloudService);
		super.activate(context);
		this.m_properties = properties;
		this.m_context = context.getBundleContext();
		LOGGER.info("Activating Bluetooth Component... Done.");
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
	 * Kura Cloud Service Binding Callback
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
	 * Callback while this component is getting deregistered
	 *
	 * @param properties
	 *            the service configuration properties
	 */
	@Deactivate
	@Override
	protected synchronized void deactivate(final ComponentContext componentContext) {
		LOGGER.info("Deactivating Bluetooth Component....");
		super.deactivate(componentContext);
		this.stop();
		LOGGER.info("Deactivating Bluetooth Component....Done");
	}

	/**
	 * Callback receiving the new set of reachable devices.
	 *
	 * @param discovered
	 *            the set of found RemoteDevice
	 */
	public void discovered(final Set<RemoteDevice> discovered) {
		if (discovered == null) {
			// Bluetooth error, we unregister all devices
			LOGGER.warn("Bluetooth error detected, unregistering all devices");
			this.unregisterAll();
			return;
		}

		// Detect devices that have left
		// We must create a copy of the list to avoid concurrent modifications
		Set<RemoteDevice> presents = Sets.newHashSet(this.m_devices.keySet());
		for (final RemoteDevice old : presents) {
			LOGGER.info("Have we lost connection with " + old.getBluetoothAddress() + " => "
					+ (!contains(discovered, old)));
			if (!contains(discovered, old)) {
				final ServiceCheckAgent serviceCheckAgent = new ServiceCheckAgent(old,
						SERVICECHECK_UNREGISTER_IF_NOT_HERE);
				BluetoothThreadManager.submit(serviceCheckAgent);
			}
		}

		// Detect new devices
		for (final RemoteDevice remote : discovered) {
			if (!this.m_devices.containsKey(remote)) {
				if (this.matchesDeviceFilter(remote)) {
					LOGGER.info("New device found (" + remote.getBluetoothAddress() + ")");
					this.register(remote); // register service as RemoteDevice
				} else {
					LOGGER.info("Device ignored because it does not match the device filter");
				}
			} else {
				LOGGER.info("Already known device " + remote.getBluetoothAddress());
			}
		}

		if ("bluez".equals(this.getBluetoothStack())) {

			// Workaround for bluez : trying to keep all the paired devices.
			// bluez doesn't return the paired devices when we have an
			// inquiry, we can try to search if some of the
			// cached devices are reachable

			LocalDevice local = null;

			try {
				local = LocalDevice.getLocalDevice();
			} catch (final BluetoothStateException e) {
				LOGGER.error("Bluetooth Adapter not started.");
			}

			local = checkNotNull(local, "Local Device can not be null");

			final RemoteDevice[] cachedDevices = local.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.CACHED);

			if ((cachedDevices == null) || (cachedDevices.length == 0)) {
				return;
			}

			presents = Sets.newHashSet(this.m_devices.keySet());

			for (final RemoteDevice cached : cachedDevices) {
				if (!contains(presents, cached)) {
					final ServiceCheckAgent serviceCheckAgent = new ServiceCheckAgent(cached,
							SERVICECHECK_REGISTER_IF_HERE);
					BluetoothThreadManager.submit(serviceCheckAgent);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doExec(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {

		switch (reqTopic.getResources()[0]) {
		case "start":
			this.start();
			this.m_activityLogService.saveLog("Bluetooth Started");
			break;

		case "stop":
			this.stop();
			this.m_activityLogService.saveLog("Bluetooth Stopped");
			break;
		}

		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Bluetooth Configuration Retrieving...");
		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			final ComponentConfiguration configuration = this.m_configurationService
					.getComponentConfiguration(APP_CONF_ID);

			final IterableMap map = new HashedMap(configuration.getConfigurationProperties());
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}
			this.m_activityLogService.saveLog("Bluetooth Configuration Retrieved");

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("Bluetooth Configuration Retrieved");
	}

	/** {@inheritDoc} */
	@Override
	protected void doPut(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Bluetooth Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			this.m_configurationService.updateConfiguration(APP_CONF_ID, reqPayload.metrics());

			this.m_activityLogService.saveLog("Bluetooth Configuration Updated");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("Bluetooth Configuration Updated");
	}

	/**
	 * Extracting required configuration for the bluetooth device discovery
	 */
	private void extractRequiredConfigurations() {

		LOGGER.info("Extracting Required Configurations...");

		this.m_period = (int) this.m_properties.get(PERIOD);
		this.m_ignoreUnnamedDevices = (boolean) this.m_properties.get(IGNORE_UNNAMED_DEVICES);
		this.m_onlineCheckOnDiscovery = (boolean) this.m_properties.get(ONLINE_CHECK_ON_DISCOVERY);
		this.m_unpairLostDevices = (boolean) this.m_properties.get(UNPAIR_LOST_DEVICES);
		this.m_devicesList = (String) this.m_properties.get(DEVICES_LIST);
		this.m_devicesListFilter = (String) this.m_properties.get(DEVICES_LIST_FILTER);

		if ("device-filter".equals(this.m_devicesListFilter)) {
			this.m_filter = null;
		} else {
			this.m_filter = Pattern.compile(this.m_devicesListFilter);
		}

		if ((Integer) this.m_properties.get(DISCOVERY_MODE) == 0) {
			this.m_discoveryMode = DiscoveryMode.GIAC;
		} else {
			this.m_discoveryMode = DiscoveryMode.LIAC;
		}

		this.m_names = this.loadListOfDevicesToBeDiscovered((String) this.m_properties.get(DEVICES));
		this.loadAutoPairingConfiguration(this.m_devicesList);

		if (this.m_period == 0) {
			this.m_period = 10; // Default to 10 seconds.
		}

		LOGGER.info("Configuration Extraction Complete");
	}

	@Override
	public String getBluetoothStack() {
		return LocalDevice.getProperty("bluecove.stack");
	}

	/** {@inheritDoc} */
	@Override
	public String getCriticalComponentName() {
		return APP_ID;
	}

	/** {@inheritDoc} */
	@Override
	public int getCriticalComponentTimeout() {
		return TIMEOUT_COMPONENT;
	}

	/**
	 * Returns the user-defined friendly name of the {@link RemoteDevice}
	 */
	private String getDeviceName(final RemoteDevice device) {
		String name = this.m_names.getProperty(device.getBluetoothAddress());
		if (name == null) {
			try {
				name = device.getFriendlyName(false);
				if ((name != null) && (name.length() != 0)) {
					LOGGER.info("New device name discovered : " + device.getBluetoothAddress() + " => " + name);
					this.m_names.setProperty(device.getBluetoothAddress(), name);
				}
			} catch (final IOException e) {
				LOGGER.info("Not able to get the device friendly name of " + device.getBluetoothAddress(), e);
			}
		} else {
			LOGGER.info("Found the device name in memory : " + device.getBluetoothAddress() + " => " + name);
		}
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isBluetoothDeviceTurnedOn() {
		return LocalDevice.isPowerOn();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isBluetoothStackSupported() {
		return SUPPORTED_STACKS.contains(this.getBluetoothStack());
	}

	/** Used to get all the configurations for the remote bluetooth devices */
	private void loadAutoPairingConfiguration(final String deviceList) {
		if (deviceList == null) {
			this.m_fleet = null;
			LOGGER.warn("No device configuration found, ignoring auto-pairing and device filter");
		} else {
			final List<String> devices = Lists.newArrayList();
			Device device = null;
			this.m_fleet = new DeviceList();

			final String DEVICE_SPLITTER = "#";
			Iterators.addAll(devices, Splitter.on(DEVICE_SPLITTER).split(deviceList).iterator());
			for (final String deviceStr : devices) {
				final String SEPARATOR = ";";
				final String NEW_LINE = "\n";

				final Splitter splitter = Splitter.on(SEPARATOR).omitEmptyStrings().trimResults();
				final Joiner stringDevicesJoiner = Joiner.on(NEW_LINE).skipNulls();

				final Properties properties = new Properties();

				final String deviceAsPropertiesFormat = stringDevicesJoiner.join(splitter.splitToList(deviceStr));

				if (isNullOrEmpty(deviceAsPropertiesFormat.toString())) {
					LOGGER.error("No Bluetooth Enabled Device Addess Found");
				}

				try {
					properties.load(new StringReader(deviceAsPropertiesFormat));
				} catch (final IOException e) {
					LOGGER.error("Error while parsing list of input bluetooth devices");
				}

				device = new Device();
				device.setId(properties.getProperty("id"));
				device.setUsername(properties.getProperty("username"));
				device.setPassword(properties.getProperty("password"));
				device.setPin(properties.getProperty("pin"));
				device.setRetry(Boolean.valueOf(properties.getProperty("retry")));
				device.setMaxRetry(new BigInteger(properties.getProperty("max-retry")));
				this.m_fleet.getDevices().add(device);
			}
		}
	}

	/**
	 * Used to parse configuration set to discover specified bluetooth enabled
	 * devices
	 *
	 * @param devices
	 *            The Configuration input as Property K-V Format
	 * @return the parsed input as properties
	 */
	private Properties loadListOfDevicesToBeDiscovered(final String devices) {
		final String SEPARATOR = "#";
		final String NEW_LINE = "\n";

		final Splitter splitter = Splitter.on(SEPARATOR).omitEmptyStrings().trimResults();
		final Joiner stringDevicesJoiner = Joiner.on(NEW_LINE).skipNulls();

		final Properties properties = new Properties();

		final String deviceAsPropertiesFormat = stringDevicesJoiner.join(splitter.splitToList(devices));

		if (isNullOrEmpty(deviceAsPropertiesFormat.toString())) {
			LOGGER.error("No Bluetooth Enabled Device Addess Found");
			return properties;
		}

		try {
			properties.load(new StringReader(deviceAsPropertiesFormat));
		} catch (final IOException e) {
			LOGGER.error("Error while parsing list of input bluetooth devices");
		}
		return properties;
	}

	/**
	 * Filter used to search for remote bluetooth devices. Regular expression
	 * character is allowed.
	 *
	 * @param device
	 *            the current device found
	 * @return true if matches the filter pattern
	 */
	public boolean matchesDeviceFilter(final RemoteDevice device) {
		if (this.m_filter == null) {
			// No filter... all devices are accepted
			return true;
		}

		final String address = device.getBluetoothAddress();
		final String name = this.getDeviceName(device);

		return (this.m_filter.matcher(address).matches() || ((name != null) && this.m_filter.matcher(name).matches()));
	}

	/** {@inheritDoc} */
	@Override
	public void onConnectionEstablished() {
		LOGGER.info("Connected to Message Broker");
	}

	/** {@inheritDoc} */
	@Override
	public void onConnectionLost() {
		LOGGER.info("Disconnected from Message Broker");
	}

	/**
	 * Used to pair {@link RemoteDevice}
	 *
	 * @param device
	 *            The currently discovered Remote Device
	 * @return if paired then true else false
	 */
	private boolean pair(final RemoteDevice device) {
		if ((this.m_fleet == null) || (this.m_fleet.getDevices() == null)) {
			LOGGER.info("Ignoring autopairing - no fleet configured");
			return true;
		}

		final String address = device.getBluetoothAddress();
		final String name = this.getDeviceName(device);

		if ((name == null) && this.m_ignoreUnnamedDevices) {
			LOGGER.warn("Pairing not attempted, ignoring unnamed devices");
			return false;
		}

		final List<Device> devices = this.m_fleet.getDevices();
		for (final Device model : devices) {
			final String regex = model.getId();
			final String pin = model.getPin();
			if (Pattern.matches(regex, address) || ((name != null) && Pattern.matches(regex, name))) {
				LOGGER.info("Paring pattern match for " + address + " / " + name + " with " + regex);
				try {
					LOGGER.info("Device " + address + " pairing started..");
					final boolean authStatus = RemoteDeviceHelper.authenticate(device, pin);
					LOGGER.info("Device (" + address + ") Pairing Authentication Status: " + authStatus);
					return true;
				} catch (final IOException e) {
					LOGGER.error("Cannot authenticate device despite it matches the regex " + regex, e);
				}
			}
		}
		return false;
	}

	/**
	 * Used to register a service per device discovered
	 *
	 * @param device
	 *            The found device
	 */
	private synchronized void register(RemoteDevice device) {
		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("device.id", device.getBluetoothAddress());
		final String name = this.getDeviceName(device);

		if (name != null) {
			// Switch device to our own implementation
			device = new RemoteNamedDevice(device, name);
			props.put("device.name", name);
		} else if (this.m_ignoreUnnamedDevices) {
			LOGGER.warn("Ignoring device " + device.getBluetoothAddress() + " - discovery set to ignore "
					+ "unnamed devices");
			return;
		}

		LOGGER.info("Registering new service for " + device.getBluetoothAddress() + " with properties " + props);

		// check autopairing
		if (!device.isAuthenticated()) {
			if (!this.pair(device)) {
				LOGGER.warn("Aborting registering for " + device.getBluetoothAddress());
				return;
			}
		}

		final ServiceRegistration<?> reg = this.m_context.registerService(RemoteDevice.class.getName(), device, props);
		this.m_devices.put(device, reg);

	}

	/**
	 * Registering devices configuration as an OSGi service
	 */
	private void registerDeviceListFleetAsService() {
		this.m_context.registerService(DeviceList.class, this.m_fleet, null);
	}

	/**
	 * Initializes the discovery.
	 */
	@Override
	public void start() {
		LOGGER.info("Enabling Bluetooth...");

		this.extractRequiredConfigurations();
		this.registerDeviceListFleetAsService();

		if (this.m_agent != null) {
			return;
		}

		if (!this.isBluetoothStackSupported()) {
			LOGGER.error(
					"The Bluetooth stack " + this.getBluetoothStack() + " is not supported (" + SUPPORTED_STACKS + ")");
			return;
		}

		if ("winsock".equals(this.getBluetoothStack())) {
			LOGGER.info("Winsock stack detected, forcing online check and lost device unpairing");
			this.m_onlineCheckOnDiscovery = true;
			this.m_unpairLostDevices = true;
		}

		this.m_agent = new DeviceDiscoveryAgent(this, this.m_discoveryMode, this.m_onlineCheckOnDiscovery);
		BluetoothThreadManager.scheduleJob(this.m_agent, this.m_period);
	}

	/**
	 * Stops the bluetooth discovery
	 */
	@Override
	public void stop() {
		LOGGER.info("Disabling Bluetooth...");
		if (this.m_agent == null) {
			return;
		}
		this.m_agent = null;
		BluetoothThreadManager.stopScheduler();
		this.unregisterAll();
		LOGGER.info("Disabling Bluetooth...Done");
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
	 * Kura Cloud Service Callback while deregistering
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
	 * Unpair the {@link RemoteDevice}
	 */
	private void unpair(final RemoteDevice device) {
		if (this.matchesDeviceFilter(device) && this.m_unpairLostDevices) {
			try {
				RemoteDeviceHelper.removeAuthentication(device);
			} catch (final IOException e) {
				LOGGER.error("Can't unpair device " + device.getBluetoothAddress(), e);
			}
		}
	}

	/**
	 * Deregister the {@link RemoteDevice} OSGi Service and unpair the device
	 */
	private synchronized void unregister(final RemoteDevice device) {
		final ServiceRegistration<?> reg = this.m_devices.remove(device);
		if (reg != null) {
			reg.unregister();
		}
		this.unpair(device);
	}

	/**
	 * Unregister and unpair all the services
	 */
	private synchronized void unregisterAll() {
		for (final Map.Entry<RemoteDevice, ServiceRegistration<?>> entry : this.m_devices.entrySet()) {
			entry.getValue().unregister();
			this.unpair(entry.getKey());
		}
		this.m_devices.clear();
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updating Bluetooth Component...");

		this.m_properties = properties;
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updating Bluetooth Component... Done.");
	}
}
