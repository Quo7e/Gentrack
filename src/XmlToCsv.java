import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * XmlToCsv.java
 *
 * File parsing class. When created, an instance of this class will expect an XML file formatted as specified in the
 * provided guidelines.
 *
 * The constructor will read the XML as text and collect the data within "<CSVIntervalData>" so long as the other
 * specified XML elements are also present.
 *
 * This data can then be written to some number of CSV files by calling buildCsv() until false is returned, indicating
 * there is no more data to be read.
 */
public class XmlToCsv {
    private String header = "", trailer = "", content = ""; // Stores data to be saved in CSV files
    private String separator = System.getProperty("line.separator"); // Should be system independent
    private String path = ""; // File path for CSV files - same path as the XML file
    private int stage = 0; // Values 0 - 9 used to track progress through the XML file

    /**
     * XmlToCsv()
     *
     * Constructor method, used to open and read a provided XML file line by line.
     * -  Calls stageCheck() to confirm the specified XML elements are present and in order.
     * -  Data from "<CSVIntervalData>" is trimmed and stored within Strings header, trailer, and content.
     *
     * @param xmlFile       The provided XML file.
     * @throws Exception    Thrown when the file cannot be found or is incorrectly formatted.
     */
    XmlToCsv(File xmlFile) throws Exception {
        Scanner scanner; // Used to read the XML file line by line
        String input; // Stores one line of the file at a time
        StringBuilder builder = new StringBuilder(); // Used to build the String content

        try {
            scanner = new Scanner(xmlFile);
        } catch (FileNotFoundException ex) {
            throw new Exception("Could not find file '" + xmlFile.toString() + "'");
        }

        path = xmlFile.getAbsolutePath();
        path = path.substring(0,path.lastIndexOf(File.separator) + 1);

        while (scanner.hasNext()) {
            input = scanner.nextLine().trim();

            if (!stageCheck(input)) {
                throw new Exception("Unknown file format - line '" + input + "'");
            }

            if (input.matches("^\\d{3}.*")) {
                if (input.startsWith("100")) {
                    header = input;
                } else if (input.startsWith("900")) {
                    trailer = input;
                } else {
                    builder.append(input);
                    builder.append(separator);
                }
            }
        }

        content = builder.toString();
        scanner.close();
    }

    /**
     * stageCheck()
     *
     * Method used to confirm specified XML elements are present and in the provided order. Specifically:
     *  <Header>
     *  <Transactions>
     *  <Transaction transactionDate="somedata" transactionID="somedata">
     *  <MeterDataNotification>
     *  <CSVIntervalData>
     * are checked for, in that order.
     * Also confirms that CSV data is only present within the "<CSVIntervalData>" element.
     *
     * @param input     A single line from the XML file currently being processed.
     * @return          False if an element is detected out of order (or missing required attributes), otherwise true.
     */
    private boolean stageCheck(String input) {
        if (input.matches("<Header>")) {
            if (stage != 0) { return false; }
            stage++;
        }
        if (input.matches("</Header>")) {
            if (stage != 1) { return false; }
            stage++;
        }
        if (input.matches("<Transactions>")) {
            if (stage != 2) { return false; }
            stage++;
        }
        if (input.matches("<Transaction .*")) {
            if (stage != 3) { return false; }
            // This element has two required attributes. If these are missing, the file is invalid.
            if (!input.matches("<Transaction transactionDate=\".*\" transactionID=\".*\">")) { return false; }
            stage++;
        }
        if (input.matches("<MeterDataNotification.*")) {
            if (stage != 4) { return false; }
            stage++;
        }
        if (input.matches("<CSVIntervalData>")) {
            if (stage != 5) { return false; }
            stage++;
        }
        // Check for CSV data, which should only be present within "<CSVIntervalData>".
        if (input.matches("^\\d{3}.*")) {
            if (stage != 6) { return false; }
        }
        if (input.matches("</CSVIntervalData>")) {
            if (stage != 6) { return false; }
            stage++;
        }
        if (input.matches("</MeterDataNotification>")) {
            if (stage != 7) { return false; }
            stage++;
        }
        if (input.matches("</Transaction>")) {
            if (stage != 8) { return false; }
            stage++;
        }
        if (input.matches("</Transactions>")) {
            if (stage != 9) { return false; }
            stage++;
        }

        return true;
    }

    /**
     * buildCsv()
     *
     * Method used to build a single CSV file from data within the String content.
     * - Each file begins with the String header and ends with the String trailer.
     * - The first line contained within the String content must begin with "200", and is used as the second line of the
     *   CSV file. Its second field (CSV rules, separated by a "," character) is used as the name of the CSV file.
     * - Once a second "200" line is encountered (or content is empty), the CSV file is closed.
     * - If there are additional lines in String content after the CSV file is closed, this data is read back into
     *   content so that future calls to this method can generate more CSVs from it.
     *
     * @return              False if String content contains nothing upon method call, true otherwise.
     * @throws Exception    Thrown when the FileWriter cannot access the CSV file, or when String content is invalid.
     */
    public boolean buildCsv() throws Exception {
        if (content.isEmpty()) { return false; }

        Scanner scanner = new Scanner(content); // Used to read the String content line by line
        String csvContent, csvHeader, csvCustomer[]; // Stores data to be saved in this specific CSV

        csvHeader = scanner.nextLine();
        if (csvHeader.startsWith("200")) {
            csvCustomer = csvHeader.split(",");
        } else {
            throw new Exception("Invalid CSV content - line '" + csvHeader + "' must begin with 200.");
        }

        File csvFile = new File(path + csvCustomer[1] + ".csv"); // The new CSV file
        FileWriter writer = new FileWriter(csvFile); // Used to write to File csvFile

        StringBuilder builder = new StringBuilder(); // Used to build a new version of String content

        writer.append(header);
        writer.append(separator);
        writer.append(csvHeader);
        writer.append(separator);

        while (scanner.hasNext()) {
            csvContent = scanner.nextLine();
            if (csvContent.startsWith("200")) {
                builder.append(csvContent);
                builder.append(separator);
                break;
            }
            writer.append(csvContent);
            writer.append(separator);
        }

        writer.append(trailer);
        writer.append(separator);
        writer.close();

        while (scanner.hasNext()) {
            builder.append(scanner.nextLine());
            builder.append(separator);
        }

        content = builder.toString();
        return true;
    }
}
