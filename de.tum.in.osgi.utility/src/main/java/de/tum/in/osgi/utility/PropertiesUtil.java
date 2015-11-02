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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The <code>PropertiesUtil</code> is a utility class providing some useful
 * utility methods for converting property types.
 *
 * @author AMIT KUMAR MONDAL
 */
public final class PropertiesUtil {

	private static boolean isEmpty(final String str) {
		return (str == null) || (str.length() == 0);
	}

	/**
	 * Returns the boolean value of the parameter or the
	 * <code>defaultValue</code> if the parameter is <code>null</code>. If the
	 * parameter is not a <code>Boolean</code> it is converted by calling
	 * <code>Boolean.valueOf</code> on the string value of the object.
	 *
	 * @param propValue
	 *            the property value or <code>null</code>
	 * @param defaultValue
	 *            the default boolean value
	 */
	public static boolean toBoolean(Object propValue, final boolean defaultValue) {
		propValue = toObject(propValue);
		if (propValue instanceof Boolean) {
			return (Boolean) propValue;
		} else if (propValue != null) {
			return Boolean.parseBoolean(String.valueOf(propValue));
		}

		return defaultValue;
	}

	/**
	 * Returns the parameter as a double or the <code>defaultValue</code> if the
	 * parameter is <code>null</code> or if the parameter is not a
	 * <code>Double</code> and cannot be converted to a <code>Double</code> from
	 * the parameter's string value.
	 *
	 * @param propValue
	 *            the property value or <code>null</code>
	 * @param defaultValue
	 *            the default double value
	 */
	public static double toDouble(Object propValue, final double defaultValue) {
		propValue = toObject(propValue);
		if (propValue instanceof Double) {
			return (Double) propValue;
		} else if (propValue != null) {
			try {
				return Double.parseDouble(String.valueOf(propValue));
			} catch (final NumberFormatException nfe) {
				// don't care, fall through to default value
			}
		}

		return defaultValue;
	}

	/**
	 * Returns the parameter as an integer or the <code>defaultValue</code> if
	 * the parameter is <code>null</code> or if the parameter is not an
	 * <code>Integer</code> and cannot be converted to an <code>Integer</code>
	 * from the parameter's string value.
	 *
	 * @param propValue
	 *            the property value or <code>null</code>
	 * @param defaultValue
	 *            the default integer value
	 */
	public static int toInteger(Object propValue, final int defaultValue) {
		propValue = toObject(propValue);
		if (propValue instanceof Integer) {
			return (Integer) propValue;
		} else if (propValue != null) {
			try {
				return Integer.parseInt(String.valueOf(propValue));
			} catch (final NumberFormatException nfe) {
				// don't care, fall through to default value
			}
		}

		return defaultValue;
	}

	/**
	 * Returns the parameter as a long or the <code>defaultValue</code> if the
	 * parameter is <code>null</code> or if the parameter is not a
	 * <code>Long</code> and cannot be converted to a <code>Long</code> from the
	 * parameter's string value.
	 *
	 * @param propValue
	 *            the property value or <code>null</code>
	 * @param defaultValue
	 *            the default long value
	 */
	public static long toLong(Object propValue, final long defaultValue) {
		propValue = toObject(propValue);
		if (propValue instanceof Long) {
			return (Long) propValue;
		} else if (propValue != null) {
			try {
				return Long.parseLong(String.valueOf(propValue));
			} catch (final NumberFormatException nfe) {
				// don't care, fall through to default value
			}
		}

		return defaultValue;
	}

