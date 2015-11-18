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
package de.tum.in.python.bluetooth.milling.machine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.util.ProcessUtil;
import org.eclipse.kura.core.util.SafeProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Used to execute python commands for Milling Machine Communication. The Linux
 * System needs to install the following tools externally to use this bundle.
 *
 * 1. pyBluez 2. Paho MQTT for Python 3. libbluetooth-dev 4. bluez-utils 5.
 * hciconfig 6. hcitool
 *
 * You have to run the bluetooth server using the same python code found in
 * resources directory. Command to execute in the bluetooth server:
 * {@code python bt.py machine}
 *
 * The scenario we have here that as soon as the client initiates a connection
 * with the server, the bluetooth server starts broadcasting data. The client
 * needs to read the data in real-time and publish it for the business clients
 * to use the data for further usage.
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class CommandUtil {

	/**
	 * Bluetooth Interface
	 */
	private static final String BD_IFACE = "hci0";

	/**
	 * Represents the hciconfig POSIX command utility
	 */
	private static final String CMD_HCI = "hciconfig";

	/**
	 * Represents the python POSIX command utility
	 */
	private static final String CMD_PYTHON = "python";

	/**
	 * Represents the python command line argument
	 */
	private static final String CMD_PYTHON_ARG = "gw";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtil.class);

	/**
	 * Python Program Location
	 */
	private static final String PROGRAM_LOCATION = "/home/pi/TUM/bt.py";

	/**
	 * Bluetooth Reset Argument
	 */
	private static final String RESET_ARG = "reset";

	/**
	 * Starts the communication with the provided bluetooth mac address of
	 * milling machine
	 */
	public static void initCommunication(final String macAddress) {
		LOGGER.info("Starting Python Bluetooth Milling Machine Communication...");

		// Before each and every bluetooth communication, reset the bluetooth
		// interface
		resetBluetoothInterface();

		SafeProcess process = null;
		BufferedReader br = null;
		final String[] command = { CMD_PYTHON, PROGRAM_LOCATION, CMD_PYTHON_ARG, macAddress };

		try {
			process = ProcessUtil.exec(command);
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.contains("command not found")) {
					LOGGER.error("Resetting Command Not Found");
					throw new KuraException(KuraErrorCode.OPERATION_NOT_SUPPORTED);
				}
			}

			LOGGER.info("Starting Python Bluetooth Milling Machine Communication...Done");
		} catch (final Exception e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		} finally {
			try {
				LOGGER.debug("Closing Buffered Reader and destroying Process: " + process);
				br.close();
				process.destroy();
				resetBluetoothInterface();
			} catch (final IOException e) {
				LOGGER.error("Error closing read buffer: " + Throwables.getStackTraceAsString(e));
			}
		}
	}

	/**
	 * Reset Bluetooth Interface
	 */
	public static void resetBluetoothInterface() {
		LOGGER.info("Bluetooth Interface is getting reset...");

		SafeProcess process = null;
		BufferedReader br = null;
		final String[] command = { CMD_HCI, BD_IFACE, RESET_ARG };

		try {
			process = ProcessUtil.exec(command);
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.contains("command not found")) {
					LOGGER.error("Resetting Command Not Found");
					throw new KuraException(KuraErrorCode.OPERATION_NOT_SUPPORTED);
				}
			}

			LOGGER.info("Bluetooth Interface is getting reset...Done");
		} catch (final Exception e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		} finally {
			try {
				LOGGER.debug("Closing Buffered Reader and destroying Process: " + process);
				br.close();
				process.destroy();
			} catch (final IOException e) {
				LOGGER.error("Error closing read buffer: " + Throwables.getStackTraceAsString(e));
			}
		}
	}

}