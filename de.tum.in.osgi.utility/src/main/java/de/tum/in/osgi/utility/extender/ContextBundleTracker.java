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

package de.tum.in.osgi.utility.extender;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;

/**
 * This class tracks bundles and manages tracking-related context information.
 * The tracking context is not related to the OSGi BundleContext.
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class ContextBundleTracker extends BundleTracker {

	/**
	 * map bundle ids to contexts
	 */
	private final Map<Long, Object> _trackingContext = new HashMap<Long, Object>();

	/**
	 * @param mode
	 *            the tracking mode to use
	 */
	public ContextBundleTracker(final TrackingMode mode) {
		super(mode);
	}

	@Override
	protected void deregister(final Bundle bundle) {
		Object context;
		synchronized (this._trackingContext) {
			context = this._trackingContext.get(bundle.getBundleId());
			this._trackingContext.remove(bundle.getBundleId());
		}
		if (context != null) {
			this.unregisterBundleContextual(bundle, context);
		}
	}

	@Override
	protected void register(final Bundle bundle) {
		final Object context = this.registerBundleContextual(bundle);
		if (context == null) {
			return; // don't really track bundle
		}
		synchronized (this._trackingContext) {
			if (this._trackingContext.put(bundle.getBundleId(), context) != null) {
				throw new IllegalStateException("a registration context was already present");
			}
		}
	}

	/**
	 * @param bundle
	 *            the bundle to register
	 * @return an arbitrary non-null context object, or null not to call
	 *         {@link #unregisterBundleContextual(Bundle, Object)}
	 */
	protected abstract Object registerBundleContextual(Bundle bundle);

	/**
	 * @param bundle
	 *            the bundle to unregister
	 * @param context
	 *            the context object returned from registration, never null
	 */
	protected abstract void unregisterBundleContextual(Bundle bundle, Object context);

}
