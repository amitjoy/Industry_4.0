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
package de.tum.in.realtime.data.dump.application;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import de.tum.in.realtime.data.operation.api.DataOperation;
import de.tum.in.realtime.data.operation.model.RealtimeData;
import osgi.enroute.configurer.api.RequireConfigurerExtender;
import osgi.enroute.google.angular.capabilities.RequireAngularWebResource;
import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;
import osgi.enroute.twitter.bootstrap.capabilities.RequireBootstrapWebResource;
import osgi.enroute.webserver.capabilities.RequireWebServerExtender;

/**
 * Real time Data UI Application
 *
 * @author AMIT KUMAR MONDAL
 */
@RequireAngularWebResource(resource = { "angular.js", "angular-resource.js", "angular-route.js" }, priority = 1000)
@RequireBootstrapWebResource(resource = "css/bootstrap.css")
@RequireWebServerExtender
@RequireConfigurerExtender
@Component(name = "de.tum.in.realtime.data.dump")
public final class DumpApplication implements REST {

	/**
	 * MQTT Data Operation Reference
	 */
	@Reference
	private DataOperation dataOperation;

	/**
	 * REST Service to retrieve the saved real time industrial data for the type
	 * specified
	 */
	public Collection<? extends RealtimeData> getData(final RESTRequest request, final String type) {
		return this.dataOperation.retrieveAll(type);
	}

}
