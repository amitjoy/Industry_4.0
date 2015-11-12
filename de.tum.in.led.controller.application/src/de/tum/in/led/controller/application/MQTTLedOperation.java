package de.tum.in.led.controller.application;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * MQTT Service Component
 */
@Component(name = "de.tum.in.led.mqtt.controller")
public final class MQTTLedOperation {

	/**
	 * Subscription Channel
	 */
	private static final String LED_CHANNEL = "tum/led";

	/**
	 * MQTT Server
	 */
	private static final String MQTT_SERVER = "iot.eclipse.org";

	/**
	 * LED Switch off Event Topic
	 */
	private static final String SWITCH_OFF_EVENT = "led/off";

	/**
	 * LED Switch on Event Topic
	 */
	private static final String SWITCH_ON_EVENT = "led/on";

	/**
	 * Event Admin Reference
	 */
	@Reference
	private volatile EventAdmin eventAdmin;

	/**
	 * MQTT Client Reference
	 */
	private MQTTClient mqttClient;

	/**
	 * Event Properties
	 */
	private final Dictionary<String, String> properties = new Hashtable<>();

	/**
	 * Activation Callback
	 */
	@Activate
	public void activate() {
		this.mqttClient = new MQTTClient(MQTT_SERVER);
		this.mqttClient.subscribe(LED_CHANNEL, message -> {
			try {
				if ("on".equalsIgnoreCase(message)) {
					LEDController.on();
					final Event event = new Event(SWITCH_ON_EVENT, this.properties);
					this.eventAdmin.postEvent(event);
					return;
				}
				if ("off".equalsIgnoreCase(message)) {
					LEDController.off();
					final Event event = new Event(SWITCH_OFF_EVENT, this.properties);
					this.eventAdmin.postEvent(event);
					return;
				}
			} catch (final Exception e) {
				e.printStackTrace(System.out);
			}
		});
	}

	/**
	 * Deactivation Callback
	 */
	@Deactivate
	public void deactivate() {
		this.mqttClient = null;
		this.mqttClient.unsubscribe(LED_CHANNEL);
	}

}
