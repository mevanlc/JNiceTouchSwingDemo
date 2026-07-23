package io.github.mevanlc.jnicetouch.swingdemo;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Timer;

@SuppressWarnings("serial")
final class GestureCanvas extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final int CARD_WIDTH = 270;
    private static final int CARD_HEIGHT = 170;

    private final GestureState state;
    private float quickLookAlpha;
    private final Timer overlayTimer;

    GestureCanvas(GestureState state) {
        this.state = state;
        setName("Gesture Playground");
        setFocusable(true);
        setOpaque(true);
        setBackground(DemoTheme.BACKGROUND);
        setPreferredSize(new Dimension(720, 430));
        setToolTipText("Scroll to pan, pinch to zoom, rotate, swipe, press, or smart magnify");
        AccessibleContext context = getAccessibleContext();
        context.setAccessibleName("Gesture Playground");
        context.setAccessibleDescription("Interactive target for JNiceTouch trackpad gestures");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                requestFocusInWindow();
            }
        });
        addMouseWheelListener(event -> event.consume());
        installKeyboardFallbacks();

        overlayTimer = new Timer(45, event -> {
            quickLookAlpha = Math.max(0f, quickLookAlpha - 0.045f);
            if (quickLookAlpha == 0f) {
                ((Timer) event.getSource()).stop();
            }
            repaint();
        });
    }

    void flashQuickLook() {
        quickLookAlpha = 1f;
        overlayTimer.restart();
    }

    void resetView() {
        state.reset();
        quickLookAlpha = 0f;
        overlayTimer.stop();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBackdrop(g);
            paintCard(g);
            paintInstructions(g);
            paintQuickLookOverlay(g);
        } finally {
            g.dispose();
        }
    }

    private void paintBackdrop(Graphics2D g) {
        g.setPaint(new GradientPaint(
                0, 0, new Color(0x18202B),
                getWidth(), getHeight(), new Color(0x0C1016)));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(255, 255, 255, 12));
        for (int x = 24; x < getWidth(); x += 32) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 24; y < getHeight(); y += 32) {
            g.drawLine(0, y, getWidth(), y);
        }
    }

    private void paintCard(Graphics2D g) {
        AffineTransform original = g.getTransform();
        double centerX = getWidth() / 2.0 + state.panX();
        double centerY = getHeight() / 2.0 - 10.0 - state.panY();
        g.translate(centerX, centerY);
        g.rotate(Math.toRadians(state.rotationDegrees()));
        g.scale(state.scale(), state.scale());

        RoundRectangle2D card = new RoundRectangle2D.Double(
                -CARD_WIDTH / 2.0,
                -CARD_HEIGHT / 2.0,
                CARD_WIDTH,
                CARD_HEIGHT,
                28,
                28);
        if (state.pressure() > 0.01) {
            g.setColor(new Color(105, 210, 200, 55 + (int) (150 * state.pressure())));
            g.setStroke(new BasicStroke((float) (5 + 15 * state.pressure())));
            g.draw(card);
        }

        g.setPaint(new GradientPaint(
                -CARD_WIDTH / 2f, -CARD_HEIGHT / 2f, new Color(0x39536F),
                CARD_WIDTH / 2f, CARD_HEIGHT / 2f, new Color(0x25423F)));
        g.fill(card);
        g.setColor(new Color(255, 255, 255, 55));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(card);

        g.setColor(DemoTheme.TEXT);
        g.setFont(displayFont(Font.BOLD, 24f));
        g.drawString("JNiceTouch", -108, -35);
        g.setColor(DemoTheme.ACCENT);
        g.fillRoundRect(-108, -19, 82, 5, 5, 5);

        g.setColor(new Color(0xD7E1EA));
        g.setFont(displayFont(Font.PLAIN, 13f));
        g.drawString(String.format("scale  %.2f×", state.scale()), -108, 17);
        g.drawString(String.format("rotation  %+.1f°", state.rotationDegrees()), -108, 40);
        g.drawString(String.format("pressure  %.2f  stage %d",
                state.pressure(), state.pressureStage()), -108, 63);

        g.setTransform(original);
    }

    private void paintInstructions(Graphics2D g) {
        g.setFont(displayFont(Font.PLAIN, 12f));
        g.setColor(DemoTheme.TEXT_MUTED);
        String instruction = hasFocus()
                ? "Keyboard: arrows pan  ·  +/− zoom  ·  [/] rotate  ·  0 reset"
                : "Click to focus  ·  gestures are applied only when this canvas is the pinned target";
        int width = g.getFontMetrics().stringWidth(instruction);
        g.drawString(instruction, Math.max(18, (getWidth() - width) / 2), getHeight() - 18);

        if (state.lastKind() != null) {
            String sequence = "last " + state.lastKind() + "  ·  sequence " + state.sequenceId();
            g.setColor(DemoTheme.ACCENT);
            g.drawString(sequence, 18, 24);
        }
    }

    private void paintQuickLookOverlay(Graphics2D g) {
        if (quickLookAlpha <= 0f) {
            return;
        }
        g.setComposite(AlphaComposite.SrcOver.derive(quickLookAlpha));
        int width = 260;
        int height = 74;
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g.setColor(new Color(0x0B1118));
        g.fillRoundRect(x, y, width, height, 22, 22);
        g.setColor(DemoTheme.ACCENT_BLUE);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, width, height, 22, 22);
        g.setColor(DemoTheme.TEXT);
        g.setFont(displayFont(Font.BOLD, 18f));
        g.drawString("Quick Look detected", x + 38, y + 43);
    }

    private Font displayFont(int style, float size) {
        Font font = getFont();
        if (font == null) {
            font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        return font.deriveFont(style, size);
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleGestureCanvas();
        }
        return accessibleContext;
    }

    private final class AccessibleGestureCanvas extends AccessibleJComponent {
        private static final long serialVersionUID = 1L;
    }

    private void installKeyboardFallbacks() {
        InputMap inputs = getInputMap(WHEN_FOCUSED);
        ActionMap actions = getActionMap();
        bind(inputs, actions, "LEFT", "pan-left", () -> state.panBy(-18, 0));
        bind(inputs, actions, "RIGHT", "pan-right", () -> state.panBy(18, 0));
        bind(inputs, actions, "UP", "pan-up", () -> state.panBy(0, 18));
        bind(inputs, actions, "DOWN", "pan-down", () -> state.panBy(0, -18));
        bind(inputs, actions, "PLUS", "zoom-in", () -> state.scaleBy(1.08));
        bind(inputs, actions, "EQUALS", "zoom-in-equals", () -> state.scaleBy(1.08));
        bind(inputs, actions, "MINUS", "zoom-out", () -> state.scaleBy(0.92));
        bind(inputs, actions, "OPEN_BRACKET", "rotate-left", () -> state.rotateBy(-5));
        bind(inputs, actions, "CLOSE_BRACKET", "rotate-right", () -> state.rotateBy(5));
        bind(inputs, actions, "0", "reset", state::reset);
    }

    private void bind(
            InputMap inputs,
            ActionMap actions,
            String keystroke,
            String name,
            Runnable operation) {
        inputs.put(KeyStroke.getKeyStroke(keystroke), name);
        actions.put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                operation.run();
                repaint();
            }
        });
    }
}
