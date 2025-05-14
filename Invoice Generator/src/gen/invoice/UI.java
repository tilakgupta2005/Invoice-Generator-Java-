package gen.invoice;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class UI extends JFrame {

    private HistoryPanel historyPanel;

    public UI() {
        // System look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Invoice Generator");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Billing", new BillingPanel());
        tabs.add("Items", new ItemsPanel());
        historyPanel = new HistoryPanel();
        tabs.add("Transaction History", historyPanel);

        // Refresh history on tab selection
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabs.getSelectedComponent() == historyPanel) {
                    historyPanel.reloadData(null, null);
                }
            }
        });

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }
}