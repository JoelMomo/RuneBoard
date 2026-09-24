# Prototype 1 results

## P1-A — keyboard chrome and visual hierarchy

Status: **PASS**

Version checkpoint:

- `0.18.0-prototype`
- `versionCode 21`

Implemented:

- dedicated header chrome surface;
- quieter unselected command row;
- primary emphasis for letters, numbers and Space;
- restrained edge definition for typing keys;
- stronger selected-key halo;
- reduced prominence for unselected controller hints.

Compatibility constraints preserved:

- no row or key geometry change;
- no `KeyboardEngine` change;
- no `ControllerMapper` change;
- no joystick or D-pad semantic change;
- no IME/window semantic change.

### Emulator QA

Reference environment:

- Android 15 / API 35;
- `1240x1080`;
- 320 dpi.

Successful GitHub Actions evidence:

- run: `35523489604`;
- head: `d62087837fbe6f87660e8bbdc6bb1895e1e7d669`;
- visual QA job: PASS.

Input-method evidence from the captured run:

- `default_input_method=io.github.joelmomo.runeboard/.RuneBoardImeService`;
- `mCurMethodId=io.github.joelmomo.runeboard/.RuneBoardImeService`;
- `mCurImeId=io.github.joelmomo.runeboard/.RuneBoardImeService`;
- `mInputShown=true`;
- RuneBoard service reported `mWindowVisible=true`.

Visual review of the `1240x1080` capture confirmed:

- header reads as a distinct chrome layer;
- command controls remain readable without competing with typing rows;
- letter/number labels dominate controller metadata;
- selected `Q` key is immediately identifiable through fill, stroke and halo;
- thumb row remains aligned to the existing validated geometry.

The temporary workflow used only to capture P1-A emulator evidence was removed before the final product checkpoint.

### Physical-device scope

P1-A does not change display placement, controller behavior, transparency semantics or IME/window behavior. No new Thor physical validation is required for this slice. The existing Prototype 0 hardware results remain the physical baseline.

## P1-B - setup and settings UX

Status: **PASS**

Version checkpoint:

- `0.19.0-prototype`
- `versionCode 22`

Implemented:

- removed the `PROTOTYPE` pill from the main header;
- replaced the three equal Setup cards with three ordered setup rows;
- added live `READY` / `SET UP` status for RuneBoard enablement, active IME selection and physical controls;
- refreshes setup state after returning from Android settings;
- simplified settings section wording and hierarchy;
- preserved the built-in keyboard test field;
- collapsed Custom style controls unless the Custom theme is selected.

Compatibility constraints preserved:

- no `KeyboardEngine` change;
- no `ControllerMapper` change;
- no joystick or D-pad semantic change;
- no IME/window semantic change;
- no change to stored theme, typing or controller preferences.

### Emulator QA

Reference environment:

- Android 15 / API 35;
- `1240x1080`;
- 320 dpi.

Successful GitHub Actions evidence:

- final setup/settings QA run: `35540090834`;
- emulator QA job: PASS.

The emulator verified:

- fresh setup view exposes exactly three `SET UP` states;
- enabling and selecting RuneBoard changes the first two steps to `READY`;
- enabling `RuneBoardControlService` changes all three steps to `READY`;
- Custom style controls are absent while a built-in theme is active;
- selecting the Custom theme reveals the Custom style controls.

Visual review confirmed:

- the header is cleaner without the prototype pill;
- setup reads as a clear ordered onboarding sequence;
- status chips are easy to distinguish without dominating the page;
- built-in themes expose the next settings sections without the full Custom editor adding noise;
- selecting Custom reveals the existing customization controls without changing their behavior.

The temporary emulator workflow used to capture P1-B evidence is removed before the final product checkpoint.

### Physical-device scope

P1-B is an ordinary setup/settings UI change. It does not change Thor display placement, controller semantics or IME behavior, so no new Thor physical validation is required for this slice.

## P1-C - theme consistency

Status: **PASS**

Version checkpoint:

- `0.20.0-prototype`
- `versionCode 23`

Implemented:

- added `selectedContent` to `KeyboardTheme`;
- derive selected content from selection-fill luminance so light selections use dark content and dark selections use light content;
- selected and active key labels use the same selected-content token;
- selected physical-button hints use selected content;
- unselected physical-button hints use secondary text with higher readable alpha;
- recommended suggestions reuse the same selected-state content model;
- theme customization remains data-driven through `KeyboardTheme` and `CustomThemeConfig`.

Compatibility constraints preserved:

- no row or key geometry change;
- no `KeyboardEngine` change;
- no `ControllerMapper` change;
- no joystick or D-pad semantic change;
- no IME/window semantic change;
- background opacity remains independent from key opacity.

### Unit and build validation

Standard Android CI:

- run: `35951279096`;
- tests, lint and debug APK assembly: PASS.

Theme tests require selected-content contrast of at least 4.5:1 for:

- RuneBoard Default;
- OLED Black;
- Transparent;
- Custom Purple;
- Custom Cyan;
- Custom Green;
- Custom Amber;
- Custom Pink.

### Emulator matrix

Reference environment:

- Android 15 / API 35;
- `1240x1080`;
- 320 dpi.

Successful matrix evidence:

- run: `35951620626`;
- artifact: `p1c-theme-opacity-matrix`;
- 16 captures: Default / OLED / Transparent / Custom x 100% / 67% / 33% / 0%;
- RuneBoard confirmed as the active and visible IME for every captured case.

Visual review confirmed:

- all four themes preserve the same hierarchy and interaction states;
- selected keys remain the dominant transient state;
- Default and Transparent correctly use dark content on the brighter violet selection;
- OLED correctly uses light content on its darker violet selection;
- Custom Amber uses dark selected content and remains clearly legible;
- controller hints remain visible without competing with primary labels;
- 0% background remains usable over underlying app content.

Representative pixel checks across opacity levels showed regular-key and selected-key fills varying by at most about one RGB level while the sampled background changed substantially, confirming that the opacity control continues to affect the background rather than the key hierarchy.

The temporary matrix script and workflow were removed before the final product checkpoint.

### Physical-device scope

P1-C changes theme/rendering contrast only. It does not affect lower-display placement, physical navigation, controller capture or IME/window behavior, so no new Thor physical validation is required for this slice.
