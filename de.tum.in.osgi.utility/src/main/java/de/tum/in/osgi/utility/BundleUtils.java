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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * OSGi Bundles utilities class
 *
 * @see org.osgi.framework.Bundle
 * @see org.osgi.service.packageadmin.PackageAdmin
 * @see org.osgi.framework.BundleContext
 */
@SuppressWarnings("deprecation")
public final class BundleUtils {
	/**
	 * Find bundle by ID
	 *
	 * @param bc
	 *            BundleContext
	 * @param bundleId
	 *            bundle id
	 * @return Bundle instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> is <code>null</code>
	 */
	public static Bundle findBundle(final BundleContext bc, final long bundleId) {
		return bc.getBundle(bundleId);
	}

	/**
	 * Find bundle by SymbolicName
	 *
	 * @param bc
	 *            BundleContext
	 * @param symbolicName
	 *            symbolicName
	 * @return Bundle instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>symbolicName</code> are
	 *             <code>null</code>
	 */
	public static Bundle findBundle(final BundleContext bc, final String symbolicName) {
		return findBundle(bc, symbolicName, null);
	}

	/**
	 * Find bundle by SymbolicName and Version
	 *
	 * @param bc
	 *            BundleContext
	 * @param symbolicName
	 *            symbolicName
	 * @param version
	 *            version
	 * @return Bundle instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>symbolicName</code> are
	 *             <code>null</code>
	 */
	public static Bundle findBundle(final BundleContext bc, final String symbolicName, final Version version) {
		final PackageAdmin packageAdmin = ServiceUtils.getService(bc, PackageAdmin.class);
		if (packageAdmin != null) {
			final Bundle[] bundles = packageAdmin.getBundles(symbolicName, version != null ? version.toString() : null);
			if ((bundles != null) && (bundles.length > 0)) {
				return bundles[0];
			}
		}
		return null;
	}

	/**
	 * Returns the absolute path of the resource from the bundle
	 *
	 * @param bundleClazz
	 *            the class type
	 * @param pathToFile
	 *            the path to file
	 * @return
	 * @throws URISyntaxException
	 */
	public static String getResourcePath(final Class<?> bundleClazz, final String pathToFile)
			throws URISyntaxException {
		final Bundle bundle = FrameworkUtil.getBundle(bundleClazz);
		final URL url = bundle.getEntry(pathToFile);
		return new File(url.getPath()).getAbsolutePath();
	}

	/**
	 * Loads a resource from any OSGi Bundle
	 *
	 * @param bundleClazz
	 *            the class type
	 * @param resourceTypeclazz
	 *            the resource type
	 * @param pathToFile
	 *            the path to file
	 * @return
	 * @throws IOException
	 */
	public static <T> T loadResource(final Class<?> bundleClazz, final Class<T> resourceTypeclazz,
			final String pathToFile) throws IOException {
		final Bundle bundle = FrameworkUtil.getBundle(bundleClazz);
		final URL url = bundle.getEntry(pathToFile);
		final InputStream stream = new FileInputStream(url.getFile());

		try {
			if (resourceTypeclazz.isInstance(InputStream.class)) {
				return resourceTypeclazz.cast(stream);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			stream.close();
		}

		return null;
	}

	/**
	 * Utility class. Only static methods are available.
	 */
	private BundleUtils() {
	}
}