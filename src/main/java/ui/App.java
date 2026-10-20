package ui;

import configuration.DBconnection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.applyDefaults();
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            CinemaGateway gateway;
            try (Connection connection = DBconnection.getConnection()) {
                gateway = new JdbcCinemaGateway();
            } catch (Exception ex) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "Could not reach MySQL.\n\n"
                                + ex.getMessage()
                                + "\n\nContinue with sample data so you can still explore the UI?",
                        "CineBook",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    System.exit(0);
                    return;
                }
                gateway = new InMemoryGateway();
            }

            new LoginFrame(gateway).setVisible(true);
        });
    }
}
