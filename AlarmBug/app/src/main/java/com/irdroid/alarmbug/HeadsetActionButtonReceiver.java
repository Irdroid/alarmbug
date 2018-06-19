package com.irdroid.alarmbug;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Broadcast receiver which receives headset action button events.
 * For this to work, you need to add the following in your manifest,
 * by replacing .receivers.HeadsetActionButtonReceiver with the relative
 * class path of where you will put this file:
 *   <receiver android:name=".receivers.HeadsetActionButtonReceiver" >
 *     <intent-filter android:priority="10000" >
 *       <action android:name="android.intent.action.MEDIA_BUTTON" />
 *     </intent-filter>
 *   </receiver>
 *
 * Then, in the activity in which you are going to use it:
 * - implement HeadsetActionButtonReceiver.Delegate methods
 * - in the onResume add:
 *   HeadsetActionButtonReceiver.delegate = this;
 *   HeadsetActionButtonReceiver.register(this);
 * - in the onPause add:
 *   HeadsetActionButtonReceiver.unregister(this);
 * And that's all.
 * @author gotev Aleksandar Gotev
 */
public class HeadsetActionButtonReceiver extends BroadcastReceiver {

    public static Delegate delegate;

    private static AudioManager mAudioManager;
    private static ComponentName mRemoteControlResponder;

    private static int doublePressSpeed = 300; // double keypressed in ms
    private static Timer doublePressTimer;
    private static int counter;

    public interface Delegate {
        void onMediaButtonSingleClick();
        void onMediaButtonDoubleClick();
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent == null || delegate == null || !Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()))
            return;

        KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
        if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_UP) return;
System.out.println("Got it");

        delegate.onMediaButtonSingleClick();
    }

    public static void register(final Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(context, HeadsetActionButtonReceiver.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAudioManager.registerMediaButtonEventReceiver(mRemoteControlResponder);
        }
    }

    public static void unregister(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
        }
        if (doublePressTimer != null) {
            doublePressTimer.cancel();
            doublePressTimer = null;
        }
    }
}