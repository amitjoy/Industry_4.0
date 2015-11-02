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
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import com.google.common.collect.Maps;

/**
 * Base class to write bundle extenders. This extender tracks started bundles
 * (or starting if they have a lazy activation policy) and will create an
 * {@link Extension} for each of them to manage it.
 *
 * The extender will handle all concurrency and synchronization issues, see
 * {@link Extension} for more information about the additional constraints.
 *
 * The extender guarantee that all extensions will be stopped synchronously with
 * the STOPPING event of a given bundle and that all extensions will be stopped
 * before the extender bundle is stopped.
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class AbstractExtender
		implements BundleActivator, BundleTrackerCustomizer<Object>, SynchronousBundleListener {

	private BundleContext context;
	private final ConcurrentMap<Bundle, FutureTask<Void>> destroying = Maps.newConcurrentMap();
	private ExecutorService executors;
	private final ConcurrentMap<Bundle, Extension> extensions = Maps.newConcurrentMap();

	private boolean preemptiveShutdown;
	private volatile boolean stopped;
	private volatile boolean stopping;
	private boolean synchronous;
	private BundleTracker<Object> tracker;

	@Override
	public Object addingBundle(final Bundle bundle, final BundleEvent event) {
		this.modifiedBundle(bundle, event, bundle);
		return bundle;
	}

	@Override
	public void bundleChanged(final BundleEvent event) {
		if (this.stopped) {
			return;
		}
		final Bundle bundle = event.getBundle();
		if ((bundle.getState() != Bundle.ACTIVE) && (bundle.getState() != Bundle.STARTING)) {
			// The bundle is not in STARTING or ACTIVE state anymore
			// so destroy the context. Ignore our own bundle since it
			// needs to kick the orderly shutdown.
			if (bundle != this.context.getBundle()) {
				this.destroyExtension(bundle);
			}
		}
	}

	/**
	 *
	 * @param bundles
	 * @return
	 */
	protected Collection<Bundle> chooseBundlesToDestroy(final Set<Bundle> bundles) {
		return null;
	}

	/**
	 * Create the executor used to start extensions asynchronously.
	 *
	 * @return an
	 */
	protected ExecutorService createExecutor() {
		return Executors.newScheduledThreadPool(3);
	}

	private void createExtension(final Bundle bundle) {
		try {
			final BundleContext bundleContext = bundle.getBundleContext();
			if (bundleContext == null) {
				// The bundle has been stopped in the mean time
				return;
			}
			final Extension extension = this.doCreateExtension(bundle);
			if (extension == null) {
				// This bundle is not to be extended
				return;
			}
			synchronized (this.extensions) {
				if (this.extensions.putIfAbsent(bundle, extension) != null) {
					return;
				}
			}
			if (this.synchronous) {
				this.debug(bundle, "Starting extension synchronously");
				extension.start();
			} else {
				this.debug(bundle, "Scheduling asynchronous start of extension");
				this.getExecutors().submit(() -> {
					try {
						extension.start();
					} catch (final Exception e) {
						AbstractExtender.this.warn(bundle, "Error starting extension", e);
					}
				});
			}
		} catch (final Throwable t) {
			this.warn(bundle, "Error while creating extension", t);
		}
	}

	protected abstract void debug(Bundle bundle, String msg);

	private void destroyExtension(final Bundle bundle) {
		FutureTask<Void> future;
		synchronized (this.extensions) {
			this.debug(bundle, "Starting destruction process");
			future = this.destroying.get(bundle);
			if (future == null) {
				final Extension extension = this.extensions.remove(bundle);
				if (extension != null) {
					this.debug(bundle, "Scheduling extension destruction");
					future = new FutureTask<Void>(() -> {
						AbstractExtender.this.debug(bundle, "Destroying extension");
						try {
							extension.destroy();
						} catch (final Exception e) {
							AbstractExtender.this.warn(bundle, "Error while destroying extension", e);
						} finally {
							AbstractExtender.this.debug(bundle, "Finished destroying extension");
							synchronized (AbstractExtender.this.extensions) {
								AbstractExtender.this.destroying.remove(bundle);
							}
						}
					} , null);
					this.destroying.put(bundle, future);
				} else {
					this.debug(bundle, "Not an extended bundle or destruction of extension already finished");
				}
			} else {
				this.debug(bundle, "Destruction already scheduled");
			}
		}
		if (future != null) {
			try {
				this.debug(bundle, "Waiting for extension destruction");
				future.run();
				future.get();
			} catch (final Throwable t) {
				this.warn(bundle, "Error while destroying extension", t);
			}
		}
	}

	/**
	 * Create the extension for the given bundle, or null if the bundle is not
	 * to be extended.
	 *
	 * @param bundle
	 *            the bundle to extend
	 * @return
	 * @throws Exception
	 */
	protected abstract Extension doCreateExtension(Bundle bundle) throws Exception;

	protected void doStart() throws Exception {
		this.startTracking();
	}

	protected void doStop() throws Exception {
		this.stopTracking();
	}

	protected abstract void error(String msg, Throwable t);

	public BundleContext getBundleContext() {
		return this.context;
	}

	public ExecutorService getExecutors() {
		return this.executors;
	}

	/**
	 * Check if the extender performs a preemptive shutdown of all extensions
	 * when the framework is being stopped. The default behavior is to wait for
	 * the framework to stop the bundles and stop the extension at that time.
	 *
	 * @return if the extender use a preemptive shutdown
	 */
	public boolean isPreemptiveShutdown() {
		return this.preemptiveShutdown;
	}

	public boolean isStopping() {
		return this.stopping;
	}

	/**
	 * Check if the extender is synchronous or not. If the flag is set, the
	 * extender will start the extension synchronously with the bundle being
	 * tracked or started. Else, the starting of the extension will be delegated
	 * to a thread pool.
	 *
	 * @return if the extender is synchronous
	 */
	public boolean isSynchronous() {
		return this.synchronous;
	}

	@Override
	public void modifiedBundle(final Bundle bundle, final BundleEvent event, final Object object) {
		// If the bundle being stopped is the system bundle,
		// do an orderly shutdown of all blueprint contexts now
		// so that service usage can actually be useful
		if (this.context.getBundle(0).equals(bundle) && (bundle.getState() == Bundle.STOPPING)) {
			if (this.preemptiveShutdown) {
				try {
					this.stop(this.context);
				} catch (final Exception e) {
					this.error("Error while performing preemptive shutdown", e);
				}
				return;
			}
		}
		if ((bundle.getState() != Bundle.ACTIVE) && (bundle.getState() != Bundle.STARTING)) {
			// The bundle is not in STARTING or ACTIVE state anymore
			// so destroy the context. Ignore our own bundle since it
			// needs to kick the orderly shutdown and not unregister the
			// namespaces.
			if (bundle != this.context.getBundle()) {
				this.destroyExtension(bundle);
			}
			return;
		}
		// Do not track bundles given we are stopping
		if (this.stopping) {
			return;
		}
		// For starting bundles, ensure, it's a lazy activation,
		// else we'll wait for the bundle to become ACTIVE
		if (bundle.getState() == Bundle.STARTING) {
			final String activationPolicyHeader = bundle.getHeaders().get(Constants.BUNDLE_ACTIVATIONPOLICY);
			if ((activationPolicyHeader == null) || !activationPolicyHeader.startsWith(Constants.ACTIVATION_LAZY)) {
				// Do not track this bundle yet
				return;
			}
		}
		this.createExtension(bundle);
	}

	@Override
	public void removedBundle(final Bundle bundle, final BundleEvent event, final Object object) {
		// Nothing to do
		this.destroyExtension(bundle);
	}

	public void setPreemptiveShutdown(final boolean preemptiveShutdown) {
		this.preemptiveShutdown = preemptiveShutdown;
	}

	public void setSynchronous(final boolean synchronous) {
		this.synchronous = synchronous;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		this.context = context;
		this.context.addBundleListener(this);
		this.tracker = new BundleTracker<Object>(this.context, Bundle.ACTIVE | Bundle.STARTING, this);
		if (!this.synchronous) {
			this.executors = this.createExecutor();
		}
		this.doStart();
	}

	protected void startTracking() {
		this.tracker.open();
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		this.stopping = true;
		while (!this.extensions.isEmpty()) {
			Collection<Bundle> toDestroy = this.chooseBundlesToDestroy(this.extensions.keySet());
			if ((toDestroy == null) || toDestroy.isEmpty()) {
				toDestroy = new ArrayList<Bundle>(this.extensions.keySet());
			}
			for (final Bundle bundle : toDestroy) {
				this.destroyExtension(bundle);
			}
		}
		this.doStop();
		if (this.executors != null) {
			this.executors.shutdown();
			try {
				this.executors.awaitTermination(60, TimeUnit.SECONDS);
			} catch (final InterruptedException e) {
				// Ignore
			}
			this.executors = null;
		}
		this.stopped = true;
	}

	protected void stopTracking() {
		this.tracker.close();
	}

	protected abstract void warn(Bundle bundle, String msg, Throwable t);

}