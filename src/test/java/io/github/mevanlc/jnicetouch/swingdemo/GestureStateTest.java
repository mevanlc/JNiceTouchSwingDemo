package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertEquals;

import io.github.mevanlc.jnicetouch.EventKind;
import java.util.Set;
import org.testng.annotations.Test;

public class GestureStateTest {
    @Test
    public void appliesTransformGesturesUsingIncrementalNativeValues() {
        GestureState state = new GestureState();

        state.apply(TestEvents.event(
                EventKind.SCROLL, 14.5, -8.25, 0, 0, 0, 0, 0,
                Set.of(), Set.of(), Set.of(), 21));
        state.apply(TestEvents.event(
                EventKind.MAGNIFY, 0, 0, 0.5, 0, 0, 0, 0,
                Set.of(), Set.of(), Set.of(), 22));
        state.apply(TestEvents.event(
                EventKind.ROTATE, 0, 0, 0, 37.5, 0, 0, 0,
                Set.of(), Set.of(), Set.of(), 23));

        assertEquals(state.panX(), 14.5);
        assertEquals(state.panY(), -8.25);
        assertEquals(state.scale(), 1.5);
        assertEquals(state.rotationDegrees(), 37.5);
        assertEquals(state.sequenceId(), 23L);
        assertEquals(state.lastKind(), EventKind.ROTATE);
    }

    @Test
    public void clampsZoomPanAndPressureToVisualBounds() {
        GestureState state = new GestureState();

        state.panBy(10_000, -10_000);
        state.scaleBy(1_000);
        state.apply(TestEvents.event(
                EventKind.PRESSURE, 0, 0, 0, 0, 1.8, 0.4, 2,
                Set.of(), Set.of(), Set.of(), 24));

        assertEquals(state.panX(), 320.0);
        assertEquals(state.panY(), -320.0);
        assertEquals(state.scale(), GestureState.MAX_SCALE);
        assertEquals(state.pressure(), 1.0);
        assertEquals(state.pressureStage(), 2);
        assertEquals(state.stageTransition(), 0.4);
    }

    @Test
    public void handlesDiscreteGesturesAndCancellation() {
        GestureState state = new GestureState();

        state.apply(TestEvents.event(EventKind.SMART_MAGNIFY));
        assertEquals(state.scale(), 1.85);
        state.apply(TestEvents.event(EventKind.SMART_MAGNIFY));
        assertEquals(state.scale(), 1.0);

        state.apply(TestEvents.event(EventKind.QUICK_LOOK));
        assertEquals(state.quickLookGeneration(), 1);
        state.apply(TestEvents.event(
                EventKind.PRESSURE, 0, 0, 0, 0, 0.8, 0.3, 2,
                Set.of(), Set.of(), Set.of(), 25));
        state.apply(TestEvents.event(EventKind.CANCEL));

        assertEquals(state.pressure(), 0.0);
        assertEquals(state.pressureStage(), 0);
        assertEquals(state.stageTransition(), 0.0);
    }
}
