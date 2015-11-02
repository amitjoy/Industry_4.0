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

import java.util.List;

/**
 * <p>
 * Title: IConfigurationService
 * </p>
 * <p>
 * Description: An interface for a configuration service in an OSGI environment
 * </p>
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public interface IConfigurationService {
	/**
	 * Gets a configuration item
	 *
	 * @param <T>
	 *            the item's class
	 * @param cls
	 *            the item's class
	 * @return the configuration item or null if not item has been set yet
	 */
	public <T extends ConfigurationItem> T get(Class<T> cls);

	/**
	 * Gets a configuration value for a given key
	 *
	 * @param key
	 *            the key
	 * @return the value or null if the configuration item has not been set yet
	 */
	public String get(String key);

	/**
	 * Gets a configuration value for a given key
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the custom default value
	 *
	 * @return the value or the default value if the configuration item has not
	 *         been set yet
	 */
	public String get(String key, String defaultValue);

	/**
	 * Gets a configuration value for a given key
	 *
	 * @param key
	 *            the key
	 *
	 * @return the value as a boolean or <code>null</code>
	 */
	public Boolean getBoolean(String key);

	/**
	 * Gets a configuration value for a given key
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the custom default value
	 *
	 * @return the value or the default value if the configuration item has not
	 *         been set yet
	 */
	public boolean getBoolean(String key, boolean defaultValue);

	/**
	 * Gets a configuration value for a given key as an integer
	 *
	 * @param key
	 *            the key
	 * @return the value as an integer or null if the configuration item has not
	 *         been set
	 * @throws NumberFormatException
	 *             if the saved configuration item cannot be converted to an
	 *             integer
	 */
	public Integer getInt(String key) throws NumberFormatException;

	/**
	 * Gets a configuration value for a given key
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the custom default value
	 *
	 * @return the value or the default value if the configuration item has not
	 *         been set yet
	 *
	 * @throws NumberFormatException
	 *             if the saved configuration item cannot be converted to an
	 *             integer
	 */
	public Integer getInt(String key, Integer defaultValue) throws NumberFormatException;

	/**
	 * Gets a list of configuration values for a given key
	 *
	 * @param key
	 *            the key
	 * @return the value or null if the configuration item has not been set yet
	 */
	public List<String> getList(String key);

	/**
	 * Gets a list of configuration values for a given key
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the custom default value
	 *
	 * @return the value or the default value if the configuration item has not
	 *         been set yet
	 */
	public List<String> getList(String key, List<String> defaultValue);

	/**
	 * Sets a configuration item
	 *
	 * @param key
	 *            the item's key
	 * @param value
	 *            the item's value
	 */
	public void set(String key, String value);

	/**
	 * Sets a configuration item
	 *
	 * @param <T>
	 *            the item's class
	 * @param item
	 *            the item to set
	 */
	public <T extends ConfigurationItem> void set(T item);

	/**
	 * Sets a configuration item as a boolean
	 *
	 * @param key
	 *            the item's key
	 * @param value
	 *            the item's value as a boolean
	 */
	public void setBoolean(String key, boolean value);

	/**
	 * Sets a configuration item as an integer
	 *
	 * @param key
	 *            the item's key
	 * @param value
	 *            the item's value as an integer
	 */
	public void setInt(String key, int value);

	/**
	 * Sets a configuration item
	 *
	 * @param key
	 *            the item's key
	 * @param value
	 *            the item's values
	 */
	public void setList(String key, List<String> value);
}
