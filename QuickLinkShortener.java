import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;

public class QuickLinkShortener {
    private JFrame frame;
    private JTextField longUrlField;
    private JTextField shortUrlField;
    private JList<String> shortUrlList;
    private DefaultListModel<String> listModel;
    private HashMap<String, String> urlMap; // To store short and original URLs
    private final String FILE_NAME = "url_mappings.txt"; // File to store URL mappings

    public QuickLinkShortener() {
        urlMap = new HashMap<>();
        initialize();
        loadUrlMappings(); // Load mappings when the application starts
    }

    private void initialize() {
        frame = new JFrame("QuickLink Shortener");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350);
        frame.setLayout(new FlowLayout());

        // Set a material-like color theme
        frame.getContentPane().setBackground(new Color(233, 236, 239)); // Light gray background

        longUrlField = new JTextField(20);
        longUrlField.setBorder(BorderFactory.createLineBorder(new Color(63, 81, 181), 1));
        
        shortUrlField = new JTextField(20);
        shortUrlField.setEditable(false);
        shortUrlField.setBorder(BorderFactory.createLineBorder(new Color(63, 81, 181), 1));

        JButton shortenUrlButton = new JButton("Shorten URL");
        shortenUrlButton.setBackground(new Color(63, 81, 181));
        shortenUrlButton.setForeground(Color.WHITE);
        shortenUrlButton.addActionListener(new ShortenUrlAction());

        JButton retrieveUrlButton = new JButton("Retrieve Original URL");
        retrieveUrlButton.setBackground(new Color(63, 81, 181));
        retrieveUrlButton.setForeground(Color.WHITE);
        retrieveUrlButton.addActionListener(new RetrieveUrlAction());

        JButton deleteUrlButton = new JButton("Delete URL");
        deleteUrlButton.setBackground(new Color(239, 83, 80));
        deleteUrlButton.setForeground(Color.WHITE);
        deleteUrlButton.addActionListener(new DeleteUrlAction());

        listModel = new DefaultListModel<>();
        shortUrlList = new JList<>(listModel);
        shortUrlList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set a border for the JList
        shortUrlList.setBorder(BorderFactory.createLineBorder(new Color(63, 81, 181), 1));
        
        JScrollPane scrollPane = new JScrollPane(shortUrlList);
        scrollPane.setPreferredSize(new Dimension(350, 100));

        // Add components to the frame
        frame.add(new JLabel("Enter Long URL:"));
        frame.add(longUrlField);
        frame.add(new JLabel("Shortened URL:"));
        frame.add(shortUrlField);
        frame.add(shortenUrlButton);
        frame.add(retrieveUrlButton);
        frame.add(deleteUrlButton);
        frame.add(scrollPane);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null); // Center the window on the screen
    }

    private class ShortenUrlAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String longUrl = longUrlField.getText().trim();
            if (!longUrl.isEmpty()) {
                String shortUrl = generateShortUrl(longUrl);
                urlMap.put(shortUrl, longUrl);
                listModel.addElement(shortUrl);
                shortUrlField.setText(shortUrl);
                longUrlField.setText(""); // Clear input field
                saveUrlMapping(shortUrl, longUrl); // Save to file
            } else {
                showError("Please enter a valid long URL.");
            }
        }
    }

    private class RetrieveUrlAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedShortUrl = shortUrlList.getSelectedValue();
            if (selectedShortUrl != null) {
                String originalUrl = urlMap.get(selectedShortUrl);
                if (originalUrl != null) {
                    // Show the original URL in a popup dialog centered on the frame
                    JOptionPane.showMessageDialog(frame, "Original URL: " + originalUrl, 
                                                  "Retrieved URL", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showError("Shortened URL not found.");
                }
            } else {
                showError("Please select a short URL from the list.");
            }
        }
    }

    private class DeleteUrlAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedShortUrl = shortUrlList.getSelectedValue();
            if (selectedShortUrl != null) {
                urlMap.remove(selectedShortUrl);
                listModel.removeElement(selectedShortUrl);
                deleteUrlMapping(selectedShortUrl); // Remove from file
                showError("Shortened URL deleted successfully.");
            } else {
                showError("Please select a short URL to delete.");
            }
        }
    }

    private String generateShortUrl(String longUrl) {
        // Simple example: generate a short URL based on the hash code
        return "short.ly/" + Math.abs(longUrl.hashCode());
    }

    private void saveUrlMapping(String shortUrl, String longUrl) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(shortUrl + "," + longUrl);
            writer.newLine();
        } catch (IOException e) {
            showError("Error saving URL mapping: " + e.getMessage());
        }
    }

    private void deleteUrlMapping(String shortUrl) {
        try {
            File inputFile = new File(FILE_NAME);
            File tempFile = new File("temp.txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (!parts[0].equals(shortUrl)) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
            inputFile.delete();
            tempFile.renameTo(inputFile);
        } catch (IOException e) {
            showError("Error deleting URL mapping: " + e.getMessage());
        }
    }

    private void loadUrlMappings() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        String shortUrl = parts[0];
                        String longUrl = parts[1];
                        urlMap.put(shortUrl, longUrl);
                        listModel.addElement(shortUrl);
                    }
                }
            } catch (IOException e) {
                showError("Error loading URL mappings: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuickLinkShortener::new);
    }
}
