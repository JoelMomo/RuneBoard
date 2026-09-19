package io.github.joelmomo.runeboard.editor;

public final class EditorActionSpec {

    public enum Kind {
        ENTER,
        GO,
        SEARCH,
        SEND,
        NEXT,
        DONE,
        PREVIOUS,
        CUSTOM
    }

    private final Kind kind;
    private final int actionId;
    private final String customLabel;

    private EditorActionSpec(
            Kind kind,
            int actionId,
            String customLabel) {
        this.kind = kind;
        this.actionId = actionId;
        this.customLabel = customLabel;
    }

    public static EditorActionSpec enter() {
        return new EditorActionSpec(Kind.ENTER, 0, null);
    }

    public static EditorActionSpec standard(
            Kind kind,
            int actionId) {
        if (kind == null
                || kind == Kind.ENTER
                || kind == Kind.CUSTOM) {
            throw new IllegalArgumentException(
                    "Standard editor action requires a standard kind");
        }
        if (actionId <= 0) {
            throw new IllegalArgumentException(
                    "Editor action id must be positive");
        }
        return new EditorActionSpec(kind, actionId, null);
    }

    public static EditorActionSpec custom(
            int actionId,
            String label) {
        if (actionId <= 0) {
            throw new IllegalArgumentException(
                    "Custom editor action id must be positive");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "Custom editor action label cannot be blank");
        }
        return new EditorActionSpec(
                Kind.CUSTOM,
                actionId,
                label.trim());
    }

    public Kind kind() {
        return kind;
    }

    public int actionId() {
        return actionId;
    }

    public String customLabel() {
        return customLabel;
    }

    public boolean performsEditorAction() {
        return kind != Kind.ENTER;
    }
}
