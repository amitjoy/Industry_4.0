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
package de.tum.in.client.message;

import java.util.Date;

/**
 * EdcPosition is a data structure to capture a geo location. It can be
 * associated to an EdcPayload to geotag an EdcMessage before sending to the
 * Everyware Cloud. Refer to the description of each of the fields for more
 * information on the model of EdcPosition.
 */
public class KuraPosition {
	/**
	 * Altitude of the position in meters.
	 */
	private Double altitude;

	/**
	 * Heading (direction) of the position in degrees
	 */
	private Double heading;

	/**
	 * Latitude of this position in degrees. This is a mandatory field.
	 */
	private Double latitude;

	/**
	 * Longitude of this position in degrees. This is a mandatory field.
	 */
	private Double longitude;

	/**
	 * Dilution of the precision (DOP) of the current GPS fix.
	 */
	private Double precision;

	/**
	 * Number of satellites seen by the systems
	 */
	private Integer satellites;

	/**
	 * Speed for this position in meter/sec.
	 */
	private Double speed;

	/**
	 * Status of GPS system: 1 = no GPS response, 2 = error in response, 4 =
	 * valid.
	 */
	private Integer status;

	/**
	 * Timestamp extracted from the GPS system
	 */
	private Date timestamp;

	public KuraPosition() {
	}

	public Double getAltitude() {
		return this.altitude;
	}

	public Double getHeading() {
		return this.heading;
	}

	public Double getLatitude() {
		return this.latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}

	public Double getPrecision() {
		return this.precision;
	}

	public Integer getSatellites() {
		return this.satellites;
	}

	public Double getSpeed() {
		return this.speed;
	}

	public Integer getStatus() {
		return this.status;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setAltitude(final double altitude) {
		this.altitude = altitude;
	}

	public void setHeading(final double heading) {
		this.heading = heading;
	}

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	public void setPrecision(final double precision) {
		this.precision = precision;
	}

	public void setSatellites(final int satellites) {
		this.satellites = satellites;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public void setStatus(final int status) {
		this.status = status;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}
}