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
package de.tum.in.client.write;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.google.common.collect.Lists;

import de.tum.in.opcua.client.OpcUaClientAction;

/**
 * This bundle is responsible for writing to OPC-UA Server (Example Write)
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.opcua.client.write")
@Service(value = { OpcUaClientAction.class })
public class OpcUaClientWrite implements OpcUaClientAction {

	/**
	 * The OPC-UA Endpoint URL
	 */
	private static final String ENDPOINT_URL = "opc.tcp://localhost:12685/tum";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientWrite.class);

	/* Constructor */
	public OpcUaClientWrite() {
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext) {
		LOGGER.info("Activating OPC-UA Write Component...");

		LOGGER.info("Activating OPC-UA Write Component... Done.");

	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating OPC-UA Write Component...");

		LOGGER.debug("Deactivating OPC-UA Write Component... Done.");
	}

	/** {@inheritDoc}} */
	@Override
	public String endpointUrl() {
		return ENDPOINT_URL;
	}

	/** {@inheritDoc}} */
	@Override
	public String name() {
		return "OPC-UA-CLIENT-WRITE";
	}

	/** {@inheritDoc}} */
	@Override
	public void run(final OpcUaClient client, final CompletableFuture<OpcUaClient> future) throws Exception {
		client.connect().get();

		final List<NodeId> nodeIds = Lists.newArrayList(new NodeId(2, "/Static/AllProfiles/Scalar/Int32"));

		for (int i = 0; i < 10; i++) {
			final Variant v = new Variant(i);

			// don't write status or timestamps
			final DataValue dv = new DataValue(v, null, null);

			// write asynchronously....
			final CompletableFuture<List<StatusCode>> f = client.writeValues(nodeIds, Lists.newArrayList(dv));

			// ...but block for the results so we write in order
			final List<StatusCode> statusCodes = f.get();
			final StatusCode status = statusCodes.get(0);

			if (status.isGood()) {
				LOGGER.info("Wrote " + v + " to nodeId " + nodeIds.get(0));
			}
		}

		future.complete(client);
	}

}