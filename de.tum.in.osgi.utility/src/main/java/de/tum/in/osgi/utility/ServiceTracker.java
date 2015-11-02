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

package de.tum.in.osgi.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Title: ServiceTracker
 * </p>
 * <p>
 * Description: Tracks active services of a certain type
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 * @param <T>
 *            The Service Type to track
 */
public abstract class ServiceTracker<T> implements ServiceListener {

	private static final Logger log = LoggerFactory.getLogger(ServiceTracker.class);

	/**
	 * Get the properties of a service
	 *
	 * @param service
	 *            the service reference
	 *
	 * @return a new properties instance containing the service properties
	 */
	public static Properties getProperties(final ServiceReference<?> service) {
		final Properties properties = new Properties();

		for (final String key : service.getPropertyKeys()) {
			properties.setProperty(key, service.getProperty(key).toString());
		}

		return properties;
	}

	/**
	 * The bundles that were searched for mapping information
	 */
	private final Set<ServiceReference<T>> added = new HashSet<ServiceReference<T>>();

	/**
	 * The bundle context (if tracking is started)
	 */
	private BundleContext context;

	/**
	 * If the tracker is running
	 */
	private boolean running;

	/**
	 * The service class
	 */
	private final Class<T> serviceClass;

	/**
	 * Creates a service tracker for the given service class
	 *
	 * @param serviceClass
	 *            the service class
	 */
	public ServiceTracker(final Class<T> serviceClass) {
		this.serviceClass = serviceClass;
	}

	/**
	 * Add a service
	 *
	 * @param service
	 *            the service reference
	 */
	private void addService(final ServiceReference<T> service) {
		synchronized (this.added) {
			if (this.added.contains(service)) {
				return;
			} else {
				this.added.add(service);
			}
		}

		this.register(service);
	}

	/**
	 * Called after a service has been removed
	 *
	 * @param service
	 *            the reference of the removed service
	 */
	protected abstract void deregister(ServiceReference<T> service);

	/**
	 * Get the bundle context
	 *
	 * @return the bundle context (may be null)
	 */
	public BundleContext getContext() {
		return this.context;
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Called after a service was found that was not yet registered
	 *
	 * @param service
	 *            the service reference
	 */
	protected abstract void register(ServiceReference<T> service);

	/**
	 * Remove a service
	 *
	 * @param service
	 *            the service reference
	 */
	private void removeService(final ServiceReference<T> service) {
		synchronized (this.added) {
			if (!this.added.contains(service)) {
				return;
			}

			this.added.remove(service);
		}

		this.deregister(service);
	}

	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void serviceChanged(final ServiceEvent event) {
		switch (event.getType()) {
		case ServiceEvent.MODIFIED:
		case ServiceEvent.REGISTERED:
			this.addService((ServiceReference<T>) event.getServiceReference());
			break;
		case ServiceEvent.UNREGISTERING:
			this.removeService((ServiceReference<T>) event.getServiceReference());
		}
	}

	/**
	 * Start the service tracker
	 *
	 * @param context
	 *            the bundle context
	 */
	public void start(final BundleContext context) {
		if (this.context != null) {
			this.stop();
		}

		this.context = context;

		log.info("Started tracking services: " + this.serviceClass.getName());

		try {
			context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + this.serviceClass.getName() + ")");
		} catch (final InvalidSyntaxException e) {
			log.error("Error adding service listener.", e);
		}

		Collection<ServiceReference<T>> services;
		try {
			services = context.getServiceReferences(this.serviceClass, null);
		} catch (final InvalidSyntaxException e) {
			services = null;
			log.error("Error getting service references.", e);
		}

		if (services != null) {
			for (final ServiceReference<T> service : services) {
				this.addService(service);
			}
		}

		this.running = true;
	}

	/**
	 * Stop bundle tracking and reset the tracker
	 */
	public void stop() {
		if (this.context != null) {
			this.context.removeServiceListener(this);
		}

		final List<ServiceReference<T>> removed = new ArrayList<ServiceReference<T>>();
		synchronized (this.added) {
			removed.addAll(this.added);
			this.added.clear();
		}

		// call deregister for remaining services
		for (final ServiceReference<T> service : removed) {
			this.deregister(service);
		}

		this.context = null;

		this.running = false;

		log.info("Stopped tracking services: " + this.serviceClass.getName());
	}

}
