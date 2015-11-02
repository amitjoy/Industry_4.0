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

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

/**
 * OSGi Filter utilities class
 *
 * @see org.osgi.framework.Filter
 * @see org.osgi.framework.FrameworkUtil#createFilter(String)
 * @see org.osgi.framework.BundleContext#createFilter(String)
 *
 * @author AMIT KUMAR MONDAL
 */
public final class FilterUtils {
	/**
	 * AND filter template
	 */
	private static final String AND_TEMPLATE = "(&%s%s)";
	/**
	 * Approximate filter template
	 */
	private static final String APPROX_FILTER_TEMPLATE = "(%s~=%s)";
	/**
	 * Equals filter template
	 */
	private static final String EQUALS_FILTER_TEMPLATE = "(%s=%s)";
	/**
	 * Greater equals filter template
	 */
	private static final String GE_FILTER_TEMPLATE = "(%s>=%s)";
	/**
	 * Less equals filter template
	 */
	private static final String LE_FILTER_TEMPLATE = "(%s<=%s)";

	/**
	 * NOT filter template
	 */
	private static final String NOT_TEMPLATE = "(!%s)";
	/**
	 * OR filter template
	 */
	private static final String OR_TEMPLATE = "(|%s%s)";
	/**
	 * Present filter template
	 */
	private static final String PRESENT_FILTER_TEMPLATE = "(%s=*)";

