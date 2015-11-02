/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
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
package de.tum.in.bluetooth.discovery;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * This static class manages the Bluetooth action. It ensures that only one
 * bluetooth action is executed at a time. Bluetooth operation are submitted to
 * this class which executed them when a free slot if available.
 *
 * @author AMIT KUMAR MONDAL
 */
public final class BluetoothThreadManager {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothThreadManager.class);

	/**
	 * Customization of the thread factory to avoid letting a uncaught exception
	 * blowing up. The exception is just logged.
	 */
	private static final ThreadFactory S_THREAD_FACTORY = target -> {
		final Thread thread = new Thread(target);
		LOGGER.debug("Creating new worker thread");
		thread.setUncaughtExceptionHandler(
				(t, e) -> LOGGER.error("Uncaught Exception thrown by " + target, Throwables.getStackTraceAsString(e)));
		return thread;
	};

	/**
	 * The thread pool executing the action. the thread pool size is limited to
	 * 1.
	 */
	private static ScheduledThreadPoolExecutor s_thread_pool = new ScheduledThreadPoolExecutor(1, S_THREAD_FACTORY);

	/**
	 * Schedules a periodic job such as the Device Inquiry
	 *
	 * @param runnable
	 *            the job
	 * @param period
	 *            the period
	 */
	public static void scheduleJob(final Runnable runnable, final int period) {
		try {
			LOGGER.info("Submitting periodic task " + runnable);
			s_thread_pool.scheduleWithFixedDelay(runnable, 0, period, TimeUnit.SECONDS);
			LOGGER.info(runnable + " submitted - waiting queue " + s_thread_pool.getQueue().size());
		} catch (final RejectedExecutionException e) {
			LOGGER.error("Cannot submit task", Throwables.getStackTraceAsString(e));
		}
	}

	/**
	 * Shutdowns the pool. No task can be submitted once this method is called.
	 */
	public static void stopScheduler() {
		LOGGER.info("Shutting down scheduler...");
		try {
			s_thread_pool.shutdownNow();
		} catch (final Throwable e) {
			LOGGER.warn("Exception during shutdown : ", Throwables.getStackTraceAsString(e));
		}
		LOGGER.info("Shutting down scheduler...Done");
	}

	/**
	 * Submits a one-shot job returning a result. A Future object is returned to
	 * get the result. It is strongly recommended to <strong>NOT</strong>
	 * interrupt the computation.
	 *
	 * @param task
	 *            the task
	 * @param <V>
	 *            the return type
	 * @return a Future object to retrieve the result
	 */
	public static <V> Future<V> submit(final Callable<V> task) {
		try {
			LOGGER.info("Submitting one-shot task " + task);
			final Future<V> future = s_thread_pool.submit(task);
			LOGGER.info(task + " submitted - waiting queue " + s_thread_pool.getQueue().size());
			return future;
		} catch (final RejectedExecutionException e) {
			LOGGER.error("Cannot submit task", Throwables.getStackTraceAsString(e));
			return null;
		}
	}

	/**
	 * Submits a one-shot job that does not return a result such as a Service
	 * Inquiry. the job will be executed when possible.
	 *
	 * @param runnable
	 *            the job
	 */
	public static void submit(final Runnable runnable) {
		try {
			LOGGER.info("Submitting one-shot task " + runnable);
			s_thread_pool.submit(runnable);
			LOGGER.info(runnable + " submitted - waiting queue " + s_thread_pool.getQueue().size());
		} catch (final RejectedExecutionException e) {
			LOGGER.error("Cannot submit task", Throwables.getStackTraceAsString(e));
		}
	}

}
