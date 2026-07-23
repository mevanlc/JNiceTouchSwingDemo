package io.github.mevanlc.jnicetouch.swingdemo;

import io.github.mevanlc.jnicetouch.EventKind;
import io.github.mevanlc.jnicetouch.MacTrackpadEvent;

final class GestureState {
    static final double MIN_SCALE = 0.45;
    static final double MAX_SCALE = 2.8;
    private static final double MAX_PAN = 320.0;

    private double panX;
    private double panY;
    private double scale = 1.0;
    private double rotationDegrees;
    private double pressure;
    private double stageTransition;
    private int pressureStage;
    private long sequenceId;
    private EventKind lastKind;
    private double lastSwipeX;
    private double lastSwipeY;
    private int quickLookGeneration;

    void apply(MacTrackpadEvent event) {
        sequenceId = event.sequenceId();
        lastKind = event.kind();
        switch (event.kind()) {
            case SCROLL -> panBy(event.deltaX(), event.deltaY());
            case MAGNIFY -> scaleBy(1.0 + event.magnification());
            case ROTATE -> rotateBy(event.rotationDegrees());
            case SWIPE -> {
                lastSwipeX = event.deltaX();
                lastSwipeY = event.deltaY();
                panBy(event.deltaX() * 55.0, event.deltaY() * 55.0);
            }
            case SMART_MAGNIFY -> scale = scale < 1.4 ? 1.85 : 1.0;
            case PRESSURE -> {
                pressure = clamp(event.pressure(), 0.0, 1.0);
                stageTransition = event.stageTransition();
                pressureStage = event.pressureStage();
            }
            case QUICK_LOOK -> ++quickLookGeneration;
            case CANCEL -> {
                pressure = 0.0;
                stageTransition = 0.0;
                pressureStage = 0;
            }
        }
    }

    void panBy(double deltaX, double deltaY) {
        panX = clamp(panX + deltaX, -MAX_PAN, MAX_PAN);
        panY = clamp(panY + deltaY, -MAX_PAN, MAX_PAN);
    }

    void scaleBy(double factor) {
        scale = clamp(scale * Math.max(0.05, factor), MIN_SCALE, MAX_SCALE);
    }

    void rotateBy(double degrees) {
        rotationDegrees = Math.IEEEremainder(rotationDegrees + degrees, 360.0);
    }

    void reset() {
        panX = 0.0;
        panY = 0.0;
        scale = 1.0;
        rotationDegrees = 0.0;
        pressure = 0.0;
        stageTransition = 0.0;
        pressureStage = 0;
        sequenceId = 0;
        lastKind = null;
        lastSwipeX = 0.0;
        lastSwipeY = 0.0;
        quickLookGeneration = 0;
    }

    double panX() {
        return panX;
    }

    double panY() {
        return panY;
    }

    double scale() {
        return scale;
    }

    double rotationDegrees() {
        return rotationDegrees;
    }

    double pressure() {
        return pressure;
    }

    double stageTransition() {
        return stageTransition;
    }

    int pressureStage() {
        return pressureStage;
    }

    long sequenceId() {
        return sequenceId;
    }

    EventKind lastKind() {
        return lastKind;
    }

    double lastSwipeX() {
        return lastSwipeX;
    }

    double lastSwipeY() {
        return lastSwipeY;
    }

    int quickLookGeneration() {
        return quickLookGeneration;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
