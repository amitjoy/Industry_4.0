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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.ServiceReference;

/**
 * <p>
 * Title: MultiServiceTracker
 * </p>
 * <p>
 * Description: Tracks service instances
 * </p>
 *
 * @author AMIT KUMAR MONDAL
 *
 * @param <T>
 *            The Service Type to track
 */
public class MultiServiceTracker<T> extends ServiceTracker<T> {

	private final Set<MultiServiceListener<T>> listeners = new HashSet<MultiServiceListener<T>>();

	private final Map<ServiceReference<T>, T> services = new HashMap<ServiceReference<T>, T>();

	/**
	 * Constructor
	 *
	 * @param serviceClass
	 *            the service type to track
	 */
	public MultiServiceTracker(final Class<T> serviceClass) {
		super(serviceClass);
	}

	/**
	 * Adds a {@link MultiServiceListener}
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addListener(final MultiServiceListener<T> listener) {
		this.listeners.add(listener);
	}

	/**
	 * @see ServiceTracker#deregister(ServiceReference)
	 */
	@Override
	protected void deregister(final ServiceReference<T> service) {
		T serviceInstance;

		synchronized (this.services) {
			serviceInstance = this.services.get(service);
			if (serviceInstance == null) {
				return;
			} else {
				this.services.remove(service);
			}
		}

		for (final MultiServiceListener<T> listener : this.listeners) {
			listener.serviceRemoved(serviceInstance);
		}
	}

	/**
	 * Get the currently available service instances
	 *
	 * @return the set of currently available service instances
	 */
	public Set<T> getServices() {
		Set<T> result;

		synchronized (this.services) {
			result = new HashSet<T>(this.services.values());
		}

		return result;
	}

	/**
	 * @see ServiceTracker#register(ServiceReference)
	 */
	@Override
	protected void register(final ServiceReference<T> service) {
		final T serviceInstance = this.getContext().getService(service);

		if (serviceInstance != null) {
			synchronized (this.services) {
				this.services.put(service, serviceInstance);
			}

			for (final MultiServiceListener<T> listener : this.listeners) {
				listener.serviceAdded(serviceInstance);
			}
		}

	}

	/**
	 * Removes a {@link MultiServiceListener}
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeListener(final MultiServiceListener<T> listener) {
		this.listeners.remove(listener);
	}

}
