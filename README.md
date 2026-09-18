# RuneBoard

[![Android CI](https://github.com/JoelMomo/RuneBoard/actions/workflows/android.yml/badge.svg)](https://github.com/JoelMomo/RuneBoard/actions/workflows/android.yml) [![Apps & tools](https://img.shields.io/badge/Apps%20%26%20tools-Browse-6F8F72?style=flat-square)](https://joelmomo.github.io/)

RuneBoard is an experimental dual-screen Android keyboard designed primarily for the **AYN Thor**.

The project is currently in **Prototype 0**. The Thor-specific architecture has been validated on physical hardware and is now being refactored into a modular product foundation.

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

Prototype 0 contains:

- a real Android `InputMethodService`;
- a touch QWERTY keyboard with an always-visible number row;
- a narrowly scoped accessibility service for physical controller buttons;
- a separate keyboard model, state engine and controller mapper;
- geometry-aware D-pad navigation across rows with different key widths;
- L1/R1 cursor movement through the active `InputConnection`;
- four keyboard opacity levels;
- persistent background opacity across IME recreation;
- built-in theme profiles: Default, OLED Black and Transparent;
- compact/minimized mode;
- a setup/test activity;
- unit tests for state, controller mappings and minimized capture policy;
- GitHub Actions running unit tests, lint and APK assembly;
- a dedicated Android emulator for non-Thor regression testing.

There are no third-party runtime dependencies.

## Architecture

The product code is split into independent layers:

- `keyboard/`: layout, key model, state and keyboard engine;
- `controller/`: controller actions and Android key-code mapping;
- `theme/`: visual profiles and supported background-opacity levels;
- `settings/`: persistent user preferences;
- `RuneKeyboardView`: rendering, touch hit testing and motion-event adapter;
- `RuneBoardImeService`: Android IME and `InputConnection` adapter;
- `RuneBoardControlService`: physical-button filtering while RuneBoard is active.

See `docs/ARCHITECTURE.md`.

## Build

Current development version: **0.0.5-prototype**

Current minimum Android version: **Android 13 / API 33**.

GitHub Actions runs:

1. unit tests;
2. Android lint;
3. debug APK assembly.

The successful workflow exposes `RuneBoard-prototype-debug` as an artifact.

## Hardware validation

Prototype 0 was tested on an AYN Thor running Android 13 / firmware `.377`.

Validated on the physical device:

- AYN pins RuneBoard to the lower display while the editor remains on the upper display;
- touch input on the lower display writes into the upper-display editor;
- D-pad and A/B/X/Y controller typing;
- L1/R1 text-cursor movement;
- minimize/restore;
- opacity-state switching.

Measured lower-display keyboard sizes during the prototype:

- full: approximately `1240x595` px;
- compact: approximately `1240x134` px.

The lower panel is `1080x1240` native and `1240x1080` in landscape.

See:

- `docs/PROTOTYPE_0_TEST_PLAN.md`
- `docs/PROTOTYPE_0_RESULTS.md`
- `docs/EMULATOR_TESTING.md`

## Emulator limitation

A dedicated Android 15 emulator can simulate a second `1240x1080` display and RuneBoard activities can run on it.

Stock Android still places the IME on display 0 even when the focused editor is on display 2. The emulator therefore cannot reproduce AYN's special cross-display IME policy.

Thor-specific display placement remains a real-hardware test.

## Scope

Prototype 0 intentionally does not include swipe typing, cloud prediction, accounts, telemetry or the final visual/theme system.

RuneBoard is Thor-first. General Android support can be evaluated later.

## Status

**Build, unit tests and lint: passing.**

**Core Thor architecture: validated.**

**RuneBoard Default visual system: implemented in emulator.**

**Final visual tuning on the physical Thor panel: pending.**

Experimental â€” not ready for daily use.

## Contributing

Bug reports, feature ideas and focused pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) before opening one.

For security or privacy-sensitive reports, follow [SECURITY.md](SECURITY.md) instead of posting details publicly.

<div align="center">

## Support development

These projects are free to use and developed in my spare time. If they've been useful to you, you can help support future development.

<p>
  <a href="https://github.com/sponsors/JoelMomo">
    <img src="https://img.shields.io/badge/GitHub%20Sponsors-Sponsor-EA4AAA?style=for-the-badge&logo=githubsponsors&logoColor=white" alt="Sponsor on GitHub">
  </a>
  <a href="https://ko-fi.com/joelmomodev">
    <img src="https://img.shields.io/badge/Ko--fi-One--time%20tip-FF5E5B?style=for-the-badge&logo=kofi&logoColor=white" alt="Leave a tip on Ko-fi">
  </a>
</p>

<sub>All projects remain free regardless of support.</sub>

</div>
