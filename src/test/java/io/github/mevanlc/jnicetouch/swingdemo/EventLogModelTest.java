package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.github.mevanlc.jnicetouch.EventFlag;
import io.github.mevanlc.jnicetouch.EventKind;
import io.github.mevanlc.jnicetouch.EventPhase;
import java.util.Set;
import javax.swing.JPanel;
import org.testng.annotations.Test;

public class EventLogModelTest {
    @Test
    public void formatsLifecycleValuesTargetAndFlags() {
        EventLogModel model = new EventLogModel();
        JPanel target = new JPanel();
        target.setName("Pinned canvas");

        model.add(target, TestEvents.event(
                EventKind.SCROLL,
                1.25,
                -2.5,
                0,
                0,
                0,
                0,
                0,
                Set.of(EventPhase.BEGAN),
                Set.of(EventPhase.CHANGED),
                Set.of(EventFlag.PRECISE_SCROLL, EventFlag.DIRECTION_INVERTED_FROM_DEVICE),
                77));

        assertEquals(model.getValueAt(0, 1), "Scroll");
        assertEquals(model.getValueAt(0, 2), "began · momentum changed");
        assertEquals(model.getValueAt(0, 3), "Δ 1.25, -2.50");
        assertEquals(model.getValueAt(0, 4), "Pinned canvas");
        assertEquals(model.getValueAt(0, 5), 77L);
        assertTrue(model.getValueAt(0, 6).toString().contains("Precise scroll"));
        assertTrue(model.getValueAt(0, 6).toString().contains("Direction inverted from device"));
    }

    @Test
    public void retainsOnlyTheNewestThreeHundredRows() {
        EventLogModel model = new EventLogModel();
        JPanel target = new JPanel();

        for (int index = 0; index < 305; ++index) {
            model.add(target, TestEvents.event(EventKind.MAGNIFY));
        }

        assertEquals(model.getRowCount(), 300);
        assertEquals(model.totalEvents(), 305L);
        assertEquals(model.getValueAt(0, 0), 305L);
        assertEquals(model.getValueAt(299, 0), 6L);

        model.clear();
        assertEquals(model.getRowCount(), 0);
        assertEquals(model.totalEvents(), 305L);
    }
}
