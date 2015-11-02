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

import java.lang.reflect.Method;

/**
 * <p>
 * Title: ItemConfigurationService
 * </p>
 * <p>
 * Description: Implements {@link #set(ConfigurationItem)} and
 * {@link #get(Class)}.
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class AbstractItemConfigurationService implements IConfigurationService {

	/**
	 * Finds the a method in the given class with the given name and one
	 * parameter assignable from the given parameter type
	 * 
	 * @param cls
	 *            the class to search
	 * @param name
	 *            the method name
	 * @param parameterType
	 *            the type of the method's parameter
	 * @return the method or null if no method was found
	 */
	private Method findMethod(final Class<?> cls, final String name, final Class<?> parameterType) {
		if ((cls == Object.class) || (cls == null)) {
			return null;
		}

		Method result = null;
		// search visible and hidden methods in this class
		final Method[] methods = cls.getDeclaredMethods();
		for (final Method m : methods) {
			if (m.getName().equals(name) && (m.getParameterTypes().length == 1)
					&& m.getParameterTypes()[0].isAssignableFrom(parameterType)) {
				result = m;
				break;
			}
		}

		// search superclass and interfaces
		if (result == null) {
			result = this.findMethod(cls.getSuperclass(), name, parameterType);
			if (result == null) {
				final Class<?>[] interfaces = cls.getInterfaces();
				for (final Class<?> inter : interfaces) {
					result = this.findMethod(inter, name, parameterType);
					if (result != null) {
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * @see IConfigurationService#get(Class)
	 */
	@Override
	public <T extends ConfigurationItem> T get(final Class<T> cls) {
		if (cls == null) {
			return null;
		}

		T result;
		try {
			result = cls.newInstance();
		} catch (final Exception e) {
			throw new RuntimeException("Could not create configuration item", e);
		}

		// find load method
		final Method lm = this.findMethod(cls, "load", IConfigurationService.class);
		if (lm == null) {
			throw new RuntimeException("Could not find load method for " + "configuration item class " + cls);
		}

		// make hidden method accessible
		final boolean accessible = lm.isAccessible();
		lm.setAccessible(true);

		try {
			lm.invoke(result, this);
		} catch (final Exception e) {
			throw new RuntimeException("Could not load configuration item", e);
		} finally {
			// restore accessible flag
			lm.setAccessible(accessible);
		}

		return result;
	}

	/**
	 * @see IConfigurationService#set(ConfigurationItem)
	 */
	@Override
	public <T extends ConfigurationItem> void set(final T item) {
		if (item == null) {
			return;
		}

		// find save method
		final Method sm = this.findMethod(item.getClass(), "save", IConfigurationService.class);
		if (sm == null) {
			throw new RuntimeException(
					"Could not find save method for " + "configuration item class " + item.getClass());
		}

		// make hidden method accessible
		final boolean accessible = sm.isAccessible();
		sm.setAccessible(true);

		try {
			sm.invoke(item, this);
		} catch (final Exception e) {
			throw new RuntimeException("Could not save configuration item", e);
		} finally {
			// restore accessible flag
			sm.setAccessible(accessible);
		}
	}

}
