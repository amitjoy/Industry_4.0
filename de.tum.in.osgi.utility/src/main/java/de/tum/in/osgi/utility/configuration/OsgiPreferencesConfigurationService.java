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

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

/**
 * <p>
 * Title: PreferenceConfigurationService
 * </p>
 * <p>
 * Description: {@link IConfigurationService} implementation using a
 * {@link PreferencesService}. Default value for all keys is <code>null</code>.
 * </p>
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class OsgiPreferencesConfigurationService extends AbstractDefaultConfigurationService {
	/**
	 * The preferences service
	 */
	private PreferencesService _ps;

	/**
	 * Creates the configuration service
	 *
	 * @param defaultProperties
	 *            the default properties, may be <code>null</code>
	 * @param fallBackToSystemProperties
	 *            if the service may fall back to the system properties for
	 *            retrieving default values
	 */
	public OsgiPreferencesConfigurationService(final Properties defaultProperties,
			final boolean fallBackToSystemProperties) {
		super(defaultProperties, fallBackToSystemProperties);
	}

	/**
	 * @return the preferences service
	 */
	public PreferencesService getPreferencesService() {
		return this._ps;
	}

	/**
	 * @see AbstractConfigurationService#getValue(java.lang.String)
	 */
	@Override
	protected String getValue(final String key) {
		String value;
		if (this._ps == null) {
			value = null;
		} else {
			final Preferences prefs = this._ps.getSystemPreferences();
			try {
				prefs.sync();
			} catch (final BackingStoreException e) {
				// ignore
			}
			value = prefs.get(key, null);
		}

		return value;
	}

	/**
	 * @see AbstractConfigurationService#removeValue(String)
	 */
	@Override
	protected void removeValue(final String key) {
		if (this._ps == null) {
			return;
		}
		final Preferences prefs = this._ps.getSystemPreferences();
		prefs.remove(key);

		// write settings to persistent store
		try {
			prefs.sync();
		} catch (final BackingStoreException e) {
			throw new IllegalStateException("Could not save preferences", e);
		}
	}

	/**
	 * Sets the preferences service
	 *
	 * @param ps
	 *            the service
	 */
	public void setPreferencesService(final PreferencesService ps) {
		this._ps = ps;
	}

	/**
	 * @see AbstractConfigurationService#setValue(String, String)
	 */
	@Override
	protected void setValue(final String key, final String value) {
		if (this._ps == null) {
			return;
		}
		final Preferences prefs = this._ps.getSystemPreferences();
		prefs.put(key, value);

		// write settings to persistent store
		try {
			prefs.sync();
		} catch (final BackingStoreException e) {
			throw new IllegalStateException("Could not save preferences", e);
		}
	}

}
