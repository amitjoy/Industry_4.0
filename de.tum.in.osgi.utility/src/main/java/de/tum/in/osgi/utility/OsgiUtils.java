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
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Preconditions;

/**
 * Utilities for OSGI
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class OsgiUtils {

	/**
	 * Interface for conditions
	 */
	public interface Condition {

		/**
		 * Evaluate the condition
		 *
		 * @return the condition value
		 */
		public boolean evaluate();

	}

	/**
	 * Name of the Eclipse Equinox bundle
	 */
	public static final String EQUINOX_BUNDLE = "org.eclipse.osgi";

	private static final Logger log = Logger.getLogger(OsgiUtilsActivator.class.getName());

	/**
	 * Add a service listener
	 *
	 * @param <T>
	 *            the service type
	 * @param listener
	 *            the listener
	 * @param serviceType
	 *            the service type
	 */
	public static <T> void addServiceListener(final SingleServiceListener<T> listener, final Class<T> serviceType) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance != null) {
			instance.addServiceListener(listener, serviceType);
		}
	}

	/**
	 * Asynchronously waits for the service with the given type to be available.
	 * Immediately returns a {@link Future} object that can be used to get the
	 * service later.
	 *
	 * ATTENTION: The future may run infinitely if the service is not available
	 * at all. Use {@link Future#get(long, java.util.concurrent.TimeUnit)} if
	 * you want to limit the time to wait.
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @return a future that will eventually contain the service instance
	 */
	public static <T> CompletableFuture<T> asyncWaitForService(final Class<T> serviceType) {
		final CompletableFuture<T> task = CompletableFuture.supplyAsync(() -> waitForService(serviceType));
		final ExecutorService exe = Executors.newCachedThreadPool();
		exe.submit(() -> {
			try {
				task.get();
			} catch (final CancellationException e) {
				task.cancel(true);
			} catch (final Exception e) {
				task.completeExceptionally(e);
			}
		});
		return task;
	}

	/**
	 * Check whether a bundle is started or not
	 */
	@SuppressWarnings("unused")
	private static void checkStarted(final Bundle bundle) {
		Preconditions.checkNotNull(bundle);
		final int bundleState = bundle.getState();
		if ((bundleState != Bundle.STARTING) && (bundleState != Bundle.ACTIVE)) {
			try {
				bundle.start();
			} catch (final BundleException ex) {
				log.log(Level.SEVERE, "Error starting bundle", bundle.getSymbolicName());
			}
		}
	}

	/**
	 * Returns all classes from a package
	 *
	 * @param one
	 *            a well known class from the package that should be searched
	 * @return the found classes (never returns null)
	 * @throws ClassNotFoundException
	 *             if one of the classes from the package could not be loaded
	 */
	public static Class<?>[] getClassesFromPackage(final Class<?> one) throws ClassNotFoundException {
		return getClassesFromPackage(one, null);
	}

	/**
	 * Searches a package and returns all classes that extend or implement the
	 * given class or interface.
	 *
	 * @param <T>
	 *            the type of the class or interface the returned classes should
	 *            extend or implement
	 * @param one
	 *            a well known class from the package that should be searched
	 * @param base
	 *            a class or an interface the returned classes should extend or
	 *            implement
	 * @return the found classes (never returns null)
	 * @throws ClassNotFoundException
	 *             if one of the classes from the package could not be loaded
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T>[] getClassesFromPackage(final Class<?> one, final Class<T> base)
			throws ClassNotFoundException {
		// get path to package
		final String pkgPath = one.getPackage().getName();

		if (System.getProperty("osgi.os") == null) {
			// OSGi not in use
			final String clsPath = one.getName().replace(".", "/") + ".class";
			final URL url = one.getClassLoader().getResource(clsPath);
			if (url != null) {
				try {
					final URLConnection con = url.openConnection();
					if (!(con instanceof JarURLConnection)) {
						throw new IllegalStateException(
								"Cannot resolve" + " the artifact containing package " + pkgPath);
					}
					final JarFile jar = ((JarURLConnection) con).getJarFile();
					return getClassesFromPackage(pkgPath, jar, base);
				} catch (final IOException e) {
					throw new IllegalStateException(e);
				}
			}
			return (Class<? extends T>[]) new Class<?>[0];
		}
		final Bundle bnd = FrameworkUtil.getBundle(one);
		return getClassesFromPackage(pkgPath, bnd, base);
	}

	/**
	 * Returns all classes from a package
	 *
	 * @param pkg
	 *            the package to search for classes
	 * @param bnd
	 *            the bundle which contains the package
	 * @return the found classes (never returns null)
	 * @throws ClassNotFoundException
	 *             if one of the classes from the package could not be loaded
	 */
	public static Class<?>[] getClassesFromPackage(final String pkg, final Bundle bnd) throws ClassNotFoundException {
		return getClassesFromPackage(pkg, bnd, null);
	}

	/**
	 * Searches a package and returns all classes that extend or implement the
	 * given class or interface.
	 *
	 * @param <T>
	 *            the type of the class or interface the returned classes should
	 *            extend or implement
	 * @param pkg
	 *            the package to search for classes
	 * @param bnd
	 *            the bundle which contains the package
	 * @param base
	 *            a class or an interface the returned classes should extend or
	 *            implement
	 * @return the found classes (never returns null)
	 * @throws ClassNotFoundException
	 *             if one of the classes from the package could not be loaded
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T>[] getClassesFromPackage(final String pkg, final Bundle bnd,
			final Class<T> base) throws ClassNotFoundException {
		final String pkgPath = pkg.replaceAll("\\.", "/");

		// find class files in package
		boolean prefix = false;
		Enumeration<?> es = bnd.findEntries(pkgPath, "*.class", true);
		if (es == null) {
			// try in "bin" directory
			es = bnd.findEntries("bin/" + pkgPath, "*.class", true);
			prefix = true;
		}
		if (es == null) {
			// try in "classes" directory
			es = bnd.findEntries("classes/" + pkgPath, "*.class", true);
			prefix = true;
		}
		if (es == null) {
			// we did not find anything in the default directories.
			// return an empty array
			return (Class<? extends T>[]) new Class<?>[0];
		}

		// load classes
		final List<Class<?>> result = new ArrayList<Class<?>>();
		while (es.hasMoreElements()) {
			final URL u = (URL) es.nextElement();

			// convert URL to qualified class name...
			String path = u.getPath();
			{
				// remove slash at the beginning
				if (path.charAt(0) == '/') {
					path = path.substring(1);
				}
				// remove "bin" or "classes" directory
				if (prefix) {
					path = path.substring(path.indexOf('/') + 1);
				}
				// remove file extension
				path = path.substring(0, path.lastIndexOf('.'));
			}
			path = path.replaceAll("/", ".");

			// load class
			final Class<?> cls = bnd.loadClass(path);
			if ((base == null) || base.isAssignableFrom(cls)) {
				result.add(cls);
			}
		}
		return (Class<? extends T>[]) result.toArray(new Class<?>[result.size()]);
	}

	/**
	 * Searches a package and returns all classes that extend or implement the
	 * given class or interface.
	 *
	 * @param pkg
	 *            the package to search for classes
	 * @param jar
	 *            the jar file which contains the package
	 * @param base
	 *            a class or an interface the returned classes should extend or
	 *            implement
	 * @return the found classes (never returns null)
	 * @throws ClassNotFoundException
	 *             if one of the classes from the package could not be loaded
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T>[] getClassesFromPackage(final String pkg, final JarFile jar,
			final Class<T> base) throws ClassNotFoundException {
		final Enumeration<JarEntry> es = jar.entries();
		final String pkgPath = pkg.replace(".", "/");

		final List<Class<?>> result = new ArrayList<Class<?>>();
		// iterate over all elements of jar file
		while (es.hasMoreElements()) {
			final JarEntry el = es.nextElement();

			// skip all files not in package or not a class
			if (!el.getName().startsWith(pkgPath) || !el.getName().endsWith(".class")) {
				continue;
			}

			// convert path into FQN
			String path = el.getName();
			// remove extension
			path = path.substring(0, path.lastIndexOf("."));
			// remove leading slash
			if (path.charAt(0) == '/') {
				path = path.substring(1);
			}
			path = path.replace("/", ".");

			// load class
			final Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(path);

			if ((base == null) || base.isAssignableFrom(cls)) {
				result.add(cls);
			}
		}
		return (Class<? extends T>[]) result.toArray(new Class<?>[result.size()]);
	}

	/**
	 * Get the service with the given type. Only use the returned instance while
	 * you are sure it is still valid.
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @return the available service of this type or null
	 */
	public static <T> T getService(final Class<T> serviceType) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance == null) {
			return null;
		}
		return instance.getService(serviceType);
	}

	/**
	 * Get the services with the given type. Only use the returned instance
	 * while you are sure it is still valid.
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @return the available services of this type or null
	 */
	public static <T> Collection<T> getServices(final Class<T> serviceType) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance == null) {
			return null;
		}
		return instance.getServices(serviceType);
	}

	/**
	 * Checks whether a bundle is a fragment
	 *
	 * @param bundle
	 *            the bundle
	 * @return true if bundle is a fragment
	 */
	public static boolean isFragment(final Bundle bundle) {
		final Dictionary<?, ?> d = bundle.getHeaders();
		return (d.get(Constants.FRAGMENT_HOST) != null);
	}

	/**
	 * Load a class from the given bundles
	 *
	 * @param bundles
	 *            the bundles
	 * @param preferredBundleName
	 *            the symbolic name of the preferred bundle (can be null)
	 * @param className
	 *            the class name
	 *
	 * @return the loaded class or <code>null</code>
	 */
	public static Class<?> loadClass(final Bundle[] bundles, final String preferredBundleName, final String className) {
		if (preferredBundleName != null) {
			for (final Bundle bundle : bundles) {
				if (bundle.getSymbolicName().equals(preferredBundleName)) {
					try {
						return bundle.loadClass(className);
					} catch (final ClassNotFoundException e) {
						// ignore
					}
					log.severe("The preferred bundle " + preferredBundleName + " does not contain the requested class "
							+ className);
					return null;
				}
			}
			log.severe("Could not find class " + className + " since the preferred bundle " + preferredBundleName
					+ " is not installed");
			return null;
		}

		// fall back. This will only be executed if there is no
		// preferred bundle. Attention: executing this code can
		// make lazy bundles start.
		Bundle eclipse = null;
		for (final Bundle bundle : bundles) {
			if (!bundle.getSymbolicName().equals(EQUINOX_BUNDLE)) {
				try {
					return bundle.loadClass(className);
				} catch (final ClassNotFoundException e) {
					// ignore
				}
			} else {
				eclipse = bundle;
			}
		}

		if (eclipse != null) {
			try {
				return eclipse.loadClass(className);
			} catch (final ClassNotFoundException e) {
				// ignore
			}
		}

		return null;
	}

	/**
	 * Load the class with the given name. The class will be loaded from the
	 * OSGi context if it is available
	 *
	 * @param name
	 *            the class name
	 * @param preferredBundleName
	 *            the symbolic name of the preferred bundle (can be null)
	 *
	 * @return the loaded class or <code>null</code>
	 */
	public static Class<?> loadClass(final String name, final String preferredBundleName) {
		if ((OsgiUtilsActivator.getInstance() != null) && (OsgiUtilsActivator.getInstance().getContext() != null)) {
			return loadClass(OsgiUtilsActivator.getInstance().getContext().getBundles(), preferredBundleName, name);
		} else {
			try {
				return Class.forName(name);
			} catch (final Throwable e) {
				return null;
			}
		}
	}

	/**
	 * Register a service
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @param service
	 *            the service implementation
	 */
	public static <T> void registerService(final Class<T> serviceType, final T service) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance != null) {
			instance.registerService(serviceType, service);
		}
	}

	/**
	 * Remove a service listener
	 *
	 * @param <T>
	 *            the service type
	 * @param listener
	 *            the listener
	 * @param serviceType
	 *            the service type
	 */
	public static <T> void removeServiceListener(final SingleServiceListener<T> listener, final Class<T> serviceType) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance != null) {
			instance.removeServiceListener(listener, serviceType);
		}
	}

	/**
	 * Unregister a previously registered service
	 *
	 * @param service
	 *            the service implementation
	 */
	public static void unregisterService(final Object service) {
		final OsgiUtilsActivator instance = OsgiUtilsActivator.getInstance();
		if (instance != null) {
			instance.unregisterService(service);
		}
	}

	/**
	 * Infinitely waits for the service with the given type to be available and
	 * then returns it. Only use the returned instance while you are sure it is
	 * still valid.
	 *
	 * ATTENTION: Please be absolute sure what you are doing before calling this
	 * method, since it uses an infinite loop internally.
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @return the service or null if the timeout has occurred
	 */
	public static <T> T waitForService(final Class<T> serviceType) {
		T result = getService(serviceType);
		if (result == null) {
			waitUntil(() -> getService(serviceType) != null);
			result = getService(serviceType);
		}
		return result;
	}

	/**
	 * Waits for the service with the given type to be available and then
	 * returns it. Only use the returned instance while you are sure it is still
	 * valid.
	 *
	 * @param <T>
	 *            the service type
	 * @param serviceType
	 *            the service type
	 * @param timeout
	 *            the timeout in seconds after which to return even if the
	 *            service is not available
	 * @return the service or null if the timeout has occurred
	 */
	public static <T> T waitForService(final Class<T> serviceType, final int timeout) {
		T result = getService(serviceType);
		if (result == null) {
			waitUntil(() -> getService(serviceType) != null, timeout);
			result = getService(serviceType);
		}
		return result;
	}

	/**
	 * Wait infinitely until the condition is <code>true</code>, the condition
	 * will be checked every second
	 *
	 * ATTENTION: Please be absolute sure what you are doing before calling this
	 * method, since it uses an infinite loop internally.
	 *
	 * @param condition
	 *            the condition
	 *
	 * @return the condition value
	 */
	public static boolean waitUntil(final Condition condition) {
		if (condition.evaluate()) {
			return true;
		} else {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					// ignore
				}
				if (condition.evaluate()) {
					return true;
				}
			}
		}
	}

	/**
	 * Wait until the condition is <code>true</code>, the condition will be
	 * checked every second
	 *
	 * @param condition
	 *            the condition
	 * @param timeout
	 *            the timeout in seconds after which to return even if the
	 *            condition is <code>false</code>
	 *
	 * @return the condition value
	 */
	public static boolean waitUntil(final Condition condition, final int timeout) {
		if (timeout <= 0) {
			return condition.evaluate();
		}

		if (condition.evaluate()) {
			return true;
		} else {
			for (int i = 0; i < timeout; i++) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					// ignore
				}
				if (condition.evaluate()) {
					return true;
				}
			}

			return false;
		}
	}

	/** Constructor */
	private OsgiUtils() {
	}

}
