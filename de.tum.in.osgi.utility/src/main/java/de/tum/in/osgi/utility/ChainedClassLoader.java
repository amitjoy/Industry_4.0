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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;

/**
 * <p>
 * Title: ChainedClassLoader
 * </p>
 * <p>
 * Description: Chaining classloader implementation that delegates the resource
 * and class loading to a number of class loaders passed in.
 * </p>
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class ChainedClassLoader extends ClassLoader {
	/**
	 * The actual class loaders
	 */
	private final ClassLoader[] loaders;

	/**
	 * Constructs a new chained class loader that uses the given class loaders
	 * 
	 * @param loaders
	 *            an array of class loaders to use
	 */
	public ChainedClassLoader(final ClassLoader... loaders) {
		assert loaders != null;

		final LinkedHashSet<ClassLoader> clset = new LinkedHashSet<ClassLoader>();
		for (final ClassLoader cl : loaders) {
			if ((cl != null) && !clset.contains(cl)) {
				clset.add(cl);
			}
		}

		this.loaders = clset.toArray(new ClassLoader[clset.size()]);
	}

	/**
	 * @see ClassLoader#findClass(String)
	 */
	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		for (final ClassLoader loader : this.loaders) {
			try {
				return loader.loadClass(name);
			} catch (final ClassNotFoundException e) {
				// ignore
			}
		}

		throw new ClassNotFoundException(name);
	}

	/**
	 * @see ClassLoader#findResource(String)
	 */
	@Override
	protected URL findResource(final String name) {
		URL url = null;
		for (final ClassLoader loader : this.loaders) {
			url = loader.getResource(name);
			if (url != null) {
				return url;
			}
		}
		return url;
	}

	/**
	 * @see ClassLoader#findResources(String)
	 */
	@Override
	protected Enumeration<URL> findResources(final String name) throws IOException {
		Enumeration<URL> urls = null;
		for (final ClassLoader loader : this.loaders) {
			urls = loader.getResources(name);
			if (urls != null) {
				return urls;
			}
		}
		return urls;
	}
}
