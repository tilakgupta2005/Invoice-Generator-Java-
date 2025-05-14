package gen.invoice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillingPanel extends JPanel {
    private JTextField customerNameField, phoneField, discountField, paymentField;
    private JTextArea invoiceArea;
    private JPanel itemsScrollPanel;

    private double currentSubtotal = 0;
    private double currentProfit = 0;
    private double currentGrandTotal = 0;
    private double currentDiscountAmt = 0;
    private double currentTaxAmt = 0;

    private List<ItemControl> itemControls = new ArrayList<>();

    public BillingPanel() {
        setLayout(new BorderLayout());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildFormPanel(), buildInvoicePanel());
        split.setResizeWeight(0.5);
        split.setDividerSize(4);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new BorderLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridLayout(0, 1, 5, 5));
        customerNameField = new JTextField();
        phoneField = new JTextField();
        discountField = new JTextField("0");
        paymentField = new JTextField();
        top.add(new JLabel("Customer Name:")); top.add(customerNameField);
        top.add(new JLabel("Phone No:")); top.add(phoneField);
        top.add(new JLabel("Discount (%):")); top.add(discountField);
        top.add(new JLabel("UPI Id:")); top.add(paymentField);

        itemsScrollPanel = new JPanel();
        itemsScrollPanel.setLayout(new BoxLayout(itemsScrollPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(itemsScrollPanel);
        scroll.setPreferredSize(new Dimension(0, 300));
        scroll.getVerticalScrollBar().setUnitIncrement(16); // faster scroll
        loadItemControls();

        JButton updateBtn = new JButton("Update Invoice");
        updateBtn.addActionListener(e -> refreshInvoice());

        JButton printBtn = new JButton("Print & Save");
        printBtn.addActionListener(e -> {
            refreshInvoice();
            saveTransaction();
            resetForm();
            JOptionPane.showMessageDialog(this, "Invoice created successfully!");
        });

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 2));
        btnPanel.add(updateBtn);
        btnPanel.add(printBtn);

        form.add(top, BorderLayout.NORTH);
        form.add(scroll, BorderLayout.CENTER);
        form.add(btnPanel, BorderLayout.SOUTH);
        return form;
    }

    private JPanel buildInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        invoiceArea = new JTextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(new JScrollPane(invoiceArea), BorderLayout.CENTER);
        return panel;
    }

    private void loadItemControls() {
        try (Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/invoice_db", "root", "root");
             PreparedStatement pst = c.prepareStatement(
                     "SELECT name, cost_price, selling_price FROM items");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                double cp = rs.getDouble("cost_price");
                double sp = rs.getDouble("selling_price");
                ItemControl ic = new ItemControl(name, cp, sp);
                itemControls.add(ic);
                itemsScrollPanel.add(ic.panel);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading items: " + e.getMessage());
        }
    }

    private void refreshInvoice() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tilak Ram\n");
        sb.append("Phoneno:- +91-7355762919 \n");
        sb.append("-----------------------------\n");
        sb.append("Customer: " + customerNameField.getText() + "\n");
        sb.append("Phone: " + phoneField.getText() + "\n");
        sb.append("Payment/Txn No: " + paymentField.getText() + "\n");
        sb.append("-----------------------------\n");
        sb.append(String.format("%-15s %5s %10s %10s\n",
                "Item", "Qty", "Unit", "Total"));

        double subtotal = 0;
        double totalProfit = 0;
        for (ItemControl ic : itemControls) {
            if (ic.qty > 0) {
                int qty = ic.qty;
                double lineTotal = qty * ic.unitPrice;
                double lineCost = qty * ic.costPrice;
                subtotal += lineTotal;
                totalProfit += (lineTotal - lineCost);
                sb.append(String.format("%-15s %5d %10.2f %10.2f\n",
                        ic.name, qty, ic.unitPrice, lineTotal));
            }
        }
        sb.append("-----------------------------\n");

        double discountAmt = subtotal * parseField(discountField) / 100;
        double taxable = subtotal - discountAmt;
        double taxAmt = taxable * 0.18;
        double grandTotal = taxable + taxAmt;

        currentSubtotal = subtotal;
        currentProfit = totalProfit;
        currentGrandTotal = grandTotal;
        currentDiscountAmt = discountAmt;
        currentTaxAmt = taxAmt;

        sb.append(String.format("Total Amount : %10.2f\n", subtotal));
        sb.append(String.format("Discount     : %10.2f\n", discountAmt));
        sb.append(String.format("Tax (18%%)    : %10.2f\n", taxAmt));
        sb.append("-----------------------------\n");
        sb.append(String.format("Amount to Pay: %10.2f\n", grandTotal));
        sb.append("-----------------------------\n");
        sb.append("Thank you for visiting!\n");

        invoiceArea.setText(sb.toString());
    }

    private void saveTransaction() {
        String sql = "INSERT INTO transaction_history " +
                "(customer_name, phone, payment_no, subtotal, discount_amt, tax_amt, profit, grand_total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/invoice_db", "root", "root");
             PreparedStatement pst = c.prepareStatement(sql)) {

            pst.setString(1, customerNameField.getText());
            pst.setString(2, phoneField.getText());
            pst.setString(3, paymentField.getText());
            pst.setDouble(4, currentSubtotal);
            pst.setDouble(5, currentDiscountAmt);
            pst.setDouble(6, currentTaxAmt);
            pst.setDouble(7, currentProfit);
            pst.setDouble(8, currentGrandTotal);

            pst.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving invoice: " + e.getMessage());
        }
    }

    private double parseField(JTextField field) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void resetForm() {
        customerNameField.setText("");
        phoneField.setText("");
        discountField.setText("0");
        paymentField.setText("");
        for (ItemControl ic : itemControls) {
            ic.qty = 0;
            ic.qtyLabel.setText("0");
        }
        invoiceArea.setText("");
    }

    // Inner class
    private static class ItemControl {
        String name;
        double costPrice, unitPrice;
        JLabel nameLabel, qtyLabel;
        JButton decBtn, incBtn;
        JPanel panel;
        int qty = 0;

        ItemControl(String name, double costPrice, double unitPrice) {
            this.name = name;
            this.costPrice = costPrice;
            this.unitPrice = unitPrice;

            nameLabel = new JLabel(name + " (₹" + unitPrice + ")");
            nameLabel.setPreferredSize(new Dimension(150, 25));

            decBtn = new JButton("−");
            incBtn = new JButton("+");
            qtyLabel = new JLabel("0", SwingConstants.CENTER);
            qtyLabel.setPreferredSize(new Dimension(30, 25));

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            panel.add(nameLabel);
            panel.add(Box.createHorizontalGlue());
            panel.add(decBtn);
            panel.add(Box.createHorizontalStrut(5));
            panel.add(qtyLabel);
            panel.add(Box.createHorizontalStrut(5));
            panel.add(incBtn);

            decBtn.addActionListener(e -> {
                if (qty > 0) qty--;
                qtyLabel.setText(String.valueOf(qty));
            });

            incBtn.addActionListener(e -> {
                qty++;
                qtyLabel.setText(String.valueOf(qty));
            });
        }
    }
}
