import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import swiftbot.SwiftBotAPI;
import swiftbot.SwiftBotAPI.ImageSize;
import swiftbot.SwiftBotAPI.Underlight;

public class SwiftbotDrawingProgram {
    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 85;
    private static final int MAX_TRIES = 3;
    private static final double MAX_ANGLE_DIFFERENCE = 5.0;
    private static final double EPSILON = 0.00001;
    private static final double WHEEL_RADIUS = 2.5;
    private static final double WHEEL_DISTANCE = 15.0;
    private static final double LOW_SPEED = 50; 
    private static final double HIGH_SPEED = 100; 
    private static final double SPEED_FACTOR = 0.34; // Proportionality factor that converts the speed of the wheels to rotations per minute (rpm)

    private static final ArrayList<String> DRAWN_SHAPES = new ArrayList<>();
    private static int squareCount = 0;
    private static int triangleCount = 0;
    private static int totalTime = 0;
    private static int largestSize = 0;
    private static String largestShape = "";
    private static int mostFrequentCount = 0;
    private static String mostFrequentShape = "";

    private static Scanner scanner = new Scanner(System.in);
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

        boolean quit = false;
        while (!quit) {
            System.out.println("Enter S to draw a square, T to draw a triangle, or Q to quit:");
            String input = scanner.nextLine().trim().toUpperCase();
            switch (input) {
                case "S":
                    drawSquare();
                    break;
                case "T":
                    drawTriangle();
                    break;
                case "Q":
                    quit = true;
                    break;
                default:
                    System.out.println("Invalid input, please try again.");
            }
        }
        try {
            writeLog();
        } catch(IOException e) {
            System.out.println("Failed to write log");
        }
        
