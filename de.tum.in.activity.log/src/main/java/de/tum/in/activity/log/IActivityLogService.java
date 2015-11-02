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
package de.tum.in.activity.log;

/**
 * Used to save and retrieve activity logs
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public interface IActivityLogService {

	/**
	 * Represents Log File Types
	 *
	 */
	public enum LogFileType {
		KURA, TUM
	}

	/**
	 * The location of the swissbit application specific log file
	 */
	public static String LOCATION_KURA_LOG = "/var/log/kura.log";

	/**
	 * The location of the kura specific log file
	 */
	public static String LOCATION_TUM_LOG = "/home/pi/TUM/tum.log";

	/**
	 * Used to retrieve saved activity logs
	 *
	 *  @param type type of log to retrieve
	 *
	 * @return the list of logs
	 */
	public String retrieveLogs(LogFileType type);

	/**
	 * Used to save log to the database
	 *
	 * @param log
	 *            the log to be saved
	 */
	public void saveLog(String log);

}
