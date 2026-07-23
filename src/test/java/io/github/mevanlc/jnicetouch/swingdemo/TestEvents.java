package io.github.mevanlc.jnicetouch.swingdemo;

import io.github.mevanlc.jnicetouch.EventFlag;
import io.github.mevanlc.jnicetouch.EventKind;
import io.github.mevanlc.jnicetouch.EventPhase;
import io.github.mevanlc.jnicetouch.MacTrackpadEvent;
import io.github.mevanlc.jnicetouch.PressureBehavior;
import java.util.Set;

final class TestEvents {
    private TestEvents() {}

    static MacTrackpadEvent event(EventKind kind) {
        return event(kind, 0, 0, 0, 0, 0, 0, 0, Set.of(), Set.of(), Set.of(), 17);
    }

    static MacTrackpadEvent event(
            EventKind kind,
            double deltaX,
            double deltaY,
            double magnification,
            double rotation,
            double pressure,
            double stageTransition,
            int pressureStage,
            Set<EventPhase> phases,
            Set<EventPhase> momentumPhases,
            Set<EventFlag> flags,
            long sequenceId) {
        int phaseBits = phaseBits(phases);
        int momentumBits = phaseBits(momentumPhases);
        int flagBits = flagBits(flags);
        return new MacTrackpadEvent(
                kind,
                phases,
                momentumPhases,
                phaseBits,
                momentumBits,
                sequenceId,
                123_456_789,
                0x1_0000,
                7,
                120.5,
                840.25,
                deltaX,
                deltaY,
                magnification,
                rotation,
                pressure,
                stageTransition,
                pressureStage,
                PressureBehavior.PRIMARY_GENERIC,
                2,
                flagBits,
                flags);
    }

    private static int phaseBits(Set<EventPhase> phases) {
        int result = 0;
        for (EventPhase phase : phases) {
            result |= switch (phase) {
                case BEGAN -> 1;
                case STATIONARY -> 1 << 1;
                case CHANGED -> 1 << 2;
                case ENDED -> 1 << 3;
                case CANCELLED -> 1 << 4;
                case MAY_BEGIN -> 1 << 5;
            };
        }
        return result;
    }

    private static int flagBits(Set<EventFlag> flags) {
        int result = 0;
        for (EventFlag flag : flags) {
            result |= switch (flag) {
                case PRECISE_SCROLL -> 1;
                case DIRECTION_INVERTED_FROM_DEVICE -> 1 << 1;
                case QUEUE_OVERFLOW_OCCURRED -> 1 << 2;
            };
        }
        return result;
    }
}
