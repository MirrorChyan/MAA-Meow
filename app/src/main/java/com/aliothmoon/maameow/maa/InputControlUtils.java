package com.aliothmoon.maameow.maa;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.aliothmoon.maameow.third.Ln;
import com.aliothmoon.maameow.third.wrappers.InputManager;
import com.aliothmoon.maameow.third.wrappers.ServiceManager;


public final class InputControlUtils {

    private static final InputManager MANAGER = ServiceManager.getInputManager();

    private static final int POINTER_ID = 0;
    private static final int TOOL_TYPE = MotionEvent.TOOL_TYPE_FINGER;

    private static long currentDownTime = 0;
    private static boolean touchSessionActive = false;

    private static float lastX = 0;
    private static float lastY = 0;
    private static int lastDisplayId = 0;

    private InputControlUtils() {
    }

    private static void sendUpEventInternal(float x, float y, int displayId) {
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent motionEvent = MotionEvent.obtain(currentDownTime, eventTime,
                MotionEvent.ACTION_UP, x, y,
                0.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0
        );
        motionEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        if (!setDisplayId(motionEvent, displayId)) {
            return;
        }

        MANAGER.injectInputEvent(motionEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean setDisplayId(InputEvent event, int displayId) {
        return displayId == 0 || InputManager.setDisplayId(event, displayId);
    }

    public static boolean down(int x, int y, int displayId) {
        float pressure = 1.0f;

        // 上一次 DOWN 没有 UP，需要结束上一会话
        if (touchSessionActive) {
            Ln.w("TouchDown: 检测到未结束的触摸会话，自动发送 UP 事件");
            sendUpEventInternal(lastX, lastY, lastDisplayId);
        }

        currentDownTime = SystemClock.uptimeMillis();
        long eventTime = currentDownTime;
        touchSessionActive = true;

        lastX = x;
        lastY = y;
        lastDisplayId = displayId;

        MotionEvent motionEvent = MotionEvent.obtain(currentDownTime, eventTime,
                MotionEvent.ACTION_DOWN,
                x, y, pressure,
                1.0f, 0, 1.0f, 1.0f, 0, 0
        );
        motionEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        if (!setDisplayId(motionEvent, displayId)) {
            return false;
        }

        return MANAGER.injectInputEvent(motionEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    public static boolean move(int x, int y, int displayId) {
        float pressure = 1.0f;

        if (!touchSessionActive) {
            Ln.w("TouchMove: 没有活跃的触摸会话，忽略移动事件");
            return false;
        }

        lastX = x;
        lastY = y;

        long eventTime = SystemClock.uptimeMillis();

        MotionEvent motionEvent = MotionEvent.obtain(currentDownTime, eventTime,
                MotionEvent.ACTION_MOVE, x, y,
                pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0
        );
        motionEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);


        if (!setDisplayId(motionEvent, displayId)) {
            return false;
        }

        return MANAGER.injectInputEvent(motionEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    public static boolean up(int x, int y, int displayId) {
        if (!touchSessionActive) {
            Ln.w("TouchUp: no active session ignore this event");
            return false;
        }

        long eventTime = SystemClock.uptimeMillis();

        touchSessionActive = false;

        MotionEvent motionEvent = MotionEvent.obtain(currentDownTime, eventTime,
                MotionEvent.ACTION_UP, x, y,
                0.0f, 1.0f, 0, 1.0f, 1.0f, 0, 0
        );
        motionEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);

        if (!setDisplayId(motionEvent, displayId)) {
            return false;
        }

        return MANAGER.injectInputEvent(motionEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }


    public static boolean keyDown(int keyCode, int displayId) {
        long downTime = SystemClock.uptimeMillis();
        KeyEvent keyEvent = new KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0);

        if (!setDisplayId(keyEvent, displayId)) {
            return false;
        }

        return MANAGER.injectInputEvent(keyEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    public static boolean keyUp(int keyCode, int displayId) {
        // 按键事件可以使用独立的时间戳，因为它们不需要维护连续的会话
        long upTime = SystemClock.uptimeMillis();
        KeyEvent keyEvent = new KeyEvent(upTime, upTime, KeyEvent.ACTION_UP, keyCode, 0);

        if (!setDisplayId(keyEvent, displayId)) {
            return false;
        }

        return MANAGER.injectInputEvent(keyEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }
}
