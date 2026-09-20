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
