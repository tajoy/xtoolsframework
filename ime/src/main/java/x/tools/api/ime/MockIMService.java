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

import x.tools.framework.log.Loggable;

import static android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED;

public class MockIMService extends InputMethodService implements KeyboardView.OnKeyboardActionListener, Loggable {

    public static final String ACTION_COMMIT_TEXT = "com.bank.tools.service.MockIMService.COMMIT_TEXT";
    public static final String EXTRA_TEXT_NAME = "com.bank.tools.service.MockIMService.EXTRAS_KEY_TEXT";

    private KeyboardView keyboardView;
    private Keyboard keyboard;

    private static MockIMService instance;

    public static MockIMService getInstance() {
        return instance;
    }

    private WebServer webServer = null;
    public static int PORT_IME_SERVICE = 14301;
    private final ImeEventHandler imeEventHandler = new ImeEventHandler();
    private final ConcurrentLinkedQueue<EventIME> queueSendEvent = new ConcurrentLinkedQueue<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (webServer == null) {
            webServer = new WebServer(PORT_IME_SERVICE);
            imeEventHandler.regOnServer(webServer);
            imeEventHandler.eventHandler = this::onImeEvent;

            new Thread(() -> {
                boolean isSendForceKill;
                do {
                    isSendForceKill = false;
                    try {
                        webServer.start();
                    } catch (IOException e) {
                        isSendForceKill = true;
                    }
                    if (isSendForceKill) {
                        ImeEventParam param = new ImeEventParam();
                        param.event = new EventIME(FORCE_KILL);
                        debug("send FORCE_KILL to other imeService: %s", param);
                        imeEventHandler.sendTo(PORT_IME_SERVICE, param);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                        }
                    }
                } while(isSendForceKill);
            }).start();

        }
        postEvent(newEvent(ON_CREATE));

        if (sendThread == null) {
            sendThread = new SendThread();
            sendThread.start();
        }
    }

    private SendThread sendThread = null;

    private class SendThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    EventIME e = queueSendEvent.poll();
                    if (e == null) {
                        Thread.sleep(10);
                        continue;
                    }
                    ImeEventParam param = new ImeEventParam();
                    param.event = e;
                    debug("send to %s", param);
                    imeEventHandler.sendTo(PORT_BASE_SERVICE, param);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }


    void postEvent(EventIME event) {
        try {
            queueSendEvent.offer(event);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    @Override
    public void onInitializeInterface() {
        postEvent(newEvent(ON_INIT_IFACE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_COMMIT_TEXT.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            String text = extras.getString(EXTRA_TEXT_NAME);
            if (text != null) {
                InputConnection inputConnection = getCurrentInputConnection();
                if (inputConnection != null) {
                    inputConnection.deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    inputConnection.commitText(text, 0);
                }
            }
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    public static void commitText(Context context, String text) {
        Intent intent = new Intent(context, MockIMService.class);
        intent.setAction(ACTION_COMMIT_TEXT);
        intent.putExtra(EXTRA_TEXT_NAME, text);
        context.startService(intent);
    }


    private static int toInt(Object num, int defaultNum) {
        try {
            if (num != null) {
                if (num instanceof Integer)
                    return (Integer) num;

                if (num instanceof Long)
                    return ((Long) num).intValue();

                if (num instanceof Float)
                    return ((Float) num).intValue();

                if (num instanceof Double)
                    return ((Double) num).intValue();
            }
        } catch (Throwable t) {
            return defaultNum;
        }
        return defaultNum;
    }

    public void onImeEvent(EventIME event) {
        boolean isHandled = true;
        InputConnection inputConnection = getCurrentInputConnection();
        EventIME eventResponse = null;
        switch (event.getType()) {
            case FORCE_KILL: {
                new Handler(this.getMainLooper()).post(() -> {
                    try {
                        webServer.stop();
                    } catch (Throwable t) {
                    }
                });
            }

            case REQ_COMMIT_TEXT:
                if (inputConnection != null) {
                    boolean isOk = inputConnection.commitText(event.getText(), 1);
                    eventResponse = newEventBoolean(RES_COMMIT_TEXT, isOk);
                } else {
                    eventResponse = newEventBoolean(RES_COMMIT_TEXT, false);
                }
                break;
            case REQ_DELETE_SURROUNDING_TEXT:
                if (inputConnection != null) {
                    int before = toInt(event.getBefore(), Integer.MAX_VALUE);
                    int after = toInt(event.getAfter(), Integer.MAX_VALUE);
                    boolean isOk = inputConnection.deleteSurroundingText(before, after);
                    eventResponse = newEventBoolean(RES_DELETE_SURROUNDING_TEXT, isOk);
                } else {
                    eventResponse = newEventBoolean(RES_DELETE_SURROUNDING_TEXT, false);
                }
                break;
            case REQ_PERFORM_EDITOR_ACTION: {
                int action = toInt(event.getAction(), IME_ACTION_UNSPECIFIED);
                if (inputConnection != null && action != IME_ACTION_UNSPECIFIED) {
                    boolean isOk = inputConnection.performEditorAction(action);
                    eventResponse = newEventBoolean(RES_PERFORM_EDITOR_ACTION, isOk);
                } else {
                    eventResponse = newEventBoolean(RES_PERFORM_EDITOR_ACTION, false);
                }
                break;
            }
            case REQ_PERFORM_CONTEXT_MENU_ACTION: {
                int action = toInt(event.getAction(), 0);
                if (inputConnection != null && action != 0) {
                    boolean isOk = inputConnection.performContextMenuAction(action);
                    eventResponse = newEventBoolean(RES_PERFORM_CONTEXT_MENU_ACTION, isOk);
                } else {
                    eventResponse = newEventBoolean(RES_PERFORM_CONTEXT_MENU_ACTION, false);
                }
                break;
            }
            case REQ_SET_SELECTION:
                if (inputConnection != null) {
                    int start = toInt(event.getStart(), 0);
                    int end = toInt(event.getEnd(), Integer.MAX_VALUE);
                    boolean isOk = inputConnection.setSelection(start, end);
                    eventResponse = newEventBoolean(RES_SET_SELECTION, isOk);
                } else {
                    eventResponse = newEventBoolean(RES_SET_SELECTION, false);
                }
                break;
            case REQ_GET_SELECTION: {
                if (inputConnection != null) {
                    eventResponse = newEventText(RES_GET_SELECTION, inputConnection.getSelectedText(0).toString());
                } else {
                    eventResponse = newEventText(RES_GET_SELECTION, null);
                }
                break;
            }
            case REQ_IS_READY_TO_INPUT: {
                boolean isInputStarted = getCurrentInputStarted();
                boolean isShowInputRequested = this.isShowInputRequested();
                boolean isInputViewShown = this.isInputViewShown();
                debug("[REQ_IS_READY_TO_INPUT] checking:"
                                + "inputConnection: %s, "
                                + "isInputStarted: %s, "
                                + "isShowInputRequested: %s, "
                                + "isInputViewShown: %s",
                        inputConnection, isInputStarted, isShowInputRequested, isInputViewShown);
                if (inputConnection != null) {
                    eventResponse = newEventBoolean(RES_IS_READY_TO_INPUT, isInputStarted && isShowInputRequested && isInputViewShown);
                } else {
                    eventResponse = newEventBoolean(RES_IS_READY_TO_INPUT, false);
                }
                break;
            }
            default:
                isHandled = false;
                break;
        }
        if (eventResponse != null) {
            postEvent(eventResponse);
        }
        if (isHandled)
            debug("[EventIME] MockIMService -> %s ===> %s", event, eventResponse);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        postEvent(newEvent(ON_DESTROY));
        instance = null;
        webServer.stop();
    }

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
        postEvent(newEvent(ON_CREATE_INPUT_VIEW));
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.input_method, null);
        keyboard = new Keyboard(this, R.xml.empty);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        postEvent(newEventEditorInfo(ON_START_INPUT_VIEW, info, restarting));
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        postEvent(newEventBoolean(ON_FINISH_INPUT_VIEW, finishingInput));
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onBindInput() {
        postEvent(newEvent(ON_BIND_INPUT));
    }

    @Override
    public void onUnbindInput() {
        postEvent(newEvent(ON_UNBIND_INPUT));
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        postEvent(newEventEditorInfo(ON_START_INPUT, attribute, restarting));
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        postEvent(newEvent(ON_FINISH_INPUT));
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

}
