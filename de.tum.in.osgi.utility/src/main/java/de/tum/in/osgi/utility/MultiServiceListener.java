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

package de.tum.in.osgi.utility;

/**
 * <p>
 * Title: MultiServiceListener
 * </p>
 * <p>
 * Description: Listener for {@link MultiServiceTracker}s
 * </p>
 *
 * @author AMIT KUMAR MONDAL
 *
 * @param <T>
 *            The Service type to listen
 */
public interface MultiServiceListener<T> {

	/**
	 * Called when a service has been added
	 *
	 * @param service
	 *            the service instance
	 */
	public void serviceAdded(T service);

	/**
	 * Called when a service has been removed
	 *
	 * @param service
	 *            the service instance
	 */
	public void serviceRemoved(T service);

}
