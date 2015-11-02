/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
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

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;

/**
 * Common Interface for consumers to expose client actions as OSGi Services.
 * Every client action needs to implement this interface.
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public interface OpcUaClientAction {

	/**
	 * Returns Endpoint URL
	 */
	public String endpointUrl();

	/**
	 * Returns the name of the client action
	 */
	public String name();

	/**
	 * The main action to perform
	 */
	public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception;

}
