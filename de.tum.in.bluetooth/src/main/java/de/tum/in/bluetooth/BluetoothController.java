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
package de.tum.in.bluetooth;

/**
 * All required services for Bluetooth Operation
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public interface BluetoothController {

	/**
	 * Exception for Bluetooth Communication Problem
	 */
	final class BluetoothException extends Exception {

		private static final String MESSAGE = "Bluetooth Initiation Failed";
		private static final long serialVersionUID = 1L;

		public BluetoothException() {
			super(MESSAGE);
		}

	}

	/**
	 * Used to get the current bluetooth device stack
	 *
	 * @return the name for the bluetooth stack
	 */
	public String getBluetoothStack();

	/**
	 * Used to check whether the local bluetooth device is turned on or not
	 *
	 * @return true if on else off
	 */
	public boolean isBluetoothDeviceTurnedOn();

	/**
	 * Used to check whether the stack used for bluetooth discovery is supported
	 * or not
	 *
	 * @return true if bluetooth stack is supported or false
	 */
	public boolean isBluetoothStackSupported();

	/**
	 * Initializes the Bluetooth Discovery
	 */
	public void start();

	/**
	 * Stops the Bluetooth Discovery
	 */
	public void stop();

}
