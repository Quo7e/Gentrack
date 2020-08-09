import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

/**
 * UserInterface.java
 *
 * Main class, contains a basic Java Swing UI to allow users to provide any desired XML file in the correct format.
 * The actual parsing is performed in XmlToCsv.java, which could be attached to a different interface if automation was
 * required instead of direct user interaction.
 */
public class UserInterface {
    private JFrame frame; // Java Swing UI frame
    private JFileChooser fileChooser; // Java Swing file chooser dialogue

    /**
     * createMenu()
     *
     * Method creating the menu bar for the UI, which contains:
     *  File -> Open        Allows the user to provide an XML file to parse
     *  File -> Quit        After confirmation, exits the program
     * ActionListener::actionPerformed is overridden for both menu items by lambda functions defined within this method.
     */
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem quitItem = new JMenuItem("Quit");
        fileMenu.add(openItem);
        fileMenu.add(quitItem);

        openItem.addActionListener(e -> {
            /* Lambda function, overrides actionPerformed in ActionListener for openItem.
             * Causes a file chooser dialog to open so the user can provide an XML file.
             * The selected file will be parsed using methods in XmlToCsv and appropriate feedback returned. */
            int returnValue = fileChooser.showOpenDialog(frame);
            int csvCount = 0; // Tracks number of CSV files built
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File xmlFile = fileChooser.getSelectedFile();

                try {
                    XmlToCsv converter = new XmlToCsv(xmlFile);
                    while (converter.buildCsv()) {
                        csvCount++;
                    }
                    JOptionPane.showMessageDialog(null, "Built " + csvCount + " CSV files.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Could not create CSV file.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });

        quitItem.addActionListener(e -> {
            /* Lambda function, overrides actionPerformed in ActionListener for quitItem.
             * Causes a dialog window to open and confirm the user wants to quit.
             * If yes, quits the program. */
            int returnValue = JOptionPane.showConfirmDialog(null, "Are you sure?", "Quit", JOptionPane.OK_CANCEL_OPTION);
            if (returnValue == 0) { System.exit(0); }
        });

        FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("XML Files", "xml");

        fileChooser = new JFileChooser("./");
        fileChooser.addChoosableFileFilter(extensionFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
    }

    /**
     * UserInterface()
     *
     * Constructor method, sets up JFrame and calls createMenu().
     */
    private UserInterface() {
        frame = new JFrame("XML to CSV Parser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createMenu();

        frame.setSize(300,80); // Entirely arbitrary sizing.
        frame.setVisible(true);
    }

    /**
     * main()
     *
     * Main function, just creates an instance of UserInterface.
     *
     * @param args      Command line arguments (none accepted for this program)
     */
    public static void main(String[] args) {
        new UserInterface();
    }
}
