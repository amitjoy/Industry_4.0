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

package de.tum.in.osgi.utility.configuration;

/**
 * <p>
 * Title: NamespaceConfigurationItem
 * </p>
 * <p>
 * Description: Configuration item where all keys will be extended by a
 * namespace. The default namespace is the package of the item class.
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class NamespaceConfigurationItem extends ConfigurationItem {

	/**
	 * Delimiter between namespace and key
	 */
	public static final String DELIMITER = "/";

	private static final long serialVersionUID = 9024126421198212638L;

	/**
	 * Get the configuration namespace
	 *
	 * @return the configuration namespace
	 */
	protected String getNamespace() {
		return this.getClass().getPackage().getName().replace(".", DELIMITER);
	}

	/**
	 * @see ConfigurationItem#load(IConfigurationService)
	 */
	@Override
	protected final void load(final IConfigurationService scs) {
		this.restore(new NamespaceConfigurationServiceDecorator(scs, this.getNamespace(), DELIMITER));
	}

	/**
	 * Fills the configuration item with value retrieved from the configuration
	 * service
	 *
	 * @param scs
	 *            the configuration service
	 */
	protected abstract void restore(IConfigurationService scs);

	/**
	 * @see ConfigurationItem#save(IConfigurationService)
	 */
	@Override
	protected final void save(final IConfigurationService scs) {
		this.store(new NamespaceConfigurationServiceDecorator(scs, this.getNamespace(), DELIMITER));
	}

	/**
	 * Stores the configuration item in the configuration service
	 *
	 * @param scs
	 *            the configuration service
	 */
	protected abstract void store(IConfigurationService scs);

}
