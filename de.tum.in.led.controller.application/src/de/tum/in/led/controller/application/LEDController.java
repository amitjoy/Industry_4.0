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
package de.tum.in.led.controller.application;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * This is used to operate on LED
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class LEDController {

	/**
	 * GPIO Controller
	 */
	private static final GpioController gpio;

	/**
	 * GPIO Pin
	 */
	private static GpioPinDigitalOutput pin;

	static {
		gpio = GpioFactory.getInstance();
	}

	/**
	 * Switches on the LED
	 */
	public static void off() throws InterruptedException {
		// set shutdown state for this pin
		pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.LOW);

		pin.low();
		release();
	}

	/**
	 * Switches on the LED
	 */
	public static void on() throws InterruptedException {
		// set shutdown state for this pin
		pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);

		pin.high();
		release();
	}

	/**
	 * stop all GPIO activity/threads by shutting down the GPIO controller (this
	 * method will forcefully shutdown all GPIO monitoring threads and scheduled
	 * tasks)
	 */
	private static void release() {
		gpio.shutdown();
		gpio.unprovisionPin(pin);
	}

}