	/**
	 * Create AND filter for two filters
	 *
	 * @param filter1
	 *            filter 1
	 * @param filter2
	 *            filter 2
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter1</code> or <code>filter2</code> are
	 *             <code>null</code>
	 */
	public static Filter and(final Filter filter1, final Filter filter2) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(AND_TEMPLATE, filter1, filter2));
	}

	/**
	 * Create AND filter for two filters
	 *
	 * @param filter1
	 *            filter 1
	 * @param filter2
	 *            filter 2
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter1</code> or <code>filter2</code> are
	 *             <code>null</code>
	 */
	public static Filter and(final Filter filter1, final String filter2) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(AND_TEMPLATE, filter1, filter2));
	}

	/**
	 * Create AND filter for two filters
	 *
	 * @param filter1
	 *            filter 1
	 * @param filter2
	 *            filter 2
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter1</code> or <code>filter2</code> are
	 *             <code>null</code>
	 */
	public static Filter and(final String filter1, final String filter2) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(AND_TEMPLATE, filter1, filter2));
	}

	/**
	 * Create APPROX filter
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value. To create filter value.toString() is used.
	 * @return new APPROX filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>key</code> or <code>value</code> are
	 *             <code>null</code>
	 */
	public static Filter approx(final String key, final Object value) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(APPROX_FILTER_TEMPLATE, key, value));
	}

	/**
	 * Create filter for service class
	 *
	 * @param clazz
	 *            service class
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>clazz</code> is <code>null</code>
	 * @see Constants#OBJECTCLASS
	 */
	public static Filter create(final Class<?> clazz) throws InvalidSyntaxException {
		return eq(Constants.OBJECTCLASS, clazz.getName());
	}

	/**
	 * Create AND filter for service class and custom filter
	 *
	 * @param clazz
	 *            service class
	 * @param filter
	 *            custom filter
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>clazz</code> or <code>filter</code> are
	 *             <code>null</code>
	 * @see Constants#OBJECTCLASS
	 */
	public static Filter create(final Class<?> clazz, final Filter filter) throws InvalidSyntaxException {
		return and(create(clazz), filter);
	}

	/**
	 * Create AND filter for service class and custom filter
	 *
	 * @param clazz
	 *            service class
	 * @param filter
	 *            custom filter
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>clazz</code> or <code>filter</code> are
	 *             <code>null</code>
	 * @see Constants#OBJECTCLASS
	 */
	public static Filter create(final Class<?> clazz, final String filter) throws InvalidSyntaxException {
		return and(create(clazz), filter);
	}

	/**
	 * Create filter for service class name
	 *
	 * @param className
	 *            service class name
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>className</code> is <code>null</code>
	 * @see Constants#OBJECTCLASS
	 */
	public static Filter create(final String className) throws InvalidSyntaxException {
		return eq(Constants.OBJECTCLASS, className);
	}

	/**
	 * Create AND filter for service class name and custom filter
	 *
	 * @param className
	 *            service class name
	 * @param filter
	 *            custom filter
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>className</code> or <code>filter</code> are
	 *             <code>null</code>
	 * @see Constants#OBJECTCLASS
	 */
	public static Filter create(final String className, final Filter filter) throws InvalidSyntaxException {
		return and(create(className), filter);
	}

	/**
	 * Create AND filter for service class name and custom filter
	 *
	 * @param className
	 *            service class name
	 * @param filter
	 *            custom filter
	 * @return new AND filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>className</code> or <code>filter</code> are
	 *             <code>null</code>
	 * @see Constants#OBJECTCLASS
	 */
	public static Filter create(final String className, final String filter) throws InvalidSyntaxException {
		return and(create(className), filter);
	}

	/**
	 * Create EQUALS filter
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value. To create filter value.toString() is used.
	 * @return new EQUALS filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>key</code> or <code>value</code> are
	 *             <code>null</code>
	 */
	public static Filter eq(final String key, final Object value) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(EQUALS_FILTER_TEMPLATE, key, value));
	}

	/**
	 * Create GREATER-EQUALS filter
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value. To create filter value.toString() is used.
	 * @return new GREATER-EQUALS filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>key</code> or <code>value</code> are
	 *             <code>null</code>
	 */
	public static Filter ge(final String key, final Object value) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(GE_FILTER_TEMPLATE, key, value));
	}

	/**
	 * Create LESS-EQUALS filter
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value. To create filter value.toString() is used.
	 * @return new LESS-EQUALS filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>key</code> or <code>value</code> are
	 *             <code>null</code>
	 */
	public static Filter le(final String key, final Object value) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(LE_FILTER_TEMPLATE, key, value));
	}

	/**
	 * Create NOT filter
	 *
	 * @param filter
	 *            filter
	 * @return new NOT filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter</code> is <code>null</code>
	 */
	public static Filter not(final Filter filter) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(NOT_TEMPLATE, filter));
	}

	/**
	 * Create NOT filter
	 *
	 * @param filter
	 *            filter
	 * @return new NOT filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter</code> is <code>null</code>
	 */
	public static Filter not(final String filter) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(NOT_TEMPLATE, filter));
	}

	/**
	 * Create OR filter for two filters
	 *
	 * @param filter1
	 *            filter 1
	 * @param filter2
	 *            filter 2
	 * @return new OR filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter1</code> or <code>filter2</code> are
	 *             <code>null</code>
	 */
	public static Filter or(final Filter filter1, final Filter filter2) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(OR_TEMPLATE, filter1, filter2));
	}

	/**
	 * Create OR filter for two filters
	 *
	 * @param filter1
	 *            filter 1
	 * @param filter2
	 *            filter 2
	 * @return new OR filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter1</code> or <code>filter2</code> are
	 *             <code>null</code>
	 */
	public static Filter or(final Filter filter1, final String filter2) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(OR_TEMPLATE, filter1, filter2));
	}

	/**
	 * Create OR filter for two filters
	 *
	 * @param filter1
	 *            filter 1
	 * @param filter2
	 *            filter 2
	 * @return new OR filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>filter1</code> or <code>filter2</code> are
	 *             <code>null</code>
	 */
	public static Filter or(final String filter1, final String filter2) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(OR_TEMPLATE, filter1, filter2));
	}

	/**
	 * Create PRESENT filter
	 *
	 * @param key
	 *            key
	 * @return new PRESENT filter
	 *
	 * @throws InvalidSyntaxException
	 *             If it is unable to create filter
	 * @throws NullPointerException
	 *             If <code>key</code> is <code>null</code>
	 */
	public static Filter present(final String key) throws InvalidSyntaxException {
		return FrameworkUtil.createFilter(String.format(PRESENT_FILTER_TEMPLATE, key));
	}

	/**
	 * Utility class. Only static methods are available.
	 */
	private FilterUtils() {
	}
}