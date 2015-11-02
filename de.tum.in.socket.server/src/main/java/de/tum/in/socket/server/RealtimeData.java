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
package de.tum.in.socket.server;

import com.google.common.base.Objects;

public final class RealtimeData {
	private String force_x;
	private String force_y;
	private String force_z;
	private String time;
	private String torque_x;
	private String torque_y;
	private String torque_z;

	public String getForce_x() {
		return this.force_x;
	}

	public String getForce_y() {
		return this.force_y;
	}

	public String getForce_z() {
		return this.force_z;
	}

	public String getTime() {
		return this.time;
	}

	public String getTorqueX() {
		return this.torque_x;
	}

	public String getTorqueY() {
		return this.torque_y;
	}

	public String getTorqueZ() {
		return this.torque_z;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.force_x, this.force_y, this.force_z, this.time, this.torque_x, this.torque_y,
				this.torque_z);
	}

	public void setDepthCut(final String depth_of_cut) {
		this.time = depth_of_cut;
	}

	public void setForce_x(final String force_x) {
		this.force_x = force_x;
	}

	public void setForce_y(final String force_y) {
		this.force_y = force_y;
	}

	public void setForce_z(final String force_z) {
		this.force_z = force_z;
	}

	public void setTime(final String time) {
		this.time = time;
	}

	public void setTorqueX(final String torque_x) {
		this.torque_x = torque_x;
	}

	public void setTorqueY(final String torque_y) {
		this.torque_y = torque_y;
	}

	public void setTorqueZ(final String torque_z) {
		this.torque_z = torque_z;
	}

	@Override
	public String toString() {
		return "force_x=" + this.force_x + ", force_y=" + this.force_y + ", force_z=" + this.force_z + ", time="
				+ this.time + ", torque_x=" + this.torque_x + ", torque_y=" + this.torque_y + ", torque_z="
				+ this.torque_z;
	}
}
