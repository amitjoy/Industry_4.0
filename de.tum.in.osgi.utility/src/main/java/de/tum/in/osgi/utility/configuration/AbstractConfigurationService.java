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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: AbstractConfigurationService
 * </p>
 * <p>
 * Description: Implements all basic configuration service methods. Default
 * value for all keys is <code>null</code>.
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class AbstractConfigurationService extends AbstractItemConfigurationService {

	/**
	 * @see IConfigurationService#get(String)
	 */
	@Override
	public String get(final String key) {
		final String value = this.getValue(key);

		if (value == null) {
			return this.getDefault(key);
		} else {
			return value;
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
		final String r = this.get(key);
		if (r == null) {
			return null;
		}
		return Boolean.parseBoolean(r);
	}

	/**
	 * @see IConfigurationService#getBoolean(String, boolean)
	 */
	@Override
	public boolean getBoolean(final String key, final boolean defaultValue) {
		final Boolean value = this.getBoolean(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Get the default value for a key. Override to provide a different default
	 * value than <code>null</code>.<br>
	 *
	 * @param key
	 *            the key
	 *
	 * @return <code>null</code>
	 */
	protected String getDefault(final String key) {
		return null;
	}

	/**
	 * @see IConfigurationService#getInt(String)
	 */
	@Override
	public Integer getInt(final String key) throws NumberFormatException {
		final String r = this.get(key);
		if (r == null) {
			return null;
		}
		return Integer.parseInt(r);
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
		final Integer count = this.getInt(key + "/count");
		if (count == null) {
			return null;
		} else {
			final List<String> result = new ArrayList<String>(count);
			for (int i = 1; i <= count; i++) {
				final String v = this.get(key + "/" + i);
				if (v != null) {
					result.add(v);
				}
			}
			return result;
		}
	}

	/**
	 * @see IConfigurationService#getList(String, List)
	 */
	@Override
	public List<String> getList(final String key, final List<String> defaultValue) {
		final List<String> result = this.getList(key);
		if (result == null) {
			return defaultValue;
		} else {
			return result;
		}
	}

	/**
	 * Get the configuration value for the given key
	 *
	 * @param key
	 *            the key
	 *
	 * @return the value or <code>null</code> if it is not set
	 */
	protected abstract String getValue(String key);

	/**
	 * Remove the value for the given key
	 *
	 * @param key
	 *            the key
	 */
	protected abstract void removeValue(String key);

	/**
	 * @see IConfigurationService#set(String, String)
	 */
	@Override
	public void set(final String key, final String value) {
		if (value == null) {
			this.removeValue(key);
		} else {
			this.setValue(key, value);
		}
	}

	/**
	 * @see IConfigurationService#setBoolean(String, boolean)
	 */
	@Override
	public void setBoolean(final String key, final boolean value) {
		this.set(key, String.valueOf(value));
	}

	/**
	 * @see IConfigurationService#setInt(String, int)
	 */
	@Override
	public void setInt(final String key, final int value) {
		this.set(key, String.valueOf(value));
	}

	/**
	 * @see IConfigurationService#setList(String, List)
	 */
	@Override
	public void setList(final String key, final List<String> value) {
		final Integer count = this.getInt(key + "/count");

		if ((count != null) && ((value == null) || (value.size() < count))) {
			final int start = (value == null) ? (1) : (value.size() + 1);

			// remove values
			this.set(key + "/count", null);
			for (int i = start; i <= count; i++) {
				this.set(key + "/" + i, null);
			}
		}

		if (value != null) {
			this.setInt(key + "/count", value.size());
			int i = 0;
			for (final String v : value) {
				this.set(key + "/" + (++i), v);
			}
		}
	}

	/**
	 * Set the value for the given key
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	protected abstract void setValue(String key, String value);

}
