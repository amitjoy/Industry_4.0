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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.google.common.collect.Lists;

/**
 * Implementation providing a sorted list of services by service ranking.
 *
 * @author AMIT KUMAR MONDAL
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SortingServiceTracker<T> extends ServiceTracker<Object, Object> {

	private int lastCount = -1;

	private int lastRefCount = -1;

	private List<ServiceReference> sortedReferences;

	private List<T> sortedServiceCache;

	/**
	 * Constructor
	 */
	public SortingServiceTracker(final BundleContext context, final String clazz) {
		super(context, clazz, null);
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	@Override
	public Object addingService(final ServiceReference reference) {
		this.sortedServiceCache = null;
		this.sortedReferences = null;
		return this.context.getService(reference);
	}

	/**
	 * Return a sorted list of the services references.
	 */
	public List<ServiceReference> getSortedServiceReferences() {
		List<ServiceReference> result = this.sortedReferences;
		if ((result == null) || (this.lastRefCount < this.getTrackingCount())) {
			this.lastRefCount = this.getTrackingCount();
			final ServiceReference[] references = this.getServiceReferences();
			if ((references == null) || (references.length == 0)) {
				result = Collections.emptyList();
			} else {
				Arrays.sort(references);
				result = Lists.newArrayList();
				for (int i = 0; i < references.length; i++) {
					result.add(references[references.length - 1 - i]);
				}
			}
			this.sortedReferences = result;
		}
		return result;
	}

	/**
	 * Return a sorted list of the services.
	 */
	public List<T> getSortedServices() {
		List<T> result = this.sortedServiceCache;
		if ((result == null) || (this.lastCount < this.getTrackingCount())) {
			this.lastCount = this.getTrackingCount();
			final ServiceReference[] references = this.getServiceReferences();
			if ((references == null) || (references.length == 0)) {
				result = Collections.emptyList();
			} else {
				Arrays.sort(references);
				result = new ArrayList<T>();
				for (int i = 0; i < references.length; i++) {
					final T service = (T) this.getService(references[references.length - 1 - i]);
					if (service != null) {
						result.add(service);
					}
				}
			}
			this.sortedServiceCache = result;
		}
		return result;
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	@Override
	public void modifiedService(final ServiceReference reference, final Object service) {
		this.sortedServiceCache = null;
		this.sortedReferences = null;
	}

	/**
	 * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
	 *      java.lang.Object)
	 */
	@Override
	public void removedService(final ServiceReference reference, final Object service) {
		this.sortedServiceCache = null;
		this.sortedReferences = null;
		this.context.ungetService(reference);
	}
}