public class AddTwoNumbers {
    public static void main(String[] args) {
        // Check that two arguments were provided
        if (args.length != 2) {
            System.err.println("Error: please provide exactly two integers as arguments.");
            System.exit(1);
        }
        
        // Parse the integers from the arguments
        int num1 = Integer.parseInt(args[0]);
        int num2 = Integer.parseInt(args[1]);
        
        // Calculate the sum
        int sum = num1 + num2;
        
        // Print the result
        System.out.println("The sum of " + num1 + " and " + num2 + " is " + sum + ".");
    }
}
