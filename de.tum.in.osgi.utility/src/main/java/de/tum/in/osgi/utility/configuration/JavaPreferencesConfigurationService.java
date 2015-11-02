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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * <p>
 * Title: JavaPreferencesConfigurationService
 * </p>
 * <p>
 * Description: {@link IConfigurationService} implementation using Java
 * {@link Preferences}
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class JavaPreferencesConfigurationService extends AbstractDefaultConfigurationService {

	/**
	 * The delimiter between namespace and the rest of the key
	 */
	private final String nodeDelimiter;

	/**
	 * The preferences service
	 */
	private final Preferences root;

	/**
	 * Constructor
	 *
	 * @param useSystemPrefs
	 *            if the system root shall be used instead of the user root
	 * @param defaultProperties
	 *            the default properties, may be <code>null</code>
	 * @param fallBackToSystemProperties
	 *            if the service may fall back to the system properties for
	 *            retrieving default values
	 */
	public JavaPreferencesConfigurationService(final boolean useSystemPrefs, final Properties defaultProperties,
			final boolean fallBackToSystemProperties) {
		this(useSystemPrefs, NamespaceConfigurationItem.DELIMITER, defaultProperties, fallBackToSystemProperties);
	}

	/**
	 * Constructor
	 *
	 * @param useSystemPrefs
	 *            if the system root shall be used instead of the user root
	 * @param nodeDelimiter
	 *            the delimiter between namespace and the rest of the key
	 * @param defaultProperties
	 *            the default properties, may be <code>null</code>
	 * @param fallBackToSystemProperties
	 *            if the service may fall back to the system properties for
	 *            retrieving default values
	 */
	public JavaPreferencesConfigurationService(final boolean useSystemPrefs, final String nodeDelimiter,
			final Properties defaultProperties, final boolean fallBackToSystemProperties) {
		super(defaultProperties, fallBackToSystemProperties);

		if (useSystemPrefs) {
			this.root = Preferences.systemRoot();
		} else {
			this.root = Preferences.userRoot();
		}

		this.nodeDelimiter = nodeDelimiter;
	}

	/**
	 * Get the key for a given key, where the namespace is removed
	 *
	 * @param key
	 *            the key
	 *
	 * @return the key without namespace
	 */
	protected String getKey(final String key) {
		final int index = key.lastIndexOf(this.nodeDelimiter);
		if (index < 0) {
			return key;
		} else {
			return key.substring(index + this.nodeDelimiter.length());
		}
	}

	/**
	 * Get the node for a given key
	 *
	 * @param key
	 *            the key
	 *
	 * @return the node for the key
	 */
	protected Preferences getNode(final String key) {
		final int index = key.lastIndexOf(this.nodeDelimiter);
		if (index <= 0) {
			return this.root;
		} else {
			final String nodeName = "/" + key.substring(0, index);
			return this.root.node(nodeName);
		}
	}

	/**
	 * @see AbstractConfigurationService#getValue(String)
	 */
	@Override
	protected String getValue(final String key) {
		final Preferences pref = this.getNode(key);
		try {
			pref.sync();
		} catch (final BackingStoreException e) {
			// ignore
		}
		return pref.get(this.getKey(key), null);
	}

	/**
	 * @see AbstractConfigurationService#removeValue(String)
	 */
	@Override
	protected void removeValue(final String key) {
		final Preferences pref = this.getNode(key);
		pref.remove(this.getKey(key));

		// write settings to persistent store
		try {
			pref.sync();
		} catch (final BackingStoreException e) {
			throw new IllegalStateException("Could not save preferences", e);
		}
	}

	/**
	 * @see AbstractConfigurationService#setValue(String, String)
	 */
	@Override
	protected void setValue(final String key, final String value) {
		final Preferences pref = this.getNode(key);
		pref.put(this.getKey(key), value);

		// write settings to persistent store
		try {
			pref.sync();
		} catch (final BackingStoreException e) {
			throw new IllegalStateException("Could not save preferences", e);
		}
	}

}
