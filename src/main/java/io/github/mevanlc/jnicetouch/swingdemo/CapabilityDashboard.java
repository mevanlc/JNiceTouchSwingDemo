package io.github.mevanlc.jnicetouch.swingdemo;

import io.github.mevanlc.jnicetouch.Capability;
import io.github.mevanlc.jnicetouch.EventKind;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
final class CapabilityDashboard extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Map<Capability, CapabilityCard> cards =
            new EnumMap<>(Capability.class);

    CapabilityDashboard() {
        super(new GridLayout(0, 2, 8, 8));
        setOpaque(false);
        for (Capability capability : Capability.values()) {
            CapabilityCard card = new CapabilityCard(capability);
            cards.put(capability, card);
            add(card);
        }
    }

    void setSupported(Set<Capability> capabilities) {
        cards.forEach((capability, card) -> card.setSupported(
                capabilities.contains(capability)));
    }

    void record(EventKind kind) {
        Capability capability = switch (kind) {
            case SCROLL -> Capability.PRECISE_SCROLL;
            case MAGNIFY -> Capability.MAGNIFY;
            case ROTATE -> Capability.ROTATE;
            case SWIPE -> Capability.SWIPE;
            case SMART_MAGNIFY -> Capability.SMART_MAGNIFY;
            case PRESSURE -> Capability.PRESSURE;
            case QUICK_LOOK -> Capability.QUICK_LOOK;
            case CANCEL -> null;
        };
        if (capability != null) {
            cards.get(capability).record();
        }
    }

    void recordHaptic() {
        cards.get(Capability.HAPTIC_FEEDBACK).record();
    }

    private static final class CapabilityCard extends JPanel {
        private static final long serialVersionUID = 1L;

        private final JLabel stateLabel = new JLabel("Not connected");
        private int count;
        private boolean supported;

        CapabilityCard(Capability capability) {
            super(new BorderLayout(4, 3));
            setOpaque(true);
            setBackground(DemoTheme.SURFACE_RAISED);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DemoTheme.BORDER),
                    BorderFactory.createEmptyBorder(8, 9, 8, 9)));
            String title = prettyName(capability);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(DemoTheme.TEXT);
            titleLabel.setFont(titleLabel.getFont().deriveFont(12f));
            stateLabel.setForeground(DemoTheme.TEXT_MUTED);
            stateLabel.setFont(stateLabel.getFont().deriveFont(10f));
            add(titleLabel, BorderLayout.NORTH);
            add(stateLabel, BorderLayout.SOUTH);
            getAccessibleContext().setAccessibleName(title + " capability");
        }

        void setSupported(boolean supported) {
            this.supported = supported;
            setBackground(supported ? new Color(0x20332F) : DemoTheme.SURFACE_RAISED);
            stateLabel.setForeground(supported ? DemoTheme.SUCCESS : DemoTheme.TEXT_MUTED);
            updateLabel();
        }

        void record() {
            ++count;
            updateLabel();
        }

        private void updateLabel() {
            if (!supported) {
                stateLabel.setText("Unavailable");
            } else if (count == 0) {
                stateLabel.setText("Ready · no events yet");
            } else {
                stateLabel.setText(count + (count == 1 ? " event" : " events"));
            }
        }

        private static String prettyName(Capability capability) {
            return switch (capability) {
                case PRECISE_SCROLL -> "Precise scroll";
                case MAGNIFY -> "Magnify";
                case ROTATE -> "Rotate";
                case SWIPE -> "Swipe";
                case SMART_MAGNIFY -> "Smart magnify";
                case PRESSURE -> "Pressure";
                case QUICK_LOOK -> "Quick Look";
                case HAPTIC_FEEDBACK -> "Haptic feedback";
            };
        }
    }
}
