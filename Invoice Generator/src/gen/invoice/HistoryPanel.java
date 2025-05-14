package gen.invoice;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class HistoryPanel extends JPanel {
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchType;

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#f0f0f0"));

        JLabel heading = new JLabel("Transaction History");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setBackground(Color.decode("#f0f0f0"));

        searchType = new JComboBox<>(new String[]{"Customer Name", "Phone"});
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        JButton resetButton = new JButton("Reset");

        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchType);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);

        String[] columns = {"UID", "Date & Time", "Customer Name", "Phone", "Payment No", "Subtotal", "Discount", "Tax", "Profit", "Grand Total"};
        tableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new EmptyBorder(10, 20, 20, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Load all data initially
        reloadData(null, null);

        // Add button actions
        searchButton.addActionListener(e -> {
            String selectedType = searchType.getSelectedItem().toString();
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                String column = selectedType.equals("Customer Name") ? "customer_name" : "phone";
                reloadData(column, keyword);
            }
        });

        resetButton.addActionListener(e -> {
            searchField.setText("");
            reloadData(null, null);
        });
    }

    // Modified reloadData to accept search filters
    public void reloadData(String searchColumn, String keyword) {
        tableModel.setRowCount(0);
        String baseSql = "SELECT uid, date_time, customer_name, phone, payment_no, subtotal, discount_amt, tax_amt, profit, grand_total FROM transaction_history";
        String sql = searchColumn != null ? baseSql + " WHERE " + searchColumn + " LIKE ? ORDER BY date_time DESC" : baseSql + " ORDER BY date_time DESC";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/invoice_db", "root", "root");
             PreparedStatement pst = conn.prepareStatement(sql)) {

            if (searchColumn != null) {
                pst.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("uid"));
                row.add(rs.getTimestamp("date_time"));
                row.add(rs.getString("customer_name"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("payment_no"));
                row.add(rs.getDouble("subtotal"));
                row.add(rs.getDouble("discount_amt"));
                row.add(rs.getDouble("tax_amt"));
                row.add(rs.getDouble("profit"));
                row.add(rs.getDouble("grand_total"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + e.getMessage());
        }
    }
}
