package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class SelectionControllerTest {

    @Test
    public void extendsCollapsedSelectionByCharacters() {
        SelectionController controller = new SelectionController();

        SelectionController.Range left =
                controller.extend("alpha", 3, 3, -1, false);
        assertEquals(3, left.anchor);
        assertEquals(2, left.caret);

        SelectionController.Range fartherLeft =
                controller.extend("alpha", 2, 3, -1, false);
        assertEquals(3, fartherLeft.anchor);
        assertEquals(1, fartherLeft.caret);
    }

    @Test
    public void canReverseDirectionAcrossAnchor() {
        SelectionController controller = new SelectionController();

        SelectionController.Range left =
                controller.extend("alpha", 2, 2, -1, false);
        assertEquals(2, left.anchor);
        assertEquals(1, left.caret);

        SelectionController.Range right =
                controller.extend("alpha", 1, 2, 1, false);
        assertEquals(2, right.anchor);
        assertEquals(2, right.caret);

        SelectionController.Range fartherRight =
                controller.extend("alpha", 2, 2, 1, false);
        assertEquals(2, fartherRight.anchor);
        assertEquals(3, fartherRight.caret);
    }

    @Test
    public void extendsExistingSelectionFromRequestedEdge() {
        SelectionController controller = new SelectionController();

        SelectionController.Range left =
                controller.extend("abcdef", 2, 4, -1, false);
        assertEquals(4, left.anchor);
        assertEquals(1, left.caret);

        controller.reset();

        SelectionController.Range right =
                controller.extend("abcdef", 2, 4, 1, false);
        assertEquals(2, right.anchor);
        assertEquals(5, right.caret);
    }

    @Test
    public void extendsSelectionByWordBoundaries() {
        SelectionController controller = new SelectionController();

        SelectionController.Range left =
                controller.extend("alpha beta", 10, 10, -1, true);
        assertEquals(10, left.anchor);
        assertEquals(6, left.caret);

        SelectionController.Range fartherLeft =
                controller.extend("alpha beta", 6, 10, -1, true);
        assertEquals(10, fartherLeft.anchor);
        assertEquals(0, fartherLeft.caret);
    }

    @Test
    public void characterSelectionKeepsSurrogatePairsIntact() {
        SelectionController controller = new SelectionController();
        String text = "a\uD83D\uDE42b";

        SelectionController.Range left =
                controller.extend(text, 3, 3, -1, false);
        assertEquals(3, left.anchor);
        assertEquals(1, left.caret);

        controller.reset();

        SelectionController.Range right =
                controller.extend(text, 1, 1, 1, false);
        assertEquals(1, right.anchor);
        assertEquals(3, right.caret);
    }

    @Test
    public void clampsAtTextBoundaries() {
        SelectionController controller = new SelectionController();

        SelectionController.Range left =
                controller.extend("abc", 0, 0, -1, false);
        assertEquals(0, left.anchor);
        assertEquals(0, left.caret);

        controller.reset();

        SelectionController.Range right =
                controller.extend("abc", 3, 3, 1, false);
        assertEquals(3, right.anchor);
        assertEquals(3, right.caret);
    }

    @Test
    public void externalSelectionChangeStartsNewAnchor() {
        SelectionController controller = new SelectionController();

        controller.extend("abcdef", 4, 4, -1, false);

        SelectionController.Range changed =
                controller.extend("abcdef", 1, 3, 1, false);

        assertEquals(1, changed.anchor);
        assertEquals(4, changed.caret);
    }
}
