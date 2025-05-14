package gen.invoice;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ItemsPanel extends JPanel {
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private Connection conn;

    public ItemsPanel() {
        setLayout(new BorderLayout());
        connectToDatabase();

        String[] columns = {"ID", "Item Name", "Cost Price (CP)", "Selling Price (MRP)", "Margin %"};
        tableModel = new DefaultTableModel(columns, 0);
        itemTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(itemTable);

        JButton addItemButton = new JButton("Add New Item");
        addItemButton.addActionListener(e -> showAddItemDialog());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addItemButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadItems();  // Initial load
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/invoice_db", "root", "root");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + e.getMessage());
        }
    }

    private void loadItems() {
        try {
            String sql = "SELECT id, name, cost_price, selling_price, " +
                         "ROUND((selling_price - cost_price) / cost_price * 100, 2) AS margin_percent " +
                         "FROM items";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0);  // Clear table first

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("cost_price"),
                    rs.getDouble("selling_price"),
                    rs.getDouble("margin_percent") + "%"
                };
                tableModel.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void showAddItemDialog() {
        JTextField nameField = new JTextField();
        JTextField cpField = new JTextField();
        JTextField spField = new JTextField();
        Object[] fields = {
            "Item Name:", nameField,
            "Cost Price (CP):", cpField,
            "Selling Price (MRP):", spField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add New Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double cp = Double.parseDouble(cpField.getText().trim());
                double sp = Double.parseDouble(spField.getText().trim());

                if (name.isEmpty() || cp <= 0 || sp <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter valid item name and positive prices.");
                    return;
                }

                String sql = "INSERT INTO items (name, cost_price, selling_price) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setDouble(2, cp);
                stmt.setDouble(3, sp);
                stmt.executeUpdate();
                stmt.close();

                loadItems();  // Refresh table after insertion
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for prices.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding item: " + e.getMessage());
            }
        }
    }
}
