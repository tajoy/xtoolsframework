package x.tools.api.ime;

public interface IMockIMServiceProxy {

    boolean setSelection(int start, int end);

    String getSelection();

    boolean deleteSurroundingText(int before, int after);

    boolean commitText(String text);

    boolean performEditorAction(int editorAction);

    boolean performContextMenuAction(int menuAction);

    boolean canInput();

    boolean inputText(String text);
}
