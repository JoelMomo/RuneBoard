package io.github.joelmomo.runeboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class LocalizationResourcesTest {

  private static final Pattern PLACEHOLDER = Pattern.compile("%\\d+\\$[sd]");

  @Test
  public void spanishCoversEveryTranslatableSourceStringWithMatchingPlaceholders() throws Exception {
    Map<String, ResourceString> source = readStrings(resolveRes("values/strings.xml"));
    Map<String, ResourceString> spanish = readStrings(resolveRes("values-es/strings.xml"));

    Map<String, ResourceString> translatable = new LinkedHashMap<>();
    for (Map.Entry<String, ResourceString> entry : source.entrySet()) {
      if (entry.getValue().translatable) {
        translatable.put(entry.getKey(), entry.getValue());
      }
    }

    assertEquals(translatable.keySet(), spanish.keySet());
    for (Map.Entry<String, ResourceString> entry : translatable.entrySet()) {
      ResourceString translated = spanish.get(entry.getKey());
      assertNotNull("Missing Spanish string " + entry.getKey(), translated);
      assertEquals(
          "Placeholder mismatch for " + entry.getKey(),
          placeholders(entry.getValue().value),
          placeholders(translated.value));
    }
  }

  @Test
  public void fixedBrandAndTypefaceNamesRemainCanonical() throws Exception {
    Map<String, ResourceString> source = readStrings(resolveRes("values/strings.xml"));

    assertEquals("RuneBoard", source.get("app_name").value);
    assertEquals("RuneBoard", source.get("theme_default_title").value);
    assertEquals("Inter", source.get("font_inter").value);
    assertEquals("Atkinson Hyperlegible", source.get("font_atkinson").value);
    assertEquals("JetBrains Mono", source.get("font_jetbrains_mono").value);
    assertEquals("Space Grotesk", source.get("font_space_grotesk").value);
    assertEquals("MedievalSharp", source.get("font_medieval_sharp").value);
  }

  private static Path resolveRes(String relative) {
    Path cwd = Path.of(System.getProperty("user.dir"));
    Path modulePath = cwd.resolve("src/main/res").resolve(relative);
    if (Files.exists(modulePath)) {
      return modulePath;
    }
    Path rootPath = cwd.resolve("app/src/main/res").resolve(relative);
    if (Files.exists(rootPath)) {
      return rootPath;
    }
    throw new AssertionError("Cannot locate Android resources from " + cwd);
  }

  private static Map<String, ResourceString> readStrings(Path path) throws Exception {
    File file = path.toFile();
    Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement();
    NodeList children = root.getChildNodes();
    Map<String, ResourceString> result = new LinkedHashMap<>();
    for (int index = 0; index < children.getLength(); index++) {
      Node node = children.item(index);
      if (!(node instanceof Element)) {
        continue;
      }
      Element element = (Element) node;
      if (!"string".equals(element.getTagName())) {
        continue;
      }
      String name = element.getAttribute("name");
      boolean translatable = !"false".equals(element.getAttribute("translatable"));
      result.put(name, new ResourceString(element.getTextContent(), translatable));
    }
    return result;
  }

  private static List<String> placeholders(String value) {
    Matcher matcher = PLACEHOLDER.matcher(value);
    List<String> placeholders = new ArrayList<>();
    while (matcher.find()) {
      placeholders.add(matcher.group());
    }
    Collections.sort(placeholders);
    return placeholders;
  }

  private static final class ResourceString {
    final String value;
    final boolean translatable;

    ResourceString(String value, boolean translatable) {
      this.value = value;
      this.translatable = translatable;
    }
  }
}