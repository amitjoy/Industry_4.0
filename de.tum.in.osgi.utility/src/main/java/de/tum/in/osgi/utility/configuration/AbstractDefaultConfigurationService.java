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

import java.util.Properties;

/**
 * <p>
 * Title: AbstractDefaultConfigurationService
 * </p>
 * <p>
 * Description: Configuration service that can be initialized with a set of
 * default {@link Properties}
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class AbstractDefaultConfigurationService extends AbstractConfigurationService {

	/**
	 * The default properties
	 */
	private final Properties defaultProperties;

	/**
	 * If the service may fall back to the system properties for retrieving
	 * default values
	 */
	private final boolean fallBackToSystemProperties;

	/**
	 * Create the configuration service. The service will not use the system
	 * properties to determine default values
	 *
	 * @param defaultProperties
	 *            the default properties
	 */
	public AbstractDefaultConfigurationService(final Properties defaultProperties) {
		this(defaultProperties, false);
	}

	/**
	 * Create the configuration service
	 *
	 * @param defaultProperties
	 *            the default properties, may be <code>null</code>
	 * @param fallBackToSystemProperties
	 *            if the service may fall back to the system properties for
	 *            retrieving default values
	 */
	public AbstractDefaultConfigurationService(final Properties defaultProperties,
			final boolean fallBackToSystemProperties) {
		super();

		if (defaultProperties == null) {
			this.defaultProperties = new Properties();
		} else {
			this.defaultProperties = new Properties();
			this.defaultProperties.putAll(defaultProperties);
		}
		this.fallBackToSystemProperties = fallBackToSystemProperties;
	}

	/**
	 * Get the default value using the default properties
	 *
	 * @param key
	 *            the property key
	 */
	@Override
	protected String getDefault(final String key) {
		if (this.fallBackToSystemProperties && !this.defaultProperties.containsKey(key)) {
			// return system property
			return System.getProperty(key);
		} else {
			// return default property
			return this.defaultProperties.getProperty(key);
		}
	}

	/**
	 * Set a default value
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setDefault(final String key, final String value) {
		this.defaultProperties.setProperty(key, value);
	}

}
