# JNiceTouch Swing Demo

An interactive Swing application and source example for every high-level
capability exposed by [JNiceTouch](../JNiceTouch/README.md).

The demo deliberately shows both sides of trackpad integration:

- a custom gesture playground where native scroll, magnify, rotate, swipe,
  smart-magnify, pressure, and Quick Look events manipulate a visible object
- a standard Swing list where AWT continues to own scrolling while JNiceTouch
  supplies phase and metadata without applying the delta a second time
- haptic controls for Generic, Alignment, and Level Change feedback
- live capability state, pinned component targets, phases, momentum phases,
  sequence IDs, modifiers, flags, native window numbers, and queue drops
- a bounded event table that makes EDT batch delivery directly observable
- an image surface that can be opened from disk and manipulated with the same
  native gestures

## Requirements

- macOS 12 or newer
- JDK 22 or newer for development
- Xcode Command Line Tools
- `rsvg-convert` and `makeicns` for regenerating the shared application icon
- the sibling checkout layout requested for this source example:

```text
JNiceTouch/
JNiceTouchSwingDemo/
```

The Gradle composite build substitutes the local `../JNiceTouch` project for
the declared `io.github.mevanlc:jnicetouch` dependency, so editing the library
and rerunning the demo always uses the current source.

## Run from source

```text
./gradlew run
```

The application plugin supplies `--enable-native-access=ALL-UNNAMED`; no IDE
VM-option setup is required when launching through Gradle.

## Test and package

```text
./gradlew test
./gradlew makeAppIconIcns
./gradlew packageMacApp
./gradlew macDeployToApplications
```

`packageMacApp` uses the maintained `org.panteleyev.jpackageplugin` Gradle
plugin to create a self-contained
`build/jpackage-output/JNiceTouch Swing Demo.app`. The deployment task installs
that bundle at `/Applications/JNiceTouch Swing Demo.app` with the required FFM
native-access option embedded in its launcher configuration.

The checked-in `appicon.svg` is the single icon source. Gradle renders it to
`appicon.png` for Swing windows and the Dock, then runs `makeicns` to create
`appicon.icns` for `jpackage`. Both generated assets can be refreshed with
`./gradlew makeAppIconIcns`.

## Interaction guide

Use two-finger scrolling over the playground to directly pan the card with your
fingers, pinch to scale, and rotate to turn it. Swipe gives the card an
additional nudge, smart magnify toggles between normal and detail scale, pressure
changes the force ring, and Quick Look briefly raises an overlay. The exact
initial target is shown and remains pinned through the gesture and scroll
momentum.

The playground starts in Canvas mode (mapping C): horizontal AppKit deltas are
applied directly and vertical deltas are reversed internally, so screen movement
follows the preference-adjusted AppKit delta on both axes. It uses the library's
`ScrollDeltaMapping.CANVAS` helper rather than implementing the sign translation
locally.
Click the mapping button or press Command-R to cycle C → A (both reversed) → B
(both raw) for comparison. Switching mappings preserves the current pan, zoom,
and rotation. The sample canvas shows each AppKit scroll vector beside the
resulting screen-space movement.

Use **Open image…** in the playground toolbar or press Command-O to replace the
sample card with a JPEG, PNG, GIF, or BMP in the native macOS file picker. Image
mode is an edge-to-edge workspace: reset fits the whole photo on screen, while
pan, rotate, and zoom (up to 64×) operate on the unadorned image. Decoding runs
off the EDT and preserves source detail up to 8192 pixels on the longest side;
larger sources are subsampled while decoding. **File → Clear Image** restores the
default surface.

The playground also has keyboard equivalents so every important action remains
available without a trackpad:

- Arrow keys: pan
- `+` / `-`: zoom
- `[` / `]`: rotate
- `0`: reset

Haptic feedback still requires compatible Apple hardware and may be suppressed
by macOS when the user is not touching the trackpad.
