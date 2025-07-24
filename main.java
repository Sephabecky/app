import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.io.*;
import java.util.stream.IntStream;

public class WelfareSystemAp { 
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField nameField, phoneField, amountField, searchField, targetAmountField;
    private JLabel totalLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private HashMap<String, Integer> contributions;
    private int targetAmount = 0;

    public WelfareSystemAp() {
        contributions = new HashMap<>();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Welfare Contribution System");
        frame.setSize(700, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.BLACK);
        JLabel welcomeLabel = new JLabel("WELCOME TO THE WELFARE SYSTEM", SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLabel.setForeground(Color.LIGHT_GRAY);
        loadingLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        welcomePanel.add(loadingLabel, BorderLayout.SOUTH);

        JPanel systemPanel = createSystemPanel();

        mainPanel.add(welcomePanel, "welcome");
        mainPanel.add(systemPanel, "system");

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Timer(10000, e -> {
            cardLayout.show(mainPanel, "system");
        }).start();
    }

    private JPanel createSystemPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(15);
        phoneField = new JTextField(15);
        amountField = new JTextField(15);
        searchField = new JTextField(15);
        targetAmountField = new JTextField(15);

        JButton registerButton = new JButton("Register");
        JButton contributeButton = new JButton("Contributed");
        JButton searchButton = new JButton("Search");
        JButton setTargetButton = new JButton("Set Target Amount");

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Amount (Ksh):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(registerButton, gbc);
        gbc.gridx = 1;
        inputPanel.add(contributeButton, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(searchField, gbc);
        gbc.gridx = 1;
        inputPanel.add(searchButton, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Total to be Contributed:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(targetAmountField, gbc);

        panel.add(inputPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Name", "Phone", "Total Contributed (Ksh)", "Balance (Ksh)"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total Balance: Ksh 0");
        bottomPanel.add(totalLabel);
        bottomPanel.add(setTargetButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> registerMember());
        contributeButton.addActionListener(e -> recordContribution());
        searchButton.addActionListener(e -> searchMember());
        setTargetButton.addActionListener(e -> setTargetAmount());

        return panel;
    }

    private void registerMember() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both name and phone number to register.");
            return;
        }
        if (contributions.containsKey(name)) {
            JOptionPane.showMessageDialog(frame, "Member already registered.");
            return;
        }
        contributions.put(name, 0);
        tableModel.addRow(new Object[]{name, phone, 0, targetAmount});
        nameField.setText("");
        phoneField.setText("");
    }

    private void recordContribution() {
        String name = nameField.getText().trim();
        String amountStr = amountField.getText().trim();

        if (name.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both name and amount.");
            return;
        }

        if (!contributions.containsKey(name)) {
            JOptionPane.showMessageDialog(frame, "Member not registered. Please register first.");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Amount must be a number.");
            return;
        }

        int currentTotal = contributions.get(name);
        int newTotal = currentTotal + amount;
        contributions.put(name, newTotal);

        int balance = targetAmount - newTotal;
        balance = Math.max(balance, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(name)) {
                tableModel.setValueAt(newTotal, i, 2);
                tableModel.setValueAt(balance, i, 3);
                break;
            }
        }

        int total = contributions.values().stream().mapToInt(Integer::intValue).sum();
        totalLabel.setText("Total Balance: Ksh " + total);

        try (FileWriter writer = new FileWriter("contributions.csv", true)) {
            writer.write(name + "," + amount + "," + LocalDate.now() + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("SMS sent to " + name + ": Thank you for contributing Ksh " + amount + ". Your new total is Ksh " + newTotal + " and your balance is Ksh " + balance + ".");

        amountField.setText("");
    }

    private void searchMember() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a name to search.");
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String rowName = tableModel.getValueAt(i, 0).toString().toLowerCase();
            if (rowName.contains(keyword)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(new Rectangle(table.getCellRect(i, 0, true)));
                return;
            }
        }

        JOptionPane.showMessageDialog(frame, "Member not found.");
    }

    private void setTargetAmount() {
        String targetStr = targetAmountField.getText().trim();
        if (targetStr.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a target amount.");
            return;
        }

        try {
            targetAmount = Integer.parseInt(targetStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Target amount must be a number.");
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            int total = contributions.getOrDefault(name, 0);
            int balance = Math.max(targetAmount - total, 0);
            tableModel.setValueAt(balance, i, 3);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelfareSystemAp());
    }
}

