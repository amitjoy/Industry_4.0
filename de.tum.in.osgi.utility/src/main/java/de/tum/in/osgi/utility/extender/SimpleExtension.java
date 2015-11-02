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

import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Simple example of an extender extension
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class SimpleExtension implements Extension {

	private final Bundle bundle;
	private final BundleContext bundleContext;
	private final AtomicBoolean destroyed = new AtomicBoolean(false);

	public SimpleExtension(final Bundle bundle) {
		this.bundle = bundle;
		this.bundleContext = bundle.getBundleContext();
	}

	@Override
	public void destroy() throws Exception {
		synchronized (this.getLock()) {
			this.destroyed.set(true);
		}
		this.doDestroy();
	}

	protected abstract void doDestroy() throws Exception;

	protected abstract void doStart() throws Exception;

	protected Object getLock() {
		return this;
	}

	public boolean isDestroyed() {
		synchronized (this.getLock()) {
			return this.destroyed.get();
		}
	}

	@Override
	public void start() throws Exception {
		synchronized (this.getLock()) {
			if (this.destroyed.get()) {
				return;
			}
			if (this.bundle.getState() != Bundle.ACTIVE) {
				return;
			}
			if (this.bundle.getBundleContext() != this.bundleContext) {
				return;
			}
			this.doStart();
		}
	}

}