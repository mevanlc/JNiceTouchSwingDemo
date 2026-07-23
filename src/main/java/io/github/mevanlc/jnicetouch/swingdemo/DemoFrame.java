package io.github.mevanlc.jnicetouch.swingdemo;

import io.github.mevanlc.jnicetouch.Capability;
import io.github.mevanlc.jnicetouch.HapticFeedbackPattern;
import io.github.mevanlc.jnicetouch.HapticPerformanceTime;
import io.github.mevanlc.jnicetouch.JNiceTouch;
import io.github.mevanlc.jnicetouch.MacTrackpadEvent;
import io.github.mevanlc.jnicetouch.MacTrackpadProvider;
import io.github.mevanlc.jnicetouch.Subscription;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

@SuppressWarnings("serial")
final class DemoFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_PLAYGROUND_HINT =
            "custom target · native deltas own movement";

    private final JPanel root = new JPanel(new BorderLayout());
    private final GestureState gestureState = new GestureState();
    private final GestureCanvas gestureCanvas = new GestureCanvas(gestureState);
    private final EventLogModel eventLog = new EventLogModel();
    private final CapabilityDashboard capabilityDashboard = new CapabilityDashboard();
    private final JTextArea eventDetails = new JTextArea();
    private final JLabel providerStatus = new JLabel("Starting native provider…");
    private final JLabel droppedStatus = new JLabel("Queue drops  —");
    private final JLabel eventCount = new JLabel("0 captured events");
    private final JLabel playgroundHint = new JLabel(DEFAULT_PLAYGROUND_HINT);
    private final JToggleButton pauseLog = new JToggleButton("Pause log");
    private final JComboBox<HapticPerformanceTime> hapticTime =
            new JComboBox<>(HapticPerformanceTime.values());
    private final JButton genericHaptic = new JButton("Generic");
    private final JButton alignmentHaptic = new JButton("Alignment");
    private final JButton levelHaptic = new JButton("Level change");
    private final JToggleButton scrollMapping = new JToggleButton("Mapping A · reversed", true);
    private final JButton openImage = new JButton("Open image…");
    private final JMenuItem clearImageMenu = new JMenuItem("Clear Image");
    private final Timer droppedTimer;

    private MacTrackpadProvider provider;
    private Subscription subscription;
    private FileDialog imageDialog;
    private SwingWorker<BufferedImage, Void> imageLoadWorker;
    private boolean shuttingDown;
    private long capturedEvents;

    DemoFrame() {
        super("JNiceTouch Swing Demo");
        AppIcon.install(this);
        configureImageActions();
        setName("JNiceTouch Swing Demo");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setPreferredSize(new Dimension(1240, 820));

        root.setName("JNiceTouch Demo Root");
        root.setBackground(DemoTheme.BACKGROUND);
        setContentPane(root);
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createWorkspace(), BorderLayout.CENTER);
        setJMenuBar(createMenuBar());

        getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
        getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
        getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                shutdown();
            }
        });

        droppedTimer = new Timer(1000, event -> updateDroppedCount());
        configureHapticButtons();
        pack();
        setLocationByPlatform(true);
    }

    void connectProvider() {
        if (!JNiceTouch.isSupported()) {
            showProviderError("JNiceTouch requires macOS with JDK 22 or newer");
            return;
        }
        try {
            provider = JNiceTouch.open();
            Set<Capability> capabilities = provider.capabilities();
            capabilityDashboard.setSupported(capabilities);
            subscription = provider.listen(root, this::trackpadEvent);
            boolean haptics = capabilities.contains(Capability.HAPTIC_FEEDBACK);
            genericHaptic.setEnabled(haptics);
            alignmentHaptic.setEnabled(haptics);
            levelHaptic.setEnabled(haptics);
            hapticTime.setEnabled(haptics);
            setProviderStatus("Connected · FFM ABI v1 · callbacks on EDT", DemoTheme.SUCCESS);
            eventDetails.setText("Move the pointer over a target and use the trackpad.\n"
                    + "The complete native record for the latest event appears here.");
            droppedTimer.start();
        } catch (RuntimeException exception) {
            showProviderError(exception.getMessage());
        }
    }

    private JComponent createHeader() {
        HeaderPanel header = new HeaderPanel();
        header.setLayout(new BorderLayout(16, 0));
        header.setBorder(BorderFactory.createEmptyBorder(26, 26, 15, 24));

        JPanel titles = new JPanel(new GridLayout(0, 1, 0, 2));
        titles.setOpaque(false);
        JLabel title = new JLabel("JNiceTouch");
        title.setForeground(DemoTheme.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 27f));
        JLabel subtitle = new JLabel("AppKit gestures → versioned C ABI → JDK FFM → Swing EDT");
        subtitle.setForeground(DemoTheme.TEXT_MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        titles.add(title);
        titles.add(subtitle);

        JPanel status = new JPanel(new GridLayout(0, 1, 0, 3));
        status.setOpaque(false);
        providerStatus.setName("Provider status");
        providerStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        providerStatus.setForeground(DemoTheme.WARNING);
        droppedStatus.setName("Native queue drops");
        droppedStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        droppedStatus.setForeground(DemoTheme.TEXT_MUTED);
        status.add(providerStatus);
        status.add(droppedStatus);

        header.add(titles, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        return header;
    }

    private JComponent createWorkspace() {
        JSplitPane horizontal = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createPlaygroundPanel(),
                createInspectorTabs());
        horizontal.setResizeWeight(0.70);
        horizontal.setDividerLocation(780);
        horizontal.setDividerSize(7);
        horizontal.setBorder(null);
        horizontal.setContinuousLayout(true);

        JSplitPane vertical = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                horizontal,
                createEventLogPanel());
        vertical.setResizeWeight(0.70);
        vertical.setDividerLocation(545);
        vertical.setDividerSize(7);
        vertical.setBorder(BorderFactory.createEmptyBorder(0, 18, 17, 18));
        vertical.setContinuousLayout(true);
        return vertical;
    }

    private JComponent createPlaygroundPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(DemoTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JLabel title = new JLabel("Gesture playground");
        DemoTheme.title(title);
        playgroundHint.setForeground(DemoTheme.TEXT_MUTED);
        playgroundHint.setFont(playgroundHint.getFont().deriveFont(11f));
        JPanel labels = new JPanel(new GridLayout(0, 1, 0, 1));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(playgroundHint);
        JButton reset = new JButton("Reset view");
        reset.setName("Reset playground");
        reset.addActionListener(event -> gestureCanvas.resetView());
        scrollMapping.setName("Playground scroll mapping");
        scrollMapping.setToolTipText(
                "Switch immediately between reversed and raw AppKit scroll deltas");
        scrollMapping.addActionListener(event -> {
            boolean reversed = scrollMapping.isSelected();
            gestureState.setReverseScrollDeltas(reversed);
            scrollMapping.setText(reversed ? "Mapping A · reversed" : "Mapping B · raw");
            gestureCanvas.repaint();
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(scrollMapping);
        actions.add(openImage);
        actions.add(reset);
        toolbar.add(labels, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        JPanel canvasBorder = new JPanel(new BorderLayout());
        canvasBorder.setBackground(DemoTheme.BACKGROUND);
        canvasBorder.setBorder(BorderFactory.createLineBorder(DemoTheme.BORDER));
        canvasBorder.add(gestureCanvas);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(canvasBorder, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createInspectorTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("Demo inspectors");
        tabs.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        tabs.addTab("Capabilities", createCapabilitiesPanel());
        tabs.addTab("Latest event", createDetailsPanel());
        tabs.addTab("Haptics & AWT", createHapticsAndAwtPanel());
        return tabs;
    }

    private JComponent createCapabilitiesPanel() {
        JPanel panel = sectionPanel();
        panel.setLayout(new BorderLayout(0, 12));
        JLabel title = new JLabel("Native ABI capabilities");
        DemoTheme.title(title);
        JLabel note = new JLabel("Cards count records received by kind");
        note.setForeground(DemoTheme.TEXT_MUTED);
        note.setFont(note.getFont().deriveFont(11f));
        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 2));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(note);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(capabilityDashboard, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createDetailsPanel() {
        JPanel panel = sectionPanel();
        panel.setLayout(new BorderLayout(0, 10));
        JLabel title = new JLabel("Latest complete event record");
        DemoTheme.title(title);
        eventDetails.setName("Latest event details");
        eventDetails.setEditable(false);
        eventDetails.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        eventDetails.setForeground(DemoTheme.TEXT);
        eventDetails.setBackground(DemoTheme.SURFACE);
        eventDetails.setMargin(new Insets(8, 8, 8, 8));
        eventDetails.setText("Waiting for the native provider…");
        JScrollPane scroll = new JScrollPane(eventDetails);
        scroll.setBorder(BorderFactory.createLineBorder(DemoTheme.BORDER));
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createHapticsAndAwtPanel() {
        JPanel panel = sectionPanel();
        panel.setLayout(new BorderLayout(0, 14));

        JPanel haptics = new JPanel(new BorderLayout(0, 8));
        haptics.setOpaque(false);
        JLabel hapticTitle = new JLabel("Haptic feedback");
        DemoTheme.title(hapticTitle);
        JPanel hapticButtons = new JPanel(new GridLayout(0, 1, 5, 5));
        hapticButtons.setOpaque(false);
        hapticButtons.add(genericHaptic);
        hapticButtons.add(alignmentHaptic);
        hapticButtons.add(levelHaptic);
        hapticButtons.add(hapticTime);
        haptics.add(hapticTitle, BorderLayout.NORTH);
        haptics.add(hapticButtons, BorderLayout.CENTER);

        JPanel awt = new JPanel(new BorderLayout(0, 7));
        awt.setOpaque(false);
        JLabel awtTitle = new JLabel("Standard Swing scrolling");
        DemoTheme.title(awtTitle);
        JList<String> list = new JList<>(IntStream.rangeClosed(1, 80)
                .mapToObj(index -> String.format(Locale.ROOT, "%02d  AWT-owned list item", index))
                .toArray(String[]::new));
        list.setName("Standard AWT Scroll List");
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(DemoTheme.SURFACE_RAISED);
        list.setForeground(DemoTheme.TEXT);
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setPreferredSize(new Dimension(260, 145));
        listScroll.setBorder(BorderFactory.createLineBorder(DemoTheme.BORDER));
        JLabel note = new JLabel("AWT scrolls; JNiceTouch only logs metadata here.");
        note.setForeground(DemoTheme.TEXT_MUTED);
        note.setFont(note.getFont().deriveFont(10f));
        awt.add(awtTitle, BorderLayout.NORTH);
        awt.add(listScroll, BorderLayout.CENTER);
        awt.add(note, BorderLayout.SOUTH);

        panel.add(haptics, BorderLayout.NORTH);
        panel.add(awt, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createEventLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(DemoTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JLabel title = new JLabel("Batched event stream");
        DemoTheme.title(title);
        eventCount.setForeground(DemoTheme.TEXT_MUTED);
        eventCount.setFont(eventCount.getFont().deriveFont(11f));
        JPanel labels = new JPanel(new GridLayout(0, 1, 0, 1));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(eventCount);

        JButton clear = new JButton("Clear");
        clear.setName("Clear event log");
        clear.addActionListener(event -> clearEventLog());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(pauseLog);
        actions.add(clear);
        toolbar.add(labels, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        JTable table = new JTable(eventLog);
        table.setName("Trackpad event log");
        table.setFillsViewportHeight(true);
        table.setRowHeight(23);
        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(DemoTheme.SURFACE);
        table.setForeground(DemoTheme.TEXT);
        table.setGridColor(DemoTheme.BORDER);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(46);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(230);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(DemoTheme.BORDER));
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JMenuBar createMenuBar() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem openImageMenu = new JMenuItem("Open Image…");
        openImageMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
        openImageMenu.addActionListener(event -> chooseSurfaceImage());
        file.add(openImageMenu);
        file.add(clearImageMenu);
        file.addSeparator();
        JMenuItem close = new JMenuItem("Close");
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut));
        close.addActionListener(event -> shutdown());
        file.add(close);

        JMenu view = new JMenu("View");
        JMenuItem toggleMapping = new JMenuItem("Toggle Scroll Mapping");
        toggleMapping.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
        toggleMapping.addActionListener(event -> scrollMapping.doClick());
        JMenuItem reset = new JMenuItem("Reset Playground");
        reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcut));
        reset.addActionListener(event -> gestureCanvas.resetView());
        JMenuItem clear = new JMenuItem("Clear Event Log");
        clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcut));
        clear.addActionListener(event -> clearEventLog());
        view.add(toggleMapping);
        view.add(reset);
        view.add(clear);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About JNiceTouch Swing Demo");
        about.addActionListener(event -> JOptionPane.showMessageDialog(
                this,
                "JNiceTouch Swing Demo\nAppKit → C ABI v1 → FFM → Swing EDT",
                "About",
                JOptionPane.INFORMATION_MESSAGE));
        help.add(about);
        bar.add(file);
        bar.add(view);
        bar.add(help);
        return bar;
    }

    private void configureImageActions() {
        openImage.setName("Open playground image");
        openImage.addActionListener(event -> chooseSurfaceImage());
        clearImageMenu.setEnabled(false);
        clearImageMenu.addActionListener(event -> clearSurfaceImage());
    }

    private void chooseSurfaceImage() {
        if (imageDialog == null) {
            Path downloads = Path.of(System.getProperty("user.home"), "Downloads");
            imageDialog = new FileDialog(this, "Open playground image", FileDialog.LOAD);
            imageDialog.setDirectory(downloads.toString());
            imageDialog.setFilenameFilter((directory, name) -> isImageFilename(name));
        }
        imageDialog.setVisible(true);
        if (imageDialog.getFile() != null) {
            loadSurfaceImage(Path.of(imageDialog.getDirectory(), imageDialog.getFile()));
        }
    }

    private static boolean isImageFilename(String name) {
        String lowercase = name.toLowerCase(Locale.ROOT);
        return lowercase.endsWith(".jpg")
                || lowercase.endsWith(".jpeg")
                || lowercase.endsWith(".png")
                || lowercase.endsWith(".gif")
                || lowercase.endsWith(".bmp")
                || lowercase.endsWith(".wbmp");
    }

    private void loadSurfaceImage(Path path) {
        openImage.setEnabled(false);
        playgroundHint.setText("loading " + path.getFileName() + "…");
        imageLoadWorker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return SurfaceImageLoader.load(path);
            }

            @Override
            protected void done() {
                if (isCancelled() || shuttingDown) {
                    return;
                }
                try {
                    BufferedImage image = get();
                    gestureCanvas.setSurfaceImage(image, path.getFileName().toString());
                    clearImageMenu.setEnabled(true);
                    playgroundHint.setText(String.format(
                            Locale.ROOT,
                            "%s · decoded %,d × %,d",
                            path.getFileName(),
                            image.getWidth(),
                            image.getHeight()));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showImageError(path, exception);
                } catch (ExecutionException exception) {
                    showImageError(path, exception.getCause());
                } finally {
                    openImage.setEnabled(true);
                    imageLoadWorker = null;
                }
            }
        };
        imageLoadWorker.execute();
    }

    private void clearSurfaceImage() {
        if (imageLoadWorker != null) {
            imageLoadWorker.cancel(true);
            imageLoadWorker = null;
        }
        gestureCanvas.clearSurfaceImage();
        playgroundHint.setText(DEFAULT_PLAYGROUND_HINT);
        openImage.setEnabled(true);
        clearImageMenu.setEnabled(false);
    }

    private void showImageError(Path path, Throwable exception) {
        playgroundHint.setText(DEFAULT_PLAYGROUND_HINT);
        JOptionPane.showMessageDialog(
                this,
                "Could not open " + path + ":\n" + exception.getMessage(),
                "Image loading failed",
                JOptionPane.ERROR_MESSAGE);
    }

    private void configureHapticButtons() {
        genericHaptic.setEnabled(false);
        alignmentHaptic.setEnabled(false);
        levelHaptic.setEnabled(false);
        hapticTime.setEnabled(false);
        genericHaptic.setName("Generic haptic feedback");
        alignmentHaptic.setName("Alignment haptic feedback");
        levelHaptic.setName("Level change haptic feedback");
        genericHaptic.addActionListener(event -> performHaptic(HapticFeedbackPattern.GENERIC));
        alignmentHaptic.addActionListener(event -> performHaptic(HapticFeedbackPattern.ALIGNMENT));
        levelHaptic.addActionListener(event -> performHaptic(HapticFeedbackPattern.LEVEL_CHANGE));
    }

    private void performHaptic(HapticFeedbackPattern pattern) {
        if (provider == null) {
            return;
        }
        try {
            HapticPerformanceTime performanceTime =
                    (HapticPerformanceTime) hapticTime.getSelectedItem();
            provider.performHapticFeedback(pattern, performanceTime);
            capabilityDashboard.recordHaptic();
            setProviderStatus(
                    "Haptic requested · " + pattern + " · " + performanceTime,
                    DemoTheme.ACCENT);
        } catch (RuntimeException exception) {
            setProviderStatus("Haptic failed · " + exception.getMessage(), DemoTheme.ERROR);
        }
    }

    private void trackpadEvent(Component target, MacTrackpadEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("JNiceTouch listener was not called on the EDT");
        }
        capabilityDashboard.record(event.kind());
        ++capturedEvents;
        if (!pauseLog.isSelected()) {
            eventLog.add(target, event);
        }
        updateEventCount();
        eventDetails.setText(describe(target, event));

        if (target == gestureCanvas || SwingUtilities.isDescendingFrom(target, gestureCanvas)) {
            gestureState.apply(event);
            if (event.kind() == io.github.mevanlc.jnicetouch.EventKind.QUICK_LOOK) {
                gestureCanvas.flashQuickLook();
            }
            gestureCanvas.repaint();
        }
    }

    private void updateDroppedCount() {
        if (provider == null || !provider.isOpen()) {
            return;
        }
        try {
            long dropped = provider.droppedNativeEventCount();
            droppedStatus.setText("Queue drops  " + dropped);
            droppedStatus.setForeground(dropped == 0 ? DemoTheme.TEXT_MUTED : DemoTheme.WARNING);
        } catch (RuntimeException exception) {
            setProviderStatus("Provider error · " + exception.getMessage(), DemoTheme.ERROR);
        }
    }

    private String describe(Component target, MacTrackpadEvent event) {
        return String.format(
                Locale.ROOT,
                "kind                 %s%n"
                        + "physical phase       %s  [0x%X]%n"
                        + "momentum phase       %s  [0x%X]%n"
                        + "sequence             %d%n"
                        + "pinned target        %s%n"
                        + "EDT callback         %s%n"
                        + "timestamp            %,d ns%n"
                        + "native window        %d%n"
                        + "screen               %.2f, %.2f%n"
                        + "delta                %+.4f, %+.4f%n"
                        + "magnification        %+.5f%n"
                        + "rotation             %+.3f°%n"
                        + "pressure             %.4f%n"
                        + "pressure stage       %d%n"
                        + "stage transition     %+.4f%n"
                        + "pressure behavior    %s  [%d]%n"
                        + "modifiers            0x%X%n"
                        + "flags                %s  [0x%X]",
                event.kind(),
                names(event.phases()),
                event.nativePhaseBits(),
                names(event.momentumPhases()),
                event.nativeMomentumPhaseBits(),
                event.sequenceId(),
                targetName(target),
                SwingUtilities.isEventDispatchThread(),
                event.timestampNanos(),
                event.nativeWindowNumber(),
                event.screenX(),
                event.screenY(),
                event.deltaX(),
                event.deltaY(),
                event.magnification(),
                event.rotationDegrees(),
                event.pressure(),
                event.pressureStage(),
                event.stageTransition(),
                event.pressureBehavior(),
                event.nativePressureBehavior(),
                event.modifiers(),
                names(event.flags()),
                event.nativeFlags());
    }

    private void showProviderError(String message) {
        capabilityDashboard.setSupported(Set.of());
        setProviderStatus("Disconnected · " + message, DemoTheme.ERROR);
        eventDetails.setText("The demo UI is running without native input.\n\n"
                + message + "\n\n"
                + "Launch through ./gradlew run or the packaged .app so the FFM "
                + "native-access option is present.");
    }

    private void clearEventLog() {
        eventLog.clear();
        updateEventCount();
    }

    private void updateEventCount() {
        eventCount.setText(capturedEvents + " captured · " + eventLog.getRowCount() + " shown");
    }

    private void setProviderStatus(String text, Color color) {
        providerStatus.setText(text);
        providerStatus.setForeground(color);
        providerStatus.getAccessibleContext().setAccessibleDescription(text);
    }

    private void shutdown() {
        if (shuttingDown) {
            return;
        }
        shuttingDown = true;
        droppedTimer.stop();
        if (imageLoadWorker != null) {
            imageLoadWorker.cancel(true);
        }
        if (subscription != null) {
            subscription.close();
        }
        if (provider != null) {
            provider.close();
        }
        dispose();
    }

    private static JPanel sectionPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(DemoTheme.SURFACE);
        panel.setBorder(DemoTheme.panelBorder());
        return panel;
    }

    private static String targetName(Component target) {
        return target.getName() == null || target.getName().isBlank()
                ? target.getClass().getSimpleName()
                : target.getName();
    }

    private static String names(Set<?> values) {
        return values.isEmpty()
                ? "none"
                : values.stream().map(Object::toString).sorted().collect(Collectors.joining(" + "));
    }

    private static final class HeaderPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        HeaderPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setPaint(new GradientPaint(
                        0, 0, new Color(0x202B38),
                        getWidth(), 0, new Color(0x152420)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(DemoTheme.BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            } finally {
                g.dispose();
            }
            super.paintComponent(graphics);
        }
    }
}
