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

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * <p>
 * Title: OsgiUtilsActivator
 * </p>
 * <p>
 * Description: The activator for the Osgi Utils bundle
 * </p>
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class OsgiUtilsActivator extends AbstractBundleActivator {

	/**
	 * The singleton instance of this activator
	 */
	private static OsgiUtilsActivator instance;

	/**
	 * @return the singleton instance of this activator
	 */
	public static OsgiUtilsActivator getInstance() {
		return instance;
	}

	private final Map<Class<?>, MultiServiceTracker<?>> multiTrackers = new HashMap<Class<?>, MultiServiceTracker<?>>();

	private final Map<Object, ServiceRegistration<?>> registrations = new IdentityHashMap<Object, ServiceRegistration<?>>();

	private final Map<Class<?>, SingleServiceTracker<?>> trackers = new HashMap<Class<?>, SingleServiceTracker<?>>();

	/**
	 * Add a service listener
	 *
	 * @param <T>
	 *            the service type
	 * @param listener
	 *            the listener
	 * @param serviceType
	 *            the service type
	 */
	@SuppressWarnings("unchecked")
	public <T> void addServiceListener(final SingleServiceListener<T> listener, final Class<T> serviceType) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();

		final BundleContext context = instance.getContext();

		SingleServiceTracker<T> tracker;

		synchronized (this.trackers) {
			tracker = (SingleServiceTracker<T>) this.trackers.get(serviceType);
			if (tracker == null) {
				tracker = new SingleServiceTracker<T>(serviceType);
				this.trackers.put(serviceType, tracker);
				tracker.start(context);
			}
		}

		tracker.addListener(listener);
	}

	/**
	 * Get the service with the given type. Only use the returned instance while
	 * you are sure it is still valid.
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @return the available service of this type or null
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService(final Class<T> serviceType) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance == null) {
			return null;
		}

		final BundleContext context = instance.getContext();

		SingleServiceTracker<T> tracker;

		synchronized (this.trackers) {
			tracker = (SingleServiceTracker<T>) this.trackers.get(serviceType);
			if (tracker == null) {
				tracker = new SingleServiceTracker<T>(serviceType);
				this.trackers.put(serviceType, tracker);
				tracker.start(context);
			}
		}

		return tracker.getService();
	}

	/**
	 * Get the services with the given type. Only use the returned instances
	 * while you are sure it is still valid.
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @return the available services of this type or null
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getServices(final Class<T> serviceType) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance == null) {
			return null;
		}

		final BundleContext context = instance.getContext();

		MultiServiceTracker<T> tracker;

		synchronized (this.multiTrackers) {
			tracker = (MultiServiceTracker<T>) this.multiTrackers.get(serviceType);
			if (tracker == null) {
				tracker = new MultiServiceTracker<T>(serviceType);
				this.multiTrackers.put(serviceType, tracker);
				tracker.start(context);
			}
		}

		return tracker.getServices();
	}

	/**
	 * Register a service
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @param service
	 *            the service implementation
	 */
	public <T> void registerService(final Class<T> serviceType, final T service) {
		final ServiceRegistration<T> serviceReg = instance.getContext().registerService(serviceType, service,
				new Hashtable<String, Object>());

		synchronized (this.registrations) {
			this.registrations.put(service, serviceReg);
		}
	}

	/**
	 * Remove a service listener
	 *
	 * @param <T>
	 *            the service type
	 * @param listener
	 *            the listener
	 * @param serviceType
	 *            the service type
	 */
	@SuppressWarnings("unchecked")
	public <T> void removeServiceListener(final SingleServiceListener<T> listener, final Class<T> serviceType) {
		SingleServiceTracker<T> tracker;

		synchronized (this.trackers) {
			tracker = (SingleServiceTracker<T>) this.trackers.get(serviceType);
			if (tracker == null) {
				return;
			}
		}

		tracker.removeListener(listener);
	}

	/**
	 * @see AbstractBundleActivator#start(BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		instance = this;
	}

	/**
	 * @see BundleActivator#stop(BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		instance = null;

		// stop and remove all trackers
		synchronized (this.trackers) {
			for (final SingleServiceTracker<?> tracker : this.trackers.values()) {
				tracker.stop();
			}
			this.trackers.clear();
		}

		// stop and remove all multi service trackers
		synchronized (this.trackers) {
			for (final MultiServiceTracker<?> tracker : this.multiTrackers.values()) {
				tracker.stop();
			}
			this.multiTrackers.clear();
		}
	}

	/**
	 * Unregister a previously registered service
	 *
	 * @param service
	 *            the service implementation
	 */
	public void unregisterService(final Object service) {
		ServiceRegistration<?> serviceReg;

		synchronized (this.registrations) {
			serviceReg = this.registrations.remove(service);
		}

		if (serviceReg != null) {
			serviceReg.unregister();
		}
	}

}
