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
package de.tum.in.bluetooth.constant;

/**
 * All the bluetooth service discovery related constants
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public enum ServiceConstants {

	BLUETOOTH_PROFILE_DESCRIPTOR_LIST(0X0009), BROWSE_GROUP_LIST(0X0005), CLIENT_EXECUTABLE_URL(
			0X000B), DOCUMENTATION_URL(0X000A), ICON_URL(0X000C), LANGUAGE_BASED_ATTRIBUTE_ID_LIST(
					0X0006), PROTOCOL_DESCRIPTOR_LIST(0X0004), SERVICE_AVAILABILITY(0X0008), SERVICE_CLASSID_LIST(
							0X0001), SERVICE_DATABASE_STATE(0X0201), SERVICE_ID(0X0003), SERVICE_INFO_TIME_TO_LIVE(
									0X0007), SERVICE_NAME(0X0100), SERVICE_RECORD_HANDLE(0X0000), SERVICE_RECORD_STATE(
											0X0002), VERSION_NUMBER_LIST(0X0200);

	/**
	 * Service Id of the constants
	 */
	private final int m_serviceId;

	/** Constructor */
	private ServiceConstants(final int serviceId) {
		this.m_serviceId = serviceId;
	}

	/**
	 * @return the m_serviceId
	 */
	public final int serviceId() {
		return this.m_serviceId;
	}

}
