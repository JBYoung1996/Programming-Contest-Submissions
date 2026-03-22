import java.util.Scanner;

/**
 * The OutlierDetector class provides a utility to identify specific numerical 
 * outliers within a user-provided dataset based on average values and 
 * neighboring data points.
 * * @author Jordon Youngblood
 * @version 2025-2026
 */
public class OutlierDetector {
    /**
     * The main entry point of the application. It handles user input, 
     * calculates the dataset average, and iterates through the data to 
     * identify and print indices of outliers.
     * * @param args Command line arguments (not used).
     * CALLER: JVM (Java Virtual Machine)
     * CALLEE: java.util.Scanner.nextInt(), java.io.PrintStream.println()
     */
    public static void main(String[] args) {
        // Scanner object to read input from the standard input stream
        Scanner sc = new Scanner(System.in);
        // Accumulator variable to store the total sum of input numbers
        int sum = 0;
        // Flag to track if at least one outlier has been identified
        boolean foundAny = false;

        System.out.println("How many numbers are we checking outliers for?: ");
        // Stores the total number of elements to be processed
        int totalNumbers = sc.nextInt();

        // Array to store the collection of integers provided by the user
        int[] dataPoints = new int[totalNumbers];
        System.out.println("Enter the numbers we are checking for outliers: ");

        /*
         * Block: Data Collection
         * Populates the dataPoints array with user-entered integers.
         */
        for (int i = 0; i < dataPoints.length; i++) {
            dataPoints[i] = sc.nextInt();

        }

        /*
         * Block: Summation
         * Iterates through the array to calculate the total sum for averaging.
         */
        for (int i = 0; i < dataPoints.length; i++) {
            sum = sum + dataPoints[i];
        }

        // Calculated arithmetic mean of the dataset converted to double for precision
        double average = (double) sum / totalNumbers;

        /*
         * Block: Outlier Detection Logic
         * Checks each element against its neighbors and the dataset average 
         * to determine if it qualifies as an outlier.
         */
        for (int i = 0; i < dataPoints.length; i++) {
            // Case: Logic for the first element in the array
            if (i == 0) {
                if (dataPoints[i] > average && dataPoints[i] > dataPoints[i + 1]) {
                    System.out.println("Outlier found: " + i);
                    foundAny = true;
                }
            } 
            // Case: Logic for the last element in the array
            else if (i == totalNumbers - 1) {
                if (dataPoints[i] > average && dataPoints[i] > dataPoints[i - 1]) {
                    System.out.println("Outlier found: " + i);
                    foundAny = true;
                }

            } 
            // Case: Logic for middle elements, comparing against the sum of neighbors
            else if (dataPoints[i] > average && dataPoints[i] > (dataPoints[i - 1] + dataPoints[i + 1])) {
                System.out.println("Outlier found: " + i);
                foundAny = true;
            }

        }

        /*
         * Block: Final Reporting
         * Outputs a message if no outliers were identified during the loop.
         */
        if (!foundAny) {
            System.out.println("No outliers found");
        }
    }
}