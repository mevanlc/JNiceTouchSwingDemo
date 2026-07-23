package io.github.mevanlc.jnicetouch.swingdemo;

import io.github.mevanlc.jnicetouch.EventKind;
import io.github.mevanlc.jnicetouch.MacTrackpadEvent;

final class GestureState {
    static final double MIN_SCALE = 0.45;
    static final double MAX_SCALE = 2.8;
    static final double IMAGE_MIN_SCALE = 0.05;
    static final double IMAGE_MAX_SCALE = 64.0;
    private static final double MAX_PAN = 320.0;
    private static final double IMAGE_MAX_PAN = 100_000.0;

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
    private boolean reverseScrollDeltas = true;
    private double lastRawScrollX;
    private double lastRawScrollY;
    private double lastScreenScrollX;
    private double lastScreenScrollY;
    private boolean hasScrollSample;
    private boolean imageMode;

    void apply(MacTrackpadEvent event) {
        sequenceId = event.sequenceId();
        lastKind = event.kind();
        switch (event.kind()) {
            case SCROLL -> applyScroll(event);
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

    private void applyScroll(MacTrackpadEvent event) {
        double mapping = reverseScrollDeltas ? -1.0 : 1.0;
        double stateDeltaX = mapping * event.deltaX();
        double stateDeltaY = mapping * event.deltaY();
        panBy(stateDeltaX, stateDeltaY);
        lastRawScrollX = event.deltaX();
        lastRawScrollY = event.deltaY();
        lastScreenScrollX = stateDeltaX;
        lastScreenScrollY = -stateDeltaY;
        hasScrollSample = true;
    }

    void setReverseScrollDeltas(boolean reverseScrollDeltas) {
        this.reverseScrollDeltas = reverseScrollDeltas;
    }

    void panBy(double deltaX, double deltaY) {
        double limit = imageMode ? IMAGE_MAX_PAN : MAX_PAN;
        panX = clamp(panX + deltaX, -limit, limit);
        panY = clamp(panY + deltaY, -limit, limit);
    }

    void scaleBy(double factor) {
        double minimum = imageMode ? IMAGE_MIN_SCALE : MIN_SCALE;
        double maximum = imageMode ? IMAGE_MAX_SCALE : MAX_SCALE;
        scale = clamp(scale * Math.max(0.05, factor), minimum, maximum);
    }

    void setImageMode(boolean imageMode) {
        this.imageMode = imageMode;
        reset();
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
        lastRawScrollX = 0.0;
        lastRawScrollY = 0.0;
        lastScreenScrollX = 0.0;
        lastScreenScrollY = 0.0;
        hasScrollSample = false;
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

    boolean reverseScrollDeltas() {
        return reverseScrollDeltas;
    }

    boolean hasScrollSample() {
        return hasScrollSample;
    }

    double lastRawScrollX() {
        return lastRawScrollX;
    }

    double lastRawScrollY() {
        return lastRawScrollY;
    }

    double lastScreenScrollX() {
        return lastScreenScrollX;
    }

    double lastScreenScrollY() {
        return lastScreenScrollY;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
