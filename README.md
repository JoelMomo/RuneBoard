# RuneBoard

RuneBoard is an experimental dual-screen Android keyboard designed primarily for the **AYN Thor**.

The project is currently in **Prototype 0**. The core Thor-specific architecture has been validated on physical hardware; visual/product development comes next.

## Prototype goals

The first prototype must prove four things on real hardware:

1. RuneBoard works as a normal Android IME and can be pinned by the Thor to the lower display.
2. The keyboard can be operated with the Thor's physical controls as well as touch.
3. The keyboard surface can become translucent so the lower-screen app remains visible.
4. RuneBoard can collapse into a compact bar and restore without disabling the IME.

## Prototype controls

- D-pad: move keyboard selection
- A: press selected key
- B: backspace
- X: space
- Y: shift
- L1 / R1: move text cursor left / right
- Start: enter
- Select: minimize
- Touch: direct key input

The DS-style D-pad/button mappings are validated on AYN Thor hardware. Analog-stick navigation remains experimental.

## Current implementation

Prototype 0 already contains:

- a real Android `InputMethodService`;
- a touch QWERTY keyboard with an always-visible number row;
- validated D-pad/button navigation through a narrowly scoped accessibility service;
- L1/R1 text-cursor movement through the active `InputConnection`;
- four keyboard opacity levels;
- compact/minimized mode that the Thor firmware resizes correctly;
- a setup/test activity with shortcuts for IME and Physical Controls settings;
- a Thor hardware test plan;
- GitHub Actions CI that assembles a debug APK successfully.

The Android project has no third-party runtime dependencies at this stage.

## Scope

This prototype intentionally does **not** include swipe typing, advanced prediction, cloud services, telemetry, persistent customization, or the final visual design.

RuneBoard is being designed Thor-first. General Android compatibility can be evaluated later.

## Build

GitHub Actions builds a debug APK on each push and pull request.

Current prototype target: **Android 13+**.

The latest successful workflow exposes the APK as the `RuneBoard-prototype-debug` artifact.

## Hardware validation

Prototype 0 has been tested on an AYN Thor running Android 13 / firmware `.377`.

Validated on the physical device:

- IME pinned by AYN to the lower display while typing into the upper display;
- direct touch input on the lower display;
- D-pad plus A/B/X/Y controller typing;
- L1/R1 text-cursor movement;
- minimize/restore: approximately `1240×595` px full and `1240×134` px compact;
- opacity-state switching while the IME remains active.

The lower panel is `1080×1240` native and `1240×1080` in landscape.

See:

- `docs/PROTOTYPE_0_TEST_PLAN.md`
- `docs/PROTOTYPE_0_RESULTS.md`
- GitHub issue **#1 — Prototype 0: validate Thor hardware gates**

## Status

**Build and lint: passing.**

**Core Thor architecture: validated.**

Transparency is functional but still needs final visual tuning on-panel. Analog-stick navigation is not yet validated.

Experimental — not ready for daily use.
