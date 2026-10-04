package io.github.mevanlc.jnicetouch.swingdemo;

import io.github.mevanlc.jnicetouch.ScrollDeltaMapping;

final class ScrollMappingUi {
    private ScrollMappingUi() {}

    static String buttonText(ScrollDeltaMapping mapping) {
        return "Mapping " + shortText(mapping);
    }

    static String shortText(ScrollDeltaMapping mapping) {
        return switch (mapping) {
            case CANVAS -> "C · Canvas";
            case REVERSED -> "A · reversed";
            case RAW_APPKIT -> "B · raw";
        };
    }

    static ScrollDeltaMapping next(ScrollDeltaMapping mapping) {
        return switch (mapping) {
            case CANVAS -> ScrollDeltaMapping.REVERSED;
            case REVERSED -> ScrollDeltaMapping.RAW_APPKIT;
            case RAW_APPKIT -> ScrollDeltaMapping.CANVAS;
        };
    }
}
