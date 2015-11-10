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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.collect.Lists;

import de.tum.in.realtime.data.operation.api.DataOperation;
import de.tum.in.realtime.data.operation.model.RealtimeData;
import osgi.enroute.configurer.api.RequireConfigurerExtender;
import osgi.enroute.google.angular.capabilities.RequireAngularWebResource;
import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;
import osgi.enroute.twitter.bootstrap.capabilities.RequireBootstrapWebResource;
import osgi.enroute.webserver.capabilities.RequireWebServerExtender;

/**
 * Realtime Data UI Application
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
	 * REST Service to retrieve the saved real time industrial data
	 */
	public Collection<? extends RealtimeData> getData(final RESTRequest request, final String type) {
		final List<RealtimeData> datas = Lists.newArrayList();

		if ("bluetooth".equals(type)) {
			System.out.println("INISDE");
			final RealtimeData data1 = new RealtimeData();
			data1.force_x = "HELLO1";
			data1.force_y = "HELLO1";
			data1.force_z = "HELLO1";
			data1.torque_x = "HELLO1";
			data1.torque_y = "HELLO1";
			data1.torque_z = "HELLO1";
			data1.time = LocalDateTime.now().toString();
			datas.add(data1);
		}

		if ("wifi".equals(type)) {
			System.out.println("INSIDE");
			final RealtimeData data1 = new RealtimeData();
			data1.force_x = "HELLO1";
			data1.force_y = "HELLO1";
			data1.force_z = "HELLO1";
			data1.torque_x = "HELLO1";
			data1.torque_y = "HELLO1";
			data1.torque_z = "HELLO1";
			final RealtimeData data2 = new RealtimeData();
			data2.force_x = "HELLO1";
			data2.force_y = "HELLO1";
			data2.force_z = "HELLO1";
			data2.torque_x = "HELLO1";
			data2.torque_y = "HELLO1";
			data2.torque_z = "HELLO1";
			datas.add(data1);
			datas.add(data2);
		}
		return datas;
	}

}
