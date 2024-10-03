import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.util.Base64;

public class SteganographyAppGUI extends JFrame implements ActionListener {
    private JButton encodeButton, decodeButton, selectImageButton;
    private JTextArea messageArea;
    private JLabel imagePathLabel, imagePreviewLabel;
    private JTextField keyField; // To input the private key
    private String imagePath;

    public SteganographyAppGUI() {
        setTitle("Steganography App");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Background Color
        getContentPane().setBackground(new Color(30, 30, 30)); // Dark background

        // Title
        JLabel titleLabel = new JLabel("Welcome to the Steganography App!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.YELLOW);
        add(titleLabel, BorderLayout.NORTH);

        imagePathLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imagePathLabel.setForeground(Color.WHITE);
        add(imagePathLabel, BorderLayout.CENTER);

        imagePreviewLabel = new JLabel();
        add(imagePreviewLabel, BorderLayout.CENTER);

        // Panel for buttons and text area
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 30));

        selectImageButton = new JButton("Select Image");
        selectImageButton.setBackground(Color.CYAN);
        selectImageButton.setForeground(Color.BLACK);
        selectImageButton.addActionListener(this);
        panel.add(selectImageButton);

        messageArea = new JTextArea(5, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(Color.LIGHT_GRAY);
        messageArea.setForeground(Color.BLACK);
        messageArea.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(new JScrollPane(messageArea));

        // Add key field for encryption/decryption key input
        keyField = new JTextField(30);
        keyField.setBackground(Color.BLACK); // Changed background to black for better contrast
        keyField.setForeground(Color.WHITE); // Changed font color to green for visibility
        keyField.setFont(new Font("Arial", Font.BOLD, 20)); // Increased font size
        keyField.setToolTipText("Enter private key for encryption/decryption");

        // Create the label for the private key input
        JLabel privateKeyLabel = new JLabel("Private Key:");
        privateKeyLabel.setForeground(Color.WHITE); // Change text color to white
        privateKeyLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Increase font size
	panel.add(privateKeyLabel);
	panel.add(keyField);
        //panel.add(new JLabel("Private Key:"));
       // panel.add(keyField);

        // Adjust button layout for a cleaner footer
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(30, 30, 30));

        encodeButton = new JButton("Encode Message");
        encodeButton.setBackground(Color.GREEN);
        encodeButton.setForeground(Color.BLACK);
        encodeButton.addActionListener(this);
        buttonPanel.add(encodeButton);

        decodeButton = new JButton("Decode Message");
        decodeButton.setBackground(Color.RED);
        decodeButton.setForeground(Color.WHITE);
        decodeButton.addActionListener(this);
        buttonPanel.add(decodeButton);

        // Add button panel above the footer
        panel.add(buttonPanel);

        add(panel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SteganographyAppGUI::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectImageButton) {
            selectImage();
        } else if (e.getSource() == encodeButton) {
            String message = messageArea.getText();
            String key = keyField.getText();
            if (imagePath != null && !message.isEmpty() && !key.isEmpty()) {
                try {
                    String encryptedMessage = encrypt(message, key);  // Encrypt the message
                    encodeMessage(imagePath, encryptedMessage);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Encryption error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an image, enter a message, and provide a key.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == decodeButton) {
            String key = keyField.getText();
            if (imagePath != null && !key.isEmpty()) {
                try {
                    String decodedMessage = decodeMessage(imagePath);
                    String decryptedMessage = decrypt(decodedMessage, key);  // Decrypt the message
                    JOptionPane.showMessageDialog(this, "Decoded and decrypted message: " + decryptedMessage);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Decryption error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an image and enter the private key.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imagePath = selectedFile.getAbsolutePath();
            imagePathLabel.setText("Selected image: " + imagePath);

            // Load and display the image
            try {
                BufferedImage originalImage = ImageIO.read(selectedFile);
                Image scaledImage = originalImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Resize to fit
                imagePreviewLabel.setIcon(new ImageIcon(scaledImage));
                imagePreviewLabel.setText(""); // Clear text if an image is displayed
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Encryption method using AES
    public static String encrypt(String data, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(hashKey(key), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decryption method using AES
    public static String decrypt(String encryptedData, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(hashKey(key), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    // Hash the key to ensure it's 16 bytes long (for AES-128)
    public static byte[] hashKey(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes());
        return java.util.Arrays.copyOf(keyBytes, 16); // Use only first 128 bits
    }

    // Encode the encrypted message into the image
    public void encodeMessage(String imagePath, String message) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            byte[] messageBytes = message.getBytes();
            int messageLength = messageBytes.length;

            // Encode the message length into the first row of pixels
            for (int i = 0; i < 32; i++) {
                int pixel = image.getRGB(i, 0);
                int newPixel = (pixel & 0xFFFFFFFE) | ((messageLength >> i) & 1);
                image.setRGB(i, 0, newPixel);
            }

            // Encode the message bytes into the least significant bits of the image pixels
            int messageIndex = 0;
            for (int i = 0; i < messageLength * 8; i++) {
                int pixel = image.getRGB(32 + (i / 8), (i % 8));
                int newPixel = (pixel & 0xFFFFFFFE) | ((messageBytes[messageIndex] >> (i % 8)) & 1);
                image.setRGB(32 + (i / 8), (i % 8), newPixel);
                if (i % 8 == 7) {
                    messageIndex++;
                }
            }

            // Save the modified image
            ImageIO.write(image, "png", new File(imagePath));
            JOptionPane.showMessageDialog(this, "Message encoded successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Encoding error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Decode the message from the image
    public String decodeMessage(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            int messageLength = 0;

            // Decode the message length from the first row of pixels
            for (int i = 0; i < 32; i++) {
                int pixel = image.getRGB(i, 0);
                messageLength |= (pixel & 1) << i;
            }

            byte[] messageBytes = new byte[messageLength];

            // Decode the message bytes from the least significant bits of the image pixels
            int messageIndex = 0;
            for (int i = 0; i < messageLength * 8; i++) {
                int pixel = image.getRGB(32 + (i / 8), (i % 8));
                messageBytes[messageIndex] |= (pixel & 1) << (i % 8);
                if (i % 8 == 7) {
                    messageIndex++;
                }
            }

            return new String(messageBytes);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Decoding error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
