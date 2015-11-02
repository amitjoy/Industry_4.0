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
package de.tum.in.activity.log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

/**
 * Component publishing all the activity logs stored in the Gateway to Splunk
 * (every 5 Hours)
 *
 * @author AMIT KUMAR MONDAL
 */
@Component(name = "de.tum.in.activity.log.publisher")
public class ActivityLogPublisher {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "ACTIVITY-LOG-PUBLISHER";

	/**
	 * Configurable Property to set Activity Events Topic Namespace
	 */
	private static final String EVENT_LOG_TOPIC = "event.log.topic";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogPublisher.class);

	/**
	 * Kura Cloud Service Injection
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Future Event Handle for Executor
	 */
	private ScheduledFuture<?> m_handle;

	/**
	 * System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/**
	 * Scheduled Thread Pool Executor Reference
	 */
	private final ScheduledExecutorService m_worker;

	/**
	 * Default Constructor Required for DS.
	 */
	public ActivityLogPublisher() {
		this.m_worker = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Callback during registration of this DS Service Component
	 *
	 * @param context
	 *            The injected reference for this DS Service Component
	 */
	@Activate
	protected synchronized void activate(final ComponentContext context) {
		LOGGER.info("Activating Bluetooth Service Discovery....");
		// cancel a current worker handle if one if active
		if (this.m_handle != null) {
			this.m_handle.cancel(true);
		}
		// Scheduling task for every 5 hours
		this.m_handle = this.m_worker.scheduleAtFixedRate(() -> this.doPublish(), 0, 5, TimeUnit.HOURS);
		LOGGER.info("Activating Bluetooth Service Discovery....Done");
	}

	/**
	 * Kura Cloud Service Binding Callback
	 */
	public synchronized void bindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == null) {
			this.m_cloudService = cloudService;
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is registering
	 */
	public synchronized void bindSystemService(final SystemService systemService) {
		if (this.m_systemService == null) {
			this.m_systemService = systemService;
		}
	}

	/**
	 * Callback while this component is getting deregistered
	 *
	 * @param properties
	 *            the service configuration properties
	 */
	@Deactivate
	protected synchronized void deactivate(final ComponentContext context) {
		LOGGER.info("Deactivating Activity Log Publisher....");
		// shutting down the worker and cleaning up the properties
		this.m_worker.shutdown();
		LOGGER.info("Deactivating Activity Log Publisher... Done.");
	}

	/**
	 * Publishes the logs to Splunk
	 */
	private void doPublish() {
		LOGGER.debug("Publishing Activity Logs Started.....");
		try {
			final String activityLogTopic = this.m_systemService.getProperties().getProperty(EVENT_LOG_TOPIC);
			final File tumLogFile = new File(IActivityLogService.LOCATION_TUM_LOG);
			Files.newReader(tumLogFile, Charsets.UTF_8).lines().forEach(line -> {
				try {
					this.m_cloudService.newCloudClient(APP_ID).controlPublish("splunk", activityLogTopic,
							line.getBytes(), 0, false, 5);
				} catch (final Exception e) {
					LOGGER.error(Throwables.getStackTraceAsString(e));
				}
			});
			// After publishing the data, clear the log file
			Files.write("", tumLogFile, Charsets.UTF_8);
		} catch (final IOException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
	}

	/**
	 * Cloud Service Callback while deregistering
	 */
	public synchronized void unbindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == cloudService) {
			this.m_cloudService = null;
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is deregistering
	 */
	public synchronized void unbindSystemService(final SystemService systemService) {
		if (this.m_systemService == systemService) {
			this.m_systemService = null;
		}
	}

}
