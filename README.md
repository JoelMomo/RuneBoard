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
- Touch: direct key input

Controller mappings are intentionally provisional until tested on AYN Thor hardware.

## Scope

This prototype intentionally does **not** include swipe typing, advanced prediction, cloud services, telemetry, or the final theme/customization system.

RuneBoard is being designed Thor-first. General Android compatibility can be evaluated later.

## Build

The repository contains a minimal Android project using Java and the platform APIs only. GitHub Actions builds a debug APK on each push.

Current prototype target: Android 13+.

## Status

**Experimental — not ready for daily use.**
