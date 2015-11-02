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

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The <code>ServiceUtil</code> is a utility class providing some useful
 * utility methods for service handling.
 *
 * @author AMIT KUMAR MONDAL
 *
 */
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * OSGi Services utilities class
 *
 * @see org.osgi.framework.BundleContext
 * @see org.osgi.util.tracker.ServiceTracker
 * @see org.osgi.util.tracker.ServiceTrackerCustomizer
 * @see org.osgi.framework.Filter
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ServiceUtils {

	public static final class ComparableImplementation implements Comparable<Object> {

		private final Map<String, Object> props;

		private ComparableImplementation(final Map<String, Object> props) {
			this.props = props;
		}

		@Override
		public int compareTo(final Object reference) {
			final Long otherId;
			Object otherRankObj;
			if (reference instanceof ServiceReference) {
				final ServiceReference<?> other = (ServiceReference<?>) reference;
				otherId = (Long) other.getProperty(Constants.SERVICE_ID);
				otherRankObj = other.getProperty(Constants.SERVICE_RANKING);
			} else if (reference instanceof Map) {
				final Map<String, Object> otherProps = (Map<String, Object>) reference;
				otherId = (Long) otherProps.get(Constants.SERVICE_ID);
				otherRankObj = otherProps.get(Constants.SERVICE_RANKING);
			} else {
				final ComparableImplementation other = (ComparableImplementation) reference;
				otherId = (Long) other.props.get(Constants.SERVICE_ID);
				otherRankObj = other.props.get(Constants.SERVICE_RANKING);
			}
			final Long id = (Long) this.props.get(Constants.SERVICE_ID);
			if (id.equals(otherId)) {
				return 0; // same service
			}

			Object rankObj = this.props.get(Constants.SERVICE_RANKING);

			// If no rank, then spec says it defaults to zero.
			rankObj = (rankObj == null) ? new Integer(0) : rankObj;
			otherRankObj = (otherRankObj == null) ? new Integer(0) : otherRankObj;

			// If rank is not Integer, then spec says it defaults to zero.
			final Integer rank = (rankObj instanceof Integer) ? (Integer) rankObj : new Integer(0);
			final Integer otherRank = (otherRankObj instanceof Integer) ? (Integer) otherRankObj : new Integer(0);

			// Sort by rank in ascending order.
			if (rank.compareTo(otherRank) < 0) {
				return -1; // lower rank
			} else if (rank.compareTo(otherRank) > 0) {
				return 1; // higher rank
			}

			// If ranks are equal, then sort by service id in descending order.
			return (id.compareTo(otherId) < 0) ? 1 : -1;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof ComparableImplementation) {
				return this.props.equals(((ComparableImplementation) obj).props);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return this.props.hashCode();
		}
	}

	/**
	 * ServiceTrackerCustomizer with lock support.
	 *
	 * @see java.util.concurrent.locks.ReentrantLock
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer
	 */
	private static class ServiceTrackerCustomizerWithLock implements ServiceTrackerCustomizer {
		private final BundleContext bc;
		private final ReentrantLock lock;

		public ServiceTrackerCustomizerWithLock(final BundleContext bc, final ReentrantLock lock) {
			this.bc = bc;
			this.lock = lock;
		}

		@Override
		public Object addingService(final ServiceReference serviceReference) {
			try {
				return this.bc.getService(serviceReference);
			} finally {
				// uplock the lock
				this.lock.unlock();
			}
		}

		@Override
		public void modifiedService(final ServiceReference serviceReference, final Object o) {
		}

		@Override
		public void removedService(final ServiceReference serviceReference, final Object o) {
		}
	}

	/**
	 * Create a comparable object out of the service properties. With the result
	 * it is possible to compare service properties based on the service ranking
	 * of a service. Therefore this object acts like
	 * {@link ServiceReference#compareTo(Object)}.
	 *
	 * @param props
	 *            The service properties.
	 * @return A comparable for the ranking of the service
	 */
	public static Comparable<Object> getComparableForServiceRanking(final Map<String, Object> props) {
		return new ComparableImplementation(props);
	}

	/**
	 * Get service instance by class
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @return service instance instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>clazz</code> are
	 *             <code>null</code>
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz) {
		final ServiceTracker tracker = new ServiceTracker(bc, clazz.getName(), null);
		tracker.open();
		try {
			// noinspection unchecked
			return (T) tracker.getService();
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get service instance by class
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param filter
	 *            filter
	 * @return service instance instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>clazz</code> are
	 *             <code>null</code>
	 * @throws InvalidSyntaxException
	 *             If <code>filter</code> contains an invalid filter string that
	 *             cannot be parsed
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final Filter filter)
			throws InvalidSyntaxException {
		final ServiceTracker tracker = new ServiceTracker(bc, FilterUtils.create(clazz, filter), null);
		tracker.open();
		try {
			// noinspection unchecked
			return (T) tracker.getService();
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get service instance by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>clazz</code> are
	 *             <code>null</code>
	 * @throws InvalidSyntaxException
	 *             If <code>filter</code> contains an invalid filter string that
	 *             cannot be parsed
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final Filter filter,
			final long timeout) throws InvalidSyntaxException {
		return getService(bc, clazz, filter, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get service instance by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            indefinately.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>clazz</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 * @throws InvalidSyntaxException
	 *             If <code>filter</code> contains an invalid filter string that
	 *             cannot be parsed
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final Filter filter,
			final long timeout, final TimeUnit timeUnit) throws InvalidSyntaxException {
		final ServiceTracker tracker = new ServiceTracker(bc, FilterUtils.create(clazz, filter), null);
		tracker.open();
		try {
			// noinspection unchecked
			return (T) tracker.waitForService(timeUnit.toMillis(timeout));
		} catch (final InterruptedException e) {
			return null;
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get service instance by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>clazz</code> are
	 *             <code>null</code>
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final long timeout) {
		return getService(bc, clazz, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get service instance by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            indefinately.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>clazz</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final long timeout,
			final TimeUnit timeUnit) {
		final ServiceTracker tracker = new ServiceTracker(bc, clazz.getName(), null);
		tracker.open();
		try {
			// noinspection unchecked
			return (T) tracker.waitForService(timeUnit.toMillis(timeout));
		} catch (final InterruptedException e) {
			return null;
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get service instance by class
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param filter
	 *            filter
	 * @return service instance instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>clazz</code> are
	 *             <code>null</code>
	 * @throws InvalidSyntaxException
	 *             If <code>filter</code> contains an invalid filter string that
	 *             cannot be parsed
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final String filter)
			throws InvalidSyntaxException {
		return getService(bc, clazz, FrameworkUtil.createFilter(filter));
	}

	/**
	 * Get service instance by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>clazz</code> are
	 *             <code>null</code>
	 * @throws InvalidSyntaxException
	 *             If <code>filter</code> contains an invalid filter string that
	 *             cannot be parsed
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final String filter,
			final long timeout) throws InvalidSyntaxException {
		return getService(bc, clazz, filter, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get service instance by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            indefinately.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>clazz</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 * @throws InvalidSyntaxException
	 *             If <code>filter</code> contains an invalid filter string that
	 *             cannot be parsed
	 */
	public static <T> T getService(final BundleContext bc, final Class<T> clazz, final String filter,
			final long timeout, final TimeUnit timeUnit) throws InvalidSyntaxException {
		return getService(bc, clazz, FrameworkUtil.createFilter(filter), timeout, timeUnit);
	}

	/**
	 * Get service instance by filter
	 *
	 * @param bc
	 *            BundleContext
	 * @param filter
	 *            filter
	 * @return service instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>filter</code> are
	 *             <code>null</code>
	 */
	public static Object getService(final BundleContext bc, final Filter filter) {
		final ServiceTracker tracker = new ServiceTracker(bc, filter, null);
		tracker.open();
		try {
			return tracker.getService();
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get service instance by filter with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>filter</code> are
	 *             <code>null</code>
	 */
	public static Object getService(final BundleContext bc, final Filter filter, final long timeout) {
		return getService(bc, filter, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get service instance by filter with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            indefinately.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>filter</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 */
	public static Object getService(final BundleContext bc, final Filter filter, final long timeout,
			final TimeUnit timeUnit) {
		final ServiceTracker tracker = new ServiceTracker(bc, filter, null);
		tracker.open();
		try {
			return tracker.waitForService(timeUnit.toMillis(timeout));
		} catch (final InterruptedException e) {
			return null;
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get service instance by class name
	 *
	 * @param bc
	 *            BundleContext
	 * @param className
	 *            className
	 * @return service instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>className</code> are
	 *             <code>null</code>
	 */
	public static Object getService(final BundleContext bc, final String className) {
		final ServiceTracker tracker = new ServiceTracker(bc, className, null);
		tracker.open();
		try {
			return tracker.getService();
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get service instance by class name with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param className
	 *            className
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>className</code> are
	 *             <code>null</code>
	 */
	public static Object getService(final BundleContext bc, final String className, final long timeout) {
		return getService(bc, className, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get service instance by className with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param className
	 *            className
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            indefinately.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return service instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>className</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 */
	public static Object getService(final BundleContext bc, final String className, final long timeout,
			final TimeUnit timeUnit) {
		final ServiceTracker tracker = new ServiceTracker(bc, className, null);
		tracker.open();
		try {
			return tracker.waitForService(timeUnit.toMillis(timeout));
		} catch (final InterruptedException e) {
			return null;
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get ServiceReference by class
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>clazz</code> are
	 *             <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final Class clazz) {
		final ServiceTracker tracker = new ServiceTracker(bc, clazz.getName(), null);
		tracker.open();
		try {
			return tracker.getServiceReference();
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get ServiceReference by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>filter</code> are
	 *             <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final Class clazz, final long timeout) {
		return getServiceReference(bc, clazz, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get ServiceReference by class with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param clazz
	 *            Class
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            indefinately.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>filter</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final Class clazz, final long timeout,
			final TimeUnit timeUnit) {
		final ReentrantLock lock = new ReentrantLock();
		final long timeoutInMillis = timeUnit.toMillis(timeout);
		final ServiceTracker tracker = new ServiceTracker(bc, clazz.getName(),
				new ServiceTrackerCustomizerWithLock(bc, lock));
		tracker.open();
		try {
			return waitForServiceReference(tracker, timeoutInMillis, lock);
		} catch (final InterruptedException e) {
			return null;
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get ServiceReference by filter
	 *
	 * @param bc
	 *            BundleContext
	 * @param filter
	 *            filter
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>filter</code> are
	 *             <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final Filter filter) {
		final ServiceTracker tracker = new ServiceTracker(bc, filter, null);
		tracker.open();
		try {
			return tracker.getServiceReference();
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get ServiceReference by filter with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>filter</code> are
	 *             <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final Filter filter,
			final long timeout) {
		return getServiceReference(bc, filter, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get ServiceReference by filter with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param filter
	 *            filter
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            indefinately.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>filter</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final Filter filter, final long timeout,
			final TimeUnit timeUnit) {
		final ReentrantLock lock = new ReentrantLock();
		final long timeoutInMillis = timeUnit.toMillis(timeout);
		final ServiceTracker tracker = new ServiceTracker(bc, filter, new ServiceTrackerCustomizerWithLock(bc, lock));
		tracker.open();
		try {
			return waitForServiceReference(tracker, timeoutInMillis, lock);
		} catch (final InterruptedException e) {
			return null;
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get ServiceReference by class name
	 *
	 * @param bc
	 *            BundleContext
	 * @param className
	 *            className
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>className</code> are
	 *             <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final String className) {
		final ServiceTracker tracker = new ServiceTracker(bc, className, null);
		tracker.open();
		try {
			return tracker.getServiceReference();
		} finally {
			tracker.close();
		}
	}

	/**
	 * Get ServiceReference by class name with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param className
	 *            className
	 * @param timeout
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinately.
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code> or <code>className</code> are
	 *             <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final String className,
			final long timeout) {
		return getServiceReference(bc, className, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get ServiceReference by class name with timeout.
	 *
	 * @param bc
	 *            BundleContext
	 * @param className
	 *            className
	 * @param timeout
	 *            time interval to wait. If zero, the method will wait
	 *            Indefinitely.
	 * @param timeUnit
	 *            time unit for the time interval
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative
	 * @throws NullPointerException
	 *             If <code>bc</code>, <code>className</code> or
	 *             <code>timeUnit</code> are <code>null</code>
	 */
	public static ServiceReference getServiceReference(final BundleContext bc, final String className,
			final long timeout, final TimeUnit timeUnit) {
		final ReentrantLock lock = new ReentrantLock();
		final long timeoutInMillis = timeUnit.toMillis(timeout);
		final ServiceTracker tracker = new ServiceTracker(bc, className,
				new ServiceTrackerCustomizerWithLock(bc, lock));
		tracker.open();
		try {
			return waitForServiceReference(tracker, timeoutInMillis, lock);
		} catch (final InterruptedException e) {
			return null;
		} finally {
			tracker.close();
		}
	}

	/**
	 * Wait for at least one ServiceReference to be tracked by ServiceTracker
	 *
	 * @param tracker
	 *            ServiceTracker
	 * @param timeoutInMillis
	 *            time interval in milliseconds to wait. If zero, the method
	 *            will wait indefinitely.
	 * @param lock
	 *            external lock that is used to handle new service adding to
	 *            ServiceTracker
	 * @return ServiceReference instance or <code>null</code>
	 *
	 * @throws IllegalArgumentException
	 *             If the value of timeout is negative.
	 * @throws InterruptedException
	 *             If another thread has interrupted the current thread.
	 */
	private static ServiceReference waitForServiceReference(final ServiceTracker tracker, final long timeoutInMillis,
			final ReentrantLock lock) throws InterruptedException {
		if (timeoutInMillis < 0) {
			throw new IllegalArgumentException("timeout value is negative");
		}
		final ServiceReference reference = tracker.getServiceReference();
		if (reference == null) {
			lock.wait(timeoutInMillis);
			return tracker.getServiceReference();
		} else {
			return reference;
		}
	}

	/**
	 * Utility class. Only static methods are available.
	 */
	private ServiceUtils() {
	}
}