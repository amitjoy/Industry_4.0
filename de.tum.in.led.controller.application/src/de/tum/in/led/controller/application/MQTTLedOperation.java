package de.tum.in.led.controller.application;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

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
	 * MQTT Client Reference
	 */
	private MQTTClient mqttClient;

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
					return;
				}
				if ("off".equalsIgnoreCase(message)) {
					LEDController.off();
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