	/**
	 * Returns the parameter as a map with string keys and string values.
	 *
	 * The parameter is considered as a collection whose entries are of the form
	 * key=value. The conversion has following rules
	 * <ul>
	 * <li>Entries are of the form key=value</li>
	 * <li>key is trimmed</li>
	 * <li>value is trimmed. If a trimmed value results in an empty string it is
	 * treated as null</li>
	 * <li>Malformed entries like 'foo','foo=' are ignored</li>
	 * <li>Map entries maintain the input order</li>
	 * </ul>
	 *
	 * Otherwise (if the property is <code>null</code>) a provided default value
	 * is returned.
	 *
	 * @param propValue
	 *            The object to convert.
	 * @param defaultArray
	 *            The default array converted to map.
	 */
	public static Map<String, String> toMap(final Object propValue, final String[] defaultArray) {
		final String[] arrayValue = toStringArray(propValue, defaultArray);

		if (arrayValue == null) {
			return null;
		}

		// in property values
		final Map<String, String> result = new LinkedHashMap<String, String>();
		for (final String kv : arrayValue) {
			final int indexOfEqual = kv.indexOf('=');
			if (indexOfEqual > 0) {
				final String key = trimToNull(kv.substring(0, indexOfEqual));
				final String value = trimToNull(kv.substring(indexOfEqual + 1));
				if (key != null) {
					result.put(key, value);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the parameter as a single value. If the parameter is neither an
	 * array nor a <code>java.util.Collection</code> the parameter is returned
	 * unmodified. If the parameter is a non-empty array, the first array
	 * element is returned. If the property is a non-empty
	 * <code>java.util.Collection</code>, the first collection element is
	 * returned. Otherwise <code>null</code> is returned.
	 *
	 * @param propValue
	 *            the parameter to convert.
	 */
	public static Object toObject(final Object propValue) {
		if (propValue == null) {
			return null;
		} else if (propValue.getClass().isArray()) {
			final Object[] prop = (Object[]) propValue;
			return prop.length > 0 ? prop[0] : null;
		} else if (propValue instanceof Collection<?>) {
			final Collection<?> prop = (Collection<?>) propValue;
			return prop.isEmpty() ? null : prop.iterator().next();
		} else {
			return propValue;
		}
	}

	/**
	 * Returns the parameter as a string or the <code>defaultValue</code> if the
	 * parameter is <code>null</code>.
	 *
	 * @param propValue
	 *            the property value or <code>null</code>
	 * @param defaultValue
	 *            the default string value
	 */
	public static String toString(Object propValue, final String defaultValue) {
		propValue = toObject(propValue);
		return (propValue != null) ? propValue.toString() : defaultValue;
	}

	/**
	 * Returns the parameter as an array of Strings. If the parameter is a
	 * scalar value its string value is returned as a single element array. If
	 * the parameter is an array, the elements are converted to String objects
	 * and returned as an array. If the parameter is a collection, the
	 * collection elements are converted to String objects and returned as an
	 * array. Otherwise (if the parameter is <code>null</code>)
	 * <code>null</code> is returned.
	 *
	 * @param propValue
	 *            The object to convert.
	 */
	public static String[] toStringArray(final Object propValue) {
		return toStringArray(propValue, null);
	}

	/**
	 * Returns the parameter as an array of Strings. If the parameter is a
	 * scalar value its string value is returned as a single element array. If
	 * the parameter is an array, the elements are converted to String objects
	 * and returned as an array. If the parameter is a collection, the
	 * collection elements are converted to String objects and returned as an
	 * array. Otherwise (if the property is <code>null</code>) a provided
	 * default value is returned.
	 *
	 * @param propValue
	 *            The object to convert.
	 * @param defaultArray
	 *            The default array to return.
	 */
	public static String[] toStringArray(final Object propValue, final String[] defaultArray) {
		if (propValue == null) {
			// no value at all
			return defaultArray;

		} else if (propValue instanceof String) {
			// single string
			return new String[] { (String) propValue };

		} else if (propValue instanceof String[]) {
			// String[]
			return (String[]) propValue;

		} else if (propValue.getClass().isArray()) {
			// other array
			final Object[] valueArray = (Object[]) propValue;
			final List<String> values = new ArrayList<String>(valueArray.length);
			for (final Object value : valueArray) {
				if (value != null) {
					values.add(value.toString());
				}
			}
			return values.toArray(new String[values.size()]);

		} else if (propValue instanceof Collection<?>) {
			// collection
			final Collection<?> valueCollection = (Collection<?>) propValue;
			final List<String> valueList = new ArrayList<String>(valueCollection.size());
			for (final Object value : valueCollection) {
				if (value != null) {
					valueList.add(value.toString());
				}
			}
			return valueList.toArray(new String[valueList.size()]);
		}

		return defaultArray;
	}

	private static String trim(final String str) {
		return str == null ? null : str.trim();
	}

	private static String trimToNull(final String str) {
		final String ts = trim(str);
		return isEmpty(ts) ? null : ts;
	}

	/** Constructor */
	private PropertiesUtil() {
	}

}