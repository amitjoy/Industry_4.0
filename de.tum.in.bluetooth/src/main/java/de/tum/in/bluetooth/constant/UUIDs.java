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

import javax.bluetooth.UUID;

/**
 * All the UUIDs needed for communication
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public enum UUIDs {

	ATT(0x0007), BNEP(0x000F), BROWSE_GROUP_DESCRIPTOR_SERVICE_CLASSID(0x1001), GROUP_NETWORK(0x1117), HTTP(
			0x000C), L2CAP(0x0100), NETWORK_ACCESS_POUUID(0x1116), OBEX(0x0008), OBEX_FILE_TRANSFER_PROFILE(
					0x1106), OBEX_OBJECT_PUSH_PROFILE(0x1105), PERSONAL_AREA_NETWORKING_USER(
							0x1115), PUBLIC_BROWSE_GROUP(0x1002), RFCOMM(0x0003), SDP(0x0001), SERIAL_PORT(
									0x1101), SERVICE_DISCOVERY_SERVER_SERVICE_CLASSID(0x1000);

	/**
	 * UUID constant
	 */
	private final int m_uuid;

	/**
	 * Constructor
	 */
	private UUIDs(final int uuid) {
		this.m_uuid = uuid;
	}

	/**
	 * Returns the UUID of the constant
	 */
	public final UUID uuid() {
		return new UUID(this.m_uuid);
	}
}
