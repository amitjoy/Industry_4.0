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
package de.tum.in.client.write.node;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;

import de.tum.in.opcua.client.OpcUaClientAction;

/**
 * This bundle is responsible for writing to a node of OPC-UA Server (Example
 * Write Node)
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.opcua.client.write.node")
@Service(value = { OpcUaClientAction.class })
public class OpcUaClientWriteNode implements OpcUaClientAction {

	/**
	 * The OPC-UA Endpoint URL
	 */
	private static final String ENDPOINT_URL = "opc.tcp://localhost:12685/tum";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientWriteNode.class);

	/* Constructor */
	public OpcUaClientWriteNode() {
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext) {
		LOGGER.info("Activating OPC-UA Write Node Component...");

		LOGGER.info("Activating OPC-UA Write Node Component... Done.");

	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating OPC-UA Write Node Component...");

		LOGGER.debug("Deactivating OPC-UA Write Node Component... Done.");
	}

	/** {@inheritDoc}} */
	@Override
	public String endpointUrl() {
		return ENDPOINT_URL;
	}

	/** {@inheritDoc}} */
	@Override
	public String name() {
		return "OPC-UA-CLIENT-WRITE-NODE";
	}

	/** {@inheritDoc}} */
	@Override
	public void run(final OpcUaClient client, final CompletableFuture<OpcUaClient> future) throws Exception {
		// synchronous connect
		client.connect().get();

		final NodeId nodeId = new NodeId(2, "/Static/AllProfiles/Scalar/Int32");

		final UaVariableNode variableNode = client.getAddressSpace().getVariableNode(nodeId);

		// read the existing value
		final Object valueBefore = variableNode.readValueAttribute().get();
		LOGGER.info("valueBefore " + valueBefore);

		// write a new random value (status and timestamps not included)
		final DataValue newValue = new DataValue(new Variant(new Random().nextInt()), null, null);
		final StatusCode writeStatus = variableNode.writeValue(newValue).get();
		LOGGER.info("writeStatus " + writeStatus);

		// read the value again
		final Object valueAfter = variableNode.readValueAttribute().get();
		LOGGER.info("valueAfter " + valueAfter);

		future.complete(client);
	}

}