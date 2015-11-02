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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import de.tum.in.bluetooth.constant.ServiceConstants;
import de.tum.in.bluetooth.devices.Device;
import de.tum.in.bluetooth.devices.DeviceList;

/**
 * Component publishing {@link ServiceRecord} for all bluetooth services. This
 * component consumes {@link RemoteDevice} services i.e all discovered bluetooth
 * devices and gets all the bluetooth service profiles of each and every
 * discovered device. For each bluetooth service profile, it publishes a
 * {@link ServiceRecord}.
 *
 * @author AMIT KUMAR MONDAL
 */
@Component(name = "de.tum.in.bluetooth.service.discovery")
public class BluetoothServiceDiscovery {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothServiceDiscovery.class);

	/**
	 * Service Name Attribute ID in Bluetooth Service Record
	 */
	private static final int SERVICE_NAME_ATTRIBUTE = ServiceConstants.SERVICE_NAME.serviceId();

	/**
	 * List of device under attempts.
	 */
	private final Map<RemoteDevice, Integer> m_attempts = Maps.newHashMap();

	/**
	 * Bundle Context.
	 */
	private BundleContext m_context;

	/**
	 * Set of devices loaded from the <tt>configuration</tt>. This contains the
	 * authentication information for the device.
	 */
	@Reference(bind = "bindDeviceListService", policy = ReferencePolicy.DYNAMIC, unbind = "unbindDeviceListService", cardinality = ReferenceCardinality.OPTIONAL_UNARY)
	private volatile DeviceList m_fleet;

	/**
	 * Remote Device Service Injection
	 */
	@Reference(bind = "bindRemoteDevice", unbind = "unbindRemoteDevice", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_UNARY)
	private volatile RemoteDevice m_remoteDevice;

	/**
	 * Map storing the currently registered ServiceRecord(with their
	 * ServiceRegistration) by RemoteDevice
	 */
	@SuppressWarnings("rawtypes")
	private final Map<RemoteDevice, Map<ServiceRecord, ServiceRegistration>> m_servicesRecord = Maps.newHashMap();

	/**
	 * Default Constructor Required for DS.
	 */
	public BluetoothServiceDiscovery() {
	}

	/**
	 * Callback during registration of this DS Service Component
	 *
	 * @param context
	 *            The injected reference for this DS Service Component
	 */
	@Activate
	protected synchronized void activate(final ComponentContext context) {
		LOGGER.info("Activating Bluetooth Service Discovery....");
		this.m_context = context.getBundleContext();
		LOGGER.info("Activating Bluetooth Service Discovery....Done");
	}

	/**
	 * Device Configuration List Service Binding Callback
	 */
	public synchronized void bindDeviceListService(final DeviceList deviceList) {
		if (this.m_fleet == null) {
			this.m_fleet = deviceList;
		}
	}

	/**
	 * A new {@link RemoteDevice} is available. Checks if it implements OBEX, if
	 * so publish the service
	 *
	 * @param device
	 *            the device
	 */
	public synchronized void bindRemoteDevice(final RemoteDevice device) {
		LOGGER.info("Binding Remote Device...." + device);

		if (this.m_remoteDevice == null) {
			this.m_remoteDevice = device;
		}

		try {
			// We can't run searches concurrently.
			final ServiceDiscoveryAgent agent = new ServiceDiscoveryAgent(this, this.m_remoteDevice);
			BluetoothThreadManager.submit(agent);
		} catch (final Exception e) {
			LOGGER.error("Cannot discover services from " + this.m_remoteDevice.getBluetoothAddress(),
					Throwables.getStackTraceAsString(e));
		}
		LOGGER.info("Binding Remote Device....Done" + this.m_remoteDevice.getBluetoothAddress());
	}

	/**
	 * Callback receiving the set of discovered service from the given
	 * {@link RemoteDevice}.
	 *
	 * @param remote
	 *            the RemoteDevice
	 * @param discoveredServices
	 *            the list of ServiceRecord
	 */
	public void discovered(final RemoteDevice remote, final List<ServiceRecord> discoveredServices) {
		if ((discoveredServices == null) || discoveredServices.isEmpty()) {
			this.unregister(remote);

			if (this.retry(remote)) {
				LOGGER.info("Retrying service discovery for device " + remote.getBluetoothAddress() + " - "
						+ this.m_attempts.get(remote));
				this.incrementAttempt(remote);
				final ServiceDiscoveryAgent agent = new ServiceDiscoveryAgent(this, remote);
				BluetoothThreadManager.submit(agent);
			} else {
				// We don't retry, either retry is false or we reached the
				// number of attempts.
				this.m_attempts.remove(remote);
			}
			return;
		}
		LOGGER.info("Agent has discovered " + discoveredServices.size() + " services from "
				+ remote.getBluetoothAddress() + ".");

		// Service discovery successful, we reset the number of attempts.
		this.m_attempts.remove(remote);
		final Device device = this.findDeviceFromFleet(remote);

		for (final ServiceRecord record : discoveredServices) {
			String url;
			if (device == null) {
				url = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			} else {
				url = record.getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
			}

			if (url == null) {
				LOGGER.warn("Can't compute the service url for device " + remote.getBluetoothAddress()
						+ " - Ignoring service record");
			} else {
				final DataElement serviceName = record.getAttributeValue(SERVICE_NAME_ATTRIBUTE);
				if (serviceName != null) {
					LOGGER.info("Service " + serviceName.getValue() + " found " + url);
				} else {
					LOGGER.info("Service found " + url);
				}
				this.register(remote, record, device, url);
			}
		}

	}

	/**
	 * Find the specified Remote Device from cache
	 *
	 * @param remote
	 *            the RemoteDevice
	 */
	private Device findDeviceFromFleet(final RemoteDevice remote) {
		if (this.m_fleet != null) {
			final String address = remote.getBluetoothAddress();
			String sn = null;
			try {
				sn = remote.getFriendlyName(false);
			} catch (final IOException e) {
				// ignore the exception. Just warn it.
				LOGGER.warn(Throwables.getStackTraceAsString(e));
			}

			for (int i = 0; i < this.m_fleet.getDevices().size(); i++) {
				final Device d = this.m_fleet.getDevices().get(i);
				final String regex = d.getId(); // id can be regex.
				if (Pattern.matches(regex, address) || ((sn != null) && Pattern.matches(regex, sn))) {
					return d;
				}
			}
		}
		return null;
	}

	/**
	 * Increments the counter for every attempt
	 */
	private void incrementAttempt(final RemoteDevice remote) {
		LOGGER.info("Attempting to retry..Retry On " + remote.getBluetoothAddress());
		Integer attempt = this.m_attempts.get(remote);
		if (attempt == null) {
			attempt = 1;
			this.m_attempts.put(remote, attempt);
		} else {
			this.m_attempts.put(remote, ++attempt);
		}
	}

	/**
	 * Is used to register {@link ServiceRecord} as an OSGi Service for each and
	 * every {@link RemoteDevice} found as OSGi Service in the Service Registry.
	 *
	 * @param remote
	 *            The Bluetooth Device found
	 * @param serviceRecord
	 *            the selected ServiceRecord to be registered
	 * @param device
	 *            the Device POJO
	 * @param url
	 *            the service record url
	 */
	@SuppressWarnings("rawtypes")
	private synchronized void register(final RemoteDevice remote, final ServiceRecord serviceRecord,
			final Device device, final String url) {
		LOGGER.info("Registering Service Records for " + remote.getBluetoothAddress() + " .....");

		if (!this.m_servicesRecord.containsKey(remote)) {
			this.m_servicesRecord.put(remote, new HashMap<ServiceRecord, ServiceRegistration>());
		}

		final Dictionary<String, Object> props = new Hashtable<String, Object>();

		props.put("device.id", remote.getBluetoothAddress());

		final int[] attributeIDs = serviceRecord.getAttributeIDs();
		if ((attributeIDs != null) && (attributeIDs.length > 0)) {
			for (final int attrID : attributeIDs) {
				if (attrID == SERVICE_NAME_ATTRIBUTE) {
					props.put("service.name", serviceRecord.getAttributeValue(attrID).getValue());
				}
			}
		}

		props.put("service.url", url);

		if (device != null) {
			props.put("fleet.device", device);
		}

		final ServiceRegistration<?> sr = this.m_context.registerService(ServiceRecord.class.getName(), serviceRecord,
				props);
		this.m_servicesRecord.get(remote).put(serviceRecord, sr);

		LOGGER.info("Registering Service Records for " + remote.getBluetoothAddress() + " ......Done");
	}

	/**
	 * Used to reattempt for discovering provided services of the bluetooth
	 * device
	 */
	private boolean retry(final RemoteDevice remote) {
		LOGGER.info("Retrying for service discovery attempt..." + remote);
		final Device device = this.findDeviceFromFleet(remote);
		if (device == null) {
			return true;
		}

		Integer numberOfTries = this.m_attempts.get(remote);
		if (numberOfTries == null) {
			numberOfTries = 0;
		}

		final BigInteger mr = device.getMaxRetry();
		int max = 1;
		if ((mr != null) && (mr.intValue() != 0)) {
			max = mr.intValue();
		}

		LOGGER.info("Retrying for service discovery attempt...Done" + remote);

		return (device.isRetry() && (max >= numberOfTries));
	}

	/**
	 * Stops the discovery. All published services are withdrawn.
	 */
	@Deactivate
	public synchronized void stop() {
		LOGGER.info("Dectivating Bluetooth Service Discovery....");
		this.unregisterAll();
		this.m_attempts.clear();
		LOGGER.info("Deactivating Bluetooth Service Discovery....");
	}

	/**
	 * Device Configuration List Service Service Callback while deregistering
	 */
	public synchronized void unbindDeviceListService(final DeviceList deviceList) {
		if (this.m_fleet == deviceList) {
			this.m_fleet = null;
		}
	}

	/**
	 * A {@link RemoteDevice} disappears. If an OBEX service was published for
	 * this device, the service is unpublished.
	 *
	 * @param device
	 *            the device
	 */
	public synchronized void unbindRemoteDevice(final RemoteDevice device) {
		LOGGER.info("Unbinding Remote Device...." + device);
		this.unregister(device);
		LOGGER.info("Unbinding Remote Device....Done" + device);
	}

	/**
	 * Deregister the provided Service Record
	 */
	@SuppressWarnings("rawtypes")
	private synchronized void unregister(final RemoteDevice remote) {
		LOGGER.info("Deregistering Service Records....");
		final Map<ServiceRecord, ServiceRegistration> services = this.m_servicesRecord.remove(remote);
		if (services == null) {
			return;
		}
		for (final ServiceRegistration<?> sr : services.values()) {
			sr.unregister();
		}
		LOGGER.info("Deregistering Service Records....Done");
	}

	/**
	 * Deregister all Service Records
	 */
	private synchronized void unregisterAll() {
		for (final RemoteDevice remoteDevice : this.m_servicesRecord.keySet()) {
			this.unregister(remoteDevice);
		}
	}

}
