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

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.config.OpcUaClientConfig;
import com.digitalpetri.opcua.sdk.client.api.identity.AnonymousProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.IdentityProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.UsernameProvider;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.Stack;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.google.common.base.Throwables;

import de.tum.in.opcua.client.util.KeyStoreLoader;

/**
 * Consumes {@link OpcUaClientAction} OSGi Services
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class OpcUaClientActionRunner {

	/**
	 * OPC-UA Client Builder
	 */
	public static class Builder {

		/**
		 * Application Certificate
		 */
		private String m_applicationCertificate;

		/**
		 * Application Name
		 */
		private String m_applicationName;

		/**
		 * Application Uri
		 */
		private String m_applicationUri;

		/**
		 * The provided client action to work
		 */
		private OpcUaClientAction m_clientAction;

		/**
		 * OPC-UA Endpoint URL
		 */
		private String m_endpointUrl;

		/**
		 * Keystore Client Alias
		 */
		private String m_keyStoreClientAlias;

		/**
		 * Keystore Password
		 */
		private String m_keyStorePassword;

		/**
		 * Keystore Server Alias
		 */
		private String m_keyStoreServerAlias;

		/**
		 * Keystore Type
		 */
		private String m_keyStoreType;

		/**
		 * OPC-UA username
		 */
		private String m_opcuaPassword;

		/**
		 * OPC-UA Password
		 */
		private String m_opcuaUsername;

		/**
		 * OPC-UA Request timeout
		 */
		private int m_requestTimeout;

		/**
		 * Security Policy for the action to perform
		 */
		private SecurityPolicy m_securityPolicy;

		/**
		 * Returns the main Runner
		 */
		public OpcUaClientActionRunner build() {
			return new OpcUaClientActionRunner(this.m_endpointUrl, this.m_securityPolicy, this.m_opcuaUsername,
					this.m_opcuaPassword, this.m_clientAction, this.m_keyStoreType, this.m_keyStoreServerAlias,
					this.m_keyStoreClientAlias, this.m_keyStorePassword, this.m_applicationName, this.m_applicationUri,
					this.m_applicationCertificate, this.m_requestTimeout);
		}

		/**
		 * Setter for Application Certificate
		 */
		public final Builder setApplicationCertificate(final String applicationCertificate) {
			this.m_applicationCertificate = applicationCertificate;
			return this;
		}

		/**
		 * Setter for Application Name
		 */
		public final Builder setApplicationName(final String applicationName) {
			this.m_applicationName = applicationName;
			return this;
		}

		/**
		 * Setter for Application URI
		 */
		public final Builder setApplicationUri(final String applicationUri) {
			this.m_applicationUri = applicationUri;
			return this;
		}

		/**
		 * Setter for Client Action
		 */
		public final Builder setClientAction(final OpcUaClientAction opcuaClientAction) {
			this.m_clientAction = opcuaClientAction;
			return this;
		}

		/**
		 * Setter for Endpoint URL
		 */
		public final Builder setEndpointUrl(final String endpointUrl) {
			this.m_endpointUrl = endpointUrl;
			return this;
		}

		/**
		 * Setter for Keystore Client Alias
		 */
		public final Builder setKeyStoreClientAlias(final String keyStoreClientAlias) {
			this.m_keyStoreClientAlias = keyStoreClientAlias;
			return this;
		}

		/**
		 * Setter for Keystore Password
		 */
		public final Builder setKeyStorePassword(final String keyStorePassword) {
			this.m_keyStorePassword = keyStorePassword;
			return this;
		}

		/**
		 * Setter for Keystore Server Alias
		 */
		public final Builder setKeyStoreServerAlias(final String keyStoreServerAlias) {
			this.m_keyStoreServerAlias = keyStoreServerAlias;
			return this;
		}

		/**
		 * Setter for Keystore Type
		 */
		public final Builder setKeystoreType(final String keystoreType) {
			this.m_keyStoreType = keystoreType;
			return this;
		}

		/**
		 * Setter for OPC-UA password
		 */
		public final Builder setOpcUaPassword(final String password) {
			this.m_opcuaPassword = password;
			return this;
		}

		/**
		 * Setter for OPC-UA username
		 */
		public final Builder setOpcUaUsername(final String username) {
			this.m_opcuaUsername = username;
			return this;
		}

		/**
		 * Setter for Request Timeout
		 */
		public final Builder setRequestTimeout(final int requestTimeout) {
			this.m_requestTimeout = requestTimeout;
			return this;
		}

		/**
		 * Setter for Security Policy
		 */
		public final Builder setSecurityPolicy(final SecurityPolicy securityPolicy) {
			this.m_securityPolicy = securityPolicy;
			return this;
		}

	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientActionRunner.class);

	/**
	 * Application Certificate
	 */
	private final String m_applicationCertificate;

	/**
	 * Application Name
	 */
	private final String m_applicationName;

	/**
	 * Application Uri
	 */
	private final String m_applicationUri;

	/**
	 * The provided client action to work
	 */
	private final OpcUaClientAction m_clientAction;

	/**
	 * OPC-UA Endpoint URL
	 */
	private final String m_endpointUrl;

	/**
	 * The worker thread for the action to perform
	 */
	private final CompletableFuture<OpcUaClient> m_future = new CompletableFuture<>();

	/**
	 * Keystore Client Alias
	 */
	private final String m_keyStoreClientAlias;

	/**
	 * Keystore Password
	 */
	private final String m_keyStorePassword;

	/**
	 * Keystore Server Alias
	 */
	private final String m_keyStoreServerAlias;

	/**
	 * Keystore Type
	 */
	private final String m_keyStoreType;

	/**
	 * The keystore loader
	 */
	private final KeyStoreLoader m_loader;

	/**
	 * OPC-UA username
	 */
	private final String m_opcuaPassword;

	/**
	 * OPC-UA Password
	 */
	private final String m_opcuaUsername;

	/**
	 * OPC-UA Request timeout
	 */
	private final int m_requestTimeout;

	/**
	 * Security Policy for the action to perform
	 */
	private final SecurityPolicy m_securityPolicy;

	/**
	 * Constructor
	 */
	private OpcUaClientActionRunner(final String endpointUrl, final SecurityPolicy securityPolicy,
			final String opcuaUsername, final String opcuaPassword, final OpcUaClientAction clientAction,
			final String keystoreType, final String keystoreServerAlias, final String keystoreClientAlias,
			final String keystorePassword, final String applicationName, final String applicationUri,
			final String applicationCert, final int requestTimeout) {
		this.m_endpointUrl = endpointUrl;
		this.m_securityPolicy = securityPolicy;
		this.m_clientAction = clientAction;
		this.m_applicationName = applicationName;
		this.m_applicationUri = applicationUri;
		this.m_keyStoreType = keystoreType;
		this.m_keyStoreClientAlias = keystoreClientAlias;
		this.m_keyStoreServerAlias = keystoreServerAlias;
		this.m_keyStorePassword = keystorePassword;
		this.m_applicationCertificate = applicationCert;
		this.m_requestTimeout = requestTimeout;
		this.m_opcuaUsername = opcuaUsername;
		this.m_opcuaPassword = opcuaPassword;
		this.m_loader = new KeyStoreLoader(this.m_keyStoreType, this.m_keyStoreClientAlias, this.m_keyStoreServerAlias,
				this.m_keyStorePassword, this.m_applicationCertificate);
	}

	/**
	 * Creates the OPC-UA Client reference for the action needed to perform
	 */
	private OpcUaClient createClient() throws Exception {
		final EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(this.m_endpointUrl).get();

		final EndpointDescription endpoint = Arrays.stream(endpoints)
				.filter(e -> e.getSecurityPolicyUri().equals(this.m_securityPolicy.getSecurityPolicyUri())).findFirst()
				.orElseThrow(() -> new Exception("no desired endpoints returned"));

		LOGGER.info("Using endpoint " + endpoint.getEndpointUrl() + " with " + this.m_securityPolicy);

		this.m_loader.load();

		IdentityProvider identityProvider = null;

		if (isNullOrEmpty(this.m_opcuaUsername) && isNullOrEmpty(this.m_opcuaPassword)) {
			identityProvider = new AnonymousProvider();
		} else {
			identityProvider = new UsernameProvider(this.m_opcuaUsername, this.m_opcuaPassword);
		}

		final OpcUaClientConfig config = OpcUaClientConfig.builder()
				.setApplicationName(LocalizedText.english(this.m_applicationName))
				.setApplicationUri(this.m_applicationUri).setCertificate(this.m_loader.getClientCertificate())
				.setKeyPair(this.m_loader.getClientKeyPair()).setEndpoint(endpoint)
				.setIdentityProvider(identityProvider).setRequestTimeout(uint(this.m_requestTimeout * 1000)).build();

		return new OpcUaClient(config);
	}

	/**
	 * Finalize the required action
	 */
	public void run() {
		this.m_future.whenComplete((client, ex) -> {
			if (client != null) {
				try {
					client.disconnect().get();
					Stack.releaseSharedResources();
				} catch (InterruptedException | ExecutionException e) {
					LOGGER.error("Error disconnecting: " + Throwables.getStackTraceAsString(e));
				}
			} else {
				LOGGER.error("Error running OPC-UA Action: " + Throwables.getStackTraceAsString(ex));
				Stack.releaseSharedResources();
			}

		});

		try {
			final OpcUaClient client = this.createClient();

			try {
				this.m_clientAction.run(client, this.m_future);
				this.m_future.get(5, TimeUnit.SECONDS);
			} catch (final Throwable t) {
				LOGGER.error("Error running OPC-UA Action: " + Throwables.getStackTraceAsString(t));
				this.m_future.complete(client);
			}
		} catch (final Throwable t) {
			LOGGER.error("Error running OPC-UA Action: " + Throwables.getStackTraceAsString(t));
			this.m_future.completeExceptionally(t);
		}

		try {
			Thread.sleep(999999999);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
}
