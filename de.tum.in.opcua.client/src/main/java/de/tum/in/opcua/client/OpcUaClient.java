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
package de.tum.in.opcua.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.google.common.collect.Lists;

import de.tum.in.activity.log.ActivityLogService;
import de.tum.in.activity.log.IActivityLogService;

/**
 * This bundle is responsible for communicating with the OPC-UA Server
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.opcua.client")
@Service(value = { OpcUaClient.class })
public class OpcUaClient extends Cloudlet implements ConfigurableComponent {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "OPCUA-V1";

	/**
	 * Configurable property to set client alias for the keystore
	 */
	private static final String KEYSTORE_CLIENT_ALIAS = "keystore.client.alias";

	/**
	 * Configurable Property to set keystore password
	 */
	private static final String KEYSTORE_PASSWORD = "keystore.password";

	/**
	 * Configurable Property to set server alias for the keystore
	 */
	private static final String KEYSTORE_SERVER_ALIAS = "keystore.server.alias";

	/**
	 * Configurable Property to set keystore type
	 */
	private static final String KEYSTORE_TYPE = "keystore.type";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClient.class);

	/**
	 * Configurable Property to set OPC-UA application certificate
	 */
	private static final String OPCUA_APPLICATION_CERTIFICATE = "opcua.certificate.location";

	/**
	 * Configurable Property to set OPC-UA application name
	 */
	private static final String OPCUA_APPLICATION_NAME = "opcua.application.name";

	/**
	 * Configurable Property to set OPC-UA application uri
	 */
	private static final String OPCUA_APPLICATION_URI = "opcua.application.uri";

	/**
	 * Configurable Property to OPC-UA server password
	 */
	private static final String OPCUA_PASSWORD = "opcua.password";

	/**
	 * Configurable property specifying the request timeout
	 */
	private static final String OPCUA_REQUEST_TIMEOUT = "opcua.request.timeout";

	/**
	 * Configurable property specifying the Security Policy
	 */
	private static final String OPCUA_SECURITY_POLICY = "opcua.security.policy";

	/**
	 * Configurable Property to set OPC-UA server username
	 */
	private static final String OPCUA_USERNAME = "opcua.password";

	/**
	 * Activity Log Service Dependency
	 */
	@Reference(bind = "bindActivityLogService", unbind = "unbindActivityLogService")
	private volatile IActivityLogService m_activityLogService;

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
	 * Placeholder for keystore client alias
	 */
	private String m_keystoreClientAlias;

	/**
	 * Placeholder for keystore password
	 */
	private String m_keystorePassword;

	/**
	 * Placeholder for keystore server alias
	 */
	private String m_keystoreServerAlias;

	/**
	 * Placeholder for keystore type
	 */
	private String m_keystoreType;

	/**
	 * Placeholder for OPC-UA certificate location
	 */
	private String m_opcuaApplicationCert;

	/**
	 * Placeholder for OPC-UA application name
	 */
	private String m_opcuaApplicationName;

	/**
	 * Placeholder for OPC-UA application uri
	 */
	private String m_opcuaApplicationUri;

	/**
	 * OPC-UA Client Service Injection
	 */
	@Reference(bind = "bindOpcUa", unbind = "unbindOpcUa", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private volatile OpcUaClientAction m_opcuaClientAction;

	/**
	 * Holds List of {@link OpcUaClientAction}
	 */
	private final List<OpcUaClientAction> m_opcuaClientActions = Lists.newCopyOnWriteArrayList();

	/**
	 * Placeholder for OPC-UA password
	 */
	private String m_opcuaPassword;

	/**
	 * Placeholder for security policy
	 */
	private SecurityPolicy m_opcuaSecurityPolicy;

	/**
	 * Placeholder for OPC-UA username
	 */
	private String m_opcuaUsername;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * Placeholder for request timeout
	 */
	private int m_requestTimeout;

	/**
	 * Placeholder for security policy
	 */
	private int m_securityPolicy;

	/* Constructor */
	public OpcUaClient() {
		super(APP_ID);
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating OPC-UA Component...");

		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);
		this.reinitializeConfiguration(properties);

		LOGGER.info("Activating OPC-UA Component... Done.");

	}

	/**
	 * Callback to be used while {@link ActivityLogService} is registering
	 */
	public synchronized void bindActivityLogService(final IActivityLogService activityLogService) {
		if (this.m_activityLogService == null) {
			this.m_activityLogService = activityLogService;
		}
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
	 * Callback to be used while {@link OpcUaClientAction} is registering
	 */
	public synchronized void bindOpcUa(final OpcUaClientAction opcuaClientAction) {
		if (!this.m_opcuaClientActions.contains(opcuaClientAction)) {
			this.m_opcuaClientActions.add(opcuaClientAction);
		}
	}

	/**
	 * Retrieves Proper Security Policy required for the client actions
	 */
	private void configureSecurityPolicy() {
		switch (this.m_securityPolicy) {
		case 0:
			this.m_opcuaSecurityPolicy = SecurityPolicy.None;
			break;
		case 1:
			this.m_opcuaSecurityPolicy = SecurityPolicy.Basic128Rsa15;
			break;
		case 2:
			this.m_opcuaSecurityPolicy = SecurityPolicy.Basic256;
			break;
		case 3:
			this.m_opcuaSecurityPolicy = SecurityPolicy.Basic256Sha256;
			break;
		}
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating OPC-UA Component...");
		LOGGER.debug("Deactivating OPC-UA Component... Done.");
	}

	/** {@inheritDoc}} */
	@Override
	protected void doExec(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("OPC-UA Client Action Started...");
		final String clientName = (String) reqPayload.getMetric("opcClientName");
		checkNotNull(clientName);

		this.m_opcuaClientActions.stream().filter(opcClientName -> clientName.equals(opcClientName))
				.forEach(opcuaClientAction -> {
					final OpcUaClientActionRunner clientActionRunner = new OpcUaClientActionRunner.Builder()
							.setApplicationName(this.m_opcuaApplicationName)
							.setApplicationUri(this.m_opcuaApplicationUri)
							.setApplicationCertificate(this.m_opcuaApplicationCert)
							.setRequestTimeout(this.m_requestTimeout).setKeyStoreClientAlias(this.m_keystoreClientAlias)
							.setKeyStorePassword(this.m_keystorePassword)
							.setKeyStoreServerAlias(this.m_keystoreServerAlias).setKeystoreType(this.m_keystoreType)
							.setEndpointUrl(opcuaClientAction.endpointUrl())
							.setSecurityPolicy(this.m_opcuaSecurityPolicy).setOpcUaUsername(this.m_opcuaUsername)
							.setOpcUaPassword(this.m_opcuaPassword).build();
					clientActionRunner.run();
				});

		this.m_activityLogService.saveLog("OPC-UA Client Action Executed");

		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

		LOGGER.info("OPC-UA Client Action Communication Done");
	}

	/** {@inheritDoc}} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("OPC-UA Configuration Retrieving...");
		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			final ComponentConfiguration configuration = this.m_configurationService.getComponentConfiguration(APP_ID);

			final IterableMap map = new HashedMap(configuration.getConfigurationProperties());
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}
			this.m_activityLogService.saveLog("OPC-UA Configuration Retrieved");

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		if ("list".equals(reqTopic.getResources()[0])) {
			final KuraPayload payload = new KuraPayload();
			this.m_opcuaClientActions.stream().map(opcClientAction -> opcClientAction.name())
					.forEach(opcClientActionName -> payload.addMetric("action", opcClientActionName));
		}

		LOGGER.info("OPC-UA Configuration Retrieved");
	}

	/** {@inheritDoc}} */
	@Override
	protected void doPut(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("OPC-UA Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			this.m_configurationService.updateConfiguration(APP_ID, reqPayload.metrics());

			this.m_activityLogService.saveLog("OPC-UA Configuration Updated");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("OPC-UA Configuration Updated");
	}

	/**
	 * Used to extract configuration for populating placeholders with respective
	 * values
	 */
	private void extractConfiguration() {
		this.m_keystoreClientAlias = (String) this.m_properties.get(KEYSTORE_CLIENT_ALIAS);
		this.m_keystoreServerAlias = (String) this.m_properties.get(KEYSTORE_SERVER_ALIAS);
		this.m_keystorePassword = (String) this.m_properties.get(KEYSTORE_PASSWORD);
		this.m_opcuaApplicationName = (String) this.m_properties.get(OPCUA_APPLICATION_NAME);
		this.m_opcuaApplicationUri = (String) this.m_properties.get(OPCUA_APPLICATION_URI);
		this.m_opcuaApplicationCert = (String) this.m_properties.get(OPCUA_APPLICATION_CERTIFICATE);
		this.m_securityPolicy = (int) this.m_properties.get(OPCUA_SECURITY_POLICY);
		this.m_requestTimeout = (int) this.m_properties.get(OPCUA_REQUEST_TIMEOUT);
		this.m_keystoreType = (String) this.m_properties.get(KEYSTORE_TYPE);
		this.m_opcuaPassword = (String) this.m_properties.get(OPCUA_PASSWORD);
		this.m_opcuaUsername = (String) this.m_properties.get(OPCUA_USERNAME);
	}

	/**
	 * Reinitialize Configurations as it gets updated
	 */
	private void reinitializeConfiguration(final Map<String, Object> properties) {
		this.m_properties = properties;
		this.extractConfiguration();
		this.configureSecurityPolicy();
	}

	/**
	 * Callback to be used while {@link ActivityLogService} is deregistering
	 */
	public synchronized void unbindActivityLogService(final IActivityLogService activityLogService) {
		if (this.m_activityLogService == activityLogService) {
			this.m_activityLogService = null;
		}
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
	 * Callback to be used while {@link OpcUaClientAction} is deregistering
	 */
	public synchronized void unbindOpcUa(final OpcUaClientAction opcuaClientAction) {
		if ((this.m_opcuaClientActions.size() > 0) && this.m_opcuaClientActions.contains(opcuaClientAction)) {
			this.m_opcuaClientActions.remove(opcuaClientAction);
		}
	}

	/**
	 * Used to be called when configurations get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updating OPC-UA Component...");

		this.reinitializeConfiguration(properties);

		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updating OPC-UA Component... Done.");
	}

}