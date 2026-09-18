# RuneBoard

RuneBoard is an experimental dual-screen Android keyboard designed primarily for the **AYN Thor**.

The project is currently in **Prototype 0 / feasibility testing**. The goal is to prove the Thor-specific interaction model before investing in the final visual design.

## Prototype goals

The first prototype must prove four things on real hardware:

1. RuneBoard works as a normal Android IME and can be pinned by the Thor to the lower display.
2. The keyboard can be operated with the Thor's physical controls as well as touch.
3. The keyboard surface can become translucent so the lower-screen app remains visible.
4. RuneBoard can collapse into a compact bar and restore without disabling the IME.

## Prototype controls

- D-pad / left stick: move keyboard selection
- A: press selected key
- B: backspace
- X: space
- Y: shift
- L1 / R1: move text cursor left / right
- Start: enter
- Select: minimize
- Touch: direct key input

Controller mappings are intentionally provisional until tested on AYN Thor hardware.

## Current implementation

Prototype 0 already contains:

- a real Android `InputMethodService`;
- a touch QWERTY keyboard with an always-visible number row;
- gamepad/D-pad navigation and provisional Thor button mappings;
- four keyboard opacity levels;
- compact/minimized mode;
- a setup/test activity;
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

The cloud build validates the Android project, but the Thor-specific behavior must be tested on real hardware.

See:

- `docs/PROTOTYPE_0_TEST_PLAN.md`
- GitHub issue **#1 — Prototype 0: validate Thor hardware gates**

## Status

**Build: passing.**

**Thor hardware gates: pending.**

Experimental — not ready for daily use.