        scanner.close();
    }

    private static void drawSquare() {

        double sideLength = getSquareSideLength("Enter the length of the square's side:");

        System.out.println("Drawing square with side length " + sideLength + " cm...");
        
        // Calculate the distance the Swiftbot needs to travel for each side of the square
        double distance = sideLength * 4; // Math.PI * WHEEL_RADIUS / 180.0;
        
        // Calculate the time the Swiftbot needs to move at low speed to cover the distance
        double time = calculateTime(distance , LOW_SPEED);

        // Increment the time
        totalTime += time;
        
        // Move the wheels at low speed for the calculated time to draw the square
        // Swiftbot.moveWheels(LOW_SPEED, LOW_SPEED, (int) (time * 1000));
        moveWheelsFwd((int)time, (int)LOW_SPEED);
        rotateSwitftbot(90, (int)LOW_SPEED);
        moveWheelsFwd((int)time, (int)LOW_SPEED);
        rotateSwitftbot(90, (int)LOW_SPEED);
        moveWheelsFwd((int)time, (int)LOW_SPEED);
        rotateSwitftbot(90, (int)LOW_SPEED);
        moveWheelsFwd((int)time, (int)LOW_SPEED);
        System.out.println("Stopping wheels and turning underlights to green...");
        turnOnGreenUnderLights();
        
        // Update the DRAWN_SHAPES, largestSize, and largestShape variables
        DRAWN_SHAPES.add("Square: " + sideLength);
        if (sideLength > largestSize) {
            largestSize = (int) sideLength;
            largestShape = "Square";
        }
    }

    private static void drawTriangle() {
        // Check whether the sides can form a triangle
        double side1 = 0.0, side2 = 0.0, side3 = 0.0;
        boolean validTriangle = true;
        int tries = 1;
        while (tries < MAX_TRIES) {
            System.out.println("The entered side lengths cannot form a triangle. Please try again.");
            side1 = getTriangleSideLength("Enter the length of the first side:");
            side2 = getTriangleSideLength("Enter the length of the second side:");
            side3 = getTriangleSideLength("Enter the length of the third side:");
            validTriangle = canFormTriangle(side1, side2, side3);
            tries++;
        }
        if (!validTriangle) {
            System.out.println("Sorry, you have exceeded the maximum number of tries. Cannot draw triangle.");
            return;
        }
    
        // Calculate the angles of the triangle
        double angle1 = calculateTriangleAngle(side1, side2, side3);
        double angle2 = calculateTriangleAngle(side2, side3, side1);
        double angle3 = calculateTriangleAngle(side3, side1, side2);
    
        // Update DRAWN_SHAPES, largestSize, and largestShape variables
        String triangleString = String.format("Triangle: %.0f, %.0f, %.0f (angles: %.2f, %.2f, %.2f)",
                side1, side2, side3, angle1, angle2, angle3);
        DRAWN_SHAPES.add(triangleString);
        triangleCount++;
        int triangleSize = (int) (side1 + side2 + side3);
        if (triangleSize > largestSize) {
            largestSize = triangleSize;
            largestShape = triangleString;
        }
    
        // Calculate the time required to move the distance
        double distance = calculateTrianglePerimeter(side1, side2, side3);
        double time = calculateTime(distance , LOW_SPEED); // calculateTime(distance, LOW_SPEED);
       
        // Increment the total time
        totalTime += time;
        // Move the wheels to draw the triangle
        // Placeholder values for the Swiftbot commands
        System.out.printf("Drawing triangle with sides %.0f, %.0f, %.0f for %.2f seconds.%n", side1, side2, side3, time);
        // System.out.println("Moving wheels forward at low speed...");
        // System.out.println("Turning wheels to make a left turn...");
        // System.out.println("Moving wheels forward at low speed...");
        // System.out.println("Turning wheels to make a right turn...");
        // System.out.println("Moving wheels forward at low speed...");
        // System.out.println("Turning wheels to make a left turn...");
        // System.out.println("Moving wheels forward at low speed...");
        moveWheelsFwd((int)calculateTime(side1 , LOW_SPEED) * 1000, (int)LOW_SPEED);
        rotateSwitftbot(angle3, (int)LOW_SPEED);
        moveWheelsFwd((int)calculateTime(side2 , LOW_SPEED) * 1000, (int)LOW_SPEED);
        rotateSwitftbot(angle1, (int)LOW_SPEED);
        moveWheelsFwd((int)calculateTime(side3 , LOW_SPEED) * 1000, (int)LOW_SPEED);
        System.out.println("Stopping wheels and turning underlights to green...");
        turnOnGreenUnderLights();

        System.out.println("Triangle drawn.");
        System.out.println();
    }
    
    private static double getSquareSideLength(String prompt) {
        double sideLength = 0;
        boolean isValid = false;
    
        while (!isValid) {
            System.out.println(prompt);
            String input = scanner.nextLine();
    
            try {
                sideLength = Double.parseDouble(input);
                if (sideLength >= MIN_LENGTH && sideLength <= MAX_LENGTH) {
                    isValid = true;
                } else {
                    System.out.println("Invalid side length. Please enter a value between " + MIN_LENGTH + " and " + MAX_LENGTH);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    
        return sideLength;
    }
    
    private static double getTriangleSideLength(String prompt) {
        double length = 0.0;
        boolean validInput = false;
        int tries = 0;
    
        while (!validInput && tries < MAX_TRIES) {
            System.out.print(prompt);
            String input = scanner.nextLine();
    
            try {
                length = Double.parseDouble(input);
                if (length < MIN_LENGTH || length > MAX_LENGTH) {
                    System.out.println("Length must be between " + MIN_LENGTH + " and " + MAX_LENGTH + ".");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
    
            tries++;
        }
    
        if (!validInput) {
            System.out.println("Too many invalid input attempts. Exiting program.");
            System.exit(1);
        }
    
        return length;
    }

    private static double calculateTriangleAngle(double a, double b, double c) {
        // Use the law of cosines to calculate the angle opposite to the first side (a)
        double cosA = (b * b + c * c - a * a) / (2 * b * c);
        double radians = Math.acos(cosA);
        double degrees = Math.toDegrees(radians);
        return degrees;
    }
    
    private static boolean canFormTriangle(double a, double b, double c) {
        if (a <= 0 || b <= 0 || c <= 0) {
            // All sides must be positive
            return false;
        }
        if (a + b <= c || a + c <= b || b + c <= a) {
            // The sum of any two sides must be greater than the third side
            return false;
        }
        return true;
    }
    
    private static void updateMostFrequentShape() {
        int squareOccurrences = Collections.frequency(DRAWN_SHAPES, "Square");
        int triangleOccurrences = Collections.frequency(DRAWN_SHAPES, "Triangle");
    
        if (squareOccurrences > triangleOccurrences) {
            mostFrequentShape = "Square";
            mostFrequentCount = squareOccurrences;
        } else if (triangleOccurrences > squareOccurrences) {
            mostFrequentShape = "Triangle";
            mostFrequentCount = triangleOccurrences;
        } else {
            mostFrequentShape = "Equal number of Squares and Triangles";
            mostFrequentCount = squareOccurrences;
        }
    }
    
    private static void writeLog() throws IOException {
        File file = new File("drawing.txt");
        FileWriter writer = new FileWriter(file);
    
        // Write names and sizes of all drawn shapes
        for (String shape : DRAWN_SHAPES) {
            writer.write(shape + "\n");
        }
    
        // Write largest shape and its size
        writer.write("Largest shape: " + largestShape + " (" + largestSize + ")\n");
    
        // Write most frequent shape and how many times it was drawn
        String frequentShape = "";
        int frequentCount = 0;
        if (squareCount > triangleCount) {
            frequentShape = "Square";
            frequentCount = squareCount;
        } else if (triangleCount > squareCount) {
            frequentShape = "Triangle";
            frequentCount = triangleCount;
        } else {
            frequentShape = "None";
        }
        writer.write("Most frequent shape: " + frequentShape + " (" + frequentCount + ")\n");
    
        // Write average time it took to draw shapes
        double avgTime = (double) totalTime / DRAWN_SHAPES.size();
        writer.write("Average time to draw shapes: " + avgTime + " seconds");
    
        writer.close();
    }
    
    private static double calculateTime(double distance, double speed) {
        double speedRpm = speed * SPEED_FACTOR;
        double circumference = 2 * Math.PI * WHEEL_RADIUS;
        double rotations = distance / circumference;
        double time = rotations * 60 / speedRpm; // time in seconds
        return time;
    }
    
    private static void turnOnGreenUnderLights() {
		try {

			// turns all green lights on with max brightness
			swiftBot.fillUnderlights(0, 255, 0, false);
			swiftBot.updateUnderlights();
			Thread.sleep(2000);

			// turns off all lights
			swiftBot.disableUnderlights();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    
    private static double calculateTrianglePerimeter(double side1, double side2, double side3) {
        return side1 + side2 + side3;
    }
    
    private static void moveWheelsFwd(int time, int speed) {
        int leftVelocity = speed;
        int rightVelocity = speed;
        try {
            swiftBot.move(leftVelocity, rightVelocity, time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void rotateSwitftbot(double angle, int speed) {
        // Conver angle from degrees to radians
        double theta = angle * 180 / (2 * Math.PI);

        // Calcualte the arc length
        double arcLength = WHEEL_RADIUS * theta / 2; 

        // Calculate the time in milliseconds required to move "arcLength" distance
        int time = (int)calculateTime(arcLength, speed) * 1000;
        
        if(angle > 0) {
            int leftVelocity = -speed;
            int rightVelocity = speed;
            try {
                swiftBot.move(leftVelocity, rightVelocity, time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            int leftVelocity = speed;
            int rightVelocity = -speed;
            try {
                swiftBot.move(leftVelocity, rightVelocity, time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}