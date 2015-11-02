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
package de.tum.in.mongodb;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * MongoDB Service Registration Component
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = false, name = "de.tum.in.mongodb")
@Service(value = { MongoDBServiceConfiguration.class })
public class MongoDBServiceConfiguration extends Cloudlet implements ConfigurableComponent {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "MONGODB-V1";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBServiceConfiguration.class);

	/**
	 * Configurable Property to set Mongo DB Database Name
	 */
	private static final String MONGO_DB_DBNAME = "mongo.db.dbname";

	/**
	 * Configurable property to set Mongo DB Server Address
	 */
	private static final String MONGO_DB_HOST = "mongo.db.host";

	/**
	 * Configurable Property to set Mongo DB Password
	 */
	private static final String MONGO_DB_PASSWORD = "mongo.db.password";

	/**
	 * Configurable Property to set Mongo DB Server Port No
	 */
	private static final String MONGO_DB_PORT = "mongo.db.port";

	/**
	 * Configurable Property for topic to publish realtime data
	 */
	private static final String MONGO_DB_USERNAME = "mongo.db.username";

	/**
	 * Eclipse Kura Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Eclipse Kura Configuration Service Dependency
	 */
	@Reference(bind = "bindConfigurationService", unbind = "unbindConfigurationService")
	private volatile ConfigurationService m_configurationService;

	/**
	 * Service Component Context
	 */
	private ComponentContext m_context;

	/**
	 * Place holder for db name
	 */
	private String m_dbname;

	/**
	 * Place holder for host
	 */
	private String m_host;

	/**
	 * Place holder for MongoDB Client
	 */
	private MongoClient m_mongoClient;

	/**
	 * Place holder for password
	 */
	private String m_password;

	/**
	 * Place holder for port
	 */
	@SuppressWarnings("unused")
	private int m_port;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;;

	/**
	 * Place holder for username
	 */
	private String m_username;

	/* Constructor */
	public MongoDBServiceConfiguration() {
		super(APP_ID);
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating MongoDB Component...");

		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);
		this.m_context = componentContext;

		this.doRegister(componentContext, properties);

		LOGGER.info("Activating MongoDB Component... Done.");

	}

	/**
	 * Callback to be used while {@link CloudService} is registering
	 */
	public synchronized void bindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == null) {
			super.setCloudService(this.m_cloudService = cloudService);
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is registering
	 */
	public synchronized void bindConfigurationService(final ConfigurationService configurationService) {
		if (this.m_configurationService == null) {
			this.m_configurationService = configurationService;
		}
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating MongoDB Component...");
		LOGGER.info("Releasing CloudApplicationClient for {}...", APP_ID);

		super.deactivate(context);

		LOGGER.debug("Deactivating MongoDB Component... Done.");
	}

	/**
	 * Registers MongoDB Service
	 */
	private void doRegister(final ComponentContext componentContext, final Map<String, Object> properties) {
		this.m_properties = properties;
		this.m_host = (String) this.m_properties.get(MONGO_DB_HOST);
		this.m_port = (int) this.m_properties.get(MONGO_DB_PORT);
		this.m_dbname = (String) this.m_properties.get(MONGO_DB_DBNAME);
		this.m_username = (String) this.m_properties.get(MONGO_DB_USERNAME);
		this.m_password = (String) this.m_properties.get(MONGO_DB_PASSWORD);

		if (this.m_username != null) {
			final MongoCredential credential = MongoCredential.createCredential(this.m_username, this.m_dbname,
					this.m_password.toCharArray());
			this.m_mongoClient = new MongoClient(new ServerAddress(this.m_host), Arrays.asList(credential));
			LOGGER.info("Authenticated as '" + this.m_username + "'");
		}

		this.registerMongoDBService(componentContext);
	}

	/**
	 * Registers {@link MongoDBService}
	 */
	private void registerMongoDBService(final ComponentContext componentContext) {
		final Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put("dbName", this.m_dbname);
		final MongoDBService dbService = new MongoDBServiceImpl(this.m_mongoClient,
				this.m_mongoClient.getDatabase(this.m_dbname));
		componentContext.getBundleContext().registerService(MongoDBService.class.getName(), dbService, properties);
	}

	/**
	 * Callback to be used while {@link CloudService} is deregistering
	 */
	public synchronized void unbindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == cloudService) {
			super.setCloudService(this.m_cloudService = null);
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is deregistering
	 */
	public synchronized void unbindConfigurationService(final ConfigurationService configurationService) {
		if (this.m_configurationService == configurationService) {
			this.m_configurationService = null;
		}
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updating MongoDB Component...");

		this.m_properties = properties;
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));
		this.doRegister(this.m_context, properties);

		LOGGER.info("Updating MongoDB Component... Done.");
	}

}