package com.hornetincorporation.beatroute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ReceiveAlarm extends BroadcastReceiver {
//Add name
    private static final String NAME = "";
    private static volatile PowerManager.WakeLock lockStatic = null;
    private static PowerManager.WakeLock lock;

    // Needed since network will to work when device is sleeping.
    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAME);
            lockStatic.setReferenceCounted(true);
        }

        return (lockStatic);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // PullPendingRequests.acquireStaticLock(context)
        try {
            lock = getLock(context);
            lock.acquire();
            context.startService(new Intent(context, TrackBeatRoute.class));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } finally {
            if (lock.isHeld()) {
                lock.release();
            }
        }
    }
}