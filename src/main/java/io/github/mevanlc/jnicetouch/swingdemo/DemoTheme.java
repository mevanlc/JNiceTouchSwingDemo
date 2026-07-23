package io.github.mevanlc.jnicetouch.swingdemo;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

final class DemoTheme {
    static final Color BACKGROUND = new Color(0x11151C);
    static final Color SURFACE = new Color(0x1B212B);
    static final Color SURFACE_RAISED = new Color(0x242C38);
    static final Color BORDER = new Color(0x364151);
    static final Color TEXT = new Color(0xF2F5F8);
    static final Color TEXT_MUTED = new Color(0x9CA9B8);
    static final Color ACCENT = new Color(0x69D2C8);
    static final Color ACCENT_BLUE = new Color(0x62A8FF);
    static final Color SUCCESS = new Color(0x6ED39A);
    static final Color WARNING = new Color(0xF3BC66);
    static final Color ERROR = new Color(0xF07878);

    private DemoTheme() {}

    static Border panelBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }

    static void title(JComponent component) {
        component.setFont(component.getFont().deriveFont(Font.BOLD, 13f));
        component.setForeground(TEXT);
    }
}
