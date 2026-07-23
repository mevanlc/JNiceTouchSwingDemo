package io.github.mevanlc.jnicetouch.swingdemo;

import java.awt.EventQueue;
import javax.swing.UIManager;

/** Launches the JNiceTouch Swing source example. */
public final class JNiceTouchSwingDemo {
    private JNiceTouchSwingDemo() {}

    /**
     * Starts the demo on the Swing event-dispatch thread.
     *
     * @param arguments ignored command-line arguments
     */
    public static void main(String[] arguments) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "JNiceTouch Swing Demo");
        installSystemLookAndFeel();
        EventQueue.invokeLater(() -> {
            DemoFrame frame = new DemoFrame();
            frame.setVisible(true);
            frame.connectProvider();
        });
    }

    private static void installSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
            System.err.println("Unable to install the system look and feel: " + exception.getMessage());
        }
        UIManager.put("Panel.background", DemoTheme.BACKGROUND);
        UIManager.put("Label.foreground", DemoTheme.TEXT);
        UIManager.put("SplitPane.background", DemoTheme.BACKGROUND);
        UIManager.put("TabbedPane.background", DemoTheme.SURFACE);
        UIManager.put("TabbedPane.foreground", DemoTheme.TEXT);
        UIManager.put("Table.background", DemoTheme.SURFACE);
        UIManager.put("Table.foreground", DemoTheme.TEXT);
        UIManager.put("TableHeader.background", DemoTheme.SURFACE_RAISED);
        UIManager.put("TableHeader.foreground", DemoTheme.TEXT);
        UIManager.put("ScrollPane.background", DemoTheme.SURFACE);
        UIManager.put("TextArea.caretForeground", DemoTheme.TEXT);
        UIManager.put("TextArea.selectionBackground", DemoTheme.ACCENT_BLUE.darker());
        UIManager.put("List.selectionBackground", DemoTheme.ACCENT_BLUE.darker());
        UIManager.put("List.selectionForeground", DemoTheme.TEXT);
    }
}
