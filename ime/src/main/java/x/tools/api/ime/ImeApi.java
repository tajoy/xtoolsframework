package x.tools.api.ime;

import x.tools.eventbus.rpc.RpcFactory;
import x.tools.framework.XContext;
import x.tools.framework.annotation.ApiConstant;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.error.XError;

public class ImeApi extends AbstractApi implements IMockIMServiceProxy {

    @ApiConstant
    public final int EDITOR_ACTION_GO = 0x00000002;

    @ApiConstant
    public final int EDITOR_ACTION_SEARCH = 0x00000003;

    @ApiConstant
    public final int EDITOR_ACTION_SEND = 0x00000004;

    @ApiConstant
    public final int EDITOR_ACTION_NEXT = 0x00000005;

    @ApiConstant
    public final int EDITOR_ACTION_DONE = 0x00000006;

    @ApiConstant
    public final int EDITOR_ACTION_PREVIOUS = 0x00000007;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_SELECT_ALL = android.R.id.selectAll;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_START_SELECTING_TEXT = android.R.id.startSelectingText;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_STOP_SELECTING_TEXT = android.R.id.stopSelectingText;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_CUT = android.R.id.cut;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_COPY = android.R.id.copy;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_PASTE = android.R.id.paste;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_COPY_URL = android.R.id.copyUrl;

    @ApiConstant
    public final int CONTEXT_MENU_ACTION_SWITCH_INPUT_METHOD = android.R.id.switchInputMethod;


    @Override
    public String getNamespace() {
        return "ime";
    }

    private IMockIMServiceProxy proxy;

    @Override
    public boolean initialize(XContext xContext) throws XError {
        if (!super.initialize(xContext)) return false;
        String process = xContext.getPackageName() + ":mock-ime-api-service";
        proxy = RpcFactory.getProxy(IMockIMServiceProxy.class, process, MockIMService.class.getName());
        if (proxy == null)
            return false;
        return true;
    }

    @Override
    public boolean setSelection(int start, int end) {
        return proxy.setSelection(start, end);
    }

    @Override
    public String getSelection() {
        return proxy.getSelection();
    }

    @Override
    public boolean deleteSurroundingText(int before, int after) {
        return proxy.deleteSurroundingText(before, after);
    }

    @Override
    public boolean commitText(String text) {
        return proxy.commitText(text);
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        return proxy.performEditorAction(editorAction);
    }

    @Override
    public boolean performContextMenuAction(int menuAction) {
        return proxy.performContextMenuAction(menuAction);
    }

    @Override
    public boolean canInput() {
        return proxy.canInput();
    }

    @Override
    public boolean inputText(String text) {
        return proxy.inputText(text);
    }
}
