package de.tum.in.realtime.data.dump.mqtt.adapter;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import de.tum.in.realtime.data.operation.api.DataOperation;

/**
 * MQTT Service Component
 */
@Component(name = "de.tum.in.realtime.data.dump.mqtt")
public final class MQTTDataOperation {

	/**
	 * Data Subscription Channel
	 */
	private static final String DATA_DUMP_CHANNEL = "myChannel";

	/**
	 * Data Dump Operation
	 */
	@Reference
	private volatile DataOperation dataOperation;

	/**
	 * MQTT Client Reference
	 */
	private MQTTClient mqttClient;

	/**
	 * Activation Callback
	 */
	@Activate
	public void activate() {
		this.mqttClient = new MQTTClient("iot.eclipse.org");
		this.mqttClient.subscribe(DATA_DUMP_CHANNEL, message -> this.dataOperation.save(null));
		System.out.println("MQTT Client" + this.mqttClient);
	}

	/**
	 * Deactivation Callback
	 */
	@Deactivate
	public void deactivate() {
		this.mqttClient = null;
		this.mqttClient.unsubscribe(DATA_DUMP_CHANNEL);
	}

}
