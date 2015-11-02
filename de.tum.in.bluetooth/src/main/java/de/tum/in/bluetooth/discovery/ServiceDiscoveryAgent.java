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
import java.util.Arrays;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import de.tum.in.bluetooth.constant.ServiceConstants;
import de.tum.in.bluetooth.constant.UUIDs;

/**
 * Discovery Agent searching services for one specific device. If a matching
 * service is found, we publishes an OSGi service matching the service record
 *
 * @author AMIT KUMAR MONDAL
 */
public final class ServiceDiscoveryAgent implements DiscoveryListener, Runnable {

	private static int[] attrIDs = new int[] { ServiceConstants.SERVICE_NAME.serviceId() };

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryAgent.class);

	/**
	 * RFCOMM UUID to discover devices for pairing
	 */
	private static UUID[] searchUuidSet = { UUIDs.RFCOMM.uuid() };

	/**
	 * The target {@link RemoteDevice} to search for {@link ServiceRecord}
	 */
	private final RemoteDevice m_device;

	/**
	 * The List of discovered {@link ServiceRecord}
	 */
	private final List<ServiceRecord> m_discoveredServices = Lists.newArrayList();

	/**
	 * The name of the device to be discovered
	 */
	private String m_name;

	/**
	 * Bluetooth Service Discovery Agent
	 */
	private final BluetoothServiceDiscovery m_parent;

	private boolean m_searchInProgress = false;

	public ServiceDiscoveryAgent(final BluetoothServiceDiscovery bluetoothServiceDiscovery, final RemoteDevice device) {
		this.m_parent = bluetoothServiceDiscovery;
		this.m_device = device;
		try {
			this.m_name = this.m_device.getFriendlyName(false);
		} catch (final IOException e) {
			this.m_name = this.m_device.getBluetoothAddress();
		}
	}

	/***************************************
	 * DiscoveryListener
	 ***************************************/
	/** {@inheritDoc} */
	@Override
	public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
		// Not used here.
	}

	/**
	 * Search for device service records to search
	 *
	 * @param local
	 *            the local device reference
	 */
	void doSearch(final LocalDevice local) {
		synchronized (this) {
			this.m_searchInProgress = true;
			try {

				if (Env.isTestEnvironmentEnabled()) {
					LOGGER.warn("=== TEST ENVIRONMENT ENABLED ===");
				} else {
					final int trans = local.getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, this.m_device,
							this);
					LOGGER.info("Service Search {} started", trans);
				}

				this.wait();
			} catch (final InterruptedException e) {
				if (this.m_searchInProgress) {
					// we're stopping, aborting discovery.
					this.m_searchInProgress = false;
					LOGGER.warn("Interrupting bluetooth service discovery - interruption");
				} else {
					// Search done !
					LOGGER.info("Bluetooth discovery for " + this.m_name + " completed !");
				}
			} catch (final BluetoothStateException e) {
				// well ... bad choice. Bluetooth driver not ready
				// Just abort.
				LOGGER.error("Cannot search for bluetooth services", Throwables.getStackTraceAsString(e));
				this.m_parent.discovered(this.m_device, null);
				return;
			}
			LOGGER.info("Bluetooth discovery for " + this.m_name + " is now completed - injecting "
					+ this.m_discoveredServices.size() + " discovered services ");
			this.m_parent.discovered(this.m_device, this.m_discoveredServices);
		}
	}

	/**
	 * Initialize Bluetooth Adapter
	 */
	private LocalDevice initialize() {
		LocalDevice local = null;
		try {
			local = LocalDevice.getLocalDevice();
			LOGGER.info("Address: " + local.getBluetoothAddress());
			LOGGER.info("Name: " + local.getFriendlyName());
		} catch (final BluetoothStateException e) {
			LOGGER.error("Bluetooth Adapter not started.");
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
			LOGGER.info("Search services on " + this.m_device.getBluetoothAddress() + " " + this.m_name);

			final LocalDevice local = this.initialize();
			if (!LocalDevice.isPowerOn() || (local == null)) {
				LOGGER.error("Bluetooth adapter not ready, aborting service discovery");
				this.m_parent.discovered(this.m_device, null);
				return;
			}

			this.doSearch(local);
		} catch (final Throwable e) {
			LOGGER.error("Unexpected exception during service inquiry", Throwables.getStackTraceAsString(e));
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

		LOGGER.info("Matching service found - " + servRecord.length);
		this.m_discoveredServices.addAll(Arrays.asList(servRecord));
	}

	/** {@inheritDoc} */
	@Override
	public void serviceSearchCompleted(final int transID, final int respCode) {
		synchronized (this) {
			LOGGER.info("Service search completed for device " + this.m_device.getBluetoothAddress());
			this.m_searchInProgress = false;
			this.notifyAll();
		}
	}

}