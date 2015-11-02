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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Title: BundleTracker
 * </p>
 * <p>
 * Description: Tracks active bundles
 * </p>
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class BundleTracker implements BundleListener {

	/**
	 * The tracking mode
	 */
	public enum TrackingMode {
		/** Track active bundles */
		Active, /** Track resolved bundles */
		Resolved
	}

	private static final Logger log = LoggerFactory.getLogger(BundleTracker.class);

	/**
	 * The bundles that were searched for mapping information
	 */
	private final Set<Bundle> added = new HashSet<Bundle>();

	/**
	 * The bundle context (if tracking is started)
	 */
	private BundleContext context;

	/**
	 * The tracking mode
	 */
	private final TrackingMode mode;

	/**
	 * Constructor
	 *
	 * @param mode
	 *            the tracking mode to use
	 */
	public BundleTracker(final TrackingMode mode) {
		this.mode = mode;
	}

	/**
	 * Add a bundle that may contain mapping information
	 *
	 * @param bundle
	 *            the bundle
	 */
	private void addBundle(final Bundle bundle) {
		synchronized (this.added) {
			if (this.added.contains(bundle)) {
				return;
			} else {
				this.added.add(bundle);
			}
		}

		this.register(bundle);
	}

	/**
	 * @see BundleListener#bundleChanged(BundleEvent)
	 */
	@Override
	public void bundleChanged(final BundleEvent event) {
		switch (this.mode) {
		case Active:
			switch (event.getType()) {
			case BundleEvent.STARTED:
				this.addBundle(event.getBundle());
				break;
			case BundleEvent.STOPPED:
				this.removeBundle(event.getBundle());
				break;
			}
			break;
		case Resolved:
			switch (event.getType()) {
			case BundleEvent.RESOLVED:
				this.addBundle(event.getBundle());
				break;
			case BundleEvent.UNRESOLVED:
				this.removeBundle(event.getBundle());
				break;
			}
			break;
		}
	}

	/**
	 * Called after a bundle has been removed
	 *
	 * @param bundle
	 *            the removed bundle
	 */
	protected abstract void deregister(Bundle bundle);

	/**
	 * Get the bundle context
	 *
	 * @return the bundle context (may be null)
	 */
	public BundleContext getContext() {
		return this.context;
	}

	/**
	 * Called after a bundle was found that was not yet registered
	 *
	 * @param bundle
	 *            the bundle
	 */
	protected abstract void register(Bundle bundle);

	/**
	 * Remove a bundle and its mapping information
	 *
	 * @param bundle
	 *            the bundle to remove
	 */
	private void removeBundle(final Bundle bundle) {
		synchronized (this.added) {
			if (!this.added.contains(bundle)) {
				return;
			}

			this.added.remove(bundle);
		}

		this.deregister(bundle);
	}

	/**
	 * Start the bundle tracker
	 *
	 * @param context
	 *            the bundle context
	 */
	public void start(final BundleContext context) {
		if (this.context != null) {
			this.stop();
		}

		this.context = context;

		log.info("Started tracking bundles (" + this.mode + ").");

		context.addBundleListener(this);

		for (final Bundle bundle : context.getBundles()) {
			switch (this.mode) {
			case Active:
				if ((bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0) {
					this.addBundle(bundle);
				}
				break;
			case Resolved:
				if ((bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE | Bundle.RESOLVED)) != 0) {
					this.addBundle(bundle);
				}
				break;
			}
		}
	}

	/**
	 * Stop bundle tracking and reset the tracker
	 */
	public void stop() {
		if (this.context != null) {
			this.context.removeBundleListener(this);
		}

		final List<Bundle> removed = new ArrayList<Bundle>();
		synchronized (this.added) {
			removed.addAll(this.added);
			this.added.clear();
		}

		// call deregister for remaining bundles
		for (final Bundle bundle : removed) {
			this.deregister(bundle);
		}

		this.context = null;

		log.info("Stopped tracking bundles.");
	}

}
