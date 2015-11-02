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
package de.tum.in.bluetooth.devices;

import java.math.BigInteger;

import javax.bluetooth.RemoteDevice;

import com.google.common.base.MoreObjects;

/**
 * Represents a {@link RemoteDevice} in an abstract way
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class Device {

	private String id;
	private BigInteger maxRetry;
	private String password;
	private String pin;
	private String realm;
	private boolean retry;
	private String username;

	/**
	 * Gets the value of the id property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets the value of the maxRetry property.
	 *
	 * @return possible object is {@link BigInteger }
	 *
	 */
	public BigInteger getMaxRetry() {
		return this.maxRetry;
	}

	/**
	 * Gets the value of the password property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Gets the value of the pin property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getPin() {
		return this.pin;
	}

	/**
	 * Gets the value of the realm property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getRealm() {
		return this.realm;
	}

	/**
	 * Gets the value of the username property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Gets the value of the retry property.
	 *
	 */
	public boolean isRetry() {
		return this.retry;
	}

	/**
	 * Sets the value of the id property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setId(final String value) {
		this.id = value;
	}

	/**
	 * Sets the value of the maxRetry property.
	 *
	 * @param value
	 *            allowed object is {@link BigInteger }
	 *
	 */
	public void setMaxRetry(final BigInteger value) {
		this.maxRetry = value;
	}

	/**
	 * Sets the value of the password property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setPassword(final String value) {
		this.password = value;
	}

	/**
	 * Sets the value of the pin property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setPin(final String value) {
		this.pin = value;
	}

	/**
	 * Sets the value of the realm property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setRealm(final String value) {
		this.realm = value;
	}

	/**
	 * Sets the value of the retry property.
	 *
	 */
	public void setRetry(final boolean value) {
		this.retry = value;
	}

	/**
	 * Sets the value of the username property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setUsername(final String value) {
		this.username = value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", this.id).add("username", this.username)
				.add("password", this.password).add("pin", this.pin).add("retry", this.retry)
				.add("max-retry", this.maxRetry).toString();
	}

}
