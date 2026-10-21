package ui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** CardLayout wrapper that cross-fades between pages without changing their existing code. */
public final class AnimatedCardPanel extends JPanel {
    private final CardLayout cardLayout;
    private BufferedImage outgoingFrame;
    private long transitionStarted;
    private static final long TRANSITION_MILLIS = 260L;
    private final Timer transitionTimer;

    public AnimatedCardPanel(CardLayout layout) {
        super(layout);
        this.cardLayout = layout;
        setOpaque(false);
        Timer timer = new Timer(16, event -> {
            if (System.currentTimeMillis() - transitionStarted >= TRANSITION_MILLIS) {
                outgoingFrame = null;
                ((Timer) event.getSource()).stop();
            }
            repaint();
        });
        timer.setCoalesce(true);
        transitionTimer = timer;
    }

    public void showCard(String cardName) {
        Component current = null;
        for (Component component : getComponents()) {
            if (component.isVisible()) {
                current = component;
                break;
            }
        }

        BufferedImage snapshot = null;
        if (current != null && getWidth() > 0 && getHeight() > 0) {
            snapshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = snapshot.createGraphics();
            imageGraphics.setClip(0, 0, getWidth(), getHeight());
            current.printAll(imageGraphics);
            imageGraphics.dispose();
        }

        cardLayout.show(this, cardName);
        revalidate();
        if (snapshot != null) {
            outgoingFrame = snapshot;
            transitionStarted = System.currentTimeMillis();
            if (!transitionTimer.isRunning()) transitionTimer.start();
        } else {
            outgoingFrame = null;
            transitionTimer.stop();
        }
        repaint();
    }

    @Override protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);
        if (outgoingFrame == null) return;

        float progress = Math.min(1f,
                (System.currentTimeMillis() - transitionStarted) / (float) TRANSITION_MILLIS);
        if (progress >= 1f) {
            outgoingFrame = null;
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        g.setComposite(AlphaComposite.SrcOver.derive(1f - progress));
        g.drawImage(outgoingFrame, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
    }

    @Override public void removeNotify() {
        transitionTimer.stop();
        outgoingFrame = null;
        super.removeNotify();
    }
}
