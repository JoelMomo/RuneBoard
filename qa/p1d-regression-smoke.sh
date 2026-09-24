#!/usr/bin/env bash
set -euo pipefail

PACKAGE="io.github.joelmomo.runeboard"
IME="$PACKAGE/.RuneBoardImeService"
CONTROL="$PACKAGE/.RuneBoardControlService"
OUT="qa-artifacts"
mkdir -p "$OUT"

adb shell wm size 1240x1080
adb shell wm density 320
adb install -r app/build/outputs/apk/debug/app-debug.apk
for i in $(seq 1 15); do
  if adb shell ime list -a | grep -q "$IME"; then
    break
  fi
  sleep 1
done
adb shell ime list -a > "$OUT/ime-list.txt"
grep -q "$IME" "$OUT/ime-list.txt"
adb shell settings put secure show_ime_with_hard_keyboard 1
adb shell ime enable "$IME"
adb shell ime set "$IME"
adb shell settings put secure enabled_accessibility_services "$CONTROL"
adb shell settings put secure accessibility_enabled 1
adb shell input keyevent 3
sleep 1
adb shell settings get secure enabled_accessibility_services > "$OUT/accessibility-services.txt"
grep -q "$CONTROL" "$OUT/accessibility-services.txt"

launch_settings() {
  adb shell am force-stop "$PACKAGE"
  adb shell ime enable "$IME"
  adb shell ime set "$IME"
  adb shell am start -W -n "$PACKAGE/.MainActivity" >/dev/null
  sleep 2
}

focus_test_field() {
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
}

capture_ime() {
  local label="$1"
  adb shell dumpsys input_method > "$OUT/${label}-ime.txt"
  grep -q "mCurMethodId=$IME" "$OUT/${label}-ime.txt"
  grep -q "mInputShown=true" "$OUT/${label}-ime.txt"
  adb exec-out screencap -p > "$OUT/${label}.png"
}

launch_settings
adb shell uiautomator dump /sdcard/setup.xml >/dev/null
adb pull /sdcard/setup.xml "$OUT/setup.xml" >/dev/null
python3 - "$OUT/setup.xml" <<'PY'
import sys
import xml.etree.ElementTree as ET

root = ET.parse(sys.argv[1]).getroot()
texts = [node.attrib.get("text", "") for node in root.iter("node")]
if texts.count("READY") != 3:
    raise SystemExit(f"Expected exactly three READY setup states, got {texts.count('READY')}")
if "PROTOTYPE" in texts:
    raise SystemExit("Obsolete PROTOTYPE badge is visible")
PY

focus_test_field
capture_ime "default-100"

cat > "$OUT/prefs.xml" <<'EOF'
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="theme_id">default</string>
    <int name="background_opacity" value="0" />
</map>
EOF
adb shell am force-stop "$PACKAGE"
adb shell "run-as $PACKAGE mkdir -p /data/user/0/$PACKAGE/shared_prefs"
adb shell "run-as $PACKAGE sh -c 'cat > /data/user/0/$PACKAGE/shared_prefs/runeboard_preferences.xml'" < "$OUT/prefs.xml"

launch_settings
focus_test_field
capture_ime "default-0"

adb shell wm size > "$OUT/wm-size.txt"
adb shell wm density > "$OUT/wm-density.txt"
grep -q "1240x1080" "$OUT/wm-size.txt"
grep -q "320" "$OUT/wm-density.txt"

rm -f "$OUT/prefs.xml" "$OUT/window.xml"
