import java.util.Scanner;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.util.Console;

import swiftbot.SwiftBotAPI;
import swiftbot.SwiftBotAPI.ImageSize;
import swiftbot.SwiftBotAPI.Underlight;

public class DoesMySwiftBotWork {
	static SwiftBotAPI swiftBot;

	public static void main(String[] args) {
		try {
			swiftBot = new SwiftBotAPI();
		} catch (Exception e) {
			/*
			 * Outputs a warning if I2C is disabled. This only needs to be turned on once,
			 * so you won't need to worry about this problem again!
			 */
			System.out.println("\nI2C disabled!");
			System.out.println("Run the following command:");
			System.out.println("sudo raspi-config nonint do_i2c 0\n");
			System.exit(0);
		}

		// creates menu for user interaction
		final Console console = new Console();
		console.title("DOES MY SWIFTBOT WORK????");
		Scanner reader = new Scanner(System.in); // Reading from System.in

		// loops main menu after each user action
		while (true) {
			// outputs user options
			console.println("Enter a number to test a feature:\n\n" + "1 = Test Camera          |	"
					+ "2 = Test Button Lights\n" + "3 = Test Ultrasound      |	" + "4 = Test Left Wheel\n"
					+ "5 = Test Right Wheel     |	" + "6 = Test Buttons\n" + "7 = Test Underlighting	 |	"
					+ "0 = Exit");
			System.out.println("\nEnter a number: ");

			// reads user input and performs corresponding action
			String ansRaw = reader.next();
			try {
				// attempts to convert input to integer
				int ans = Integer.parseInt(ansRaw);
				switch (ans) {
				// CAMERA
				case 1:
					System.out.println("TESTING CAMERA " + ans);
					// writes to file directory
					String filePath = ".";
					String fileName = "DoesMyCameraWork.png";
					try {
						/*
						 * takes a 720x720 image as a test, however smaller image sizes are recommended
						 * to avoid code running too slowly.
						 */
						swiftBot.takeStill(filePath, fileName, ImageSize.SQUARE_720x720, true);
					} catch (NullPointerException e) {
						/*
						 * Outputs a warning if Camera is disabled. These commands only need to be
						 * entered once, so you won't need to worry about this problem again!
						 */
						System.out.println("\nCamera not enabled!");
						System.out.println("Try running the following command: ");
						System.out.println("sudo raspi-config nonint do_camera 0\n");
						System.out.println("Then reboot using the following command: ");
						System.out.println("sudo reboot\n");
						System.exit(0);
					}
					break;

				// BUTTON LIGHTS
				case 2:
					System.out.println("TESTING BUTTON LIGHTS " + ans);
					testButtonLights();
					break;

				// ULTRASOUND
				case 3:
					System.out.println("TESTING ULTRASOUND " + ans);
					// gets distance to nearest obstacle
					double distance = swiftBot.useUltrasound();
					// pretty prints the distance
					System.out.println("Distance from obstacle: " + distance + "cm");
					break;

				// LEFT WHEEL
				case 4:
					System.out.println("TESTING LEFT WHEEL " + ans);
					testWheel("left");
					break;

				// RIGHT WHEEL
				case 5:
					System.out.println("TESTING RIGHT WHEEL " + ans);
					testWheel("right");
					break;

				// BUTTONS
				case 6:
					System.out.println("TESTING BUTTONS " + ans);
					testButtons();
					break;

				// UNDERLIGHTS
				case 7:
					System.out.println("TESTING UNDERLIGHTS " + ans);
					testUnderlighting();
					break;

				// EXIT
				case 0:
					swiftBot.shutdown();
					reader.close();
					System.exit(0);
					break;

				// ERROR HANDLING
				default:
					throw new Exception();
				}
			} catch (Exception e) {
				System.out.println("\nInvalid input, please enter a number 0-7\n");
			}
		}
	}

	/*
	 * Tests all lights on top of the SwiftBot. They are all turned on using their
	 * individual light commands. They are then kept on for 2 seconds using {@code
	 * Thread.sleep()}. Finally, they are all turned off using a for loop.
	 */
	public static void testButtonLights() {
		try {
			// turns all button lights on for 1 second, one-by-one

			// button light A
			System.out.println("Testing Button Light A");
			swiftBot.setButtonLightA(true);
			if (swiftBot.getButtonLightA()) {
				System.out.println("Button Light A: ON");
			}
			Thread.sleep(1000);
			swiftBot.setButtonLightA(false);

			// button light B
			System.out.println("Testing Button Light B");
			swiftBot.setButtonLightB(true);
			if (swiftBot.getButtonLightB()) {
				System.out.println("Button Light B: ON");
			}
			Thread.sleep(1000);
			swiftBot.toggleButtonLightB();

			// button light X
			System.out.println("Testing Button Light X");
			swiftBot.toggleButtonLightX();
			if (swiftBot.getButtonLightX()) {
				System.out.println("Button Light X: ON");
			}
			Thread.sleep(1000);
			swiftBot.setButtonLightX(false);

			// button light Y
			System.out.println("Testing Button Light Y");
			swiftBot.toggleButtonLightY();
			if (swiftBot.getButtonLightY()) {
				System.out.println("Button Light Y: ON");
			}
			Thread.sleep(1000);
			swiftBot.toggleButtonLightY();

			// toggles all lights for one second
			System.out.println("All Button Lights");
			swiftBot.fillButtonLights();
			Thread.sleep(1000);
			swiftBot.disableButtonLights();

		} catch (InterruptedException e) {
			// outputs any errors
			e.printStackTrace();
		}

	}

