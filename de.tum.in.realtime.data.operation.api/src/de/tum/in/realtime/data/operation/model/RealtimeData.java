/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package de.tum.in.realtime.data.operation.model;

import org.osgi.dto.DTO;

/**
 * Bluetooth Real time Data Representation
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class RealtimeData extends DTO {

	public String force_x;
	public String force_y;
	public String force_z;
	public String time;
	public String torque_x;
	public String torque_y;
	public String torque_z;
	public String type;

	@Override
	public String toString() {
		return "force_x=" + this.force_x + ", force_y=" + this.force_y + ", force_z=" + this.force_z + ", torque_x="
				+ this.torque_x + ", torque_y=" + this.torque_y + ", torque_z=" + this.torque_z + ", type=" + this.type;
	}
}
