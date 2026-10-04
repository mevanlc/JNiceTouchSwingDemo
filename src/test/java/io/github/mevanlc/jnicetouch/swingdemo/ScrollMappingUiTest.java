package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertEquals;

import io.github.mevanlc.jnicetouch.ScrollDeltaMapping;
import org.testng.annotations.Test;

public class ScrollMappingUiTest {
    @Test
    public void cyclesCanvasThenReversedThenRaw() {
        assertEquals(
                ScrollMappingUi.next(ScrollDeltaMapping.CANVAS),
                ScrollDeltaMapping.REVERSED);
        assertEquals(
                ScrollMappingUi.next(ScrollDeltaMapping.REVERSED),
                ScrollDeltaMapping.RAW_APPKIT);
        assertEquals(
                ScrollMappingUi.next(ScrollDeltaMapping.RAW_APPKIT),
                ScrollDeltaMapping.CANVAS);
    }
}
