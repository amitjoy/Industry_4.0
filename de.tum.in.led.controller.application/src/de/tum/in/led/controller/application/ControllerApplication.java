package de.tum.in.led.controller.application;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import osgi.enroute.configurer.api.RequireConfigurerExtender;
import osgi.enroute.eventadminserversentevents.capabilities.RequireEventAdminServerSentEventsWebResource;
import osgi.enroute.google.angular.capabilities.RequireAngularWebResource;
import osgi.enroute.rest.api.REST;
import osgi.enroute.twitter.bootstrap.capabilities.RequireBootstrapWebResource;
import osgi.enroute.webserver.capabilities.RequireWebServerExtender;

@RequireAngularWebResource(resource = { "angular.js", "angular-resource.js", "angular-route.js" }, priority = 1000)
@RequireBootstrapWebResource(resource = "css/bootstrap.css")
@RequireEventAdminServerSentEventsWebResource
@RequireWebServerExtender
@RequireConfigurerExtender
@Component(name = "de.tum.in.led.controller")
public class ControllerApplication implements REST {

	/**
	 * Event Admin Reference
	 */
	@Reference
	private EventAdmin eventAdmin;

	public void putTopic(final Map<String, Object> properties) {
		final String topic = "led/on";
		final Event event = new Event(topic, properties);
		this.eventAdmin.postEvent(event);
	}

}
