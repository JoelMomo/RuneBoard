package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import io.github.joelmomo.runeboard.controller.ControllerAction;
import io.github.joelmomo.runeboard.language.KeyboardProfile;
import io.github.joelmomo.runeboard.language.KeyboardProfiles;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class MobileLayoutTest {

  @Test
  public void everyProfileHasACompactBarAndFourTypingRows() {
    for (KeyboardProfile profile : KeyboardProfiles.builtIns()) {
      KeyboardLayout layout = profile.mobileLayout;
      assertEquals(profile.id, 5, layout.getRowCount());

      KeyboardRow bar = layout.getRow(0);
      assertTrue(bar.getHeightWeight() < 1f);
      for (int col = 0; col < bar.size(); col++) {
        KeyboardKey.Type type = bar.getKey(col).getType();
        assertTrue(type == KeyboardKey.Type.COMMAND || type == KeyboardKey.Type.EDIT);
      }

      assertEquals(KeyboardKey.Type.SHIFT, layout.getKey(3, 0).getType());
      assertEquals(
          KeyboardKey.Type.BACKSPACE, layout.getKey(3, layout.getRow(3).size() - 1).getType());

      KeyboardRow actions = layout.getRow(4);
      assertEquals(KeyboardKey.Type.MODE, actions.getKey(0).getType());
      assertEquals(KeyboardKey.Type.SPACE, actions.getKey(2).getType());
      assertEquals(KeyboardKey.Type.ENTER, actions.getKey(actions.size() - 1).getType());
    }
  }

  @Test
  public void compactBarKeepsEditingButDropsThorOnlyControls() {
    List<KeyboardKey.Type> barTypes = new ArrayList<>();
    List<EditorCommand> commands = new ArrayList<>();
    KeyboardRow bar = KeyboardLayouts.mobileSpanishQwerty().getRow(0);
    for (int col = 0; col < bar.size(); col++) {
      barTypes.add(bar.getKey(col).getType());
      commands.add(bar.getKey(col).getCommand());
    }

    assertTrue(commands.contains(EditorCommand.COPY));
    assertTrue(commands.contains(EditorCommand.PASTE));
    assertTrue(commands.contains(EditorCommand.UNDO));
    assertTrue(commands.contains(EditorCommand.REDO));
    assertTrue(barTypes.contains(KeyboardKey.Type.EDIT));
    assertFalse(barTypes.contains(KeyboardKey.Type.OPACITY));
    assertFalse(barTypes.contains(KeyboardKey.Type.MINIMIZE));
    // The space bar drag moves the cursor, so the bar leaves room for wider keys.
    assertFalse(commands.contains(EditorCommand.CURSOR_LEFT));
    assertFalse(commands.contains(EditorCommand.CURSOR_RIGHT));
    assertEquals(5, bar.size());
  }

  @Test
  public void spanishMobileKeepsEnyeAsADedicatedKey() {
    KeyboardLayout layout =
        KeyboardProfiles.byId(KeyboardProfiles.ID_ES_ES).mobileLayout;
    KeyboardRow home = layout.getRow(2);
    assertEquals(10, home.size());
    assertEquals("ñ", home.getKey(9).getText());
    assertEquals("Ñ", shiftedText(layout, 2, 9));
  }

  @Test
  public void accentsStayOnLongPressForMobileLetters() {
    KeyboardLayout layout = KeyboardLayouts.mobileSpanishQwerty();
    java.util.Locale spanish = java.util.Locale.forLanguageTag("es-ES");
    for (String vowel : new String[] {"a", "e", "i", "o", "u"}) {
      KeyboardKey key = findText(layout, vowel);
      assertFalse(vowel, KeyVariants.forKey(key.getText(), spanish, false).isEmpty());
    }
    assertEquals("ú", KeyVariants.forKey("u", spanish, false).get(0));
  }

  @Test
  public void mobileSymbolsKeepDigitsLanguageExtrasAndBackspace() {
    KeyboardLayout symbols =
        KeyboardProfiles.byId(KeyboardProfiles.ID_ES_ES).mobileSymbolLayout;
    assertEquals("1", symbols.getKey(1, 0).getText());
    assertEquals("0", symbols.getKey(1, 9).getText());
    assertEquals("¿", findText(symbols, "¿").getText());
    assertEquals("¡", findText(symbols, "¡").getText());
    assertEquals(
        KeyboardKey.Type.BACKSPACE,
        symbols.getKey(4, symbols.getRow(4).size() - 1).getType());
    assertEquals(KeyboardKey.Type.MODE, symbols.getKey(5, 0).getType());
  }

  @Test
  public void symbolsKeepTheSameTotalHeightAsLetters() {
    assertEquals(
        KeyboardLayouts.mobileSpanishQwerty().getTotalHeightWeight(),
        KeyboardLayouts.mobileSpanishSymbols().getTotalHeightWeight(),
        0.25f);
  }

  @Test
  public void profilesKeepTheirThorLayoutsUntouched() {
    for (KeyboardProfile profile : KeyboardProfiles.builtIns()) {
      assertEquals(6, profile.layout.getRowCount());
      assertEquals(6, profile.symbolLayout.getRowCount());
      assertEquals(KeyboardKey.Type.OPACITY, profile.layout.getKey(0, 6).getType());
      assertEquals(KeyboardKey.Type.MINIMIZE, profile.layout.getKey(0, 7).getType());
      assertNotSame(profile.layout, profile.mobileLayout);
    }
    assertSameShape(
        KeyboardLayouts.spanishQwerty(),
        KeyboardProfiles.byId(KeyboardProfiles.ID_ES_ES).layout);
  }

  @Test
  public void contextualEnterKeyRunsTheEditorActionOnlyWhenEnabled() {
    KeyboardLayout layout = KeyboardLayouts.mobileEnglishQwerty();
    RecordingOutput output = new RecordingOutput();
    KeyboardEngine engine =
        new KeyboardEngine(layout, layout, output, 255, java.util.Locale.ROOT);
    int enterCol = layout.getRow(4).size() - 1;

    engine.select(4, enterCol);
    engine.pressSelected();
    assertEquals(List.of("\n"), output.text);
    assertEquals(0, output.enterCount);

    engine.setContextualEnterKey(true);
    engine.pressSelected();
    assertEquals(List.of("\n"), output.text);
    assertEquals(1, output.enterCount);
  }

  @Test
  public void initialSelectionSkipsTheCompactBar() {
    KeyboardLayout layout = KeyboardLayouts.mobileSpanishQwerty();
    KeyboardEngine engine =
        new KeyboardEngine(layout, layout, new RecordingOutput(), 255, java.util.Locale.ROOT);
    assertEquals(1, engine.getState().getSelectedRow());
    assertEquals(KeyboardKey.Type.TEXT, engine.getState().getSelectedKey().getType());
  }

  private static void assertSameShape(KeyboardLayout expected, KeyboardLayout actual) {
    assertEquals(expected.getRowCount(), actual.getRowCount());
    for (int row = 0; row < expected.getRowCount(); row++) {
      KeyboardRow expectedRow = expected.getRow(row);
      KeyboardRow actualRow = actual.getRow(row);
      assertEquals(expectedRow.size(), actualRow.size());
      assertEquals(expectedRow.getHeightWeight(), actualRow.getHeightWeight(), 0f);
      assertEquals(expectedRow.getLeftInsetWeight(), actualRow.getLeftInsetWeight(), 0f);
      for (int col = 0; col < expectedRow.size(); col++) {
        KeyboardKey expectedKey = expectedRow.getKey(col);
        KeyboardKey actualKey = actualRow.getKey(col);
        assertEquals(expectedKey.getType(), actualKey.getType());
        assertEquals(expectedKey.getText(), actualKey.getText());
        assertEquals(expectedKey.getWeight(), actualKey.getWeight(), 0f);
      }
    }
  }

  private static KeyboardKey findText(KeyboardLayout layout, String text) {
    for (int row = 0; row < layout.getRowCount(); row++) {
      for (int col = 0; col < layout.getRow(row).size(); col++) {
        KeyboardKey key = layout.getKey(row, col);
        if (text.equals(key.getText())) {
          return key;
        }
      }
    }
    throw new AssertionError("Missing key " + text);
  }

  private static String shiftedText(KeyboardLayout layout, int row, int col) {
    RecordingOutput output = new RecordingOutput();
    KeyboardEngine engine =
        new KeyboardEngine(layout, layout, output, 255, java.util.Locale.forLanguageTag("es-ES"));
    engine.getState().select(row, col);
    engine.handle(ControllerAction.SHIFT);
    engine.handle(ControllerAction.PRESS_SELECTED);
    return output.text.get(0);
  }

  private static final class RecordingOutput implements KeyboardEngine.Output {
    final List<String> text = new ArrayList<>();
    int enterCount;

    @Override
    public void onText(String value) {
      text.add(value);
    }

    @Override
    public void onBackspace() {}

    @Override
    public void onSpace() {}

    @Override
    public void onEnter() {
      enterCount++;
    }

    @Override
    public void onMoveCursor(int direction) {}

    @Override
    public void onMoveWord(int direction) {}

    @Override
    public void onNextLanguage() {}

    @Override
    public void onAcceptSuggestion() {}

    @Override
    public void onEditorCommand(EditorCommand command) {}

    @Override
    public void onMinimizedChanged(boolean minimized) {}

    @Override
    public void onBackgroundOpacityChanged(int opacity) {}
  }
}
