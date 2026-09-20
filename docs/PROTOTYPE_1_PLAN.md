# Prototype 1 — product baseline

Prototype 1 starts from the hardware-validated `0.17.3-prototype` baseline. Prototype 0 proved the AYN Thor architecture on physical hardware; Prototype 1 turns that validated foundation into a coherent product without reopening the controller or IME architecture unless a regression requires it.

## Goal

Make RuneBoard visually coherent, easier to understand and suitable for sustained daily-use testing on the AYN Thor while preserving the behavior already validated in Prototype 0.

## Inherited invariants

Prototype 1 treats the following as compatibility constraints:

- RuneBoard remains a standard Android `InputMethodService`.
- AYN firmware remains responsible for placing the IME on the lower display.
- One deliberate left-stick gesture produces one keyboard move.
- D-pad tap moves once; D-pad hold keeps delayed autorepeat.
- The on-screen Enter key inserts a newline; the physical Enter action keeps Android's contextual editor action.
- Remapped controller actions, minimized capture policy and language switching keep their current semantics.
- Background opacity remains `100% / 67% / 33% / 0%`, independent from key opacity.
- No root or overlay permission is introduced for the core keyboard.
- Visual work must not silently change keyboard-engine, controller or text-entry behavior.

## Product scope

Prototype 1 focuses on four areas.

### P1-A — keyboard visual hierarchy

- make the typing surface read immediately as a keyboard rather than a diagnostic prototype;
- keep letters and numbers visually primary;
- make command/editing controls quieter but still discoverable;
- keep controller selection as the strongest transient visual state;
- preserve the validated row geometry and bottom-screen fit unless a change is explicitly tested.

### P1-B — setup and settings UX

- present setup state and required Android permissions clearly;
- group appearance, typing and controller settings by task;
- remove prototype/debug wording where it no longer describes the product;
- keep the built-in test field available for verification.

### P1-C — theme consistency

- Default, OLED Black, Transparent and Custom must share the same hierarchy and interaction states;
- text, selection and physical-button hints must remain readable at every supported background opacity;
- customization stays data-driven through `KeyboardTheme` / `CustomThemeConfig`.

### P1-D — regression gate

Every implementation slice must keep unit tests, lint and debug assembly green. Emulator validation is used for ordinary UI work. Thor-specific behavior is re-tested only when a change can affect display placement, physical controls, transparency or final lower-panel rendering.

## Out of scope for this phase

Prototype 1 does not introduce:

- swipe typing;
- cloud prediction or accounts;
- private clipboard history;
- a new controller architecture;
- a new IME/window architecture;
- Japanese composition;
- broad general-Android support;
- a project/package reorganization solely for aesthetics.

## Visual acceptance rules

A visual change is acceptable only when:

1. the selected key remains immediately identifiable;
2. primary typing labels have stronger hierarchy than controller hints;
3. command-row controls remain readable without competing with the typing rows;
4. all interactive elements retain practical touch targets;
5. 100%, 67%, 33% and 0% background states remain usable;
6. the change does not require behavior changes in `KeyboardEngine` or `ControllerMapper` unless separately justified and tested.

## Implementation order

1. establish the Prototype 1 visual/product baseline;
2. polish keyboard chrome and visual hierarchy without changing input behavior;
3. polish the setup/settings activity;
4. run emulator visual/regression QA;
5. perform final physical-panel review on the Thor when explicitly authorized.

Each step should land as a focused, recoverable change rather than a broad redesign.