	/*
	 * Tests the user selected wheel. Sets the speed of the wheel the user selects
	 * to 100%. When testing backwards movement sets the speed of the user selected
	 * wheel to -100%. (Hover your mouse over swiftBot.move for more info!)
	 */
	public static void testWheel(String wheel) throws InterruptedException {
		int leftVelocity = 0;
		int rightVelocity = 0;
		if (wheel.equals("left")) {
			System.out.println("Testing the left wheel.");
			leftVelocity = 100;
		} else {
			System.out.println("Testing the right wheel.");
			rightVelocity = 100;
		}

		System.out.println("Forwards.");
		swiftBot.move(leftVelocity, rightVelocity, 2000);
		System.out.println("Backwards.");
		swiftBot.move(-leftVelocity, -rightVelocity, 2000);
		System.out.println("Finished testing.");
	}

	/*
	 * Tests if all buttons are correctly working. Adds event handlers to all
	 * buttons using a for loop which output when a button is pressed. Waits for 5
	 * seconds. Removes event handlers from all 4 buttons manually.
	 * 
	 * (After all event handlers are removed there's a code template for you to
	 * experiment with!)
	 */
	public static void testButtons() {

		// iterates through all buttons
		for (GpioPinDigitalInput button : swiftBot.BUTTONS) {

			// create and register gpio pin listener
			button.addListener(new GpioPinListenerDigital() {
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					// display pin state on console
					if (event.getState().isLow()) {
						System.out.println("	" + event.getPin() + " pressed");
					}
				}
			});

		}

		// button testing time
		System.out.println("All buttons are now active for 5 seconds");
		long endTime = System.currentTimeMillis() + 5_000;

		// do nothing while buttons are tested
		while (System.currentTimeMillis() < endTime) {

		}

		// remove event handlers from all buttons
		swiftBot.BUTTON_A.removeAllListeners();
		swiftBot.BUTTON_B.removeAllListeners();
		swiftBot.BUTTON_X.removeAllListeners();
		swiftBot.BUTTON_Y.removeAllListeners();

		System.out.println("Buttons are now inactive");

		// example action listener!
		swiftBot.BUTTON_A.addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (event.getState().isLow()) {
					// Your code goes here!
				}
			}
		});

		// removes example action listener
		swiftBot.BUTTON_A.removeAllListeners();
	}

	/*
	 * Tests out the SwiftBot's underlighting using a range of different parameters.
	 * Hover over each swiftBot method name to learn more!
	 */
	public static void testUnderlighting() {
		try {
			// turns all red lights on with max brightness
			swiftBot.fillUnderlights(255, 0, 0);
			Thread.sleep(1000);

			// turns all green lights on with max brightness
			swiftBot.fillUnderlights(0, 255, 0, false);
			swiftBot.updateUnderlights();
			Thread.sleep(1000);

			// turns all blue lights on with max brightness
			int[] blue = { 0, 0, 255 };
			swiftBot.fillUnderlights(blue);
			Thread.sleep(1000);

			// turns all lights to different white intensities
			swiftBot.setUnderlight(Underlight.FRONT_LEFT, 255, 255, 255);
			swiftBot.setUnderlight(Underlight.FRONT_RIGHT, 225, 225, 225, true);
			swiftBot.setUnderlight(Underlight.MIDDLE_LEFT, 195, 195, 195, false);
			int[] midRightLight = { 165, 165, 165 };
			swiftBot.setUnderlight(Underlight.MIDDLE_RIGHT, midRightLight);
			swiftBot.setUnderlight(Underlight.BACK_LEFT, 135, 135, 135);
			swiftBot.setUnderlight(Underlight.BACK_RIGHT, 105, 105, 105, false);
			swiftBot.updateUnderlights();

			// iterates through all lights and assigns them red
			// does not update the lights (yet!)
			int[] red = { 255, 0, 0 };
			for (Underlight underlight : swiftBot.UNDERLIGHTS) {
				swiftBot.setUnderlight(underlight, red, false);
			}

			// keeps the lights at their white intensities for 1 second
			Thread.sleep(1000);

			// updates the lights to their stored red values
			swiftBot.updateUnderlights();
			Thread.sleep(1000);

			// turns off all lights
			swiftBot.disableUnderlights();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}