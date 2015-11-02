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
 * Title: SingleServiceListener
 * </p>
 * <p>
 * Description: Listens on events of a single service tracker
 * </p>
 *
 * @author AMIT KUMAR MONDAL
 *
 * @param <T>
 *            Service Type
 */
public interface SingleServiceListener<T> {

	/**
	 * Called after the service changed
	 *
	 * @param service
	 *            the current service (may be null)
	 */
	public abstract void afterServiceChange(T service);

	/**
	 * Called before a service is becomes invalid
	 *
	 * @param service
	 *            the service about to be removed
	 */
	public abstract void beforeServiceRemove(T service);

}
