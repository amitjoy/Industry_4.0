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
import java.time.LocalDateTime;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

/**
 * The implementation of IActivityLogService
 *
 * @see IActivityLogService
 * @author AMIT KUMAR MONDAL
 */
@Component(immediate = true, name = "de.tum.in.activity.log.service")
@Service(value = { IActivityLogService.class })
public class ActivityLogService extends Cloudlet implements IActivityLogService {

	/**
	 * Defines Application ID for Activity Logs
	 */
	private static final String APP_ID = "LOGS-V1";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogService.class);

	/**
	 * Cloud Service Injection
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Constructor
	 */
	public ActivityLogService() {
		super(APP_ID);
	}

	/**
	 * Callback while this component is getting registered
	 *
	 * @param properties
	 *            the service configuration properties
	 */
	@Override
	@Activate
	protected synchronized void activate(final ComponentContext context) {
		LOGGER.info("Activating Activity Log Service....");
		super.setCloudService(this.m_cloudService);
		super.activate(context);
		LOGGER.info("Activating Activity Log Service... Done.");
	}

	/**
	 * Kura Cloud Service Binding Callback
	 */
	public synchronized void bindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == null) {
			super.setCloudService(this.m_cloudService = cloudService);
		}
	}

	/**
	 * Callback while this component is getting deregistered
	 *
	 * @param properties
	 *            the service configuration properties
	 */
	@Override
	@Deactivate
	protected synchronized void deactivate(final ComponentContext context) {
		LOGGER.info("Deactivating Activity Log Service....");
		super.deactivate(context);

		LOGGER.info("Deactivating Activity Log Service... Done.");
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		if ("logs".equals(reqTopic.getResources()[0])) {
			respPayload.addMetric("tumlog", this.retrieveLogs(LogFileType.TUM));
			respPayload.addMetric("kuralog", this.retrieveLogs(LogFileType.KURA));
		}
		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String retrieveLogs(final LogFileType type) {
		LOGGER.debug("Retrieving logs from the Activity Logs Database...");
		try {
			if (type == LogFileType.KURA) {
				return new String(Files.asByteSource(new File(LOCATION_KURA_LOG)).read(), Charsets.UTF_8);
			}
			if (type == LogFileType.TUM) {
				return new String(Files.asByteSource(new File(LOCATION_TUM_LOG)).read(), Charsets.UTF_8);
			}
		} catch (final Exception e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		LOGGER.debug("Retrieving logs from the Activity Logs Database...Done");
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveLog(final String log) {
		LOGGER.debug("Saving log to the Activity Logs Database...");
		try {
			Files.append(LocalDateTime.now() + " " + log + System.lineSeparator(), new File(LOCATION_TUM_LOG),
					Charsets.UTF_8);
		} catch (final IOException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		LOGGER.debug("Saving log to the Activity Logs Database...Done");
	}

	/**
	 * Kura Cloud Service Callback while deregistering
	 */
	public synchronized void unbindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == cloudService) {
			super.setCloudService(this.m_cloudService = null);
		}
	}

}
