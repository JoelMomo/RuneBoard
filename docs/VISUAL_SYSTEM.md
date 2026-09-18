# RuneBoard visual system

## RuneBoard Default

The first product theme is designed around the AYN Thor lower display in landscape.

### Layout

- working reference size: 1240x1080;
- always-visible number row;
- staggered QWERTY rows instead of a stretched rectangular grid;
- weighted utility row with a wider space key;
- compact status header;
- controller selection is always visually explicit.

### Palette

RuneBoard Default uses a dark neutral base with a restrained violet accent.

- background: near-black vertical gradient;
- regular keys: dark graphite;
- utility keys: darker graphite;
- focus/selection: violet fill with a pale violet outline;
- primary text: off-white;
- secondary/status text: muted grey-violet.

The palette lives in `theme/KeyboardTheme` and `theme/RuneThemes`, not in the input engine.

### Transparency

Background opacity is independent from key opacity.

Current prototype levels:

- 100%;
- 71%;
- 35%;
- 0%.

At 0% the keyboard surface is transparent while keys, labels and controller selection remain visible.

This is intentional: it allows the lower-screen application to remain readable without making RuneBoard itself unusable.

### Compact mode

The minimized state uses a thin RuneBoard bar containing:

- RuneBoard badge;
- active layout;
- explicit `A RESTORE` controller hint.

### Architecture rule

Visual customization must never modify text-entry or controller behavior directly.

Future themes should be expressible through theme/profile data:

- background colors or image;
- key colors;
- text colors;
- selection colors;
- key radius;
- gaps/margins;
- font choices;
- key/background opacity.

Persistent user customization is the next layer on top of this theme model.


### Persistence

Background opacity is now persisted independently from the theme.

The IME loads the saved opacity before constructing the keyboard view, so recreating or restarting RuneBoard does not reset `BG`.

Built-in theme profiles currently defined in code:

- RuneBoard Default;
- OLED Black;
- Transparent.

A visual theme picker will sit on top of this model rather than adding theme logic to the renderer.


### Physical-control hints

Utility keys display the physical button currently mapped to their action. These hints are generated from ControllerBindings, so remapping is reflected directly on the keyboard.

The minimized bar also resolves the active Confirm and Minimize buttons dynamically instead of assuming A / Select.
