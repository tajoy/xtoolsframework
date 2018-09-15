package x.tools.api.ime;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import x.tools.eventbus.rpc.RpcFactory;
import x.tools.framework.log.Loggable;

import static android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED;

public class MockIMService extends InputMethodService implements KeyboardView.OnKeyboardActionListener, Loggable, IMockIMServiceProxy {

    public static final String ACTION_COMMIT_TEXT = "com.bank.tools.service.MockIMService.COMMIT_TEXT";
    public static final String EXTRA_TEXT_NAME = "com.bank.tools.service.MockIMService.EXTRAS_KEY_TEXT";

    private KeyboardView keyboardView;
    private Keyboard keyboard;

    @Override
    public void onCreate() {
        super.onCreate();
        RpcFactory.registerProxyHost(IMockIMServiceProxy.class, this, MockIMService.class.getName());
    }


    @Override
    public void onDestroy() {
        RpcFactory.unregisterProxyHost(IMockIMServiceProxy.class, this, MockIMService.class.getName());
        super.onDestroy();
    }

    @Override
    public void onInitializeInterface() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

//    public void onImeEvent(EventIME event) {
//        boolean isHandled = true;
//        InputConnection inputConnection = getCurrentInputConnection();
//        EventIME eventResponse = null;
//        switch (event.getType()) {
//            case FORCE_KILL: {
//                new Handler(this.getMainLooper()).post(() -> {
//                    try {
//                        webServer.stop();
//                    } catch (Throwable t) {
//                    }
//                });
//            }
//
//            case REQ_COMMIT_TEXT:
//                if (inputConnection != null) {
//                    boolean isOk = inputConnection.commitText(event.getText(), 1);
//                    eventResponse = newEventBoolean(RES_COMMIT_TEXT, isOk);
//                } else {
//                    eventResponse = newEventBoolean(RES_COMMIT_TEXT, false);
//                }
//                break;
//            case REQ_DELETE_SURROUNDING_TEXT:
//                if (inputConnection != null) {
//                    int before = toInt(event.getBefore(), Integer.MAX_VALUE);
//                    int after = toInt(event.getAfter(), Integer.MAX_VALUE);
//                    boolean isOk = inputConnection.deleteSurroundingText(before, after);
//                    eventResponse = newEventBoolean(RES_DELETE_SURROUNDING_TEXT, isOk);
//                } else {
//                    eventResponse = newEventBoolean(RES_DELETE_SURROUNDING_TEXT, false);
//                }
//                break;
//            case REQ_PERFORM_EDITOR_ACTION: {
//                int action = toInt(event.getAction(), IME_ACTION_UNSPECIFIED);
//                if (inputConnection != null && action != IME_ACTION_UNSPECIFIED) {
//                    boolean isOk = inputConnection.performEditorAction(action);
//                    eventResponse = newEventBoolean(RES_PERFORM_EDITOR_ACTION, isOk);
//                } else {
//                    eventResponse = newEventBoolean(RES_PERFORM_EDITOR_ACTION, false);
//                }
//                break;
//            }
//            case REQ_PERFORM_CONTEXT_MENU_ACTION: {
//                int action = toInt(event.getAction(), 0);
//                if (inputConnection != null && action != 0) {
//                    boolean isOk = inputConnection.performContextMenuAction(action);
//                    eventResponse = newEventBoolean(RES_PERFORM_CONTEXT_MENU_ACTION, isOk);
//                } else {
//                    eventResponse = newEventBoolean(RES_PERFORM_CONTEXT_MENU_ACTION, false);
//                }
//                break;
//            }
//            case REQ_SET_SELECTION:
//                if (inputConnection != null) {
//                    int start = toInt(event.getStart(), 0);
//                    int end = toInt(event.getEnd(), Integer.MAX_VALUE);
//                    boolean isOk = inputConnection.setSelection(start, end);
//                    eventResponse = newEventBoolean(RES_SET_SELECTION, isOk);
//                } else {
//                    eventResponse = newEventBoolean(RES_SET_SELECTION, false);
//                }
//                break;
//            case REQ_GET_SELECTION: {
//                if (inputConnection != null) {
//                    eventResponse = newEventText(RES_GET_SELECTION, inputConnection.getSelectedText(0).toString());
//                } else {
//                    eventResponse = newEventText(RES_GET_SELECTION, null);
//                }
//                break;
//            }
//            case REQ_IS_READY_TO_INPUT: {
//                boolean isInputStarted = getCurrentInputStarted();
//                boolean isShowInputRequested = this.isShowInputRequested();
//                boolean isInputViewShown = this.isInputViewShown();
//                debug("[REQ_IS_READY_TO_INPUT] checking:"
//                                + "inputConnection: %s, "
//                                + "isInputStarted: %s, "
//                                + "isShowInputRequested: %s, "
//                                + "isInputViewShown: %s",
//                        inputConnection, isInputStarted, isShowInputRequested, isInputViewShown);
//                if (inputConnection != null) {
//                    eventResponse = newEventBoolean(RES_IS_READY_TO_INPUT, isInputStarted && isShowInputRequested && isInputViewShown);
//                } else {
//                    eventResponse = newEventBoolean(RES_IS_READY_TO_INPUT, false);
//                }
//                break;
//            }
//            default:
//                isHandled = false;
//                break;
//        }
//        if (eventResponse != null) {
//            postEvent(eventResponse);
//        }
//        if (isHandled)
//            debug("[EventIME] MockIMService -> %s ===> %s", event, eventResponse);
//    }


    @Override
    public View onCreateExtractTextView() {
        return super.onCreateExtractTextView();
    }

    @Override
    public View onCreateCandidatesView() {
        return null;
    }

    @Override
    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.mock_input_method, null);
        keyboard = new Keyboard(this, R.xml.empty);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
//        postEvent(newEventEditorInfo(ON_START_INPUT_VIEW, info, restarting));
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onBindInput() {
    }

    @Override
    public void onUnbindInput() {
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }


    /****************************************************************************************/
    /********************************* API RPC METHOD ***************************************/
    /****************************************************************************************/

    @Override
    public boolean setSelection(int start, int end) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return false;
        }
        return inputConnection.setSelection(start, end);
    }

    @Override
    public String getSelection() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return null;
        }
        CharSequence ret = inputConnection.getSelectedText(0);
        return ret == null ? null : ret.toString();
    }

    @Override
    public boolean deleteSurroundingText(int before, int after) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return false;
        }
        return inputConnection.deleteSurroundingText(before, after);
    }

    @Override
    public boolean commitText(String text) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return false;
        }
        return inputConnection.commitText(text, 1);
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return false;
        }
        return inputConnection.performEditorAction(editorAction);
    }

    @Override
    public boolean performContextMenuAction(int menuAction) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return false;
        }
        return inputConnection.performContextMenuAction(menuAction);
    }

    @Override
    public boolean canInput() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return false;
        }
        boolean isInputStarted = getCurrentInputStarted();
        boolean isShowInputRequested = this.isShowInputRequested();
        boolean isInputViewShown = this.isInputViewShown();
        return isInputStarted && isShowInputRequested && isInputViewShown;
    }

    @Override
    public boolean inputText(String text) {
        if (!setSelection(0, 0)) return false;
        if (!deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE)) return false;
        return commitText(text);
    }
}
