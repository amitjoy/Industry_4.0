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
package de.tum.in.bluetooth.discovery;

/**
 * System Property Utility class to determine whether bluetooth application will
 * be used for testing purposes or production purposes
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class Env {

	/**
	 * Disables the test environment
	 */
	public static void disableTestEnvironment() {
		System.setProperty("bluetooth.test", "false");
	}

	/**
	 * Enables the test environment
	 */
	public static void enableTestEnvironment() {
		System.setProperty("bluetooth.test", "true");
	}

	/**
	 * Checks to see if test environment is enables
	 *
	 * @return true if test environment is enabled
	 */
	public static boolean isTestEnvironmentEnabled() {
		return Boolean.getBoolean("bluetooth.test");
	}

	/** Constructor */
	private Env() {
		// Empty. Not used for instantiation
	}
}
