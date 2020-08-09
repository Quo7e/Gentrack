import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Testing.java
 *
 * Automated testing class, contains a single test as only one test file was provided and the program only does one
 * thing.
 * This class can be run instead of UserInterface when testing is required.
 */
public class Testing {
    /**
     * Testing()
     *
     * Constructor method, tests XmlToCsv functionality.
     * -  Creates an instance of XmlToCsv using the provided test file, located in "./test/".
     * -  Builds CSVs to the same directory using XmlToCsv::buildCsv - fails if the number does not match expected.
     * -  Compares the output CSVs to the precomputed CSVs provided, located in "./expected/" - fails if there is any
     *    mismatch between two identically named files, or when trying to compare files without identical names.
     * FileFilter::accept is overridden for both File Arrays by a lambda function defined within this method.
     */
    private Testing() {
        XmlToCsv testXml; // Instance of XmlToCsv for testing
        int csvCount = 0; // Tracks number of CSV files built
        Scanner scanner1, scanner2; // Used to read each CSV file line by line
        FileFilter csvFilter; // Defined in lambda below, filters out non-CSV files
        File[] expectedCsv, testCsv; // Stores the CSV files in each folder
        String line1, line2; // Stores one line of each file currently being compared

        System.out.println("Beginning automated test using ./test/testfile.xml..." + System.lineSeparator());

        try {
            testXml = new XmlToCsv(new File("./test/testfile.xml"));

            while (testXml.buildCsv()) {
                csvCount++;
            }
        } catch (Exception ex) {
            System.out.println("Caught exception: " + ex.getMessage());
            System.exit(1);
        }

        System.out.println("Built " + csvCount + " CSV files." + System.lineSeparator());

        if (csvCount != 2) {
            System.out.println("FAILED - Should have built 2 CSV files from testfile.xml.");
            System.exit(1);
        }

        System.out.println("Comparing files against expected output..." + System.lineSeparator());

        csvFilter = file -> {
            /* Lambda function, overrides accept in FileFilter for csvFilter.
             * Ensures only files with the .csv extension are added to the File arrays.*/
            String path = file.getAbsolutePath();
            String extension = "";

            if ((path.contains(".")) && (path.lastIndexOf(".") != 0)) {
                extension = path.substring(path.lastIndexOf(".") + 1);
            }

            return extension.equalsIgnoreCase("csv");
        };

        expectedCsv = new File("./expected/").listFiles(csvFilter);
        testCsv = new File("./test/").listFiles(csvFilter);

        // The File Arrays must be sorted to ensure testing only compares identically named files where possible.
        if ((expectedCsv != null) && (expectedCsv.length != 0)) {
            Arrays.sort(expectedCsv);
        } else {
            System.out.println("ERROR - Could not find prebuilt CSVs!");
            System.exit(2);
        }
        if ((testCsv != null) && (testCsv.length != 0)) {
            Arrays.sort(testCsv);
        } else {
            System.out.println("ERROR - Could not find CSVs built during this test!");
            System.exit(2);
        }

        for (int i = 0; i < csvCount; ++i) {
            if (!expectedCsv[i].getName().equals(testCsv[i].getName())) {
                System.out.println("FAILED - trying to compare two files with different names.");
                System.exit(3);
            }

            try {
                scanner1 = new Scanner(expectedCsv[i]);
                scanner2 = new Scanner(testCsv[i]);

                line1 = scanner1.nextLine();
                line2 = scanner2.nextLine();

                while (scanner1.hasNext() && scanner2.hasNext()) {
                    if (!line1.equals(line2)) {
                        System.out.println("FAILED - files '" + expectedCsv[i].getAbsolutePath() + "' and '" +
                            testCsv[i].getAbsolutePath() + "' are not identical in content.");
                        scanner1.close();
                        scanner2.close();
                        System.exit(5);
                    }

                    line1 = scanner1.nextLine();
                    line2 = scanner2.nextLine();
                }

                if (scanner1.hasNext() || scanner2.hasNext()) {
                    System.out.println("FAILED - files '" + expectedCsv[i].getAbsolutePath() + "' and '" +
                    testCsv[i].getAbsolutePath() + "' are not identical in length.");
                    scanner1.close();
                    scanner2.close();
                    System.exit(6);
                }

                scanner1.close();
                scanner2.close();
            } catch (IOException ex) {
                System.out.println("ERROR - Could not open CSV files!");
                System.exit(4);
            }
        }

        System.out.println("SUCCESS - test passed.");
    }

    /**
     * main()
     *
     * Main function, just creates an instance of Testing.
     *
     * @param args      Command line arguments (none accepted for this program)
     */
    public static void main(String[] args) {
        new Testing();
    }
}
