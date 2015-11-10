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
package de.tum.in.realtime.data.operation.api;

import java.util.List;

import de.tum.in.realtime.data.operation.model.RealtimeData;

/**
 * Data Dump Operation
 *
 * @author AMIT KUMAR MONDAL
 */
public interface DataOperation {

	/**
	 * Retrieves all saved {@link RealtimeData} for the type specified
	 */
	public List<RealtimeData> retrieveAll(final String type);

	/**
	 * Saves the provided {@link RealtimeData} to the database
	 */
	public boolean save(final RealtimeData realtimeData);

}
