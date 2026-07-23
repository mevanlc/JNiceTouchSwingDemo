package io.github.mevanlc.jnicetouch.swingdemo;

enum ScrollMapping {
    PIXELMATOR("Mapping C · Pixelmator", "C · Pixelmator", 1.0, -1.0),
    REVERSED("Mapping A · reversed", "A · reversed", -1.0, -1.0),
    RAW("Mapping B · raw", "B · raw", 1.0, 1.0);

    private final String buttonText;
    private final String shortText;
    private final double xMultiplier;
    private final double yMultiplier;

    ScrollMapping(
            String buttonText,
            String shortText,
            double xMultiplier,
            double yMultiplier) {
        this.buttonText = buttonText;
        this.shortText = shortText;
        this.xMultiplier = xMultiplier;
        this.yMultiplier = yMultiplier;
    }

    String buttonText() {
        return buttonText;
    }

    String shortText() {
        return shortText;
    }

    double stateDeltaX(double appKitDeltaX) {
        return xMultiplier * appKitDeltaX;
    }

    double stateDeltaY(double appKitDeltaY) {
        return yMultiplier * appKitDeltaY;
    }

    ScrollMapping next() {
        ScrollMapping[] mappings = values();
        return mappings[(ordinal() + 1) % mappings.length];
    }
}
