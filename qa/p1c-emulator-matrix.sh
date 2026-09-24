#!/usr/bin/env bash
set -euo pipefail

PACKAGE="io.github.joelmomo.runeboard"
IME="$PACKAGE/.RuneBoardImeService"
OUT="qa-artifacts"
mkdir -p "$OUT"

adb shell wm size 1240x1080
adb shell wm density 320
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell settings put secure show_ime_with_hard_keyboard 1
adb shell ime enable "$IME"

capture_case() {
  local theme="$1"
  local opacity="$2"
  local label="$3"

  cat > "$OUT/prefs.xml" <<EOF
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="theme_id">$theme</string>
    <int name="background_opacity" value="$opacity" />
$(if [[ "$theme" == "custom" ]]; then echo '    <int name="custom_accent" value="-278748" />'; fi)
</map>
EOF

  adb shell am force-stop "$PACKAGE"
  adb shell "run-as $PACKAGE mkdir -p /data/user/0/$PACKAGE/shared_prefs"
  adb shell "run-as $PACKAGE sh -c 'cat > /data/user/0/$PACKAGE/shared_prefs/runeboard_preferences.xml'" < "$OUT/prefs.xml"
  adb shell ime enable "$IME"
  adb shell ime set "$IME"
  adb shell am start -W -n "$PACKAGE/.MainActivity"
  sleep 1

  for i in 1 2 3 4 5 6 7; do
    adb shell input swipe 620 920 620 180 180
  done

  adb shell uiautomator dump /sdcard/window.xml >/dev/null
  adb pull /sdcard/window.xml "$OUT/window.xml" >/dev/null
  python3 - "$OUT/window.xml" <<'PY'
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

root = ET.parse(sys.argv[1]).getroot()
target = next(
    (node for node in root.iter("node")
     if node.attrib.get("class") == "android.widget.EditText"),
    None,
)
if target is None:
    raise SystemExit("RuneBoard test EditText not found")
match = re.fullmatch(
    r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
    target.attrib["bounds"],
)
if match is None:
    raise SystemExit("Unexpected EditText bounds")
left, top, right, bottom = map(int, match.groups())
subprocess.run(
    ["adb", "shell", "input", "tap",
     str((left + right) // 2), str((top + bottom) // 2)],
    check=True,
)
PY

  sleep 2
  adb shell dumpsys input_method > "$OUT/${label}-ime.txt"
  grep -q "mCurMethodId=$IME" "$OUT/${label}-ime.txt"
  grep -q "mInputShown=true" "$OUT/${label}-ime.txt"
  adb exec-out screencap -p > "$OUT/${label}.png"
}

for theme in default oled transparent custom; do
  for opacity in 255 170 85 0; do
    case "$opacity" in
      255) pct=100 ;;
      170) pct=67 ;;
      85) pct=33 ;;
      0) pct=0 ;;
    esac
    capture_case "$theme" "$opacity" "${theme}-${pct}"
  done
done

rm -f "$OUT/prefs.xml" "$OUT/window.xml"
