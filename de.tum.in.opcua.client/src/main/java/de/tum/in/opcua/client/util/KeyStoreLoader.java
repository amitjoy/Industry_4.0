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
package de.tum.in.opcua.client.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Loads the provided keystore
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class KeyStoreLoader {

	/**
	 * Keystore Certificate
	 */
	private final String m_certificate;

	/**
	 * Keystore Client Alias
	 */
	private final String m_clientAlias;

	/**
	 * Keystore Client Certificate
	 */
	private X509Certificate m_clientCertificate;

	/**
	 * Keystore Key Pair
	 */
	private KeyPair m_clientKeyPair;

	/**
	 * Keystore Type
	 */
	private final String m_keystoreType;

	/**
	 * Keystore Password
	 */
	private final char[] m_password;

	/**
	 * Keystore Server Alias
	 */
	@SuppressWarnings("unused")
	private final String m_serverAlias;

	/**
	 * Constructor
	 */
	public KeyStoreLoader(final String keystoreType, final String clientAlias, final String serverAlias,
			final String password, final String certificate) {
		this.m_keystoreType = keystoreType;
		this.m_clientAlias = clientAlias;
		this.m_serverAlias = serverAlias;
		this.m_password = password.toCharArray();
		this.m_certificate = certificate;
	}

	/**
	 * Returns the client certificate
	 */
	public X509Certificate getClientCertificate() {
		return this.m_clientCertificate;
	}

	/**
	 * Returns the client key pair
	 */
	public KeyPair getClientKeyPair() {
		return this.m_clientKeyPair;
	}

	/**
	 * Loads the certificate
	 */
	public KeyStoreLoader load() throws Exception {
		final KeyStore keyStore = KeyStore.getInstance(this.m_keystoreType);
		keyStore.load(Files.newInputStream(Paths.get(this.m_certificate)), this.m_password);
		final Key clientPrivateKey = keyStore.getKey(this.m_clientAlias, this.m_password);

		if (clientPrivateKey instanceof PrivateKey) {
			this.m_clientCertificate = (X509Certificate) keyStore.getCertificate(this.m_clientAlias);
			final PublicKey clientPublicKey = this.m_clientCertificate.getPublicKey();
			this.m_clientKeyPair = new KeyPair(clientPublicKey, (PrivateKey) clientPrivateKey);
		}

		return this;
	}
}
