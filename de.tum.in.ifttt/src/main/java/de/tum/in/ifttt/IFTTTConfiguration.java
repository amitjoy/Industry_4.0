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
package de.tum.in.ifttt;

/**
 * Used to send a tagged email to IFTTT
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public interface IFTTTConfiguration {

	/**
	 * The Trigger Email for IFTTT
	 */
	public static final String TRIGGER_EMAIL = "trigger@recipe.ifttt.com";

	/**
	 * Used to send an email to IFTTT trigger
	 */
	public void trigger();

}
