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

import java.io.Serializable;

/**
 * <p>
 * Title: ConfigurationItem
 * </p>
 * <p>
 * Description: An abstract base class for configuration items
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class ConfigurationItem implements Serializable {
	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = -7054533865704881190L;

	/**
	 * Fills the configuration item with values retrieved from the server
	 * configuration service. Note: {@link IConfigurationService#get(Class)}
	 * calls this method. So, this method should use
	 * {@link IConfigurationService#get(String)} to retrieve the configuration
	 * values. This method is hidden, so only the ServerConfigurationService
	 * implementation can access it (via reflections)
	 * 
	 * @param scs
	 *            the server configuration service
	 */
	protected abstract void load(IConfigurationService scs);

	/**
	 * Saves the configuration item to the server configuration service. Note:
	 * {@link IConfigurationService#set(ConfigurationItem)} calls this method.
	 * So, this method should use
	 * {@link IConfigurationService#set(String, String)} to save the
	 * configuration values. This method is hidden, so only the
	 * ServerConfigurationService implementation can access it (via reflections)
	 * 
	 * @param scs
	 *            the server configuration service
	 */
	protected abstract void save(IConfigurationService scs);
}
