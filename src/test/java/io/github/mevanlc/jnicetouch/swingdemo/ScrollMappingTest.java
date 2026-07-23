package io.github.mevanlc.jnicetouch.swingdemo;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class ScrollMappingTest {
    @Test
    public void cyclesPixelmatorThenReversedThenRaw() {
        assertEquals(ScrollMapping.PIXELMATOR.next(), ScrollMapping.REVERSED);
        assertEquals(ScrollMapping.REVERSED.next(), ScrollMapping.RAW);
        assertEquals(ScrollMapping.RAW.next(), ScrollMapping.PIXELMATOR);
    }
}
