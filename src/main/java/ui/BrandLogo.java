package ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class BrandLogo extends JComponent {

    public BrandLogo(int size) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int size = Math.min(getWidth(), getHeight());
        int inset = Math.max(2, size / 12);
        g.setPaint(new java.awt.GradientPaint(inset, inset, UiTheme.PINK,
                size - inset, size - inset, UiTheme.PURPLE));
        g.fillRoundRect(inset, inset, size - inset * 2, size - inset * 2, size / 3, size / 3);

        int ticketWidth = size * 3 / 5;
        int ticketHeight = size * 2 / 5;
        int ticketX = (size - ticketWidth) / 2;
        int ticketY = (size - ticketHeight) / 2;
        g.setColor(Color.WHITE);
        g.fillRoundRect(ticketX, ticketY, ticketWidth, ticketHeight, size / 10, size / 10);

        int centerX = size / 2;
        int centerY = size / 2;
        int[] xPoints = {centerX - size / 10, centerX - size / 10, centerX + size / 8};
        int[] yPoints = {centerY - size / 7, centerY + size / 7, centerY};
        g.setColor(UiTheme.PINK);
        g.fillPolygon(xPoints, yPoints, 3);
        g.dispose();
    }
}
