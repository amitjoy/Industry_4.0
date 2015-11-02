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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * <p>
 * Title: AbstractBundleActivator
 * </p>
 * <p>
 * Description: Basic bundle activator
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class AbstractBundleActivator implements BundleActivator {

	private BundleContext context;

	/**
	 * @return the context
	 */
	public BundleContext getContext() {
		return this.context;
	}

	/**
	 * @see BundleActivator#start(BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		this.context = context;
	}

}
