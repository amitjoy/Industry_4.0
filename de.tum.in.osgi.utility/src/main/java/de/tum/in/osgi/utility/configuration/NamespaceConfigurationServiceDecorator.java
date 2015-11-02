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
 * Title: NamespaceConfigurationServiceDecorator
 * </p>
 * <p>
 * Description: Decorator for an {@link IConfigurationService} that extends all
 * keys by a namespace. If there is no value for the extended key, the normal
 * key will used to try to get a default value
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class NamespaceConfigurationServiceDecorator extends AbstractItemConfigurationService {

	/**
	 * The decorated configuration service
	 */
	private final IConfigurationService configurationService;

	/**
	 * The delimiter between namespace and key
	 */
	private final String delimiter;

	/**
	 * The namespace
	 */
	private final String namespace;

	/**
	 * Constructor
	 *
	 * @param configurationService
	 *            the inner configuration service
	 * @param namespace
	 *            the namespace
	 * @param delimiter
	 *            the delimiter
	 */
	public NamespaceConfigurationServiceDecorator(final IConfigurationService configurationService,
			final String namespace, final String delimiter) {
		super();
		this.configurationService = configurationService;
		this.namespace = namespace;
		this.delimiter = delimiter;
	}

	/**
	 * Extend the key with the namespace
	 *
	 * @param key
	 *            the key
	 *
	 * @return the extended key
	 */
	protected String extendKey(final String key) {
		return this.namespace + this.delimiter + key;
	}

	/**
	 * @see IConfigurationService#get(String)
	 */
	@Override
	public String get(final String key) {
		final String result = this.configurationService.get(this.extendKey(key));
		if (result == null) {
			// fall back to normal key
			return this.configurationService.get(key);
		} else {
			return result;
		}
	}

	/**
	 * @see IConfigurationService#get(String, String)
	 */
	@Override
	public String get(final String key, final String defaultValue) {
		final String value = this.get(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * @see IConfigurationService#getBoolean(String)
	 */
	@Override
	public Boolean getBoolean(final String key) {
		final Boolean result = this.configurationService.getBoolean(this.extendKey(key));
		if (result == null) {
			// fall back to normal key
			return this.configurationService.getBoolean(key);
		} else {
			return result;
		}
	}

	/**
	 * @see IConfigurationService#getBoolean(String, boolean)
	 */
	@Override
	public boolean getBoolean(final String key, final boolean defaultValue) {
		final Boolean value = this.getBoolean(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * @see IConfigurationService#getInt(String)
	 */
	@Override
	public Integer getInt(final String key) throws NumberFormatException {
		final Integer result = this.configurationService.getInt(this.extendKey(key));
		if (result == null) {
			// fall back to normal key
			return this.configurationService.getInt(key);
		} else {
			return result;
		}
	}

	/**
	 * @see IConfigurationService#getInt(String, Integer)
	 */
	@Override
	public Integer getInt(final String key, final Integer defaultValue) throws NumberFormatException {
		final Integer value = this.getInt(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * @see IConfigurationService#getList(String)
	 */
	@Override
	public List<String> getList(final String key) {
		return this.configurationService.getList(this.extendKey(key));
	}

	/**
	 * @see IConfigurationService#getList(String, List)
	 */
	@Override
	public List<String> getList(final String key, final List<String> defaultValue) {
		return this.configurationService.getList(this.extendKey(key), defaultValue);
	}

	/**
	 * @see IConfigurationService#set(String, String)
	 */
	@Override
	public void set(final String key, final String value) {
		this.configurationService.set(this.extendKey(key), value);
	}

	/**
	 * @see IConfigurationService#setBoolean(String, boolean)
	 */
	@Override
	public void setBoolean(final String key, final boolean value) {
		this.configurationService.setBoolean(this.extendKey(key), value);
	}

	/**
	 * @see IConfigurationService#setInt(String, int)
	 */
	@Override
	public void setInt(final String key, final int value) {
		this.configurationService.setInt(this.extendKey(key), value);
	}

	/**
	 * @see IConfigurationService#setList(String, List)
	 */
	@Override
	public void setList(final String key, final List<String> value) {
		this.configurationService.setList(this.extendKey(key), value);
	}

}
