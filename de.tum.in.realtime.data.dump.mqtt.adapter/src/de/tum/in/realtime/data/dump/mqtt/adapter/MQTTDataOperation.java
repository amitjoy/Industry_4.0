package de.tum.in.realtime.data.dump.mqtt.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import de.tum.in.realtime.data.operation.api.DataOperation;
import de.tum.in.realtime.data.operation.model.RealtimeData;

/**
 * MQTT Service Component
 */
@Component(name = "de.tum.in.realtime.data.dump.mqtt")
public final class MQTTDataOperation {

	/**
	 * MQTT Client ID
	 */
	private static final String CLIENT_ID = "DWH-Industry";

	/**
	 * Data Subscription Channel
	 */
	private static final String DATA_DUMP_CHANNEL = "$EDC/tum/splunk/data/dump";

	/**
	 * MQTT Server Password
	 */
	private static final String MQTT_PASSWORD = "iotiwbiot";

	/**
	 * MQTT Server Port
	 */
	private static final String MQTT_PORT = "1883";

	/**
	 * MQTT Server
	 */
	private static final String MQTT_SERVER = "iot.eclipse.org";

	/**
	 * MQTT Server Username
	 */
	private static final String MQTT_USERNAME = "user@email.com";

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
		this.mqttClient = new MQTTClient(MQTT_SERVER, MQTT_PORT, null, null, CLIENT_ID);
		this.mqttClient.subscribe(DATA_DUMP_CHANNEL, message -> {
			final List<String> list = Arrays.stream(message.split(",")).map(msg -> Arrays.asList(msg.split("=")))
					.flatMap(lst -> lst.stream().map(a -> a.replaceAll("\0", ""))).collect(Collectors.toList());

			this.dataOperation.save(this.wrap(list));
		});
	}

	/**
	 * Deactivation Callback
	 */
	@Deactivate
	public void deactivate() {
		this.mqttClient.unsubscribe(DATA_DUMP_CHANNEL);
	}

	/**
	 * Wraps to {@link RealtimeData}
	 */
	private RealtimeData wrap(final List<String> list) {
		final RealtimeData data = new RealtimeData();
		data.force_x = list.get(1);
		data.force_y = list.get(3);
		data.force_z = list.get(5);
		data.torque_x = list.get(7);
		data.torque_y = list.get(9);
		data.torque_z = list.get(11);
		data.time = list.get(13);
		data.type = list.get(15);
		return data;
	}

}
