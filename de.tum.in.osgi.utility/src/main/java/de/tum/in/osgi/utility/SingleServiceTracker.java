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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.osgi.framework.ServiceReference;

/**
 * Single Service Tracker
 * 
 * @author AMIT KUMAR MONDAL
 *
 * @param <T>
 *            Service Type
 */
public class SingleServiceTracker<T> extends ServiceTracker<T> {

	private final Set<SingleServiceListener<T>> listeners = new HashSet<SingleServiceListener<T>>();
	private final Set<ServiceReference<T>> queuedServices = new LinkedHashSet<ServiceReference<T>>();

	private T service;

	private ServiceReference<T> serviceRef;

	/**
	 * Constructor
	 *
	 * @param serviceClass
	 *            the service type to track
	 */
	public SingleServiceTracker(final Class<T> serviceClass) {
		super(serviceClass);
	}

	/**
	 * Adds a listener
	 *
	 * @param listener
	 *            the listener
	 */
	public void addListener(final SingleServiceListener<T> listener) {
		this.listeners.add(listener);
	}

	/**
	 * @see ServiceTracker#deregister(ServiceReference)
	 */
	@Override
	protected void deregister(final ServiceReference<T> service) {
		if (this.serviceRef.equals(service)) {
			ServiceReference<T> newService = null;
			synchronized (this.queuedServices) {
				final Iterator<ServiceReference<T>> it = this.queuedServices.iterator();
				if (it.hasNext()) {
					newService = it.next();
				}
			}

			this.updateService(newService);
		} else {
			synchronized (this.queuedServices) {
				this.queuedServices.remove(service);
			}
		}
	}

	/**
	 * Get the current service instance
	 *
	 * @return the service instance (may be null)
	 */
	public T getService() {
		return this.service;
	}

	/**
	 * @see ServiceTracker#register(ServiceReference)
	 */
	@Override
	protected void register(final ServiceReference<T> service) {
		if (this.service == null) {
			// set new service
			this.updateService(service);
		} else {
			// queue service for later
			synchronized (this.queuedServices) {
				this.queuedServices.add(service);
			}
		}
	}

	/**
	 * Removes a listener
	 *
	 * @param listener
	 *            the listener
	 */
	public void removeListener(final SingleServiceListener<T> listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Update the service instance
	 *
	 * @param newService
	 *            the new service reference
	 */
	private void updateService(final ServiceReference<T> newService) {
		if ((this.service != null) && (this.serviceRef != null)) {
			for (final SingleServiceListener<T> listener : this.listeners) {
				listener.beforeServiceRemove(this.service);
			}

			// clean up old service
			this.getContext().ungetService(this.serviceRef);
		}

		this.service = null;
		this.serviceRef = null;

		if (newService != null) {
			this.serviceRef = newService;
			this.service = this.getContext().getService(newService);
		}

		for (final SingleServiceListener<T> listener : this.listeners) {
			listener.afterServiceChange(this.service);
		}
	}

}
