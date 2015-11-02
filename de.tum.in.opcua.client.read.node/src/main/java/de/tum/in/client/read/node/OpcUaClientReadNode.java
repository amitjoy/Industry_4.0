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
package de.tum.in.client.read.node;

import java.util.concurrent.CompletableFuture;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.google.common.base.Throwables;

import de.tum.in.opcua.client.OpcUaClientAction;

/**
 * This bundle is responsible for reading node of OPC-UA Server (Example Read
 * Node)
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.opcua.client.read.node")
@Service(value = { OpcUaClientAction.class })
public class OpcUaClientReadNode implements OpcUaClientAction {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "OPC-READ-NODE-V1";

	/**
	 * The OPC-UA Endpoint URL
	 */
	private static final String ENDPOINT_URL = "opc.tcp://localhost:12685/tum";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientReadNode.class);

	/**
	 * Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/* Constructor */
	public OpcUaClientReadNode() {
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext) {
		LOGGER.info("Activating OPC-UA Read Node Component...");

		LOGGER.info("Activating OPC-UA Read Node Component... Done.");

	}

	/**
	 * Callback to be used while {@link CloudService} is registering
	 */
	public synchronized void bindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == null) {
			this.m_cloudService = cloudService;
		}
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating OPC-UA Read Node Component...");

		LOGGER.debug("Deactivating OPC-UA Read Node Component... Done.");
	}

	/** {@inheritDoc}} */
	@Override
	public String endpointUrl() {
		return ENDPOINT_URL;
	}

	/** {@inheritDoc}} */
	@Override
	public String name() {
		return "OPC-UA-CLIENT-READ-NODE";
	}

	/** {@inheritDoc}} */
	@Override
	public void run(final OpcUaClient client, final CompletableFuture<OpcUaClient> future) throws Exception {
		// synchronous connect
		client.connect().get();

		final NodeId nodeId = new NodeId(2, "/Static/AllProfiles/Scalar/Int32");

		// read the value of the current time node
		final UaVariableNode variableNode = client.getAddressSpace().getVariableNode(nodeId);

		final Object value = variableNode.readValueAttribute().get();

		LOGGER.info("value" + value);

		final KuraPayload payload = new KuraPayload();
		payload.addMetric("value", value);

		// Publish the data to $EDC/account_name/device_id/OPC-READ-NODE-V1/data
		try {
			this.m_cloudService.newCloudClient(APP_ID).controlPublish("data", payload, 0, false, 5);
		} catch (final Exception e) {
			LOGGER.warn(Throwables.getStackTraceAsString(e));
		}

		future.complete(client);
	}

	/**
	 * Callback to be used while {@link CloudService} is deregistering
	 */
	public synchronized void unbindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == cloudService) {
			this.m_cloudService = null;
		}
	}

}