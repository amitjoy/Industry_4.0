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
package de.tum.in.data.cache;

import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.tum.in.events.Events;

/**
 * OSGi Event Listener to cache the data in a Concurrent Map
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.realtime.data.cache")
@Service
public class DataCache implements EventHandler {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DataCache.class);

	/**
	 * The cache to store data
	 */
	private Cache<String, Object> m_cache;

	/**
	 * Placeholder to store the data from the event properties
	 */
	private String m_realtimeData;

	/**
	 * Placeholder to store the data timestamp name from the event properties
	 */
	private String m_timestamp;

	/**
	 * The callback while the component gets registered in the service registry
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext) {
		LOGGER.info("Activating Caching Component...");

		this.m_cache = CacheBuilder.newBuilder().concurrencyLevel(5).weakValues().maximumSize(50000)
				.expireAfterWrite(3, TimeUnit.HOURS).removalListener(new RemoveRealtimeDataListener()).build();

		LOGGER.info("Activating Caching Component...Done");
	}

	/**
	 * The callback while the component gets deregistered in the service
	 * registry
	 */
	@Deactivate
	protected synchronized void deactivate(final ComponentContext componentContext) {
		LOGGER.info("Deactivating Caching Component...");
		this.m_cache.cleanUp();
		this.m_cache = null;
		LOGGER.info("Deactivating Caching Component...Done");
	}

	/** {@inheritDoc} */
	@Override
	public void handleEvent(final Event event) {
		LOGGER.debug("Cache Event Handler starting....");

		Preconditions.checkNotNull(event);
		if (Events.DATA_CACHE.equals(event.getTopic())) {
			LOGGER.debug("Cache Event Handler caching....");

			// Extract all the event properties
			this.m_timestamp = (String) event.getProperty("timestamp");
			this.m_realtimeData = (String) event.getProperty("data");

			// Now put the data in the cache
			this.m_cache.put(this.m_timestamp, this.m_realtimeData);

			LOGGER.debug("Cache Event Handler Caching...done");
		}
	}
}
