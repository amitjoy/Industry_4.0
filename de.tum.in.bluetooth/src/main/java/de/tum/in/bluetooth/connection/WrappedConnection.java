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
package de.tum.in.bluetooth.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Used to wrap Bluetooth Serial Profile Connection stream
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class WrappedConnection implements StreamConnection {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WrappedConnection.class);

	/**
	 * Streaming connection for bluetooth communication
	 */
	private final StreamConnection m_connection;

	/**
	 * Data Object Input Stream for Bluetooth Communication
	 */
	private DataInputStream m_dataInputStream;

	/**
	 * Data Object Input Stream for Bluetooth Communication
	 */
	private DataOutputStream m_dataOutputStream;

	/**
	 * Communication Input Stream
	 */
	private InputStream m_inputStream;

	/**
	 * Communication Output Stream
	 */
	private OutputStream m_outputStream;

	/** Constructor */
	public WrappedConnection(final StreamConnection connection) {
		LOGGER.debug("Constructing wrapped connection");
		this.m_connection = connection;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {

		try {
			LOGGER.debug("Closing connection");
			this.m_connection.close();
			if (this.m_dataInputStream != null) {
				LOGGER.debug("Closing dataInputStream");
				this.m_dataInputStream.close();
			}
			if (this.m_inputStream != null) {
				LOGGER.debug("Closing inputStream");
				this.m_inputStream.close();
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to close connection" + Throwables.getStackTraceAsString(e));
			throw e;
		}

	}

	/** {@inheritDoc} */
	@Override
	public DataInputStream openDataInputStream() throws IOException {
		LOGGER.info("Opening DataInputStream connection");
		try {
			this.m_dataInputStream = this.m_connection.openDataInputStream();
			return this.m_dataInputStream;
		} catch (final Exception e) {
			LOGGER.error("Failed to open DataInputStream" + Throwables.getStackTraceAsString(e));
			throw e;
		}
	}

	/** {@inheritDoc} */
	@Override
	public DataOutputStream openDataOutputStream() throws IOException {
		LOGGER.debug("Opening DataOutputStream connection");
		try {
			this.m_dataOutputStream = this.m_connection.openDataOutputStream();
			return this.m_dataOutputStream;
		} catch (final Exception e) {
			LOGGER.error("Failed to open DataOutputStream" + Throwables.getStackTraceAsString(e));
			throw e;

		}
	}

	/** {@inheritDoc} */
	@Override
	public InputStream openInputStream() throws IOException {
		LOGGER.debug("Opening InputStream connection");
		try {
			this.m_inputStream = this.m_connection.openInputStream();
			return this.m_inputStream;
		} catch (final Exception e) {
			LOGGER.error("Failed to open InputStream" + Throwables.getStackTraceAsString(e));
			throw e;

		}
	}

	/** {@inheritDoc} */
	@Override
	public OutputStream openOutputStream() throws IOException {
		LOGGER.debug("Opening OutputStream connection");
		try {
			this.m_outputStream = this.m_connection.openOutputStream();
			return this.m_outputStream;
		} catch (final Exception e) {
			LOGGER.error("Failed to open OutputStream", Throwables.getStackTraceAsString(e));
			throw e;

		}
	}

}
