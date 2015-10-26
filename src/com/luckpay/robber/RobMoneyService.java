package com.luckpay.robber;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class RobMoneyService extends AccessibilityService {
    private static final String HONGBAO_TEXT_KEY = "[微信红包]";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
        case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence text : texts) {
                    String content = text.toString();
                    if (content.contains(HONGBAO_TEXT_KEY)) {
                        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                            Notification notification = (Notification) event.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            try {
                                pendingIntent.send();
                            } catch (CanceledException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            break;
        case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            String className = event.getClassName().toString();
            if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                getPacket();
            } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                openPacket();
            }
            break;
        }
    }

    @SuppressLint("NewApi")
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
            for (AccessibilityNodeInfo n : list) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }

    @SuppressLint("NewApi")
    private void getPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
            if (list == null) {
                return;
            }
            if (list.isEmpty()) {
                list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
                for (AccessibilityNodeInfo n : list) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            } else {
                for (int i = list.size() - 1; i >= 0; i--) {
                    AccessibilityNodeInfo parent = list.get(i).getParent();
                    if (parent != null) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

}
