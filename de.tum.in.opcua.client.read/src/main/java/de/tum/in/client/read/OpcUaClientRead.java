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
package de.tum.in.client.read;

import java.util.List;
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
import com.digitalpetri.opcua.stack.core.Identifiers;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.enumerated.ServerState;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import de.tum.in.opcua.client.OpcUaClientAction;

/**
 * This bundle is responsible for reading OPC-UA Server (Example Read)
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.opcua.client.read")
@Service(value = { OpcUaClientAction.class })
public class OpcUaClientRead implements OpcUaClientAction {

	/**
	 * The OPC-UA Endpoint URL
	 */
	private static final String ENDPOINT_URL = "opc.tcp://localhost:12685/tum";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientRead.class);

	/**
	 * Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/* Constructor */
	public OpcUaClientRead() {
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext) {
		LOGGER.info("Activating OPC-UA Read Component...");

		LOGGER.info("Activating OPC-UA Read Component... Done.");

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
		LOGGER.debug("Deactivating OPC-UA Read Component...");

		LOGGER.debug("Deactivating OPC-UA Read Component... Done.");
	}

	/** {@inheritDoc}} */
	@Override
	public String endpointUrl() {
		return ENDPOINT_URL;
	}

	/** {@inheritDoc}} */
	@Override
	public String name() {
		return "OPC-UA-CLIENT-READ";
	}

	/**
	 * Reads server state and time
	 */
	private CompletableFuture<List<DataValue>> readServerStateAndTime(final OpcUaClient client) {
		final List<NodeId> nodeIds = Lists.newArrayList(Identifiers.Server_ServerStatus_State,
				Identifiers.Server_ServerStatus_CurrentTime);

		return client.readValues(0.0, TimestampsToReturn.Both, nodeIds);
	}

	/** {@inheritDoc}} */
	@Override
	public void run(final OpcUaClient client, final CompletableFuture<OpcUaClient> future) throws Exception {
		client.connect().get();

		this.readServerStateAndTime(client).thenAccept(values -> {
			final DataValue v0 = values.get(0);
			final DataValue v1 = values.get(1);
			final Object state = ServerState.from((Integer) v0.getValue().getValue());
			final Object currentTime = v1.getValue().getValue();

			LOGGER.info("State: " + state);
			LOGGER.info("CurrentTime: " + currentTime);

			final KuraPayload payload = new KuraPayload();
			payload.addMetric("state", state);
			payload.addMetric("current-time", currentTime);
			try {
				this.m_cloudService.newCloudClient("OPC-READ-V1").controlPublish("opc-read-v1", payload, 0, false, 5);
			} catch (final Exception e) {
				LOGGER.warn(Throwables.getStackTraceAsString(e));
			}

			future.complete(client);
		});
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